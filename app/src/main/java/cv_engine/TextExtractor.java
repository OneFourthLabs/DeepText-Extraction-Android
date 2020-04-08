package cv_engine;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Size;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import cv_engine.detection.EastTextDetector;
import cv_engine.recognition.MORAN_Recognizer;
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

        // Convert Bitmap ARGB to RGB Mat: https://stackoverflow.com/a/60724380
        Mat inputImg = new Mat();
        Utils.bitmapToMat(imageBitmap, inputImg);
        Imgproc.cvtColor(inputImg, inputImg, Imgproc.COLOR_RGBA2RGB);
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

        List<ArrayList<Bitmap>> textCrops = getDetections(imageBitmap, saveDetectionTo, context);

        List<ArrayList<String>> outputs = new ArrayList<> ();
        for (ArrayList<Bitmap> row : textCrops) {
            ArrayList<String> currentPredictionRow = new ArrayList<> ();
            for (Bitmap bmp : row) {
                String output = recognizer.predict(bmp);
                currentPredictionRow.add(output);
            }
            outputs.add(currentPredictionRow);
        }

        return outputs;
    }

}
