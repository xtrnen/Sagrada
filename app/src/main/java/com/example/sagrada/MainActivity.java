package com.example.sagrada;

import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import org.opencv.android.Utils;
import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import java.io.IOException;
import Model.ImageProcessor;

public class MainActivity extends AppCompatActivity {

    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        System.loadLibrary("native-lib");

        Mat img = null;
        Mat retImg = new Mat();
        try {
            img = Utils.loadResource(this, R.drawable.nejsvetejsi_trojice);
        } catch (IOException e){
            e.printStackTrace();
        }

        ImageProcessor imgProcessor = new ImageProcessor(img, this);
        imgProcessor.AddTemplateImgs();
        imgProcessor.AddDiceImg();
        //imgProcessor.testFunction(retImg.getNativeObjAddr());
        imgProcessor.DiceDetector(retImg.getNativeObjAddr());

        if(retImg != null){
            imageView = (ImageView)findViewById(R.id.mat);
            imageView.setImageBitmap(convMatToBitmap(retImg));
        }
    }

    private static Bitmap convMatToBitmap(Mat input){
        Bitmap bmp = null;
        Mat rgb = new Mat();
        Imgproc.cvtColor(input, rgb, Imgproc.COLOR_BGR2RGB);

        try {
            bmp = Bitmap.createBitmap(rgb.cols(), rgb.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(rgb, bmp);
        } catch (CvException e){
            Log.d("Exception", e.getMessage());
        }

        return bmp;
    }

}
