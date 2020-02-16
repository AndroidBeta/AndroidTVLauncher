package com.stv.launcher;

import android.content.ComponentName;
import android.graphics.drawable.Drawable;

public class AppBean {
    private Drawable icon;
    private String title;
    private ComponentName cn;

    public Drawable getIcon() {
        return this.icon;
    }

    public String getTitle() {
        return this.title;
    }

    public ComponentName getComponent() {
        return this.cn;
    }

    public void setIcon(Drawable d) {
        this.icon = d;
    }

    public void setTitle(String s) {
        this.title = s;
    }

    public void setComponent(ComponentName c) {
        this.cn = c;
    }
}
