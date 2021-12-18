package com.chatz.whatsapp.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;

import androidx.exifinterface.media.ExifInterface;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Const {

    public Const() {

    }

    public static String getCompressFilePath(String localFilePath, String stanzaId) {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap scaledBitmap = null;
        Bitmap bmp = null;
        bmp = BitmapFactory.decodeFile(localFilePath, options);

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;
        final float maxHeight = 1920.0f;
        final float maxWidth = 1080.0f;
        float imgRatio = imageRatio(actualWidth, actualHeight);
        float maxRatio = maxWidth / maxHeight;

        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight;
                actualWidth = (int) (imgRatio * actualWidth);
                actualHeight = (int) maxHeight;
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            } else {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;
            }
        }

        ImageLoadingUtils utils = new ImageLoadingUtils();
        options.inSampleSize = utils.calculateInSampleSize(options, actualWidth, actualHeight);
        options.inJustDecodeBounds = false;
        options.inDither = false;
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inTempStorage = new byte[32 * 1024];

        try {
            bmp = BitmapFactory.decodeFile(localFilePath, options);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }

        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
        } catch (Exception exception) {
            exception.printStackTrace();
            scaledBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
        }

        final float ratioX = actualWidth / (float) options.outWidth;
        final float ratioY = actualHeight / (float) options.outHeight;
        final float middleX = actualWidth / 2.0f;
        final float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        if (bmp != null) {
            canvas.drawBitmap(bmp, (middleX - (float) bmp.getWidth() / 2), (middleY - (float) bmp.getHeight() / 2), new Paint(Paint.FILTER_BITMAP_FLAG));
        }

        ExifInterface exif;
        try {
            exif = new ExifInterface(localFilePath);

            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
            } else if (orientation == 3) {
                matrix.postRotate(180);
            } else if (orientation == 8) {
                matrix.postRotate(270);
            }
            scaledBitmap = Bitmap.createBitmap(
                    scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        FileOutputStream out = null;
        String filename = getFilename(stanzaId);
        try {
            out = new FileOutputStream(filename + ".t");
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 50, out);
            File tFile = new File(filename + ".t");
            File file = new File(filename);
            tFile.renameTo(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return filename != null ? filename : "";

    }

    private static float imageRatio(int actualWidth, int actualHeight) {
        return (float) actualWidth / actualHeight;
    }

    private static String getFilename(String stanzaId) {

        String path = Environment.getExternalStorageDirectory().getPath();
        File file = new File(path, "WhatsAppClone_Image");

        if (!file.exists()) {
            file.mkdirs();
        }

        return file.getAbsolutePath() + File.separator + "IMG_" + stanzaId.toUpperCase() + ".jpg";

    }

    public static String getRealPathFromURI(Activity activity, Uri contentURI) {

        try {
            String[] proj = {MediaStore.MediaColumns.DATA};
            Cursor cursor = activity.managedQuery(contentURI, proj, null, null, null);
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            cursor.moveToFirst();
            return cursor.getString(columnIndex);
        } catch (Exception e) {
            return contentURI.getPath();
        }

    }

    public static String convertUriToRealPath(Context context, Uri imageUri) {
        String realPath = "";
        try {
            if (imageUri != null) {
                realPath = GlobalFilePathLocator.getPath(context, imageUri);
                if (realPath == null || realPath.isEmpty() || realPath.equalsIgnoreCase("null")) {
                    realPath = GlobalFilePathLocator.getSharedFileData(imageUri, context);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            realPath = "";
        }
        return realPath;
    }

    public static void intentForPhotos(Context context, int REQUEST_CODE, Fragment fragment) {


        List<Intent> targets = new ArrayList<Intent>();
        targets.add(new Intent());
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        List<ResolveInfo> candidates = context.getPackageManager().queryIntentActivities(intent, 0);

        if (candidates.size() > 1) {

            for (ResolveInfo candidate : candidates) {
                String packageName = candidate.activityInfo.packageName;
                if (!packageName.equals("com.google.android.apps.photos") && !packageName.equals("com.google.android.apps.plus") && !packageName.equals("com.android.documentsui")) {
                    Intent newIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    newIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                    newIntent.setPackage(packageName);
                    targets.add(newIntent);
                }
            }
        } else {
            for (ResolveInfo candidate : candidates) {
                String packageName = candidate.activityInfo.packageName;
                Intent newIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                newIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                newIntent.setPackage(packageName);
                targets.add(newIntent);
            }
        }

        Intent chooser = Intent.createChooser(targets.remove(0), "Select Picture");
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, targets.toArray(new Parcelable[targets.size()]));
        if (fragment != null) {
            fragment.startActivityForResult(chooser, REQUEST_CODE);
        } else {
            ((Activity) context).startActivityForResult(chooser, REQUEST_CODE);
        }

    }

    public static File getOutputMediaFile(Context context) {
        String imageDirectoryName = "WhatsAppClone_Image";
        File mediaStorageDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), imageDirectoryName);

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");

        return mediaFile;
    }

    public static Uri getLocalBitmapUri(Context context, Bitmap bmp) {
        Uri bmpUri = null;
        try {
            File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "feed_image_" + System.currentTimeMillis() + ".png");
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();
            bmpUri = Uri.fromFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;
    }

}
