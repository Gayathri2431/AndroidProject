package com.cameracountmodule.activity;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.Switch;

import com.cameracountmodule.R;
import com.cameracountmodule.Utils.Global;

public class SettingsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    Switch liveCamera,opticalFlow,countOverlay,waterMark,drawingOverlay;
    String[] array = {"1", "2", "3", "4"};
    Spinner opticalDur;
    String selected;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initHome();
        loadData();

    }

    //Initialize the views
    private void initHome() {
        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Settings");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        liveCamera = (Switch) findViewById(R.id.liveCamera);
        opticalFlow = (Switch) findViewById(R.id.opticalFlow);
        countOverlay = (Switch) findViewById(R.id.countResults);
        waterMark = (Switch) findViewById(R.id.waterMark);
        drawingOverlay = (Switch) findViewById(R.id.drawingOverlay);
        opticalDur = (Spinner) findViewById(R.id.opticalDur);
        ArrayAdapter aa = new ArrayAdapter(this,android.R.layout.simple_spinner_item,array);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        opticalDur.setAdapter(aa);
        opticalDur.setOnItemSelectedListener(this);
    }
    //Assign data to the necessary views
    private void loadData() {
        liveCamera.setChecked(Global.settingsManager.getLiveCamera());
        countOverlay.setChecked(Global.settingsManager.getCountOverlay());
        opticalFlow.setChecked(Global.settingsManager.getOpticalFlow());
        waterMark.setChecked(Global.settingsManager.getWaterMark());
        drawingOverlay.setChecked(Global.settingsManager.getDrawingOverlay());
        if(Global.settingsManager.getOpticalDuration().length() != 0) {
            opticalDur.setSelection(Integer.parseInt(Global.settingsManager.getOpticalDuration()) - 1);
        }
        selected = Global.settingsManager.getOpticalDuration();
    }

    public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {

        switch (position) {
            case 0:
                selected = "1";
                break;
            case 1:
                selected = "2";
                break;
            case 2:
                selected = "3";
                break;
            case 3:
                selected = "4";
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Global.settingsManager.setLiveCamera(liveCamera.isChecked());
                Global.settingsManager.setCountOverlay(countOverlay.isChecked());
                Global.settingsManager.setOpticalFlow(opticalFlow.isChecked());
                Global.settingsManager.setWaterMark(waterMark.isChecked());
                Global.settingsManager.setDrawingOverlay(drawingOverlay.isChecked());
                Global.settingsManager.setOpticalDuration(selected);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        Global.settingsManager.setLiveCamera(liveCamera.isChecked());
        Global.settingsManager.setCountOverlay(countOverlay.isChecked());
        Global.settingsManager.setOpticalFlow(opticalFlow.isChecked());
        Global.settingsManager.setWaterMark(waterMark.isChecked());
        Global.settingsManager.setDrawingOverlay(drawingOverlay.isChecked());
        Global.settingsManager.setOpticalDuration(selected);
        finish();
    }
}
