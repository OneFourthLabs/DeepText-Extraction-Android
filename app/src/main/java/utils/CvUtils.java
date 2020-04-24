package utils;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class CvUtils {

    public static Mat rgb2gray(Mat source) {
        Mat destination = new Mat();
        Imgproc.cvtColor(source, destination, Imgproc.COLOR_RGB2GRAY);
        return destination;
    }

    public static Mat gray2rgb(Mat source) {
        Mat destination = new Mat();
        Imgproc.cvtColor(source, destination, Imgproc.COLOR_GRAY2RGB);
        return destination;
    }

    public static Mat binarizeLocalAdaptiveGaussian(Mat source) {
        return binarizeLocalAdaptiveGaussian(source, 11, 3);
    }

    public static Mat binarizeLocalAdaptiveGaussian(Mat source, int neighborhood, int meanSubtract) {
        Mat destination = new Mat();
        Imgproc.adaptiveThreshold(source, destination, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
                Imgproc.THRESH_BINARY, neighborhood, meanSubtract);
        return destination;
    }

    public static Mat binarizeGaussianOtsu(Mat source) {
        Mat tmp = new Mat();
        Imgproc.GaussianBlur(source, tmp, new Size(5,5), 0, 0);
        Mat destination = new Mat();
         Imgproc.threshold(tmp, destination, 0, 255, Imgproc.THRESH_BINARY|Imgproc.THRESH_OTSU);
         return destination;
    }

}
