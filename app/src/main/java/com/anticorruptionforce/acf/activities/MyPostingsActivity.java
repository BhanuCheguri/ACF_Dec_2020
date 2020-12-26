package com.anticorruptionforce.acf.activities;

import androidx.core.view.MenuItemCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.anticorruptionforce.acf.R;
import com.anticorruptionforce.acf.databinding.ActivityMyPostingsBinding;
import com.anticorruptionforce.acf.modelclasses.DashboardCategories;
import com.anticorruptionforce.acf.modelclasses.MyPostingModel;
import com.anticorruptionforce.acf.network.APIInterface;
import com.anticorruptionforce.acf.network.APIRetrofitClient;
import com.anticorruptionforce.acf.utilities.App;
//import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.pd.chocobar.ChocoBar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static com.anticorruptionforce.acf.utilities.DataUtilities.getExtensionType;
import static com.anticorruptionforce.acf.utilities.DataUtilities.loadImagePath;

public class MyPostingsActivity extends BaseActivity {

    private ActivityMyPostingsBinding dataBiding;
    ArrayList<MyPostingModel.Result> myPostingResult;
    APIRetrofitClient apiRetrofitClient;
    ArrayList<DashboardCategories.Result> lstCatagories;
    MyPostingAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_my_postings);
        dataBiding = DataBindingUtil.setContentView(this, R.layout.activity_my_postings);
        init();
        //LoadAdapter();
    }
    private void init() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.mipmap.ic_launcher_icon);
        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.gradient_theme));
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        getSupportActionBar().setTitle("My Postings");

        lstCatagories = new ArrayList<DashboardCategories.Result>();
        apiRetrofitClient = new APIRetrofitClient();

        if(App.isNetworkAvailable())
            new AsyncGetDashboardCategories().execute();
        else{
            ChocoBar.builder().setView(dataBiding.mainLayout)
                    .setText("No Internet connection")
                    .setDuration(ChocoBar.LENGTH_INDEFINITE)
                    //.setActionText(android.R.string.ok)
                    .red()   // in built red ChocoBar
                    .show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mypostings, menu);
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setSubmitButtonEnabled(true);
        searchView.setQueryHint("Search");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                // filter recycler view when text is changed
                if (adapter!=null)
                    adapter.getFilter().filter(query);
                Toast.makeText(MyPostingsActivity.this,
                        getString(R.string.abc_searchview_description_submit),
                        Toast.LENGTH_SHORT)
                        .show();
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    public class AsyncGetDashboardCategories extends AsyncTask<String,String,String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog(MyPostingsActivity.this, "Updating Member location");
        }

        @Override
        protected String doInBackground(String... strings) {
            getDashboardCategories();
            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if(App.isNetworkAvailable())
                getMyPostingDetails();
            else{
                ChocoBar.builder().setView(dataBiding.mainLayout)
                        .setText("No Internet connection")
                        .setDuration(ChocoBar.LENGTH_INDEFINITE)
                        //.setActionText(android.R.string.ok)
                        .red()   // in built red ChocoBar
                        .show();
            }
        }
    }

    private ArrayList<DashboardCategories.Result> getDashboardCategories() {
        try {
            Retrofit retrofit = apiRetrofitClient.getRetrofit(APIInterface.BASE_URL);
            APIInterface api = retrofit.create(APIInterface.class);
            Call<DashboardCategories> call = api.getDashboardCategories();

            call.enqueue(new Callback<DashboardCategories>() {
                @Override
                public void onResponse(Call<DashboardCategories> call, Response<DashboardCategories> response) {
                    DashboardCategories myProfileData = response.body();
                    hideProgressDialog(MyPostingsActivity.this);
                    if(myProfileData != null) {
                        String status = myProfileData.getStatus();
                        String msg = myProfileData.getMessage();
                        if (msg.equalsIgnoreCase("SUCCESS")) {
                            lstCatagories = myProfileData.getResult();
                        }
                    }
                }

                @Override
                public void onFailure(Call<DashboardCategories> call, Throwable t) {
                    //FirebaseCrashlytics.getInstance().setCustomKey("MyPostingsActivity", t.getMessage());                    hideProgressDialog(MyPostingsActivity.this);
                    Toast.makeText(MyPostingsActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                    showAlert(MyPostingsActivity.this,"Error","Unable to get the Categories list","OK");
                }
            });

        }catch (Exception e)
        {
            e.printStackTrace();
            //FirebaseCrashlytics.getInstance().setCustomKey("MyPostingsActivity", e.getMessage());
        }
        return lstCatagories;
    }

    private void getMyPostingDetails() {
        String strMemberID = getStringSharedPreference(MyPostingsActivity.this,"MemberID");
        Retrofit retrofit = apiRetrofitClient.getRetrofit(APIInterface.BASE_URL);
        APIInterface api = retrofit.create(APIInterface.class);
       // final Call<List<MyPostingModel>> call = api.getMyPostings(strMemberID);
        final Call<MyPostingModel> call = api.getMyPostings(strMemberID);
        call.enqueue(new Callback<MyPostingModel>() {
            @Override
            public void onResponse(Call<MyPostingModel> call, Response<MyPostingModel> response) {
                MyPostingModel myPostingsData = response.body();
                if(myPostingsData != null) {
                    hideProgressDialog(MyPostingsActivity.this);
                    String status = myPostingsData.getStatus();
                    String msg = myPostingsData.getMessage();
                    if (msg.equalsIgnoreCase("SUCCESS")) {
                        myPostingResult = myPostingsData.getResult();
                        adapter = new MyPostingAdapter(MyPostingsActivity.this,myPostingResult);
                        dataBiding.lvPosting.setLayoutManager(new LinearLayoutManager(MyPostingsActivity.this));
                        dataBiding.lvPosting.setItemAnimator(new DefaultItemAnimator());
                        dataBiding.lvPosting.setAdapter(adapter);
                    }else{
                        dataBiding.llNoData.setVisibility(View.VISIBLE);
                        hideProgressDialog(MyPostingsActivity.this);
                    }
                }else
                {
                    dataBiding.llNoData.setVisibility(View.VISIBLE);
                    hideProgressDialog(MyPostingsActivity.this);
                }
            }

            @Override
            public void onFailure(Call<MyPostingModel> call, Throwable t) {
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    public class MyPostingAdapter extends RecyclerView.Adapter<MyPostingAdapter.ViewHolder>  implements Filterable {
        public ArrayList<MyPostingModel.Result> dataSet;
        FragmentActivity context;
        private LayoutInflater inflater=null;
        HashMap<String, String> hshMapCategoryLst = new HashMap<>();
        private ArrayList<MyPostingModel.Result> dataListFiltered;


        public MyPostingAdapter(FragmentActivity context,ArrayList<MyPostingModel.Result> data) {
            this.dataSet = data;
            this.context = context;
            this.dataListFiltered = data;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            TextView txtTitle;
            TextView txtDescription;
            TextView txtLocation;
            TextView txtDateTime;
            TextView txtCategory;
            TextView txtStatus;
            ImageView imgFilePath;
            LinearLayout linearLayout;
            LinearLayout linearImages;

            public ViewHolder(View rowView) {
                super(rowView);
                this.txtTitle = (TextView) rowView.findViewById(R.id.tv_title);
                this.txtDescription = (TextView) rowView.findViewById(R.id.tv_description);
                this.txtLocation = (TextView) rowView.findViewById(R.id.tv_location);
                this.txtDateTime = (TextView) rowView.findViewById(R.id.tv_DateTime);
                this.txtCategory = (TextView) rowView.findViewById(R.id.tv_Category);
                this.txtStatus = (TextView) rowView.findViewById(R.id.tv_Status);
                this.imgFilePath = (ImageView)rowView.findViewById(R.id.imgFilePath);
                this.linearLayout = (LinearLayout)rowView.findViewById(R.id.linear);
                this.linearImages = (LinearLayout) rowView.findViewById(R.id.linearImages);
                this.imgFilePath.setBackgroundResource(R.drawable.rippleeffect);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.custom_mypostings, parent, false);

            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final MyPostingModel.Result dataModel = dataListFiltered.get(position);
            holder.txtTitle.setText(dataModel.getTitle());
            holder.txtDescription.setText(dataModel.getDescription());
            holder.txtLocation.setText(dataModel.getLocation());
            holder.txtDateTime.setText(getPostedDate(dataModel.getPostedDate()));
            holder.txtCategory.setText(getCategoryName(dataModel.getCategoryID()));
            holder.txtStatus.setText("Status : "+ dataModel.getStatus());

            ArrayList<String> lstFilepaths = new ArrayList<>();
            String strFilePaths = dataModel.getFilePath();
            if (strFilePaths != null && !strFilePaths.equalsIgnoreCase("")) {
                holder.linearImages.setVisibility(View.VISIBLE);
                String[] strFilepathArray = strFilePaths.split(",");
                if (strFilepathArray.length > 0) {
                    if (strFilepathArray.length > 1) {

                        holder.linearLayout.setVisibility(View.VISIBLE);
                        if(holder.linearLayout.getChildCount() > 0)
                            holder.linearLayout.removeAllViews();

                        for (int i = 0; i < strFilepathArray.length; i++) {
                            System.out.println("Image URL ::: " + strFilepathArray[i].trim().toString());
                            final String strFile = strFilepathArray[i].trim().toString();
                            if (strFile != null && !strFile.equalsIgnoreCase("")) {
                                lstFilepaths.add(strFilepathArray[i]);
                                final ImageView imageView = new ImageView(context);
                                LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(350, 350);
                                imageView.setLayoutParams(parms);
                                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                                imageView.setBackgroundResource(R.drawable.rippleeffect);
                                imageView.setId(i);
                                imageView.setImageResource(R.mipmap.ic_refresh);
                                imageView.setContentDescription(strFilepathArray[i].trim().toString());
                                imageView.setPadding(5, 5, 5, 5);
                                if (i == 0)
                                    loadImages(strFile, imageView, holder.linearLayout, true, holder.imgFilePath, strFilepathArray.length);
                                else
                                    loadImages(strFile, imageView, holder.linearLayout, false, holder.imgFilePath, strFilepathArray.length);
                                final ImageView img = holder.imgFilePath;

                                final ViewHolder finalHolder1 = holder;
                                imageView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        String url = imageView.getContentDescription().toString();
                                        loadImagePath(url, finalHolder1.imgFilePath,context);
                                    }
                                });
                            }
                        }
                    } else {
                        String strFile = strFilepathArray[0].trim().toString();
                        try {
                            if (strFile != null && !strFile.equalsIgnoreCase("")) {
                                loadImages(strFile, null, holder.linearLayout, true, holder.imgFilePath, strFilepathArray.length);
                            }
                        } catch (Exception e) {
                            System.out.println(e);
                        }
                    }
                } else
                    holder.linearImages.setVisibility(View.GONE);
            } else {
                holder.linearImages.setVisibility(View.GONE);
            }

            holder.imgFilePath.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String content = v.getContentDescription().toString();
                    try {
                        openFile(Uri.parse(content), content, context);
                    } catch (ActivityNotFoundException e) {
                        e.printStackTrace();
                        Toast.makeText(context, "No Activity found to handle this file. Need to install supported Application", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence charSequence) {

                    String charString = charSequence.toString();

                    if (charString.isEmpty()) {

                        dataListFiltered = dataSet;
                    } else {

                        ArrayList<MyPostingModel.Result> list = new ArrayList<>();

                        for (MyPostingModel.Result result : dataSet) {

                            if (result.getTitle().toLowerCase().contains(charString)) {

                                list.add(result);
                            }
                        }
                        dataListFiltered = list;
                    }

                    FilterResults filterResults = new FilterResults();
                    filterResults.values = dataListFiltered;
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                    dataListFiltered = (ArrayList<MyPostingModel.Result>) filterResults.values;
                    MyPostingAdapter.this.notifyDataSetChanged();
                }
            };
        }

        private void openFile(Uri uri, String url, Context ctx) {
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


        private String getCategoryName(int categoryID) {
            String Catergory ="";
            if (lstCatagories.size() > 0) {
                for (int i = 0; i < lstCatagories.size(); i++) {
                    hshMapCategoryLst.put(lstCatagories.get(i).getCategoryID().toString().trim(), lstCatagories.get(i).getName().toString().trim());
                }
            }
            for (Map.Entry mapElement : hshMapCategoryLst.entrySet()) {
                String key = (String)mapElement.getKey();
                int nKey = Integer.valueOf(key);
                if(nKey == categoryID)
                    Catergory= mapElement.getValue().toString();
                System.out.println(key + " : " + Catergory);
            }
            return Catergory;
        }

        @Override
        public int getItemCount() {
            return dataListFiltered.size();
        }



        public void loadImages(String url, final ImageView img, LinearLayout linearLayout, boolean first, final ImageView imgFilePath, int length) {
            String strMimeType = getExtensionType(Uri.parse(url), context);
            if (strMimeType != null && !strMimeType.equalsIgnoreCase("")) {
                if (strMimeType.equalsIgnoreCase("jpg") || strMimeType.equalsIgnoreCase("jpeg")  || strMimeType.equalsIgnoreCase("png")) {
                    //imageLoader.DisplayImage(url, img);
                    if (first) {
                        imgFilePath.setContentDescription(url);
                        //imageLoader.DisplayImage(url, imgFilePath);
                        Glide.with(context).load(url).into(imgFilePath);
                    }else
                        Glide.with(context).load(url).override(600, 300).into(img);

                    if (length == 1)
                        linearLayout.setVisibility(View.GONE);
                    else {
                        linearLayout.setVisibility(View.VISIBLE);
                        Glide.with(context).load(url).override(600, 300).into(img);
                    }
                } else if (strMimeType.equalsIgnoreCase("mp4")) {
                    if (first) {
                        Glide.with(context).load(R.drawable.mp4).into(imgFilePath);
                        imgFilePath.setContentDescription(url);
                    }
                    if (length == 1)
                        linearLayout.setVisibility(View.GONE);
                    else {
                        linearLayout.setVisibility(View.VISIBLE);
                        Glide.with(context).load(R.drawable.mp4).override(600, 300).into(img);
                    }
                } else if (strMimeType.equalsIgnoreCase("application/pdf") || strMimeType.equalsIgnoreCase("pdf")) {
                    if (first) {
                        Glide.with(context).load(R.drawable.pdf).into(imgFilePath);
                        imgFilePath.setContentDescription(url);
                    }
                    if (length == 1)
                        linearLayout.setVisibility(View.GONE);
                    else {
                        linearLayout.setVisibility(View.VISIBLE);
                        Glide.with(context).load(R.drawable.pdf).override(600, 300).into(img);
                    }
                } else if (strMimeType.equalsIgnoreCase("docx") || strMimeType.equalsIgnoreCase("doc") || strMimeType.equalsIgnoreCase("application/vnd.openxmlformats-officedocument.wordprocessingml.document") || strMimeType.equalsIgnoreCase("application/msword")) {
                    if (first) {
                        Glide.with(context).load(R.drawable.doc).into(imgFilePath);
                        imgFilePath.setContentDescription(url);
                    }
                    if (length == 1)
                        linearLayout.setVisibility(View.GONE);
                    else {
                        Glide.with(context).load(R.drawable.doc).override(600, 300).into(img);
                        linearLayout.setVisibility(View.VISIBLE);
                    }
                } else if (strMimeType.equalsIgnoreCase("xlsx") || strMimeType.equalsIgnoreCase("xls") || strMimeType.equalsIgnoreCase("application/vnd.ms-excel") || strMimeType.equalsIgnoreCase("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
                    if (first) {
                        Glide.with(context).load(R.drawable.excel).into(imgFilePath);
                        imgFilePath.setContentDescription(url);
                    }
                    if (length == 1)
                        linearLayout.setVisibility(View.GONE);
                    else {
                        linearLayout.setVisibility(View.VISIBLE);
                        Glide.with(context).load(R.drawable.excel).override(600, 300).into(img);
                    }
                } else if (strMimeType.equalsIgnoreCase("ppt") || strMimeType.equalsIgnoreCase("application/vnd.ms-powerpoint") || strMimeType.equalsIgnoreCase("application/vnd.openxmlformats-officedocument.presentationml.presentation")) {
                    if (first) {
                        Glide.with(context).load(R.drawable.powerpoint).into(imgFilePath);
                        imgFilePath.setContentDescription(url);
                    }
                    if (length == 1)
                        linearLayout.setVisibility(View.GONE);
                    else {
                        linearLayout.setVisibility(View.VISIBLE);
                        Glide.with(context).load(R.drawable.powerpoint).override(600, 300).into(img);
                    }
                } else if (strMimeType.equalsIgnoreCase("txt") || strMimeType.equalsIgnoreCase("text/plain")) {
                    if (first) {
                        Glide.with(context).load(R.drawable.txt).into(imgFilePath);
                        imgFilePath.setContentDescription(url);
                    }
                    if (length == 1)
                        linearLayout.setVisibility(View.GONE);
                    else {
                        linearLayout.setVisibility(View.VISIBLE);
                        Glide.with(context).load(R.drawable.txt).override(600, 300).into(img);
                    }
                } else if (strMimeType.equalsIgnoreCase("mp3") || strMimeType.equalsIgnoreCase("audio/mpeg")) {
                    if (first) {
                        Glide.with(context).load(R.drawable.mp3).into(imgFilePath);
                        imgFilePath.setContentDescription(url);
                    }
                    if (length == 1)
                        linearLayout.setVisibility(View.GONE);
                    else {
                        linearLayout.setVisibility(View.VISIBLE);
                        Glide.with(context).load(R.drawable.mp3).override(600, 300).into(img);
                    }
                }
                if (linearLayout != null) {
                    linearLayout.addView(img);
                    if (length == 1)
                        linearLayout.setVisibility(View.GONE);
                    else {
                        linearLayout.setVisibility(View.VISIBLE);
                    }
                }
            }
        }

        private String getPostedDate(String postedDate)
        {
            if(postedDate.contains("T"))
            {
                String[] strDateZ = null;
                String date ="";
                Duration diff = null;

                String[] strPostedDate = postedDate.split("T");
                String strDate = strPostedDate[0] + " " + strPostedDate[1];
                if(strDate.contains("Z"))
                {
                    strDateZ = strDate.split("Z");
                    date = strDateZ[0];
                }
                try {
                    if(strDateZ != null  && !date.equalsIgnoreCase("")) {
                        SimpleDateFormat spf = new SimpleDateFormat("yyyy-MM-dd hh:mm:sss");
                        try {

                            Date oldDate = spf.parse(date);
                            System.out.println(oldDate);

                            int day = 0;
                            int hh = 0;
                            int mm = 0;
                            Date currentDate = new Date();
                            Long timeDiff = currentDate.getTime() - oldDate.getTime();
                            day = (int) TimeUnit.MILLISECONDS.toDays(timeDiff);
                            hh = (int) (TimeUnit.MILLISECONDS.toHours(timeDiff) - TimeUnit.DAYS.toHours(day));
                            mm = (int) (TimeUnit.MILLISECONDS.toMinutes(timeDiff) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(timeDiff)));


                            if (mm <= 60 && hh!= 0) {
                                //if (hh <= 60 && day != 0) {
                                if (hh <= 60 && day != 0) {
                                    if(day > 5){
                                        spf = new SimpleDateFormat("dd MMM yyyy");
                                        date = spf.format(oldDate);
                                        return date;
                                    }
                                    else
                                        return day + " Days";
                                } else {
                                    return hh + " Hrs";
                                }
                            } else {
                                return mm + " Min";
                            }
                        } catch (ParseException e) {

                            e.printStackTrace();
                        }

                    }
                    return date;
                }catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            return postedDate;
        }
    }
}
