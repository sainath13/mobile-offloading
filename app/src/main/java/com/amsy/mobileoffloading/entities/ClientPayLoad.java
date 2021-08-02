package com.amsy.mobileoffloading.entities;

import java.io.Serializable;

public class ClientPayLoad implements Serializable {
    private String tag;
    private Object data;

    public String getTag() {
        return tag;
    }

    public ClientPayLoad setTag(String tag) {
        this.tag = tag;
        return this;
    }

    public Object getData() {
        return data;
    }

    public ClientPayLoad setData(Object data) {
        this.data = data;
        return this;
    }
}
