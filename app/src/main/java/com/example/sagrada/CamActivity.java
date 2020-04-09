package com.example.sagrada;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.otaliastudios.cameraview.CameraException;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraOptions;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.PictureResult;

import java.util.ArrayList;

import Model.GameBoard.Structs.Slot;

public class CamActivity extends AppCompatActivity implements View.OnClickListener {
    private CameraView cameraView;
    public static int VALID_PREVIEW = 10;
    private int requestC;

    CameraListener cameraListener = new CameraListener() {
        @Override
        public void onCameraOpened(@NonNull CameraOptions options) {
            super.onCameraOpened(options);
        }

        @Override
        public void onCameraClosed() {
            super.onCameraClosed();
        }

        @Override
        public void onCameraError(@NonNull CameraException exception) {
            super.onCameraError(exception);
        }

        @Override
        public void onPictureTaken(@NonNull PictureResult result) {
            super.onPictureTaken(result);
            ShowPictureActivity.setPictureResult(result);
            Intent intent = new Intent(CamActivity.this, ShowPictureActivity.class);
            intent.putExtra("Data", requestC);
            startActivityForResult(intent, VALID_PREVIEW);
        }

        @Override
        public void onOrientationChanged(int orientation) {
            super.onOrientationChanged(orientation);
        }
    };

    @Override
    public void onClick(View view){
        switch (view.getId()){
            case R.id.takePicBtn: takePicture(); break;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cam_layout);
        Toolbar toolbar = findViewById(R.id.CameraToolbarID);
        requestC = getIntent().getIntExtra("Data", 0);
        if(requestC == 0){
            finish();
        }
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> {
            /*Intent data = new Intent();
            ArrayList<Slot> slots = new ArrayList<Slot>();
            slots.add(new Slot("RED", 1,1));
            data.putParcelableArrayListExtra("Slots", slots);
            setResult(REQUEST_SLOTS, data);
            finish();*/
            onBackPressed();
        });

        cameraView = findViewById(R.id.camViewID);
        Button takePictureBtn = findViewById(R.id.takePicBtn);

        cameraView.setLifecycleOwner(this);
        cameraView.addCameraListener(cameraListener);
        takePictureBtn.setOnClickListener(this);
    }

    private void takePicture(){
        if(cameraView.isTakingPicture()) return;
        cameraView.takePicture();
    }

    //Permissions
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean valid = true;
        for(int grant : grantResults){
            valid = valid && grant == PackageManager.PERMISSION_GRANTED;
        }
        if(valid && !cameraView.isOpened()){
            cameraView.open();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == VALID_PREVIEW){
            Intent retData = new Intent();
            if(requestC == GameActivity.REQUEST_SLOTS){
                retData.putParcelableArrayListExtra("slots", data.getParcelableArrayListExtra("slots"));
            }
            if(requestC == GameActivity.REQUEST_DICES){
                retData.putParcelableArrayListExtra("dices", data.getParcelableArrayListExtra("dices"));
            }

            setResult(requestC, retData);
            finish();
        }
    }
}
