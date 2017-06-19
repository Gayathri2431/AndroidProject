//
// Created by Anirudh N J on 02/03/17.
//
#include <jni.h>
#include "ImageProc.h"

extern "C" {


MultiTracker *trackers = 0;
int trackerFrameCounter = 0;
vector<Vec3f> g_circles;


void draw_circle_result(Mat &src, Point &center, int average_radius, int count_num);
bool compare_vec3f(Vec3f a, Vec3f b);
vector<Vec3f> filter_concentric_circles(vector<Vec3f> circles);


//Tracking code

void init_tracker_circles(const vector<Vec3f> &circles, Mat &frame, MultiTracker *trackers);
void add_tracker_circles(const vector<Vec3f> &circles, Mat &frame, MultiTracker *trackers);
void initialize_tracker();
void destroy_tracker();
Vec3f get_circle_point_from_rect_bounds(Rect2d rect);

vector<Vec3f> process_image_hough_small_circles(Mat &src);
vector<Vec3f> process_image_hough_circles(Mat &src);
vector<Vec3f> circles ,circles_all;


JNIEXPORT void JNICALL Java_com_cameracountmodule_Algorithm_ImageAnalysis_FindCircles(JNIEnv*, jobject, jlong addrRgba, jlong addrGray , jlong addrCircleMat)
{
    Mat&  src  = *(Mat*)addrRgba;
    Mat&  gray = *(Mat*)addrGray;
    Mat&  circleMat =  *(Mat*)addrCircleMat;

    HoughTransformParameters ht_param;

    //Pre processing of the image
    cvtColor(src, gray, CV_BGR2GRAY);
    GaussianBlur(gray, gray,
                 Size(ht_param.m_gaussian_kernel_size, ht_param.m_gaussian_kernel_size),
                 ht_param.m_gaussian_kernel_deviation,
                 ht_param.m_gaussian_kernel_deviation);
    //Object detection
    HoughCircles(gray, circles, CV_HOUGH_GRADIENT,
                 ht_param.m_accumulator_resolution,
                 ht_param.m_min_circle_distance,
                 ht_param.m_canny_param_1, ht_param.m_canny_param_2,
                 (int)ht_param.m_min_radius, (int)ht_param.m_max_radius);
    //Averaging and sorting the detected objects
    int num_circles = (int)circles.size();
    if(num_circles == 0)
    {
        gray = src;
        return;
    }

    vector<int> circles_radii;

    for (size_t i = 0; i < num_circles; i++)
    {
        circles_radii.push_back(cvRound(circles[i][2]));
    }
    //int sum_radii = accumulate(circles_radii.begin(), circles_radii.end(), 0);
    int sum_radii = 0;
    for(int i = 0 ; i < circles_radii.size() ; i++)
    {
            sum_radii += circles_radii.at(i);
    }

    int average_radius = sum_radii / num_circles;
    sort(circles.begin(), circles.end(), compare_vec3f);
    circleMat = Mat(circles);
    //Averaging, sorting the detected objects and overlay the results
    //__android_log_print(ANDROID_LOG_INFO, "foo ","%s",SSTR(num_circles).c_str());
    for (size_t i = 0; i < num_circles; i++)
    {
        Point center(cvRound(circles[i][0]), cvRound(circles[i][1]));
        //draw_circle_result(src, center, average_radius, i);
    }
    gray = src.clone();
}

JNIEXPORT void JNICALL Java_com_cameracountmodule_Algorithm_ImageAnalysis_FindSmallCircles(JNIEnv*, jobject, jlong addrRgba, jlong addrGray ,jlong addrCircleMat)
{
    Mat& src = *(Mat*)addrRgba;
    Mat&  gray = *(Mat*)addrGray;
    Mat&  circleMat =  *(Mat*)addrCircleMat;

    //Pre processing of the image
    cvtColor(src, gray, CV_BGR2GRAY);
    GaussianBlur(gray, gray, Size(k_gaussian_kernel_size, k_gaussian_kernel_size),
                 k_gaussian_kernel_deviation, k_gaussian_kernel_deviation);

    //Object detection
    vector<Vec3f> circles_all, circles;
    for(int i = 0 ; i < 5; i++)
    {
        vector<Vec3f> circles_i;
        HoughCircles(gray, circles_i, CV_HOUGH_GRADIENT,
                     k_accumulator_resolution, 2.0,
                     (80 - (i*10)),
                     (80 - (i*10)),
                     2, 24);

        circles_all.insert(circles_all.end(), circles_i.begin(), circles_i.end());
    }
    circles = filter_concentric_circles(circles_all);

    //Averaging, sorting the detected objects and overlay the results
    int num_circles = (int)circles.size();
    //__android_log_print(ANDROID_LOG_INFO, "foo ","%s",SSTR(num_circles).c_str());
    if(num_circles == 0)
    {
        gray = src;
        return;
    }

    vector<int> circles_radii;
    for (size_t i = 0; i < num_circles; i++)
    {
        circles_radii.push_back(cvRound(circles[i][2]));
    }

    //int sum_radii = accumulate(circles_radii.begin(), circles_radii.end(), 0);
    int sum_radii = 0;
    for(int i = 0 ; i < circles_radii.size() ; i++)
    {
        sum_radii += circles_radii.at(i);
    }


    int average_radius = sum_radii / num_circles;

    sort(circles.begin(), circles.end(), compare_vec3f);
    circleMat = Mat(circles);

    for (size_t i = 0; i < num_circles; i++)
    {
        Point center(cvRound(circles[i][0]), cvRound(circles[i][1]));
        //draw_circle_result(src, center, average_radius, i);
    }

    gray = src;
}

bool compare_vec3f(Vec3f a, Vec3f b)
{
    if( a[1] < b[1])
    {
        return true;
    }
    else
    {
        return false;
    }
}


JNIEXPORT void JNICALL Java_com_cameracountmodule_Algorithm_ImageAnalysis_FindCirclesSelective(JNIEnv*, jobject, jlong addrRgba, jlong addrGray , jlong addrCircleMat ,jfloat  circle_radius)
{
    vector<Vec3f> circles ,circles_all;

    if(circles.size()!=0)
    {
        circles.clear();
        circles_all.clear();
    }

    Mat& src = *(Mat*)addrRgba;
    Mat&  gray = *(Mat*)addrGray;
    Mat&  circleMat =  *(Mat*)addrCircleMat;


    //Pre processing of the image
    cvtColor(src, gray, CV_BGR2GRAY);
    GaussianBlur(gray, gray, Size(k_gaussian_kernel_size, k_gaussian_kernel_size),
                 k_gaussian_kernel_deviation, k_gaussian_kernel_deviation);


    //Object detection
    for(int i = 0 ; i < 3; i++)
    {
        vector<Vec3f> circles_i;
        HoughCircles(gray, circles_i, CV_HOUGH_GRADIENT,
                     k_accumulator_resolution, 2 * circle_radius,
                     (k_canny_param_1 - (i*10)),
                     (k_canny_param_2 - (i*10)),
                     circle_radius, 4 * circle_radius);

        circles_all.insert(circles_all.end(), circles_i.begin(), circles_i.end());
    }
    circles = filter_concentric_circles(circles_all);

    //Averaging, sorting the detected objects and overlay the results
    int num_circles = (int)circles.size();
    if(num_circles == 0)
    {
        gray = src;
        return;
    }

    vector<int> circles_radii;
    for (size_t i = 0; i < num_circles; i++)
    {
        circles_radii.push_back(cvRound(circles[i][2]));
    }

    int sum_radii = accumulate(circles_radii.begin(), circles_radii.end(), 0);
    int average_radius = sum_radii / num_circles;

    sort(circles.begin(), circles.end(), compare_vec3f);
    circleMat = Mat(circles);


    for (size_t i = 0; i < num_circles; i++)
    {
        Point center(cvRound(circles[i][0]), cvRound(circles[i][1]));
        //draw_circle_result(src, center, average_radius, i);
    }
    gray = src;

}

JNIEXPORT void JNICALL Java_com_cameracountmodule_Algorithm_ImageAnalysis_FindSmallCirclesSelective(JNIEnv*, jobject, jlong addrRgba, jlong addrGray , jlong addrCircleMat ,jfloat  circle_radius)
{
   vector<Vec3f> circles ,circles_all;

    if(circles.size()!=0)
    {
        circles.clear();
        circles_all.clear();
    }

    Mat& src = *(Mat*)addrRgba;
    Mat&  gray = *(Mat*)addrGray;
    Mat&  circleMat =  *(Mat*)addrCircleMat;

    //Pre processing of the image
    cvtColor(src, gray, CV_BGR2GRAY);
    GaussianBlur(gray, gray, Size(k_gaussian_kernel_size, k_gaussian_kernel_size),
                 k_gaussian_kernel_deviation, k_gaussian_kernel_deviation);

    //Object detection
    for(int i = 0 ; i < 3; i++)
    {
        vector<Vec3f> circles_i;
        HoughCircles(gray, circles_i, CV_HOUGH_GRADIENT,
                     k_accumulator_resolution, circle_radius,
                     (80 - (i*10)),
                     (80 - (i*10)),
                     (int)circle_radius, (int)(circle_radius * 12.0));

        circles_all.insert(circles_all.end(), circles_i.begin(), circles_i.end());
    }
    circles = filter_concentric_circles(circles_all);

    //Averaging, sorting the detected objects and overlay the results
    int num_circles = (int)circles.size();
    if(num_circles == 0)
    {
        gray = src;
        return;
    }

    vector<int> circles_radii;
    for (size_t i = 0; i < num_circles; i++)
    {
        circles_radii.push_back(cvRound(circles[i][2]));
    }

    int sum_radii = accumulate(circles_radii.begin(), circles_radii.end(), 0);
    int average_radius = sum_radii / num_circles;

    sort(circles.begin(), circles.end(), compare_vec3f);
    circleMat = Mat(circles);

    for (size_t i = 0; i < num_circles; i++)
    {
        Point center(cvRound(circles[i][0]), cvRound(circles[i][1]));
        //draw_circle_result(src, center, average_radius, i);
    }
    gray = src;

}


void draw_circle_result(Mat &src, Point &center, int average_radius, int count_num)
{


    circle(src, center, average_radius,
           k_outline_color, k_circle_thickness, k_line_type);

    drawMarker(src, center, k_crosshair_color,
               k_crosshair_type, k_crosshair_length,
               k_crosshair_thickness, k_line_type);

    circle(src, center, k_crosshair_circle_radius,
           k_crosshair_color, k_crosshair_thickness, k_line_type);


}

vector<Vec3f> filter_concentric_circles(vector<Vec3f> circles)
{
    vector<Vec3f> circles_2;

    if(circles.size() == 0)
        return circles_2;

    auto in_itr = circles.begin();
    circles_2.push_back(*in_itr);
    ++in_itr;

    while(in_itr != circles.end())
    {
        bool similar_circle_added = false;
        for(auto out_itr = circles_2.begin();
            out_itr != circles_2.end() ;
            ++out_itr)
        {
            if( ( abs ( (*in_itr)[0] - (*out_itr)[0] ) < (*out_itr)[2] ) &&
                ( abs ( (*in_itr)[1] - (*out_itr)[1]) < (*out_itr)[2]) )
            {
                similar_circle_added = true;
                break;
            }
        }
        if(similar_circle_added == false)
        {
            circles_2.push_back(*in_itr);
        }
        ++in_itr;
    }
    return circles_2;
}


//-------------------------------Tracker Code---------------------------
JNIEXPORT void JNICALL Java_com_cameracountmodule_Algorithm_ImageAnalysis_InitTracker(JNIEnv*, jobject)
{
    trackers = new MultiTracker("KCF");
    trackerFrameCounter = 0;
    __android_log_print(ANDROID_LOG_INFO, "foo ","Tracker Code");
}

void init_tracker_circles(const vector<Vec3f> &circles, Mat &frame, MultiTracker *trackers)
{
    if (trackers == 0)
        return;

    vector<Rect2d> objects;
    for (int i = 0; i < circles.size(); i++)
    {
        cv::Point center_i(cvRound(circles[i][0]), cvRound(circles[i][1]));
        int radius_i = (int)circles[i][2];
        Rect2d circle_i_rect(center_i.x - radius_i,
                             center_i.y - radius_i,
                             2 * radius_i,
                             2 * radius_i);
        objects.push_back(circle_i_rect);

    }
    trackers->add(frame, objects);
}

void add_tracker_circles(const vector<Vec3f> &circles, Mat &frame, MultiTracker *trackers)
{
    if (trackers == 0)
        return;
    circles_all.clear();
    vector<Rect2d> objects;
    for (int i = 0; i < circles.size(); i++)
    {
        cv::Point center_i(cvRound(circles[i][0]), cvRound(circles[i][1]));
        int radius_i = (int)circles[i][2];
        bool circle_is_untracked = true;

        for (int trk_i = 0; trk_i < trackers->objects.size(); trk_i++)
        {
            if (trackers->objects[trk_i].contains(center_i))
            {
                circle_is_untracked = false;
                break;
            }
        }
        circles_all.push_back(circles.at(i));

        if (circle_is_untracked == true)
        {

            Rect2d circle_i_rect(center_i.x - radius_i,
                                 center_i.y - radius_i,
                                 2 * radius_i,
                                 2 * radius_i);
            objects.push_back(circle_i_rect);

        }
    }
    trackers->add(frame, objects);
}



JNIEXPORT void JNICALL Java_com_cameracountmodule_Algorithm_ImageAnalysis_Destroytracker(JNIEnv*, jobject)
{
    delete trackers;
    trackers = 0;
}

Vec3f get_circle_point_from_rect_bounds(Rect2d rect)
{
    Vec3f c_point;
    c_point[0] = (float)(rect.x + (rect.width / 2.0));
    c_point[1] = (float)(rect.y + (rect.height / 2.0));
    c_point[2] = (float)((rect.width + rect.height) / 4.0);
    return c_point;
}
int get_average_circle_radius(vector<Rect2d> rects)
{
    if(rects.size() == 0)
        return 0;
    int sum_radii = 0;
    for(int i = 0 ; i < rects.size() ; i++)
    {
        sum_radii += (rects[i].width + rects[i].height) / 4.0;
    }
    return (int)(sum_radii / rects.size());
}

JNIEXPORT int JNICALL Java_com_cameracountmodule_Algorithm_ImageAnalysis_ImageHoughCirclesTracker(JNIEnv*, jobject, jlong addrRgba, jlong addrGray , jlong addrCircleMat )
{

    if(circles.size()!=0)
    {
        circles.clear();
        circles_all.clear();
    }

    int average_radius = 0;
    Mat gray;

    Mat& in_image = *(Mat*)addrRgba;
    Mat&  out_image = *(Mat*)addrGray;
    Mat&  circleMat =  *(Mat*)addrCircleMat;

    jint size;

    cvtColor(in_image, gray, CV_BGR2RGB);

    if(trackers->objects.size() != 0) {
        trackerFrameCounter ++;
        if(trackerFrameCounter % 7)
        {
            trackers->update(gray);
        }
    }

    out_image = in_image;

    if (average_radius == 0)
    {
        average_radius = get_average_circle_radius(trackers->objects);
    }
    for (int i = 0; i < trackers->objects.size(); i++)
    {
        Vec3f c_point = get_circle_point_from_rect_bounds(trackers->objects[i]);
        cv::Point center(cvRound(c_point[0]), cvRound(c_point[1]));
        //draw_circle_result(out_image, center, average_radius, i);

    }

    auto circles = process_image_hough_circles(in_image);

    if(trackers->objects.size() == 0)
    {
        init_tracker_circles(circles, in_image, trackers);
    }
    else
    {
        add_tracker_circles(circles, in_image, trackers);
    }

    size = trackers->objects.size();
    circleMat = Mat(circles_all);
    __android_log_print(ANDROID_LOG_INFO, "foo---contour-centeroid->>>>> ","%s", SSTR(trackers->objects.size()).c_str());
    __android_log_print(ANDROID_LOG_INFO, "foo---contour-centeroid-##### ","%s", SSTR(circleMat.rows).c_str());

    return size;
}

JNIEXPORT int JNICALL Java_com_cameracountmodule_Algorithm_ImageAnalysis_ImageHoughSmallCirclesTracker(JNIEnv*, jobject, jlong addrRgba, jlong addrGray , jlong addrCircleMat )
{

    if(circles.size()!=0)
    {
        circles.clear();
        circles_all.clear();
    }

    int average_radius = 0;
    Mat gray;
    Mat& in_image = *(Mat*)addrRgba;
    Mat&  out_image = *(Mat*)addrGray;
    Mat&  circleMat =  *(Mat*)addrCircleMat;

    jint size;


    cvtColor(in_image, gray, CV_BGR2RGB);

    if(trackers->objects.size() != 0) {
        trackerFrameCounter ++;
        if(trackerFrameCounter % 7)
        {
            trackers->update(gray);
        }
    }

    out_image = in_image;

    if (average_radius == 0)
    {
        average_radius = get_average_circle_radius(trackers->objects);
    }
    for (int i = 0; i < trackers->objects.size(); i++)
    {
        Vec3f c_point = get_circle_point_from_rect_bounds(trackers->objects[i]);
        cv::Point center(cvRound(c_point[0]), cvRound(c_point[1]));
        //draw_circle_result(out_image, center, average_radius, i);

    }

    auto circles = process_image_hough_small_circles(in_image);

    if(trackers->objects.size() == 0)
    {
        init_tracker_circles(circles, in_image, trackers);
    }
    else
    {
        add_tracker_circles(circles, in_image, trackers);
    }

    size = trackers->objects.size();
    circleMat = Mat(circles_all);

    __android_log_print(ANDROID_LOG_INFO, "foo---contour-centeroid->>>>> ","%s", SSTR(trackers->objects.size()).c_str());
    __android_log_print(ANDROID_LOG_INFO, "foo---contour-centeroid-##### ","%s", SSTR(circleMat.rows).c_str());

    return size;
}

vector<Vec3f> process_image_hough_circles(Mat &src)
{
    Mat gray;
    //Pre processing of the image
    cvtColor(src, gray, CV_BGR2GRAY);
    GaussianBlur(gray, gray, cv::Size(k_gaussian_kernel_size, k_gaussian_kernel_size),
                 k_gaussian_kernel_deviation, k_gaussian_kernel_deviation);
    //Object detection
    vector<Vec3f> circles;
    HoughCircles(gray, circles, CV_HOUGH_GRADIENT,
                 k_accumulator_resolution, k_min_circle_distance,
                 k_canny_param_1, k_canny_param_2,
                 (int)k_min_radius, (int)k_max_radius);
    return circles;
}

vector<Vec3f> process_image_hough_small_circles(Mat &src)
{
    Mat gray;
    cvtColor(src, gray, CV_BGR2GRAY);
    GaussianBlur(gray, gray, cv::Size(k_gaussian_kernel_size, k_gaussian_kernel_size),
                 k_gaussian_kernel_deviation, k_gaussian_kernel_deviation);

    //Object detection
    vector<Vec3f> circles_all;
    for(int i = 0 ; i < 5; i++)
    {
        g_circles.clear();
        HoughCircles(gray, g_circles, CV_HOUGH_GRADIENT,
                     k_accumulator_resolution, 2.0,
                     (80 - (i*10)),
                     (80 - (i*10)),
                     2.0, 24.0);
        circles_all.insert(circles_all.end(), g_circles.begin(), g_circles.end());
    }
    circles_all = filter_concentric_circles(circles_all);
    return circles_all;
}


}
