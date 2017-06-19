package com.cameracountmodule.Utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.provider.Settings;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.v4.app.ActivityCompat;

import com.afollestad.materialdialogs.MaterialDialog;
import com.cameracountmodule.R;
import com.cameracountmodule.manager.SettingsManager;
import com.cameracountmodule.manager.ViewExtras;

import org.opencv.core.Core;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class Global {

    public static Context context;
    public static SettingsManager settingsManager;
    public static float screenRatio;

    public static int screenWidth;
    public static int screenHeight;
    public static boolean isTracking ;


    //Set the Image size to be sent to OpenCv
    public static int processingImgWidth = 640;
    public static int processingImgHeight = 480;

    public static String currentPathVideo = "";
    public static String currentPicturePath = "";

    public static boolean isRecordVideo = false;
    public static boolean isCanRotateOrientation = false;

    public static boolean sendForTraining = false;
    public static Bitmap currentBitmap;

    public static int mOrientation =  -1;
    public static int rectangles =  0;
    public static int lines =  0;
    public static int circles =  0;

    public static final String ORIGINAL_IMAGE ="1";
    public static final String PROCESSED_IMAGE = "2";
    public static final String TRAINING_IMAGE = "3";

    public static final int CIRCLE_TYPE_PIPE = 1;
    public static final int RECTANGLE_TYPE_PIPE = 2;
    public static final int SHEET_TYPE_PIPE = 3;
    public static final int MULTI_TYPE_PIPE = 4;
    public static final int SMALL_CIRCLE_TYPE_PIPE =5;

    public static final int ORIENTATION_PORTRAIT_NORMAL =  1;
    public static final int ORIENTATION_PORTRAIT_INVERTED =  2;
    public static final int ORIENTATION_LANDSCAPE_NORMAL =  3;
    public static final int ORIENTATION_LANDSCAPE_INVERTED =  4;

    // Hough transform image processing constants
    public static final int k_gaussian_kernel_size = 19;
    public static final int k_gaussian_kernel_deviation = 2;
    public static final double k_accumulator_resolution = 1.0;
    public static final double k_min_circle_distance = 27.0;
    public static final double k_canny_param_1 = 64.0;
    public static final double k_canny_param_2 = 54.0;
    public static final int k_min_radius = 25;
    public static final int k_max_radius = 83;

    // Drawing constants
    public static final Scalar k_outline_color = new Scalar(93, 255, 254,250);
    public static final Scalar k_circle_color = new Scalar(93, 255, 254,250);
    public static final int k_circle_thickness = 2;
    public static final Scalar k_crosshair_color = new Scalar(0, 255, 0,250);
    public static final int k_crosshair_length = 12;
    public static final int k_crosshair_thickness = 1;
    public static final int k_crosshair_type = Imgproc.MARKER_CROSS;
    public static final int k_crosshair_circle_radius = 4;
    public static final Scalar k_text_color= new Scalar(200, 200, 200,250);
    public static final int k_text_thickness = 1;
    //public static final int k_text_font = FONT_HERSHEY_SIMPLEX;
    public static final int k_text_offset_x = 5;
    public static final int k_text_offset_y = 20;
    public static final int k_line_type = Core.LINE_8;
    public static final int k_line_thickness = 2;

    // Cascade classifier image processing constants
    public static final String k_pipe_classifier_model = "black_pipe_small.xml";
    public static final double k_classifier_scale_factor = 1.1;
    public static final int k_minimum_neighbours = 2;
    public static final int k_haar_detect_flags = 2;
    public static final int k_min_obj_size = 30;
    public static final int k_max_objsize = 200;

    // Contour rectangle_add image processing constants 
    // /////////////////////////// 
    public static double k_threshold_max = 255;
    public static double k_threshold_step = 26;
    public static double k_shape_appoximator_ratio = 0.03;
    public static double k_min_contour_area = 250;
    public static double k_max_contour_area_divisor = 5.0;
    public static double k_cosine_angle = 0.4;
    public static double k_centroid_min_distance = 25.0;


    // Hough line image processing constants
    public static double k_min_canny_threshold = 50;
    public static  double k_max_canny_threshold = 200;
    public static  double k_hough_line_rho = 1.0;
    public static  double k_hough_line_theta = Math.PI / 180.0;
    public static  double k_hough_line_threshold = 40.0;
    public static  double k_hough_line_length_factor = 0.35;
    public static  double k_hough_line_max_inter_distance = 10.0;

    private static LocationListener myLocationListener;
    private static Location location;
    private static LocationManager myLocationManager;

    public static String getCurrentDateAndTime() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        String formattedDate = df.format(c.getTime());
        return formattedDate;
    }

    public static Bitmap rotate(Bitmap bitmap, int degree) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        Matrix mtx = new Matrix();
        mtx.setRotate(degree);
        return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
    }
    public static final float GEOFENCE_RADIUS_IN_METERS = 2000;

    public static boolean getAutoRotationStatus(Context context) {
        if (android.provider.Settings.System.getInt(context.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0) == 1)

            return true;
        else
            return false;
    }

    public static org.jcodec.common.model.Point convertIntoOriginalPipeCenterFormat(org.jcodec.common.model.Point point) {
        org.jcodec.common.model.Point tapPoint;
        int pointX = 0, pointY = 0;

        if (Global.mOrientation == 1) // for Portrait orientation
        {
            pointX = Math.abs(point.getY() - Global.processingImgHeight);
            pointY = point.getX();
        } else if (Global.mOrientation == 2) // for reverse portrait orientation
        {
            pointX = Math.abs(point.getY());
            pointY = Math.abs(point.getX() - Global.processingImgWidth);

        } else if (Global.mOrientation == 3)  //for landscape orientation
        {
            pointX = point.getX();
            pointY = point.getY();
        } else if (Global.mOrientation == 4)  //for reverse landscape orientation
        {
            pointX = Math.abs(point.getX() - Global.processingImgWidth);
            pointY = Math.abs(point.getY() - Global.processingImgHeight);
        }
        tapPoint = new org.jcodec.common.model.Point(pointX, pointY);

        return tapPoint;

    }

    public static org.jcodec.common.model.Point convertCenterFormat(org.opencv.core.Point point) {
        org.jcodec.common.model.Point tapPoint;
        int pointX = 0, pointY = 0;

        if (Global.mOrientation == 1) // for Portrait orientation
        {
            pointX = (int) Math.abs(point.y - Global.processingImgHeight);
            pointY = (int) point.x;
        } else if (Global.mOrientation == 2) // for reverse portrait orientation
        {
            pointX = (int) Math.abs(point.y);
            pointY = (int) Math.abs(point.x - Global.processingImgWidth);

        } else if (Global.mOrientation == 3)  //for landscape orientation
        {
            pointX = (int) point.x;
            pointY = (int) point.y;
        } else if (Global.mOrientation == 4)  //for reverse landscape orientation
        {
            pointX = (int) Math.abs(point.x - Global.processingImgWidth);
            pointY = (int) Math.abs(point.y - Global.processingImgHeight);
        }
        tapPoint = new org.jcodec.common.model.Point(pointX, pointY);

        return tapPoint;

    }

    public static Location getLocation() {
        return location;
    }

    public static String lastLocation(Context context) throws IOException {
        String adr = "";
        myLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        if (myLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                int currentapiVersion = android.os.Build.VERSION.SDK_INT;

                if (currentapiVersion >= 23) {
                    ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

                }
                return "Permission Error";
            }
            location = myLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (location == null) {
                myLocationListener = new MyLocationListener();

                myLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, myLocationListener);

            }
            if (location != null) {
                Geocoder gCoder = new Geocoder(context, Locale.ENGLISH);
                List<Address> addresses = gCoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                if (addresses != null && addresses.size() > 0) {

                    adr = addresses.get(0).getAddressLine(0);

                    if (addresses.get(0).getAddressLine(1) != null)
                    {
                        adr += ", ";
                        adr += addresses.get(0).getAddressLine(1);
                    }
                    if (addresses.get(0).getAddressLine(2) != null)
                    {
                        adr += ", ";
                        adr += addresses.get(0).getAddressLine(2);
                    }
                    if (addresses.get(0).getAddressLine(3) != null)
                    {
                        adr += ", ";
                        adr += addresses.get(0).getAddressLine(3);
                    }
                    if (addresses.get(0).getAddressLine(4) != null)
                    {
                        adr += ", ";
                        adr += addresses.get(0).getAddressLine(4);
                    }
                    if (addresses.get(0).getAddressLine(5) != null)
                    {
                        adr += ", ";
                        adr += addresses.get(0).getAddressLine(5);
                    }
                }

            }
            return adr;

        } else {
            return "";
        }
    }

    private static class MyLocationListener extends ActivityCompat implements LocationListener {
        public void onLocationChanged(Location argLocation) {
            // TODO Auto-generated method stub

            location = argLocation;

        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub
        }

        public void onStatusChanged(String provider,
                                    int status, Bundle extras) {
            // TODO Auto-generated method stub
        }
    }

    public static Bitmap blur(Context context, Bitmap image) {
        int mWidth = Math.round(image.getWidth() );
        int mHeight = Math.round(image.getHeight() );
        Bitmap givenBitmap = Bitmap.createScaledBitmap(image, mWidth, mHeight, false);
        Bitmap takenBitmap = Bitmap.createBitmap(givenBitmap);
        RenderScript rs = RenderScript.create(context);
        ScriptIntrinsicBlur theIntrinsic = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        Allocation tmpIn = Allocation.createFromBitmap(rs, givenBitmap);
        Allocation tmpOut = Allocation.createFromBitmap(rs, takenBitmap);
        theIntrinsic.setRadius(20f);
        theIntrinsic.setInput(tmpIn);
        theIntrinsic.forEach(tmpOut);
        tmpOut.copyTo(takenBitmap);
        return takenBitmap;
    }

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    public static void showDisconnectNetworkMessage(Context context, String message) {
        new MaterialDialog.Builder(context)
                .title("Error")
                .titleColor(ViewExtras.getColor(context, R.color.colorMain))
                .content(message
                )
                .positiveText(R.string.ok)
                .positiveColor(ViewExtras.getColor(context, R.color.main_color))
                .show();
    }

    public static void showSharedMessage(Context context, String message) {
        new MaterialDialog.Builder(context)
                .title("Info")
                .titleColor(ViewExtras.getColor(context, R.color.colorMain))
                .content(message
                )
                .positiveText(R.string.ok)
                .positiveColor(ViewExtras.getColor(context, R.color.main_color))
                .show();
    }
}
