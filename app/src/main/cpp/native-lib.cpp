#include <jni.h>
#include "opencv2/imgproc/imgproc.hpp"
#include "opencv2/highgui/highgui.hpp"
#include "opencv2/highgui.hpp"
#include "opencv2/features2d.hpp"
#include <iostream>
#include <android/log.h>
#include <algorithm>
#include <vector>
#include "DiceAnalyzer.cpp"
#include "PatternAnalyzer.cpp"
#include <string>

using namespace cv;
using namespace std;

Mat tp;

Mat GetObjectImg(JNIEnv *env, jobject obj, string _propTypeRoute, string _propName);

vector<s_circle> DetectLowerNumberContour(Mat _img)
{
    vector<s_circle> circles;
    vector<vector<Point>> contours;
    findContours(_img, contours, RETR_LIST, CHAIN_APPROX_SIMPLE);
    for(int i = 0; i < contours.size(); i++)
    {
        vector<Point> polyContour;
        Point2f center;
        float radius;
        double epsilon = 0.01*arcLength(contours[i],true);
        approxPolyDP( contours[i], polyContour, epsilon, true );
        int area = (int)contourArea(polyContour);
        minEnclosingCircle(contours[i], center, radius);
        int circleArea = (int)(radius*radius*3.14);
        int offsetArea = (int)(circleArea* 0.25);
        if(polyContour.size() > 5 && area > 100 && area >= circleArea - offsetArea && area <= circleArea + offsetArea){
            circle(tp, center, radius + 5, Scalar(0,255,0), 5);
            //drawContours(img, contours, i, Scalar(0,0,255), 3);
            circles.push_back(s_circle{(int)radius, Point((int)center.x, (int)center.y)});
        }
    }

    __android_log_print(ANDROID_LOG_INFO, "NUMBER Contour", "%d", circles.size());

    return circles;
}

int ControlNumber(bool q1,bool q2,bool q3,bool q4,bool qC)
{
    if((q1 || q3) && qC)
    {
        return 3;
    }
    else if(q2 || q4)
    {
        return 2;
    }
    else if(qC)
    {
        return 1;
    }
    else
    {
        return -42;
    }
}

int ControlBlobPosition(vector<s_circle> circles, Point imgCenter)
{
    bool q1 = false;
    bool q2 = false;
    bool q3 = false;
    bool q4 = false;
    bool qC = false;
    for(s_circle circ : circles)
    {
        if(circ.center.y >= imgCenter.y - circ.radius && circ.center.y <= imgCenter.y + circ.radius)
        {
            qC = true;
        }
        else if(circ.center.x > imgCenter.x + circ.radius && circ.center.y < imgCenter.y)
        {
            if(circ.center.x > imgCenter.x + circ.radius)
            {
                q1 = true;
            }
            else
            {
                q2 = true;
            }
        }
        else if(circ.center.y > imgCenter.y)
        {
            if(circ.center.x < imgCenter.x - circ.radius)
            {
                q3 = true;
            }
            else{
                q4 = true;
            }
        }
    }

    int number = ControlNumber(q1, q2, q3, q4, qC);
    if(number == 3 || number == 2 || number == 1)
    {
        return number;
    }
    else
    {
        return -42;
    }
}

