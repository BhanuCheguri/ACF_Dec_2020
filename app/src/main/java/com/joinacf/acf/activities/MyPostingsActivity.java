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
import com.joinacf.acf.modelclasses.MyPostsModel;
import com.joinacf.acf.modelclasses.MyProfileModel;
import com.joinacf.acf.network.APIInterface;
import com.joinacf.acf.network.APIRetrofitClient;
import com.joinacf.acf.petitions.MyPetitionListActivity;
import com.joinacf.acf.petitions.PetitionModel;
import com.joinacf.acf.petitions.PetitionsListAdapter;

import java.util.ArrayList;
import java.util.List;

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

    /*private void LoadAdapter() {
        model= new ArrayList<>();

        model.add(new PetitionModel("1","RK Reddy","Not Verified","Complain on cheating","12th Aug 2019 , 7:20PM" ,"0"));
        model.add(new PetitionModel("2","RK Reddy","Not Verified","Complain on cheating","14th Aug 2019 , 8:20PM" ,"0"));
        model.add(new PetitionModel("3","RK Reddy","Verified","Complain on cheating","18th Aug 2019 , 2:20PM" ,"0"));
        model.add(new PetitionModel("4","RK Reddy","Not Verified","Complain on cheating","19th Aug 2019 , 1:20PM" ,"0"));
        model.add(new PetitionModel("5","RK Reddy","Verified","Complain on cheating","22th Aug 2019 , 9:20PM" ,"0"));

        MyPostingAdapter adaper = new MyPostingAdapter(MyPostingsActivity.this,model);
        dataBiding.lvPosting.setAdapter(adaper);
    }*/

    private void  init() {
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
}
