package com.cameracountmodule.manager;

import android.content.Context;
import android.content.SharedPreferences;

import com.cameracountmodule.Utils.Global;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SettingsManager {

    private final String MyPREFERENCES = "VISUALOGYX";
    private final String TypeCamera = "TypeCamera";
    private final String FlashMode = "FlashMode";
    private final String shapeType = "shapeType";
    private final String twitterToken = "acessToken";
    private final String twitterSecret = "secretToken";
    private final String liveCamera = "liveCamera";
    private final String opticalFlow = "opticalFlow";
    private final String countOverlay = "countOverlay";
    private final String waterMark = "waterMark";
    private final String opticalDuration = "opticalDuration";
    private final String drawingOverlay = "drawingOverlay";

    private static SharedPreferences sharedpreferences;
    private SharedPreferences.Editor editor;

    private static SettingsManager instance = null;

    int x = (Global.screenWidth - 300) / 2;
    int y = (Global.screenHeight - 300) / 2;

    private SettingsManager(Context context) {
        sharedpreferences = context.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        editor = sharedpreferences.edit();
    }

    public static SettingsManager getInstance(Context context) {
        if (instance == null)
            instance = new SettingsManager(context);
        return instance;
    }

    public void setTypeCamera(int value) {
        editor.putInt(TypeCamera, value);
        editor.commit();
    }

    public int getTypeCamera() {
        return sharedpreferences.getInt(TypeCamera, 0);
    }

    public void setFlashMode(String value) {
        editor.putString(FlashMode, value);
        editor.commit();
    }

    public String getFlashMode() {
        return sharedpreferences.getString(FlashMode, "on");
    }

    public int getShapeType() {
        return sharedpreferences.getInt(shapeType, 1);
    }

    public void setShapeType(int type){
        editor.putInt(shapeType,type);
        editor.commit();
    }

    public Boolean getLiveCamera() {
        return sharedpreferences.getBoolean(liveCamera, false);
    }

    public void setLiveCamera(Boolean type){
        editor.putBoolean(liveCamera,type);
        editor.commit();
    }

    public Boolean getOpticalFlow() {
        return sharedpreferences.getBoolean(opticalFlow, false);
    }

    public void setOpticalFlow(Boolean type){
        editor.putBoolean(opticalFlow,type);
        editor.commit();
    }

    public Boolean getCountOverlay() {
        return sharedpreferences.getBoolean(countOverlay, false);
    }

    public void setCountOverlay(Boolean type){
        editor.putBoolean(countOverlay,type);
        editor.commit();
    }

    public Boolean getWaterMark() {
        return sharedpreferences.getBoolean(waterMark, false);
    }

    public void setWaterMark(Boolean type){
        editor.putBoolean(waterMark,type);
        editor.commit();
    }

    public Boolean getDrawingOverlay() {
        return sharedpreferences.getBoolean(drawingOverlay, false);
    }

    public void setDrawingOverlay(Boolean type){
        editor.putBoolean(drawingOverlay,type);
        editor.commit();
    }

    public String getOpticalDuration() {
        return sharedpreferences.getString(opticalDuration, "");
    }

    public void setOpticalDuration(String type){
        editor.putString(opticalDuration,type);
        editor.commit();
    }

    public String getTwitterToken() {
        return sharedpreferences.getString(twitterToken, "");
    }

    public void setTwitterToken(String token){
        editor.putString(twitterToken,token);
        editor.commit();
    }

    public String getTwitterSecret() {
        return sharedpreferences.getString(twitterSecret, "");
    }

    public void setTwitterSecret(String token){
        editor.putString(twitterSecret,token);
        editor.commit();
    }
}
