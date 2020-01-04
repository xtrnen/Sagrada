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

bool compareRectsOnRow (const Rect& l, const Rect& r) {
    return l.br().x > r.br().x;
}

class PatternAnalyzer
{
public:
    Mat patternImg;
    Mat templateImg;
    Point controlPoint;
    int refOffset;
    int refHeight;
    int refWidth;
    vector<PatternID> pattern;
    Mat tmp;

    PatternAnalyzer(Mat _patternImg, Mat _templateImg)
    {
        this->patternImg = _patternImg;
        this->templateImg = _templateImg;
    }

    vector<Mat> CreatePatternGrid()
    {
        //PrepGrayImg
        Mat gray = PrepGrayImg();

        //DetectEdges
        Mat edges = DetectEdges(gray);
        this->patternImg.copyTo(this->tmp);

        //FindControlPoint
        Point controlPoint = DetectControlPoint(gray);
        this->controlPoint = controlPoint;

        //DetectContours
        vector<Rect> boxes;
        FindContours(edges, boxes);

        //SplitImg
        vector<Rect> matrixRect = RectPatternGrid(4, 5, controlPoint, boxes);

        for(Rect rect : matrixRect)
        {
            rectangle(this->tmp, rect, Scalar(0,0,255), 10);
        }

        return SplitImageToPattern(4, 5, matrixRect, this->patternImg);
    }

    void GetCardPattern(vector<Mat> _splittedImg)
    {
        vector<PatternID> cardPattern;
        PatternID pID;
        for (Mat img : _splittedImg) {
            if(IsColorPattern(img, pID)){   //TODO: VIOLET
                cardPattern.push_back(pID);
                __android_log_print(ANDROID_LOG_INFO, "TREE :", "IS COLOR %s", PIDName(pID));
            }
            else if(IsDicePattern(img, 1, 6, pID)){
                cardPattern.push_back(pID);
                __android_log_print(ANDROID_LOG_INFO, "TREE :", "IS DICE %s", PIDName(pID));
            }
            else{
                cardPattern.push_back(PatternID(PATID_NONE));
                __android_log_print(ANDROID_LOG_INFO, "TREE :", "IS UNKNOWN %s", PIDName(pID));
            }
        }

        this->pattern = cardPattern;
    }

private:
    Mat PrepGrayImg()
    {
        Mat prepImg;
        this->patternImg.copyTo(prepImg);

        cvtColor(prepImg, prepImg, COLOR_BGR2GRAY);
        equalizeHist(prepImg, prepImg);
        GaussianBlur(prepImg, prepImg, Size(5,5), 2, 2, BORDER_CONSTANT);
        erode(prepImg, prepImg, getStructuringElement(MORPH_RECT, Size(5,5) , Point(0,0)));

        return prepImg;
    }

    Mat DetectEdges(Mat _grayImg)
    {
        Mat edges;
        _grayImg.copyTo(edges);

        double otsuThresh = threshold(edges, edges, 128, 255, THRESH_BINARY );

        Canny(edges, edges, otsuThresh*0.3, otsuThresh, 3, true);
        dilate(edges, edges, getStructuringElement(MORPH_RECT, Size(5,5) , Point(0,0)));

        return edges;
    }

