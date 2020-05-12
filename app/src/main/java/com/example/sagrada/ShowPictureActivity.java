package com.example.sagrada;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.shapes.Shape;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

import com.otaliastudios.cameraview.PictureResult;
import com.otaliastudios.cameraview.size.Size;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvException;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Arrays;

import Model.GameBoard.Structs.Dice;
import Model.GameBoard.Structs.Slot;
import Model.ImageProcessor;

public class ShowPictureActivity extends AppCompatActivity implements InvalidDetectionDialogFragment.IDetectionFailedDialogListener {
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
        toolbar.setNavigationOnClickListener(v -> sendBackData());

        declineBtn.setOnClickListener(v -> {
            invalidDialog();
        });
        confirmBtn.setOnClickListener(v -> sendBackData());

        final PictureResult result = picture;
        if(result == null){
            finish();
            return;
        }

        try {
            Size resultSize = result.getSize();
            result.toBitmap(resultSize.getWidth(), resultSize.getHeight(), bitmap -> {
                Matrix matrix = new Matrix();
                Bitmap newBitmap;
                int rot;
                switch (result.getRotation()){
                    case 180:
                        rot = 270;
                        break;
                    case 270:
                        rot = 360;
                        break;
                    case 0:
                        rot = 90;
                        break;
                    default:
                        rot = 0;
                        break;
                }
                matrix.postRotate(rot);
                newBitmap = Bitmap.createBitmap(bitmap, 0,0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

                int x = rectX * bitmap.getWidth() / rectW;
                int y = rectY * bitmap.getHeight() / rectH;
                int endX = (rectX + CamActivity.CROP_WIDTH) * bitmap.getWidth() / rectW;
                int endY = (rectY + CamActivity.CROP_HEIGHT) * bitmap.getHeight() / rectH;

                newBitmap = Bitmap.createBitmap(newBitmap, x, y, endX - x, endY - y);

                Mat mat = new Mat();
                Utils.bitmapToMat(newBitmap, mat);
                ImageProcessor imageProcessor = new ImageProcessor();
                if(requestCode == GameActivity.REQUEST_SLOTS){
                    imageProcessor.AddPatternImg(mat);
                    Slot[] slots = imageProcessor.PatternDetector(mat.getNativeObjAddr());
                    slotArray = new ArrayList<>(Arrays.asList(slots));
                } else if(requestCode == GameActivity.REQUEST_DICES){
                    imageProcessor.AddDiceImg(mat);
                    Dice[] dices = imageProcessor.DiceDetector(mat.getNativeObjAddr());
                    diceArray = new ArrayList<>(Arrays.asList(dices));
                }

                newBitmap = convMatToBitmap(mat);
                imageView.setImageBitmap(newBitmap);
            });
        } catch (UnsupportedOperationException e){
            Toast.makeText(this, "Cant show pic format:" + picture.getFormat(), Toast.LENGTH_LONG).show();
        } catch (UnknownError e){
            Toast.makeText(this, "Error in getting bitmap", Toast.LENGTH_LONG).show();
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

    private void sendBackData(){
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
    }
    private void invalidDialog(){
        DialogFragment detectionDialog = new InvalidDetectionDialogFragment();
        detectionDialog.show(getSupportFragmentManager(), "InvalidDialogFragment");
    }

    @Override
    public void onCaptureAgain() {
        setResult(CamActivity.CAPTURE_AGAIN);
        finish();
    }

    @Override
    public void onUserHandle() {
        setResult(GameActivity.REQUEST_INFO_ACTIVITY);
        finish();
    }

    @Override
    public void onCancel() {
        //Do nothing
    }

    private static Bitmap convMatToBitmap(Mat input){
        Bitmap bmp = null;
        Mat rgb = new Mat();
        Imgproc.cvtColor(input, rgb, Imgproc.COLOR_BGR2RGB);

        try {
            bmp = Bitmap.createBitmap(rgb.cols(), rgb.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(rgb, bmp);
        } catch (CvException e){
            //
        }

        return bmp;
    }
}
