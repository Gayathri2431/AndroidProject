package com.cameracountmodule.manager;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;

import com.cameracountmodule.Algorithm.ImageAnalysis;
import com.cameracountmodule.R;
import com.cameracountmodule.Utils.Global;
import com.cameracountmodule.activity.CameraActivity;
import com.cameracountmodule.cameraCropper.CameraBitmapUtils;
import com.cameracountmodule.interfaceUI.DetectionCallback;
import com.cameracountmodule.interfaceUI.FinishTakePhoto;
import com.cameracountmodule.model.DataClass;
import com.cameracountmodule.model.ImageResultType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint3f;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback {

    private SurfaceHolder mHolder;
    public Camera mCamera;
    private FinishTakePhoto finishTakePhoto;
    private DetectionCallback detectionCallback;
    private String cameraAccessFrom;

    private RenderScript rs;
    private ScriptIntrinsicYuvToRGB yuvToRgbIntrinsic;
    private Type.Builder yuvType, rgbaType;
    private Allocation in, out;
    private Context ctx;
    private ImageAnalysis imageAnalysis;
    private SurfaceHolder surfaceHolder;

    //record video
    private MediaRecorder mMediaRecorder;
    private int flag = 1;

    private int counterTime = 0;
    private Handler timerHandler;
    private Runnable timerRunnable;
    private ProgressDialog mProgressDialog;

    public CameraPreview(Context context, String type) {
        super(context);

        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        ctx = context;
        rs = RenderScript.create(context);
        yuvToRgbIntrinsic = ScriptIntrinsicYuvToRGB.create(rs, Element.U8_4(rs));
        cameraAccessFrom = type;
        imageAnalysis = new ImageAnalysis(ctx);
        mProgressDialog = new ProgressDialog(context);

        if (!Global.isCanRotateOrientation)
            initCamera();
    }

    public void setFinishTakePhotoInterface(FinishTakePhoto finishTakePhoto) {
        this.finishTakePhoto = finishTakePhoto;
    }

    public void setDetectionInterface(DetectionCallback detectionCallback) {
        this.detectionCallback = detectionCallback;
    }

    private void initCamera() {
        mCamera = openCamera();
        setParametersCamera();
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        } catch (IOException e) {
        }
    }

    private void initRecordCamera() {
        mCamera.unlock();

        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setCamera(mCamera);

        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_720P));
        mMediaRecorder.setPreviewDisplay(surfaceHolder.getSurface());

        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "VISUALOGYX/VISUALOGYX" + Global.getCurrentDateAndTime() + ".mp4";
        Global.currentPathVideo = path;
        mMediaRecorder.setOutputFile(path);

        if (Global.settingsManager.getTypeCamera() == Camera.CameraInfo.CAMERA_FACING_BACK)
            mMediaRecorder.setOrientationHint(90);
        else
            mMediaRecorder.setOrientationHint(270);

        try {
            mMediaRecorder.prepare();
        } catch (Exception e) {
        }

    }

    public void startRecordCamera() {
        initRecordCamera();
        mMediaRecorder.start();
    }

    public void stopRecordCamera() {
        try {
            if (mMediaRecorder != null) {
                mMediaRecorder.stop();
                mMediaRecorder.reset();
            }
        } catch (Exception e) {
        }
    }

    private void shutDownRecordCamera() {
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }

    private Camera openCamera() {
        int cameraID = Global.settingsManager.getTypeCamera();
        return Camera.open(cameraID);
    }

    private void setParametersCamera() {
        Camera.Parameters params = mCamera.getParameters();
        params.setPreviewSize(Global.processingImgWidth, Global.processingImgHeight);
        params.setPreviewFrameRate(25);

        Camera.Size pictureSize = getSmallestPictureSize(params);
        params.setPictureSize(640, 480);

        setFocusable(true);
        setFocusableInTouchMode(true);

        if (Global.settingsManager.getTypeCamera() == Camera.CameraInfo.CAMERA_FACING_BACK)
            params.setRotation(0);
        else
            params.setRotation(270);

        mCamera.setDisplayOrientation(90);
        try {
            mCamera.setParameters(params);
        } catch (Exception e) {
        }
    }

    public void switchCamera() {
        if (Global.settingsManager.getTypeCamera() == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            Global.settingsManager.setTypeCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
        } else {
            Global.settingsManager.setTypeCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
        }

        resetCamera();
    }

    public void resetCamera() {
        releaseCamera();
        initCamera();

        try {
            mCamera.setPreviewCallback(this);
            mCamera.setPreviewDisplay(surfaceHolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mCamera.startPreview();
    }

    private void releaseCamera() {

        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }

    public void focusCamera(float x, float y) {
        if (mCamera != null) {
            mCamera.cancelAutoFocus();
            Rect touchRect = new Rect(
                    (int) (x - 100),
                    (int) (y - 100),
                    (int) (x + 100),
                    (int) (y + 100));
            final Rect targetFocusRect = new Rect(
                    touchRect.left * 2000 / this.getWidth() - 1000,
                    touchRect.top * 2000 / this.getHeight() - 1000,
                    touchRect.right * 2000 / this.getWidth() - 1000,
                    touchRect.bottom * 2000 / this.getHeight() - 1000);
            doTouchFocus(targetFocusRect);
        }
    }

    public void doTouchFocus(final Rect tfocusRect) {
        try {
            final List<Camera.Area> focusList = new ArrayList<Camera.Area>();
            Camera.Area focusArea = new Camera.Area(tfocusRect, 1000);
            focusList.add(focusArea);

            Camera.Parameters para = mCamera.getParameters();
            para.setFocusAreas(focusList);
            para.setMeteringAreas(focusList);
            mCamera.setParameters(para);

            mCamera.autoFocus(myAutoFocusCallback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    Camera.AutoFocusCallback myAutoFocusCallback = new Camera.AutoFocusCallback() {

        @Override
        public void onAutoFocus(boolean arg0, Camera arg1) {
            if (arg0) {
                mCamera.cancelAutoFocus();
            }
        }
    };

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        try {
            if (mCamera == null) {
                initCamera();
            }

            this.surfaceHolder = surfaceHolder;
            mCamera.setPreviewDisplay(surfaceHolder);
            mCamera.startPreview();

        } catch (Exception e) {
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
        if (!Global.isRecordVideo) {
            if (mHolder.getSurface() == null)
                return;

            try {
                mCamera.stopPreview();
            } catch (Exception e) {
            }

            try {
                this.surfaceHolder = surfaceHolder;
                mCamera.setPreviewCallback(this);
                mCamera.setPreviewDisplay(mHolder);
                mCamera.startPreview();
            } catch (IOException e) {
            }
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        destroyCamera();
    }

    public void destroyCamera() {
        releaseCamera();
        shutDownRecordCamera();
    }

    private Camera.Size getBestPreviewSize(int width, int height, Camera.Parameters parameters) {
        Camera.Size result = null;
        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
            if (size.width <= width && size.height <= height) {
                if (result == null) {
                    result = size;
                } else {
                    int resultArea = result.width * result.height;
                    int newArea = size.width * size.height;

                    if (newArea > resultArea) {
                        result = size;
                    }
                }
            }
        }

        return (result);
    }

    private Camera.Size getSmallestPictureSize(Camera.Parameters parameters) {
        Camera.Size result = null;
        List<Camera.Size> sizeList = parameters.getSupportedPictureSizes();


        for (Camera.Size size : parameters.getSupportedPictureSizes()) {
            if (result == null) {
                result = size;
            } else {
                int resultArea = result.width * result.height;
                int newArea = size.width * size.height;

                return sizeList.get(sizeList.size() / 2 - 1);

            }
        }

        return (result);
    }

    private Camera.Size getBestPictureSize(Camera.Parameters params) {
        List<Camera.Size> sizeList = params.getSupportedPictureSizes();
        Camera.Size result = sizeList.get(0);
        for (Camera.Size size : sizeList) {
            float ratio = (float) size.height / size.width;
            if (ratio == Global.screenRatio) {
                if (result.width < size.width)
                    result = size;
            }
        }
        return result;
    }

    private Camera.Size getBestPreviewSize(Camera.Parameters params) {

        List<Camera.Size> sizeList = params.getSupportedPreviewSizes();
        int i = 0;
        List<Camera.Size> sizeList2 = new ArrayList<>();
        for (int j = 0; j < sizeList.size(); j++) {
            Camera.Size size = sizeList.get(j);
            float ratio = (float) size.height / size.width;
            if (ratio == Global.screenRatio) {
                sizeList2.add(size);
            }
        }
        if (sizeList2.size() > 1)
            return sizeList2.get(1);
        else
            return sizeList2.get(0);
    }

    public void take_photo() {
        AudioManager mgr = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
        mgr.setStreamMute(AudioManager.STREAM_SYSTEM, true);
        mCamera.takePicture(shutterCallback, rawCallback, jpegCallback);

    }

    Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
        public void onShutter() {
        }
    };

    Camera.PictureCallback rawCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
        }
    };

    Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            resetCamera();
            Bitmap bitmap = getBitmapFromSurface(data);
            Global.isRecordVideo=false;
            if (cameraAccessFrom.equals("home")) {
                if (finishTakePhoto != null) {
                    if(Global.settingsManager.getCountOverlay()) {
                        if (Global.settingsManager.getShapeType() == Global.CIRCLE_TYPE_PIPE) {
                            ImageAnalysis.setDetectedCirclesParameter();
                        } else if(Global.settingsManager.getShapeType() == Global.SMALL_CIRCLE_TYPE_PIPE) {
                            ImageAnalysis.setDetectedSmallCirclesParameter();
                        } else if(Global.settingsManager.getShapeType() == Global.SHEET_TYPE_PIPE) {
                            ImageAnalysis.setResultLinesParameters();
                        } else if(Global.settingsManager.getShapeType() == Global.RECTANGLE_TYPE_PIPE) {
                            ImageAnalysis.setResultRectanglesParameters();
                        }
                    }

                    finishTakePhoto.finish(bitmap);
                }
            } else {
                FileOutputStream outStream = null;
                try {
                    outStream = new FileOutputStream(String.format(
                            "/%s/still%d.jpg", Environment.getExternalStorageDirectory().getAbsolutePath(),
                            System.currentTimeMillis()));
                    outStream.write(data);
                    outStream.close();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        // TODO Auto-generated method stub

        Camera.Parameters parameters = camera.getParameters();
        int width = parameters.getPreviewSize().width;
        int height = parameters.getPreviewSize().height;

        mCamera.addCallbackBuffer(data);

        if (yuvType == null) {
            yuvType = new Type.Builder(rs, Element.U8(rs)).setX(data.length);
            in = Allocation.createTyped(rs, yuvType.create(), Allocation.USAGE_SCRIPT);

            rgbaType = new Type.Builder(rs, Element.RGBA_8888(rs)).setX(width).setY(height);
            out = Allocation.createTyped(rs, rgbaType.create(), Allocation.USAGE_SCRIPT);
        }

        in.copyFrom(data);

        yuvToRgbIntrinsic.setInput(in);
        yuvToRgbIntrinsic.forEach(out);

        Bitmap mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        out.copyTo(mBitmap);
        CameraActivity.resultImageType = ImageResultType.Took;
        CameraActivity.liveCamera.setVisibility(VISIBLE);
        if(Global.settingsManager.getLiveCamera()) {
            if (Global.settingsManager.getShapeType() == Global.CIRCLE_TYPE_PIPE) {
                if (!Global.isRecordVideo) {
                    Bitmap processedBitmap = imageAnalysis.circleDetection(mBitmap, width, height, true);
                    CameraActivity.liveCamera.setImageBitmap(processedBitmap);
                    ImageAnalysis.setDetectedCirclesParameter();
                    CameraActivity.drawingPad.backgroundImage = processedBitmap;
                    CameraActivity.drawingPad.drawOverlay();
                    if(flag == 1) {
                        CameraActivity.mCropView.setImageBitmap(processedBitmap);
                        flag++;
                    }
                } else {
                    Bitmap processedBitmap = imageAnalysis.process_image_tracking_circle(mBitmap, width, height, true);
                    CameraActivity.liveCamera.setImageBitmap(processedBitmap);
                    ImageAnalysis.setDetectedCirclesParameter();
                    CameraActivity.drawingPad.backgroundImage = processedBitmap;
                    CameraActivity.drawingPad.drawOverlay();
                    if(flag == 1) {
                        CameraActivity.mCropView.setImageBitmap(processedBitmap);
                        flag++;
                    }
                }
            } else if (Global.settingsManager.getShapeType() == Global.RECTANGLE_TYPE_PIPE) {
                Bitmap processedBitmap = imageAnalysis.process_image_contour_rectangle(mBitmap, true);
                CameraActivity.liveCamera.setImageBitmap(processedBitmap);
                ImageAnalysis.setResultRectanglesParameters();
                CameraActivity.drawingPad.backgroundImage = processedBitmap;
                CameraActivity.drawingPad.drawOverlay();
                if(flag == 1) {
                    CameraActivity.mCropView.setImageBitmap(processedBitmap);
                    flag++;
                }
            } else if (Global.settingsManager.getShapeType() == Global.SHEET_TYPE_PIPE) {
                Bitmap processedBitmap = imageAnalysis.process_image_hough_lines(mBitmap, true);
                CameraActivity.liveCamera.setImageBitmap(processedBitmap);
                ImageAnalysis.setResultLinesParameters();
                CameraActivity.drawingPad.backgroundImage = processedBitmap;
                CameraActivity.drawingPad.drawOverlay();
            } else if (Global.settingsManager.getShapeType() == Global.SMALL_CIRCLE_TYPE_PIPE) {
                if (!Global.isRecordVideo) {
                    Bitmap processedBitmap = imageAnalysis.process_image_hough_small_circles_selective(mBitmap, 2.0f, true);
                    CameraActivity.liveCamera.setImageBitmap(processedBitmap);
                    ImageAnalysis.setDetectedSmallCirclesParameter();
                    CameraActivity.drawingPad.backgroundImage = processedBitmap;
                    CameraActivity.drawingPad.drawOverlay();
                    if(flag == 1) {
                        CameraActivity.mCropView.setImageBitmap(processedBitmap);
                        flag++;
                    }
                } else {
                    Bitmap processedBitmap = imageAnalysis.process_image_tracking_small_circle(mBitmap, width, height, true);
                    CameraActivity.liveCamera.setImageBitmap(processedBitmap);
                    ImageAnalysis.setDetectedSmallCirclesParameter();
                    CameraActivity.drawingPad.backgroundImage = processedBitmap;
                    CameraActivity.drawingPad.drawOverlay();
                    if(flag == 1) {
                        CameraActivity.mCropView.setImageBitmap(processedBitmap);
                        flag++;
                    }
                }
            } else if(Global.settingsManager.getShapeType() == Global.MULTI_TYPE_PIPE) {
                Bitmap processedBitmap = imageAnalysis.process_image_multi_shape(mBitmap,true);
                CameraActivity.liveCamera.setImageBitmap(processedBitmap);
                CameraActivity.drawingPad.backgroundImage = processedBitmap;
                CameraActivity.drawingPad.drawOverlay();
                if(flag == 1) {
                    CameraActivity.mCropView.setImageBitmap(processedBitmap);
                    flag++;
                }
            } else {
                mProgressDialog.setMessage("Detecting Shapes");
                mProgressDialog.show();
                playTimer(mBitmap);
            }
        } else {
            if (Global.settingsManager.getTypeCamera() == Camera.CameraInfo.CAMERA_FACING_BACK) {
                mBitmap = rotate(mBitmap, 90);
            } else if (Global.mOrientation == Global.ORIENTATION_PORTRAIT_NORMAL) {
                mBitmap = rotate(mBitmap, -90);
            } else if (Global.mOrientation == Global.ORIENTATION_LANDSCAPE_NORMAL) {
                mBitmap = rotate(mBitmap, 90);
            } else if (Global.mOrientation == Global.ORIENTATION_LANDSCAPE_INVERTED) {
                mBitmap = rotate(mBitmap, -270);
            }
            CameraActivity.liveCamera.setImageBitmap(mBitmap);
            if(flag == 1) {
                CameraActivity.mCropView.setImageBitmap(mBitmap);
                flag++;
            }
        }
    }

    public Bitmap getBitmapFromSurface(byte[] data) {

        try {
            Bitmap realImage = BitmapFactory.decodeByteArray(data, 0, data.length);

            switch (Global.mOrientation) {
                case Global.ORIENTATION_PORTRAIT_NORMAL:
                    if (Global.settingsManager.getTypeCamera() == Camera.CameraInfo.CAMERA_FACING_FRONT)
                        realImage = rotate(realImage, -90);
                    else
                        realImage = rotate(realImage, 90);
                    break;
                case Global.ORIENTATION_LANDSCAPE_NORMAL:
                    if (Global.settingsManager.getTypeCamera() == Camera.CameraInfo.CAMERA_FACING_FRONT)
                        realImage = rotate(realImage, 0);
                    else
                        realImage = rotate(realImage, 0);
                    break;
                case Global.ORIENTATION_PORTRAIT_INVERTED:
                    if (Global.settingsManager.getTypeCamera() == Camera.CameraInfo.CAMERA_FACING_FRONT)
                        realImage = rotate(realImage, -270);
                    else
                        realImage = rotate(realImage, 270);
                    break;
                case Global.ORIENTATION_LANDSCAPE_INVERTED:
                    if (Global.settingsManager.getTypeCamera() == Camera.CameraInfo.CAMERA_FACING_FRONT)
                        realImage = rotate(realImage, -180);
                    else
                        realImage = rotate(realImage, 180);
                    break;
            }

            if ((Global.settingsManager.getTypeCamera() == Camera.CameraInfo.CAMERA_FACING_FRONT)) {
                Matrix matrix = new Matrix();
                matrix.setScale(-1, 1);
                realImage = Bitmap.createBitmap(realImage, 0, 0, realImage.getWidth(), realImage.getHeight(), matrix, true);
            }

            return realImage;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Bitmap rotate(Bitmap bitmap, int degree) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        Matrix mtx = new Matrix();
        mtx.setRotate(degree);
        return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
    }

    public void setFlashMode(String m) {
        if (mCamera == null)
            return;

        Camera.Parameters params = mCamera.getParameters();
        switch (m) {
            case "on":
                params.setFlashMode("on");
                break;
            case "off":
                params.setFlashMode("off");
                break;
            case "auto":
                params.setFlashMode("auto");
                break;
        }
        mCamera.setParameters(params);
    }

    public boolean hasFlash() {
        if (mCamera == null)
            return false;
        Camera.Parameters params = mCamera.getParameters();
        List<String> flashModes = params.getSupportedFlashModes();
        if (flashModes == null) {
            return false;
        }

        for (String flashMode : flashModes) {
            if (Camera.Parameters.FLASH_MODE_ON.equals(flashMode)) {
                return true;
            }
        }
        return false;
    }

    public void freezeCamera() {
        mCamera.stopPreview();
        mCamera.setPreviewCallback(null);
    }

    public void unfreezeCamera() {
        mCamera.startPreview();
    }

    public void handleZoom(MotionEvent event) {
        Camera.Parameters params = mCamera.getParameters();
        int maxZoom = params.getMaxZoom();
        int zoom = params.getZoom();
        float newDist = getFingerSpacing(event);
        if (newDist > CameraActivity.mDist) {
            //zoom in
            if (zoom < maxZoom)
                zoom++;
        } else if (newDist < CameraActivity.mDist) {
            //zoom out
            if (zoom > 0)
                zoom--;
        }
        CameraActivity.mDist = newDist;
        params.setZoom(zoom);
        mCamera.setParameters(params);
    }

    private float getFingerSpacing(MotionEvent event) {
        // ...
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float)Math.sqrt(x * x + y * y);
    }

    public void playTimer(final Bitmap bitmap) {
        timerHandler = new Handler();
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (counterTime == 400) {
                    Bitmap processedBitmap = imageAnalysis.circleDetection(bitmap, 640, 480, true);
                    ImageAnalysis.setDetectedCirclesParameter();
                } else if(counterTime == 500) {
                    Bitmap processedBitmap = imageAnalysis.process_image_contour_rectangle(bitmap, true);
                    ImageAnalysis.setResultRectanglesParameters();
                } else if(counterTime == 600) {
                    Bitmap processedBitmap = imageAnalysis.process_image_hough_small_circles_selective(bitmap, 2.0f, true);
                    ImageAnalysis.setDetectedSmallCirclesParameter();
                } else if(counterTime == 700) {
                    Bitmap processedBitmap = imageAnalysis.process_image_hough_lines(bitmap, true);
                    ImageAnalysis.setResultLinesParameters();
                }

                if(counterTime > 700) {
                    DataClass data = DataClass.getInstance();
                    if(data.circles.size() > data.rectangles.size() && data.circles.size() > data.smallCircles.size() && data.circles.size() > data.lines.size()) {
                        timerHandler.removeCallbacks(timerRunnable);
                        Global.settingsManager.setShapeType(Global.CIRCLE_TYPE_PIPE);
                        mProgressDialog.cancel();
                        detectionCallback.detected();
                    } else if(data.rectangles.size() > data.circles.size() && data.rectangles.size() > data.smallCircles.size() && data.rectangles.size() > data.lines.size()) {
                        timerHandler.removeCallbacks(timerRunnable);
                        Global.settingsManager.setShapeType(Global.RECTANGLE_TYPE_PIPE);
                        mProgressDialog.cancel();
                        detectionCallback.detected();
                    } else if(data.smallCircles.size() > data.circles.size() && data.smallCircles.size() > data.rectangles.size() && data.smallCircles.size() > data.lines.size()) {
                        timerHandler.removeCallbacks(timerRunnable);
                        Global.settingsManager.setShapeType(Global.SMALL_CIRCLE_TYPE_PIPE);
                        mProgressDialog.cancel();
                        detectionCallback.detected();
                    } else if(data.lines.size() > data.circles.size() && data.lines.size() > data.smallCircles.size() && data.lines.size() > data.smallCircles.size()) {
                        timerHandler.removeCallbacks(timerRunnable);
                        Global.settingsManager.setShapeType(Global.SHEET_TYPE_PIPE);
                        mProgressDialog.cancel();
                        detectionCallback.detected();
                    } else {
                        counterTime = 300;
                        timerHandler.postDelayed(this, 100);
                    }
                } else {
                    counterTime += 100;
                    timerHandler.postDelayed(this, 100);
                }
            }
        };
        timerHandler.postDelayed(timerRunnable, 0);
    }
}