    Point DetectControlPoint(Mat _grayImg)
    {
        Mat grayImg;
        //Mat tmp;
        threshold(_grayImg, grayImg, 180, 255, THRESH_BINARY);
        /*double otsuThresh = threshold(grayImg, tmp, 0, 255, THRESH_BINARY + THRESH_OTSU);

        vector<Point> centers = DetectCircles(grayImg, 64, otsuThresh, 50, 20, 20);

        auto rightMost = minmax_element(centers.begin(), centers.end(), [](Point const& a, Point const& b){
            return a.x < b.x;
        });*/
        vector<vector<Point>> contours;
        vector<Vec4i> hierarchy;

        findContours(grayImg, contours, hierarchy, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);

        vector<vector<Point>> contours_poly( contours.size());
        vector<Point>centers( contours.size());

        for( size_t i = 0; i < contours.size(); i++ )
        {
            double epsilon = 0.01*arcLength(contours[i],true);
            approxPolyDP( contours[i], contours_poly[i], epsilon, true );

            double area = contourArea(contours_poly[i]);
            Point2f center;
            float radius;
            minEnclosingCircle(contours_poly[i], center, radius);
            double cArea = radius * radius * 3.14;
            //__android_log_print(ANDROID_LOG_INFO, "DDD", "%f || %f", area, cArea);
            if(area > 100.0 && cArea >= area && cArea < area * 2){
                //__android_log_print(ANDROID_LOG_INFO, "DDD", "%f || %f", area, cArea);
                //circle(this->tmp, center, (int)radius, Scalar(0,255,0), 3);
                centers.push_back(Point((int)center.x, (int)center.y));
            }

        }

        Point bottom = Point(0,0);

        for(Point cent : centers)
        {
            if(cent.y > bottom.y)
                bottom = cent;
        }
        for(Point cent : centers)
        {
            if(cent.y >= bottom.y - 10 && cent.x > bottom.x)
                bottom = cent;
        }

        circle(this->tmp, bottom, 1, Scalar(0,0,255), 20);

        return bottom;
    }

