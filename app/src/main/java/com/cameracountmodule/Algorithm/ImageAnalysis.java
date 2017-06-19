package com.cameracountmodule.Algorithm;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Path;
import android.hardware.Camera;
import android.widget.ListView;

import com.cameracountmodule.Utils.Global;
import com.cameracountmodule.activity.CameraActivity;
import com.cameracountmodule.model.Circles;
import com.cameracountmodule.model.DataClass;
import com.cameracountmodule.model.ImageResultType;
import com.cameracountmodule.model.Lines;
import com.cameracountmodule.model.Rectangles;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Point3;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by gayathris on 08/02/17.
 */

class RectangleContour {
    MatOfPoint contour;
    double perimeter;
    Point centeroid = new Point();
}


public class ImageAnalysis {
    private Context ctx;
    private CascadeClassifier pipe_classifier;
    private Mat circles;
    private MatOfRect pipe_rects = new MatOfRect();

    private static DataClass dataClass = DataClass.getInstance();
    private static List<RectangleContour> rectCount;
    private static List<MatOfPoint> lineList;
    private static MatOfPoint3f circleDetc;
    private static MatOfPoint3f smallCircleDetc;

    static {
        System.loadLibrary("native-lib");
    }

    public native int stringFromJNI(long addrRgba, long addrGray);

    public native void FindCircles(long addrRgba, long addrGray, long addrCircleMat);

    public native void FindLines(long addrRgba, long addrGray);

    public native void FindRectangles(long addrRgba, long addrGray, long addrList);

    public native void FindRectanglesSelective(long addrRgba, long addrGray, long addrList, int rectArea);

    public native void FindSmallCircles(long addrRgba, long addrGray, long addrCircleMat);

    public native int ImageHoughSmallCirclesTracker(long addrRgba, long addrGray, long addrCircleMat);

    public native int ImageHoughCirclesTracker(long addrRgba, long addrGray, long addrCircleMat);

    public native void InitTracker();

    public native void Destroytracker();

    public native void GetRectContourFromIndexNew(int index, long contourAddr, long centeroid);

    public native void FindLinesSelective(long addrRgba, long addrGray, int rectArea);

    public native int GetRectangleListSizeRet();

    public native int GetLinesCount();

    public native int GetLinesCoordinates(int index, long lineAddr);

    public native int FindCirclesSelective(long addrRgba, long addrGray, long addrCircleMat, float circle_radius);

    public native int FindSmallCirclesSelective(long addrRgba, long addrGray, long addrCircleMat, float circle_radius);


    public ImageAnalysis(Context context) {
        ctx = context;
        loadFile();
        Destroytracker();
        InitTracker();
    }

    public ImageAnalysis() {
    }


    public Bitmap process_image_tracking_circle(Bitmap bitmap, int width, int height, boolean rotation) {
        Mat mat = new Mat(bitmap.getWidth(), bitmap.getHeight(),
                CvType.CV_8UC1);
        org.opencv.android.Utils.bitmapToMat(bitmap, mat);
        Mat outMat = new Mat(height, width, CvType.CV_8SC1);
        MatOfPoint3f circleMat = new MatOfPoint3f();
        int count = 0;

        count = ImageHoughCirclesTracker(mat.getNativeObjAddr(), outMat.getNativeObjAddr(), circleMat.getNativeObjAddr());

        org.opencv.android.Utils.matToBitmap(outMat, bitmap);

        if (circleDetc != null)
            circleDetc.empty();
        circleDetc = new MatOfPoint3f();
        circleDetc = circleMat;

        if (rotation) {
            if (Global.settingsManager.getTypeCamera() == Camera.CameraInfo.CAMERA_FACING_BACK) {
                bitmap = rotate(bitmap, 90);
            } else if (Global.mOrientation == Global.ORIENTATION_PORTRAIT_NORMAL) {
                bitmap = rotate(bitmap, -90);
            } else if (Global.mOrientation == Global.ORIENTATION_LANDSCAPE_NORMAL) {
                bitmap = rotate(bitmap, 90);
            } else if (Global.mOrientation == Global.ORIENTATION_LANDSCAPE_INVERTED) {
                bitmap = rotate(bitmap, -270);
            }
        }

        Global.circles = circleMat.toArray().length;
        return bitmap;
    }