extern "C"
JNIEXPORT void JNICALL Java_Model_ImageProcessor_testFunction(JNIEnv *env, jobject obj, jlong output){
    Mat& outputImg = *(Mat*) output;
    Mat patternImg;
    Mat imgTemplate;

    patternImg = GetObjectImg(env, obj, "org/opencv/core/Mat", "patternImg");
    imgTemplate = GetObjectImg(env, obj, "org/opencv/core/Mat", "templateImg");

    PatternAnalyzer patternAnalyzer = PatternAnalyzer(patternImg, imgTemplate);

    vector<Mat> grid = patternAnalyzer.CreatePatternGrid();

    //Correction in grid creater
    patternAnalyzer.GetCardPattern(grid);

    patternAnalyzer.patternImg.copyTo(outputImg);
};
extern "C"
JNIEXPORT void JNICALL Java_Model_ImageProcessor_DiceDetector(JNIEnv *env, jobject obj, jlong output)
{
    Mat& outputImg = *(Mat*) output;
    Mat diceImg = GetObjectImg(env, obj, "org/opencv/core/Mat", "diceImg");
    Mat tmp;

    cvtColor(diceImg, diceImg, COLOR_BGR2RGB);
    DiceAnalyzer diceAnalyzer = DiceAnalyzer(diceImg);
    diceAnalyzer.DetectDiceGrid();
    diceAnalyzer.DetectDices();
    diceAnalyzer.SortDices(4,5);
    diceAnalyzer.DiceOutput();

    /*Mat mask;
    Mat img;
    cvtColor(diceAnalyzer.diceImage, img, COLOR_BGR2Lab);
    int L1 = 0;
    int L2 = 35 * 255/100;
    int a1 = -20 + 128;
    int a2 = 20 + 128;
    int b1 = -10 + 128;
    int b2 = 10 + 128;
    inRange(img, Scalar(L1, a1, b1), Scalar(L2, a2, b2), mask);

    double scaleWidth = img.size().width / 512.0;
    double scaleHeight = img.size().height / 512.0;
    //
    resize(mask,mask, Size(512,512));

    Mat kernel = Mat::ones(Size(3,3), CV_8UC1);
    erode(mask,mask, kernel, Point(-1,-1), 5);
    vector<vector<Point>> contours;

    findContours(mask, contours, RETR_TREE, CHAIN_APPROX_SIMPLE);

    vector<vector<Point>> contours_poly( contours.size());
    Rect boundry = Rect(0,0,0,0);
    for( size_t i = 0; i < contours.size(); i++ )
    {
        double epsilon = 0.01*arcLength(contours[i],true);
        approxPolyDP( contours[i], contours_poly[i], epsilon, true );
        Rect bound = boundingRect(contours[i]);
        if(bound.area() > 30000.0 && bound.width > 100){//height
            if(boundry.width == 0){
                boundry = bound;
            }
            if(boundry.area() > bound.area()){
                boundry = bound;
            }
            __android_log_print(ANDROID_LOG_INFO, "RECT SIZE", "w: %d | h: %d |area: %d", bound.width, bound.height, bound.area());
            bound.width =(int) (bound.width * scaleWidth);
            bound.height =(int) (bound.height * scaleHeight);
            bound.x =(int) (bound.x * scaleWidth);
            bound.y =(int) (bound.y * scaleHeight);
            rectangle(diceAnalyzer.diceImage, bound, Scalar(255,255,255), 5);
        }
    }
    boundry.width =(int) (boundry.width * scaleWidth);
    boundry.height =(int) (boundry.height * scaleHeight);
    boundry.x =(int) (boundry.x * scaleWidth);
    boundry.y =(int) (boundry.y * scaleHeight);
    if(boundry.width != 0)
        diceAnalyzer.diceImage(boundry).copyTo(outputImg);*/

    diceAnalyzer.tmp.copyTo(outputImg);
};
Mat GetObjectImg(JNIEnv *env, jobject obj, string _propTypeRoute, string _propName){
    jclass thisClass = env->GetObjectClass(obj);
    jclass typeClass = env->FindClass(&_propTypeRoute[0]);
    jmethodID ptrMethod = env->GetMethodID(typeClass, "getNativeObjAddr", "()J");

    string propTypeRoute = "L" + _propTypeRoute + ";";

    jfieldID propID = env->GetFieldID(thisClass, &_propName[0], &propTypeRoute[0]);
    jobject propJ = env->GetObjectField(obj, propID);

    return *(Mat*)env->CallLongMethod(propJ, ptrMethod);
}