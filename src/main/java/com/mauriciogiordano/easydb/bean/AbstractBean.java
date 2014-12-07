package com.mauriciogiordano.easydb.bean;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

    public AbstractBean(Class<T> clazz, boolean cache) {
        this.clazz = clazz;
        this.cache = cache;
    }

    public AbstractBean(Class<T> clazz, boolean cache, Context context) {
        this.clazz = clazz;
        this.context = context;
        this.cache = cache;
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
    public abstract JSONObject toJson();
    public abstract T fromJson(JSONObject json);

}
