package com.cameracountmodule.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cameracountmodule.Algorithm.ImageAnalysis;
import com.cameracountmodule.R;
import com.cameracountmodule.Utils.Global;
import com.cameracountmodule.interfaceUI.CropCallback;
import com.cameracountmodule.interfaceUI.DrawingCallback;
import com.cameracountmodule.manager.DrawingPad;
import com.cameracountmodule.manager.GoogleMapManager;
import com.cameracountmodule.manager.ShareManager;
import com.cameracountmodule.model.DataClass;
import com.cameracountmodule.model.ImageResultType;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.ryanharter.android.tooltips.ToolTip;
import com.ryanharter.android.tooltips.ToolTipLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import toan.android.floatingactionmenu.FloatingActionButton;
import toan.android.floatingactionmenu.FloatingActionsMenu;

public class CameraResultActivity extends AppCompatActivity {
    private ImageView resultImage, overLayImageView, fadedBackgroundImage;
    private DrawingPad drawingPad;
    private String imageName = "";
    private TextView userNameTV, locationTV, fadedLocationTV, fadedId;
    private TextView countPipeTV, fadedPipeNameTimeTV, fadedTime;
    private TextView timeStamp;
    private RelativeLayout waterMarkRL;
    private ToolTip toolTip;
    private ToolTipLayout tipContainer;
    private View abstractView;
    private View view;
    private FloatingActionsMenu shapesMenu;
    private FloatingActionButton circlePipe, rectanglePipe, sheetPipe, smallPipebutton, multiShape;

