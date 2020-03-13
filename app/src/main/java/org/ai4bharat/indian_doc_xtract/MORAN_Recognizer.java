package org.ai4bharat.indian_doc_xtract;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.util.Log;

import org.pytorch.IValue;
import org.pytorch.Module;
import org.pytorch.Tensor;

public class MORAN_Recognizer {

    Module model;
    float mean = 0.5f;
    float std = 0.5f;
    String alphabet = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ$";
    char[] output_map = alphabet.toCharArray();

    public MORAN_Recognizer(String modelPath){

        model = Module.load(modelPath);

    }

    public void setMeanAndStd(float mean, float std){

        this.mean = mean;
        this.std = std;
    }

    public Bitmap toGrayscale(Bitmap bmpOriginal)
    {
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

    public Tensor preprocess(Bitmap bitmap){

        bitmap = Bitmap.createScaledBitmap(bitmap,100,32,false);
        bitmap = toGrayscale(bitmap);
        return TensorImageCustomUtils.bitmapGrayscaleToFloat32Tensor(bitmap, this.mean, this.std);

    }

    public int argMax(float[] inputs){

        int maxIndex = -1;
        float maxvalue = 0.0f;

        for (int i = 0; i < inputs.length; i++){

            if(inputs[i] > maxvalue) {

                maxIndex = i;
                maxvalue = inputs[i];
            }

        }
        return maxIndex;
    }

    String getPrediction(float[] predictionBlob, int maxChars) {
        StringBuilder output = new StringBuilder();
        for (int i = 0, max_j=0; i < maxChars; ++i, max_j=0) {
            for (int j = 0; j < output_map.length; ++j) {
                if (predictionBlob[i*output_map.length + j] > predictionBlob[i*output_map.length + max_j])
                    max_j = j;
            }
            if (output_map[max_j] == '$') break;
            output.append(output_map[max_j]);
        }
        return output.toString();
    }

    public String predict(Bitmap bitmap){

        Tensor tensor = preprocess(bitmap);


        int maxLength = 20;
        long[] textBlob = new long[maxLength];
        for (int i = 0; i < maxLength; ++i) textBlob[i] = 0;
        Tensor textTensor = Tensor.fromBlob(textBlob, new long[]{maxLength});

        Tensor lengthTensor = Tensor.fromBlob(new int[]{maxLength}, new long[]{1});

        IValue inputs = IValue.from(tensor);
        IValue text = IValue.from(textTensor);
        IValue length = IValue.from(lengthTensor);

        IValue outputs = model.forward(inputs, length, text, text);
        IValue[] outs = outputs.toTuple();

        Tensor pred = outs[0].toTensor();

        return getPrediction(pred.getDataAsFloatArray(), maxLength);

    }

}
