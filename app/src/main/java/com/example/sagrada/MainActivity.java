package com.example.sagrada;

import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import org.opencv.android.Utils;
import org.opencv.core.CvException;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import java.io.IOException;

import Model.Points.Quests.CQ_TYPES;
import Model.Points.Quests.PQ_TYPES;
import Model.Points.Quests.Quest;
import Model.ImageProcessor;
import Model.Structs.Dice;
import Model.Structs.Slot;

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
        Dice[] dices = imgProcessor.DiceDetector(retImg.getNativeObjAddr());
        Slot[] slots = imgProcessor.PatternDetector(retImg.getNativeObjAddr());

        /*for(Slot slot : slots){
            Log.println(Log.INFO, "slot", slot.row + " | " + slot.col + " - " + slot.info);
        }
        for(Dice dice : dices){
            Log.println(Log.INFO, "dice", dice.row + " | " + dice.col + " - " + dice.number + " | " + dice.color);
        }
        */
        /*TEST*/
        Quest q = new Quest();
        q.SetPersonalCalculator(PQ_TYPES.EMERALD);
        q.SetCommonCalculator(CQ_TYPES.SAME_DIAGONAL);
        q.RunEvaluation(dices);

        if(img != null){
            imageView = (ImageView)findViewById(R.id.mat);
            imageView.setImageBitmap(convMatToBitmap(img));
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
