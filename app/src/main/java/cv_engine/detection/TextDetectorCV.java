package cv_engine.detection;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public abstract class TextDetectorCV {

    String modelPath;
    final int Y_THRESH_MAX = 10;

    public TextDetectorCV() {
    }

    public TextDetectorCV(String modelPath) {
        this.modelPath = modelPath;
    }

    public abstract Point[][] detect(Mat image);

    /**
     * To extract a slanted bounding box from an image as a rectangle image
     * References:
     * Concept - https://stackoverflow.com/a/57209955
     * Perspective Transform in Java - https://stackoverflow.com/questions/40688491/
     *
     * @param vertices The slanted rect vertices in clockwise from bottom-left
     * @param frame The input frame from which rectangle is to be cropped
     * @return Mat object of the cropped region
     */
    private Mat cropSlantRect(Point[] vertices, Mat frame, int w, int h) {
        MatOfPoint2f src = new MatOfPoint2f(
                vertices[0],
                vertices[1],
                vertices[2],
                vertices[3]
        );

        MatOfPoint2f dst = new MatOfPoint2f(
                new Point(0, h-1),
                new Point(0,0),
                new Point(w-1,0),
                new Point(w-1,h-1)
        );

        Mat warpMat = Imgproc.getPerspectiveTransform(src, dst);
        Mat destImage = new Mat();
        Imgproc.warpPerspective(frame, destImage, warpMat, new Size(w,h));

        return destImage;
    }

    /**
     * Sort a given list of rectangles using their list of 4 vertices
     * @param vertices Rectangle Vertices. Dimension: [Num_rects, 4]
     */
    void sortRectanglesFromPoints(Point[][] vertices) {
        // Inplace Insertion Sort, Row Major
        for (int i = 0; i < vertices.length-1; ++i) {
            int min_j = i;
            for (int j = i+1; j < vertices.length; ++j) {
                if((vertices[j][1].y < vertices[min_j][1].y && Math.abs(vertices[j][1].y - vertices[min_j][1].y) > Y_THRESH_MAX)
                        || (Math.abs(vertices[j][1].y - vertices[min_j][1].y) <= Y_THRESH_MAX && vertices[j][1].x < vertices[min_j][1].x)) {
                    min_j = j;
                }
            }
            if (min_j != i) {
                Point[] tmp = vertices[i];
                vertices[i] = vertices[min_j];
                vertices[min_j] = tmp;
            }
        }
    }

    public List<ArrayList<Mat>> getFixedCropImages(Point[][] rectVertices, Mat frame, int w, int h) {

        List<ArrayList<Mat>> crops = new ArrayList<ArrayList<Mat>> ();
        if (rectVertices == null)
            return crops;

        // vertex[0] contains bottom-left point, vertex[1] top-left point, ... (clockwise)
        sortRectanglesFromPoints(rectVertices);
        double last_x = 0, last_y = 0;
        ArrayList<Mat> currentRow = new ArrayList<Mat> ();
        for (int i = 0; i < rectVertices.length; ++i) {
            Mat crop = cropSlantRect(rectVertices[i], frame, w, h);
            if (rectVertices[i][1].x < last_x && Math.abs(rectVertices[i][1].y - last_y) >= Y_THRESH_MAX) {
                crops.add(currentRow);
                currentRow = new ArrayList<Mat> ();
            }
            currentRow.add(crop);
            last_x = rectVertices[i][1].x;
            last_y = rectVertices[i][1].y;
        }
        crops.add(currentRow);
        return crops;
    }

}