    public Bitmap process_image_tracking_small_circle(Bitmap bitmap, int width, int height, boolean rotation) {
        Mat mat = new Mat(bitmap.getWidth(), bitmap.getHeight(),
                CvType.CV_8UC1);
        org.opencv.android.Utils.bitmapToMat(bitmap, mat);
        Mat outMat = new Mat(height, width, CvType.CV_8SC1);
        MatOfPoint3f circleMat = new MatOfPoint3f();
        int count = 0;

        count = ImageHoughSmallCirclesTracker(mat.getNativeObjAddr(), outMat.getNativeObjAddr(), circleMat.getNativeObjAddr());

        org.opencv.android.Utils.matToBitmap(outMat, bitmap);
        if (smallCircleDetc != null)
            smallCircleDetc.empty();
        smallCircleDetc = new MatOfPoint3f();
        smallCircleDetc = circleMat;

        if (rotation) {
            if (Global.settingsManager.getTypeCamera() == Camera.CameraInfo.CAMERA_FACING_BACK) {
                bitmap = rotate(bitmap, 90);
            } else if (Global.mOrientation == Global.ORIENTATION_PORTRAIT_NORMAL) {
                bitmap = rotate(bitmap, -90);
            } else if (Global.mOrientation == Global.ORIENTATION_LANDSCAPE_NORMAL) {
                bitmap = rotate(bitmap, 90);
            } else if (Global.mOrientation == Global.ORIENTATION_LANDSCAPE_INVERTED) {
                bitmap = rotate(bitmap, -270);
            }
        }

        Global.circles = circleMat.toArray().length;
        return bitmap;
    }

    public Bitmap circleDetection(Bitmap bitmap, int width, int height, boolean rotation) {
        Mat mat = new Mat(bitmap.getWidth(), bitmap.getHeight(),
                CvType.CV_8UC1);
        org.opencv.android.Utils.bitmapToMat(bitmap, mat);
        Mat outMat = new Mat(height, width, CvType.CV_8SC1);
        MatOfPoint3f circleMat = new MatOfPoint3f();

        FindCircles(mat.getNativeObjAddr(), outMat.getNativeObjAddr(), circleMat.getNativeObjAddr());

        org.opencv.android.Utils.matToBitmap(outMat, bitmap);
        if (circleDetc != null)
            circleDetc.empty();
        circleDetc = new MatOfPoint3f();
        circleDetc = circleMat;


        int sum_radii = 0;
        for (int j = 0; j < circleMat.toArray().length; j++) {
            Point3 circleCoordinates = circleMat.toArray()[j];
            sum_radii += (int) circleCoordinates.z;
        }

        int average_radius = 0;
        if (circleMat.rows() == 0)
            average_radius = 0;
        else
            average_radius = sum_radii / circleMat.rows();

        for (int i = 0; i < circleMat.toArray().length; i++) {
            Point3 circleCoordinates = circleMat.toArray()[i];
            int x = (int) circleCoordinates.x, y = (int) circleCoordinates.y;
            Point center = new Point(x, y);
            draw_circle_result(mat, center, average_radius, i);
        }


        if (rotation) {
            if (Global.settingsManager.getTypeCamera() == Camera.CameraInfo.CAMERA_FACING_BACK) {
                bitmap = rotate(bitmap, 90);

                if (Global.getAutoRotationStatus(ctx) == true) {
                    if (Global.mOrientation == Global.ORIENTATION_LANDSCAPE_NORMAL)
                        bitmap = rotate(bitmap, -90);

                    else if (Global.mOrientation == Global.ORIENTATION_LANDSCAPE_INVERTED)
                        bitmap = rotate(bitmap, 90);
                }


            } else if (Global.mOrientation == Global.ORIENTATION_PORTRAIT_NORMAL) {
                bitmap = rotate(bitmap, -90);
            } else if (Global.mOrientation == Global.ORIENTATION_LANDSCAPE_NORMAL) {
                bitmap = rotate(bitmap, 90);
            } else if (Global.mOrientation == Global.ORIENTATION_LANDSCAPE_INVERTED) {
                bitmap = rotate(bitmap, -270);
            }
        }
        Global.circles = circleMat.toArray().length;
        return bitmap;
    }

