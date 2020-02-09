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

jobjectArray BuildOutput(JNIEnv *env, vector<Dice_s> dices);
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
JNIEXPORT jobjectArray JNICALL Java_Model_ImageProcessor_DiceDetector(JNIEnv *env, jobject obj, jlong output)
{
    Mat& outputImg = *(Mat*) output;
    Mat diceImg = GetObjectImg(env, obj, "org/opencv/core/Mat", "diceImg");

    cvtColor(diceImg, diceImg, COLOR_BGR2RGB);
    DiceAnalyzer diceAnalyzer = DiceAnalyzer(diceImg);
    diceAnalyzer.DetectDiceGrid();
    diceAnalyzer.DetectDiceSlots();
    diceAnalyzer.DetectDices();
    //diceAnalyzer.DiceOutput();
    //TODO: map dices to Java Array
    jobjectArray outputArray = BuildOutput(env, diceAnalyzer.dices);
    return outputArray;
    //diceAnalyzer.diceBoundImg.copyTo(outputImg);
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
jobjectArray BuildOutput(JNIEnv *env, vector<Dice_s> dices)
{
    jclass jDice = env->FindClass("Model/Structs/Dice");
    jobjectArray jDiceArray = env->NewObjectArray(dices.size(), jDice, 0);

    jmethodID jDiceInit = env->GetMethodID(jDice, "<init>", "(Ljava/lang/String;III)V");
    if(jDiceInit == 0)
        __android_log_print(ANDROID_LOG_ERROR, "BuildOutput", "Cannot create init method!");

    for(int i = 0; i < dices.size(); i++){
        jstring dColor = env->NewStringUTF(dices[i].GetColorString().c_str());
        jobject dice = env->NewObject(jDice, jDiceInit, dColor, dices[i].number, dices[i].row, dices[i].col);
        env->SetObjectArrayElement(jDiceArray, i, dice);
    }

    return jDiceArray;
}