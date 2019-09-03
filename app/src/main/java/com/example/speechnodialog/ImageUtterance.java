package com.example.speechnodialog;

import android.graphics.drawable.Drawable;

public class ImageUtterance {
    private String uri;
    private String name;
    private Drawable drawable;

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        uri = uri;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Drawable getDrawable() {
        return drawable;
    }

    public void setDrawable(Drawable drawable) {
        this.drawable = drawable;
    }
}
