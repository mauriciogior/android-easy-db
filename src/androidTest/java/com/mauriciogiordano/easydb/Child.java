package com.mauriciogiordano.easydb;

import android.content.Context;

import com.mauriciogiordano.easydb.bean.Model;

/**
 * Created by mauricio on 1/21/15.
 */
public class Child extends Model<Child> {

    @ModelField
    private String id;
    @ModelField
    private String hash;

    public Child() {
        super(Child.class, false);
    }

    public Child(Context context) {
        super(Child.class, false, context);
    }

    @Override
    public Object getId() {
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
