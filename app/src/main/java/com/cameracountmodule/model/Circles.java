package com.cameracountmodule.model;

import android.graphics.Rect;

/**
 * Created by gayathris on 10/05/17.
 */

public class Circles {
    public float x;
    public float y;
    public float radius;
    public Rect frame;
    public int id;

    public Circles(float x,float y,float radius,int id) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.id = id;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getRadius() {
        return radius;
    }

    public Rect getFrame() {
        int offSet = 0;
        int left = (int) (getX() - getRadius()) + offSet;
        int top = (int) (getY() - getRadius()) + offSet;
        this.frame = new Rect(left, top, left + (int) getRadius() * 2, top + (int) getRadius() * 2);

        return this.frame;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public void setFrame(Rect frame) {
        this.frame = frame;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
