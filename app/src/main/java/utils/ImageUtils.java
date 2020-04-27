package utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageUtils {

    public static boolean saveBitmapToAppDirectoryAsPNG(Bitmap bitmapImage, String filename, Context context) {
        File externalFilesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File savePath = new File(externalFilesDir, filename.toLowerCase().endsWith(".png") ? filename : filename+".png");
        return saveBitmap(bitmapImage, savePath, Bitmap.CompressFormat.PNG);
    }

    public static boolean saveBitmapToAppDirectoryAsJPG(Bitmap bitmapImage, String filename, Context context) {
        File externalFilesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File savePath = new File(externalFilesDir, filename.toLowerCase().endsWith(".jpg") ? filename : filename+".jpg");
        return saveBitmap(bitmapImage, savePath, Bitmap.CompressFormat.JPEG);
    }

    private static boolean saveBitmap(Bitmap bitmapImage, File savePath, Bitmap.CompressFormat format) {
        // Taken from: https://stackoverflow.com/a/17674787/
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(savePath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(format, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    public static Matrix getOrientationMatrix(String path) {
        // Credits: https://stackoverflow.com/q/40000782
        Matrix matrix = new Matrix();
        ExifInterface exif;
        try {
            exif = new ExifInterface(path);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                    matrix.setScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.setRotate(180);
                    break;
                case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                    matrix.setRotate(180);
                    matrix.postScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_TRANSPOSE:
                    matrix.setRotate(90);
                    matrix.postScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.setRotate(90);
                    break;
                case ExifInterface.ORIENTATION_TRANSVERSE:
                    matrix.setRotate(-90);
                    matrix.postScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.setRotate(-90);
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return matrix;
    }

    public static Bitmap readBitmapFromAppDirectory(String filename, Context context) {
        File externalFilesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File readPath = new File(externalFilesDir, filename);
        return readBitmapFromImage(readPath);
    }

    private static Bitmap readBitmapFromImage(File readPath) {
        try {
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(readPath));
            // Adjust orientation
            b = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), getOrientationMatrix(readPath.getAbsolutePath()), false);
            return b;
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static Bitmap letterboxResizeBitmap(Bitmap originalImage, int width, int height) {
        // Source: https://stackoverflow.com/a/15441311
        Bitmap background = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        float originalWidth = originalImage.getWidth();
        float originalHeight = originalImage.getHeight();

        Canvas canvas = new Canvas(background);

        float scale = width / originalWidth;

        float xTranslation = 0.0f;
        float yTranslation = (height - originalHeight * scale) / 2.0f;

        Matrix transformation = new Matrix();
        transformation.postTranslate(xTranslation, yTranslation);
        transformation.preScale(scale, scale);

        Paint paint = new Paint();
        paint.setFilterBitmap(true);

        canvas.drawBitmap(originalImage, transformation, paint);

        return background;
    }

    public static Bitmap bitmapToGrayscale(Bitmap bmpOriginal) {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }
}
