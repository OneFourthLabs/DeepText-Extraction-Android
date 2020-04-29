package org.ai4bharat.indian_doc_xtract;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Trace;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cv_engine.QrDetectorBoofCV;
import cv_engine.TextExtractor;
import cv_engine.detection.EastTextDetector;
import indian_docs_processor.DocProcessor;
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
    String captureMode;

    TextExtractor textExtractor;
    QrDetectorBoofCV qrDetector;
    ImgCaptureHandler imgCaptureHandler;
    boolean isOpenCvInitialized;

    Spinner spinnerChooseDoc;

    boolean loadModels() {
        textExtractor = new TextExtractor(DETECTION_INPUT_SIZE,
                AndroidUtils.assetFilePath(MainActivity.this,"frozen_east_text_detection.pb"),
                AndroidUtils.assetFilePath(MainActivity.this,"moran.pt"));
        qrDetector = new QrDetectorBoofCV();
        return true;
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    loadModels();
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
        setupUI();
    }

    void setupUI() {
        spinnerChooseDoc = findViewById(R.id.spinner_choose_doc);
        ArrayList<String> docCategories = new ArrayList<String>(Arrays.asList(Constants.DOC_TYPES));
        docCategories.addAll(Arrays.asList(DocProcessor.DOCS_SUPPORTED));
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, docCategories);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerChooseDoc.setAdapter(dataAdapter);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    public void btnCapture(View view) {
        captureMode = String.valueOf(spinnerChooseDoc.getSelectedItem()).toLowerCase();
        try{
            Intent cameraIntent = imgCaptureHandler.getTakePictureIntent(MainActivity.this);
            startActivityForResult(cameraIntent, ImgCaptureHandler.REQUEST_TAKE_PHOTO);
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "ERROR: Unable to start camera", Toast.LENGTH_SHORT).show();
            Log.d(TAG, e.getMessage());
        }
    }

    @Override
    public void onResume() {
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == ImgCaptureHandler.REQUEST_TAKE_PHOTO && resultCode == RESULT_OK){
            Log.d(TAG, "Getting image...");

            Intent resultView = new Intent(this, Result.class);

            Bitmap imageBitmap = imgCaptureHandler.getPic();
            String saveDetectionTo = "result.jpg";

            Log.d(TAG, "Processing image...");
            String prediction = "---NO PREDICTIONS---";
            if (captureMode.contains("qr")) {
                Toast.makeText(getApplicationContext(), "Detecting text from QR code...", Toast.LENGTH_SHORT).show();
                List<String> outputs = qrDetector.detectMessages(imageBitmap);
                prediction = outputs.isEmpty() ? "---NO QR FOUND---" : outputs.get(0);
                saveDetectionTo = imgCaptureHandler.currentPhotoName;
            } else {
                List<ArrayList<String>> outputs = textExtractor.extractText(imageBitmap, saveDetectionTo, getApplicationContext());
                prediction = textExtractor.outputToString(outputs);
            }

            resultView.putExtra("pred", prediction);
            resultView.putExtra("docType", captureMode);
            resultView.putExtra("result_path", saveDetectionTo);
            Log.d(TAG, "Displaying result...");
            startActivity(resultView);

        }

    }
}
