//
// Created by jtrne on 21.12.2019.
//
#include "opencv2/imgproc/imgproc.hpp"
#include "opencv2/highgui/highgui.hpp"
#include "opencv2/highgui.hpp"
#include <iostream>
#include <vector>

using namespace std;
using namespace cv;

struct color_range
{
    /*Yellow color*/
    Scalar lowYellow = Scalar(20,100,20);
    Scalar highYellow = Scalar(30,255,255);

    /*Green color*/
    Scalar lowGreen = Scalar(40,50,20);
    Scalar highGreen = Scalar(70,255,255);

    /*Blue color*/
    Scalar lowBlue = Scalar(80,50,20);
    Scalar highBlue = Scalar(127,255,255);

    /*Red color*/
    Scalar lowRedFirstMask = Scalar(0, 150, 70);
    Scalar highRedFirstMask = Scalar(10, 255, 255);
    Scalar lowRedSecondMask = Scalar(175, 150, 70);
    Scalar highRedSecondMask = Scalar(180, 255, 255);

    /*Violet color*/
    Scalar lowViolet = Scalar(145,100,20);
    Scalar highViolet = Scalar(170,255,255);

    /*White color*/
    Scalar lowWhite = Scalar(0,0,168);
    Scalar highWhite = Scalar(180,50,255);
} COLOR_RANGES;

enum SagradaColor {
    S_RED,
    S_BLUE,
    S_GREEN,
    S_YELLOW,
    S_VIOLET,
    S_WHITE
};
struct Dice_s
{
    Rect boundRect;
    int number;
    SagradaColor color;
};

class DiceAnalyzer
{
public:
    Mat diceImage;
    //color ranges
    vector<Dice_s> dices;
    DiceAnalyzer(Mat _image)
    {
        this->diceImage = _image;
    }
    void PrepareImg()
    {

    }
    void DetectColor(){}
    void DetectNumber(){}
    void ControlEmptySlots(){}

};