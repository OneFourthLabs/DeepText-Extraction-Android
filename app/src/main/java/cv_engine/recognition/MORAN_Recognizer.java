package cv_engine.recognition;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.util.Pair;
import android.util.Size;

import utils.TensorImageCustomUtils;
import org.pytorch.IValue;
import org.pytorch.Module;
import org.pytorch.Tensor;

import static utils.ImageUtils.bitmapToGrayscale;

public class MORAN_Recognizer extends TextRecognizerTorch {

    final Module model;
    public static final Size inputSize = new Size(100, 32);

    float mean = 0.5f;
    float std = 0.5f;
    static final String alphabet = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ$";
    static final char[] output_map = alphabet.toCharArray();

    public MORAN_Recognizer(String modelPath){
        model = Module.load(modelPath);
    }

    public void setMeanAndStd(float mean, float std){
        this.mean = mean;
        this.std = std;
    }

    public Tensor preprocess(Bitmap bitmap) {
        bitmap = Bitmap.createScaledBitmap(bitmap, inputSize.getWidth(), inputSize.getHeight(),false);
        bitmap = bitmapToGrayscale(bitmap);
        return TensorImageCustomUtils.bitmapGrayscaleToFloat32Tensor(bitmap, this.mean, this.std);
    }

    Pair<String, Float> getPrediction(float[] predictionBlob, int maxChars) {
        // Sorry for the mess
        StringBuilder output = new StringBuilder();
        float confidence = 0; // Geometric mean of all probabilities
        float partitionFunction = 0;
        for (int i = 0, max_j=0; i < maxChars; ++i, max_j=0, partitionFunction=0) {
            for (int j = 0; j < output_map.length; ++j) {
                partitionFunction += Math.exp(predictionBlob[i*output_map.length + j]);
                // TODO: Use ArgMax
                if (predictionBlob[i*output_map.length + j] > predictionBlob[i*output_map.length + max_j])
                    max_j = j;
            }
            if (output_map[max_j] == '$') break;
            confidence += predictionBlob[i*output_map.length + max_j] - Math.log(partitionFunction);
            output.append(output_map[max_j]);
        }
        confidence = output.length() > 0 ? (float) Math.exp(confidence / output.length()) : 0;
        return new Pair<String, Float>(output.toString(), confidence);
    }

    public Pair<String, Float> predict(Bitmap bitmap) {

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
