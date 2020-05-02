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
    ArrayList<MyPostingModel.Result> myPostingResult;
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
        showProgressDialog(MyPostingsActivity.this);
        getMyPostingDetails();
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


        public MyPostingAdapter(FragmentActivity context,ArrayList<MyPostingModel.Result> data) {
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
            final MyPostingModel.Result dataModel = dataSet.get(position);
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
