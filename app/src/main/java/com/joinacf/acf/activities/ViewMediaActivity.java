package com.joinacf.acf.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.databinding.DataBindingUtil;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.MediaController;

import com.joinacf.acf.R;
import com.joinacf.acf.databinding.ActivityViewMediaBinding;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class ViewMediaActivity extends BaseActivity {

    String strFilepath = "";
    String strFileType = "";
    Bitmap bitmap;
    private Bitmap bm;
    ActivityViewMediaBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_view_media);
        //setActionBarTitle();

        binding = DataBindingUtil.setContentView(this, R.layout.activity_view_media);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
        }
        else
        {
            Bundle bundle = getIntent().getExtras();
            if (bundle != null)
            {
                strFilepath = bundle.getString("FilePath","");
                strFileType = bundle.getString("FileType","");
                try {
                    if(strFileType.equalsIgnoreCase("jpg") && !strFilepath.equalsIgnoreCase("")) {
                        binding.imageView.setVisibility(View.VISIBLE);
                        binding.videoView.setVisibility(View.GONE);

                        byte[] byteArray = getIntent().getByteArrayExtra("strFilepath");
                        bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                        binding.imageView.setImageBitmap(bitmap);
                    }else if(strFileType.equalsIgnoreCase("mp4") && !strFilepath.equalsIgnoreCase(""))
                    {
                        binding.imageView.setVisibility(View.GONE);
                        binding.videoView.setVisibility(View.VISIBLE);
                        binding.videoView.setKeepScreenOn(true);
                        binding.videoView.setVideoURI(Uri.parse(strFilepath));
                        binding.videoView.requestFocus();

                        binding.videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                            @Override
                            public void onPrepared(MediaPlayer mediaPlayer) {
                                binding.videoView.start();
                                mediaPlayer.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
                                    @Override
                                    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                                        MediaController mediaController = new MediaController(ViewMediaActivity.this);
                                        binding.videoView.setMediaController(mediaController);
                                        mediaController.setAnchorView(binding.videoView);
                                    }
                                });
                            }
                        });

                        binding.videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                            @Override
                            public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                                Log.d("video", "setOnErrorListener ");
                                return true;
                            }
                        });
                    }
                }catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
}
