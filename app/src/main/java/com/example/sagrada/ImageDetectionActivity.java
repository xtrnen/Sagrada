package com.example.sagrada;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.opencv.android.OpenCVLoader;

public class ImageDetectionActivity extends AppCompatActivity {
    private static Bitmap picture;
    private Button confirmBtn;
    private Button declineBtn;
    private ImageView imageView;
    private int requestCode;

    public static void setPictureResult(@Nullable Bitmap pictureResult){
        picture = pictureResult;
    }

    static {
        if (!OpenCVLoader.initDebug()) {
            // Handle initialization error
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_detection_layout);

        requestCode = getIntent().getIntExtra("RequestCode", 0);

        Toolbar toolbar = findViewById(R.id.DetectionToolbarID);
        confirmBtn = findViewById(R.id.confirmDetBtnID);
        declineBtn = findViewById(R.id.declineDetBtnID);
        imageView = findViewById(R.id.DetectedViewID);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        declineBtn.setOnClickListener(v -> {//TODO: Blbost
            });
        confirmBtn.setOnClickListener(v -> {
            //TODO: return
        });

        imageView.setImageBitmap(picture);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(!isChangingConfigurations()){
            setPictureResult(null);
        }
    }

    private void sendDataBack(){

    }
}