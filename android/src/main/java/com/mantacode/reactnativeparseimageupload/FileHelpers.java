package com.mantacode.reactnativeparseimageupload;

import android.content.Context;
import android.net.Uri;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.*;

public class FileHelpers {

    public static byte[] getJpgImageFromUri(Context context, String uri, int maxWidth, int maxHeight) throws Exception {
        byte[] result = null;
        InputStream inputStream = null;
        try {
            Uri contentUri = Uri.parse(uri);
            inputStream = context.getContentResolver().openInputStream(contentUri);
            result = getResizedImageFromStream(inputStream, maxWidth, maxHeight);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }

        return result;
    }

    public static byte[] getJpgImageFromRemoteUrl(String url, int maxWidth, int maxHeight) throws Exception {
        byte[] result = null;
        InputStream inputStream = null;
        try {
            URL fileUrl = new URL(url);
            inputStream = fileUrl.openStream();
            result = getResizedImageFromStream(inputStream, maxWidth, maxHeight);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }

        return result;
    }

    private static byte[] getResizedImageFromStream(InputStream inputStream, int maxWidth, int maxHeight) {
        byte[] result = null;
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
        Bitmap resizedImage;
        if (maxWidth > 0 || maxHeight > 0) {
            resizedImage = resizeImage(bitmap, maxWidth, maxHeight);
        } else {
            resizedImage = bitmap;
        }
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        resizedImage.compress(Bitmap.CompressFormat.JPEG, 90, outStream);
        return outStream.toByteArray();
    }

    private static Bitmap resizeImage(Bitmap image, int maxWidth, int maxHeight) {
        if (image == null) {
            return null; // Can't load the image from the given path.
        }

        float width = image.getWidth();
        float height = image.getHeight();

        // do not upscale
        maxWidth = maxWidth > 0 && maxWidth < width ? maxWidth : width;
        maxHeight = maxHeight > 0 && maxHeight < height ? maxHeight : height;

        float ratio = Math.min((float)maxWidth / width, (float)maxHeight / height);

        int finalWidth = (int) (width * ratio);
        int finalHeight = (int) (height * ratio);

        return Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true);
    }
}
