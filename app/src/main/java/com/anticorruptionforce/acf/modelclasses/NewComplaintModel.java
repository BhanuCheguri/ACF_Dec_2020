package com.anticorruptionforce.acf.modelclasses;

import android.graphics.Bitmap;
import android.net.Uri;

public class NewComplaintModel {

    String FilePath;
    String Content;
    String ExtensionType;
    Uri Uri;

    public android.graphics.Bitmap getBitmap() {
        return Bitmap;
    }

    public void setBitmap(android.graphics.Bitmap bitmap) {
        Bitmap = bitmap;
    }

    Bitmap Bitmap;

    public android.net.Uri getUri() {
        return Uri;
    }

    public void setUri(android.net.Uri uri) {
        Uri = uri;
    }

    public String getFilePath() {
        return FilePath;
    }

    public void setFilePath(String filePath) {
        FilePath = filePath;
    }

    public String getContent() {
        return Content;
    }

    public void setContent(String content) {
        Content = content;
    }

    public String getExtensionType() {
        return ExtensionType;
    }

    public void setExtensionType(String extensionType) {
        ExtensionType = extensionType;
    }
}
