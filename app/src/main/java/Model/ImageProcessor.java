package Model;

import android.content.Context;

import com.example.sagrada.R;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.IOException;

public class ImageProcessor
{
    private Context context;
    Mat patternImg;
    Mat templateImg;
    Mat diceImg;

    public ImageProcessor(Mat _patternImg, Context _context)
    {
        this.patternImg = _patternImg;
        this.context = _context;
    }

    public void AddTemplateImgs()
    {
        try
        {
            this.templateImg = Utils.loadResource(this.context, R.drawable.circle_template);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }

    public void AddDiceImg(Mat _diceImg)
    {
        this.diceImg = _diceImg;
    }

    public native void testFunction(long output);
    public native void DiceDetector();
}
