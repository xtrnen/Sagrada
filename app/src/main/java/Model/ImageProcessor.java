package Model;

import android.content.Context;
import android.graphics.Bitmap;

import com.example.sagrada.R;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.IOException;
import java.io.InputStream;

import Model.GameBoard.Structs.Dice;
import Model.GameBoard.Structs.Slot;

public class ImageProcessor
{
    Mat patternImg;
    Mat diceImg;

    public ImageProcessor() {}

    public void AddPatternImg(Mat img) { patternImg = img; }
    public void AddDiceImg(Mat img)
    {
        diceImg = img;
    }

    public native Slot[] PatternDetector(long output);
    public native Dice[] DiceDetector(long output);

    //TODO: Error handlers
}
