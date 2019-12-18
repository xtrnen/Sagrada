#include <jni.h>
#include "opencv2/imgproc/imgproc.hpp"
#include "opencv2/highgui/highgui.hpp"
#include "opencv2/highgui.hpp"
#include "opencv2/features2d.hpp"
#include <iostream>
#include <android/log.h>
#include <algorithm>
#include <vector>

using namespace cv;
using namespace std;

enum SagradaColor {
    S_RED,
    S_BLUE,
    S_GREEN,
    S_YELLOW,
    S_VIOLET,
    S_WHITE
};

enum BlurType {
    BLUR_GAUSSIAN,
    BLUR_MEDIAN
};

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

int MAX_COLOR_VALUE = 255;
int MIN_COLOR_VALUE = 0;

bool compareRectsOnRow (const Rect& l, const Rect& r) {
    return l.br().x > r.br().x;
}
bool compareRefRects(const Rect& h, const Rect& l){
    return h.br().y < l.br().y;
}

void GetJClassData(JNIEnv *env, jobject obj, Mat& img, Mat *imgTemplates);
PatternID CompareWithTemplate(Mat patternImg, Mat img);

Mat BlurImage(Mat input, BlurType blurType);
Mat FindHsvColor(Mat inputImage, SagradaColor color);
Mat FindContours(Mat input, vector<Rect>& rectBoxes);
Mat GetEdges(Mat input);
Mat EdgeDetection(Mat input);
Mat ColorDetection(Mat input);
vector<Point> DetectCircles(Mat input, int distDivider, double param1, double param2, int minRad, int maxRad);
Point FindControlPoint(Mat input);
Rect GetClosestRect(vector<Rect> polyRects, Point controlPoint);
vector<Rect> CompleteRefRects(Rect controlRect, Point controlPoint);
bool IsDefaultRect(Point controlPoint, Point br);
Rect CreateBlankRect(Rect prevRect, bool reverseDirection);
vector<Rect> FindRectsOnRow(vector<Rect> polyRects, Rect refRect);
vector<Rect> FindBlankRects(vector<Rect> rectsOnRow, Rect refRect, Point refPoint); //TODO : More functions/more elegant, currently so big branching
double getOtsuThresh(Mat input, Mat& output);
vector<Rect> RectPatternGrid(int rows, int cols, Point controlPoint, vector<Rect> contourBoxes);
vector<Mat> SplitImageToPattern(int rows, int cols, vector<Rect> rectMatrix, Mat image);  //TODO: MORE ELEGANT NAME
bool IsColorPattern(Mat subject, PatternID& pID);   //TODO: calibrate colors
bool IsDicePattern(Mat subject, int lowNum, int highNum, PatternID& pID);   //TODO: detect if given img is number on dice pattern on card
vector<PatternID> GetCardPattern(int rows, int cols, vector<Mat> splittedImg /*color palette, numRange*/);

//TODO : Structures with specified color range instead of creation in function.
//TODO : File with Enums, structures
//TODO : Function to fill output class
//TODO : Class that encapsulates game card pattern
//TODO : Class that encapsulates ImageDetection
//TODO : Class that encapsulates final dice positions
//TODO : Class that handles rules over the final dice class

Mat PATTERN_IMG;

