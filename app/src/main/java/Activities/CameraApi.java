package Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
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

import Model.GameBoard.Structs.Dice;
import Model.GameBoard.Structs.Slot;
import Model.ImageProcessor;


public class CameraApi extends AppCompatActivity {
    ImageView imageView;
    Button takePattern;
    Button takeDice;

    int captureMode = 0;

    static final int REQUEST_IMAGE_CAPTURE = 1;

    String currentPhotoPath;

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test);
        System.loadLibrary("native-lib");

        imageView = (ImageView)findViewById(R.id.image);
        takePattern = (Button)findViewById(R.id.btn_patternPhoto);
        takeDice = (Button)findViewById(R.id.btn_dicePhoto);

        takePattern.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                captureMode = 0;
                dispatchTakePictureIntent();
            }
        });

        takeDice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                captureMode = 1;
                dispatchTakePictureIntent();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK){
            Bitmap image = BitmapFactory.decodeFile(currentPhotoPath);

            ExifInterface exif;
            try {
                exif = new ExifInterface(currentPhotoPath);
                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                switch (orientation) {
                    case androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_270:
                        Log.println(Log.INFO, "exif", "270");
                        break;
                    case androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_180:
                        Log.println(Log.INFO, "exif", "180");
                        break;
                    case androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_90:
                        Log.println(Log.INFO, "exif", "90");
                        break;
                    default:
                        Log.println(Log.INFO, "exif", "??");
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            Mat retImg = new Mat();
            Mat in = new Mat();

            Utils.bitmapToMat(image, in);

            ImageProcessor imgProcessor = new ImageProcessor(in, this);

            ImageView ivPhoto;
            if(captureMode == 0){
                Slot[] slots = imgProcessor.PatternDetector(retImg.getNativeObjAddr());

                if(slots.length != 20){
                    Log.println(Log.ERROR, "Slot array", "Length doesn't fit");
                }
                for(Slot slot : slots){
                    Log.println(Log.INFO, "slot", slot.row + " | " + slot.col + " - " + slot.info);
                }

                ivPhoto = findViewById(R.id.patternImage);
                ivPhoto.setImageBitmap(MainActivity.convMatToBitmap(retImg));
            }
            else{
                Utils.bitmapToMat(image, in);
                imgProcessor.AddDiceImg(in);
                Dice[] dices = imgProcessor.DiceDetector(retImg.getNativeObjAddr());
                if(dices.length ==0){
                    Log.println(Log.ERROR, "Dice array", "No dice!");
                }
                for(Dice dice : dices){
                    Log.println(Log.INFO, "dice", dice.row + " | " + dice.col + " - " + dice.number + " | " + dice.color);
                }

                ivPhoto = findViewById(R.id.diceImage);
                ivPhoto.setImageBitmap(MainActivity.convMatToBitmap(retImg));
            }
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