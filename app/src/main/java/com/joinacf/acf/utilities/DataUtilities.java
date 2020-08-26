package com.joinacf.acf.utilities;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.crashlytics.android.Crashlytics;
import com.joinacf.acf.R;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

public class DataUtilities {

    public static String getExtensionType(Uri contentURI, Context context) {
        String extension;
        if (contentURI.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            final MimeTypeMap mime = MimeTypeMap.getSingleton();
            extension = mime.getExtensionFromMimeType(context.getContentResolver().getType(contentURI));
        } else {
            extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(new File(contentURI.getPath())).toString());
        }
        return extension;
    }

    public static void openFile(Uri uri, String url, Context ctx) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        if (url.toString().contains(".doc") || url.toString().contains(".docx")) {
            // Word document
            intent.setDataAndType(uri, "application/msword");
        } else if (url.toString().contains(".pdf")) {
            // PDF file
            intent.setDataAndType(uri, "application/pdf");
        } else if (url.toString().contains(".ppt") || url.toString().contains(".pptx")) {
            // Powerpoint file
            intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
        } else if (url.toString().contains(".xls") || url.toString().contains(".xlsx")) {
            // Excel file
            intent.setDataAndType(uri, "application/vnd.ms-excel");
        } else if (url.toString().contains(".zip") || url.toString().contains(".rar")) {
            // WAV audio file
            intent.setDataAndType(uri, "application/x-wav");
        } else if (url.toString().contains(".rtf")) {
            // RTF file
            intent.setDataAndType(uri, "application/rtf");
        } else if (url.toString().contains(".wav") || url.toString().contains(".mp3")) {
            // WAV audio file
            intent.setDataAndType(uri, "audio/x-wav");
        } else if (url.toString().contains(".gif")) {
            // GIF file
            intent.setDataAndType(uri, "image/gif");
        } else if (url.toString().contains(".jpg") || url.toString().contains(".jpeg") || url.toString().contains(".png")) {
            // JPG file
            intent.setDataAndType(uri, "image/jpeg");
        } else if (url.toString().contains(".txt")) {
            // Text file
            intent.setDataAndType(uri, "text/plain");
        } else if (url.toString().contains(".3gp") || url.toString().contains(".mpg") || url.toString().contains(".mpeg") || url.toString().contains(".mpe") || url.toString().contains(".mp4") || url.toString().contains(".avi")) {
            // Video files
            intent.setDataAndType(uri, "video/*");
        } else {
            // Other files
            intent.setDataAndType(uri, "*/*");
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(intent);
    }

    public static void loadImagePath(String url, ImageView imgFilePath,Context context) {
        String strMimeType = getExtensionType(Uri.parse(url), context);
        imgFilePath.setContentDescription(url);
        if (strMimeType != null && !strMimeType.equalsIgnoreCase("")) {
            if (strMimeType.equalsIgnoreCase("jpg") || strMimeType.equalsIgnoreCase("jpeg") || strMimeType.equalsIgnoreCase("png")) {
                Glide.with(context).load(url).into(imgFilePath);
            } else if (strMimeType.equalsIgnoreCase("mp4")) {
                Glide.with(context).load(R.drawable.mp4).into(imgFilePath);
            } else if (strMimeType.equalsIgnoreCase("application/pdf") || strMimeType.equalsIgnoreCase("pdf")) {
                Glide.with(context).load(R.drawable.pdf).into(imgFilePath);
            } else if (strMimeType.equalsIgnoreCase("docx") || strMimeType.equalsIgnoreCase("doc") || strMimeType.equalsIgnoreCase("application/vnd.openxmlformats-officedocument.wordprocessingml.document") || strMimeType.equalsIgnoreCase("application/msword")) {
                Glide.with(context).load(R.drawable.doc).into(imgFilePath);
            } else if (strMimeType.equalsIgnoreCase("xlsx") || strMimeType.equalsIgnoreCase("xls") || strMimeType.equalsIgnoreCase("application/vnd.ms-excel") || strMimeType.equalsIgnoreCase("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
                Glide.with(context).load(R.drawable.excel).into(imgFilePath);
            } else if (strMimeType.equalsIgnoreCase("ppt") || strMimeType.equalsIgnoreCase("application/vnd.ms-powerpoint") || strMimeType.equalsIgnoreCase("application/vnd.openxmlformats-officedocument.presentationml.presentation")) {
                Glide.with(context).load(R.drawable.powerpoint).into(imgFilePath);
            } else if (strMimeType.equalsIgnoreCase("txt") || strMimeType.equalsIgnoreCase("text/plain")) {
                Glide.with(context).load(R.drawable.txt).into(imgFilePath);
            } else if (strMimeType.equalsIgnoreCase("mp3") || strMimeType.equalsIgnoreCase("audio/mpeg")) {
                Glide.with(context).load(R.drawable.mp3).into(imgFilePath);
            }
        }
    }
}
