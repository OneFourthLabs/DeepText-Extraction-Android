package utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
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

    public static Bitmap readBitmapFromAppDirectory(String filename, Context context) {
        File externalFilesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File readPath = new File(externalFilesDir, filename);
        return readBitmapFromImage(readPath);
    }

    private static Bitmap readBitmapFromImage(File readPath) {
        try {
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(readPath));
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
}
