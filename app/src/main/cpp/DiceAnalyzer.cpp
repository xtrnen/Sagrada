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
    Scalar lowYellow = Scalar(15,90,20);//,100,20
    Scalar lowYellowSlot = Scalar(15,90,60);
    Scalar highYellow = Scalar(35,255,255);

    /*Green color*/
    Scalar lowDiceGreen = Scalar(40,100,35);
    Scalar lowGreen = Scalar(35,50,35);//40
    Scalar highGreen = Scalar(80,255,255);//90
    Scalar highDiceGreen = Scalar(85,255,255);//90-255-255

    /*Blue color*/
    Scalar lowDiceBlue = Scalar(85,80,40);//80-50-20
    Scalar lowBlue = Scalar(80,50,20);
    Scalar highBlue = Scalar(130,255,255);//127-255-255

    /*Red color*/
    Scalar lowRedFirstMask = Scalar(0, 135, 50);//(0, 150, 70)
    Scalar highRedFirstMask = Scalar(15, 255, 255);//(10, 255, 255)
    Scalar lowRedSecondMask = Scalar(170, 135, 50);//(171, 150, 70)
    Scalar highRedSecondMask = Scalar(180, 255, 255);//(180, 255, 255)

    /*Violet color*/
    Scalar lowViolet = Scalar(135,100,35);//135-50-20
    Scalar highViolet = Scalar(170,255,255);

    /*White color*/
    Scalar lowWhite = Scalar(0,0,120);
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

    Dice_s(Rect _boundRect, SagradaColor _color)
    {
        this->boundRect = _boundRect;
        this->color = _color;

        this->number = 0;
        this->col = -42;
        this->row = -42;
    }

    string GetColorString(){
        switch (color) {
            case S_RED:
                return string("RED");
            case S_GREEN:
                return string("GREEN");
            case S_BLUE:
                return string("BLUE");
            case S_YELLOW:
                return string("YELLOW");
            case S_VIOLET:
                return string("VIOLET");
            default:
                return string("NONE");
        }
    }
};

Mat tMask;
Mat tp;

bool sort_by_x(Rect slot1, Rect slot2) { return slot1.x < slot2.x; }
bool sort_by_y(Rect slot1, Rect slot2) { return slot1.y < slot2.y; }
class DiceAnalyzer
{
    //TODO: "dice_correct" red dice One blob detection problem
public:
    Mat diceImage;
    Mat hsvImage;
    vector<Dice_s> dices;
    Mat diceBoundImg;
    int leftXBoundry;
    int refWidth;

