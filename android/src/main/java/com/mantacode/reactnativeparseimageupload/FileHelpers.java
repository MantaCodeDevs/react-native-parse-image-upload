package com.mantacode.reactnativeparseimageupload;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;

public class FileHelpers {

    public static byte[] getJpgImageFromUri(Context context, String uri, int maxWidth, int maxHeight, int rotation) throws Exception {
        byte[] result = null;
        InputStream inputStream = null;
        try {
            Uri contentUri = Uri.parse(uri);
            inputStream = context.getContentResolver().openInputStream(contentUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            bitmap = rotateImageIfRequired(context, bitmap, contentUri, rotation);
            result = getResizedImageFromStream(bitmap, contentUri.getPath(), maxWidth, maxHeight);
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
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            result = getResizedImageFromStream(bitmap, url, maxWidth, maxHeight);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }

        return result;
    }

    private static byte[] getResizedImageFromStream(Bitmap bitmap, String path, int maxWidth, int maxHeight) {
        byte[] result = null;
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

    private static Bitmap rotateImageIfRequired(Context context, Bitmap img, Uri uri, int rotation) throws Exception {
        if (rotation > -1) {
            return rotation != 0 ? rotateImage(img, rotation) : img;
        }

        int rotationAngle = getOrientationFromMediaStore(context, uri);
        if (rotationAngle > 0) {
            return rotateImage(img, rotationAngle);
        }

        rotationAngle = getOrientationFromExif(context, uri);
        if (rotationAngle > 0) {
            return rotateImage(img, rotationAngle);
        }

        return img;
    }

    private static int getOrientationFromMediaStore(Context context, Uri photoUri) {
        Cursor cursor = context.getContentResolver().query(photoUri,
                new String[]{MediaStore.Images.ImageColumns.ORIENTATION}, null, null, null);

        if (cursor == null) return -1;

        if (cursor.getCount() != 1 || cursor.getColumnCount() == 0) {
            cursor.close();
            return -1;
        }

        cursor.moveToFirst();
        int orientation = cursor.getInt(0);
        cursor.close();
        return orientation;
    }


    private static int getOrientationFromExif(ExifInterface exif) {
        int rotate = 0;
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_270:
                rotate = 270;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                rotate = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                rotate = 90;
                break;
        }

        return rotate;
    }

    private static int getOrientationFromExif(Context context, Uri photoUri) throws Exception {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            InputStream inputStream = null;
            try {
                Uri contentUri = Uri.parse(photoUri.toString());
                inputStream = context.getContentResolver().openInputStream(contentUri);
                ExifInterface exif = new ExifInterface(inputStream);
                return getOrientationFromExif(exif);
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
            }
        } else {
            Cursor cursor = context.getContentResolver().query(photoUri,
                    new String[]{MediaStore.Images.Media.DATA}, null, null, null);
            int column_index = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
            if (column_index == -1) return -1;

            cursor.moveToFirst();
            String path = cursor.getString(column_index);
            ExifInterface exif = new ExifInterface(path);
            return getOrientationFromExif(exif);
        }
    }

    private static Bitmap rotateImage(Bitmap img, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }
}