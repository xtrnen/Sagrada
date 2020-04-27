//
// Created by jtrne on 23.12.2019.
//
#include "opencv2/imgproc/imgproc.hpp"
#include "opencv2/highgui/highgui.hpp"
#include "opencv2/highgui.hpp"
#include <iostream>
#include <vector>
#include <cmath>

using namespace std;
using namespace cv;

enum PatternID {
    PATID_ONE = 1,
    PATID_TWO = 2,
    PATID_THREE = 3,
    PATID_FOUR = 4,
    PATID_FIVE = 5,
    PATID_SIX = 6,
    PATID_RED = 7,
    PATID_BLUE = 8,
    PATID_GREEN = 9,
    PATID_YELLOW = 10,
    PATID_VIOLET = 11,
    PATID_WHITE = 12,
    PATID_NONE = -42
};

struct Slot {
    int row;
    int col;
    PatternID info;

    Slot(int _row, int _col, PatternID _info)
    {
        row = _row;
        col = _col;
        info = _info;
    }
    string RetStr()
    {
        switch (info){
            case PATID_ONE:
                return "ONE";
            case PATID_TWO:
                return "TWO";
            case PATID_THREE:
                return "THREE";
            case PATID_FOUR:
                return "FOUR";
            case PATID_FIVE:
                return "FIVE";
            case PATID_SIX:
                return "SIX";
            case PATID_RED:
                return "RED";
            case PATID_BLUE:
                return "BLUE";
            case PATID_GREEN:
                return "GREEN";
            case PATID_YELLOW:
                return "YELLOW";
            case PATID_VIOLET:
                return "VIOLET";
            case PATID_WHITE:
                return "WHITE";
            default:
                return "NONE";
        }
    }
};

bool compareRectsOnRow (const Rect& l, const Rect& r) {
    return l.br().x > r.br().x;
}

class PatternAnalyzer
{
public:
    Mat patternImg;
    vector<Slot> slots;
    vector<Rect> matrix;
    Point controlPoint;
    int refOffset;
    int refHeight;
    int refWidth;
    Mat tmp;

    PatternAnalyzer(Mat _patternImg)
    {
        this->patternImg = _patternImg;
    }

    vector<Mat> CreatePatternGrid()
    {
        //PrepGrayImg
        __android_log_print(ANDROID_LOG_INFO, "MSG", "Prep Img");
        Mat gray = PrepGrayImg();

        //FindControlPoint
        __android_log_print(ANDROID_LOG_INFO, "MSG", "Control Point");
        this->patternImg.copyTo(this->tmp);
        Point controlPoint = DetectControlPoint(gray);
        this->controlPoint = controlPoint;
        __android_log_print(ANDROID_LOG_INFO, "Control Point", "%d|%d", controlPoint.x, controlPoint.y);

        //Crop image rectangle
        int oft = 50;
        while(controlPoint.x + oft > this->patternImg.cols){
            oft -=10;
        }

        //Find control Point
        controlPoint.x += oft;
        Rect rect = Rect(Point(0,0), controlPoint);
        __android_log_print(ANDROID_LOG_INFO, "MSG", "Rects");
        //Find color slots & detect contours
        vector<Rect> boxes;
        boxes = ApplyColorMasks(this->patternImg(rect));

        this->refOffset = DetectRectOffset(boxes);
        __android_log_print(ANDROID_LOG_INFO, "MSG", "Split");
        //SplitImg
        vector<Rect> matrixRect = RectPatternGrid(4, 5, controlPoint, boxes);
        __android_log_print(ANDROID_LOG_INFO, "MSG", "Rect");

        for(Rect rect : matrixRect)
        {
            if(!rect.empty())
                rectangle(this->tmp, rect, Scalar(0,0,255), 3);
        }

        matrix = matrixRect;

        return SplitImageToPattern(4, 5, matrixRect, this->patternImg);
    }