    private boolean galleryPicture = true;
    private String utcTime = "";
    private int rotation;
    private int shapeType = 1;
    private boolean rotationFlag = false;
    private SupportMapFragment mapFragment;
    private SupportMapFragment fadeMapFragment;
    private GoogleMapManager googleMapManager;
    private DataClass dataClass;
    private ImageAnalysis imageAnalysis;
    private Bitmap rawBitmap, processedBitmap;
    private boolean toolTipShareFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_result);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        drawingPad = new DrawingPad(CameraResultActivity.this, new DrawingResponse());
        view = (View) findViewById(R.id.total);

        rawBitmap = DataClass.getInstance().rawBitmap;
        processedBitmap = DataClass.getInstance().processedBitmap;

        initHome();
        loadData();
    }

    //Initialize the views
    private void initHome() {
        waterMarkRL = (RelativeLayout) findViewById(R.id.waterMark);
        shapesMenu = (FloatingActionsMenu) findViewById(R.id.shapesMenu);
        userNameTV = (TextView) findViewById(R.id.userName);
        timeStamp = (TextView) findViewById(R.id.timeStamp);
        locationTV = (TextView) findViewById(R.id.location);
        fadedLocationTV = (TextView) findViewById(R.id.fadedLocation);
        countPipeTV = (TextView) findViewById(R.id.pipeCount);
        fadedPipeNameTimeTV = (TextView) findViewById(R.id.fadedPipeNameTime);
        fadedTime = (TextView) findViewById(R.id.fadedTime);
        resultImage = (ImageView) findViewById(R.id.imageView);
        fadedBackgroundImage = (ImageView) findViewById(R.id.fadedImageview);
        overLayImageView = (ImageView) findViewById(R.id.overLayImageView);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        fadeMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fadedMapFragment);
        fadedId = (TextView) findViewById(R.id.fadedId);
        tipContainer = (ToolTipLayout) findViewById(R.id.tooltip_container);
        abstractView = findViewById(R.id.abstractView);

        shapeType = Global.settingsManager.getShapeType();
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        overLayImageView.setOnTouchListener(drawingPad);
    }

    //Assign data to the necessary views
    private void loadData() {
        dataClass = DataClass.getInstance();
        imageAnalysis = new ImageAnalysis(getApplicationContext());
        rotation();

        String userName = ", User";
        userNameTV.setText(userName);
        SimpleDateFormat df = new SimpleDateFormat("MM-dd-yyyy HH:mm");
        try {
            df.setTimeZone(TimeZone.getTimeZone("UTC"));
            utcTime = df.format(new Date());
        } catch (Exception e) {
            utcTime = "null";
        }

        timeStamp.setText(utcTime);
        String location = null;
        try {
            location = Global.lastLocation(CameraResultActivity.this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        locationTV.setText(location);
        fadedLocationTV.setText(location);

        if (Global.getLocation() != null)
            googleMapManager = new GoogleMapManager(this, mapFragment, new LatLng(Global.getLocation().getLatitude(), Global.getLocation().getLongitude()), false);
        else
            googleMapManager = new GoogleMapManager(this, mapFragment, null, false);

        if (Global.getLocation() != null)
            googleMapManager = new GoogleMapManager(this, fadeMapFragment, new LatLng(Global.getLocation().getLatitude(), Global.getLocation().getLongitude()), false);
        else
            googleMapManager = new GoogleMapManager(this, fadeMapFragment, null, false);

        resultImage.setImageBitmap(rawBitmap);
        Bitmap blurBitmap = dataClass.rawBitmap.copy(dataClass.rawBitmap.getConfig(), dataClass.rawBitmap.isMutable());
        fadedBackgroundImage.setImageBitmap(Global.blur(this, blurBitmap));
        fadedId.setText("AB64-9FB6-0024-G112");

        overLayImageView.setBackgroundColor(Color.TRANSPARENT);
        drawingPad.backgroundImage = rawBitmap;
        drawingPad.overlayImageView = overLayImageView;
        drawingPad.drawOverlay();
        if (!Global.settingsManager.getDrawingOverlay()) {
            drawingPad.overlayImageView = null;
            drawingPad.backgroundImage = null;
        }
        fadedPipeNameTimeTV.setText(countPipeTV.getText().toString() + userNameTV.getText().toString());
        fadedTime.setText(timeStamp.getText().toString());

        tipContainer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        dismissTooltip();
                }
                return false;
            }
        });

        circlePipe = (FloatingActionButton) findViewById(R.id.circlePipes);
        circlePipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shapeType = Global.CIRCLE_TYPE_PIPE;
                setCurrentPipeType(shapeType);
                shapesMenu.collapse();
                Global.settingsManager.setShapeType(shapeType);
                updateAfterOperation();
            }
        });

        rectanglePipe = (FloatingActionButton) findViewById(R.id.rectPipes);
        rectanglePipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shapeType = Global.RECTANGLE_TYPE_PIPE;
                setCurrentPipeType(shapeType);
                shapesMenu.collapse();
                Global.settingsManager.setShapeType(shapeType);
                updateAfterOperation();
            }
        });

        sheetPipe = (FloatingActionButton) findViewById(R.id.sheets);
        sheetPipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shapeType = Global.SHEET_TYPE_PIPE;
                setCurrentPipeType(shapeType);
                shapesMenu.collapse();
                Global.settingsManager.setShapeType(shapeType);
                updateAfterOperation();
            }
        });

        smallPipebutton = (FloatingActionButton) findViewById(R.id.smallPipe);
        smallPipebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shapeType = Global.SMALL_CIRCLE_TYPE_PIPE;
                setCurrentPipeType(shapeType);
                shapesMenu.collapse();
                Global.settingsManager.setShapeType(shapeType);
                updateAfterOperation();
            }
        });

        multiShape = (FloatingActionButton) findViewById(R.id.multiShape);
        multiShape.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shapeType = Global.MULTI_TYPE_PIPE;
                setCurrentPipeType(shapeType);
                shapesMenu.collapse();
                Global.settingsManager.setShapeType(shapeType);
                updateAfterOperation();
            }
        });

        if (Global.settingsManager.getShapeType() == Global.CIRCLE_TYPE_PIPE) {
            countPipeTV.setText(createSpannableString(dataClass.circles.size(), getPipeTypeString(Global.settingsManager.getShapeType(), dataClass.circles.size())));
        } else if (Global.settingsManager.getShapeType() == Global.RECTANGLE_TYPE_PIPE) {
            countPipeTV.setText(createSpannableString(dataClass.rectangles.size(), getPipeTypeString(Global.settingsManager.getShapeType(), dataClass.rectangles.size())));
        } else if (Global.settingsManager.getShapeType() == Global.SHEET_TYPE_PIPE) {
            countPipeTV.setText(createSpannableString(dataClass.lines.size(), getPipeTypeString(Global.settingsManager.getShapeType(), dataClass.lines.size())));
        } else if (Global.settingsManager.getShapeType() == Global.SMALL_CIRCLE_TYPE_PIPE) {
            countPipeTV.setText(createSpannableString(dataClass.circles.size(), getPipeTypeString(Global.settingsManager.getShapeType(), dataClass.circles.size())));
        } else {
            countPipeTV.setText(createSpannableString(dataClass.circles.size() + dataClass.lines.size() + dataClass.rectangles.size() + dataClass.smallCircles.size(), getPipeTypeString(Global.settingsManager.getShapeType(), dataClass.circles.size())));
        }

        setCurrentPipeType(Global.settingsManager.getShapeType());
    }

    //Create Tooltip for sharing
    private ToolTip createToolTip(View target) {

        ToolTip toolTip = new ToolTip.Builder(getApplicationContext())
                .anchor(target)
                .gravity(Gravity.BOTTOM)
                .color(Color.WHITE)
                .pointerSize(20)
                .contentView(createView())
                .build();
        return toolTip;
    }

    //Dismiss Tooltip for sharing
    private void dismissTooltip() {
        tipContainer.dismiss();
        abstractView.setVisibility(View.GONE);
        if (toolTip != null)
            tipContainer.removeView(toolTip.getView());
    }

    //Getting the result processed image
    private Bitmap getImageFromLayout() {
        shapesMenu.setVisibility(View.INVISIBLE);
        if (Global.settingsManager.getWaterMark()) {
            fadedPipeNameTimeTV.setText(countPipeTV.getText().toString() + userNameTV.getText().toString());
            fadedTime.setText(timeStamp.getText().toString());
            waterMarkRL.setVisibility(View.VISIBLE);
        } else {
            waterMarkRL.setVisibility(View.INVISIBLE);
        }

        Bitmap image = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.RGB_565);
        view.draw(new Canvas(image));

        waterMarkRL.setVisibility(View.VISIBLE);
        shapesMenu.setVisibility(View.VISIBLE);
        return image;
    }

    //Creating string to display in bottom bar
    private Spannable createSpannableString(int size, String type) {
        String totalString = "";
        Spannable sb;

        String val1 = String.valueOf(size);
        String val2 = type;
        if (size == 0) {
            totalString = val2;

            sb = new SpannableString(totalString);
        } else {
            totalString = val1 + val2;

            sb = new SpannableString(totalString);
            sb.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), totalString.indexOf(val1), totalString.indexOf(val1) + val1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); //bold
        }
        return sb;
    }

    //Rotate image to match the Camera image
    private void rotation() {
        if (rotationFlag == false && galleryPicture != true) {
            switch (rotation) {
                case Global.ORIENTATION_PORTRAIT_NORMAL:
                    if (Global.settingsManager.getTypeCamera() == Camera.CameraInfo.CAMERA_FACING_FRONT)
                        rawBitmap = rotate(rawBitmap, 0);
                    else
                        rawBitmap = rotate(rawBitmap, 0);
                    break;
                case Global.ORIENTATION_LANDSCAPE_NORMAL:

                    if (Global.settingsManager.getTypeCamera() == Camera.CameraInfo.CAMERA_FACING_FRONT)
                        rawBitmap = rotate(rawBitmap, -90);
                    else {
                        if (Global.getAutoRotationStatus(getApplicationContext()) == true)
                            rawBitmap = rotate(rawBitmap, 0);
                        else
                            rawBitmap = rotate(rawBitmap, 270);
                    }
                    break;
                case Global.ORIENTATION_PORTRAIT_INVERTED:
                    if (Global.settingsManager.getTypeCamera() == Camera.CameraInfo.CAMERA_FACING_FRONT)
                        rawBitmap = rotate(rawBitmap, -180);
                    else
                        rawBitmap = rotate(rawBitmap, 180);
                    break;
                case Global.ORIENTATION_LANDSCAPE_INVERTED:
                    if (Global.settingsManager.getTypeCamera() == Camera.CameraInfo.CAMERA_FACING_FRONT)
                        rawBitmap = rotate(rawBitmap, 90);
                    else {
                        if (Global.getAutoRotationStatus(getApplicationContext()) == true)
                            rawBitmap = rotate(rawBitmap, 0);
                        else
                            rawBitmap = rotate(rawBitmap, 90);
                    }
                    break;
            }
        }
        rotationFlag = true;
    }

    //Rotate bitmap to specific degress
    private Bitmap rotate(Bitmap bitmap, int degree) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        Matrix mtx = new Matrix();
        mtx.setRotate(degree);
        return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
    }

    //Getting the bottom bar string based on the shape selected
    private String getPipeTypeString(int pipeType, int pipeSize) {
        int numberOfPipes = pipeSize;
        String type = "";
        if (pipeType == Global.CIRCLE_TYPE_PIPE) {
            if (numberOfPipes == 0)
                type = "No circles found";
            else if (numberOfPipes == 1)
                type = " circle";
            else
                type = " circles";
        } else if (pipeType == Global.RECTANGLE_TYPE_PIPE) {
            if (numberOfPipes == 0)
                type = "No rectangles found";
            else if (numberOfPipes == 1)
                type = " rectangle";
            else
                type = " rectangles";
        } else if (pipeType == Global.SHEET_TYPE_PIPE) {
            if (numberOfPipes == 0)
                type = "No lines found";
            else if (numberOfPipes == 1)
                type = " line";
            else
                type = " lines";
        } else if (pipeType == Global.SMALL_CIRCLE_TYPE_PIPE) {
            if (numberOfPipes == 0)
                type = "No small circles found";
            else if (numberOfPipes == 1)
                type = " small circle";
            else
                type = " small circles";
        } else if (pipeType == Global.MULTI_TYPE_PIPE) {
            if (numberOfPipes == 0)
                type = "No multi shapes found";
            else if (numberOfPipes == 1)
                type = " multi shape";
            else
                type = " multi shapes";
        }
        return type;
    }

    //Update the overlay and image after Crop/Recount
    private void updateAfterOperation() {
        CameraActivity.resultImageType = ImageResultType.Picked;
        Bitmap result = dataClass.rawBitmap.copy(dataClass.rawBitmap.getConfig(), dataClass.rawBitmap.isMutable());
        if (Global.settingsManager.getShapeType() == Global.CIRCLE_TYPE_PIPE) {
            Bitmap processedBitmap = imageAnalysis.circleDetection(result, 640, 480, false);
            ImageAnalysis.setDetectedCirclesParameter();
        } else if (Global.settingsManager.getShapeType() == Global.RECTANGLE_TYPE_PIPE) {
            Bitmap processedBitmap = imageAnalysis.process_image_contour_rectangle(result, false);
            ImageAnalysis.setResultRectanglesParameters();
        } else if (Global.settingsManager.getShapeType() == Global.SHEET_TYPE_PIPE) {
            Bitmap processedBitmap = imageAnalysis.process_image_hough_lines(result, false);
            ImageAnalysis.setResultLinesParameters();
        } else if (Global.settingsManager.getShapeType() == Global.SMALL_CIRCLE_TYPE_PIPE) {
            Bitmap processedBitmap = imageAnalysis.process_image_tracking_small_circle(result, 640, 480, false);
            ImageAnalysis.setDetectedCirclesParameter();
        } else {
            Bitmap processedBitmap = imageAnalysis.process_image_multi_shape(result,false);
        }
        drawingPad.drawOverlay();
    }

    //Creating tooltip view for sharing
    private View createView() {

        final View itemView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.item_show_share_camera_result, null, false);

        ImageView facebookImgView = (ImageView) itemView.findViewById(R.id.facebookImgViewCR);
        facebookImgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismissTooltip();
                if (!Global.isNetworkConnected(getApplicationContext())) {
                    Global.showDisconnectNetworkMessage(getApplicationContext(), getResources().getString(R.string.error_in_sharing));
                    return;
                }
                ShareManager.getInstance(CameraResultActivity.this).shareFB(getImageFromLayout());
            }
        });

        ImageView twitterImgView = (ImageView) itemView.findViewById(R.id.twitterImgViewCR);
        twitterImgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismissTooltip();
                if (!Global.isNetworkConnected(getApplicationContext())) {
                    Global.showDisconnectNetworkMessage(getApplicationContext(), getResources().getString(R.string.error_in_sharing));
                    return;
                }
                ShareManager.getInstance(CameraResultActivity.this).shareTwitter("", getImageFromLayout());
            }
        });

        ImageView whatsAppImgView = (ImageView) itemView.findViewById(R.id.whatsAppImgViewCR);
        whatsAppImgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismissTooltip();
                if (!Global.isNetworkConnected(getApplicationContext())) {
                    Global.showDisconnectNetworkMessage(getApplicationContext(), getResources().getString(R.string.error_in_sharing));
                    return;
                }
                ShareManager.getInstance(CameraResultActivity.this).shareWhatsApp("", getImageFromLayout());
            }
        });

        ImageView saveImgView = (ImageView) itemView.findViewById(R.id.saveImgViewCR);
        saveImgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissTooltip();
                toolTipShareFlag = false;
                Bitmap mergedImages = getImageFromLayout();
                MediaStore.Images.Media.insertImage(getApplicationContext().getContentResolver(), mergedImages, imageName, "");
                Global.showSharedMessage(CameraResultActivity.this, getResources().getString(R.string.image_saved_on_device));
            }
        });

        ImageView copyImgView = (ImageView) itemView.findViewById(R.id.copyImgViewCR);
        copyImgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissTooltip();
                try {
                    Bitmap mergedImages = getImageFromLayout();
                    File cachePath = new File(getApplicationContext().getCacheDir(), "images");
                    cachePath.mkdirs();
                    FileOutputStream stream = new FileOutputStream(cachePath + "/image.png"); // overwrites this image every time
                    mergedImages.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    stream.close();

                    File imagePath = new File(getApplicationContext().getCacheDir(), "images");
                    File newFile = new File(imagePath, "image.png");
                    Uri contentUri = FileProvider.getUriForFile(getApplicationContext(), "com.cameracountmodule.fileprovider", newFile);

                    ClipboardManager mClipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ContentValues values = new ContentValues(2);
                    values.put(MediaStore.Images.Media.MIME_TYPE, "Image/jpg");
                    values.put(MediaStore.Images.Media.DATA, contentUri.toString());
                    ContentResolver theContent = getContentResolver();
                    Uri imageUri = theContent.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                    ClipData theClip = ClipData.newUri(getContentResolver(), "Image", contentUri);
                    mClipboard.setPrimaryClip(theClip);
                    Global.showSharedMessage(CameraResultActivity.this, getResources().getString(R.string.image_copied_to_clipboard));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        ImageView emailView = (ImageView) itemView.findViewById(R.id.emailImgViewCR);
        emailView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Bitmap mergedImages = getImageFromLayout();
                    File cachePath = new File(getApplicationContext().getCacheDir(), "images");
                    cachePath.mkdirs();
                    FileOutputStream stream = new FileOutputStream(cachePath + "/image.png"); // overwrites this image every time
                    mergedImages.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    stream.close();

                    File imagePath = new File(getApplicationContext().getCacheDir(), "images");
                    File newFile = new File(imagePath, "image.png");
                    Uri contentUri = FileProvider.getUriForFile(getApplicationContext(), "com.cameracountmodule.fileprovider", newFile);

                    Intent emailIntent = new Intent(Intent.ACTION_SEND);
                    emailIntent.setType("vnd.android.cursor.dir/email");
                    emailIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Visualogyx : Processed Image");
                    startActivity(Intent.createChooser(emailIntent, "Send email..."));

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        return itemView;
    }

    //Setting the image based on the selected shape
    private void setCurrentPipeType(int type) {
        if (type == 1)
            shapesMenu.setIcon(getResources().getDrawable(R.drawable.circle_type));
        else if (type == 2)
            shapesMenu.setIcon(getResources().getDrawable(R.drawable.rect_type));
        else if (type == 3)
            shapesMenu.setIcon(getResources().getDrawable(R.drawable.line_type));
        else if (type == 4)
            shapesMenu.setIcon(getResources().getDrawable(R.drawable.h_symbol));
        else if (type == 5)
            shapesMenu.setIcon(getResources().getDrawable(R.drawable.small_pipes));

    }

    //Callback from DrawingPad after Addition/Deletion
    public class DrawingResponse implements DrawingCallback {
        @Override
        public void changedOverlay() {
            if (Global.settingsManager.getShapeType() == Global.CIRCLE_TYPE_PIPE) {
                countPipeTV.setText(createSpannableString(dataClass.circles.size(), getPipeTypeString(Global.settingsManager.getShapeType(), dataClass.circles.size())));
                fadedPipeNameTimeTV.setText(countPipeTV.getText().toString() + userNameTV.getText().toString());

            } else if (Global.settingsManager.getShapeType() == Global.RECTANGLE_TYPE_PIPE) {
                countPipeTV.setText(createSpannableString(dataClass.rectangles.size(), getPipeTypeString(Global.settingsManager.getShapeType(), dataClass.rectangles.size())));
                fadedPipeNameTimeTV.setText(countPipeTV.getText().toString() + userNameTV.getText().toString());

            } else if (Global.settingsManager.getShapeType() == Global.SHEET_TYPE_PIPE) {
                countPipeTV.setText(createSpannableString(dataClass.lines.size(), getPipeTypeString(Global.settingsManager.getShapeType(), dataClass.lines.size())));
                fadedPipeNameTimeTV.setText(countPipeTV.getText().toString() + userNameTV.getText().toString());

            } else if (Global.settingsManager.getShapeType() == Global.SMALL_CIRCLE_TYPE_PIPE) {
                countPipeTV.setText(createSpannableString(dataClass.smallCircles.size(), getPipeTypeString(Global.settingsManager.getShapeType(), dataClass.smallCircles.size())));
                fadedPipeNameTimeTV.setText(countPipeTV.getText().toString() + userNameTV.getText().toString());
            } else {
                countPipeTV.setText(createSpannableString(dataClass.circles.size() + dataClass.lines.size() + dataClass.rectangles.size() + dataClass.smallCircles.size(), getPipeTypeString(Global.settingsManager.getShapeType(), dataClass.circles.size() + dataClass.lines.size() + dataClass.rectangles.size() + dataClass.smallCircles.size())));
                fadedPipeNameTimeTV.setText(countPipeTV.getText().toString() + userNameTV.getText().toString());
            }
            fadedTime.setText(timeStamp.getText().toString());
        }
    }

    //Callback from ImageCropper after Cropping done
    public class ImageCropcallback implements CropCallback {

        @Override
        public void doneCropping() {
            overLayImageView.setBackgroundColor(Color.TRANSPARENT);
            Bitmap result = dataClass.rawBitmap.copy(dataClass.rawBitmap.getConfig(), dataClass.rawBitmap.isMutable());
            resultImage.setImageBitmap(result);
            drawingPad.backgroundImage = result;
            drawingPad.overlayImageView = overLayImageView;
            updateAfterOperation();
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(CameraResultActivity.this, CameraActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        CameraResultActivity.this.finish();
        overridePendingTransition(0, 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_result, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
        /*switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(CameraResultActivity.this, CameraActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                CameraResultActivity.this.finish();
                return true;
            case R.id.done:
                dataClass.resultBitmap = getImageFromLayout();
                Intent main = new Intent(CameraResultActivity.this, MainActivity.class);
                main.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(main);
                CameraResultActivity.this.finish();
                return true;
            case R.id.crop:
                Intent crop = new Intent(CameraResultActivity.this, ImageCropper.class);
                ImageCropper.callback = new ImageCropcallback();
                startActivity(crop);
                return true;
            case R.id.recount:
                updateAfterOperation();
                return true;
            case R.id.share:
                if (toolTipShareFlag == false) {
                    toolTipShareFlag = true;
                    toolTip = createToolTip(findViewById(R.id.share));
                    abstractView.setVisibility(View.VISIBLE);
                    tipContainer.addTooltip(toolTip);
                    imageName = "Visualogyx_" + System.currentTimeMillis() + ".png";
                } else {
                    toolTipShareFlag = false;
                    dismissTooltip();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }*/
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /*public class LineDrawingResponse implements LineHintInterface {

        @Override
        public void currentLines(int count) {
            if(presetType.equals(Global.PROCESSED_IMAGE)) {
                if(count == 1) {
                    hintLine.setVisibility(View.VISIBLE);
                    hintLine.setText("Select another point to draw a line");
                } else {
                    hintLine.setVisibility(View.VISIBLE);
                    hintLine.setText("Select a point to draw a line");
                }
            }
        }
    }*/

}