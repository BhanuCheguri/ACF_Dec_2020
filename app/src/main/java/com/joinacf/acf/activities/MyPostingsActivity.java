package com.joinacf.acf.activities;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.crashlytics.android.Crashlytics;
import com.joinacf.acf.R;
import com.joinacf.acf.adapters.HomePageAdapter;
import com.joinacf.acf.databinding.ActivityMyPostingsBinding;
import com.joinacf.acf.modelclasses.DashboardCategories;
import com.joinacf.acf.modelclasses.MyPostingModel;
import com.joinacf.acf.network.APIInterface;
import com.joinacf.acf.network.APIRetrofitClient;
import com.joinacf.acf.utilities.App;
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

public class MyPostingsActivity extends BaseActivity {

    private ActivityMyPostingsBinding dataBiding;
    ArrayList<MyPostingModel.Result> myPostingResult;
    APIRetrofitClient apiRetrofitClient;
    ArrayList<DashboardCategories.Result> lstCatagories;
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
                    Crashlytics.logException(t);
                    hideProgressDialog(MyPostingsActivity.this);
                    Toast.makeText(MyPostingsActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                    showAlert(MyPostingsActivity.this,"Error","Unable to get the Categories list","OK");
                }
            });

        }catch (Exception e)
        {
            Crashlytics.logException(e);
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
                        dataBiding.lvPosting.setLayoutManager(new LinearLayoutManager(MyPostingsActivity.this));
                        dataBiding.lvPosting.setItemAnimator(new DefaultItemAnimator());
                        dataBiding.lvPosting.setAdapter(new MyPostingAdapter(MyPostingsActivity.this,myPostingResult));
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


    public class MyPostingAdapter extends RecyclerView.Adapter<MyPostingAdapter.ViewHolder> {
        public ArrayList<MyPostingModel.Result> dataSet;
        FragmentActivity context;
        private LayoutInflater inflater=null;
        HashMap<String, String> hshMapCategoryLst = new HashMap<>();


        public MyPostingAdapter(FragmentActivity context,ArrayList<MyPostingModel.Result> data) {
            this.dataSet = data;
            this.context = context;
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
            final MyPostingModel.Result dataModel = dataSet.get(position);
            holder.txtTitle.setText(dataModel.getTitle());
            holder.txtDescription.setText(dataModel.getDescription());
            holder.txtLocation.setText(dataModel.getLocation());
            holder.txtDateTime.setText(getPostedDate(dataModel.getPostedDate()));
            holder.txtCategory.setText(getCategoryName(dataModel.getCategoryID()));
            holder.txtStatus.setText("Status : "+ dataModel.getStatus());

            ArrayList<String> lstFilepaths = new ArrayList<>();
            String strFilePaths = dataModel.getFilePath();
            if(strFilePaths != null && !strFilePaths.equalsIgnoreCase(""))
            {
                String[] strFilepathArray = strFilePaths.split(",");
                if(strFilepathArray.length > 0) {
                    holder.imgFilePath.setVisibility(View.VISIBLE);
                    for (int i = 0; i < strFilepathArray.length; i++) {

                        System.out.println("Image URL ::: "+strFilepathArray[i].trim().toString());
                        String strFile = strFilepathArray[i].trim().toString();
                        lstFilepaths.add(strFilepathArray[i]);
                        final ImageView imageView = new ImageView(context);
                        imageView.setBackgroundResource(R.drawable.rippleeffect);
                        imageView.setId(i);
                        imageView.setContentDescription(strFilepathArray[i].trim().toString());
                        imageView.setPadding(5, 5, 5, 5);
                        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        //LinearLayout.MarginLayoutParams mlp = (LinearLayout.MarginLayoutParams)imageView.getLayoutParams();
                        //mlp.setMargins(5, 0, 5, 0);
                        Glide.with(context).load(strFile).override(500, 500).into(imageView);
                        holder.linearLayout.addView(imageView);
                        final ImageView img = holder.imgFilePath;
                        Glide.with(context).load(strFilepathArray[0].trim().toString()).into(img);
                        imageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String url = imageView.getContentDescription().toString();
                                Glide.with(context).load(url).into(img);
                            }
                        });
                    }
                }
                else
                    holder.imgFilePath.setVisibility(View.GONE);
            }else
                holder.imgFilePath.setVisibility(View.GONE);
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
            return dataSet.size();
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
