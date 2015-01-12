package com.mauriciogiordano.easydb.bean;

import android.content.Context;

/**
 * Created by mauricio on 12/7/14.
 */
public abstract class HasOneModel<T, O> extends Model<T> {

    protected Class<O> relatedClazz;

    public HasOneModel(Class<T> clazz, Class<O> relatedClazz, boolean cache) {
        super(clazz, cache);
        this.relatedClazz = relatedClazz;
    }

    public HasOneModel(Class<T> clazz, Class<O> relatedClazz, boolean cache, Context context) {
        super(clazz, cache, context);
        this.relatedClazz = relatedClazz;
    }

    public O findRelated(String fallback) {
        O object = null;

        try {
            O dummy = relatedClazz.newInstance();

            object = (O) ((Model) dummy).find(getRelatedId());
        } catch(IllegalAccessException e) {
            e.printStackTrace();
        } catch(InstantiationException e) {
            e.printStackTrace();
        }

        return object;
    }

    public abstract Object getRelatedId();

}
