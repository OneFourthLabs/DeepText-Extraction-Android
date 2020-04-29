package cv_engine;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.Size;
import android.widget.Toast;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import cv_engine.detection.EastTextDetector;
import cv_engine.recognition.MORAN_Recognizer;
import utils.CvUtils;
import utils.ImageUtils;

public class TextExtractor {

    EastTextDetector detector;
    MORAN_Recognizer recognizer;

    public TextExtractor(Size detectorInpSiz, String eastDetectorModel, String moranRecognizerModel) {
        detector = new EastTextDetector(eastDetectorModel, detectorInpSiz.getWidth(), detectorInpSiz.getHeight());
        recognizer = new MORAN_Recognizer(moranRecognizerModel);
    }

    List<ArrayList<Bitmap>> getDetections(Bitmap imageBitmap, String saveDetectionTo, Context context) {
        // Resize image to pass to detector model
        imageBitmap = ImageUtils.letterboxResizeBitmap(imageBitmap, (int) detector.input_size.width, (int) detector.input_size.height);

        Mat inputImg = CvUtils.convertBitmapARGB2MatRGB(imageBitmap);
        Mat outputImg = inputImg.clone();

        // Get the detections
        Point[][] bBoxes = detector.detect(outputImg);

        // Save the detection
        if (saveDetectionTo != null && context != null) {
            Utils.matToBitmap(outputImg, imageBitmap);
            ImageUtils.saveBitmapToAppDirectoryAsJPG(imageBitmap, saveDetectionTo, context);
        }

        List<ArrayList<Mat>> textCrops = detector.getFixedCropImages(bBoxes, inputImg, recognizer.inputSize.getWidth(), recognizer.inputSize.getHeight());
        List<ArrayList<Bitmap>> textBitmaps = new ArrayList<ArrayList<Bitmap>>();

        for (ArrayList<Mat> row : textCrops) {
            ArrayList<Bitmap> currentRow = new ArrayList<Bitmap> ();
            for (Mat mat : row) {
                Bitmap bmp = Bitmap.createBitmap(recognizer.inputSize.getWidth(), recognizer.inputSize.getHeight(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(mat, bmp);
                currentRow.add(bmp);
            }
            textBitmaps.add(currentRow);
        }

        return textBitmaps;
    }

    public List<ArrayList<String>> extractText(Bitmap imageBitmap, String saveDetectionTo, Context context) {
        long start = System.currentTimeMillis();
        Toast.makeText(context, "Detecting Text in image...", Toast.LENGTH_SHORT).show();
        List<ArrayList<Bitmap>> textCrops = getDetections(imageBitmap, saveDetectionTo, context);
        Log.d("DETECTION", ""+(System.currentTimeMillis()-start)/1000.0);

        start = System.currentTimeMillis();
        Toast.makeText(context, "Recognizing " + textCrops.size() + " text regions detected...", Toast.LENGTH_SHORT).show();
        List<ArrayList<String>> outputs = recognizer.bulkPredict(textCrops, 0.0f);
        Log.d("RECOGNITION", ""+(System.currentTimeMillis()-start)/1000.0);

        return outputs;
    }

    public String outputToString(List<ArrayList<String>> outputs) {
        StringBuilder finalOutput = new StringBuilder("");
        for (ArrayList<String> row : outputs) {
            for (String str : row)
                finalOutput.append(str).append('\t');
            finalOutput.append('\n');
        }
        return finalOutput.toString();
    }

}
