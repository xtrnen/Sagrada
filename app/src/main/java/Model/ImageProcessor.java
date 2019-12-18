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
    Mat[] templateImgs = new Mat[2];

    public ImageProcessor(Mat _patternImg, Context _context)
    {
        this.patternImg = _patternImg;
        this.context = _context;
    }

    public void AddTemplateImgs()
    {
        try
        {
            this.templateImgs[0] = Utils.loadResource(this.context, R.drawable.circle_template);
            this.templateImgs[1] = Utils.loadResource(this.context, R.drawable.one_template);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }

    public native void testFunction(long output);
}
