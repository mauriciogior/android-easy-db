package com.mauriciogiordano.easydb;

/**
 * Created by mauricio on 1/21/15.
 */

import android.content.Context;

import com.mauriciogiordano.easydb.bean.Model;

public class Single extends Model<Single> {

    @ModelField
    private String id;
    @ModelField
    private String hash;

    public Single() {
        super(Single.class, false);
    }

    public Single(Context context) {
        super(Single.class, false, context);
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
