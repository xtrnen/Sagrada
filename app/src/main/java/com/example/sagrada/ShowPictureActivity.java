package com.example.sagrada;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.otaliastudios.cameraview.PictureResult;
import com.otaliastudios.cameraview.size.Size;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.Arrays;

import Model.GameBoard.Structs.Dice;
import Model.GameBoard.Structs.Slot;
import Model.ImageProcessor;

public class ShowPictureActivity extends AppCompatActivity {
    private static PictureResult picture;
    private static int rectX;
    private static int rectY;
    private static int rectW;
    private static int rectH;
    private Button confirmBtn;
    private Button declineBtn;
    private ImageView imageView;
    private int requestCode;
    private ArrayList<Slot> slotArray;
    private ArrayList<Dice> diceArray;

    public static void setPictureResult(@Nullable PictureResult pictureResult){
        picture = pictureResult;
    }
    public static void setRectCoord(int x, int y, int width, int height){
        rectX = x;
        rectY = y;
        rectW = width;
        rectH = height;
    }

    static {
        if (!OpenCVLoader.initDebug()) {
            // Handle initialization error
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.picture_preview_activity);
        System.loadLibrary("native-lib");

        requestCode = getIntent().getIntExtra("Data", 0);

        Toolbar toolbar = findViewById(R.id.PicturePreviewToolbarID);
        confirmBtn = findViewById(R.id.confirmDetBtnID);
        declineBtn = findViewById(R.id.declineDetBtnID);
        imageView = findViewById(R.id.DetectedViewID);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        declineBtn.setOnClickListener(v -> {//TODO: Blbost
        });
        confirmBtn.setOnClickListener(v -> {
            Intent data = new Intent();
            if(requestCode == GameActivity.REQUEST_SLOTS){
                data.putParcelableArrayListExtra(GameActivity.DATA_SLOTS, slotArray);
            } else if(requestCode == GameActivity.REQUEST_DICES){
                data.putParcelableArrayListExtra(GameActivity.DATA_DICES, diceArray);
            } else {
                return;
            }
            setResult(CamActivity.VALID_PREVIEW, data);
            finish();
        });

        final PictureResult result = picture;
        if(result == null){
            finish();
            return;
        }

        try {
            Size resultSize = result.getSize();
            result.toBitmap(resultSize.getWidth(), resultSize.getHeight(), bitmap -> {
                int x = rectX * bitmap.getWidth() / rectW;
                int y = rectY * bitmap.getHeight() / rectH;
                int endX = (rectX + CamActivity.CROP_WIDTH) * bitmap.getWidth() / rectW;
                int endY = (rectY + CamActivity.CROP_HEIGHT) * bitmap.getHeight() / rectH;
                Bitmap newBitmap = Bitmap.createBitmap(bitmap, x, y, endX - x, endY - y);

                Mat mat = new Mat();
                Utils.bitmapToMat(newBitmap, mat);
                ImageProcessor imageProcessor = new ImageProcessor();
                if(requestCode == GameActivity.REQUEST_SLOTS){
                    imageProcessor.AddPatternImg(mat);
                    Slot[] slots = imageProcessor.PatternDetector(mat.getNativeObjAddr());
                    for(Slot slot : slots){
                        Log.println(Log.INFO, "SLOTS", String.valueOf(slot.info));
                    }
                    slotArray = new ArrayList<>(Arrays.asList(slots));
                } else if(requestCode == GameActivity.REQUEST_DICES){
                    imageProcessor.AddDiceImg(mat);
                    Dice[] dices = imageProcessor.DiceDetector(mat.getNativeObjAddr());
                    /*for(Dice dice : dices){
                        Log.println(Log.INFO, "DICES", dice.color + "|" + dice.number + "( " + dice.row + "|" + dice.col + ")");
                    }*/
                    diceArray = new ArrayList<>(Arrays.asList(dices));
                }

                newBitmap = MainActivity.convMatToBitmap(mat);
                imageView.setImageBitmap(newBitmap);
            });
        } catch (UnsupportedOperationException e){
            Toast.makeText(this, "Cant show pic format:" + picture.getFormat(), Toast.LENGTH_LONG).show();
        } catch (UnknownError e){
            Log.println(Log.ERROR, "DETECT", ":(");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(!isChangingConfigurations()){
            setPictureResult(null);
            setRectCoord(0,0,0,0);
        }
    }

    private Mat detectPattern(Bitmap bitmap){
        Mat mat = new Mat();
        Utils.bitmapToMat(bitmap, mat);
        ImageProcessor imageProcessor = new ImageProcessor();
        imageProcessor.AddPatternImg(mat);
        imageProcessor.PatternDetector(mat.getNativeObjAddr());
        return mat;
    }
}
