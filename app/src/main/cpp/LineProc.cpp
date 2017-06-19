//
// Created by Anirudh N J on 02/03/17.
//
#include <jni.h>
#include "ImageProc.h"


extern "C" {


vector<Vec4i> lines;

void draw_circle_result(Mat &src, Point &center, int average_radius, int count_num);
bool compare_vec3f(Vec3f a, Vec3f b);

JNIEXPORT void JNICALL Java_com_cameracountmodule_Algorithm_ImageAnalysis_FindLines(JNIEnv *,
                                                                                 jobject,
                                                                                 jlong addrRgba,
                                                                                 jlong addrGray) {

    Mat &src = *(Mat *) addrRgba;
    Mat &gray = *(Mat *) addrGray;
    //src = imread(in_file_path.c_str());


    double avg_image_dimension = (src.rows + src.cols) / 2.0;
    //Pre processing of the image
    cvtColor(src, gray, CV_BGR2GRAY);
    Canny(gray, gray, k_min_canny_threshold, k_max_canny_threshold);

    //Object detection
    //vector<Vec4i> lines;
    HoughLinesP(gray, lines, k_hough_line_rho, k_hough_line_theta,
                (int) k_hough_line_threshold, avg_image_dimension * k_hough_line_length_factor,
                k_hough_line_max_inter_distance);

    //Averaging, sorting the detected objects and overlay the results
    for (size_t i = 0; i < lines.size(); i++) {
        /*line(src,
             Point(lines[i][0], lines[i][1]),
             Point(lines[i][2], lines[i][3]),
             k_outline_color, k_line_thickness, k_line_type);*/
    }

    gray = src;

}

JNIEXPORT int JNICALL Java_com_cameracountmodule_Algorithm_ImageAnalysis_GetLinesCount(JNIEnv *,
                                                                                    jobject) {

    return lines.size();
}

JNIEXPORT void JNICALL Java_com_cameracountmodule_Algorithm_ImageAnalysis_GetLinesCoordinates(JNIEnv *,
                                                                                           jobject,
                                                                                           jint index,
                                                                                           jlong lineAddr) {
    cv::Mat &Line = *(Mat *) lineAddr;


    Point startPoint;
    Point endPoint;

    startPoint.x = lines.at(index)[0];
    startPoint.y = lines.at(index)[1];
    endPoint.x = lines.at(index)[2];
    endPoint.y = lines.at(index)[3];

    Line.push_back(startPoint);
    Line.push_back(endPoint);


}

JNIEXPORT void JNICALL Java_com_cameracountmodule_Algorithm_ImageAnalysis_FindLinesSelective(JNIEnv *,
                                                                                 jobject,
                                                                                 jlong addrRgba,
                                                                                 jlong addrGray,
                                                                                 jint line_width) {

    Mat &src = *(Mat *) addrRgba;
    Mat &gray = *(Mat *) addrGray;

    //double avg_image_dimension = ((src.rows + src.cols) / 2.0) ;
    double avg_image_dimension = line_width ;
    //Pre processing of the image
    cvtColor(src, gray, CV_BGR2GRAY);
    Canny(gray, gray, k_min_canny_threshold, k_min_canny_threshold * 3, 3);

    //Object detection
    HoughLinesP(gray, lines, k_hough_line_rho, k_hough_line_theta,
                k_hough_line_threshold,
                avg_image_dimension * k_hough_line_length_factor,
                k_hough_line_max_inter_distance);

    //Averaging, sorting the detected objects and overlay the results
    for (size_t i = 0; i < lines.size(); i++) {
        line(src,
             Point(lines[i][0], lines[i][1]),
             Point(lines[i][2], lines[i][3]),
             k_outline_color, k_line_thickness, k_line_type);
    }
    gray = src;

}

}


