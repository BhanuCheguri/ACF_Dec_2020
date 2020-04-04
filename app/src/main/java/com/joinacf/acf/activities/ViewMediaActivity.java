package com.joinacf.acf.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.databinding.DataBindingUtil;

import android.os.Bundle;
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

        Bundle bundle = getIntent().getExtras();
        if (bundle != null)
        {
            strFilepath = bundle.getString("FilePath","");
            strFileType = bundle.getString("FileType","");
              try {
                    if(strFileType.equalsIgnoreCase("jpg") && !strFilepath.equalsIgnoreCase("")) {
                        binding.imageView.setVisibility(View.VISIBLE);
                        binding.videoView.setVisibility(View.GONE);
                        bitmap = getBitmap(strFilepath);
                        binding.imageView.setImageBitmap(bitmap);
                    }else if(strFileType.equalsIgnoreCase("mp4") && !strFilepath.equalsIgnoreCase(""))
                    {
                        binding.imageView.setVisibility(View.GONE);
                        binding.videoView.setVisibility(View.VISIBLE);
                        MediaController mediaController = new MediaController(this);
                        mediaController.setAnchorView(binding.videoView);
                        binding.videoView.setMediaController(mediaController);
                        binding.videoView.setKeepScreenOn(true);
                        binding.videoView.setVideoPath(strFilepath);
                        binding.videoView.start();
                        binding.videoView.requestFocus();
                    }
                }catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

    }

    public Bitmap getBitmap(String url) {
        FileInputStream fIn = null;
        try {
            //fIn = new FileInputStream(new File(imgData.get(position).toString()));
            BitmapFactory.Options bfOptions = new BitmapFactory.Options();
            bfOptions.inDither = false;                     //Disable Dithering mode
            bfOptions.inPurgeable = true;                   //Tell to gc that whether it needs free memory, the Bitmap can be cleared
            bfOptions.inInputShareable = true;              //Which kind of reference will be used to recover the Bitmap data after being clear, when it will be used in the future
            bfOptions.inTempStorage = new byte[32 * 1024];

            fIn = new FileInputStream(new File(url));

            if (fIn != null) {
                bm = BitmapFactory.decodeFileDescriptor(fIn.getFD(), null, bfOptions);
                if (bm != null) {

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fIn != null) {
                try {
                    fIn.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return bm;
    }
}
