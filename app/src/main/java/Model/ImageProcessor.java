package Model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import com.example.sagrada.R;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import Model.Structs.Dice;
import Model.Structs.Slot;

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

    public void AddDiceImg()
    {
        /*try{
            this.diceImg = Utils.loadResource(this.context, R.drawable.dices1);
        }
        catch (IOException e){
            e.printStackTrace();
        }*/
        RotateImage();
    }

    public void RotateImage(){
        try {
            InputStream inStream = this.context.getAssets().open("dice_not_con.jpg");
            Bitmap bitmap = ImageRotator.rotateImage(inStream);
            this.diceImg = ImageRotator.convertImage(bitmap);
            inStream.close();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public native Slot[] PatternDetector(long output);
    public native Dice[] DiceDetector(long output);
}
