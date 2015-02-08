package com.mauriciogiordano.easydb.bean;

import android.content.Context;
import android.content.SharedPreferences;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class HasManyModel<F, C> extends Model {

    /**
     * Configuration variables.
     */
    protected Class<C> childClazz;

    /**
     * Cache related.
     */
    private List<String> childrenList = null;

    /**
     * Default constructor, but all inherited objects should have an empty constructor for...
     * Reflection.
     *
     * If this constructor is called, it is necessary to run setContext() afterwards for context..
     * related operations (save, find, etc).
     *
     * @param fatherClazz The father's class.
     * @param childClazz The child's class.
     * @param cache Should cache the objects.
     */
    public HasManyModel(Class<F> fatherClazz, Class<C> childClazz, boolean cache) {
        super(fatherClazz, cache);
        this.childClazz = childClazz;
    }

    /**
     * All inherited objects should have an empty constructor for Reflection.
     *
     * @param fatherClazz The father's class.
     * @param childClazz The child's class.
     * @param cache Should cache the objects.
     * @param context Application context to use inside sharedPreferences.
     */
    public HasManyModel(Class<F> fatherClazz, Class<C> childClazz, boolean cache, Context context) {
        super(fatherClazz, cache, context);
        this.childClazz = childClazz;
    }

    /**
     * Get a list of child objects ids.
     *
     * @throws com.mauriciogiordano.easydb.exception.NoContextFoundException in case of null..
     * context.
     * @return List of all children.
     */
    private List<String> getChildrenList() {
        if (childrenList != null) return childrenList;

        SharedPreferences prefs = loadSharedPreferences("children");

        String list = prefs.getString(getId(), null);

        childrenList = (list == null) ? new ArrayList<String>() : new ArrayList<>(Arrays.asList(list.split(",")));
        return childrenList;
    }

    /**
     * Returns the index of a specific child.
     * Although it's a public api, it is useless outside of the class. (turn it private?)
     *
     * @param id The child's id.
     * @throws com.mauriciogiordano.easydb.exception.NoContextFoundException in case of null context.
     * @return The index of the child.
     */
    public int indexOfChild(String id) {
        return getChildrenList().indexOf(id);
    }

    /**
     * Adds the object to the child ids list.
     *
     * @param child The child to be added.
     * @throws com.mauriciogiordano.easydb.exception.NoContextFoundException in case of null context.
     */
    public void addChild(C child) {
        SharedPreferences prefs = loadSharedPreferences("children");

        List<String> objects = getChildrenList();

        Model toPut = (Model) child;

        if (objects.indexOf(toPut.getId()) != ArrayUtils.INDEX_NOT_FOUND) {
            return;
        }

        objects.add(toPut.getId());

        toPut.save();

        prefs.edit().putString(String.valueOf(getId()), StringUtils.join(objects, ",")).commit();
    }

    /**
     * Removes the object from the child ids list.
     *
     * @param id The child's id to be removed.
     * @throws com.mauriciogiordano.easydb.exception.NoContextFoundException in case of null context.
     * @return True if removed successfully and false otherwise.
     */
    public boolean removeChild(String id) {
        SharedPreferences prefs = loadSharedPreferences("children");

        List<String> objects = getChildrenList();

        if (objects.indexOf(id) == ArrayUtils.INDEX_NOT_FOUND) {
            return false;
        }

        objects.remove(id);
        prefs.edit().putString(String.valueOf(getId()), StringUtils.join(objects, ",")).commit();

        return true;
    }

    /**
     * Removes the object from the child ids list.
     *
     * @param object The child to be removed
     * @throws com.mauriciogiordano.easydb.exception.NoContextFoundException in case of null context.
     * @return True if removed successfully and false otherwise.
     */
    public boolean removeChild(C object) {
        SharedPreferences prefs = loadSharedPreferences("children");

        List<String> objects = getChildrenList();

        String id = ((Model) object).getId();

        if (objects.indexOf(id) == ArrayUtils.INDEX_NOT_FOUND) {
            return false;
        }

        objects.remove(id);
        prefs.edit().putString(String.valueOf(getId()), StringUtils.join(objects, ",")).commit();

        return true;
    }

    /**
     * Find a specific object from the child list.
     *
     * TODO: Figure out how to make this accesible without...
     *       creating a dummy instance.
     *
     * @param id Child's id
     * @throws com.mauriciogiordano.easydb.exception.NoContextFoundException in case of null context.
     * @return The child if found, null otherwise.
     */
    public C findChild(String id) {
        int index = getChildrenList().indexOf(id);

        if (index == ArrayUtils.INDEX_NOT_FOUND) {
            return null;
        }

        C child = null;

        try {
            Model dummy = (Model) childClazz.newInstance();
            dummy.setContext(context);

            child = (C) dummy.find(id);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return child;
    }

    /**
     * Find all objects from the child list.
     *
     * TODO: Figure out how to make this accesible without...
     *       creating a dummy instance.
     *
     * @throws com.mauriciogiordano.easydb.exception.NoContextFoundException in case of null context.
     * @return A list of all children.
     */
    public List<C> findAllChildren() {
        List<String> objects = getChildrenList();

        List<C> children = new ArrayList<C>();

        try {
            Model dummy = (Model) childClazz.newInstance();
            dummy.setContext(context);

            for (String id : objects) {
                children.add((C) dummy.find(id));
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }

        return children;
    }
}
