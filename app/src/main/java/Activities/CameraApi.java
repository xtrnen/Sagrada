package Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.example.sagrada.MainActivity;
import com.example.sagrada.R;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import Model.GameBoard.Structs.Slot;
import Model.ImageProcessor;


public class CameraApi extends AppCompatActivity {
    ImageView imageView;
    Button takePicture;

    static final int REQUEST_IMAGE_CAPTURE = 1;

    String currentPhotoPath;

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        System.loadLibrary("native-lib");

        imageView = (ImageView)findViewById(R.id.image);
        takePicture = (Button)findViewById(R.id.btn_takepicture);

        takePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK){
            Bitmap image = BitmapFactory.decodeFile(currentPhotoPath);

            Mat retImg = new Mat();
            Mat in = new Mat();

            Utils.bitmapToMat(image, in);

            ImageProcessor imgProcessor = new ImageProcessor(in, this);
            Slot[] slots = imgProcessor.PatternDetector(retImg.getNativeObjAddr());

            if(slots.length != 20){
                Log.println(Log.ERROR, "Slot array", "Length doesn't fit");
            }
            for(Slot slot : slots){
                Log.println(Log.INFO, "slot", slot.row + " | " + slot.col + " - " + slot.info);
            }

            ImageView ivPhoto;
            ivPhoto = findViewById(R.id.image);
            ivPhoto.setImageBitmap(image);
        }
    }

    private void dispatchTakePictureIntent(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(takePictureIntent.resolveActivity(getPackageManager()) != null){
            File photoFile = null;
            try {
                photoFile = createImgFile();
            } catch (IOException er){
                Log.println(Log.ERROR, "Create Photo", er.getMessage());
            }

            if(photoFile != null){
                Uri photoUri = FileProvider.getUriForFile(this, "com.example.sagrada.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImgFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_"+timeStamp+"_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File img = File.createTempFile(imageFileName, ".jpg",storageDir);

        currentPhotoPath = img.getAbsolutePath();
        return img;
    }
}