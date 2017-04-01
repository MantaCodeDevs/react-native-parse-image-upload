package com.mantacode.reactnativeparseimageupload;

import android.content.Context;
import android.net.Uri;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.net.URL;
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
        float widthRatio = maxWidth > 0 && maxWidth < width ? (float)maxWidth / width : 1.0f;
        float heightRatio = maxHeight > 0 && maxHeight < height ? (float)maxHeight / height : 1.0f;

        float ratio = Math.min(widthRatio, heightRatio);

        int finalWidth = (int) (width * ratio);
        int finalHeight = (int) (height * ratio);

        return Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true);
    }
}
