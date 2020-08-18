package com.joinacf.acf.utilities;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import com.crashlytics.android.Crashlytics;

import java.net.URI;
import java.net.URISyntaxException;

public class DataUtilities {

    public static String getExtensionType(Uri contentURI, Context context)
    {
        String extension = "";
        if(contentURI != null) {
            ContentResolver contentResolver = context.getContentResolver();
            MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
            extension = mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(contentURI));
            System.out.print("extension ::" + extension);
        }

        return extension;
    }
}