    void GetCardPattern(vector<Mat> _splittedImg)
    {
        PatternID pID;
        int col = 0;
        int row = 0;
        for (int i = 0; i < _splittedImg.size(); i++) {
            Mat img = _splittedImg[i];
            if(IsColorPattern(img, pID)){
                slots.push_back(Slot(row, col, pID));
            }
            else if(IsDicePattern(img, 1, 6, pID)){
                slots.push_back(Slot(row, col, pID));
            }
            else{
                slots.push_back(Slot(row, col, pID));
            }
            col++;
            if(col == 5){
                row++;
                col = 0;
            }
            Point *point = new Point(matrix[i].tl().x + matrix[i].width / 2, matrix[i].br().y);
            putText(tmp, patternIdType(pID), Point(point->x, point->y), FONT_HERSHEY_PLAIN, 5, Scalar(0,0,255), LINE_AA);
        }
    }

private:
    Mat PrepGrayImg()
    {
        Mat prepImg;
        this->patternImg.copyTo(prepImg);
        //resize(prepImg, prepImg, Size(720,720));

        cvtColor(prepImg, prepImg, COLOR_BGR2GRAY);
        equalizeHist(prepImg, prepImg);
        GaussianBlur(prepImg, prepImg, Size(5,5), 2, 2, BORDER_CONSTANT);
        //erode(prepImg, prepImg, getStructuringElement(MORPH_RECT, Size(5,5) , Point(0,0)));

        return prepImg;
    }

    Point DetectControlPoint(Mat _grayImg)
    {
        Mat grayImg;
        //Mat tmp;
        threshold(_grayImg, grayImg, 100, 255, THRESH_BINARY);
        Canny(grayImg, grayImg, 100, 180);
        //grayImg.copyTo(tmp);

        vector<vector<Point>> contours;
        vector<Vec4i> hierarchy;

        findContours(grayImg, contours, hierarchy, RETR_TREE, CHAIN_APPROX_SIMPLE);

        vector<Point> poly;
        vector<Point> centers;

        for( size_t i = 0; i < contours.size(); i++ )
        {
            double epsilon = 0.01*arcLength(contours[i],true);
            approxPolyDP( contours[i], poly, epsilon, true );

            double area = contourArea(poly);
            Point2f center;
            float radius;
            minEnclosingCircle(poly, center, radius);
            double cArea = radius * radius * 3.14;
            double areaOffset = area * 0.5;

            if(area > 50.0 && cArea >= area && cArea <= area + areaOffset){
                __android_log_print(ANDROID_LOG_INFO, "DDD", "%f || %f", area, cArea);
                centers.push_back(Point((int)center.x, (int)center.y));
            }
        }

        if(centers.empty()){
            __android_log_print(ANDROID_LOG_ERROR, "CPoint", "Centers not found");
        }

        Point bottom = Point(0,0);

        for(Point cent : centers)
        {
            if(cent.y > bottom.y)
                bottom = cent;
        }
        for(Point cent : centers)
        {
            if(cent.y >= bottom.y - 30 && cent.x > bottom.x)
                bottom = cent;
        }

        circle(this->tmp, bottom, 1, Scalar(0,255,0), 5);

        return bottom;
    }

    vector<Rect> ApplyColorMasks(Mat _img)
    {
        vector<Rect> colorRects;
        Mat mask;
        //Get hsv model for img
        Mat hsv;
        cvtColor(_img, hsv, COLOR_BGR2HSV);

        /*Get rects for each color*/
        //Red
        mask = FindHsvColor(hsv, S_RED);
        //Green
        mask += FindHsvColor(hsv, S_GREEN);
        //Blue
        mask += FindHsvColor(hsv, S_BLUE);
        //Yellow
        mask += FindHsvColor(hsv, S_YELLOW);
        //Violet
        mask += FindHsvColor(hsv, S_VIOLET);
        mask += FindHsvColor(hsv, S_WHITE);

        vector<Rect> boxes;
        //mask.copyTo(this->tmp);
        FindContours(mask, boxes);

        return boxes;
    }

