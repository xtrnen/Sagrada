//
// Created by jtrne on 21.12.2019.
//
#include "opencv2/imgproc/imgproc.hpp"
#include "opencv2/highgui/highgui.hpp"
#include "opencv2/highgui.hpp"
#include <iostream>
#include <vector>
#include <string>

using namespace std;
using namespace cv;

Mat tap;

enum SagradaColor {
    S_RED,
    S_BLUE,
    S_GREEN,
    S_YELLOW,
    S_VIOLET,
    S_WHITE
};

struct color_range
{
    /*Yellow color*/
    Scalar lowYellow = Scalar(20,100,20);
    Scalar highYellow = Scalar(30,255,255);

    /*Green color*/
    Scalar lowGreen = Scalar(40,50,20);
    Scalar highGreen = Scalar(90,255,255);

    /*Blue color*/
    Scalar lowBlue = Scalar(80,50,50);
    Scalar highBlue = Scalar(127,255,255);

    /*Red color*/
    Scalar lowRedFirstMask = Scalar(0, 150, 70);
    Scalar highRedFirstMask = Scalar(10, 255, 255);
    Scalar lowRedSecondMask = Scalar(165, 150, 70);
    Scalar highRedSecondMask = Scalar(180, 255, 255);

    /*Violet color*/
    Scalar lowViolet = Scalar(135,50,20);
    Scalar highViolet = Scalar(175,255,255);

