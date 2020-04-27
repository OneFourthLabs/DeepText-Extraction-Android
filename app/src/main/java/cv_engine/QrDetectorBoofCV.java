package cv_engine;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.List;

import boofcv.abst.fiducial.QrCodeDetector;
import boofcv.alg.fiducial.qrcode.QrCode;
import boofcv.android.ConvertBitmap;
import boofcv.factory.fiducial.ConfigQrCode;
import boofcv.factory.fiducial.FactoryFiducial;
import boofcv.struct.image.GrayU8;

public class QrDetectorBoofCV {

    final QrCodeDetector<GrayU8> detector;

    public QrDetectorBoofCV() {
        detector = FactoryFiducial.qrcode(null, GrayU8.class);
    }

    public List<String> detectMessages(final Bitmap bitmap) {
        GrayU8 grayImage = ConvertBitmap.bitmapToGray(bitmap, (GrayU8) null, null);
        detector.process(grayImage);
        List<String> qrResult = new ArrayList<>();
        for (QrCode qr : detector.getDetections()) {
            qrResult.add(qr.message);
        }
        for (QrCode qr : detector.getFailures()) {
            System.out.println(qr.failureCause);
        }
        return qrResult;
    }

}