    DiceAnalyzer(Mat _image)
    {
        this->diceImage = _image;
        diceBoundImg = _image;
        leftXBoundry = 0;
        refWidth = 0;
    }
    void DetectDiceGrid(){
        Mat labImg;
        Mat mask;
        Mat kernel = Mat::ones(Size(3,3), CV_8UC1);
        double scaleWidth;
        double scaleHeight;
        Rect bound = Rect(0,0,0,0);
        int L1 = 0;
        int L2 = 40;
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
            if(tmpBound.area() > 30000 && tmpBound.height > 100){
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
            diceImage.copyTo(diceBoundImg);
        }
        else{
            diceImage(bound).copyTo(diceBoundImg);
        }
        copyMakeBorder(diceBoundImg, diceBoundImg, 5, 5, 5, 5, BORDER_CONSTANT, Scalar(0,0,0));
    }
    vector<Rect> DetectDiceSlots()
    {
        Mat hsv;
        Mat mask;
        Mat maskTmp;
        vector<Rect> slots;
        cvtColor(diceBoundImg, hsv, COLOR_BGR2HSV);

        double scaleWidth;
        double scaleHeight;
        SetScaleValues(diceBoundImg.size().width, diceBoundImg.size().height, scaleWidth, scaleHeight, 512, 512);
        __android_log_print(ANDROID_LOG_INFO, "DetectDiceSlots", "color ranges");
        //red
        inRange(hsv, COLOR_RANGES.lowRedFirstMask, COLOR_RANGES.highRedFirstMask, mask);
        inRange(hsv, COLOR_RANGES.lowRedSecondMask, COLOR_RANGES.highRedSecondMask, maskTmp);
        mask += maskTmp;
        //green
        inRange(hsv, COLOR_RANGES.lowDiceGreen, COLOR_RANGES.highDiceGreen, maskTmp);
        mask += maskTmp;
        //blue
        inRange(hsv, COLOR_RANGES.lowDiceBlue, COLOR_RANGES.highBlue, maskTmp);
        mask += maskTmp;
        //yellow
        inRange(hsv, COLOR_RANGES.lowYellow, COLOR_RANGES.highYellow, maskTmp);
        mask += maskTmp;
        //violet
        inRange(hsv, COLOR_RANGES.lowViolet, COLOR_RANGES.highViolet, maskTmp);
        mask += maskTmp;
        //white
        /*inRange(hsv, COLOR_RANGES.lowDiceWhite, COLOR_RANGES.highDiceWhite, maskTmp);
        mask += maskTmp;*/
        maskTmp.release();

        /*Clear image*/
        GaussianBlur(mask, mask, Size(3, 3), 0);
        //morphologyEx(mask, mask, MORPH_ERODE, getStructuringElement(MORPH_RECT, Size(9, 9), Point(-1, -1)));
        morphologyEx(mask, mask, MORPH_OPEN, getStructuringElement(MORPH_RECT, Size(9, 9), Point(-1, -1)));

        resize(mask, mask, Size(512,512));
        threshold(mask, mask, 50, 255, THRESH_BINARY);
        __android_log_print(ANDROID_LOG_INFO, "DICE_MSG", "Contours");
        vector<vector<Point>> contours;
        findContours(mask, contours, RETR_TREE, CHAIN_APPROX_SIMPLE);

        int bigWidth = 0;

        for(size_t i = 0; i < contours.size(); i++){
            Rect tmpBound = boundingRect(contours[i]);
            //filter contours && find fitting contours
            //TODO: Instead of bound.width use bound.height when image is being rotated correctly
            if(tmpBound.width > 45 && tmpBound.width < 150 && tmpBound.height > 45 && tmpBound.height < 150){
                //resize back
                tmpBound.width =(int) (tmpBound.width * scaleWidth);
                tmpBound.height =(int) (tmpBound.height * scaleHeight);
                tmpBound.x =(int) (tmpBound.x * scaleWidth);
                tmpBound.y =(int) (tmpBound.y * scaleHeight);
                if(!SlotExists(slots, tmpBound)){
                    //store slots
                    rectangle(diceBoundImg, tmpBound, Scalar(255,255,255), 2);
                    slots.push_back(tmpBound);
                    if(tmpBound.width > bigWidth){
                        bigWidth = tmpBound.width;
                    }
                }
            }
        }

        refWidth = bigWidth;

        return slots;
    }
    void DetectDices()
    {
        cvtColor(diceBoundImg, hsvImage, COLOR_BGR2HSV);
        //Get slots
        __android_log_print(ANDROID_LOG_INFO, "DICE_MSG", "Detect Dice Slots");
        vector<Rect> slotBounds = DetectDiceSlots();
        //sort slots by rows
        __android_log_print(ANDROID_LOG_INFO, "DICE_MSG", "Sort slots");
        //set boundry for left side (starting side of sorting)
        leftXBoundry = getXLeftBound();

        vector<vector<Rect>> sortedSlots = SortSlots(4, slotBounds);
        __android_log_print(ANDROID_LOG_INFO, "DICE_MSG", "Fill output");
        //for each row -> from left to right check slot, assign Dice value, color, row and col
        for(int row = 0; row < sortedSlots.size(); row++){
            __android_log_print(ANDROID_LOG_INFO, "DICE_MSG", "ROW - %d", row);
            //Check each slot
            for(int col = 0; col < sortedSlots[row].size(); col++){
                Dice_s dice = FindDice(sortedSlots[row][col]);
                if(dice.color != S_NONE)
                {
                    dice.row = row;
                    dice.col = col;
                    dices.push_back(dice);
                    rectangle(diceBoundImg, dice.boundRect.tl(), dice.boundRect.br(), Scalar(0,0,255), 5, LINE_AA);
                }
            }
        }
    }
    vector<vector<Rect>> SortSlots(int _rows, vector<Rect> _slots)
    {
        sort(_slots.begin(), _slots.end(), sort_by_y);
        //Create rows
        vector<vector<Rect>> slotRows;
        vector<Rect> slotRow;
        //slotRow = AddBlankSlots(slotRow, _slots[0]);
        int ref_value = _slots[0].y;
        slotRow.push_back(_slots[0]);
        //rows
        for(int i = 1; i < _slots.size(); i++){
            Rect slot = _slots[i];
            if(slot.y >= ref_value - slot.height/2 && slot.y <= ref_value + slot.height/2){
                slotRow.push_back(slot);
            }
            else if(slot.y > ref_value + slot.height/2){
                sort(slotRow.begin(), slotRow.end(), sort_by_x);
                slotRow = AddBlankSlots(slotRow);
                slotRows.push_back(slotRow);
                slotRow.clear();
                slotRow.push_back(slot);
                ref_value = slot.y;
            }
            if(i == _slots.size() - 1){
                sort(slotRow.begin(), slotRow.end(), sort_by_x);
                slotRow = AddBlankSlots(slotRow);
                slotRows.push_back(slotRow);
                slotRow.clear();
            }
        }
        //Rows assertion
        if(slotRows.size() > _rows){
            __android_log_print(ANDROID_LOG_ERROR, "Number of detected rows exceeded", "rows : %d", _rows);
        }

        return slotRows;
    }
    vector<Rect> AddBlankSlots(vector<Rect> rowSlots)
    {
        int index = 0;
        vector<Rect> outputVec;
        for(int i = 0; i < rowSlots.size(); i++){
            Rect slot = rowSlots[i];
            int possibleSlots = 0;
            //First slot in line
            if(slot.x - slot.width < leftXBoundry){
                outputVec.push_back(slot);
                continue;
            }
            //No slots given
            if(rowSlots.size() == 0){
                possibleSlots = slot.x / refWidth;
                if(possibleSlots != 0){
                    //add given number of slots
                    for(int i = possibleSlots; i >= 1; i--){
                        Rect newRect = Rect(slot.x - (slot.width * (i + 1)) - 1, slot.y, slot.width, slot.height);
                        if(newRect.x > leftXBoundry){
                            outputVec.push_back(newRect);
                        }
                    }
                }
                outputVec.push_back(slot);
                continue;
            }
            //Fill previous space
            Rect lastRect;
            if(outputVec.empty()){
                lastRect = Rect(leftXBoundry,slot.y,0,0);
                possibleSlots = slot.x / refWidth;
            }
            else{
                lastRect = outputVec[outputVec.size() - 1];
                possibleSlots = (slot.x - (lastRect.x + lastRect.width)) / refWidth;
            }
            if(possibleSlots > 0){
                //add given number of slots
                for(int a = possibleSlots; a >= 1; a--){
                    int width = slot.width * a;
                    Rect newRect = Rect(slot.x - width - 10, slot.y, slot.width, slot.height);
                    if(newRect.x > lastRect.x + lastRect.width){
                        outputVec.push_back(newRect);
                    }
                }
            }
            outputVec.push_back(slot);
            continue;
        }

        if(outputVec.size() == 0){
            outputVec = rowSlots;
        }
        __android_log_print(ANDROID_LOG_INFO, "DICE_MSG", "%d count outputVec", outputVec.size());
        return outputVec;
    }
    Dice_s FindDice(Rect slot)
    {
        //Check Red
        Dice_s redDice = FindDiceByColor(S_RED, slot);
        if(redDice.boundRect.width != 0)
            return redDice;
        //Check Blue
        Dice_s blueDice = FindDiceByColor(S_BLUE, slot);
        if(blueDice.boundRect.width != 0)
            return blueDice;
        //Check Green
        Dice_s greenDice = FindDiceByColor(S_GREEN, slot);
        if(greenDice.boundRect.width != 0)
            return greenDice;
        //Check Yellow
        Dice_s yellowDice = FindDiceByColor(S_YELLOW, slot);
        if(yellowDice.boundRect.width != 0)
            return yellowDice;
        //Check Violet
        Dice_s violetDice = FindDiceByColor(S_VIOLET, slot);
        if(violetDice.boundRect.width != 0)
            return violetDice;
        Dice_s noDice(Rect(0,0,0,0), S_NONE);
        return noDice;
    }
    //Output Mat is for debug
    Dice_s FindDiceByColor(SagradaColor _color, Rect slot)
    {
        Mat colorMask;
        vector<Scalar> cRange = SelectRange(_color);
        //Red color complex mask
        if(_color == S_RED)
        {
            Mat tmp;
            inRange(this->hsvImage(slot), cRange[0], cRange[2], tmp);
            inRange(this->hsvImage(slot), cRange[1], cRange[3], colorMask);
            colorMask += tmp;
            colorMask.copyTo(tMask);
        }
        else
        {
            inRange(this->hsvImage(slot), cRange[0], cRange[1], colorMask);
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
            if(area >= slot.area() * 0.25){
                int blob_count = 0;
                if(IsNumber(this->hsvImage(slot), blob_count) && blob_count > 0 && blob_count < 7){
                    Dice_s dice(slot, _color);
                    dice.number = blob_count;
                    return dice;
                }
            }
        }
        Dice_s dice(Rect(0,0,0,0), _color);
        return dice;
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
                outputVec.push_back(COLOR_RANGES.lowDiceGreen);
                outputVec.push_back(COLOR_RANGES.highDiceGreen);
            case S_BLUE:
                outputVec.push_back(COLOR_RANGES.lowDiceBlue);
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

            if(area >= cArea - areaOffset && hierarchy[i][3] == -1 && area > 100)
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
    static void SetScaleValues(int _width, int _height, double& scaleWidth, double& scaleHeight, double refWidth, double refHeight)
    {
        scaleWidth = _width / refWidth;
        scaleHeight = _height / refHeight;
        //__android_log_print(ANDROID_LOG_INFO, "scale", "%d | %d | %f | %f", _width, _height, scaleWidth, scaleHeight);
    }
    static void CalculateLabValues(int& L, int& a, int& b)
    {
        L = L * 255/100;
        a = a + 128;
        b = b + 128;
    }
    bool SlotExists(vector<Rect> slots, Rect newSlot)
    {
        for(Rect rect : slots){
            int slotCX = rect.x + rect.width / 2;
            int slotCY = rect.y + rect.height / 2;
            int lowerRangeX = newSlot.x - newSlot.width/2;
            int upperRangeX = newSlot.x + newSlot.width + newSlot.width/2;
            int lowerRangeY = newSlot.y - newSlot.height/2;
            int upperRangeY = newSlot.y + newSlot.height + newSlot.height/2;
            if(slotCX >= lowerRangeX && slotCX <= upperRangeX &&
               slotCY >= lowerRangeY && slotCY <= upperRangeY){
                return true;
            }
        }
        return false;
    }
    int getXLeftBound(){
        Mat mask;
        int area = 0;
        int valueX = 0;

        inRange(hsvImage, Scalar(0,0,0), Scalar(180,100,125), mask);
        resize(mask, mask, Size(720,720));

        double scaleWidth;
        double scaleHeight;
        SetScaleValues(diceBoundImg.size().width, diceBoundImg.size().height, scaleWidth, scaleHeight, 720, 720);

        morphologyEx(mask, mask, MORPH_OPEN, getStructuringElement(MORPH_RECT, Size(9, 9), Point(-1, -1)));

        vector<vector<Point>> recCont;
        findContours(mask, recCont, RETR_TREE, CHAIN_APPROX_SIMPLE);
        for (size_t x = 0; x < recCont.size(); x++) {
            Rect tmpR = boundingRect(recCont[x]);
            if (area < tmpR.area()) {
                area = tmpR.area();
                valueX = (int)(tmpR.tl().x * scaleWidth);
            }
        }

        return valueX;
    }
};