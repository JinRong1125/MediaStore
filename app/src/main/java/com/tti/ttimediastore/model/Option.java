package com.tti.ttimediastore.model;

import android.graphics.drawable.Drawable;

import java.io.Serializable;

/**
 * Created by dylan_liang on 2017/5/15.
 */

public class Option implements Serializable {

    private String name;
    private Drawable icon;

    public Option() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }
}