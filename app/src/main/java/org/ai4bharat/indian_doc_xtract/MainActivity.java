package org.ai4bharat.indian_doc_xtract;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;

import cv_engine.detection.EastTextDetector;
import utils.ImageUtils;
import utils.ImgCaptureHandler;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

import static org.ai4bharat.indian_doc_xtract.Constants.*;


public class MainActivity extends AppCompatActivity {

    private static final String  TAG              = "MainActivity";

    //    MORAN_Recognizer classifier;
    EastTextDetector detector;
    ImgCaptureHandler imgCaptureHandler;
    boolean isOpenCvInitialized;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    detector = new EastTextDetector(AndroidUtils.assetFilePath(MainActivity.this,"frozen_east_text_detection.pb"), DETECTION_INPUT_SIZE.getWidth(), DETECTION_INPUT_SIZE.getHeight());
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
            isOpenCvInitialized = true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        imgCaptureHandler = new ImgCaptureHandler(true, getApplicationContext());

//        classifier = new MORAN_Recognizer(Utils.assetFilePath(this,"moran.pt"));

        Button capture = findViewById(R.id.capture);

        capture.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                try{
                    Intent cameraIntent = imgCaptureHandler.getTakePictureIntent(MainActivity.this);
                    startActivityForResult(cameraIntent, ImgCaptureHandler.REQUEST_TAKE_PHOTO);
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "ERROR: Unable to start camera", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, e.getMessage());
                }



//                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                startActivityForResult(cameraIntent,cameraRequestCode);
            }
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public void onResume()
    {
        super.onResume();
        // TODO: Loads each time??
        if (isOpenCvInitialized)
            return;
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){

        if(requestCode == ImgCaptureHandler.REQUEST_TAKE_PHOTO && resultCode == RESULT_OK){
            Log.d(TAG, "Getting image...");

            Intent resultView = new Intent(this, Result.class);

            Bitmap imageBitmap = imgCaptureHandler.getPic();
            imageBitmap = ImageUtils.letterboxResizeBitmap(imageBitmap, (int) detector.input_size.width, (int) detector.input_size.height);

            // Convert Bitmap ARGB to RGB Mat: https://stackoverflow.com/a/60724380
            Mat inputImg = new Mat();
            Utils.bitmapToMat(imageBitmap, inputImg);
            Imgproc.cvtColor(inputImg, inputImg, Imgproc.COLOR_RGBA2RGB);

            Log.d(TAG, "Predicting image...");
//            String pred = classifier.predict(imageBitmap);
            detector.detect(inputImg);
            Utils.matToBitmap(inputImg, imageBitmap);
            Log.d(TAG, "Saving image...");
            ImageUtils.saveBitmapToAppDirectoryAsJPG(imageBitmap, "result.jpg", getApplicationContext());

            String pred = "AI4Bharat";
            resultView.putExtra("pred",pred);
            resultView.putExtra("result_path", "result.jpg");
            Log.d(TAG, "Displaying image...");
            startActivity(resultView);

        }

    }
}
