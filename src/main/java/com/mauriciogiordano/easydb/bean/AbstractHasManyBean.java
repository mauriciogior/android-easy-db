package com.mauriciogiordano.easydb.bean;

import android.content.Context;
import android.content.SharedPreferences;

import com.mauriciogiordano.easydb.helper.JSONArray;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mauricio on 12/7/14.
 */
public abstract class AbstractHasManyBean<T, O> extends AbstractBean {

    protected Class<O> childClazz;

    public AbstractHasManyBean(Class<T> clazz, Class<O> childClazz, boolean cache) {
        super(clazz, cache);
        this.childClazz = childClazz;
    }

    public AbstractHasManyBean(Class<T> clazz, Class<O> childClazz, boolean cache, Context context) {
        super(clazz, cache, context);
        this.childClazz = childClazz;
    }

    private JSONArray childrenList() {
        SharedPreferences prefs = loadSharedPreferences("children");

        String list = prefs.getString(String.valueOf(getId()), null);

        JSONArray objects = null;

        if(list == null) {
            objects = new JSONArray();
        } else {
            try {
                objects = new JSONArray(list);
            } catch(JSONException e) {
                e.printStackTrace();
                throw new RuntimeException("Something very wrong just happened! \n" + list);
            }
        }

        return objects;
    }

    public void addChild(O child) {
        SharedPreferences prefs = loadSharedPreferences("children");

        JSONArray objects = childrenList();

        AbstractBean toPut = (AbstractBean) child;

        if(indexOfChild(toPut.getId()) != -1) {
            return;
        }

        objects.put(toPut.getId());

        prefs.edit().putString(String.valueOf(getId()), objects.toString()).commit();
    }

    public int indexOfChild(Object id) {
        JSONArray objects = childrenList();

        for(int i = 0; i < objects.length(); i++) {
            try {
                if(objects.get(i).equals(id)) {
                    return i;
                }
            } catch(JSONException e) {
                e.printStackTrace();
            }
        }

        return -1;
    }

    public boolean removeChild(Object id) {
        SharedPreferences prefs = loadSharedPreferences("children");

        JSONArray objects = childrenList();

        int index = indexOfChild(id);

        if(index == -1) {
            return false;
        }

        objects.pop(index);
        prefs.edit().putString(String.valueOf(getId()), objects.toString()).commit();

        return false;
    }

    public List<O> findAllChildren() {
        JSONArray objects = childrenList();

        List<O> children = new ArrayList<O>();

        try {
            AbstractBean dummy = (AbstractBean) childClazz.newInstance();
            dummy.setContext(context);

            for (int i = 0; i < objects.length(); i++) {
                try {
                    Object id = objects.get(i);

                    children.add((O) dummy.find(id));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } catch(IllegalAccessException e) {
            e.printStackTrace();
        } catch(InstantiationException e) {
            e.printStackTrace();
        }

        return children;
    }
}
