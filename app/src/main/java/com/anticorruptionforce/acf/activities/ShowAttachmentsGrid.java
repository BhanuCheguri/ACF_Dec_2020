package com.anticorruptionforce.acf.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.anticorruptionforce.acf.R;
import com.anticorruptionforce.acf.adapters.SPPetitionsListAdapter;
import com.anticorruptionforce.acf.utilities.Utils;
import com.bumptech.glide.Glide;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import static com.anticorruptionforce.acf.utilities.DataUtilities.getExtensionType;

public class ShowAttachmentsGrid extends BaseActivity {

    ArrayList<String> lstImages;
    ArrayList<String> lstLatestImages;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_attachments_grid);

        lstLatestImages =new ArrayList<>();

        GridView showAttachmentsGrid = (GridView) findViewById(R.id.showAttachmentsGrid);

        Bundle extras = getIntent().getExtras();
        if(extras != null){
            //lstImages = extras.getParcelable("Images");
            lstImages = (ArrayList<String>) getIntent().getSerializableExtra("Images");
            String strTitle = getIntent().getStringExtra("Title");

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.mipmap.ic_launcher_icon);
            getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.gradient_theme));
            setActionBarTitle(strTitle);
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }

        if(lstImages != null){
            for (int i=0; i<lstImages.size() ; i++) {
                if (!lstImages.get(i).equalsIgnoreCase("") && lstImages.get(i) != null) {
                    if (!lstImages.get(i).contains("http://")) {
                        try {
                            URL url = new URL("http://api.ainext.in/" + lstImages.get(i));
                            lstLatestImages.add(url.toString());
                        } catch (MalformedURLException e) {
                            Log.v("myApp", "bad url entered");
                        }
                    } else
                        lstLatestImages.add(lstImages.get(i));
                }
            }
            showAttachmentsGrid.setAdapter(new GridImageAdapter(this,lstLatestImages));
        }
    }

    public class GridImageAdapter extends BaseAdapter {
        private Context mContext;
        ArrayList<String> lstGridImages;
        // Constructor
        public GridImageAdapter(Context c, ArrayList<String> lstImages) {
            mContext = c;
            lstGridImages = lstImages;
        }

        public int getCount() {
            return lstGridImages.size();
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {

            View itemView = LayoutInflater.from(mContext)
                    .inflate(R.layout.grid_row_layout, parent, false);

            ImageView imageView = (ImageView)itemView.findViewById(R.id.grid_img);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            Glide.with(mContext).load(lstGridImages.get(position)).into(imageView);
            imageView.setContentDescription(lstGridImages.get(position));

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!(v.getContentDescription().toString()).equalsIgnoreCase("") || (v.getContentDescription().toString()) !=null) {
                        String strMimeType = getExtensionType(Uri.parse(v.getContentDescription().toString()), mContext);
                        System.out.println("SP Petitions url :: " + v.getContentDescription().toString());
                        Utils.showDialog(strMimeType, v.getContentDescription().toString(), mContext);
                    }
                }
            });

            return itemView;
        }
    }
}