extern "C"
JNIEXPORT void JNICALL Java_Model_ImageProcessor_testFunction(JNIEnv *env, jobject obj, jlong output){
    Mat& outputImg = *(Mat*) output;
    Mat patternImg;
    Mat imgTemplateArray[6];

    GetJClassData(env, obj, patternImg, imgTemplateArray);

    Mat gray, hsv;

    cvtColor(patternImg, gray, COLOR_BGR2GRAY);
    cvtColor(patternImg, hsv, COLOR_BGR2HSV);

    Mat histed, blurredImage;
    equalizeHist(gray, histed);    //Equalizing histogram of grayscaled image
    blurredImage = BlurImage(histed, BLUR_GAUSSIAN);    //Blurring image
    erode(blurredImage, blurredImage, getStructuringElement(MORPH_RECT, Size(5,5) , Point(0,0)));   //Highlighting lines

    //Detect edges
    Mat edges = EdgeDetection(blurredImage);    //Get edges
    Point controlPoint = FindControlPoint(blurredImage);

    vector<Rect> boxes;
    Mat drawing = FindContours(edges, boxes);

    vector<Rect> matrixRect = RectPatternGrid(4, 5, controlPoint ,boxes);

    vector<Mat> imageMatrix = SplitImageToPattern(4, 5, matrixRect, patternImg);

    imgTemplateArray[0].copyTo(PATTERN_IMG);

    vector<PatternID> card = GetCardPattern(4,5, imageMatrix);

    /*Mat tmp = imgTemplateArray[0];
    Mat tmpIn = imageMatrix[15];

    cvtColor(tmp, tmp, COLOR_BGR2GRAY);
    cvtColor(tmpIn, tmpIn, COLOR_BGR2GRAY);

    Mat result32f = Mat(tmpIn.rows - tmp.rows + 1, tmpIn.cols - tmp.cols + 1, CV_32FC1);
    Mat img_display;
    tmpIn.copyTo(img_display);

    matchTemplate(tmpIn, tmp, result32f, TM_CCOEFF_NORMED);

    Mat result;
    result32f.convertTo(result, CV_8U, 255.0);
    Mat ret = Mat::zeros( result.size(), CV_8UC3 );
    vector<Vec3f> circles;
    HoughCircles(result, circles, HOUGH_GRADIENT, 1, result.rows/16, 255, 10, 30, 50);

    vector<Point> centersVector;

    for( size_t i = 0; i < circles.size(); i++ )
    {
        Vec3i c = circles[i];
        Point center = Point(c[0], c[1]);
        // circle center --debug
        circle( result, center, 1, Scalar(255,0,0), 3, LINE_AA);
        // circle outline --debug
        int radius = c[2];
        circle( result, center, radius, Scalar(255,0,0), 5, LINE_AA);
        __android_log_print(ANDROID_LOG_INFO, "Circle Pos", "REF %d | %d ", center.x, center.y);
    }

    circle(result, Point(result.cols/2, result.rows/2), 1, Scalar(255,255,255), 3, LINE_AA);*/

    /*Mat img;
    Mat out;
    imageMatrix[4].copyTo(img);
    Mat draw = Mat::zeros( img.size(), CV_8UC3);
    bool ind = false;
    cvtColor(img,img, COLOR_BGR2GRAY);
    img = BlurImage(img, BLUR_GAUSSIAN);
    threshold(img, img, 170, 255, THRESH_BINARY);
    vector<vector<Point>> contours;
    vector<Vec4i> hierarchy;

    findContours(img, contours, hierarchy, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);

    vector<vector<Point>> contours_poly( contours.size());
    vector<Rect> boundRect( contours.size());
    vector<Point2f>centers( contours.size());

    Scalar color = Scalar( 0,0,0 );
    Scalar color1 = Scalar (0,0,255);
    int counter = 0;
    for( size_t i = 0; i < contours.size(); i++ )
    {
        double epsilon = 0.01*arcLength(contours[i],true);
        approxPolyDP( contours[i], contours_poly[i], epsilon, true );

        double areaContour = contourArea(contours_poly[i]);

        if( areaContour > 1000.0 && areaContour < 10000.0 && contours_poly[i].size() > 5){
            //__android_log_print(ANDROID_LOG_INFO, "CONTOUR SIZE", "%d", contours_poly[i].size());
            drawContours(draw, contours, (int) i, color1);
            counter++;
        }
        else{
            drawContours(draw, contours, (int) i, color);
        }
    }*/

    outputImg = patternImg;
};
void GetJClassData(JNIEnv *env, jobject obj, Mat& img, Mat *imgTemplates){
    jclass thisClass = env->GetObjectClass(obj);
    jclass matClass = env->FindClass("org/opencv/core/Mat");

    jmethodID getPtrMethod = env->GetMethodID(matClass, "getNativeObjAddr", "()J");
    jfieldID patternImgID = env->GetFieldID(thisClass, "patternImg", "Lorg/opencv/core/Mat;");
    jfieldID templateImgsID = env->GetFieldID(thisClass, "templateImgs", "[Lorg/opencv/core/Mat;");

    jobject patternImgJ = env->GetObjectField(obj, patternImgID);
    jobjectArray templateImgsJ = (jobjectArray)env->GetObjectField(obj, templateImgsID);

    //Mat templateImgs[1];
    imgTemplates[0] = *(Mat*)env->CallLongMethod(env->GetObjectArrayElement(templateImgsJ, 0), getPtrMethod);
    imgTemplates[1] = *(Mat*)env->CallLongMethod(env->GetObjectArrayElement(templateImgsJ, 1), getPtrMethod);
    img = *(Mat*)env->CallLongMethod(patternImgJ, getPtrMethod);
}
bool DetectWhiteBlobNumber(Mat _img, PatternID& _pID){
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
int PositionComparison(vector<Point> circles, Mat refImg){
    //calculate img center point
    Point imgCenter = Point(refImg.cols/2, refImg.rows/2);
    __android_log_print(ANDROID_LOG_INFO, "Circle Pos", "CENTER POINT %d | %d ", imgCenter.x, imgCenter.y);
    int offset = 40;
    //initialize 4 quadrants
    vector<Point> quadrant1, quadrant2, quadrant3, quadrant4, quadrantC;
    //separate circles in quadrants
    for(Point circle : circles){
        if(circle.x > imgCenter.x + offset && circle.y <= imgCenter.y){
            //1 Q
            __android_log_print(ANDROID_LOG_INFO, "Circle Pos", "1 Quadrant %d | %d ", circle.x, circle.y);
            quadrant1.push_back(circle);
        }
        else if(circle.x < imgCenter.x - offset && circle.y < imgCenter.y){
            //2 Q
            __android_log_print(ANDROID_LOG_INFO, "Circle Pos", "2 Quadrant %d | %d ", circle.x, circle.y);
            quadrant2.push_back(circle);
        }
        else if(circle.x < imgCenter.x - offset && circle.y >= imgCenter.y){
            //3 Q
            __android_log_print(ANDROID_LOG_INFO, "Circle Pos", "3 Quadrant %d | %d ", circle.x, circle.y);
            quadrant3.push_back(circle);
        }
        else if(circle.x > imgCenter.x + offset && circle.y > imgCenter.y){
            //4 Q
            __android_log_print(ANDROID_LOG_INFO, "Circle Pos", "4 Quadrant %d | %d ", circle.x, circle.y);
            quadrant4.push_back(circle);
        }
        else{
            //Center Q
            __android_log_print(ANDROID_LOG_INFO, "Circle Pos", "Center Quadrant %d | %d ", circle.x, circle.y);
            quadrantC.push_back(circle);
        }
    }

    if(!quadrantC.empty() && !quadrant1.empty() && !quadrant3.empty()){
        __android_log_print(ANDROID_LOG_INFO, "Circle Pos", "OUT 3");
        return 3;
    }
    else if(quadrantC.empty() && !quadrant2.empty() && !quadrant4.empty()){
        return 2;
    }
    else{
        return -42;
    }
}
PatternID CompareWithTemplate(Mat patternImg, Mat img){
    Mat pattern, Img;
    cvtColor(patternImg, pattern, COLOR_BGR2GRAY);
    cvtColor(img, Img, COLOR_BGR2GRAY);

    Mat result32f = Mat(Img.rows - pattern.rows + 1, Img.cols - pattern.cols + 1, CV_32FC1);
    Mat img_display;
    img.copyTo(img_display);

    matchTemplate(Img, pattern, result32f, TM_CCOEFF_NORMED);

    Mat result;
    result32f.convertTo(result, CV_8U, 255.0);

    vector<Point> centers = DetectCircles(result, 16, 255, 10, 30, 50);

    return PatternID(PositionComparison(centers, img));
}

Mat FindContours(Mat input, vector<Rect>& rectBoxes){
    Mat drawing = Mat::zeros( input.size(), CV_8UC3);
    vector<vector<Point>> contours;
    vector<Vec4i> hierarchy;

    findContours(input, contours, hierarchy, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);

    vector<vector<Point>> contours_poly( contours.size());
    vector<Rect> boundRect( contours.size());
    vector<Point2f>centers( contours.size());

    Scalar color = Scalar( 0,0,255 );
    Scalar color1 = Scalar (0,255,0);
    Scalar color2 = Scalar(0,255,255);
    for( size_t i = 0; i < contours.size(); i++ )
    {
        double epsilon = 0.01*arcLength(contours[i],true);
        approxPolyDP( contours[i], contours_poly[i], epsilon, true );

        if(contours_poly[i].size() == 4){
            drawContours(drawing, contours, (int) i, color1);
            boundRect[i] = boundingRect( contours_poly[i] );
            if(boundRect[i].area() > 50000.0){
                //rectangle(drawing, boundRect[i].tl(), boundRect[i].br(), color, 5);
                rectBoxes.push_back(boundRect[i]);
            }
        }
    }

    return drawing;
}
Mat FindHsvColor(Mat inputImage, SagradaColor color){
    Mat output;
    Mat lowerMask;
    Mat upperMask;

    /*Yellow color OK!*/
    static Scalar lowYellow = Scalar(20,100,20);
    static Scalar highYellow = Scalar(30,255,255);
    /*Green color OK!*/
    static Scalar lowGreen = Scalar(40,50,20);
    static Scalar highGreen = Scalar(70,255,255);
    /*Blue color OK*/
    static Scalar lowBlue = Scalar(80,50,20);
    static Scalar highBlue = Scalar(127,255,255);
    /*Red color OK!*/
    static Scalar lowRedFirstMask = Scalar(0, 150, 70);
    static Scalar highRedFirstMask = Scalar(10, 255, 255);
    static Scalar lowRedSecondMask = Scalar(175, 150, 70);
    static Scalar highRedSecondMask = Scalar(180, 255, 255);
    /*Violet color OK!*/
    static Scalar lowViolet = Scalar(145,100,20);
    static Scalar highViolet = Scalar(170,255,255);
    //TODO White color
    static Scalar lowWhite = Scalar(0,0,168);
    static Scalar highWhite = Scalar(180,50,255);

    switch (color){
        case S_GREEN:
            inRange(inputImage, lowGreen , highGreen , output); //Green color
            break;
        case S_BLUE:
            inRange(inputImage, lowBlue , highBlue , output); //Blue color
            break;
        case S_RED:
            inRange(inputImage, lowRedFirstMask , highRedFirstMask , lowerMask);   //Red color
            inRange(inputImage, lowRedSecondMask, highRedSecondMask, upperMask);
            output = lowerMask + upperMask;
            break;
        case S_YELLOW:
            inRange(inputImage, lowYellow , highYellow , output);   //Yellow color
            break;
        case S_VIOLET:  // TODO: Color range Violet
            inRange(inputImage, lowViolet , highViolet , output); //Violer/Pink color
            break;
        case S_WHITE:   // TODO: Color range + possible model White
            inRange(inputImage, lowWhite, highWhite, output);
            break;
        default:
            output = inputImage;
            break;
    }

    return output;

}
Mat BlurImage(Mat input, BlurType blurType){
    Mat output;
    switch (blurType){
        case BLUR_GAUSSIAN:
            GaussianBlur(input, output, Size(5,5), 2, 2, BORDER_CONSTANT);    //Blurring IMG using Gaussian blur
            break;
        case BLUR_MEDIAN:
            medianBlur(input, output, 3);   //Blurring IMG using Median blur
            break;
    }
    return output;
}
double getOtsuThresh(Mat input, Mat& output){

    double otsuThresh = threshold(input, output, MIN_COLOR_VALUE, MAX_COLOR_VALUE, THRESH_BINARY + THRESH_OTSU);  //Getting OTSU threshold from Grayscale Img

    return otsuThresh;
}
Mat GetEdges(Mat input){
    Mat edges;
    Mat otsuThreshImg;

    double otsuThresh = getOtsuThresh(input, otsuThreshImg);

    Canny(otsuThreshImg, edges, otsuThresh*0.3, otsuThresh, 3, true);   //Edge detection

    return edges;
}
Mat EdgeDetection(Mat input){

    Mat imageEdges = GetEdges(input);    //Canny algorithm to get edges
    dilate(imageEdges, imageEdges, getStructuringElement(MORPH_RECT, Size(5,5) , Point(0,0)));  //Highlighting edges

    return imageEdges;
}
/**/
vector<Point> DetectCircles(Mat input, int distDivider, double param1, double param2, int minRad, int maxRad)  {
    //Mat ret = Mat::zeros( input.size(), CV_8UC3 );
    vector<Vec3f> circles;
    HoughCircles(input, circles, HOUGH_GRADIENT, 1, input.rows/distDivider, param1, param2, minRad, maxRad);

    vector<Point> centersVector;

    for( size_t i = 0; i < circles.size(); i++ )
    {
        Vec3i c = circles[i];
        Point center = Point(c[0], c[1]);
        centersVector.push_back(center);
        /* circle center --debug
        circle( ret, center, 1, Scalar(255,0,0), 3, LINE_AA);
        // circle outline --debug
        int radius = c[2];
        circle( ret, center, radius, Scalar(255,0,0), 10, LINE_AA);*/
    }

    return centersVector;
}
Point FindControlPoint(Mat input){
    Mat otsuThreshImg;
    double otsuThresh = getOtsuThresh(input, otsuThreshImg);

    vector<Point> centers = DetectCircles(input, 64, otsuThresh, 20, 20, 50);

    /*Find right most element*/
    /*For bottom most a.y < b.y*/
    auto rightMost = minmax_element(centers.begin(), centers.end(), [](Point const& a, Point const& b){
        return a.x < b.x;
    });

    return Point(rightMost.second->x,rightMost.second->y);
}
/**/
Rect GetClosestRect(vector<Rect> polyRects, Point controlPoint){
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
bool IsDefaultRect(Point controlPoint, Point br) {
    int approxWidthL = 50;
    int approxWidthR = 150;

    return (br.x >= (controlPoint.x - approxWidthL) && br.x <= (controlPoint.x + approxWidthR));
}
vector<Rect> FindRectsOnRow(vector<Rect> polyRects, Rect refRect){
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
void SplitRowByRefRect(vector<Rect>& leftVector, vector<Rect>& rightVector, vector<Rect> inputVector, Point refPoint){
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
bool IsNextRectNeighbor(Rect rightRect, Rect nextRect, bool reverseDirection){
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
Rect CreateBlankRect(Rect prevRect, bool reverseDirection){
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
vector<Rect> FindBlankRects(vector<Rect> rectsOnRow, Rect refRect, Point refPoint){
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
vector<Rect> CompleteRefRects(Rect controlRect, Point controlPoint){
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
vector<Rect> RectPatternGrid(int rows, int cols, Point controlPoint, vector<Rect> contourBoxes){
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
/**/
vector<Mat> SplitImageToPattern(int rows, int cols, vector<Rect> rectMatrix, Mat image){
    int cellsNum = rows*cols;
    vector<Mat> outputMatrix;

    for(int i = 0; i < cellsNum; i++){
        outputMatrix.push_back(image(rectMatrix[i]));
    }

    return outputMatrix;
}
/**/
bool IsColorRect(Mat subject, SagradaColor color){
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
PatternID CheckColor(Mat subject){
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
bool IsColorPattern(Mat subject, PatternID& pID){
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
PatternID CheckNumber(Mat subject, int minRadius, int maxRadius, double lowerThresh, double upperThresh, int minDist){
    vector<Vec3f> circles;

    HoughCircles(subject, circles, HOUGH_GRADIENT, 1, minDist, upperThresh, lowerThresh, minRadius, maxRadius);

    //__android_log_print(ANDROID_LOG_INFO, "NUM OF CIRCLES: ", "%d", circles.size());

    return PatternID(circles.size());
}
bool IsDicePattern(Mat subject, int lowNum, int highNum, PatternID& pID){
    Mat img;
    cvtColor(subject, img, COLOR_BGR2GRAY);
    img = BlurImage(img, BLUR_GAUSSIAN);    //Blurring image
    erode(img, img, getStructuringElement(MORPH_RECT, Size(5,5) , Point(0,0)));   //Highlighting lines

    Mat otsuThreshImg;
    double otsuThresh = getOtsuThresh(img, otsuThreshImg);

    /*PatternID tmpPID = CheckNumber(img, 35, 40, 20, otsuThresh,img.rows/16);
    if(tmpPID > 3 && tmpPID < 7){
        pID = tmpPID;
        return true;
    }
    tmpPID = CheckNumber(img, 40,50, 10, 255, 110);
    if(tmpPID == 3){
        pID = tmpPID;
        return true;
    }*/
    PatternID tmpPID;
    if(DetectWhiteBlobNumber(subject, tmpPID)){
        pID = tmpPID;
    }
    else{
        tmpPID = CompareWithTemplate(PATTERN_IMG, subject);
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
vector<PatternID> GetCardPattern(int rows, int cols, vector<Mat> splittedImg){
    vector<PatternID> cardPattern;
    PatternID pID;
    for (Mat img : splittedImg) {
        if(IsColorPattern(img, pID)){   //TODO: VIOLET
            cardPattern.push_back(pID);
            __android_log_print(ANDROID_LOG_INFO, "TREE :", "IS COLOR %d", pID);
        }
        else if(IsDicePattern(img, 1, 6, pID)){
            cardPattern.push_back(pID);
            __android_log_print(ANDROID_LOG_INFO, "TREE :", "IS DICE %d", pID);
        }
        else{
            cardPattern.push_back(PatternID(PATID_NONE));
            __android_log_print(ANDROID_LOG_INFO, "TREE :", "IS UNKNOWN %d", pID);
        }
    }

    return cardPattern;
}