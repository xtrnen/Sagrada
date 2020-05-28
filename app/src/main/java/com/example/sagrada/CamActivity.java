package com.example.sagrada;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.otaliastudios.cameraview.CameraException;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraOptions;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.PictureResult;
import com.otaliastudios.cameraview.overlay.OverlayLayout;

public class CamActivity extends AppCompatActivity implements View.OnClickListener {
    private CameraView cameraView;
    public static int VALID_PREVIEW = 10;
    private int requestC;
    private ImageView frame;
    static final int CROP_WIDTH = 1080;
    static final int CROP_HEIGHT = 720;
    public static int CAPTURE_AGAIN = 1;

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
            int[] location = new int[2];
            frame.getLocationOnScreen(location);
            int[] view = new int[2];
            cameraView.getLocationOnScreen(view);
            ShowPictureActivity.setRectCoord(location[0], location[1] - view[1], cameraView.getWidth(), cameraView.getHeight());
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
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        cameraView = findViewById(R.id.camViewID);

        frame = new ImageView(this);
        frame.setImageDrawable(getDrawable(R.drawable.camera_frame));

        OverlayLayout.LayoutParams params = new OverlayLayout.LayoutParams(CROP_WIDTH, CROP_HEIGHT);
        params.drawOnPreview = true;
        params.gravity = Gravity.CENTER;
        frame.setLayoutParams(params);
        cameraView.addView(frame);

        Button takePictureBtn = findViewById(R.id.takePicBtn);

        cameraView.setLifecycleOwner(this);
        cameraView.addCameraListener(cameraListener);
        takePictureBtn.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
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
        if (resultCode == VALID_PREVIEW) {
            Intent retData = new Intent();
            if (requestC == GameActivity.REQUEST_SLOTS) {
                retData.putParcelableArrayListExtra(GameActivity.DATA_SLOTS, data.getParcelableArrayListExtra(GameActivity.DATA_SLOTS));
            }
            if (requestC == GameActivity.REQUEST_DICES) {
                retData.putParcelableArrayListExtra(GameActivity.DATA_DICES, data.getParcelableArrayListExtra(GameActivity.DATA_DICES));

            }
            setResult(requestC, retData);
            finish();
        } else if( resultCode == CAPTURE_AGAIN){
            //Do nothing
        } else if( resultCode == GameActivity.REQUEST_INFO_ACTIVITY){
            Intent ret = new Intent();
            if (requestC == GameActivity.REQUEST_SLOTS) {
                ret.putParcelableArrayListExtra(GameActivity.DATA_SLOTS, data.getParcelableArrayListExtra(GameActivity.DATA_SLOTS));
            }
            if (requestC == GameActivity.REQUEST_DICES) {
                ret.putParcelableArrayListExtra(GameActivity.DATA_DICES, data.getParcelableArrayListExtra(GameActivity.DATA_DICES));

            }
            setResult(GameActivity.REQUEST_INFO_ACTIVITY, ret);
            finish();
        }
    }
}
