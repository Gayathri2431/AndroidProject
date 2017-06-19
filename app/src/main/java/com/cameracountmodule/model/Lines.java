package com.cameracountmodule.model;

import android.graphics.Path;

/**
 * Created by gayathris on 10/05/17.
 */

public class Lines {
    public float startx;
    public float starty;
    public float endx;
    public float endy;

    public Lines(float startx, float starty, float endx, float endy) {
        this.startx = startx;
        this.starty = starty;
        this.endx = endx;
        this.endy = endy;
    }

    public float getStartx() {
        return startx;
    }

    public float getStarty() {
        return starty;
    }

    public float getEndx() {
        return endx;
    }

    public float getEndy() {
        return endy;
    }

    public void setStartx(float startx) {
        this.startx = startx;
    }

    public void setStarty(float starty) {
        this.starty = starty;
    }

    public void setEndx(float endx) {
        this.endx = endx;
    }

    public void setEndy(float endy) {
        this.endy = endy;
    }

    public Path getPath() {
        Path path = new Path();
        path.moveTo(startx,starty);
        path.lineTo(endx,endy);
        path.close();
        return path;
    }
}
