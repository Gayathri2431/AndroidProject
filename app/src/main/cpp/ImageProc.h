//
// Created by Anirudh N J on 02/03/17.
//

#ifndef VISUALOGYX_IMAGEPROC_H_H
#define VISUALOGYX_IMAGEPROC_H_H

#include <iostream>
#include <vector>
#include <string>
#include <stdio.h>

#include <sstream>

#define SSTR( x ) static_cast< std::ostringstream & >( \
        ( std::ostringstream() << std::dec << x ) ).str()

#include <android/log.h>

#include "opencv2/core/core.hpp"
#include "opencv2/highgui/highgui.hpp"
#include "opencv2/imgproc/imgproc.hpp"
#include "opencv2/objdetect/objdetect.hpp"
#include <opencv2/tracking.hpp>
#include <opencv2/videoio.hpp>
#include "opencv2/core/utility.hpp"

using namespace std;
using namespace cv;
const int k_width_pixels = 640;
const int k_height_pixels = 480;

// Cascade classifier image processing constants
/////////////////////////////
const string k_pipe_classifier_model = "black_pipe_small.xml";
const double k_classifier_scale_factor = 1.1;
const int k_minimum_neighbours = 2;
const int k_haar_detect_flags = 0 | CV_HAAR_SCALE_IMAGE;
const int k_min_obj_size = 30;
const int k_max_objsize = 200;

// Hough transform image processing constants
/////////////////////////////
//preprocessing control cluster - altering this has little effect on the results
const int k_gaussian_kernel_size = 19;
const int k_gaussian_kernel_deviation = 2;
//algorithm control cluster - impacts the results heavily. For noisy results,
//reduce the canny params
const double k_accumulator_resolution = 2.0;
const double k_canny_param_1 = 100.0;
const double k_canny_param_2 = 100.0;
//image dimension control cluster - impacts how big or small object we want to find
const double k_min_radius = 25.0;
const double k_min_circle_distance = k_min_radius * 2.0;
const double k_max_radius = k_min_radius * 4.0;

// Contour rectangle image processing constants
/////////////////////////////
const double k_threshold_max = 255;
const double k_threshold_step = 26;
const double k_shape_appoximator_ratio = 0.03;
const double k_min_contour_area = 250;
const double k_max_contour_area_divisor = 5.0;
const double k_cosine_angle = 0.4;
const double k_centroid_min_distance = 25.0;

// Hough line image processing constants
/////////////////////////////
const double k_min_canny_threshold = 50;
const double k_max_canny_threshold = 200;
const double k_hough_line_rho = 1.0;
const double k_hough_line_theta = CV_PI / 180.0;
const double k_hough_line_threshold = 40.0;
const double k_hough_line_length_factor = 0.35;
const double k_hough_line_max_inter_distance = 10.0;

// Drawing constants
/////////////////////////////
const Scalar k_outline_color(93,255,254,250);
const int k_circle_thickness = 1;
const Scalar k_crosshair_color(0, 255, 0,250);
const int k_crosshair_length = 12;
const int k_crosshair_thickness = 1;
const int k_crosshair_type = MARKER_CROSS;
const int k_crosshair_circle_radius = 4;
const Scalar k_text_color(200, 200, 200);
const int k_text_thickness = 1;
const int k_text_font = FONT_HERSHEY_SIMPLEX;
const int k_text_offset_x = 5;
const int k_text_offset_y = 20;
const int k_line_type = CV_AA;
const int k_line_thickness = 1;

//Hough transform image processing structure
struct HoughTransformParameters {
    int     m_gaussian_kernel_size;
    int     m_gaussian_kernel_deviation;
    double  m_accumulator_resolution ;
    double  m_min_circle_distance ;
    double  m_canny_param_1;
    double  m_canny_param_2 ;
    double  m_min_radius ;
    double  m_max_radius ;
    HoughTransformParameters()
    {
        m_gaussian_kernel_size = k_gaussian_kernel_size;
        m_gaussian_kernel_deviation = k_gaussian_kernel_deviation;
        m_accumulator_resolution = k_accumulator_resolution;
        m_min_circle_distance = k_min_circle_distance;
        m_canny_param_1 = k_canny_param_1;
        m_canny_param_2 = k_canny_param_2;
        m_min_radius = k_min_radius;
        m_max_radius = k_max_radius;
    }
};

struct RectangleContour
{
    vector<Point> contour;
    double perimeter;
    Point centroid;
};



#endif //VISUALOGYX_IMAGEPROC_H_H
