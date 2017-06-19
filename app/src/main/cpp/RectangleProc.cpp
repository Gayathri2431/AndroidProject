//
// Created by Anirudh N J on 02/03/17.
//

#include <jni.h>
#include "ImageProc.h"


extern "C" {

vector<RectangleContour> filteredRectangles ;

double angle_between_points(Point pt1, Point pt2, Point pt0);
vector<RectangleContour> filter_concentric_rectangles(vector<RectangleContour> rectangles);
void draw_rectangle_result(Mat &src, vector<RectangleContour> rectangles);

JNIEXPORT void JNICALL Java_com_cameracountmodule_Algorithm_ImageAnalysis_FindRectangles(JNIEnv *,
                                                                                      jobject,
                                                                                      jlong addrRgba,
                                                                                      jlong addrGray,
                                                                                      jlong addrList) {
    filteredRectangles.clear();
    Mat &src = *(Mat *) addrRgba;
    Mat &gray = *(Mat *) addrGray;

    int image_area = src.rows * src.cols;
    int min_rect_area = (int) (image_area / k_max_contour_area_divisor);

    //Pre processing of the image
    cvtColor(src, gray, CV_BGR2GRAY);
    GaussianBlur(gray, gray,
                 Size(k_gaussian_kernel_size, k_gaussian_kernel_size),
                 k_gaussian_kernel_deviation, k_gaussian_kernel_deviation);

    vector<RectangleContour> rectangles ;
    //Object detection


    for (int threshold_itr = 0;
         threshold_itr < k_threshold_max;
         threshold_itr += k_threshold_step) {
        Mat gray_thres;
        threshold(gray, gray_thres, threshold_itr, k_threshold_max, THRESH_BINARY);
        vector<vector<Point> > contours;

        findContours(gray_thres, contours, CV_RETR_LIST, CV_CHAIN_APPROX_SIMPLE);

        for (int contour_index = 0;
             contour_index != contours.size();
             contour_index++) {
            double contour_perimeter = arcLength(contours[contour_index], true);

            //DP approximation will asume 1-5% of the contour perimeter
            //3% works fine for the test sample
            vector<Point> approx_shape;
            approxPolyDP(contours[contour_index],
                         approx_shape,
                         k_shape_appoximator_ratio * contour_perimeter,
                         true);

            int shape_size = (int) approx_shape.size();

            if (shape_size != 4)
                continue;

            double contour_area = contourArea(approx_shape);
            if (contour_area < k_min_contour_area || contour_area > min_rect_area)
                continue;

            vector<double> cosine_angles;
            for (int j = 2; j < shape_size + 1; j++) {
                double cos_angle = angle_between_points(
                        approx_shape[j % shape_size],
                        approx_shape[j - 2],
                        approx_shape[j - 1]);
                cosine_angles.push_back(cos_angle);
            }
            // Sort ascending the cosine values
            sort(cosine_angles.begin(), cosine_angles.end());

            // Get the lowest and the highest cosine
            double max_cosine_angle = cosine_angles.back();
            if (max_cosine_angle < k_cosine_angle) {
                RectangleContour rect_contour;
                rect_contour.perimeter = contour_perimeter;
                rect_contour.contour = approx_shape;

                Moments rect_moments = moments(rect_contour.contour);
                rect_contour.centroid.x = (int) (rect_moments.m10 / rect_moments.m00);
                rect_contour.centroid.y = (int) (rect_moments.m01 / rect_moments.m00);

                rectangles.push_back(rect_contour);
            }
        }
    }
    filteredRectangles = filter_concentric_rectangles(rectangles);

    //Averaging, sorting the detected objects and overlay the results
    /*vector<RectangleContour> rectangles_2 = filter_concentric_rectangles(rectangles);
    filteredRectangles = rectangles_2;
    //draw_rectangle_result(src, rectangles_2);
    for (int i = 0; i < rectangles_2.size(); i++) {
        filteredRectangles.push_back(rectangles_2.at(i));
    }*/

    gray = src;
}

double angle_between_points(Point pt1, Point pt2, Point pt0) {
    double dx1 = pt1.x - pt0.x;
    double dy1 = pt1.y - pt0.y;
    double dx2 = pt2.x - pt0.x;
    double dy2 = pt2.y - pt0.y;
    return (dx1 * dx2 + dy1 * dy2) /
           sqrt((dx1 * dx1 + dy1 * dy1) * (dx2 * dx2 + dy2 * dy2) + 1e-10);
}

vector<RectangleContour> filter_concentric_rectangles(vector<RectangleContour> rectangles) {
    vector<RectangleContour> rectangles_2;

    if (rectangles.size() == 0)
        return rectangles_2;

    auto in_itr = rectangles.begin();
    rectangles_2.push_back(*in_itr);
    ++in_itr;

    while (in_itr != rectangles.end()) {
        bool similar_rectangle_added = false;
        for (auto out_itr = rectangles_2.begin();
             out_itr != rectangles_2.end();
             ++out_itr) {
            if ((abs(in_itr->centroid.x - out_itr->centroid.x) < k_centroid_min_distance) &&
                (abs(in_itr->centroid.y - out_itr->centroid.y) < k_centroid_min_distance)) {
                similar_rectangle_added = true;
                break;
            }
        }
        if (similar_rectangle_added == false) {
            rectangles_2.push_back(*in_itr);
        }
        ++in_itr;
    }
    return rectangles_2;
}

bool sort_rect_perimeter_high_to_low(RectangleContour a, RectangleContour b) {
    return a.perimeter > b.perimeter;
}

void draw_rectangle_result(Mat &src, vector<RectangleContour> rectangles) {
    vector<vector<Point> > tmp_rects;
    for (int i = 0; i < rectangles.size(); i++) {
        tmp_rects.push_back(rectangles[i].contour);
        drawMarker(src, rectangles[i].centroid, k_crosshair_color,
                   k_crosshair_type, k_crosshair_length,
                   1, k_line_type);

        circle(src, rectangles[i].centroid, k_crosshair_circle_radius,
               k_crosshair_color, k_crosshair_thickness, k_line_type);
    }

    drawContours(src, tmp_rects,-1 ,k_outline_color,1,k_line_type );
}

JNIEXPORT int JNICALL
Java_com_cameracountmodule_Algorithm_ImageAnalysis_GetRectangleListSizeRet(JNIEnv *,
                                                                     jobject) {

    int size ;
    size = filteredRectangles.size();

    return size;

}


JNIEXPORT void JNICALL
Java_com_cameracountmodule_Algorithm_ImageAnalysis_GetRectContourFromIndexNew(JNIEnv *,
                                                                        jobject, jint index,
                                                                        jlong contourAddr ,jlong centeroidAddr) {
    cv::Mat &cont = *(cv::Mat *) contourAddr;
    cv::Mat &center = *(cv::Mat *) centeroidAddr;

    //__android_log_print(ANDROID_LOG_INFO, "foo---Size-->>>>> ","%s",SSTR(filteredRectangles.size()).c_str());

    for (int i =0 ; i <filteredRectangles.at(index).contour.size() ; i++ )
    {
        Point pnts = filteredRectangles.at(index).contour[i];
        cont.push_back(pnts);

        center.push_back(filteredRectangles.at(index).centroid);

    }

}

JNIEXPORT void JNICALL Java_com_cameracountmodule_Algorithm_ImageAnalysis_FindRectanglesSelective(JNIEnv *,
                                                                                      jobject,
                                                                                      jlong addrRgba,
                                                                                      jlong addrGray,
                                                                                      jint rectArea) {
    Mat &src = *(Mat *) addrRgba;
    Mat &gray = *(Mat *) addrGray;

    __android_log_print(ANDROID_LOG_INFO, "foo---contour-centeroid->>>>> ","%s",SSTR(rectArea).c_str());
    //int image_area = src.rows * src.cols;
    int min_rect_area =  src.rows * src.cols * (int) (rectArea);

    //Pre processing of the image
    cvtColor(src, gray, CV_BGR2GRAY);
    GaussianBlur(gray, gray,
                 Size(k_gaussian_kernel_size,k_gaussian_kernel_size),
                 k_gaussian_kernel_deviation, k_gaussian_kernel_deviation);

    vector<RectangleContour> rectangles;
    //Object detection

    for( int threshold_itr = 0;
         threshold_itr < k_threshold_max ;
         threshold_itr += k_threshold_step)
    {
        Mat gray_thres;
        threshold(gray, gray_thres, threshold_itr, k_threshold_max, THRESH_BINARY);
        vector<vector<Point> > contours;

        findContours(gray_thres, contours, CV_RETR_LIST, CV_CHAIN_APPROX_SIMPLE);

        for(int contour_index = 0;
            contour_index != contours.size() ;
            contour_index++)
        {
            double contour_perimeter = arcLength(contours[contour_index], true);

            //DP approximation will asume 1-5% of the contour perimeter
            //3% works fine for the test sample
            vector<Point> approx_shape;
            approxPolyDP(contours[contour_index],
                         approx_shape,
                         k_shape_appoximator_ratio * contour_perimeter,
                         true);

            int shape_size = approx_shape.size();

            if (shape_size != 4)
                continue;

            double contour_area = contourArea(approx_shape);
            if ( contour_area < k_min_contour_area || contour_area > min_rect_area )
                continue;

            vector<double> cosine_angles;
            for (int j = 2; j < shape_size + 1; j++)
            {
                double cos_angle = angle_between_points (
                        approx_shape [j%shape_size],
                        approx_shape[j-2],
                        approx_shape[j-1]);
                cosine_angles.push_back(cos_angle);
            }
            // Sort ascending the cosine values
            sort(cosine_angles.begin(), cosine_angles.end());

            // Get the lowest and the highest cosine
            double max_cosine_angle = cosine_angles.back();
            if(max_cosine_angle < k_cosine_angle)
            {
                RectangleContour rect_contour;
                rect_contour.perimeter = contour_perimeter;
                rect_contour.contour = approx_shape;

                Moments rect_moments = moments(rect_contour.contour);
                rect_contour.centroid.x = (int) ( rect_moments.m10 / rect_moments.m00);
                rect_contour.centroid.y = (int) ( rect_moments.m01 / rect_moments.m00);

                rectangles.push_back(rect_contour);
            }
        }
    }

    //Averaging, sorting the detected objects and overlay the results
    vector<RectangleContour> rectangles_2 = filter_concentric_rectangles(rectangles);
    filteredRectangles = rectangles_2;
    draw_rectangle_result(src, rectangles_2);
    for (int i = 0; i < rectangles_2.size(); i++) {
        filteredRectangles.push_back(rectangles_2.at(i));
    }

    gray = src;

}


}
