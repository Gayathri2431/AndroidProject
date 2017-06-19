package com.cameracountmodule.model;

import android.graphics.Bitmap;

import java.util.ArrayList;

/**
 * Created by gayathris on 10/05/17.
 */

public class DataClass {
    private static final DataClass INSTANCE = new DataClass();
    public ArrayList<Circles> circles = new ArrayList<>();
    public ArrayList<Lines> lines = new ArrayList<>();
    public ArrayList<Rectangles> rectangles = new ArrayList<>();
    public ArrayList<Circles> smallCircles = new ArrayList<>();
    public Bitmap rawBitmap;
    public Bitmap processedBitmap;
    public Bitmap resultBitmap;
    // Private constructor prevents instantiation from other classes
    private DataClass() {}

    public static DataClass getInstance() {
        return INSTANCE;
    }
}
