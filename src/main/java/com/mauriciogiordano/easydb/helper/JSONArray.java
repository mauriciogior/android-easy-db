package com.mauriciogiordano.easydb.helper;

import org.json.JSONException;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class JSONArray extends org.json.JSONArray {

    private List reflectedValuesList;

    private boolean found = false;

    public JSONArray() {
        super();
    }

    public JSONArray(String a) throws JSONException {
        super(a);

        try {
            Field field = getClass().getSuperclass().getDeclaredField("values");
            field.setAccessible(true);

            reflectedValuesList = (ArrayList) field.get(this);

            found = true;
        } catch(NoSuchFieldException e) {
            e.printStackTrace();
        } catch(IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void pop(int index) {
        if(found) {
            reflectedValuesList.remove(index);
        } else {
            // TODO
        }
    }
}
