package cv_engine.recognition;

import android.graphics.Bitmap;
import android.util.Pair;

import org.pytorch.Tensor;

import java.util.ArrayList;
import java.util.List;

public abstract class TextRecognizerTorch {

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

    abstract Tensor preprocess(Bitmap bitmap, boolean convertToGrayScale);
    public abstract Pair<String, Float> predict(Bitmap bitmap, boolean convertToGrayScale);

    public List<ArrayList<String>> bulkPredict(List<ArrayList<Bitmap>> textCrops, float confidenceThreshold, boolean convertToGrayScale) {
        List<ArrayList<String>> outputs = new ArrayList<> ();
        for (ArrayList<Bitmap> row : textCrops) {
            ArrayList<String> currentPredictionRow = new ArrayList<> ();
            for (Bitmap bmp : row) {
                Pair<String, Float> output = predict(bmp, convertToGrayScale);
                if (output.second > confidenceThreshold)
                    currentPredictionRow.add(output.first);
            }
            outputs.add(currentPredictionRow);
        }
        return outputs;
    }
    
}
