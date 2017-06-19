package com.cameracountmodule.model;

import android.graphics.Path;

/**
 * Created by gaurav on 8/3/17.
 */


public class LineModel {
    private Path linePath;
    private  String type;


    public LineModel(String type, Path linePath) {
       this.linePath = linePath;
        this.type = type;
    }

    public Path getPath() {
        return this.linePath;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