    public Bitmap process_image_hough_circles_selective(Bitmap bitmap, float circle_range) {

        if (circles != null)
            circles.empty();

        Mat mat = new Mat(bitmap.getWidth(), bitmap.getHeight(),
                CvType.CV_8UC1);
        org.opencv.android.Utils.bitmapToMat(bitmap, mat);
        Mat outMat = new Mat(bitmap.getWidth(), bitmap.getHeight(), CvType.CV_8SC1);
        MatOfPoint3f circleMat = new MatOfPoint3f();

        FindCirclesSelective(mat.getNativeObjAddr(), outMat.getNativeObjAddr(), circleMat.getNativeObjAddr(), circle_range);
        org.opencv.android.Utils.matToBitmap(outMat, bitmap);
        //Global.circles = numberOfCircles;
        if (smallCircleDetc != null)
            smallCircleDetc.empty();
        smallCircleDetc = new MatOfPoint3f();
        smallCircleDetc = circleMat;

        int sum_radii = 0;
        for (int j = 0; j < circleMat.toArray().length; j++) {
            Point3 circleCoordinates = circleMat.toArray()[j];
            sum_radii += (int) circleCoordinates.z;
        }

        int average_radius = 0;
        if (circleMat.rows() == 0)
            average_radius = 0;
        else
            average_radius = sum_radii / circleMat.rows();


        for (int i = 0; i < circleMat.toArray().length; i++) {
            Point3 circleCoordinates = circleMat.toArray()[i];
            int x = (int) circleCoordinates.x, y = (int) circleCoordinates.y;
            Point center = new Point(x, y);
            draw_circle_result(mat, center, average_radius, i);
        }
        Global.circles = circleMat.toArray().length;
        return bitmap;


    }

    public Bitmap process_image_hough_small_circles_selective(Bitmap bitmap, float circle_range, boolean rotation) {

        Mat mat = new Mat(bitmap.getWidth(), bitmap.getHeight(),
                CvType.CV_8UC1);
        org.opencv.android.Utils.bitmapToMat(bitmap, mat);
        Mat outMat = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8SC1);
        MatOfPoint3f circleMat = new MatOfPoint3f();

        FindSmallCirclesSelective(mat.getNativeObjAddr(), outMat.getNativeObjAddr(), circleMat.getNativeObjAddr(), circle_range);

        org.opencv.android.Utils.matToBitmap(outMat, bitmap);
        if (smallCircleDetc != null)
            smallCircleDetc.empty();
        smallCircleDetc = new MatOfPoint3f();
        smallCircleDetc = circleMat;

        int sum_radii = 0;
        for (int j = 0; j < circleMat.toArray().length; j++) {
            Point3 circleCoordinates = circleMat.toArray()[j];
            sum_radii += (int) circleCoordinates.z;
        }

        int average_radius = 0;
        if (circleMat.rows() == 0)
            average_radius = 0;
        else
            average_radius = sum_radii / circleMat.rows();


        for (int i = 0; i < circleMat.toArray().length; i++) {
            Point3 circleCoordinates = circleMat.toArray()[i];
            int x = (int) circleCoordinates.x, y = (int) circleCoordinates.y;
            Point center = new Point(x, y);
            draw_circle_result(mat, center, average_radius, i);
        }

        if (rotation) {
            if (Global.settingsManager.getTypeCamera() == Camera.CameraInfo.CAMERA_FACING_BACK) {
                bitmap = rotate(bitmap, 90);
                if (Global.getAutoRotationStatus(ctx) == true) {
                    if (Global.mOrientation == Global.ORIENTATION_LANDSCAPE_NORMAL)
                        bitmap = rotate(bitmap, -90);

                    else if (Global.mOrientation == Global.ORIENTATION_LANDSCAPE_INVERTED)
                        bitmap = rotate(bitmap, 90);
                }
            } else if (Global.mOrientation == Global.ORIENTATION_PORTRAIT_NORMAL) {
                bitmap = rotate(bitmap, -90);
            } else if (Global.mOrientation == Global.ORIENTATION_LANDSCAPE_NORMAL) {
                bitmap = rotate(bitmap, 90);
            } else if (Global.mOrientation == Global.ORIENTATION_LANDSCAPE_INVERTED) {
                bitmap = rotate(bitmap, -270);
            }
        }