    vector<Point> DetectCircles(Mat input, int _distDivider, double _param1, double _param2, int _minRad, int _maxRad)
    {
        vector<Vec3f> circles;
        HoughCircles(input, circles, HOUGH_GRADIENT, 1, input.rows/_distDivider, _param1, _param2, _minRad, _maxRad);

        vector<Point> centersVector;

        for( size_t i = 0; i < circles.size(); i++ )
        {
            Vec3i c = circles[i];
            Point center = Point(c[0], c[1]);
            centersVector.push_back(center);
            //circle(this->tmp, center, c[2], Scalar(0,0,255), 3, LINE_AA);
        }

        return centersVector;
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

        long sum = 0;
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
                if(roRect.boundingRect().br().y < this->controlPoint.y && area > 100 && hierarchy[i][2] != -1 && roArea >= area && roArea < area*2)
                {
                    sum += area;
                    count++;
                    Rect rect = boundingRect( contours_poly[i] );
                    rectBoxes.push_back(rect);
                }
            }
        }

        int approx = sum / count;

        int sumWidth = 0;
        int sumHeight = 0;
        count = 0;

        for(int i = 0; i < rectBoxes.size(); i++)
        {
            if(rectBoxes[i].area() < approx)
            {
                rectBoxes.erase(rectBoxes.begin() + i);
            }
            else
            {
                sumWidth += rectBoxes[i].width;
                sumHeight += rectBoxes[i].height;
                count++;
            }
        }

        this->refHeight = sumHeight / count;
        this->refWidth = sumWidth / count;

        return drawing;
    }

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
            if(rectsOnRow.size() > 4){
                __android_log_print(ANDROID_LOG_INFO, "ERROR-INFO", "RECTS ON ROW > 4");
            }
            if(!rectsOnRow.empty()){
                sort(rectsOnRow.begin(), rectsOnRow.end(), compareRectsOnRow);
            }

            blankRects = FindBlankRects(rectsOnRow, refRects[i], controlPoint);

            for(Rect rect : blankRects){
                tmpVector.push_back(rect);
            }

            sort(tmpVector.begin(), tmpVector.end(), compareRectsOnRow);

            retVector.insert(retVector.begin(), tmpVector.begin(), tmpVector.end());

            tmpVector.clear();
        }

        return retVector;
    }

    vector<Rect> CompleteRefRects(Rect controlRect, Point controlPoint)
    {
        int offset = 50;
        vector<Rect> outputVector;
        Point br;
        Point tl;
        Rect refRect = controlRect;
        Rect tmpRect;

        while(refRect.br().y < controlPoint.y - refRect.width - offset) {
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
        int offset = 50;
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

        return Rect(tl, br);
    }

    Rect GetClosestRect(vector<Rect> polyRects, Point controlPoint)
    {
        Rect tmpRect = Rect(Point(0,0),Point(0,0));
        int offset = 30;

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

    PatternID CheckNumber(Mat subject, int minRadius, int maxRadius, double lowerThresh, double upperThresh, int minDist)
    {
        vector<Vec3f> circles;

        HoughCircles(subject, circles, HOUGH_GRADIENT, 1, minDist, upperThresh, lowerThresh, minRadius, maxRadius);

        //__android_log_print(ANDROID_LOG_INFO, "NUM OF CIRCLES: ", "%d", circles.size());

        return PatternID(circles.size());
    }

    bool IsDicePattern(Mat subject, int lowNum, int highNum, PatternID& pID)
    {
        Mat img;
        cvtColor(subject, img, COLOR_BGR2GRAY);
        GaussianBlur(img, img, Size(5,5), 2, 2, BORDER_CONSTANT);    //Blurring image
        erode(img, img, getStructuringElement(MORPH_RECT, Size(5,5) , Point(0,0)));   //Highlighting lines

        Mat otsuThreshImg;
        double otsuThresh = threshold(img, otsuThreshImg, 0, 255, THRESH_BINARY + THRESH_OTSU);

        PatternID tmpPID;
        if(DetectWhiteBlobNumber(subject, tmpPID)){
            pID = tmpPID;
        }
        else{
            tmpPID = CompareWithTemplate(this->templateImg, subject);
        }
        if(tmpPID != PATID_NONE){
            pID = tmpPID;
            return true;
        }
        tmpPID = CheckNumber(img, 48,50, 10, otsuThresh, img.rows);
        if(tmpPID == 1){
            pID = tmpPID;
            return true;
        }
        else{
            return false;
        }
    }

    bool DetectWhiteBlobNumber(Mat _img, PatternID& _pID)
    {
        Mat img;
        _img.copyTo(img);
        bool ind = false;
        cvtColor(img,img, COLOR_BGR2GRAY);
        threshold(img, img, 170, 255, THRESH_BINARY);
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
        for( size_t i = 0; i < contours.size(); i++ )
        {
            double epsilon = 0.01*arcLength(contours[i],true);
            approxPolyDP( contours[i], contours_poly[i], epsilon, true );

            double areaContour = contourArea(contours_poly[i]);

            if( areaContour > 1000.0 && areaContour < 10000.0 && contours_poly[i].size() > 5){
                drawContours(img, contours, (int) i, color1);
                counter++;
            }
            else{
                drawContours(img, contours, (int) i, color);
            }
        }

        if(counter == 6 || counter == 4 || counter == 5){
            //__android_log_print(ANDROID_LOG_INFO, "DETECT WHITE BLOB", "%d", counter); //DEBUG
            _pID = PatternID(counter);
            return true;
        }
        else{
            _pID = PATID_NONE;
            return false;
        }
    }

    int PositionComparison(vector<Point> circles, Mat refImg)
    {
        //calculate img center point
        Point imgCenter = Point(refImg.cols/2, refImg.rows/2);
        //__android_log_print(ANDROID_LOG_INFO, "Circle Pos", "CENTER POINT %d | %d ", imgCenter.x, imgCenter.y);
        int offset = 40;
        //initialize 4 quadrants
        vector<Point> quadrant1, quadrant2, quadrant3, quadrant4, quadrantC;
        //separate circles in quadrants
        for(Point circle : circles){
            if(circle.x > imgCenter.x + offset && circle.y < imgCenter.y - offset){
                //1 Q
                __android_log_print(ANDROID_LOG_INFO, "Circle Pos", "1 Quadrant %d | %d ", circle.x, circle.y);
                quadrant1.push_back(circle);
            }
            else if(circle.x < imgCenter.x - offset && circle.y < imgCenter.y - offset){
                //2 Q
                __android_log_print(ANDROID_LOG_INFO, "Circle Pos", "2 Quadrant %d | %d ", circle.x, circle.y);
                quadrant2.push_back(circle);
            }
            else if(circle.x < imgCenter.x - offset && circle.y > imgCenter.y + offset){
                //3 Q
                __android_log_print(ANDROID_LOG_INFO, "Circle Pos", "3 Quadrant %d | %d ", circle.x, circle.y);
                quadrant3.push_back(circle);
            }
            else if(circle.x > imgCenter.x + offset && circle.y > imgCenter.y + offset){
                //4 Q
                __android_log_print(ANDROID_LOG_INFO, "Circle Pos", "4 Quadrant %d | %d ", circle.x, circle.y);
                quadrant4.push_back(circle);
            }
            else if(circle.x >= imgCenter.x - offset && circle.x <= imgCenter.x + offset){
                //Center Q
                __android_log_print(ANDROID_LOG_INFO, "Circle Pos", "Center Quadrant %d | %d ", circle.x, circle.y);
                quadrantC.push_back(circle);
            }
        }

        if(!quadrantC.empty() && (!quadrant1.empty() || !quadrant3.empty())){
            //for each centerP check each point in q1 & q3 for diagonal matching
            for(Point refPoint : quadrantC)
            {
                bool q1 = false;
                bool q3 = false;
                for(Point q1P : quadrant1)
                {
                    if(IsOnDiagonal(refPoint, q1P, 225.0, 50))
                    {
                        q1 = true;
                        break;
                    }
                }
                for(Point q3P : quadrant3)
                {
                    if(IsOnDiagonal(refPoint, q3P, -45.0, 50))
                    {
                        q3 = true;
                        break;
                    }
                }
                if(q1 || q3)
                {
                    return 3;
                }
                return -42;
            }
        }
        else if(quadrantC.empty() && !quadrant2.empty() && !quadrant4.empty()){
            for(Point refPoint : quadrant2)
            {
                bool q4 = false;
                for(Point q4P : quadrant4)
                {
                    if(IsOnDiagonal(refPoint, q4P, 315.0, 50))
                    {
                        q4 = true;
                        break;
                    }
                }
                if(q4)
                {
                    return 2;
                }
            }
            return -42;
        }
        else{
            return -42;
        }
    }

    PatternID CompareWithTemplate(Mat patternImg, Mat img)
    {
        Mat pattern, Img;
        cvtColor(patternImg, pattern, COLOR_BGR2GRAY);
        cvtColor(img, Img, COLOR_BGR2GRAY);

        Mat result32f = Mat(Img.rows - pattern.rows + 1, Img.cols - pattern.cols + 1, CV_32FC1);
        Mat img_display;
        img.copyTo(img_display);

        matchTemplate(Img, pattern, result32f, TM_CCOEFF_NORMED);

        Mat result;
        result32f.convertTo(result, CV_8U, 255.0);

        vector<Point> centers = DetectCircles(result, 8, 255, 10, 30, 40);//result, 8, 255, 10, 30, 50

        return PatternID(PositionComparison(centers, img));
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
            case S_VIOLET:  // TODO: Color range Violet
                inRange(inputImage, COLOR_RANGES.lowViolet , COLOR_RANGES.highViolet , output); //Violer/Pink color
                break;
            case S_WHITE:   // TODO: Color range + possible model White
                inRange(inputImage, COLOR_RANGES.lowWhite, COLOR_RANGES.highWhite, output);
                break;
            default:
                output = inputImage;
                break;
        }

        return output;

    }

    char* PIDName(PatternID _pid)
    {
        switch (_pid)
        {
            case PATID_ONE :
                return "ONE";
            case PATID_TWO :
                return "TWO";
            case PATID_THREE :
                return "THREE";
            case PATID_FOUR :
                return "FOUR";
            case PATID_FIVE :
                return "FIVE";
            case PATID_SIX :
                return "SIX";
            case PATID_RED :
                return "RED";
            case PATID_BLUE :
                return "BLUE";
            case PATID_GREEN :
                return "GREEN";
            case PATID_YELLOW :
                return "YELLOW";
            case PATID_VIOLET :
                return "VIOLET";
            case PATID_WHITE :
                return "WHITE";
            case PATID_NONE :
                return "UNKNOWN";
        }
    }

    bool IsOnDiagonal(Point refPoint, Point sugPoint, double angle, int offset)
    {
        //diagonal line equation
        double rad = angle*3.14159/180;
        double k = tan(angle);
        double q = sugPoint.y - (k * sugPoint.x);
        //find Point on given height
        double x = (refPoint.y - q)/k;
        //check if in range
        __android_log_print(ANDROID_LOG_INFO, "DIAGONAL", "X : %f | K : %f | Q : %f", x, k, q);
        return (x >= refPoint.x - offset && x <= refPoint.x + offset);
    }

    Rect DetectKeyRect()
    {

    }

    int DetectRectOffset()
    {

    }

};