    Mat FindContours(Mat input, vector<Rect>& rectBoxes)
    {
        Mat drawing = Mat::zeros( input.size(), CV_8UC3);
        vector<vector<Point>> contours;
        vector<Vec4i> hierarchy;

        findContours(input, contours, hierarchy, RETR_TREE, CHAIN_APPROX_SIMPLE);

        vector<vector<Point>> contours_poly( contours.size());
        vector<Rect> boundRect( contours.size());
        vector<Point2f>centers( contours.size());

        Scalar color1 = Scalar (0,255,0);
        vector<Rect> boxes;

        int sum = 0;
        int count = 0;

        for( size_t i = 0; i < contours.size(); i++ )
        {
            double epsilon = 0.01*arcLength(contours[i],true);
            approxPolyDP( contours[i], contours_poly[i], epsilon, true );
            if(contours_poly[i].size() == 4)
            {
                RotatedRect roRect = minAreaRect(contours_poly[i]);
                int area = (int)contourArea(contours_poly[i]);
                int roArea = roRect.boundingRect().area();
                if(roRect.boundingRect().br().y < this->controlPoint.y && area > 50 /*&& hierarchy[i][2] != -1*/ && roArea >= area && roArea < area*2)
                {
                    sum += area;
                    count++;
                    Rect rect = boundingRect( contours_poly[i] );
                    boxes.push_back(rect);
                }
            }
        }

        if(count != 0){
            sum = sum / count;
        }

        int sumWidth = 0;
        int sumHeight = 0;
        count = 0;
        for(int i = 0; i < boxes.size(); i++)
        {
            if(boxes[i].area() > (sum - sum*0.25))
            {
                rectBoxes.push_back(boxes[i]);
                sumWidth += boxes[i].width;
                sumHeight += boxes[i].height;
                count++;
                rectangle(drawing, boxes[i], Scalar(0,255,0), 5);
            }
        }
        //__android_log_print(ANDROID_LOG_INFO, "DD", "%d", count);
        if(count != 0)
        {
            this->refHeight = sumHeight / count;
            this->refWidth = sumWidth / count;
        }

        boxes.clear();

        return drawing;
    }   //100

    vector<Rect> RectPatternGrid(int rows, int cols, Point controlPoint, vector<Rect> contourBoxes)
    {
        vector<vector<Rect>> rowsWithBoxes; //output of FOR cycle with rectangles found on specified row
        vector<Rect> rectsOnRow(5); //temporary storage for know rectangles on row
        vector<Rect> refRects(4);  //temporary storage for referenceRect on each row
        vector<Rect> blankRects;    //storage of created rects on positions where should be found rect contour, but was not found
        vector<Rect> tmpVector;     //For cycle vector of all rectangles on line, which is later sorted and pushed to output vector
        vector<Rect> retVector;

        Rect closestRect = GetClosestRect(contourBoxes, controlPoint);

        //Nejniže -> Control Rect
        refRects = CompleteRefRects(closestRect, controlPoint);

        for(int i = 0; i < 4 ; i++){
            if(i >= refRects.size()){
                closestRect = refRects[i-1];
                Point tmpCP = Point(closestRect.br().x, closestRect.tl().y);
                refRects.push_back(GetClosestRect(contourBoxes, tmpCP));
            }
            tmpVector.push_back(refRects[i]);
            rectsOnRow = FindRectsOnRow(contourBoxes, refRects[i]);

            for(Rect rect : rectsOnRow){
                tmpVector.push_back(rect);
            }

            if(!rectsOnRow.empty()){
                sort(rectsOnRow.begin(), rectsOnRow.end(), compareRectsOnRow);
            }

            blankRects = FindBlankRects(rectsOnRow, refRects[i], controlPoint);

            for(Rect rect : blankRects){
                tmpVector.push_back(rect);
            }

            sort(tmpVector.begin(), tmpVector.end(), sort_by_x);

            retVector.insert(retVector.begin(), tmpVector.begin(), tmpVector.end());

            tmpVector.clear();

        }

        return retVector;
    }

