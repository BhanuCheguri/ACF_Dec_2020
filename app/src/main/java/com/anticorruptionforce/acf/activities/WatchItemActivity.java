package com.anticorruptionforce.acf.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.MediaController;

import com.anticorruptionforce.acf.R;
import com.anticorruptionforce.acf.databinding.ActivityWatchItemBinding;
import com.bumptech.glide.Glide;

public class WatchItemActivity extends BaseActivity {
    String mimeType = "",content = "";
    ActivityWatchItemBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_watch_item);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_watch_item);

        Bundle b = getIntent().getExtras();
        if(b != null){
            mimeType = b.getString("MimeType");
            content = b.getString("content");
            System.out.println("Content:"+content);
        }
        if(mimeType.equalsIgnoreCase("jpg")){
            binding.imgPicture.setVisibility(View.VISIBLE);
            binding.imgVideo.setVisibility(View.GONE);

            Glide.with(this)
                    .load(content) // image url
                    .override(200, 200).centerCrop().into(binding.imgPicture);  // imageview object.
        }else if(mimeType.equalsIgnoreCase("mp4")){
            binding.imgPicture.setVisibility(View.GONE);
            binding.imgVideo.setVisibility(View.VISIBLE);
            try {
                // Start the MediaController
                MediaController mediacontroller = new MediaController(this);
                mediacontroller.setAnchorView(binding.imgVideo);

                Uri videoUri = Uri.parse(content);
                binding.imgVideo.setMediaController(mediacontroller);
                binding.imgVideo.setVideoURI(videoUri);

            } catch (Exception e) {

                e.printStackTrace();
            }

            binding.imgVideo.requestFocus();
            binding.imgVideo.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                // Close the progress bar and play the video
                public void onPrepared(MediaPlayer mp) {
                    binding.imgVideo.start();
                }
            });
            binding.imgVideo.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                public void onCompletion(MediaPlayer mp) {
                    finish();
                }
            });
        }
    }
}