    /*White color*/
    Scalar lowWhite = Scalar(0,0,168);
    Scalar highWhite = Scalar(180,50,255);
} COLOR_RANGES;

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
    Mat hsvImage;
    Mat tmp;
    //color ranges
    vector<Dice_s> dices;
    DiceAnalyzer(Mat _image)
    {
        this->diceImage = _image;
        this->tmp = Mat::zeros( _image.size(), CV_8UC3);
    }
    void DetectDices()
    {
        //RGB -> HSV
        cvtColor(this->diceImage, this->hsvImage, COLOR_BGR2HSV);
        //Filter each color
        DetectColor(S_RED, 50000.0);
        DetectColor(S_GREEN, 50000.0);
        DetectColor(S_BLUE, 50000.0);
        DetectColor(S_YELLOW, 50000.0);
        DetectColor(S_VIOLET, 50000.0);
        //TODO: Detect White color
        //detect number
        DetectNumber();
        //DEBUG
        for(int i; i < this->dices.size(); i++){
            rectangle(this->tmp, this->dices[i].boundRect.tl(), this->dices[i].boundRect.br(), Scalar(0,0,255), 5, LINE_AA);
        }
    }
    void DetectColor(SagradaColor _color, double size)
    {
        Mat colorMask;
        vector<Scalar> cRange = SelectRange(_color);
        //Red color complex mask
        if(_color == S_RED)
        {
            Mat tmp;
            inRange(this->hsvImage, cRange[0], cRange[2], tmp);
            inRange(this->hsvImage, cRange[1], cRange[3], colorMask);
            colorMask += tmp;
            colorMask.copyTo(tap);
        }
        else
        {
            inRange(this->hsvImage, cRange[0], cRange[1], colorMask);
        }

        Mat drawing = Mat::zeros( this->hsvImage.size(), CV_8UC3);
        vector<vector<Point>> contours;
        vector<Vec4i> hierarchy;

        findContours(colorMask, contours, hierarchy, RETR_TREE, CHAIN_APPROX_SIMPLE);

        vector<vector<Point>> contours_poly( contours.size());

        for( size_t i = 0; i < contours.size(); i++ )
        {
            double epsilon = 0.01*arcLength(contours[i],true);
            approxPolyDP( contours[i], contours_poly[i], epsilon, true );

            double area = contourArea(contours_poly[i]);
            if(area > 50000.0){
                drawContours(drawing, contours, (int) i, Scalar(0,0,255));
                Dice_s dice;
                dice.color = _color;
                dice.boundRect = boundingRect(contours_poly[i]);
                this->dices.push_back(dice);
            }
        }
    }
    void DetectNumber()
    {
        for(size_t index = 0; index < this->dices.size(); index++)
        {
            int number = 0;
            Dice_s victimDice = dices[index];
            Mat victimImg = this->hsvImage(victimDice.boundRect);
            if(IsNumber(victimImg, number))
            {
                this->dices[index].number = number;
            }
            else
            {
                //Analyze space
                    //Either random space or empty slot

            }
        }
    }
    void DetectEmptySlot()
    {
        //get template rect
        int defRectHeight;
        int defRectWidth;

        GetDefaultRect(defRectHeight, defRectWidth);
        //Get corner dice (at least one corner will have dice placed on template)

    }


    //
    vector<Scalar> SelectRange(SagradaColor _color)
    {
        vector<Scalar> outputVec;

        switch (_color)
        {
            case S_WHITE:
                outputVec.push_back(COLOR_RANGES.lowWhite);
                outputVec.push_back(COLOR_RANGES.highWhite);
            case S_RED:
                outputVec.push_back(COLOR_RANGES.lowRedFirstMask);
                outputVec.push_back(COLOR_RANGES.lowRedSecondMask);
                outputVec.push_back(COLOR_RANGES.highRedFirstMask);
                outputVec.push_back(COLOR_RANGES.highRedSecondMask);
            case S_GREEN:
                outputVec.push_back(COLOR_RANGES.lowGreen);
                outputVec.push_back(COLOR_RANGES.highGreen);
            case S_BLUE:
                outputVec.push_back(COLOR_RANGES.lowBlue);
                outputVec.push_back(COLOR_RANGES.highBlue);
            case S_YELLOW:
                outputVec.push_back(COLOR_RANGES.lowYellow);
                outputVec.push_back(COLOR_RANGES.highYellow);
            case S_VIOLET:
                outputVec.push_back(COLOR_RANGES.lowViolet);
                outputVec.push_back(COLOR_RANGES.highViolet);
        }
        return outputVec;
    }
    bool IsNumber(Mat _img, int& _number)
    {
        //TODO Correct number count, because of random contour
        Mat mask;
        int number = 0;

        inRange(_img, COLOR_RANGES.lowWhite, COLOR_RANGES.highWhite, mask);

        vector<vector<Point>> contours;
        vector<Vec4i> hierarchy;

        findContours(mask, contours, hierarchy, RETR_TREE, CHAIN_APPROX_SIMPLE);

        vector<vector<Point>> contours_poly( contours.size());

        for( size_t i = 0; i < contours.size(); i++ )
        {
            double epsilon = 0.01*arcLength(contours[i],true);
            approxPolyDP( contours[i], contours_poly[i], epsilon, true );

            double area = contourArea(contours_poly[i]);
            if(area > 2000.0 && contours_poly[i].size() > 5){
                number++;
            }
        }

        _number = number;

        return true;
    }
    void DiceOutput()
    {
        for(int i = 0; i < this->dices.size(); i++)
        {
            __android_log_print(ANDROID_LOG_INFO, "DiceOutput", "%d", this->dices[i].number);
            if(i % 4 == 0)
            {
                __android_log_print(ANDROID_LOG_INFO, "DiceOutput", "------");
            }
        }
    }
    void GetDefaultRect(int& _height, int& _width)
    {
        int sumHeight = 0;
        int sumWidth = 0;

        for(int i = 0; i < this->dices.size(); i++)
        {
            sumHeight += this->dices[i].boundRect.height;
            sumWidth += this->dices[i].boundRect.width;
        }

        _height = sumHeight / this->dices.size();
        _width = sumWidth / this->dices.size();
    }
    int GetCornerDice(Dice_s& _dice)
    {
        Dice_s cornerDice = Dice_s();
        int mode = 0;
        vector<Dice_s> selDices = this->dices;

        //search for dice with number and color
        //for()
        //take TL, TR, BL, BR if there is. At least one will be

        _dice = cornerDice;
        return mode;
    }
};