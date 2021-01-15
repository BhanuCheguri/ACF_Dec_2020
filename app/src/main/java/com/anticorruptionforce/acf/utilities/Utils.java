package com.anticorruptionforce.acf.utilities;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import com.anticorruptionforce.acf.R;
import com.bumptech.glide.Glide;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

public class Utils {
    public static void CopyStream(InputStream is, OutputStream os)
    {
        final int buffer_size=1024;
        try
        {
            byte[] bytes=new byte[buffer_size];
            for(;;)
            {
                int count=is.read(bytes, 0, buffer_size);
                if(count==-1)
                    break;
                os.write(bytes, 0, count);
            }
        }
        catch(Exception ex){}
    }


    public static Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);
        // RECREATE THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height,
                matrix, false);
        return resizedBitmap;
    }

    public static void showDialog(String strMimeType, String content, Context context) {
        Rect displayRectangle = new Rect();
        Dialog dialog2 = new Dialog(context, R.style.DialogFullScreenTheme);
        dialog2.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog2.getWindow().setBackgroundDrawable(null);
        //dialog2.setContentView(R.layout.activity_watch_item);
        WindowManager.LayoutParams lp = dialog2.getWindow().getAttributes();
        Window window = dialog2.getWindow();
        lp.copyFrom(window.getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(lp);
        lp.gravity = Gravity.CENTER;
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.activity_watch_item, null);
        layout.setMinimumWidth((int)(displayRectangle.width() * 0.9f));
        layout.setMinimumHeight((int)(displayRectangle.height() * 0.9f));
        dialog2.setContentView(layout);

        final ImageView imgPicture=(ImageView)dialog2.findViewById(R.id.imgPicture);
        final VideoView imgVideo=(VideoView)dialog2.findViewById(R.id.imgVideo);
        final ImageView imgClose=(ImageView)dialog2.findViewById(R.id.imgClose);

        imgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog2.dismiss();
            }
        });

        dialog2.show();
        if(strMimeType.equalsIgnoreCase("jpg")){
            imgPicture.setVisibility(View.VISIBLE);
            imgVideo.setVisibility(View.GONE);

            Glide.with(context)
                    .load(content) // image url
                    .override(200, 250).centerCrop().into(imgPicture);  // imageview object.

            /*try {
                URL url = new URL(content);
                Bitmap bmp;
                bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                imgPicture.setImageBitmap(bmp);
            }catch (Exception e){
                e.printStackTrace();
            }*/
        }else if(strMimeType.equalsIgnoreCase("mp4")){
            imgPicture.setVisibility(View.GONE);
            imgVideo.setVisibility(View.VISIBLE);
            try {
                // Start the MediaController
                MediaController mediacontroller = new MediaController(context);
                mediacontroller.setAnchorView(imgVideo);

                Uri videoUri = Uri.parse(content);
                imgVideo.setMediaController(mediacontroller);
                imgVideo.setVideoURI(videoUri);

            } catch (Exception e) {

                e.printStackTrace();
            }

            imgVideo.requestFocus();
            imgVideo.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                // Close the progress bar and play the video
                public void onPrepared(MediaPlayer mp) {
                    imgVideo.start();
                }
            });
            imgVideo.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                public void onCompletion(MediaPlayer mp) {

                }
            });
        }
    }

}