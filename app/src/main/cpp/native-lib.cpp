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

jobjectArray BuildDicesOutput(JNIEnv *env, vector<Dice_s> dices);
jobjectArray BuildSlotsOutput(JNIEnv *env, vector<Slot> slots);

extern "C"
JNIEXPORT jobjectArray JNICALL Java_Model_ImageProcessor_PatternDetector(JNIEnv *env, jobject obj, jlong output){
    Mat& outputImg = *(Mat*) output;
    Mat patternImg;
    Mat imgTemplate;

    patternImg = GetObjectImg(env, obj, "org/opencv/core/Mat", "patternImg");

    PatternAnalyzer patternAnalyzer = PatternAnalyzer(patternImg);

    vector<Mat> grid = patternAnalyzer.CreatePatternGrid();

    //Correction in grid creater
    patternAnalyzer.GetCardPattern(grid);

    patternAnalyzer.tmp.copyTo(outputImg);

    return BuildSlotsOutput(env, patternAnalyzer.slots);

    /*Mat labImg;
    Mat mask;
    Mat kernel = Mat::ones(Size(3,3), CV_8UC1);
    double scaleWidth;
    double scaleHeight;
    Rect bound = Rect(0,0,0,0);
    int L1 = 0;
    int L2 = 35;
    int a1 = -20;
    int a2 = 20;
    int b1 = -10;
    int b2 = 10;
    //convert image to Lab color space
    cvtColor(patternImg, labImg, COLOR_BGR2Lab);
    //Set scale values for resized image
    DiceAnalyzer::SetScaleValues(patternImg.size().width, patternImg.size().height, scaleWidth, scaleHeight, 512, 512);
    //Set OpenCV Lab values for black
    DiceAnalyzer::CalculateLabValues(L1, a1, b1); //lower bound
    DiceAnalyzer::CalculateLabValues(L2, a2, b2); //upper bound
    //Create black color mask
    inRange(labImg, Scalar(L1, a1, b1), Scalar(L2, a2, b2), mask);
    //resize mask
    resize(mask, mask, Size(512,512));
    //erode
    //erode(mask, mask, kernel, Point(-1,-1), 5);
    //Find contours
    vector<vector<Point>> contours;
    findContours(mask, contours, RETR_TREE, CHAIN_APPROX_SIMPLE);
    for(size_t i = 0; i < contours.size(); i++){
        Rect tmpBound = boundingRect(contours[i]);
        //filter contours && find fitting contours
        //TODO: Instead of bound.width use bound.height when image is being rotated correctly
        if(tmpBound.area() > 30000 && tmpBound.width > 100){
            //Store the lowest fitting contour
            if(tmpBound.area() < bound.area() || bound.width == 0){
                bound = tmpBound;
            }
        }
    }
    //scale bound to fit diceImg size
    bound.width =(int) (bound.width * scaleWidth);
    bound.height =(int) (bound.height * scaleHeight);
    bound.x =(int) (bound.x * scaleWidth);
    bound.y =(int) (bound.y * scaleHeight);
    mask.copyTo(outputImg);*/

    /*Mat kernel = Mat::ones(Size(3,3), CV_8UC1);
    Mat img;
    int ind = 0;
    int c = 0;
    grid[ind].copyTo(img);

    cvtColor(img, img, COLOR_BGR2GRAY);

    vector<s_circle> circles;

    threshold(img, img, 0, 255, THRESH_BINARY|THRESH_OTSU);
    dilate(img,img, kernel, Point(-1,-1), 2);

    vector<vector<Point>> contours;
    findContours(img, contours, RETR_LIST, CHAIN_APPROX_SIMPLE);
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
            circle(grid[ind], center, (int)radius, Scalar(255,0,0), 5);
            //drawContours(img, contours, i, Scalar(0,0,255), 3);
            //circles.push_back(s_circle{(int)radius, Point((int)center.x, (int)center.y)});
            c++;
        }
    }

    if(c == 0){
        threshold(img, img, 0, 255, THRESH_BINARY_INV);
        dilate(img,img, kernel, Point(-1,-1), 4);
        distanceTransform(img, img, DIST_L2, 3);
        normalize(img, img, 0, 1.0, NORM_MINMAX);
        threshold(img, img, 0.5, 1.0, THRESH_BINARY);
        img.convertTo(img, CV_8U, 255.0);

        findContours(img, contours, RETR_LIST, CHAIN_APPROX_SIMPLE);

        for(int i = 0; i < contours.size(); i++)
        {
            vector<Point> polyContour;
            Point2f center;
            float radius;
            double epsilon = 0.01*arcLength(contours[i],true);
            approxPolyDP( contours[i], polyContour, epsilon, true );
            int area = (int)contourArea(contours[i]);
            minEnclosingCircle(contours[i], center, radius);
            int circleArea = (int)(radius*radius*3.14);
            int offsetArea = (int)(circleArea* 0.25);
            //__android_log_print(ANDROID_LOG_INFO, "CIRCLE", "%d | %d", area, circleArea);
            if(polyContour.size() > 5 && area > 100 && area >= circleArea - offsetArea && area <= circleArea + offsetArea){
                circle(grid[ind], center, (int)radius, Scalar(0,255,0), 5);
                //drawContours(img, contours, i, Scalar(0,0,255), 3);
                //circles.push_back(s_circle{(int)radius, Point((int)center.x, (int)center.y)});
            }
        }
    }

    grid[ind].copyTo(outputImg);*/
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
    jobjectArray outputArray = BuildDicesOutput(env, diceAnalyzer.dices);

    diceAnalyzer.diceBoundImg.copyTo(outputImg);
    return outputArray;
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

jobjectArray BuildSlotsOutput(JNIEnv *env, vector<Slot> slots)
{
    jclass jSlot = env->FindClass("Model/GameBoard/Structs/Slot");
    jobjectArray jSlotArray = env->NewObjectArray(slots.size(), jSlot, 0);

    jmethodID jSlotInit = env->GetMethodID(jSlot, "<init>", "(Ljava/lang/String;II)V");
    if(jSlotInit == 0){
        __android_log_print(ANDROID_LOG_ERROR, "BuildSlotsOutput", "Cannot create init method!");
    }

    for(int i = 0; i < slots.size(); i++){
        jstring slotInfo = env->NewStringUTF(slots[i].RetStr().c_str());
        jobject slot = env->NewObject(jSlot, jSlotInit, slotInfo, slots[i].row, slots[i].col);
        env->SetObjectArrayElement(jSlotArray, i, slot);
    }

    return jSlotArray;
}

jobjectArray BuildDicesOutput(JNIEnv *env, vector<Dice_s> dices)
{
    jclass jDice = env->FindClass("Model/GameBoard/Structs/Dice");
    jobjectArray jDiceArray = env->NewObjectArray(dices.size(), jDice, 0);

    jmethodID jDiceInit = env->GetMethodID(jDice, "<init>", "(Ljava/lang/String;III)V");
    if(jDiceInit == 0)
        __android_log_print(ANDROID_LOG_ERROR, "BuildDicesOutput", "Cannot create init method!");

    for(int i = 0; i < dices.size(); i++){
        jstring dColor = env->NewStringUTF(dices[i].GetColorString().c_str());
        jobject dice = env->NewObject(jDice, jDiceInit, dColor, dices[i].number, dices[i].row, dices[i].col);
        env->SetObjectArrayElement(jDiceArray, i, dice);
    }

    return jDiceArray;
}