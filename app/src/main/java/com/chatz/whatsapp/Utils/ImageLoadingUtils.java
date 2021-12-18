package com.chatz.whatsapp.Utils;

import android.graphics.BitmapFactory;

public class ImageLoadingUtils {

    public static final int AVTAR_MAX_HEIGHT = 55;

    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        final float totalPixels = getTotalPixels(width, height);
        final float totalReqPixelsCap = getTotalReqPixelsCap(reqWidth, reqHeight);

        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }

        return inSampleSize;
    }

    private float getTotalPixels(int width, int height) {
        return (float) width * height;
    }

    private float getTotalReqPixelsCap(int reqWidth, int reqHeight) {
        return (float) reqWidth * reqHeight * 2;
    }

}