    vector<Rect> CompleteRefRects(Rect controlRect, Point controlPoint)
    {
        int offset = this->refOffset;
        vector<Rect> outputVector;
        Point br;
        Point tl;
        Rect refRect = controlRect;
        Rect tmpRect;

        while(refRect.br().y < controlPoint.y - refRect.width - offset ) {
            br = Point(refRect.br().x, refRect.br().y + refRect.width + offset);
            tl = Point(refRect.tl().x, refRect.br().y + offset);
            tmpRect = Rect(tl, br);
            outputVector.push_back(tmpRect);
            refRect = tmpRect;
        }

        outputVector.push_back(controlRect);

        return outputVector;
    }

    vector<Rect> FindBlankRects(vector<Rect> rectsOnRow, Rect refRect, Point refPoint)
    {
        Rect rightRect;
        Rect nextRect;
        int numOfRectOnRow = 1;
        int index = 0;
        bool reverse = false;

        vector<Rect> blankRects;

        vector<Rect> leftVector;
        vector<Rect> rightVector;

        if(!rectsOnRow.empty()){
            numOfRectOnRow += rectsOnRow.size();
        }

        if(IsDefaultRect(refPoint, refRect.br())){
            // <------
            //__android_log_print(ANDROID_LOG_INFO, "BUUG", "GOING LEFT");
            rightRect = refRect;
            while(numOfRectOnRow < 5){
                if(!rectsOnRow.empty()){
                    nextRect = rectsOnRow[index];
                    if(IsNextRectNeighbor(rightRect, nextRect, false)){
                        rightRect = nextRect;
                        index++;
                    }
                    else{
                        Rect blank = CreateBlankRect(rightRect, false);
                        blankRects.push_back(blank);
                        rightRect = blank;
                        numOfRectOnRow++;
                    }
                }
                else{
                    Rect blank = CreateBlankRect(rightRect, false);
                    blankRects.push_back(blank);
                    rightRect = blank;
                    numOfRectOnRow++;
                }
            }
        }
        else{
            // <----- ----->
            //__android_log_print(ANDROID_LOG_INFO, "BUUG", "GOING RIGHT");
            SplitRowByRefRect(leftVector, rightVector, rectsOnRow, refRect.br());
            rightRect = refRect;

            while(numOfRectOnRow < 5){
                // ----->
                if(!reverse){
                    if(rightVector.empty()){
                        Rect blank = CreateBlankRect(rightRect, true);
                        if(blank.br().x > refPoint.x + blank.width/2){
                            reverse = true;
                            index = 0;
                            rightRect = refRect;
                            continue;
                        }
                        blankRects.push_back(blank);
                        rightRect = blank;
                        numOfRectOnRow++;
                    }
                    else{
                        nextRect = rightVector[rightVector.size() - index];
                        if(IsNextRectNeighbor(rightRect, nextRect, false)){
                            rightRect = nextRect;
                        }
                        else{
                            Rect blank = CreateBlankRect(rightRect, true);
                            blankRects.push_back(blank);
                            rightRect = blank;
                            numOfRectOnRow++;
                        }
                        index++;
                    }
                }
                    // <-----
                else{
                    if(leftVector.empty()){
                        Rect blank = CreateBlankRect(rightRect, false);
                        if(blank.tl().x <= 0){
                            __android_log_print(ANDROID_LOG_INFO, "LEFT_BLANK", "NEGATIVE VALUE OF RECT");
                            break;
                        }
                        blankRects.push_back(blank);
                        rightRect = blank;
                        numOfRectOnRow++;
                    }
                    else{
                        nextRect = leftVector[index];
                        if(IsNextRectNeighbor(rightRect, nextRect, false)){
                            rightRect = nextRect;
                            index++;
                        }
                        else{
                            Rect blank = CreateBlankRect(rightRect, false);
                            blankRects.push_back(blank);
                            rightRect = blank;
                            numOfRectOnRow++;
                        }
                    }
                }
            }
        }

        return blankRects;
    }

