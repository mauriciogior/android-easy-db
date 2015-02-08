package com.mauriciogiordano.easydb;

import android.content.Context;

import com.mauriciogiordano.easydb.bean.Model;

import org.apache.http.client.HttpClient;

/**
 * Created by mauricio on 1/21/15.
 */
public class Broken extends Model {

    @ModelField
    private String id;
    @ModelField
    private String hash;
    @ModelField
    private HttpClient client;

    public Broken() {
        super(Broken.class, false);
    }

    public Broken(Context context) {
        super(Broken.class, false, context);
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
