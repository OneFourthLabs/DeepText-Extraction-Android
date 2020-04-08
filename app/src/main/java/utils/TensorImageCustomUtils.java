package utils;

import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.media.Image;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Locale;
import org.pytorch.Tensor;

public class TensorImageCustomUtils {

    public static Tensor bitmapGrayscaleToFloat32Tensor(
            final Bitmap bitmap, final float normMean, final float normStd) {

        return bitmapGrayscaleToFloat32Tensor(
                bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), normMean, normStd);
    }

    private static void checkGrayscaleOutBufferCapacity(
            FloatBuffer outBuffer, int outBufferOffset, int tensorWidth, int tensorHeight) {
        if (outBufferOffset + tensorWidth * tensorHeight > outBuffer.capacity()) {
            throw new IllegalStateException("Buffer underflow");
        }
    }

    public static void bitmapGrayscaleToFloatBuffer(
            final Bitmap bitmap,
            final int x,
            final int y,
            final int width,
            final int height,
            final float normMean,
            final float normStd,
            final FloatBuffer outBuffer,
            final int outBufferOffset) {
        checkGrayscaleOutBufferCapacity(outBuffer, outBufferOffset, width, height);

        final int pixelsCount = height * width;
        final int[] pixels = new int[pixelsCount];
        bitmap.getPixels(pixels, 0, width, x, y, width, height);
        for (int i = 0; i < pixelsCount; i++) {
            final int c = pixels[i];
            float r = ((c >> 16) & 0xff) / 255.0f;
            float rF = (r - normMean) / normStd;
            outBuffer.put(outBufferOffset + i, rF);
        }
    }

    public static Tensor bitmapGrayscaleToFloat32Tensor(
            final Bitmap bitmap,
            int x,
            int y,
            int width,
            int height,
            float normMean,
            float normStd) {

        final FloatBuffer floatBuffer = Tensor.allocateFloatBuffer(width * height);
        bitmapGrayscaleToFloatBuffer(bitmap, x, y, width, height, normMean, normStd, floatBuffer, 0);
        return Tensor.fromBlob(floatBuffer, new long[] {1, 1, height, width});
    }

}
