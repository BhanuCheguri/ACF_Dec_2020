package com.joinacf.acf.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.joinacf.acf.R;
import com.joinacf.acf.databinding.ActivityMyPostingsBinding;
import com.joinacf.acf.modelclasses.MyPostingModel;
import com.joinacf.acf.network.APIInterface;
import com.joinacf.acf.network.APIRetrofitClient;
import com.joinacf.acf.petitions.PetitionModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MyPostingsActivity extends BaseActivity {

    private ActivityMyPostingsBinding dataBiding;
    ArrayList<PetitionModel> model;
    APIRetrofitClient apiRetrofitClient;

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

        apiRetrofitClient = new APIRetrofitClient();
        getMyPostingDetails();
    }

    private void getMyPostingDetails() {
        String strMemberID = getStringSharedPreference(MyPostingsActivity.this,"MemberID");

        final ArrayList<MyPostingModel> lstPostsModel = new ArrayList<>();

        Retrofit retrofit = apiRetrofitClient.getRetrofit(APIInterface.BASE_URL);
        APIInterface api = retrofit.create(APIInterface.class);
       // final Call<List<MyPostingModel>> call = api.getMyPostings(strMemberID);
        final Call<List<MyPostingModel>> call = api.getMyPostings("1");
        call.enqueue(new Callback<List<MyPostingModel>>() {
            @Override
            public void onResponse(Call<List<MyPostingModel>> call, Response<List<MyPostingModel>> response) {
                List<MyPostingModel> myPostingsData = response.body();

                //String[] heroes = new String[myPostingsData.size()];

                //looping through all the heroes and inserting the names inside the string array
                for (int i = 0; i < myPostingsData.size(); i++) {
                    MyPostingModel myPostsModel = new MyPostingModel();
                    myPostsModel.setItemID(myPostingsData.get(i).getItemID());
                    myPostsModel.setTitle(myPostingsData.get(i).getTitle());
                    myPostsModel.setDescription(myPostingsData.get(i).getDescription());
                    myPostsModel.setCategoryID(myPostingsData.get(i).getCategoryID());
                    myPostsModel.setPostedDate(myPostingsData.get(i).getPostedDate());
                    myPostsModel.setLocation(myPostingsData.get(i).getLocation());
                    myPostsModel.setLatitude(myPostingsData.get(i).getLatitude());
                    myPostsModel.setLangitude(myPostingsData.get(i).getLangitude());
                    myPostsModel.setStatus(myPostingsData.get(i).getStatus());
                    lstPostsModel.add(myPostsModel);
                }
                dataBiding.lvPosting.setLayoutManager(new LinearLayoutManager(MyPostingsActivity.this));
                dataBiding.lvPosting.setItemAnimator(new DefaultItemAnimator());
                dataBiding.lvPosting.setAdapter(new MyPostingAdapter(MyPostingsActivity.this,lstPostsModel));
            }

            @Override
            public void onFailure(Call<List<MyPostingModel>> call, Throwable t) {
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    public class MyPostingAdapter extends RecyclerView.Adapter<MyPostingAdapter.ViewHolder> {
        public ArrayList<MyPostingModel> dataSet;
        FragmentActivity context;
        private LayoutInflater inflater=null;


        public MyPostingAdapter(FragmentActivity context,ArrayList<MyPostingModel> data) {
            this.dataSet = data;
            this.context = context;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            TextView txtTitle;
            TextView txtDescription;
            TextView txtLocation;
            TextView txtDateTime;

            public ViewHolder(View rowView) {
                super(rowView);
                this.txtTitle = (TextView) rowView.findViewById(R.id.tv_title);
                this.txtDescription = (TextView) rowView.findViewById(R.id.tv_description);
                this.txtLocation = (TextView) rowView.findViewById(R.id.tv_location);
                this.txtDateTime = (TextView) rowView.findViewById(R.id.tv_DateTime);
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
            final MyPostingModel dataModel = dataSet.get(position);
            holder.txtTitle.setText(dataModel.getTitle());
            holder.txtDescription.setText(dataModel.getDescription());
            holder.txtLocation.setText(dataModel.getLocation());
            holder.txtDateTime.setText(getPostedDate(dataModel.getPostedDate()));
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

                            // Log.e("toyBornTime", "" + toyBornTime);

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

   /* private void getMyPostingDetails() {
        String strMemberID = getStringSharedPreference(MyPostingsActivity.this,"MemberID");

        final ArrayList<MyPostsModel> lstPostsModel = new ArrayList<>();
        final MyPostsModel myPostsModel = new MyPostsModel();

        Retrofit retrofit = apiRetrofitClient.getRetrofit(APIInterface.BASE_URL);
        APIInterface api = retrofit.create(APIInterface.class);
        final Call<List<MyPostsModel>> call = api.getMyPostings(strMemberID);
        call.enqueue(new Callback<List<MyPostsModel>>() {
            @Override
            public void onResponse(Call<List<MyPostsModel>> call, Response<List<MyPostsModel>> response) {
                List<MyPostsModel> myPostingsData = response.body();

                String[] heroes = new String[myPostingsData.size()];

                //looping through all the heroes and inserting the names inside the string array
                for (int i = 0; i < myPostingsData.size(); i++) {
                    myPostsModel.setCreatedDate(myPostingsData.get(i).getCreatedDate());
                    myPostsModel.setFullName(myPostingsData.get(i).getFullName());
                    myPostsModel.setImage1(myPostingsData.get(i).getImage1());
                    myPostsModel.setImage2(myPostingsData.get(i).getImage2());
                    myPostsModel.setImage3(myPostingsData.get(i).getImage3());
                    myPostsModel.setImage4(myPostingsData.get(i).getImage4());
                    myPostsModel.setImage5(myPostingsData.get(i).getImage5());
                    myPostsModel.setImage6(myPostingsData.get(i).getImage6());
                    myPostsModel.setMobile(myPostingsData.get(i).getMobile());
                    myPostsModel.setTitle(myPostingsData.get(i).getTitle());
                    myPostsModel.setStatus(myPostingsData.get(i).getStatus());
                    myPostsModel.setRemarks(myPostingsData.get(i).getRemarks());
                    myPostsModel.setPetitionID(myPostingsData.get(i).getPetitionID());
                    myPostsModel.setVerificationCode(myPostingsData.get(i).getVerificationCode());
                    myPostsModel.setVerificationDate(myPostingsData.get(i).getVerificationDate());
                    lstPostsModel.add(myPostsModel);
                }

                dataBiding.lvPosting.setLayoutManager(new LinearLayoutManager(MyPostingsActivity.this));
                dataBiding.lvPosting.setItemAnimator(new DefaultItemAnimator());
                dataBiding.lvPosting.setAdapter(new MyPostingAdapter(MyPostingsActivity.this,lstPostsModel));
            }

            @Override
            public void onFailure(Call<List<MyPostsModel>> call, Throwable t) {
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    public class MyPostingAdapter extends RecyclerView.Adapter<MyPostingAdapter.ViewHolder> {
        public ArrayList<MyPostsModel> dataSet;
        FragmentActivity context;
        private LayoutInflater inflater=null;


        public MyPostingAdapter(FragmentActivity context,ArrayList<MyPostsModel> data) {
            this.dataSet = data;
            this.context = context;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            TextView txtTitle;
            TextView txtFullName;
            TextView txtMobileNo;
            TextView txtRemarks;
            TextView txtVerificationCode;
            TextView txtVerified;

            public ViewHolder(View rowView) {
                super(rowView);
                this.txtTitle = (TextView) rowView.findViewById(R.id.tv_Title);
                this.txtFullName = (TextView) rowView.findViewById(R.id.tv_FullName);
                this.txtMobileNo = (TextView) rowView.findViewById(R.id.tv_MobileNo);
                this.txtRemarks = (TextView) rowView.findViewById(R.id.tv_Remarks);
                this.txtVerificationCode = (TextView) rowView.findViewById(R.id.tv_verificationCode);
                this.txtVerified = (TextView) rowView.findViewById(R.id.tvVerified);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.custom_myposts_list_item, parent, false);

            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final MyPostsModel dataModel = dataSet.get(position);
            holder.txtTitle.setText(dataModel.getTitle());
            holder.txtVerificationCode.setText(dataModel.getVerificationCode());
            holder.txtRemarks.setText(dataModel.getRemarks());
            holder.txtFullName.setText(dataModel.getFullName());
            holder.txtMobileNo.setText(dataModel.getMobile());
            if(dataModel.getVerificationDate().equalsIgnoreCase(""))
                holder.txtVerified.setText("Not Verified");
            else
                holder.txtVerified.setText("Verified");
        }

        @Override
        public int getItemCount() {
            return dataSet.size();
        }
    }
}*/
