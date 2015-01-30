package com.mauriciogiordano.easydb.bean;

import android.content.Context;
import android.content.SharedPreferences;

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

    /*
     * Annotation used to identify a field that should be stored.
     */
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface ModelField { }

    /*
     * Configuration variables.
     */
    protected Class<T> clazz;
    protected Context context;
    
    /*
     * Cache related.
     */
    protected boolean cache;
    private List<T> cachedObjects = null;

    /*
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

    /*
     * List of evaluated classes.
     * This avoids evaluating every time a new instance is created. 
     */
    private static List<Class<?>> evaluatedClasses = new ArrayList<Class<?>>();

    /*
     * Verifies if the given field follow the rules.
     *  
     * @param {Field} field
     * @return {boolean}
     * @api private
     */
    private boolean isAllowed(Field field) {
        return ClassUtils.isPrimitiveOrWrapper(field.getType())
                || CharSequence.class.isAssignableFrom(field.getType());
    }

    /*
     * Evaluates the object if not yet evaluated.
     *  
     * @throws {RuntimeException} in case object don't follow the rules.
     * @api private 
     */
    private void evaluateObject() {
        if(evaluatedClasses.contains(clazz)) {
            return;
        }

        Field[] fields = clazz.getDeclaredFields();

        for(Field field : fields) {
            if(field.isAnnotationPresent(ModelField.class)
            && !isAllowed(field)) {
                throw new RuntimeException("Field '" + field.getName() + "' has type '" + field.getType().getSimpleName() + "' that is not allowed!");
            }
        }

        evaluatedClasses.add(clazz);
    }

    /*
     * Default constructor, but all inherited objects should have...
     * an empty constructor for Reflection.
     * 
     * If this constructor is called, it is necessary to run...
     * setContext() afterwards.
     *  
     * @param {Class<T>}
     * @param {boolean}
     * @api public
     */
    public Model(Class<T> clazz, boolean cache) {
        this.clazz = clazz;
        this.cache = cache;

        evaluateObject();
    }

    /*
     * All inherited objects should have an empty constructor...
     * for Reflection.
     *
     * @param {Class<T>}
     * @param {boolean}
     * @param {Context}
     * @api public
     */
    public Model(Class<T> clazz, boolean cache, Context context) {
        this.clazz = clazz;
        this.context = context;
        this.cache = cache;

        evaluateObject();
    }

    /*
     * Return a Fields object for a given field's Class.
     *
     * @param {Class<T>}
     * @return {Fields}
     * @api private
     */
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

    /*
     * Analyzes the entire json object and creates a brand-new...
     * instance from its representation. 
     *
     * TODO: Figure out how to make this accesible without...
     *       creating a dummy instance.
     *  
     * @param {JSONObject}
     * @return {T}
     * @api public
     */
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

    /*
     * Analyzes the entire object and creates a brand-new json...
     * representation.
     *
     * @return {JSONObject}
     * @api public
     */
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

    /*
     * Sets the context for the current instance.
     *
     * @param {Context}
     * @api public
     */
    public void setContext(Context context) {
        this.context = context;
    }

    /*
     * Loads a reference to the SharedPreferences for a given...
     * namespace.
     *
     * @param String namespace
     * @return {SharedPreferences}
     * @throws {RuntimeException} in case of null context.
     * @api protected
     */
    protected SharedPreferences loadSharedPreferences(String namespace) {
        if(context == null) {
            throw new RuntimeException("A context is needed to operate this object! Have you used Model#setContext?");
        }
        
        return context.getSharedPreferences(clazz.getPackage().getName()
                        + "." + clazz.getName() + "." + namespace,
                Context.MODE_PRIVATE);
    }

    /*
     * Saves the current object.
     *
     * @throws {RuntimeException} in case of null context.
     * @api public
     */
    public synchronized void save() {
        SharedPreferences prefs = loadSharedPreferences("object");

        if(find(getId()) == null) {
            addObject();

            modelListenerHandler.execOnUpdateListeners(this, OnUpdateListener.Status.CREATED);
            
            if(cache && cachedObjects != null) {
                cachedObjects.add((T) this);
            }
        } else {
            modelListenerHandler.execOnUpdateListeners(this, OnUpdateListener.Status.UPDATED);
        }
        
        prefs.edit().putString(String.valueOf(getId()), toJson().toString()).commit();
    }

    /*
     * Removes the current object.
     *
     * @throws {RuntimeException} in case of null context.
     * @api public
     */
    public synchronized void remove() {
        SharedPreferences prefs = loadSharedPreferences("object");

        if(find(getId()) != null) {
            removeObject();

            modelListenerHandler.execOnUpdateListeners(this, OnUpdateListener.Status.REMOVED);
            
            prefs.edit().putString(String.valueOf(getId()), null).commit();
        }
    }

    /*
     * Find all objects of type T.
     * 
     * TODO: Add offset. 
     *
     * @return {List<T>}
     * @throws {RuntimeException} in case of null context.
     * @api public
     */
    public List<T> findAll() {
        if(cache && cachedObjects != null) {
            return cachedObjects;
        }

        List<String> ids = getObjectList();

        List<T> listT = new ArrayList<T>();

        for(String id : ids) {
            listT.add(find(id));
        }

        if(cache) {
            cachedObjects = listT;
        }

        return listT;
    }

    /*
     * Get a list of objects ids.
     *
     * @return {List<String>}
     * @throws {RuntimeException} in case of null context.
     * @api private
     */
    private List<String> getObjectList() {
        SharedPreferences prefs = loadSharedPreferences("objectList");

        String list = prefs.getString("list", null);

        return (list == null) ? Arrays.asList(new String[0]) : Arrays.asList(list.split(","));
    }

    /*
     * Adds the object to the ids list.
     *
     * @throws {RuntimeException} in case of null context.
     * @api private
     */
    private void addObject() {
        SharedPreferences prefs = loadSharedPreferences("objectList");

        List<String> objects = getObjectList();
        objects.add(getId());
        
        prefs.edit().putString("list", StringUtils.join(objects, ",")).commit();
    }

    /*
     * Removes the object from the ids list.
     *
     * @throws {RuntimeException} in case of null context.
     * @api private
     */
    private void removeObject() {
        SharedPreferences prefs = loadSharedPreferences("objectList");

        List<String> objects = getObjectList();
        
        int index = objects.indexOf(getId());
        
        if(index == ArrayUtils.INDEX_NOT_FOUND) return;
        
        objects.remove(index);

        prefs.edit().putString("list", StringUtils.join(objects, ",")).commit();
    }

    /*
     * Find a specific object from its id.
     *
     * TODO: Figure out how to make this accesible without...
     *       creating a dummy instance.
     *
     * @param {String}
     * @return {T} 
     * @throws {RuntimeException} in case of null context.
     * @api private
     */
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

    /*
     * Handles all listeners.
     */
    protected final ModelListenerHandler modelListenerHandler = new ModelListenerHandler();

    /*
     * Listeners.
     */

    /*
     * Listener that should be triggered whenever the object...
     * is CREATED, UPDATED or REMOVED. 
     *  
     * @api public
     */
    public static abstract class OnUpdateListener {

        /*
         * Possible statuses.
         */
        public enum Status {
            CREATED,
            UPDATED,
            REMOVED
        }

        /*
         * Run the listener. 
         * 
         * @param {Model}
         * @param {Status}
         * @api private
         */
        public abstract void onUpdate(Model object, Status status);
    }

    /*
     * Listener handler.
     *
     * @api private
     */
    private class ModelListenerHandler {

        public List<OnUpdateListener> onUpdateListeners;

        public ModelListenerHandler() {
            onUpdateListeners = new ArrayList<OnUpdateListener>();
        }

        /*
         * Triggers all listeners listening to this object.
         *
         * @param {Model}
         * @param {Status}
         * @api private
         */
        protected void execOnUpdateListeners(Model target, OnUpdateListener.Status status) {
            for(OnUpdateListener onUpdateListener : onUpdateListeners) {
                onUpdateListener.onUpdate(target, status);
            }
        }
    }

    /*
     * Add a new update listener.
     *
     * @param {OnUpdateListener}
     * @api public
     */
    public void addOnUpdateListener(OnUpdateListener onUpdateListener) {
        modelListenerHandler.onUpdateListeners.add(onUpdateListener);
    }

    /*
     * Remove an update listener.
     *
     * @param {OnUpdateListener}
     * @api public
     */
    public void removeOnUpdateListener(OnUpdateListener onUpdateListener) {
        modelListenerHandler.onUpdateListeners.remove(onUpdateListener);
    }

    /*
     * Abstract methods.
     */

    /*
     * Returns the object id.
     *  
     * @return {String} 
     * @api public
     */
    public abstract String getId();
}
