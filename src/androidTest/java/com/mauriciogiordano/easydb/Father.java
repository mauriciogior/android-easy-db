package com.mauriciogiordano.easydb;

import android.content.Context;

import com.mauriciogiordano.easydb.bean.HasManyModel;

/**
 * Created by mauricio on 1/21/15.
 */
public class Father extends HasManyModel<Father, Child> {

    @ModelField
    private String id;
    @ModelField
    private String hash;

    public Father() {
        super(Father.class, Child.class, false);
    }

    public Father(Context context) {
        super(Father.class, Child.class, false, context);
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }
}