    Rect CreateBlankRect(Rect prevRect, bool reverseDirection)
    {
        int offset = this->refOffset;
        Point br = Point(0,0);
        Point tl = Point(0,0);

        if(reverseDirection){
            br.x = prevRect.br().x + prevRect.width + offset;
            br.y = prevRect.br().y;
            tl.x = prevRect.tl().x + prevRect.width + offset;
            tl.y = prevRect.tl().y;
        }
        else{
            br.x = prevRect.br().x - prevRect.width - offset;
            br.y = prevRect.br().y;
            tl.x = prevRect.tl().x - prevRect.width - offset;
            tl.y = prevRect.tl().y;
        }

        if(tl.x < 0 || br.x > patternImg.cols ){
            tl = prevRect.tl();
            br = prevRect.br();
        }

        return Rect(tl, br);
    }

    Rect GetClosestRect(vector<Rect> polyRects, Point controlPoint)
    {
        Rect tmpRect = Rect(Point(0,0),Point(0,0));

        int offset = this->refOffset;

        for(Rect rect : polyRects){
            if(rect.br().y > tmpRect.br().y + tmpRect.height && rect.br().y < controlPoint.y){
                tmpRect = rect;
            }
            else if(rect.br().y >= tmpRect.br().y - offset && rect.br().y < controlPoint.y && rect.br().x > tmpRect.br().x){
                tmpRect = rect;
            }
        }

        return tmpRect;
    }

    bool IsDefaultRect(Point controlPoint, Point br)
    {
        int approxWidthL = 50;
        int approxWidthR = 150;

        return (br.x >= (controlPoint.x - approxWidthL) && br.x <= (controlPoint.x + approxWidthR));
    }

    vector<Rect> FindRectsOnRow(vector<Rect> polyRects, Rect refRect)
    {
        int widthOffset = 100;
        int heightOffset = 50;
        Point refPoint = refRect.br();
        int refHeight = refRect.height - heightOffset;
        vector<Rect> rectsOnRow(0);

        for(Rect rect : polyRects){
            if(rect.br().y >= refPoint.y - heightOffset && rect.br().y <= refPoint.y + heightOffset){
                if(rect != refRect){
                    rectsOnRow.push_back(rect);
                }
            }
        }

        return rectsOnRow;
    }

    void SplitRowByRefRect(vector<Rect>& leftVector, vector<Rect>& rightVector, vector<Rect> inputVector, Point refPoint)
    {
        for(Rect rect : inputVector){
            if(rect.br().x < refPoint.x){
                //left
                leftVector.push_back(rect);
            }
            else{
                //right
                rightVector.push_back(rect);
            }
        }
    }

    bool IsNextRectNeighbor(Rect rightRect, Rect nextRect, bool reverseDirection)
    {
        int maxWidthRange = 0;
        int minWidthRange = 0;

        if(reverseDirection){   //----->
            minWidthRange = rightRect.br().x;
            maxWidthRange = rightRect.br().x + (rightRect.width*2);
            return nextRect.br().x >= minWidthRange && nextRect.br().x <= maxWidthRange;
        }
        else{   //<-----
            minWidthRange = rightRect.br().x - (rightRect.width*2);
            maxWidthRange = rightRect.br().x - rightRect.width + 50; //50 debug value !!!!!! TODO: rectangels covering themselves, repair it!
            return nextRect.br().x >= minWidthRange && nextRect.br().x <= maxWidthRange;
        }

    }

    vector<Mat> SplitImageToPattern(int rows, int cols, vector<Rect> rectMatrix, Mat image)
    {
        int cellsNum = rows*cols;
        vector<Mat> outputMatrix;

        for(int i = 0; i < cellsNum; i++){
            if(rectMatrix[i].empty()){
                return outputMatrix;
            }
            outputMatrix.push_back(image(rectMatrix[i]));
        }
        return outputMatrix;
    }

