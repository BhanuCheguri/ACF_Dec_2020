package com.anticorruptionforce.acf.activities;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.anticorruptionforce.acf.R;
import com.anticorruptionforce.acf.adapters.HomePageAdapter;
import com.anticorruptionforce.acf.databinding.ActivityKnowYourActsBinding;
import com.anticorruptionforce.acf.modelclasses.WallPostsModel;
import com.anticorruptionforce.acf.network.APIInterface;
import com.anticorruptionforce.acf.network.APIRetrofitClient;
import com.anticorruptionforce.acf.utilities.App;
import com.pd.chocobar.ChocoBar;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class KnowYourActsActivity extends BaseActivity implements View.OnClickListener {
    APIRetrofitClient apiRetrofitClient;
    String strCategoryID = "";
    String strName = "";
    ActivityKnowYourActsBinding dataBiding;
    ArrayList<WallPostsModel.Result> lstWallPost;
    HomePageAdapter adapter;
    int nRadioGroup = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataBiding = DataBindingUtil.setContentView(this, R.layout.activity_know_your_acts);
        init();
    }


    private void  init() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.mipmap.ic_launcher_icon);
        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.gradient_theme));
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        Bundle b = getIntent().getExtras();
        if (b != null)
        {
            strCategoryID = b.getString("CatergoryID").toString();
            String[] strArr = strCategoryID.split("-");
            strCategoryID = strArr[0];
            strName = strArr[1];
            Log.i("activity name",strName);
            getSupportActionBar().setTitle(strName);
            setActionBarTitle(strName);
            LoadAdapter();
        }

        dataBiding.radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.rti) {
                    nRadioGroup = 0;
                    dataBiding.howtoapply.setVisibility(View.VISIBLE);
                    Toast.makeText(KnowYourActsActivity.this, "You have selected Right to Information", Toast.LENGTH_SHORT).show();
                } else if(checkedId == R.id.rts) {
                    nRadioGroup = 1;
                    dataBiding.howtoapply.setVisibility(View.GONE);
                    Toast.makeText(KnowYourActsActivity.this, "You have selected Right to Service", Toast.LENGTH_SHORT).show();
                } else {
                    nRadioGroup = 2;
                    dataBiding.howtoapply.setVisibility(View.GONE);
                    Toast.makeText(KnowYourActsActivity.this, "You have selected Citizens Chart", Toast.LENGTH_SHORT).show();
                }
            }
        });

        dataBiding.actTelugu.setOnClickListener(this);
        dataBiding.actEnglish.setOnClickListener(this);
        dataBiding.faq.setOnClickListener(this);
        dataBiding.howtoapply.setOnClickListener(this);

        dataBiding.swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                LoadAdapter();
            }
        });
        // Configure the refreshing colors
        dataBiding.swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

    }

    private void LoadAdapter()
    {
        showProgressDialog(KnowYourActsActivity.this);
        if(App.isNetworkAvailable())
            getWallPostDetails(strCategoryID,"-1");
        else{
            ChocoBar.builder().setView(dataBiding.mainLayout)
                    .setText("No Internet connection")
                    .setDuration(ChocoBar.LENGTH_INDEFINITE)
                    //.setActionText(android.R.string.ok)
                    .red()   // in built red ChocoBar
                    .show();
        }

        dataBiding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(KnowYourActsActivity.this, NewComplaintActivity.class);
                intent.putExtra("Category",strName);
                startActivity(intent);
            }
        });
    }

    public void getWallPostDetails(String categoryID, String Days) {
        apiRetrofitClient = new APIRetrofitClient();
        Retrofit retrofit = apiRetrofitClient.getRetrofit(APIInterface.BASE_URL);
        APIInterface api = retrofit.create(APIInterface.class);
        Call<WallPostsModel> call = api.getWallPostDetails(categoryID, Days);

        call.enqueue(new Callback<WallPostsModel>() {
            @Override
            public void onResponse(Call<WallPostsModel> call, Response<WallPostsModel> response) {
                if(response != null) {
                    WallPostsModel myWallData  = response.body();
                    if(myWallData != null) {
                        dataBiding.llNoData.setVisibility(View.GONE);
                        String status = myWallData.getStatus();
                        String msg = myWallData.getMessage();
                        if(msg.equalsIgnoreCase("SUCCESS")) {
                            lstWallPost = myWallData.getResult();
                            populateListView(lstWallPost);
                        }else
                        {
                            dataBiding.llNoData.setVisibility(View.VISIBLE);
                            hideProgressDialog(KnowYourActsActivity.this);
                        }
                    }else {
                        dataBiding.llNoData.setVisibility(View.VISIBLE);
                        hideProgressDialog(KnowYourActsActivity.this);
                    }
                }else {
                    dataBiding.llNoData.setVisibility(View.VISIBLE);
                    hideProgressDialog(KnowYourActsActivity.this);
                }
            }

            @Override
            public void onFailure(Call<WallPostsModel> call, Throwable t) {
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
                hideProgressDialog(KnowYourActsActivity.this);
            }
        });
    }

    private void populateListView(ArrayList<WallPostsModel.Result> wallPostData) {
        adapter = new HomePageAdapter(KnowYourActsActivity.this,wallPostData);
        dataBiding.lvMoreFeed.setLayoutManager(new LinearLayoutManager(KnowYourActsActivity.this));
        dataBiding.lvMoreFeed.setItemAnimator(new DefaultItemAnimator());
        dataBiding.lvMoreFeed.setNestedScrollingEnabled(false);
        dataBiding.lvMoreFeed.setAdapter(adapter);
        hideProgressDialog(KnowYourActsActivity.this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        switch (v.getId()){
            case R.id.act_english:
                if(nRadioGroup == 0){
                    intent.setDataAndType(Uri.parse("http://api.ainext.in/dimen_250eng.pdf"), "application/pdf");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }else if(nRadioGroup == 1){
                    intent.setDataAndType(Uri.parse("http://api.ainext.in/rtseng.pdf"), "application/pdf");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }else{http://api.ainext.in/cctel.pdf
                    intent.setDataAndType(Uri.parse("http://api.ainext.in/cceng.pdf"), "application/pdf");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                break;
            case R.id.act_telugu:
                if(nRadioGroup == 0){
                    intent.setDataAndType(Uri.parse("http://api.ainext.in/dimen_250tel.pdf"), "application/pdf");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }else if(nRadioGroup == 1){
                    intent.setDataAndType(Uri.parse("http://api.ainext.in/rtstel.pdf"), "application/pdf");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }else{
                    intent.setDataAndType(Uri.parse("http://api.ainext.in/cctel.pdf"), "application/pdf");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                break;
            case R.id.faq:
                if(nRadioGroup == 0){
                    intent.setDataAndType(Uri.parse("http://api.ainext.in/rtifaq.pdf"), "application/pdf");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                break;

            case R.id.howtoapply:
                if(nRadioGroup == 0){
                    intent.setDataAndType(Uri.parse("http://api.ainext.in/rtiappl.pdf"), "application/pdf");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                break;
            }
        }
}
