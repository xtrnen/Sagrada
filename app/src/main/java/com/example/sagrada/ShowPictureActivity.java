package com.example.sagrada;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.otaliastudios.cameraview.PictureResult;
import com.otaliastudios.cameraview.size.Size;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import Model.GameBoard.Structs.Slot;
import Model.ImageProcessor;

public class ShowPictureActivity extends AppCompatActivity implements InvalidDetectionDialogFragment.IDetectionFailedDialogListener {
    private static PictureResult picture;
    private ImageProcessor imageProcessor;

    public static void setPictureResult(@Nullable PictureResult pictureResult){
        picture = pictureResult;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.picture_preview_activity);

        Toolbar toolbar = findViewById(R.id.PicturePreviewToolbarID);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(v -> {
            onBackPressed();
        });

        System.loadLibrary("native-lib");
        final PictureResult result = picture;
        if(result == null){
            finish();
            return;
        }
        imageProcessor = new ImageProcessor();
        final ImageView imageView = findViewById(R.id.imageViewID);
        try {
            Size resultSize = result.getSize();
            result.toBitmap(resultSize.getWidth(), resultSize.getHeight(), bitmap -> {
                //TODO: Image recognition here
                //TODO: Handle error msg -> Display InvalidDetectionDialog
                /*Mat mat = new Mat();
                Utils.bitmapToMat(bitmap, mat);
                ImageProcessor imageProcessor = new ImageProcessor(mat, ShowPictureActivity.this);
                Slot[] slots = imageProcessor.PatternDetector(mat.getNativeObjAddr());
                for(Slot slot : slots){
                    Log.println(Log.INFO, "slot", slot.row + " | " + slot.col + " - " + slot.info);
                }*/
                findViewById(R.id.LoadingPanelId).setVisibility(View.GONE);
                imageView.setImageBitmap(bitmap);
            });
        } catch (UnsupportedOperationException e){
            imageView.setImageDrawable(new ColorDrawable(Color.RED));
            Toast.makeText(this, "Cant show pic format:" + picture.getFormat(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(!isChangingConfigurations()){
            setPictureResult(null);
        }
    }

    private void ShowInvalidDetectionDialog(){
        InvalidDetectionDialogFragment dialogFragment = new InvalidDetectionDialogFragment();
        dialogFragment.show(getSupportFragmentManager(), "InvalidDetectionDialog");
    }

    /*Invalid detection listener*/
    @Override
    public void onCaptureAgain() {
        //TODO: Back to CamActivity
    }

    @Override
    public void onUserHandle() {
        //TODO: Custom creation
    }

    @Override
    public void onCancel() {
        //TODO: Return to GameActivity
    }
}
