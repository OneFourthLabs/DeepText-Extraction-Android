package cv_engine.detection;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfRotatedRect;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

public class EastTextDetector extends TextDetectorCV {
	// Java code inspired from: https://gist.github.com/berak/788da80d1dd5bade3f878210f45d6742
	// Reference CPP: https://github.com/opencv/opencv/blob/master/samples/dnn/text_detection.cpp

    private Net model;
    public final Size input_size;

    public EastTextDetector(String modelPath) {
        this(modelPath, 320, 320);
    }

    public EastTextDetector(String modelPath, int width, int height) {
        super(modelPath);
        input_size = new Size(width, height);
        // Model from https://github.com/argman/EAST
        // You can find it here : https://github.com/opencv/opencv_extra/blob/master/testdata/dnn/download_models.py#L309
        model = Dnn.readNetFromTensorflow(modelPath);
    }

    public Mat preprocess(Mat frame) {
        // binarizeInRGB(frame).copyTo(frame); // Increases FP's drastically, have to tune parameters for each case
        Mat blob = Dnn.blobFromImage(frame, 1.0, input_size, new Scalar(123.68, 116.78, 103.94), true, false);
        return blob;
    }

    public Point[][] detect(Mat frame) {
        return detect(frame, 0.5f, 0.4f);
    }

    public Point[][] detect(Mat frame, float scoreThresh, float nmsThresh) {

        int W = (int)(input_size.width / 4); // width of the output geometry  / score maps
        int H = (int)(input_size.height / 4); // height of those. the geometry has 4, vertically stacked maps, the score one 1
        Mat blob = preprocess(frame);
        model.setInput(blob);

        List<Mat> outs = new ArrayList<>(2);
        List<String> outNames = new ArrayList<String>();
        outNames.add("feature_fusion/Conv_7/Sigmoid");
        outNames.add("feature_fusion/concat_3");
        model.forward(outs, outNames);

        // Decode predicted bounding boxes.
        Mat scores = outs.get(0).reshape(1, H);
        // My lord and savior : http://answers.opencv.org/question/175676/javaandroid-access-4-dim-mat-planes/
        Mat geometry = outs.get(1).reshape(1, 5 * H); // don't hardcode it !
        List<Float> confidencesList = new ArrayList<>();
        List<RotatedRect> boxesList = decodeOutput(scores, geometry, confidencesList, scoreThresh);
        if (confidencesList.size() < 1)
            return null;

        // Apply non-maximum suppression procedure.
        MatOfFloat confidences = new MatOfFloat(Converters.vector_float_to_Mat(confidencesList));
        RotatedRect[] boxesArray = boxesList.toArray(new RotatedRect[0]);
        MatOfRotatedRect boxes = new MatOfRotatedRect(boxesArray);
        MatOfInt indices = new MatOfInt();
        Dnn.NMSBoxesRotated(boxes, confidences, scoreThresh, nmsThresh, indices);

        // Render detections
        Point ratio = new Point((float)frame.cols()/input_size.width, (float)frame.rows()/input_size.height);
        int[] indexes = indices.toArray();
        Point[][] listOfVertices = new Point[indexes.length][];
        for(int i = 0; i<indexes.length;++i) {
            RotatedRect rot = boxesArray[indexes[i]];
            Point[] vertices = new Point[4];
            rot.points(vertices);
            for (int j = 0; j < 4; ++j) {
                vertices[j].x *= ratio.x;
                vertices[j].y *= ratio.y;
            }
            listOfVertices[i] = vertices;
            for (int j = 0; j < 4; ++j) {
                Imgproc.line(frame, vertices[j], vertices[(j + 1) % 4], new Scalar(0, 0,255), 1);
            }
        }

        return listOfVertices;
    }

    private static List<RotatedRect> decodeOutput(Mat scores, Mat geometry, List<Float> confidences, float scoreThresh) {
        // size of 1 geometry plane
        int W = geometry.cols();
        int H = geometry.rows() / 5;
        //System.out.println(geometry);
        //System.out.println(scores);

        List<RotatedRect> detections = new ArrayList<>();
        for (int y = 0; y < H; ++y) {
            Mat scoresData = scores.row(y);
            Mat x0Data = geometry.submat(0, H, 0, W).row(y);
            Mat x1Data = geometry.submat(H, 2 * H, 0, W).row(y);
            Mat x2Data = geometry.submat(2 * H, 3 * H, 0, W).row(y);
            Mat x3Data = geometry.submat(3 * H, 4 * H, 0, W).row(y);
            Mat anglesData = geometry.submat(4 * H, 5 * H, 0, W).row(y);

            for (int x = 0; x < W; ++x) {
                double score = scoresData.get(0, x)[0];
                if (score >= scoreThresh) {
                    double offsetX = x * 4.0;
                    double offsetY = y * 4.0;
                    double angle = anglesData.get(0, x)[0];
                    double cosA = Math.cos(angle);
                    double sinA = Math.sin(angle);
                    double x0 = x0Data.get(0, x)[0];
                    double x1 = x1Data.get(0, x)[0];
                    double x2 = x2Data.get(0, x)[0];
                    double x3 = x3Data.get(0, x)[0];
                    double h = x0 + x2;
                    double w = x1 + x3;
                    Point offset = new Point(offsetX + cosA * x1 + sinA * x2, offsetY - sinA * x1 + cosA * x2);
                    Point p1 = new Point(-1 * sinA * h + offset.x, -1 * cosA * h + offset.y);
                    Point p3 = new Point(-1 * cosA * w + offset.x,      sinA * w + offset.y); // original trouble here !
                    RotatedRect r = new RotatedRect(new Point(0.5 * (p1.x + p3.x), 0.5 * (p1.y + p3.y)), new Size(w, h), -1 * angle * 180 / Math.PI);
                    detections.add(r);
                    confidences.add((float) score);
                }
            }
        }
        return detections;
    }
}
