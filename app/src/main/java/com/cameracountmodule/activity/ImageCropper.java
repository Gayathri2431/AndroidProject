package com.cameracountmodule.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.cameracountmodule.R;
import com.cameracountmodule.interfaceUI.CropCallback;
import com.cameracountmodule.model.DataClass;
import com.theartofdev.edmodo.cropper.CropImageView;

public class ImageCropper extends AppCompatActivity {
    CropImageView mCropView;
    DataClass dataClass;
    Button done,cancel;
    public static CropCallback callback;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_cropper);

        dataClass = DataClass.getInstance();

        mCropView = (CropImageView) findViewById(R.id.cropImageView);
        mCropView.setImageBitmap(dataClass.rawBitmap);

        done = (Button) findViewById(R.id.done);
        cancel = (Button) findViewById(R.id.cancel);

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dataClass.rawBitmap = mCropView.getCroppedImage();
                callback.doneCropping();
                finish();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