    bool IsColorRect(Mat subject, SagradaColor color)
    {
        Mat testImg = FindHsvColor(subject, color);
        vector<vector<Point>> contours;
        vector<Vec4i> hierarchy;

        findContours(testImg, contours, hierarchy, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);

        vector<vector<Point>> contours_poly( contours.size());
        vector<Rect> boundRect( contours.size());
        vector<Point2f>centers( contours.size());

        for( size_t i = 0; i < contours.size(); i++ )
        {
            double epsilon = 0.01*arcLength(contours[i],true);
            approxPolyDP( contours[i], contours_poly[i], epsilon, true );

            if(contours_poly[i].size() == 4){
                boundRect[i] = boundingRect( contours_poly[i] );

                if(boundRect[i].area() >= (subject.rows*subject.cols)/2){
                    return true;
                }
            }
        }

        return false;
    }

    PatternID CheckColor(Mat subject)
    {
        if(IsColorRect(subject, S_WHITE)){
            //__android_log_print(ANDROID_LOG_INFO, "COLOR INFO:", "IS_WHITE");
            return PatternID(PATID_WHITE);
        }
        else if(IsColorRect(subject, S_BLUE)){
            //__android_log_print(ANDROID_LOG_INFO, "COLOR INFO:", "IS_BLUE");
            return PatternID(PATID_BLUE);
        }
        else if(IsColorRect(subject, S_GREEN)){
            //__android_log_print(ANDROID_LOG_INFO, "COLOR INFO:", "IS_GREEN");
            return PatternID(PATID_GREEN);
        }
        else if(IsColorRect(subject, S_RED)){
            //__android_log_print(ANDROID_LOG_INFO, "COLOR INFO:", "IS_RED");
            return PatternID(PATID_RED);
        }
        else if(IsColorRect(subject, S_VIOLET)){
            //__android_log_print(ANDROID_LOG_INFO, "COLOR INFO:", "IS_VIOLET");
            return PatternID(PATID_VIOLET);
        }
        else if(IsColorRect(subject, S_YELLOW)){
            //__android_log_print(ANDROID_LOG_INFO, "COLOR INFO:", "IS_YELLOW");
            return PatternID(PATID_YELLOW);
        }
        else {
            //__android_log_print(ANDROID_LOG_INFO, "COLOR INFO:", "ERROR");
            //TODO: ERROR HANDLE
            return PatternID(PATID_NONE);
        }
    }

    bool IsColorPattern(Mat subject, PatternID& pID)
    {
        Mat img;

        cvtColor(subject, img, COLOR_BGR2HSV);

        switch(CheckColor(img)){
            case PATID_WHITE:
                pID = PATID_WHITE;
                return true;
            case PATID_BLUE:
                pID = PATID_BLUE;
                return true;
            case PATID_GREEN:
                pID = PATID_GREEN;
                return true;
            case PATID_RED:
                pID = PATID_RED;
                return true;
            case PATID_VIOLET:
                pID = PATID_VIOLET;
                return true;
            case PATID_YELLOW:
                pID = PATID_YELLOW;
                return true;
            default:
                pID = PATID_NONE;
                return false;
        }
    }

    bool IsDicePattern(Mat subject, int lowNum, int highNum, PatternID& pID)
    {
        Mat img;
        cvtColor(subject, img, COLOR_BGR2GRAY);

        Mat otsuThreshImg;
        double otsuThresh = threshold(img, otsuThreshImg, 0, 255, THRESH_BINARY + THRESH_OTSU);

        PatternID tmpPID;
        __android_log_print(ANDROID_LOG_INFO, "FUK", "WHITE");
        if(DetectWhiteBlobNumber(img, tmpPID)){
            pID = tmpPID;
        }
        else
        {
            cvtColor(subject, img, COLOR_BGR2HSV);
            pID = DetectLowerNumber(img);
        }
        return pID != PATID_NONE;
    }

