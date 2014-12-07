package com.mauriciogiordano.easydb.bean;

import android.content.Context;

/**
 * Created by mauricio on 12/7/14.
 */
public abstract class AbstractHasOneBean<T, O> extends AbstractBean<T> {

    protected Class<O> relatedClazz;

    public AbstractHasOneBean(Class<T> clazz, Class<O> relatedClazz, boolean cache) {
        super(clazz, cache);
        this.relatedClazz = relatedClazz;
    }

    public AbstractHasOneBean(Class<T> clazz, Class<O> relatedClazz, boolean cache, Context context) {
        super(clazz, cache, context);
        this.relatedClazz = relatedClazz;
    }

    public O findRelated(String fallback) {
        O object = null;

        try {
            O dummy = relatedClazz.newInstance();

            object = (O) ((AbstractBean) dummy).find(getRelatedId());
        } catch(IllegalAccessException e) {
            e.printStackTrace();
        } catch(InstantiationException e) {
            e.printStackTrace();
        }

        return object;
    }

    public abstract Object getRelatedId();

}
