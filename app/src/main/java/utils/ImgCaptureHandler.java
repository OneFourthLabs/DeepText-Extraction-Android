package utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.ImageView;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static utils.ImageUtils.getOrientationMatrix;

public class ImgCaptureHandler {
    // Abstracted from: https://developer.android.com/training/camera/photobasics.html#java

    public static final int REQUEST_TAKE_PHOTO = 1;
    boolean randomSave = true;
    public String currentPhotoPath, currentPhotoName;
    private File externalFilesDir;

    public ImgCaptureHandler(boolean randomSave, Context context) {
        this.randomSave = randomSave;
        externalFilesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
    }

    public boolean dispatchTakePictureIntent(Activity activity) throws IOException {
        Intent takePictureIntent = getTakePictureIntent(activity);
        if (takePictureIntent == null)
            return false;

        activity.startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
        return true;
    }

    public Intent getTakePictureIntent(Activity activity) throws IOException {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(activity.getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = createImageFile(randomSave);

            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(activity,
                        activity.getPackageName()+".fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                return takePictureIntent;
            }
        }
        return null;
    }

    private File createImageFile(boolean random) throws IOException {
        // Create an image file name
        String imageFileName;
        if (random) {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            imageFileName = "JPEG_" + timeStamp + "_";
        } else {
            imageFileName = "tmp_capture";
        }

        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                externalFilesDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        currentPhotoName = image.getName();
        return image;
    }

    public void galleryAddPic(Context context) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
    }

    public Bitmap getPic() {
        Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);
        // Adjust orientation
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), getOrientationMatrix(currentPhotoPath), false);
        return bitmap;
    }

    public void setPic(ImageView imageView) {
        // Get the dimensions of the View
        int targetW = imageView.getWidth();
        int targetH = imageView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;

        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
        imageView.setImageBitmap(bitmap);
    }

}
