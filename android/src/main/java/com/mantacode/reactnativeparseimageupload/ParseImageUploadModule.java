package com.mantacode.reactnativeparseimageupload;

import android.net.Uri;
import com.facebook.react.bridge.*;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ProgressCallback;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.UUID;


public class ParseImageUploadModule extends ReactContextBaseJavaModule {

    public ParseImageUploadModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return "ParseImageUpload";
    }

    @ReactMethod
    public void uploadImage(String uri, String fileName, int maxWidth, int maxHeight, final Promise promise) {
        fileName = fileName == null || fileName.length() == 0 ? UUID.randomUUID().toString() : fileName;
        boolean isRemoteUrl = uri.indexOf("http://") > -1 || uri.indexOf("https://") > -1;
        try {
            byte[] fileData = isRemoteUrl ?
                    FileHelpers.getJpgImageFromRemoteUrl(uri, maxWidth, maxHeight) :
                    FileHelpers.getJpgImageFromUri(getReactApplicationContext(), uri, maxWidth, maxHeight);
            saveFile(fileData, fileName + ".jpg", promise);
        } catch (Exception ex) {
            promise.reject(ex);
        }
    }

    private void saveFile(byte[] fileData, String fileName, final Promise promise) {
        try {
            final ParseFile parseFile = new ParseFile(fileName, fileData);
            parseFile.saveInBackground(new SaveCallback() {
                public void done(ParseException e) {
                    if (e == null) {
                        WritableMap map = Arguments.createMap();
                        map.putString("name", parseFile.getName());
                        map.putString("url", parseFile.getUrl());
                        map.putString("__type", "File");
                        promise.resolve(map);
                    } else {
                        promise.reject(e);
                    }
                }
            }, new ProgressCallback() {
                public void done(Integer percentDone) {
                    // Update your progress spinner here. percentDone will be between 0 and 100.
                }
            });
        } catch (Exception ex) {
            promise.reject(ex);
        }
    }
}
