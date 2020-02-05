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

enum SagradaColor {
    S_RED,
    S_BLUE,
    S_GREEN,
    S_YELLOW,
    S_VIOLET,
    S_WHITE,
    S_NONE
};

struct color_range
{
    /*Yellow color*/
    Scalar lowYellow = Scalar(15,150,20);//20
    Scalar highYellow = Scalar(35,255,255);

    /*Green color*/
    Scalar lowGreen = Scalar(40,100,35);
    Scalar highGreen = Scalar(85,255,255);//90-255-255

    /*Blue color*/
    Scalar lowBlue = Scalar(85,80,40);//80-50-20
    Scalar highBlue = Scalar(130,255,255);//127-255-255

    /*Red color*/
    Scalar lowRedFirstMask = Scalar(0, 150, 20);//(0, 150, 70)
    Scalar highRedFirstMask = Scalar(10, 255, 255);//(10, 255, 255)
    Scalar lowRedSecondMask = Scalar(175, 150, 20);//(165, 150, 70)
    Scalar highRedSecondMask = Scalar(180, 255, 255);//(180, 255, 255)

    /*Violet color*/
    Scalar lowViolet = Scalar(135,100,35);//135-50-20
    Scalar highViolet = Scalar(174,255,255);

    /*White color*/
    Scalar lowWhite = Scalar(0,0,168);
    Scalar lowDiceWhite = Scalar(0,0,120);
    Scalar highDiceWhite = Scalar(180,80,255);
    Scalar highWhite = Scalar(180,50,255);
} COLOR_RANGES;

struct s_circle
{
    int radius;
    Point center;
};

struct Dice_s
{
    Rect boundRect;
    int number;
    SagradaColor color;
    int col;
    int row;
    Dice_s* leftDice;
    Dice_s* rightDice;
    Dice_s* upperDice;
    Dice_s* lowerDice;

    Dice_s(Rect _boundRect, SagradaColor _color)
    {
        this->boundRect = _boundRect;
        this->color = _color;

        this->number = 0;
        this->col = NULL;
        this->row = NULL;
        this->leftDice = NULL;
        this->rightDice = NULL;
        this->upperDice = NULL;
        this->lowerDice = NULL;
    }
    /*
     * _side = true -> Row 1 (upper border)
     * _side = false -> Row 4 (bottom border)*/
    void SetBorderRow(bool _side)
    {
        _side ? this->row = 1 : this->row = 4;
    }
    /*
     * _side = true -> Col 1 (upper border)
     * _side = false -> Col 5 (bottom border)*/
    void SetBorderCol(bool _side)
    {
        _side ? this->col = 1 : this->col = 5;
    }
};

Mat tMask;

bool sort_by_x(Dice_s dice1, Dice_s dice2) { return dice1.boundRect.tl().x < dice2.boundRect.tl().x; }
bool sort_by_y(Dice_s dice1, Dice_s dice2) { return dice1.boundRect.br().y < dice2.boundRect.br().y; }

