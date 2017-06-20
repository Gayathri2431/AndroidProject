package com.cameracountmodule.activity;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.cameracountmodule.Algorithm.ImageAnalysis;
import com.cameracountmodule.R;
import com.cameracountmodule.Utils.Global;
import com.cameracountmodule.cameraCropper.CameraBitmapUtils;
import com.cameracountmodule.cameraCropper.CameraCropImageView;
import com.cameracountmodule.interfaceUI.DetectionCallback;
import com.cameracountmodule.interfaceUI.DrawingCallback;
import com.cameracountmodule.interfaceUI.FinishTakePhoto;
import com.cameracountmodule.manager.CameraPreview;
import com.cameracountmodule.manager.DrawingPad;
import com.cameracountmodule.manager.SettingsManager;
import com.cameracountmodule.model.DataClass;
import com.cameracountmodule.model.ImageResultType;
import com.mikepenz.iconics.view.IconicsImageView;
import com.mikhaellopez.circularimageview.CircularImageView;

import toan.android.floatingactionmenu.FloatingActionButton;
import toan.android.floatingactionmenu.FloatingActionsMenu;

public class CameraActivity extends AppCompatActivity {

    private CameraPreview cameraPreview;
    private ImageView galleryBtn, switchBtn, cropBtn;
    private IconicsImageView flashBtn;
    private CircularImageView captureBtn, captureSmallBtn;
    private OrientationEventListener mOrientationEventListener;
    private RelativeLayout captureRL, sliderLayout;
    private FloatingActionsMenu shapesMenu;
    private FloatingActionButton circlePipe, rectanglePipe, sheetPipe, multiShape, smallPipebutton;
    private ProgressBar progressBar;
    private boolean isTakingPicture = false;
    private ImageResultType imageResultType;
    private ImageView overLayImageView;

    private int shapeType = 1;
    private boolean autoRotation = true;
    private boolean takePhoto = false;
    private DataClass dataClass;

    public static ImageResultType resultImageType;
    public static CameraCropImageView mCropView;
    public static DrawingPad drawingPad;
    public static ImageView liveCamera;
    public static float mDist = 0;

    private int counterTime = 0;
    private Handler timerHandler;
    private Runnable timerRunnable;
    private Handler captureHandler;

