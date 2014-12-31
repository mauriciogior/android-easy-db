package com.mauriciogiordano.easydb.bean;

import android.content.Context;
import android.content.SharedPreferences;

import com.mauriciogiordano.easydb.helper.JSONArray;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mauricio on 12/7/14.
 */
public abstract class AbstractBean<T> {

    protected Class<T> clazz;
    protected Context context;
    protected boolean cache;

    private List<T> cachedObjects = null;

    private enum Fields {
        INT,
        LONG,
        FLOAT,
        DOUBLE,
        STRING,
        BOOLEAN
    }

    private final List<String> allowedFields = initAllowedFields();
    private static List<Class<?>> evaluated = new ArrayList<Class<?>>();

    private final List<String> initAllowedFields() {
        List<String> list = new ArrayList<String>();

        list.add("int");
        list.add("integer");
        list.add("long");
        list.add("float");
        list.add("double");
        list.add("boolean");
        list.add("string");

        return list;
    }

    private void evaluateObject() {
        if(evaluated.contains(clazz)) {
            return;
        }

        Field[] fields = clazz.getDeclaredFields();

        for(int i=0; i<fields.length; i++) {
            if(!allowedFields.contains(fields[i].getType().getSimpleName().toLowerCase())) {
                throw new RuntimeException("Field '" + fields[i].getName() + "' has type '" + fields[i].getType().getSimpleName() + "' that is not allowed!");
            }
        }

        evaluated.add(clazz);
    }

    public AbstractBean(Class<T> clazz, boolean cache) {
        this.clazz = clazz;
        this.cache = cache;

        evaluateObject();
    }

    public AbstractBean(Class<T> clazz, boolean cache, Context context) {
        this.clazz = clazz;
        this.context = context;
        this.cache = cache;

        evaluateObject();
    }

    private static Fields toFieldEnum(String field) {
        if(field.equals("int") || field.equals("integer")) {
            return Fields.INT;
        } else if(field.equals("long")) {
            return Fields.LONG;
        } else if(field.equals("float")) {
            return Fields.FLOAT;
        } else if(field.equals("double")) {
            return Fields.DOUBLE;
        } else if(field.equals("string")) {
            return Fields.STRING;
        } else if(field.equals("boolean")) {
            return Fields.BOOLEAN;
        } else {
            throw new RuntimeException("Field not found!");
        }
    }

    public T fromJson(JSONObject json) {
        T object = null;

        try {
            object = clazz.newInstance();

            Field[] fields = clazz.getDeclaredFields();

            try {
                for (int i = 0; i < fields.length; i++) {

                    String name = fields[i].getName();
                    boolean was = fields[i].isAccessible();

                    fields[i].setAccessible(true);

                    switch (toFieldEnum(fields[i].getType().getSimpleName().toLowerCase())) {
                        case INT:
                            fields[i].setInt(object, json.optInt(name, 0));
                            break;
                        case LONG:
                            fields[i].setLong(object, json.optLong(name, 0));
                            break;
                        case FLOAT:
                            fields[i].setFloat(object, json.optLong(name, 0));
                            break;
                        case DOUBLE:
                            fields[i].setDouble(object, json.optDouble(name, 0));
                            break;
                        case STRING:
                            fields[i].set(object, json.opt(name));
                            break;
                        case BOOLEAN:
                            fields[i].setBoolean(object, json.optBoolean(name, false));
                            break;
                    }

                    fields[i].setAccessible(was);
                }
            } catch(IllegalAccessException e) {
                e.printStackTrace();
            }

        } catch(IllegalAccessException e) {
            e.printStackTrace();
        } catch(InstantiationException e) {
            e.printStackTrace();
        }

        ((AbstractBean<T>) object).setContext(context);

        return object;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();

        Field[] fields = clazz.getDeclaredFields();

        try {
            for (int i = 0; i < fields.length; i++) {

                String name = fields[i].getName();
                boolean was = fields[i].isAccessible();

                fields[i].setAccessible(true);

                switch (toFieldEnum(fields[i].getType().getSimpleName().toLowerCase())) {
                    case INT:
                        json.put(name, fields[i].getInt(this));
                        break;
                    case LONG:
                        json.put(name, fields[i].getLong(this));
                        break;
                    case FLOAT:
                        json.put(name, fields[i].getFloat(this));
                        break;
                    case DOUBLE:
                        json.put(name, fields[i].getDouble(this));
                        break;
                    case STRING:
                        json.put(name, fields[i].get(this));
                        break;
                    case BOOLEAN:
                        json.put(name, fields[i].getBoolean(this));
                        break;
                }

                fields[i].setAccessible(was);
            }
        } catch(IllegalAccessException e) {
            e.printStackTrace();
        } catch(JSONException e) {
            e.printStackTrace();
        }

        return json;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    protected SharedPreferences loadSharedPreferences(String compl) {
        return context.getSharedPreferences(clazz.getPackage().getName()
                        + "." + clazz.getName() + "." + compl,
                Context.MODE_PRIVATE);
    }

    public synchronized void save() {
        SharedPreferences prefs = loadSharedPreferences("object");

        if(find(getId()) == null) {
            addObject(getId());

            if(cache && cachedObjects != null) {
                cachedObjects.add((T) this);
            }
        }

        prefs.edit().putString(String.valueOf(getId()), toJson().toString()).commit();
    }

    public synchronized void remove() {
        SharedPreferences prefs = loadSharedPreferences("object");

        if(find(getId()) != null) {
            removeObject(getId());

            prefs.edit().putString(String.valueOf(getId()), null).commit();
        }
    }

    public List<T> findAll() {
        if(cache && cachedObjects != null) {
            return cachedObjects;
        }

        JSONArray objects = getObjectList();

        List<T> listT = new ArrayList<T>();

        for(int i=0; i<objects.length(); i++) {
            listT.add(find(objects.optString(i, "")));
        }

        if(cache) {
            cachedObjects = listT;
        }

        return listT;
    }

    private JSONArray getObjectList() {
        SharedPreferences prefs = loadSharedPreferences("objectList");

        String list = prefs.getString("list", null);

        JSONArray objects = null;

        if(list == null) {
            objects = new JSONArray();
        } else {
            try {
                objects = new JSONArray(list);
            } catch(JSONException e) {
                e.printStackTrace();
                throw new RuntimeException("Something very wrong just happened!");
            }
        }

        return objects;
    }

    private void addObject(Object id) {
        SharedPreferences prefs = loadSharedPreferences("objectList");

        JSONArray objects = getObjectList();

        objects.put(String.valueOf(id));

        prefs.edit().putString("list", objects.toString()).commit();
    }

    private void removeObject(Object id) {
        SharedPreferences prefs = loadSharedPreferences("objectList");

        JSONArray objects = getObjectList();

        try {
            for (int i = 0; i < objects.length(); i++) {
                Object _id = objects.getJSONObject(i).getString("_id");

                if (_id.equals(id)) {
                    objects.pop(i);

                    break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        prefs.edit().putString("list", objects.toString()).commit();
    }

    public T find(Object id) {
        SharedPreferences prefs = loadSharedPreferences("object");

        JSONObject object = null;

        try {
            String json = prefs.getString(String.valueOf(id), null);

            if(json == null) return null;

            object = new JSONObject(json);
        } catch(JSONException e) {
            e.printStackTrace();
        }

        return fromJson(object);
    }

    public abstract Object getId();
}
