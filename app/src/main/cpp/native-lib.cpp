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

Mat GetObjectImg(JNIEnv *env, jobject obj, string _propTypeRoute, string _propName);

extern "C"
JNIEXPORT void JNICALL Java_Model_ImageProcessor_testFunction(JNIEnv *env, jobject obj, jlong output){
    Mat& outputImg = *(Mat*) output;
    Mat patternImg;
    Mat imgTemplate;

    patternImg = GetObjectImg(env, obj, "org/opencv/core/Mat", "patternImg");
    imgTemplate = GetObjectImg(env, obj, "org/opencv/core/Mat", "templateImg");

    PatternAnalyzer patternAnalyzer = PatternAnalyzer(patternImg, imgTemplate);

    vector<Mat> grid = patternAnalyzer.CreatePatternGrid();

    patternAnalyzer.GetCardPattern(grid);

    patternAnalyzer.patternImg.copyTo(outputImg);
};
extern "C"
JNIEXPORT void JNICALL Java_Model_ImageProcessor_DiceDetector(JNIEnv *env, jobject obj, jlong output)
{
    Mat& outputImg = *(Mat*) output;
    Mat diceImg = GetObjectImg(env, obj, "org/opencv/core/Mat", "diceImg");
    Mat tmp;

    DiceAnalyzer diceAnalyzer = DiceAnalyzer(diceImg);
    diceAnalyzer.DetectDices();
    diceAnalyzer.DiceOutput();

    /*diceAnalyzer.hsvImage(diceAnalyzer.dices[16].boundRect).copyTo(tmp);

    Mat mask;

    inRange(tmp, COLOR_RANGES.lowWhite, COLOR_RANGES.highWhite, mask);

    vector<vector<Point>> contours;
    vector<Vec4i> hierarchy;

    findContours(mask, contours, hierarchy, RETR_TREE, CHAIN_APPROX_SIMPLE);

    vector<vector<Point>> contours_poly( contours.size());
    int num = 0;
    for( size_t i = 0; i < contours.size(); i++ )
    {
        double epsilon = 0.01*arcLength(contours[i],true);
        approxPolyDP( contours[i], contours_poly[i], epsilon, true );
        double area = contourArea(contours_poly[i]);
        if(area > 2000.0 && contours_poly[i].size() > 5){
            drawContours(tmp, contours, i, Scalar(0,0,0), FILLED);
            num++;
        }
        if(i == contours.size() - 1){
            __android_log_print(ANDROID_LOG_INFO, "Num", "%d", num);
        }
    }*/

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