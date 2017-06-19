package com.cameracountmodule.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.cameracountmodule.R;
import com.cameracountmodule.manager.ViewExtras;
import com.cameracountmodule.model.DataClass;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private Button instaCount;
    ImageView resultImage;
    DataClass dataClass;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= 23) {
            ArrayList<String> requestList = new ArrayList<>();

            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestList.add(Manifest.permission.CAMERA);
            }

            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }

            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }

            if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                requestList.add(Manifest.permission.RECORD_AUDIO);
            }

            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestList.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }

            if (requestList.size() > 0) {
                String[] requestArr = new String[requestList.size()];
                requestArr = requestList.toArray(requestArr);
                ActivityCompat.requestPermissions(this, requestArr, 1);
            } else {
            }
        } else {
        }

        instaCount = (Button) findViewById(R.id.insta_count);

        instaCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(MainActivity.this, CameraActivity.class);
                startActivity(cameraIntent);
            }
        });

        dataClass = dataClass.getInstance();
        if(dataClass.resultBitmap != null) {
            resultImage = (ImageView) findViewById(R.id.resultImage);
            resultImage.setImageBitmap(dataClass.resultBitmap);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        boolean isGratedFully = true;
        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                isGratedFully = false;
                break;
            }
        }

        if (isGratedFully) {

        } else {
            new MaterialDialog.Builder(this)
                    .title(R.string.app_name)
                    .titleColor(ViewExtras.getColor(this, R.color.colorMain))
                    .content(R.string.not_full_permission)
                    .positiveText("Exit")
                    .negativeColor(ViewExtras.getColor(this, R.color.main_color))
                    .positiveColor(ViewExtras.getColor(this, R.color.main_color))
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(MaterialDialog dialog, DialogAction which) {
                            dialog.cancel();
                            //SignInActivity.this.finish();
                        }
                    })
                    .canceledOnTouchOutside(false)
                    .cancelable(false)
                    .show();
        }
    }
}