    bool DetectWhiteBlobNumber(Mat _img, PatternID& _pID)
    {
        Mat img;
        _img.copyTo(img);
        bool ind = false;
        //cvtColor(img,img, COLOR_BGR2GRAY);
        //cvtC  olor(_img, img, COLOR_BGR2HSV);

        /*vector<Mat> splits;
        split(_img, splits);
        splits[2].copyTo(img);*/

        GaussianBlur(img, img, Size(3,3), 0);

        threshold(img, img, 150, 255, THRESH_BINARY);//128

        equalizeHist(img, img);

        morphologyEx(img, img, MORPH_OPEN, getStructuringElement(MORPH_RECT, Size(3, 3), Point(-1, -1)));//9-9

        /*Mat kernel = Mat::ones(Size(3,3), CV_8UC1);
        erode(img, img, kernel, Point(-1,-1), 1);*/
        //DETECT 6,5,4
            vector<vector<Point>> contours;
            vector<Vec4i> hierarchy;

            findContours(img, contours, hierarchy, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);

            vector<vector<Point>> contours_poly( contours.size());
            vector<Rect> boundRect( contours.size());
            vector<Point2f>centers( contours.size());

            Scalar color = Scalar( 0,0,0 );
            Scalar color1 = Scalar (255,255,255);
            int counter = 0;
            int sumArea = 0;
            vector<double> circles;
            for( size_t i = 0; i < contours.size(); i++ )
            {
                double epsilon = 0.01*arcLength(contours[i],true);
                approxPolyDP( contours[i], contours_poly[i], epsilon, true );

                int area = (int)contourArea(contours_poly[i]);//double
                Point2f center;
                float radius;
                minEnclosingCircle(contours_poly[i], center, radius);
                int cArea = (int)(radius * radius * 3.14);//double

                if(/*cArea > 50.0 &&*/ cArea < area + area*0.5 && hierarchy[i][3] == -1)//0,3
                {
                    counter++;
                }
            }

        if(counter != 0){
        }
        if(counter == 6 || counter == 5 || counter == 4){
            _pID = PatternID(counter);
            return true;
        }
        else{
            _pID = PATID_NONE;
            return false;
        }
    }

    PatternID DetectLowerNumber(Mat _img)
    {
        Mat kernel = Mat::ones(Size(3,3), CV_8UC1);
        Mat img;
        Mat mask;
        vector<Mat> splits;
        split(_img, splits);
        splits[2].copyTo(img);
        splits.clear();
        vector<s_circle> circles;

        mask = LowerNumMask(img, false, 50);

        int number = DetectLowerNumberContour(mask);
        if(number == 3 || number == 2){
            return PatternID(number);
        }
        else{
            mask = LowerNumMask(img, true, 50);
            number = DetectLowerNumberContour(mask);
            if(number == 1 || number == 2 || number == 3){
                return PatternID(number);
            }
            else{
                mask = LowerNumMask(img, false, 40);
                number = DetectLowerNumberContour(mask);
                return PatternID(number);
            }

        }
    }

    int DetectLowerNumberContour(Mat _img)
    {
        int c = 0;
        Point split = Point(_img.cols/2, _img.rows/2);

        vector<s_circle> circles;
        vector<vector<Point>> contours;
        findContours(_img, contours, RETR_TREE, CHAIN_APPROX_SIMPLE);
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
            int offsetArea = (int)(circleArea* 0.3);
            if(area > 50 && area >= circleArea - offsetArea && area <= circleArea + offsetArea){
                circles.push_back(s_circle{(int)radius, Point((int)center.x, (int)center.y)});
                c++;
            }
        }

        return ControlBlobPosition(circles, split);
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

    Mat FindHsvColor(Mat inputImage, SagradaColor color)
    {
        Mat output;
        Mat lowerMask;
        Mat upperMask;

        switch (color){
            case S_GREEN:
                inRange(inputImage, COLOR_RANGES.lowGreen , COLOR_RANGES.highGreen , output); //Green color
                break;
            case S_BLUE:
                inRange(inputImage, COLOR_RANGES.lowBlue , COLOR_RANGES.highBlue , output); //Blue color
                break;
            case S_RED:
                inRange(inputImage, COLOR_RANGES.lowRedFirstMask , COLOR_RANGES.highRedFirstMask , lowerMask);   //Red color
                inRange(inputImage, COLOR_RANGES.lowRedSecondMask, COLOR_RANGES.highRedSecondMask, upperMask);
                output = lowerMask + upperMask;
                break;
            case S_YELLOW:
                inRange(inputImage, COLOR_RANGES.lowYellow , COLOR_RANGES.highYellow , output);   //Yellow color
                break;
            case S_VIOLET:
                inRange(inputImage, COLOR_RANGES.lowViolet , COLOR_RANGES.highViolet , output); //Violer/Pink color
                break;
            case S_WHITE:
                inRange(inputImage, COLOR_RANGES.lowWhite, COLOR_RANGES.highWhite, output);
                break;
            default:
                output = inputImage;
                break;
        }

        return output;

    }

