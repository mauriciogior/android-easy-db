package com.mauriciogiordano.easydb.bean;

import android.content.Context;
import android.content.SharedPreferences;

import com.mauriciogiordano.easydb.helper.JSONArray;

import org.apache.commons.lang3.ClassUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mauricio on 12/7/14.
 */
public abstract class Model<T> {

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface ModelField { }

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

    private static List<Class<?>> evaluated = new ArrayList<Class<?>>();

    private boolean isAllowed(Field field) {
        if(ClassUtils.isPrimitiveOrWrapper(field.getType())
        || CharSequence.class.isAssignableFrom(field.getType())) {
            return true;
        }

        return false;
    }

    private void evaluateObject() {
        if(evaluated.contains(clazz)) {
            return;
        }

        Field[] fields = clazz.getDeclaredFields();

        for(Field field : fields) {
            if(field.isAnnotationPresent(ModelField.class)
            && !isAllowed(field)) {
                throw new RuntimeException("Field '" + field.getName() + "' has type '" + field.getType().getSimpleName() + "' that is not allowed!");
            }
        }

        evaluated.add(clazz);
    }

    public Model(Class<T> clazz, boolean cache) {
        this.clazz = clazz;
        this.cache = cache;

        evaluateObject();
    }

    public Model(Class<T> clazz, boolean cache, Context context) {
        this.clazz = clazz;
        this.context = context;
        this.cache = cache;

        evaluateObject();
    }

    private static Fields toFieldEnum(Class<?> clazz) {
        String name = clazz.getSimpleName().toLowerCase();

        if(name.equals("int") || name.equals("integer")) {
            return Fields.INT;
        } else if(name.equals("long")) {
            return Fields.LONG;
        } else if(name.equals("float")) {
            return Fields.FLOAT;
        } else if(name.equals("double")) {
            return Fields.DOUBLE;
        } else if(CharSequence.class.isAssignableFrom(clazz)) {
            return Fields.STRING;
        } else if(name.equals("boolean")) {
            return Fields.BOOLEAN;
        } else {
            throw new RuntimeException("Field not found!");
        }
    }

    public T fromJson(JSONObject json) {
        T object = null;

        try {
            object = clazz.newInstance();
            ((Model) object).setContext(context);

            Field[] fields = clazz.getDeclaredFields();

            try {
                for (Field field : fields) {

                    if(!field.isAnnotationPresent(ModelField.class)) {
                        continue;
                    }

                    String name = field.getName();
                    boolean was = field.isAccessible();

                    field.setAccessible(true);

                    switch (toFieldEnum(field.getType())) {
                        case INT:
                            field.setInt(object, json.optInt(name, 0));
                            break;
                        case LONG:
                            field.setLong(object, json.optLong(name, 0));
                            break;
                        case FLOAT:
                            field.setFloat(object, json.optLong(name, 0));
                            break;
                        case DOUBLE:
                            field.setDouble(object, json.optDouble(name, 0));
                            break;
                        case STRING:
                            field.set(object, json.opt(name));
                            break;
                        case BOOLEAN:
                            field.setBoolean(object, json.optBoolean(name, false));
                            break;
                    }

                    field.setAccessible(was);
                }
            } catch(IllegalAccessException e) {
                e.printStackTrace();
            }

        } catch(IllegalAccessException e) {
            e.printStackTrace();
        } catch(InstantiationException e) {
            e.printStackTrace();
        }

        return object;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();

        Field[] fields = clazz.getDeclaredFields();

        try {
            for (Field field : fields) {

                if(!field.isAnnotationPresent(ModelField.class)) {
                    continue;
                }

                String name = field.getName();
                boolean was = field.isAccessible();

                field.setAccessible(true);

                switch (toFieldEnum(field.getType())) {
                    case INT:
                        json.put(name, field.getInt(this));
                        break;
                    case LONG:
                        json.put(name, field.getLong(this));
                        break;
                    case FLOAT:
                        json.put(name, field.getFloat(this));
                        break;
                    case DOUBLE:
                        json.put(name, field.getDouble(this));
                        break;
                    case STRING:
                        json.put(name, field.get(this));
                        break;
                    case BOOLEAN:
                        json.put(name, field.getBoolean(this));
                        break;
                }

                field.setAccessible(was);
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

    /* Listeners */
    protected final ModelListenerHandler modelListenerHandler = ModelListenerHandler.getInstance();

    public static abstract class OnUpdateListener {
        public enum Status {
            CREATED,
            UPDATED,
            REMOVED
        }

        public abstract void onUpdate(Model object, Status status);
    }

    private static class ModelListenerHandler {

        public List<OnUpdateListener> onUpdateListeners;

        private static ModelListenerHandler modelListenerHandler = null;

        public static ModelListenerHandler getInstance() {
            if(modelListenerHandler == null) {
                modelListenerHandler = new ModelListenerHandler();
            }

            return modelListenerHandler;
        }

        private ModelListenerHandler() {
            onUpdateListeners = new ArrayList<OnUpdateListener>();
        }

        protected void execOnUpdateListeners(Model target, OnUpdateListener.Status status) {
            int size = onUpdateListeners.size();

            for(int i = 0; i < size; i++) {
                onUpdateListeners.get(i).onUpdate(target, status);
            }
        }
    }

    public void addOnUpdateListener(OnUpdateListener onUpdateListener) {
        modelListenerHandler.onUpdateListeners.add(onUpdateListener);
    }

    public void removeOnUpdateListener(OnUpdateListener onUpdateListener) {
        modelListenerHandler.onUpdateListeners.remove(onUpdateListener);
    }

    /* Abstract methods */
    public abstract Object getId();
}