class DiceAnalyzer
{
public:
    Mat diceImage;
    Mat hsvImage;
    Mat tmp;
    vector<Dice_s> dices;
    Mat diceBoundImg;
    DiceAnalyzer(Mat _image)
    {
        this->diceImage = _image;
        _image.copyTo(this->tmp);
    }
    void DetectDiceGrid(){
        Mat labImg;
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
        cvtColor(diceImage, labImg, COLOR_BGR2Lab);
        //Set scale values for resized image
        SetScaleValues(diceImage.size().width, diceImage.size().height, scaleWidth, scaleHeight, 512, 512);
        //Set OpenCV Lab values for black
        CalculateLabValues(L1, a1, b1); //lower bound
        CalculateLabValues(L2, a2, b2); //upper bound
        //Create black color mask
        inRange(labImg, Scalar(L1, a1, b1), Scalar(L2, a2, b2), mask);
        //resize mask
        resize(mask, mask, Size(512,512));
        //erode
        erode(mask, mask, kernel, Point(-1,-1), 5);
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
        //check if bound was found
        if(bound.width == 0){
            __android_log_print(ANDROID_LOG_ERROR, "Detect dices", "No bound was found!");
        }
        diceImage(bound).copyTo(diceBoundImg);
    }
    void DetectDices()
    {
        //RGB -> HSV
        cvtColor(diceBoundImg, hsvImage, COLOR_BGR2HSV);
        //Filter each color
        FindDiceByColor(S_RED);
        FindDiceByColor(S_GREEN);
        FindDiceByColor(S_BLUE);
        FindDiceByColor(S_YELLOW);
        FindDiceByColor(S_VIOLET);
        //DEBUG
        __android_log_print(ANDROID_LOG_INFO, "Number of dices", "%d", this->dices.size());
        diceBoundImg.copyTo(tmp);
        for(int i = 0; i < dices.size(); i++){
            rectangle(tmp, dices[i].boundRect.tl(), dices[i].boundRect.br(), Scalar(0,0,255), 10, LINE_AA);
        }
    }
    vector<Dice_s> SortDices(int _rows, int _cols)
    {
        sort(this->dices.begin(), this->dices.end(), sort_by_y);
        vector<Dice_s> outputVector;
        //Create rows
        vector<vector<Dice_s>> dice_rows;
        vector<Dice_s> dice_row;

        int ref_value = this->dices[0].boundRect.y;

        dice_row.push_back(this->dices[0]);
        //rows
        for(int i = 1; i < this->dices.size(); i++){
            Dice_s dice = this->dices[i];
            if(dice.boundRect.y >= ref_value - dice.boundRect.height/2 && dice.boundRect.y <= ref_value + dice.boundRect.height/2){
                dice_row.push_back(dice);
            }
            else if(dice.boundRect.y > ref_value + dice.boundRect.height/2){
                dice_rows.push_back(dice_row);
                dice_row.clear();
                dice_row.push_back(dice);
                ref_value = dice.boundRect.y;
            }
            if(i == this->dices.size() - 1){
                dice_rows.push_back(dice_row);
                dice_row.clear();
            }
        }
        //Rows assertion
        if(dice_rows.size() > _rows){
            __android_log_print(ANDROID_LOG_ERROR, "Number of detected rows exceeded", "rows : %d", _rows);
        }
        else if(dice_rows.size() < _rows){
            //create empty rows
            //TODO: Empty rows
        }
        //Cols
        vector<Dice_s> dice_col;
        vector<vector<Dice_s>> dice_cols;

        sort(this->dices.begin(), this->dices.end(), sort_by_x);
        dice_col.push_back(this->dices[0]);
        ref_value = this->dices[0].boundRect.x;

        for(int i = 1; i < this->dices.size(); i++){
            Dice_s dice = this->dices[i];
            if(dice.boundRect.x >= ref_value - dice.boundRect.width/2 && dice.boundRect.x <= ref_value + dice.boundRect.width/2){
                dice_col.push_back(dice);
            }
            else if(dice.boundRect.x > ref_value + dice.boundRect.width/2){
                dice_cols.push_back(dice_col);
                dice_col.clear();
                dice_col.push_back(dice);
                ref_value = dice.boundRect.x;
            }
            if(i == this->dices.size() -1){
                dice_cols.push_back(dice_col);
                dice_col.clear();
            }
        }
        //Cols assertion
        if(dice_cols.size() > _cols){
            __android_log_print(ANDROID_LOG_ERROR, "Number of detected cols exceeded", "cols: %d", _cols);
        }
        else if(dice_cols.size() < _rows){
            //create empty cols
            //TODO: Empty cols
        }

        return outputVector;
    }
    vector<vector<Dice_s>> AdjustDiceRows(vector<vector<Dice_s>> _diceRows)
    {
        //TODO
        //dice on the biggest (y - value) row
        Dice_s dice = _diceRows[_diceRows.size()-1][0];
        //offset
        int offset = (int)(dice.boundRect.height * 0.25);
        //Control row below
        vector<vector<Dice_s>> vec;
        return vec;
    }
    //Output Mat is for debug
    Mat FindDiceByColor(SagradaColor _color)
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
            colorMask.copyTo(tMask);
        }
        else
        {
            inRange(this->hsvImage, cRange[0], cRange[1], colorMask);
        }

        vector<vector<Point>> contours;
        vector<Vec4i> hierarchy;

        Mat kernel = Mat::ones(Size(3,3), CV_8UC1);
        erode(colorMask,colorMask, kernel, Point(-1,-1), 4);

        findContours(colorMask, contours, hierarchy, RETR_TREE, CHAIN_APPROX_SIMPLE);

        vector<vector<Point>> contours_poly( contours.size());

        for( size_t i = 0; i < contours.size(); i++ )
        {
            double epsilon = 0.01*arcLength(contours[i],true);
            approxPolyDP( contours[i], contours_poly[i], epsilon, true );

            double area = contourArea(contours_poly[i]);
            Rect bound = boundingRect(contours[i]);
            if(hierarchy[i][3] == -1 ){
                int blob_count = 0;
                if(IsNumber(this->hsvImage(bound), blob_count) && blob_count > 0 && blob_count < 7){
                    Dice_s dice(boundingRect(contours_poly[i]), _color);
                    dice.number = blob_count;
                    this->dices.push_back(dice);
                }
            }
        }

        return colorMask;
    }
    vector<Scalar> SelectRange(SagradaColor _color)
    {
        vector<Scalar> outputVec;

        switch (_color)
        {
            case S_WHITE:
                outputVec.push_back(COLOR_RANGES.lowDiceWhite);
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
            default:
                break;
        }
        return outputVec;
    }
    bool IsNumber(Mat _img, int& _number)
    {
        Mat mask;
        int number = 0;

        inRange(_img, COLOR_RANGES.lowDiceWhite, COLOR_RANGES.highDiceWhite, mask);

        vector<vector<Point>> contours;
        vector<Vec4i> hierarchy;

        findContours(mask, contours, hierarchy, RETR_TREE, CHAIN_APPROX_SIMPLE);

        vector<vector<Point>> contours_poly( contours.size());

        int count = 0;

        for( size_t i = 0; i < contours.size(); i++ )
        {
            double epsilon = 0.01*arcLength(contours[i],true);
            approxPolyDP( contours[i], contours_poly[i], epsilon, true );
            double area = contourArea(contours_poly[i]);
            Point2f center;
            float radius;
            minEnclosingCircle(contours_poly[i], center, radius);
            double cArea = radius * radius * 3.14;
            double areaOffset = cArea * 0.25;

            if(area >= cArea - areaOffset && hierarchy[i][3] == -1)
            {
                count++;
            }
        }

        _number = count;

        if(count >= 1 && count <= 6)
        {
            _number = count;
            return true;
        }
        else
        {
            _number = -1;
            return false;
        }
    }
    void SetScaleValues(int _width, int _height, double& scaleWidth, double& scaleHeight, double refWidth, double refHeight)
    {
        scaleWidth = _width / refWidth;
        scaleHeight = _height / refHeight;
        __android_log_print(ANDROID_LOG_INFO, "scale", "%d | %d | %f | %f", _width, _height, scaleWidth, scaleHeight);
    }
    void CalculateLabValues(int& L, int& a, int& b)
    {
        L = L * 255/100;
        a = a + 128;
        b = b + 128;
    }

    void DiceOutput()
    {
        for(int i = 0; i < this->dices.size(); i++)
        {
            __android_log_print(ANDROID_LOG_INFO, "DiceOutput", "%d | %s", this->dices[i].number, PIDName(this->dices[i].color));
        }
    }
    char* PIDName(SagradaColor _pid)
    {
        switch (_pid)
        {
            case S_WHITE :
                return "White";
            case S_BLUE :
                return "Blue";
            case S_GREEN :
                return "Green";
            case S_RED :
                return "Red";
            case S_VIOLET :
                return "Violet";
            case S_YELLOW :
                return "Yellow";
            default:
                return "UNKNOWN";
        }
    }
};