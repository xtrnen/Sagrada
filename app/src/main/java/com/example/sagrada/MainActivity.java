package com.example.sagrada;

import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.camera2.CameraDevice;
import android.media.ExifInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.widget.ImageView;
import org.opencv.android.Utils;
import org.opencv.core.CvException;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import java.io.IOException;

import Activities.CameraApi;
import Model.GameBoard.GameBoard;
import Model.GameBoard.Structs.Slot;
import Model.GameBoard.Structs.SlotInfo;
import Model.Points.Quests.CQ_TYPES;
import Model.Points.Quests.PQ_TYPES;
import Model.Points.Quests.Quest;
import Model.ImageProcessor;
import Model.GameBoard.Structs.Dice;
import Model.Rules.RuleHandler;

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
            img = Utils.loadResource(this, R.drawable.sagrada_up_color);
        } catch (IOException e){
            e.printStackTrace();
        }


        Bitmap bitmap = convMatToBitmap(img);
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        Bitmap bit = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        Utils.bitmapToMat(bit, img);
        ImageProcessor imgProcessor = new ImageProcessor(img, this);
        imgProcessor.AddDiceImg(img);
        //Slot[] slots = imgProcessor.PatternDetector(retImg.getNativeObjAddr());
        Dice[] dices = imgProcessor.DiceDetector(retImg.getNativeObjAddr());

        /*if(slots.length != 20){
            Log.println(Log.ERROR, "Slot array", "Length doesn't fit");
        }
        if(dices.length == 0){
            Log.println(Log.ERROR, "Dice array", "No dice detected");
        }*/

        /*for(Slot slot : slots){
            Log.println(Log.INFO, "slot", slot.row + " | " + slot.col + " - " + slot.info);
        }*/
        for(Dice dice : dices){
            Log.println(Log.INFO, "dice", dice.row + " | " + dice.col + " - " + dice.number + " | " + dice.color);
        }

        imageView = (ImageView)findViewById(R.id.testDice);
        imageView.setImageBitmap(convMatToBitmap(retImg));

        /*TEST*/
        /*GameBoard gameBoard = new GameBoard(dices, slots, 4, 5);
        RuleHandler hand = new RuleHandler(gameBoard.diceArray, gameBoard.slotArray);
        if(hand.CheckRules()){
            int points = gameBoard.Evaluation(PQ_TYPES.AMETHYST, CQ_TYPES.MIDDLE_PAIR);
            Log.println(Log.INFO, "Points", Integer.toString(points));
        }
        else{
            Log.println(Log.INFO, "Rules", "Rules validation failed");
        }*/

    }

    public static Bitmap convMatToBitmap(Mat input){
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
