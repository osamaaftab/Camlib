package com.library.camlibrary.manager;

public class CameraOutputModel {
    private int type;
    private String path;

    public CameraOutputModel(int type, String path) {
        this.type = type;
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public int getType() {
        return type;
    }
}