    private boolean cropShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        initHome();
    }

    //Initialize the layout views
    private void initHome() {
        Global.settingsManager = SettingsManager.getInstance(getApplicationContext());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        shapesMenu = (FloatingActionsMenu) findViewById(R.id.shapesMenu);
        shapesMenu.setVisibility(View.VISIBLE);
        progressBar = (ProgressBar) findViewById(R.id.circular_animation);
        shapeType = 0;
        sliderLayout = (RelativeLayout) findViewById(R.id.sliderLayout);
        captureRL = (RelativeLayout) findViewById(R.id.captureRL);
        liveCamera = (ImageView) findViewById(R.id.live_camera);
        mCropView = (CameraCropImageView) findViewById(R.id.cropImageView);
        overLayImageView = (ImageView) findViewById(R.id.overLayImageView);
        drawingPad = new DrawingPad(CameraActivity.this, new DrawingResponse());
        overLayImageView.setBackgroundColor(Color.TRANSPARENT);
        drawingPad.overlayImageView = overLayImageView;

        mCropView.setAutoZoomEnabled(false);

        galleryBtn = (ImageView) findViewById(R.id.galleryBtn);
        galleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, 1);
            }
        });

        captureBtn = (CircularImageView) findViewById(R.id.captureBtn);
        captureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        captureSmallBtn = (CircularImageView) findViewById(R.id.captureSmallBtn);
        captureSmallBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (isTakingPicture)
                            return false;
                        isTakingPicture = true;
                        if (Global.settingsManager.getOpticalFlow()) {
                            if (imageResultType != ImageResultType.Picked) {
                                Global.isRecordVideo = false;
                                counterTime = 0;
                                playTimer();
                            }
                        }
                        captureSmallBtn.setBorderColor(R.color.colorRed);
                        return true;
                    case MotionEvent.ACTION_UP:
                        if (imageResultType != ImageResultType.Picked) {
                            if (timerHandler != null)
                                timerHandler.removeCallbacks(timerRunnable);
                            counterTime = 0;
                            if (Global.isRecordVideo && takePhoto == false) {
                                takePhoto = true;
                                cameraPreview.take_photo();
                                imageResultType = ImageResultType.Recorded;
                                resultImageType = ImageResultType.Recorded;
                            } else {
                                if (cameraPreview.mCamera != null && takePhoto == false) {
                                    takePhoto = true;
                                    resultImageType = ImageResultType.Took;
                                    cameraPreview.take_photo();
                                }
                            }
                        } else {
                            sliderLayout.clearAnimation();
                            shapesMenu.clearAnimation();
                            galleryBtn.clearAnimation();
                            captureRL.clearAnimation();

                            sliderLayout.setVisibility(View.INVISIBLE);
                            shapesMenu.setVisibility(View.INVISIBLE);
                            galleryBtn.setVisibility(View.INVISIBLE);
                            captureRL.setVisibility(View.INVISIBLE);
                        }
                        captureSmallBtn.setBorderColor(R.color.white);
                        captureBtn.setBorderColor(R.color.captureColor);
                        return true;
                }
                return false;
            }
        });

        flashBtn = (IconicsImageView) findViewById(R.id.flashBtn);
        flashBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFlashMode();
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
            }
        });

        switchBtn = (ImageView) findViewById(R.id.switchBtn);
        switchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int camerasNumber = Camera.getNumberOfCameras();
                if (camerasNumber > 1) {
                    cameraPreview.switchCamera();
                    changeFlashMode();
                } else {
                    Toast toast = Toast.makeText(getApplicationContext(), R.string.one_camera_in_phone, Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        });

        cropBtn = (ImageView) findViewById(R.id.cropBtn);
        cropBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            if(cropShown == false) {
                mCropView.setVisibility(View.VISIBLE);
                cropShown = true;
            } else {
                cropShown = false;
                mCropView.setVisibility(View.INVISIBLE);
            }
            }
        });

        Global.settingsManager.setShapeType(0);
        setCurrentPipeType(shapeType);
        initCamera();

    }

    //Initialize the Camera
    private void initCamera() {
        cameraPreview = new CameraPreview(this, "home");
        final FrameLayout camera_view = (FrameLayout) findViewById(R.id.camera_view);
        camera_view.addView(cameraPreview);
        cameraPreview.setFinishTakePhotoInterface(new FinishTakePhotoResponse());
        cameraPreview.setDetectionInterface(new DetectionResponse());

        RelativeLayout rl = (RelativeLayout) findViewById(R.id.camera_preview);
        rl.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                System.out.println("Count " + event.getPointerCount());
                int count = event.getPointerCount();
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (count > 1) {
                            // handle multi-touch events
                            mDist = getFingerSpacing(event);
                        } else {
                            // handle single touch events
                            float x = event.getX();
                            float y = event.getY();
                            cameraPreview.focusCamera(x, y);
                        }
                        return true;
                    case MotionEvent.ACTION_UP:
                        if (count > 1) {
                            cameraPreview.handleZoom(event);
                        }
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        if (count > 1) {
                            cameraPreview.handleZoom(event);
                        }
                        return true;
                }
                return true;
            }
        });

        setFlashMode();
    }

    //Getting finger spacing for Zoom operation
    private float getFingerSpacing(MotionEvent event) {
        // ...
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    //Setting up flash mode
    private void setFlashMode() {
        if (cameraPreview.hasFlash() && Global.settingsManager.getTypeCamera() == 0) {
            if (Global.settingsManager.getFlashMode().compareTo("off") == 0)
                turnOffFlashCamera();
            else if (Global.settingsManager.getFlashMode().compareTo("on") == 0)
                turnOnFlashCamera();
        } else
            turnOffFlashCamera();
    }

    //Changing up flash mode
    private void changeFlashMode() {
        if (cameraPreview.hasFlash() && Global.settingsManager.getTypeCamera() == 0) {
            if (Global.settingsManager.getFlashMode().compareTo("off") == 0)
                turnOnFlashCamera();
            else if (Global.settingsManager.getFlashMode().compareTo("on") == 0)
                turnOffFlashCamera();
        } else
            turnOffFlashCamera();
    }

    //Turning up flash mode
    private void turnOnFlashCamera() {
        flashBtn.setIcon("ion-ios-bolt");
        cameraPreview.setFlashMode("on");
        Global.settingsManager.setFlashMode("on");
    }

    //Turning off flash mode
    private void turnOffFlashCamera() {
        flashBtn.setIcon("ion-flash-off");
        cameraPreview.setFlashMode("off");
        Global.settingsManager.setFlashMode("off");
    }

    //Animation for Tracking the shape operation
    public void playTimer() {
        timerHandler = new Handler();
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (counterTime > 300) {
                    captureBtn.setBorderColor(R.color.colorRed);
                    Global.isRecordVideo = true;
                    final ObjectAnimator animation = ObjectAnimator.ofInt(progressBar, "progress", 0, 500);
                    animation.setDuration(4000); //in milliseconds
                    animation.setInterpolator(new DecelerateInterpolator());
                    animation.start();
                    captureHandler = new Handler();
                    captureHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (cameraPreview.mCamera != null) {
                                if (takePhoto == false) {
                                    cameraPreview.take_photo();
                                    animation.cancel();
                                    //Global.isRecordVideo = false;
                                    takePhoto = true;
                                }
                            }
                        }
                    }, 4000);
                    timerHandler.removeCallbacks(timerRunnable);
                } else {
                    counterTime += 100;
                    timerHandler.postDelayed(this, 100);
                }
            }
        };
        timerHandler.postDelayed(timerRunnable, 0);
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

    //Callback from Camera after taking picture
    public class FinishTakePhotoResponse implements FinishTakePhoto {
        @Override
        public void finish(Bitmap bitmap) {

            imageResultType = ImageResultType.Took;
            resultImageType = ImageResultType.Took;
            cameraPreview.freezeCamera();

            if(cropShown) {
                imageResultType = ImageResultType.Picked;
                resultImageType = ImageResultType.Picked;
                Bitmap croppedBitmap = CameraBitmapUtils.cropBitmapObjectHandleOOM(bitmap, mCropView.getCropPoints(), 0,
                        mCropView.mCropOverlayView.isFixAspectRatio(), mCropView.mCropOverlayView.getAspectRatioX(), mCropView.mCropOverlayView.getAspectRatioY(),
                        mCropView.mFlipHorizontally, mCropView.mFlipVertically).bitmap;

                ImageAnalysis imageAnalysis = new ImageAnalysis(getApplicationContext());
                Bitmap processedBitmap = null;

                if (Global.settingsManager.getShapeType() == Global.CIRCLE_TYPE_PIPE) {
                    processedBitmap = imageAnalysis.circleDetection(croppedBitmap, 640, 480, false);
                    ImageAnalysis.setDetectedCirclesParameter();
                } else if (Global.settingsManager.getShapeType() == Global.RECTANGLE_TYPE_PIPE) {
                    processedBitmap = imageAnalysis.process_image_contour_rectangle(croppedBitmap, false);
                    ImageAnalysis.setResultRectanglesParameters();
                } else if (Global.settingsManager.getShapeType() == Global.SHEET_TYPE_PIPE) {
                    processedBitmap = imageAnalysis.process_image_hough_lines(croppedBitmap, false);
                    ImageAnalysis.setResultLinesParameters();
                } else if (Global.settingsManager.getShapeType() == Global.SMALL_CIRCLE_TYPE_PIPE) {
                    processedBitmap = imageAnalysis.process_image_hough_small_circles_selective(croppedBitmap, 2.0f, false);
                    ImageAnalysis.setDetectedSmallCirclesParameter();
                } else {
                    processedBitmap = imageAnalysis.process_image_multi_shape(croppedBitmap, false);
                }
                dataClass = DataClass.getInstance();
                dataClass.rawBitmap = Bitmap.createBitmap(croppedBitmap);
                dataClass.processedBitmap = processedBitmap;
            } else {
                Bitmap processedBitmap = ((BitmapDrawable) liveCamera.getDrawable()).getBitmap();
                dataClass = DataClass.getInstance();
                dataClass.rawBitmap = Bitmap.createBitmap(bitmap);
                dataClass.processedBitmap = processedBitmap;
            }
            imageResultType = ImageResultType.Took;
            resultImageType = ImageResultType.Took;
            Intent cameraResult = new Intent(CameraActivity.this, CameraResultActivity.class);
            startActivity(cameraResult);
        }

        @Override
        public void changeRotation() {

        }
    }

    //Callback from Auto Detection
    public class DetectionResponse implements DetectionCallback {

        @Override
        public void detected() {
            setCurrentPipeType(Global.settingsManager.getShapeType());
        }
    }

    //Callback from DrawingPad after Addition/Deletion
    public class DrawingResponse implements DrawingCallback {
        @Override
        public void changedOverlay() {

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == 1 && resultCode == Activity.RESULT_OK && null != data) {
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};

                Cursor cursor = this.getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String imgDecodableString = cursor.getString(columnIndex);
                cursor.close();

                Bitmap currentBitmap = BitmapFactory.decodeFile(imgDecodableString);
                imageResultType = ImageResultType.Picked;
                resultImageType = ImageResultType.Picked;
                //cameraPreview.freezeCamera();

                ImageAnalysis imageAnalysis = new ImageAnalysis(getApplicationContext());
                Bitmap toProcess = Bitmap.createBitmap(currentBitmap);
                Bitmap processedBitmap = null;

                if (Global.settingsManager.getShapeType() == Global.CIRCLE_TYPE_PIPE) {
                    processedBitmap = imageAnalysis.circleDetection(toProcess, 640, 480, false);
                    ImageAnalysis.setDetectedCirclesParameter();
                } else if (Global.settingsManager.getShapeType() == Global.RECTANGLE_TYPE_PIPE) {
                    processedBitmap = imageAnalysis.process_image_contour_rectangle(toProcess, false);
                    ImageAnalysis.setResultRectanglesParameters();
                } else if (Global.settingsManager.getShapeType() == Global.SHEET_TYPE_PIPE) {
                    processedBitmap = imageAnalysis.process_image_hough_lines(toProcess, false);
                    ImageAnalysis.setResultLinesParameters();
                } else if (Global.settingsManager.getShapeType() == Global.SMALL_CIRCLE_TYPE_PIPE) {
                    processedBitmap = imageAnalysis.process_image_hough_small_circles_selective(toProcess, 2.0f, false);
                    ImageAnalysis.setDetectedSmallCirclesParameter();
                } else {
                    processedBitmap = imageAnalysis.process_image_multi_shape(toProcess, false);
                }
                dataClass = DataClass.getInstance();
                dataClass.rawBitmap = Bitmap.createBitmap(currentBitmap);
                dataClass.processedBitmap = processedBitmap;
                finish();
                Intent cameraResult = new Intent(CameraActivity.this, CameraResultActivity.class);
                startActivity(cameraResult);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            if (mOrientationEventListener == null) {
                mOrientationEventListener = new OrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL) {
                    @Override
                    public void onOrientationChanged(int orientation) {
                        if (!Global.isCanRotateOrientation) {
                            int lastOrientation = Global.mOrientation;
                            if (orientation >= 315 || orientation < 45) {
                                if (Global.mOrientation != Global.ORIENTATION_PORTRAIT_NORMAL) {
                                    Global.mOrientation = Global.ORIENTATION_PORTRAIT_NORMAL;
                                }
                            } else if (orientation < 315 && orientation >= 225) {
                                if (Global.mOrientation != Global.ORIENTATION_LANDSCAPE_NORMAL) {
                                    Global.mOrientation = Global.ORIENTATION_LANDSCAPE_NORMAL;
                                }
                            } else if (orientation < 225 && orientation >= 135) {
                                if (Global.mOrientation != Global.ORIENTATION_PORTRAIT_INVERTED) {
                                    Global.mOrientation = Global.ORIENTATION_PORTRAIT_INVERTED;
                                }
                            } else { // orientation <135 && orientation > 45
                                if (Global.mOrientation != Global.ORIENTATION_LANDSCAPE_INVERTED) {
                                    Global.mOrientation = Global.ORIENTATION_LANDSCAPE_INVERTED;
                                }
                            }
                            if (lastOrientation != Global.mOrientation) {
                                if (Global.getAutoRotationStatus(getApplicationContext()) != true) {
                                    autoRotation = false;
                                }
                            }
                        }
                    }
                };
            }
            if (mOrientationEventListener.canDetectOrientation()) {
                mOrientationEventListener.enable();
            }
        } catch (Exception e) {

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mOrientationEventListener != null)
            mOrientationEventListener.disable();
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(0, 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
        /*switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.settings:
                Intent intent = new Intent(CameraActivity.this, SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }*/
    }
}