        Global.circles = circleMat.toArray().length;
        return bitmap;
    }


    public Bitmap process_image_cascade_classifier(Bitmap bitmap) {
        Mat mat = new Mat(bitmap.getWidth(), bitmap.getHeight(),
                CvType.CV_8UC1);

        Mat gray = new Mat(bitmap.getWidth(), bitmap.getHeight(),
                CvType.CV_8UC1);
        //Load the classifier model file
        if (pipe_classifier.empty()) {
            System.out.print("not loaded");
        }

        //Pre processing of the image
        int colorChannels = (mat.channels() == 3) ? Imgproc.COLOR_BGR2GRAY
                : ((mat.channels() == 4) ? Imgproc.COLOR_BGRA2GRAY : 1);
        org.opencv.android.Utils.bitmapToMat(bitmap, mat);
        Imgproc.cvtColor(mat, gray, colorChannels);
        List<Mat> planes = new ArrayList<Mat>();
        Core.split(gray, planes);
        Imgproc.equalizeHist(planes.get(0), planes.get(0));
        Core.merge(planes, gray);

        //Object detection
        pipe_classifier.detectMultiScale(gray, pipe_rects,
                Global.k_classifier_scale_factor,
                Global.k_minimum_neighbours,
                Global.k_haar_detect_flags,
                new Size(Global.k_min_obj_size, Global.k_min_obj_size),
                new Size(Global.k_max_objsize, Global.k_max_objsize));

        //Averaging and sorting the detected objects
        int num_circles = (pipe_rects.rows() == 0) ? 0 : pipe_rects.cols();
        int sum_radii = 0;

        for (int i = 0; i < num_circles; i++) {
            double[] circleCoordinates = pipe_rects.get(0, i);
            sum_radii += circleCoordinates[2] + circleCoordinates[3] * 0.25;
        }
        int average_radius = 0;
        if (num_circles == 0)
            average_radius = 0;
        else
            average_radius = sum_radii / num_circles;

        //sort(pipe_rects.begin(), pipe_rects.end(), compare_rect);

        //Overlay results on the source image and write to file
        for (int i = 0; i < num_circles; i++) {
            double[] circleCoordinates = pipe_rects.get(0, i);
            Point center = new Point(circleCoordinates[0] + circleCoordinates[2] * 0.5, circleCoordinates[1] + circleCoordinates[3] * 0.5);
            draw_circle_result(mat, center, average_radius, i);
        }

        org.opencv.android.Utils.matToBitmap(mat, bitmap);
        return bitmap;
    }

    public Bitmap process_image_hough_lines_selective(Bitmap bitmap, boolean rotation, int slider) {
        Mat mat = new Mat(bitmap.getWidth(), bitmap.getHeight(),
                CvType.CV_8UC1);
        org.opencv.android.Utils.bitmapToMat(bitmap, mat);
        Mat outMat = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8SC1);

        FindLinesSelective(mat.getNativeObjAddr(), outMat.getNativeObjAddr(), slider);

        org.opencv.android.Utils.matToBitmap(outMat, bitmap);

        getLineList();
        if (getLineList() != null) {
            Global.lines = getLineList().size();
        }

        if (rotation) {
            if (Global.settingsManager.getTypeCamera() == Camera.CameraInfo.CAMERA_FACING_BACK) {
                bitmap = rotate(bitmap, 90);
                if (Global.getAutoRotationStatus(ctx) == true) {
                    if (Global.mOrientation == Global.ORIENTATION_LANDSCAPE_NORMAL)
                        bitmap = rotate(bitmap, -90);

                    else if (Global.mOrientation == Global.ORIENTATION_LANDSCAPE_INVERTED)
                        bitmap = rotate(bitmap, 90);
                }
            } else if (Global.mOrientation == Global.ORIENTATION_PORTRAIT_NORMAL) {
                bitmap = rotate(bitmap, -90);
            } else if (Global.mOrientation == Global.ORIENTATION_LANDSCAPE_NORMAL) {
                bitmap = rotate(bitmap, 90);
            } else if (Global.mOrientation == Global.ORIENTATION_LANDSCAPE_INVERTED) {
                bitmap = rotate(bitmap, -270);
            }
        }

        return bitmap;
    }


    public Bitmap process_image_hough_lines(Bitmap bitmap, boolean rotation) {

        Mat mat = new Mat(bitmap.getWidth(), bitmap.getHeight(),
                CvType.CV_8UC1);
        org.opencv.android.Utils.bitmapToMat(bitmap, mat);
        Mat outMat = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8SC1);

        FindLines(mat.getNativeObjAddr(), outMat.getNativeObjAddr());

        org.opencv.android.Utils.matToBitmap(outMat, bitmap);

        getLineList();
        if (getLineList() != null) {
            Global.lines = getLineList().size();
        }

        if (rotation) {
            if (Global.settingsManager.getTypeCamera() == Camera.CameraInfo.CAMERA_FACING_BACK) {
                bitmap = rotate(bitmap, 90);
                if (Global.getAutoRotationStatus(ctx) == true) {
                    if (Global.mOrientation == Global.ORIENTATION_LANDSCAPE_NORMAL)
                        bitmap = rotate(bitmap, -90);

                    else if (Global.mOrientation == Global.ORIENTATION_LANDSCAPE_INVERTED)
                        bitmap = rotate(bitmap, 90);
                }
            } else if (Global.mOrientation == Global.ORIENTATION_PORTRAIT_NORMAL) {
                bitmap = rotate(bitmap, -90);
            } else if (Global.mOrientation == Global.ORIENTATION_LANDSCAPE_NORMAL) {
                bitmap = rotate(bitmap, 90);
            } else if (Global.mOrientation == Global.ORIENTATION_LANDSCAPE_INVERTED) {
                bitmap = rotate(bitmap, -270);
            }
        }


        return bitmap;

    }

    private void draw_circle_result(Mat src, Point center, int average_radius, int count_num) {
        Imgproc.circle(src, center, average_radius,
                Global.k_circle_color, Global.k_circle_thickness, Global.k_line_type, 0);
        Imgproc.drawMarker(src, center, Global.k_crosshair_color,
                Global.k_crosshair_type, Global.k_crosshair_length,
                Global.k_crosshair_thickness, Global.k_line_type);
        Imgproc.circle(src, center, Global.k_crosshair_circle_radius,
                Global.k_crosshair_color, Global.k_crosshair_thickness, Global.k_line_type, 0);
        double font_scale = 1.0;
        if (average_radius < 30)
            font_scale = 0.5;
        //Convert int count_num to string
        /*stringstream ss;
        ss << (int)(count_num + 1);
        string str = ss.str();
        putText(src, str.c_str(),
                Point(center.x + k_text_offset_x, center.y + k_text_offset_y),
                k_text_font, font_scale, k_text_color, k_text_thickness, k_line_type);*/
    }

    public static Bitmap rotate(Bitmap bitmap, int degree) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        Matrix mtx = new Matrix();
        mtx.setRotate(degree);
        return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
    }

    public Bitmap process_image_contour_rectangle(Bitmap bitmap, boolean rotation) {
        Mat mat = new Mat(bitmap.getWidth(), bitmap.getHeight(),
                CvType.CV_8UC1);
        org.opencv.android.Utils.bitmapToMat(bitmap, mat);
        MatOfPoint contourlist = new MatOfPoint();


        Mat outMat = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC1);
        FindRectangles(mat.getNativeObjAddr(), outMat.getNativeObjAddr(), contourlist.getNativeObjAddr());

        org.opencv.android.Utils.matToBitmap(outMat, bitmap);

        getRectangleList();

        System.out.println(rectCount.size());

        if (rotation) {
            if (Global.settingsManager.getTypeCamera() == Camera.CameraInfo.CAMERA_FACING_BACK) {
                bitmap = rotate(bitmap, 90);
                if (Global.getAutoRotationStatus(ctx) == true) {
                    if (Global.mOrientation == Global.ORIENTATION_LANDSCAPE_NORMAL)
                        bitmap = rotate(bitmap, -90);

                    else if (Global.mOrientation == Global.ORIENTATION_LANDSCAPE_INVERTED)
                        bitmap = rotate(bitmap, 90);
                }
            } else if (Global.mOrientation == Global.ORIENTATION_PORTRAIT_NORMAL) {
                bitmap = rotate(bitmap, -90);
            } else if (Global.mOrientation == Global.ORIENTATION_LANDSCAPE_NORMAL) {
                bitmap = rotate(bitmap, 90);
            } else if (Global.mOrientation == Global.ORIENTATION_LANDSCAPE_INVERTED) {
                bitmap = rotate(bitmap, -270);
            }
        }
        return bitmap;
    }


    public Bitmap process_image_contour_rectangle_selective(Bitmap bitmap, boolean rotation, int area) {

        Mat mat = new Mat(bitmap.getWidth(), bitmap.getHeight(),
                CvType.CV_8UC1);
        org.opencv.android.Utils.bitmapToMat(bitmap, mat);
        MatOfPoint contourlist = new MatOfPoint();


        Mat outMat = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC1);
        FindRectanglesSelective(mat.getNativeObjAddr(), outMat.getNativeObjAddr(), contourlist.getNativeObjAddr(), area);

        org.opencv.android.Utils.matToBitmap(outMat, bitmap);

        getRectangleList();

        if (rotation) {
            if (Global.settingsManager.getTypeCamera() == Camera.CameraInfo.CAMERA_FACING_BACK) {
                bitmap = rotate(bitmap, 90);
            } else if (Global.mOrientation == Global.ORIENTATION_PORTRAIT_NORMAL) {
                bitmap = rotate(bitmap, -90);
            } else if (Global.mOrientation == Global.ORIENTATION_LANDSCAPE_NORMAL) {
                bitmap = rotate(bitmap, 90);
            } else if (Global.mOrientation == Global.ORIENTATION_LANDSCAPE_INVERTED) {
                bitmap = rotate(bitmap, -270);
            }
        }
        return bitmap;
    }

    public Bitmap process_image_multi_shape(Bitmap mBitmap,boolean rotation) {
        circleDetection(mBitmap, 640, 480, true);
        process_image_contour_rectangle(mBitmap, true);
        process_image_hough_lines(mBitmap, true);
        process_image_hough_small_circles_selective(mBitmap, 2.0f, true);
        setDetectedCirclesParameter();
        setResultRectanglesParameters();
        setResultLinesParameters();
        setDetectedSmallCirclesParameter();

        if (rotation) {
            if (Global.settingsManager.getTypeCamera() == Camera.CameraInfo.CAMERA_FACING_BACK) {
                mBitmap = rotate(mBitmap, 90);
                if (Global.getAutoRotationStatus(ctx) == true) {
                    if (Global.mOrientation == Global.ORIENTATION_LANDSCAPE_NORMAL)
                        mBitmap = rotate(mBitmap, -90);

                    else if (Global.mOrientation == Global.ORIENTATION_LANDSCAPE_INVERTED)
                        mBitmap = rotate(mBitmap, 90);
                }
            } else if (Global.mOrientation == Global.ORIENTATION_PORTRAIT_NORMAL) {
                mBitmap = rotate(mBitmap, -90);
            } else if (Global.mOrientation == Global.ORIENTATION_LANDSCAPE_NORMAL) {
                mBitmap = rotate(mBitmap, 90);
            } else if (Global.mOrientation == Global.ORIENTATION_LANDSCAPE_INVERTED) {
                mBitmap = rotate(mBitmap, -270);
            }
        }
        return mBitmap;
    }

    public void loadFile() {
        File mCascadeFile;
        try {
            // load cascade file from application resources
            InputStream is = ctx.getResources().getAssets().open("black_pipe_small.xml");
            File cascadeDir = ctx.getDir("cascade", Context.MODE_PRIVATE);
            mCascadeFile = new File(cascadeDir, "black_pipe_small.xml");
            FileOutputStream os = new FileOutputStream(mCascadeFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();

            pipe_classifier = new CascadeClassifier(mCascadeFile.getAbsolutePath());
            cascadeDir.delete();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<RectangleContour> getRectangleList() {
        rectCount = new ArrayList<>();
        for (int i = 0; i < GetRectangleListSizeRet(); i++) {
            MatOfPoint contour = new MatOfPoint();
            MatOfPoint centeroid = new MatOfPoint();

            GetRectContourFromIndexNew(i, contour.getNativeObjAddr(), centeroid.getNativeObjAddr());

            RectangleContour currentContour = new RectangleContour();
            currentContour.contour = contour;
            if (centeroid.toList().size() != 0) {
                currentContour.centeroid = centeroid.toList().get(0);
            }
            rectCount.add(currentContour);
        }
        Global.rectangles = rectCount.size();
        return rectCount;
    }

    public static int getTouchPointInsideRectangle(Point pt, int countRectangle) {
        int touchValue = 0;
        MatOfPoint2f NewMtx = new MatOfPoint2f(rectCount.get(countRectangle).contour.toArray());
        touchValue = (int) Imgproc.pointPolygonTest(NewMtx, pt, false);
        return touchValue;
    }

    public List<MatOfPoint> getLineList() {
        lineList = new ArrayList<>();
        for (int i = 0; i < GetLinesCount(); i++) {
            MatOfPoint line = new MatOfPoint();
            GetLinesCoordinates(i, line.getNativeObjAddr());
            lineList.add(line);
        }
        return lineList;
    }

    public static void setDetectedCirclesParameter() {
        if (circleDetc != null) {
            int numberOfCircles = circleDetc.rows();
            dataClass.circles.clear();
            System.out.println("Orientation: " + Global.mOrientation);
            for (int i = 0; i < numberOfCircles; i++) {
                double[] circleCoordinates = circleDetc.get(i, 0);
                int x = (int) circleCoordinates[0], y = (int) circleCoordinates[1];
                int radius = (int) circleCoordinates[2];
                org.jcodec.common.model.Point imagePoint = new org.jcodec.common.model.Point(x, y);
                org.jcodec.common.model.Point point = Global.convertIntoOriginalPipeCenterFormat(imagePoint);
                if (CameraActivity.resultImageType == ImageResultType.Took || CameraActivity.resultImageType == ImageResultType.Recorded) {
                    Circles circle = new Circles(point.getX(), point.getY(), radius, i + 1);
                    dataClass.circles.add(circle);
                } else {
                    Circles circle = new Circles(x, y, radius, i + 1);
                    dataClass.circles.add(circle);
                }
            }
        }
    }

    public static void setDetectedSmallCirclesParameter() {
        if (smallCircleDetc != null) {
            int numberOfCircles = smallCircleDetc.rows();
            dataClass.smallCircles.clear();
            System.out.println("Orientation: " + Global.mOrientation);
            for (int i = 0; i < numberOfCircles; i++) {
                double[] circleCoordinates = smallCircleDetc.get(i, 0);
                int x = (int) circleCoordinates[0], y = (int) circleCoordinates[1];
                int radius = (int) circleCoordinates[2];
                org.jcodec.common.model.Point imagePoint = new org.jcodec.common.model.Point(x, y);
                org.jcodec.common.model.Point point = Global.convertIntoOriginalPipeCenterFormat(imagePoint);
                if (CameraActivity.resultImageType == ImageResultType.Took || CameraActivity.resultImageType == ImageResultType.Recorded) {
                    Circles circle = new Circles(point.getX(), point.getY(), radius, i + 1);
                    dataClass.smallCircles.add(circle);
                } else {
                    Circles circle = new Circles(x, y, radius, i + 1);
                    dataClass.smallCircles.add(circle);
                }
            }
        }
    }

    public static void setResultLinesParameters() {
        dataClass.lines.clear();
        if (lineList.size() != 0) {
            int numberOfLines = lineList.size();

            for (int i = 0; i < numberOfLines; i++) {

                Point startPoint = lineList.get(i).toArray()[0];
                Point endPoint = lineList.get(i).toArray()[1];

                if (CameraActivity.resultImageType == ImageResultType.Took || CameraActivity.resultImageType == ImageResultType.Recorded) {
                    org.jcodec.common.model.Point convertedStartLinePoint = Global.convertCenterFormat(startPoint);
                    org.jcodec.common.model.Point convertedEndLinePoint = Global.convertCenterFormat(endPoint);

                    Lines line = new Lines(convertedStartLinePoint.getX(), convertedStartLinePoint.getY(), convertedEndLinePoint.getX(), convertedEndLinePoint.getY());
                    dataClass.lines.add(line);
                } else {
                    Lines line = new Lines((float) startPoint.x, (float) startPoint.y, (float) endPoint.x, (float) endPoint.y);
                    dataClass.lines.add(line);
                }
            }
        }
    }

    public static void setResultRectanglesParameters() {
        dataClass.rectangles.clear();
        int totalRectangle = rectCount.size() / 2;
        int height, width, avg, perimeter = 0;
        if (totalRectangle != 0) {
            for (int i = 0; i < totalRectangle; i++) {
                height = Imgproc.boundingRect(rectCount.get(i).contour).height;
                width = Imgproc.boundingRect(rectCount.get(i).contour).width;
                avg = (height + width) / 2;
                perimeter += avg;
            }
            perimeter = perimeter / totalRectangle;
        }
        for (int i = 0; i < rectCount.size(); i++) {
            RectangleContour contour = rectCount.get(i);
            List<Point> point = contour.contour.toList();
            if (CameraActivity.resultImageType == ImageResultType.Took || CameraActivity.resultImageType == ImageResultType.Recorded) {
                org.jcodec.common.model.Point xy1 = Global.convertCenterFormat(new Point(point.get(0).x, point.get(0).y));
                org.jcodec.common.model.Point xy2 = Global.convertCenterFormat(new Point(point.get(1).x, point.get(1).y));
                org.jcodec.common.model.Point xy3 = Global.convertCenterFormat(new Point(point.get(2).x, point.get(2).y));
                org.jcodec.common.model.Point xy4 = Global.convertCenterFormat(new Point(point.get(3).x, point.get(3).y));
                int xAxis = (int) rectCount.get(i).centeroid.x;
                int yAxis = (int) rectCount.get(i).centeroid.y;
                org.jcodec.common.model.Point imagePoint = new org.jcodec.common.model.Point(xAxis, yAxis);
                org.jcodec.common.model.Point convertedCenter = Global.convertIntoOriginalPipeCenterFormat(imagePoint);

                float x1 = (float) xy1.getX();
                float y1 = (float) xy1.getY();
                float x2 = (float) xy2.getX();
                float y2 = (float) xy2.getY();
                float x3 = (float) xy3.getX();
                float y3 = (float) xy3.getY();
                float x4 = (float) xy4.getX();
                float y4 = (float) xy4.getY();

                Rectangles rect = new Rectangles(x1, y1, x2, y2, x3, y3, x4, y4, convertedCenter.getX(), convertedCenter.getY(), perimeter, i + 1);
                dataClass.rectangles.add(rect);
            } else {
                int xAxis = (int) rectCount.get(i).centeroid.x;
                int yAxis = (int) rectCount.get(i).centeroid.y;
                float x1 = (float) point.get(0).x;
                float y1 = (float) point.get(0).y;
                float x2 = (float) point.get(1).x;
                float y2 = (float) point.get(1).y;
                float x3 = (float) point.get(2).x;
                float y3 = (float) point.get(2).y;
                float x4 = (float) point.get(3).x;
                float y4 = (float) point.get(3).y;

                Rectangles rect = new Rectangles(x1, y1, x2, y2, x3, y3, x4, y4, xAxis, yAxis, perimeter, i + 1);
                dataClass.rectangles.add(rect);
            }
        }
    }
}