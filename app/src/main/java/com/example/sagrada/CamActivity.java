package com.example.sagrada;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.otaliastudios.cameraview.CameraException;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraOptions;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.PictureResult;

public class CamActivity extends AppCompatActivity implements View.OnClickListener {
    private Button takePictureBtn;
    private CameraView cameraView;
    private Bitmap resultBitmap;

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
            startActivity(intent);
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
        cameraView = (CameraView) findViewById(R.id.camViewID);
        takePictureBtn = (Button) findViewById(R.id.takePicBtn);

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
}
