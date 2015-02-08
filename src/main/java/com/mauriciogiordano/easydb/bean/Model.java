package com.mauriciogiordano.easydb.bean;

import android.content.Context;
import android.content.SharedPreferences;

import com.mauriciogiordano.easydb.exception.NoContextFoundException;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class Model<T> {

    /**
     * Annotation used to identify a field that should be stored.
     */
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface ModelField { }

    /**
     * Configuration variables.
     */
    protected Class<T> clazz;
    protected Context context;
    
    /**
     * Cache related.
     */
    protected boolean cache;
    private List<T> cachedObjects = null;
    private List<String> cachedIds = null;

    /**
     * Available fields for storage.
     * TODO: Allow anything that extends Serializable. 
     */
    private enum Fields {
        INT,
        LONG,
        FLOAT,
        DOUBLE,
        STRING,
        BOOLEAN
    }

    /**
     * List of evaluated classes.
     * This avoids evaluating every time a new instance is created. 
     */
    private static List<Class<?>> evaluatedClasses = new ArrayList<Class<?>>();

    /**
     * Verifies if the given field follow the rules.
     *  
     * @param field Field to be verified
     * @return True if field is allowed and false otherwise.
     */
    private boolean isAllowed(Field field) {
        return ClassUtils.isPrimitiveOrWrapper(field.getType())
                || CharSequence.class.isAssignableFrom(field.getType());
    }

    /**
     * Evaluates the object if not yet evaluated.
     *  
     * @throws RuntimeException in case object don't follow the rules.
     */
    private void evaluateObject() {
        if (evaluatedClasses.contains(clazz)) {
            return;
        }

        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            if (field.isAnnotationPresent(ModelField.class)
            && !isAllowed(field)) {
                throw new RuntimeException("Field '"
                        + field.getName()
                        + "' has type '" + field.getType().getSimpleName()
                        + "' that is not allowed!");
            }
        }

        evaluatedClasses.add(clazz);
    }

    /**
     * Default constructor, but all inherited objects should have...
     * an empty constructor for Reflection.
     * 
     * If this constructor is called, it is necessary to run...
     * setContext() afterwards for context related operations (save...
     * , find, etc)
     *  
     * @param clazz The class that will be used.
     * @param cache Should cache the objects.
     */
    public Model(Class<T> clazz, boolean cache) {
        this.clazz = clazz;
        this.cache = cache;

        evaluateObject();
    }

    /**
     * All inherited objects should have an empty constructor...
     * for Reflection.
     *
     * @param clazz The class that will be used.
     * @param cache Should cache the objects.
     * @param context Application context to use inside sharedPreferences.
     */
    public Model(Class<T> clazz, boolean cache, Context context) {
        this.clazz = clazz;
        this.context = context;
        this.cache = cache;

        evaluateObject();
    }

    /**
     * Return a Fields object for a given field's Class.
     *
     * @param clazz The class that will be used.
     * @throws RuntimeException in case the field type is not found.
     * @return A Fields type if found.
     */
    private static Fields toFieldEnum(Class<?> clazz) {
        String name = clazz.getSimpleName().toLowerCase();

        if (name.equals("int") || name.equals("integer")) {
            return Fields.INT;
        } else if (name.equals("long")) {
            return Fields.LONG;
        } else if (name.equals("float")) {
            return Fields.FLOAT;
        } else if (name.equals("double")) {
            return Fields.DOUBLE;
        } else if (CharSequence.class.isAssignableFrom(clazz)) {
            return Fields.STRING;
        } else if (name.equals("boolean")) {
            return Fields.BOOLEAN;
        } else {
            throw new RuntimeException("Field not found!");
        }
    }

    /**
     * Analyzes the entire json object and creates a brand-new...
     * instance from its representation. 
     *
     * TODO: Figure out how to make this accesible without...
     *       creating a dummy instance.
     *  
     * @param json The JSONObject representation of the object.
     * @return The object T if able to convert and null otherwise.
     */
    public T fromJson(JSONObject json) {
        T object = null;

        try {
            object = clazz.newInstance();
            ((Model) object).setContext(context);

            Field[] fields = clazz.getDeclaredFields();

            try {
                for (Field field : fields) {

                    if (!field.isAnnotationPresent(ModelField.class)) {
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
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }

        return object;
    }

    /**
     * Analyzes the entire object and creates a brand-new json...
     * representation.
     *
     * @return The JSONObject representation of the object.
     */
    public JSONObject toJson() {
        JSONObject json = new JSONObject();

        Field[] fields = clazz.getDeclaredFields();

        try {
            for (Field field : fields) {

                if (!field.isAnnotationPresent(ModelField.class)) {
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
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return json;
    }

    /**
     * Overrides the toString method.
     * Returns a readable version of the object.
     *
     * @return The string representation of the object (in json format).
     */
    @Override
    public String toString() {
        return toJson().toString();
    }

    /**
     * Sets the context for the current instance.
     *
     * @param context Sets the application context for the current instance.
     */
    public void setContext(Context context) {
        this.context = context;
    }

    /**
     * Loads a reference to the SharedPreferences for a given...
     * namespace.
     *
     * @param namespace The namespace to be used.
     * @return The sharedPreferences object.
     * @throws com.mauriciogiordano.easydb.exception.NoContextFoundException in case of null context.
     */
    protected SharedPreferences loadSharedPreferences(String namespace) {
        if (context == null) {
            throw new NoContextFoundException();
        }
        
        return context.getSharedPreferences(clazz.getPackage().getName()
                        + "." + clazz.getName() + "." + namespace,
                Context.MODE_PRIVATE);
    }

    /**
     * Get a list of objects ids.
     *
     * @return The list of all object ids.
     * @throws com.mauriciogiordano.easydb.exception.NoContextFoundException in case of null context.
     */
    private List<String> getObjectList() {
        if (cachedIds != null) return cachedIds;

        SharedPreferences prefs = loadSharedPreferences("objectList");

        String list = prefs.getString("list", null);

        cachedIds = (list == null) ? new ArrayList<String>() : new ArrayList<>(Arrays.asList(list.split(",")));
        return cachedIds;
    }

    /**
     * Adds the object to the ids list.
     *
     * @throws com.mauriciogiordano.easydb.exception.NoContextFoundException in case of null context.
     */
    private void addObject() {
        SharedPreferences prefs = loadSharedPreferences("objectList");

        List<String> objects = getObjectList();
        objects.add(getId());
        
        prefs.edit().putString("list", StringUtils.join(objects, ",")).commit();
    }

    /**
     * Removes the object from the ids list.
     *
     * @throws com.mauriciogiordano.easydb.exception.NoContextFoundException in case of null context.
     * @return True if removed successfully and false otherwise.
     */
    private boolean removeObject() {
        SharedPreferences prefs = loadSharedPreferences("objectList");

        List<String> objects = getObjectList();
        
        int index = objects.indexOf(getId());
        
        if (index == ArrayUtils.INDEX_NOT_FOUND) return false;

        objects.remove(getId());

        if (objects.size() == 0) {
            prefs.edit().putString("list", null).commit();
        } else {
            prefs.edit().putString("list", StringUtils.join(objects, ",")).commit();
        }

        return true;
    }

    /**
     * Saves the current object.
     *
     * @throws com.mauriciogiordano.easydb.exception.NoContextFoundException in case of null context.
     */
    public synchronized void save() {
        SharedPreferences prefs = loadSharedPreferences("object");

        if (find(getId()) == null) {
            addObject();

            modelListenerHandler.execOnUpdateListeners(this, OnUpdateListener.Status.CREATED);

            if (cache && cachedObjects != null) {
                cachedObjects.add((T) this);
            }
        } else {
            modelListenerHandler.execOnUpdateListeners(this, OnUpdateListener.Status.UPDATED);
        }

        prefs.edit().putString(String.valueOf(getId()), toJson().toString()).commit();
    }

    /**
     * Removes the current object.
     *
     * @throws com.mauriciogiordano.easydb.exception.NoContextFoundException in case of null context.
     */
    public synchronized boolean remove() {
        SharedPreferences prefs = loadSharedPreferences("object");

        if (find(getId()) != null) {
            if (!removeObject()) return false;

            modelListenerHandler.execOnUpdateListeners(this, OnUpdateListener.Status.REMOVED);

            prefs.edit().putString(String.valueOf(getId()), null).commit();

            return true;
        }

        return false;
    }

    /**
     * Find a specific object from its id.
     *
     * TODO: Figure out how to make this accesible without...
     *       creating a dummy instance.
     *
     * @param id Object's id.
     * @return The object if found, null otherwise.
     * @throws com.mauriciogiordano.easydb.exception.NoContextFoundException in case of null context.
     */
    public T find(String id) {
        SharedPreferences prefs = loadSharedPreferences("object");

        JSONObject object = null;

        try {
            String json = prefs.getString(String.valueOf(id), null);

            if (json == null) return null;

            object = new JSONObject(json);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return fromJson(object);
    }

    /**
     * Find all objects of type T.
     *
     * TODO: Add offset.
     *
     * @return A list of all objects on the database.
     * @throws com.mauriciogiordano.easydb.exception.NoContextFoundException in case of null context.
     */
    public List<T> findAll() {
        if (cache && cachedObjects != null) {
            return cachedObjects;
        }

        List<String> ids = getObjectList();

        List<T> listT = new ArrayList<>();

        for (String id : ids) {
            listT.add(find(id));
        }

        if (cache) {
            cachedObjects = listT;
        }

        return listT;
    }

    /**
     * Handles all listeners.
     */
    protected final ModelListenerHandler modelListenerHandler = new ModelListenerHandler();

    /**
     * Listeners.
     */

    /**
     * Listener that should be triggered whenever the object...
     * is CREATED, UPDATED or REMOVED.
     */
    public static abstract class OnUpdateListener {

        /**
         * Possible statuses.
         */
        public enum Status {
            CREATED,
            UPDATED,
            REMOVED
        }

        /**
         * Run the listener. 
         * 
         * @param object The object that was modified.
         * @param status What happened to the object.
         */
        public abstract void onUpdate(Model object, Status status);
    }

    /**
     * Listener handler.
     */
    private class ModelListenerHandler {

        public List<OnUpdateListener> onUpdateListeners;

        public ModelListenerHandler() {
            onUpdateListeners = new ArrayList<OnUpdateListener>();
        }

        /**
         * Triggers all listeners listening to this object.
         *
         * @param target The object that was modified.
         * @param status What happened to the object.
         */
        protected void execOnUpdateListeners(Model target, OnUpdateListener.Status status) {
            for (OnUpdateListener onUpdateListener : onUpdateListeners) {
                onUpdateListener.onUpdate(target, status);
            }
        }
    }

    /**
     * Add a new update listener.
     *
     * @param onUpdateListener The listener.
     */
    public void addOnUpdateListener(OnUpdateListener onUpdateListener) {
        modelListenerHandler.onUpdateListeners.add(onUpdateListener);
    }

    /**
     * Remove an update listener.
     *
     * @param onUpdateListener The listener.
     */
    public void removeOnUpdateListener(OnUpdateListener onUpdateListener) {
        modelListenerHandler.onUpdateListeners.remove(onUpdateListener);
    }

    /**
     * Abstract methods.
     */

    /**
     * Returns the object id.
     *  
     * @return The object's id.
     */
    public abstract String getId();
}
