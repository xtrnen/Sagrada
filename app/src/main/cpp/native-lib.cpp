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

jobjectArray BuildDicesOutput(JNIEnv *env, vector<Dice_s> dices);
jobjectArray BuildSlotsOutput(JNIEnv *env, vector<Slot> slots);

extern "C"
JNIEXPORT jobjectArray JNICALL Java_Model_ImageProcessor_PatternDetector(JNIEnv *env, jobject obj, jlong output){
    Mat& outputImg = *(Mat*) output;
    Mat patternImg;
    Mat imgTemplate;

    patternImg = GetObjectImg(env, obj, "org/opencv/core/Mat", "patternImg");

    cvtColor(patternImg, patternImg, COLOR_BGR2RGB);
    resize(patternImg, patternImg, Size(1024,720));
    PatternAnalyzer patternAnalyzer = PatternAnalyzer(patternImg);

    //Detect Control Point
    vector<Mat> grid = patternAnalyzer.CreatePatternGrid();
    //Detect Slots
    //Detect attribute
    patternAnalyzer.GetCardPattern(grid);

    patternAnalyzer.tmp.copyTo(outputImg);

    return BuildSlotsOutput(env, patternAnalyzer.slots);
};
extern "C"
JNIEXPORT jobjectArray JNICALL Java_Model_ImageProcessor_DiceDetector(JNIEnv *env, jobject obj, jlong output)
{
    Mat& outputImg = *(Mat*) output;
    Mat diceImg = GetObjectImg(env, obj, "org/opencv/core/Mat", "diceImg");

    cvtColor(diceImg, diceImg, COLOR_BGR2RGB);
    DiceAnalyzer diceAnalyzer = DiceAnalyzer(diceImg);
    diceAnalyzer.DetectDices();
    jobjectArray outputArray = BuildDicesOutput(env, diceAnalyzer.dices);
    resize(diceAnalyzer.diceBoundImg, outputImg, Size(diceImg.cols, diceImg.rows));
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

    if(slots.empty()){
        return jSlotArray;
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

    for(int i = 0; i < dices.size(); i++){
        jstring dColor = env->NewStringUTF(dices[i].GetColorString().c_str());
        jobject dice = env->NewObject(jDice, jDiceInit, dColor, dices[i].number, dices[i].row, dices[i].col);
        env->SetObjectArrayElement(jDiceArray, i, dice);
    }

    return jDiceArray;
}