    Mat LowerNumMask(Mat img, bool reversThreshType, int threshValue)
    {
        Mat workImg;

        GaussianBlur(img, workImg, Size(3,3), 0);
        equalizeHist(workImg, workImg);

        if(reversThreshType){
            threshold(workImg, workImg, threshValue, 255, THRESH_BINARY_INV);//35-50
            distanceTransform(workImg, workImg, DIST_L2, 3);
            normalize(workImg, workImg, 0, 1.0, NORM_MINMAX);
            threshold(workImg, workImg, 0.5, 1.0, THRESH_BINARY);
            workImg.convertTo(workImg, CV_8U, 255.0);
        }
        else{
            threshold(workImg, workImg, threshValue, 255, THRESH_BINARY);//35-50
        }

        return workImg;
    }

    int DetectRectOffset(vector<Rect> boxes)
    {
        int offset = 10;
        Rect box = boxes.back();
        boxes.pop_back();
        while(!boxes.empty())
        {
            int xR = box.br().x;
            int xL = box.tl().x;
            int yT = box.tl().y;
            int yB = box.br().y;
            for(Rect rect : boxes)
            {
                if(rect.tl().x > xR && rect.tl().x <= xR + this->refWidth && rect.br().y >= yB - offset && rect.br().y <= yB + offset)
                {
                    //__android_log_print(ANDROID_LOG_INFO, "OFS--xR", "%d | %d -- %d", rect.tl().x, xR, rect.tl().x - xR);
                    return  rect.tl().x - xR;
                }
                else if (rect.br().x < xL && rect.br().x >= xL - this->refWidth && rect.br().y >= yB - offset && rect.br().y <= yB + offset)
                {
                    //__android_log_print(ANDROID_LOG_INFO, "OFS--xL", "%d | %d -- %d", rect.br().x, xL, xL - rect.br().x);
                    return xL - rect.br().x;
                }
                else if (rect.br().y < yT && rect.br().y >= yT - this->refHeight && rect.br().x >= xL - offset && rect.br().x <= xL + offset)
                {
                    //__android_log_print(ANDROID_LOG_INFO, "OFS--yT", "%d | %d -- %d", rect.br().y, yT, yT - rect.br().y);
                    return yT - rect.br().y;
                }
                else if (rect.tl().y > yB && rect.tl().y <= yB + this->refHeight && rect.tl().x >= xR - offset && rect.tl().x <= xR + offset)
                {
                    //__android_log_print(ANDROID_LOG_INFO, "OFS--yB", "%d | %d -- %d", rect.tl().y, yB, rect.br().y - yB);
                    return (rect.br().y - yB) - rect.height;
                }
            }
            box = boxes.back();
            boxes.pop_back();
        }

        return this->refWidth / 8;
    }

    String patternIdType(PatternID id){
        switch (id){
            case PATID_ONE:
                return "1";
            case PATID_TWO:
                return "2";
            case PATID_THREE:
                return "3";
            case PATID_FOUR:
                return "4";
            case PATID_FIVE:
                return "5";
            case PATID_SIX:
                return "6";
            case PATID_RED:
                return "C";
            case PATID_BLUE:
                return "M";
            case PATID_GREEN:
                return "Ze";
            case PATID_YELLOW:
                return "Zl";
            case PATID_VIOLET:
                return "F";
            case PATID_WHITE:
                return "B";
            case PATID_NONE:
            default:
                return "?";
        }
    }
};