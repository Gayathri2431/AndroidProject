package com.cameracountmodule.model;

import android.graphics.Path;

import org.opencv.core.Rect;

/**
 * Created by gayathris on 12/05/17.
 */

public class Rectangles {
    public float x1;
    public float y1;
    public float x2;
    public float y2;
    public float x3;
    public float y3;
    public float x4;
    public float y4;
    public float perimeter;
    public int id;
    public float centerx;
    public float centery;

    public Rectangles(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4, float centerx, float centery, float perimeter,int id) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.x3 = x3;
        this.y3 = y3;
        this.x4 = x4;
        this.y4 = y4;
        this.centerx = centerx;
        this.centery = centery;
        this.id = id;
        this.perimeter = perimeter;
    }

    public float getX1() {
        return x1;
    }

    public float getY1() {
        return y1;
    }

    public float getX2() {
        return x2;
    }

    public float getY2() {
        return y2;
    }

    public float getX3() {
        return x3;
    }

    public float getY3() {
        return y3;
    }

    public float getX4() {
        return x4;
    }

    public float getY4() {
        return y4;
    }

    public void setX1(float x1) {
        this.x1 = x1;
    }

    public void setY1(float y1) {
        this.y1 = y1;
    }

    public void setX2(float x2) {
        this.x2 = x2;
    }

    public void setY2(float y2) {
        this.y2 = y2;
    }

    public void setX3(float x3) {
        this.x3 = x3;
    }

    public void setY3(float y3) {
        this.y3 = y3;
    }

    public void setX4(float x4) {
        this.x4 = x4;
    }

    public void setY4(float y4) {
        this.y4 = y4;
    }

    public float getCenterx() {
        return centerx;
    }

    public void setCentery(float centery) {
        this.centery = centery;
    }

    public void setCenterx(float centerx) {
        this.centerx = centerx;
    }

    public float getCentery() {
        return centery;
    }

    public float getPerimeter() {
        return perimeter;
    }

    public void setPerimeter(float perimeter) {
        this.perimeter = perimeter;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Path getPath() {
        Path path = new Path();
        path.moveTo(x1,y1);
        path.lineTo(x2,y2);
        path.lineTo(x3,y3);
        path.lineTo(x4,y4);
        path.close();
        return path;
    }
}
