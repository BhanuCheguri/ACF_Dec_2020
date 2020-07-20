package com.joinacf.acf.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.Toast;


import com.joinacf.acf.R;
import com.joinacf.acf.databinding.ActivityPetetionBinding;
import com.joinacf.acf.fragments.BaseFragment;
import com.joinacf.acf.network.APIInterface;
import com.joinacf.acf.network.APIRetrofitClient;
import com.joinacf.acf.modelclasses.PetitionModel;
import com.joinacf.acf.adapters.PetitionsListAdapter;
import com.joinacf.acf.utilities.App;
import com.pd.chocobar.ChocoBar;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MyPetitionListActivity extends BaseFragment {

    ArrayList<PetitionModel> model;
    String strCategoryID = "";
    String strName = "";
    ActivityPetetionBinding dataBiding;
    APIRetrofitClient apiRetrofitClient;
    ArrayList<PetitionModel.Result> lstPetitionData;
    PetitionsListAdapter adapter;
    int mLastFirstVisibleItem;
    int mLastVisibleItemCount;

    public static MyPetitionListActivity newInstance() {
        return new MyPetitionListActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        dataBiding = DataBindingUtil.inflate(inflater, R.layout.activity_petetion, null, false);
        //dataBiding = DataBindingUtil.setContentView(getActivity(), R.layout.activity_petetion);
        setActionBarTitle(getString(R.string.title_socialEvil));

        init();

        String MemberID = getStringSharedPreference(getActivity(), "MemberID");
        showProgressDialog(getActivity());
        if(App.isNetworkAvailable())
            getMyPetitions(MemberID);
        else{
            ChocoBar.builder().setView(dataBiding.mainLayout)
                    .setText("No Internet connection")
                    .setDuration(ChocoBar.LENGTH_INDEFINITE)
                    //.setActionText(android.R.string.ok)
                    .red()   // in built red ChocoBar
                    .show();
        }

        return dataBiding.getRoot();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void populateListView(ArrayList<PetitionModel.Result> PetitionData) {
        adapter = new PetitionsListAdapter(getActivity(),PetitionData, "MyPetitions");
        dataBiding.lvPetitions.setAdapter(adapter);
        hideProgressDialog(getActivity());
    }

    private void  init() {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setHomeAsUpIndicator(R.mipmap.ic_launcher_icon);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.gradient_theme));

        ((AppCompatActivity) getActivity()).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
       /* Bundle b = getIntent().getExtras();
        if (b != null)
        {
            strCategoryID = b.getString("CatergoryID").toString();
            strName = b.getString("Name").toString();
        }*/
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Petitions List");

        ((MainActivity)getActivity()).showBottomNavigation();

        dataBiding.lvPetitions.setOnScrollListener(new AbsListView.OnScrollListener() {
            int last_item;
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                if (mLastFirstVisibleItem > firstVisibleItem) {
                    Log.e(getClass().toString(), "scrolling up");
                    ((MainActivity)getActivity()).hideBottomNavigation();
                } else if (mLastFirstVisibleItem < firstVisibleItem) {
                    Log.e(getClass().toString(), "scrolling down");
                    ((MainActivity)getActivity()).showBottomNavigation();
                } else if (mLastVisibleItemCount < visibleItemCount) {
                    Log.e(getClass().toString(), "scrolling down");
                    ((MainActivity)getActivity()).showBottomNavigation();
                } else if (mLastVisibleItemCount > visibleItemCount) {
                    Log.e(getClass().toString(), "scrolling up");
                    ((MainActivity)getActivity()).hideBottomNavigation();
                }
                mLastFirstVisibleItem = firstVisibleItem;
                mLastVisibleItemCount = visibleItemCount;
            }
        });

    }


    public void getMyPetitions(String MemberID) {
        apiRetrofitClient = new APIRetrofitClient();
        Retrofit retrofit = apiRetrofitClient.getRetrofit(APIInterface.BASE_URL);
        APIInterface api = retrofit.create(APIInterface.class);
        Call<PetitionModel> call = api.getMyPetitions(MemberID);

        call.enqueue(new Callback<PetitionModel>() {
            @Override
            public void onResponse(Call<PetitionModel> call, Response<PetitionModel> response) {
                if(response != null) {
                    PetitionModel myPetitionData  = response.body();
                    if(myPetitionData != null) {
                        String status = myPetitionData.getStatus();
                        String msg = myPetitionData.getMessage();
                        if(msg.equalsIgnoreCase("SUCCESS")) {
                            lstPetitionData = myPetitionData.getResult();
                            populateListView(lstPetitionData);
                        }else
                        {
                            hideProgressDialog(getActivity());
                        }
                    }else {
                        hideProgressDialog(getActivity());
                    }
                }else {
                    hideProgressDialog(getActivity());
                }
            }

            @Override
            public void onFailure(Call<PetitionModel> call, Throwable t) {
                Toast.makeText(getActivity(), t.getMessage(), Toast.LENGTH_SHORT).show();
                hideProgressDialog(getActivity());
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.add_petition, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId())
        {
            case android.R.id.home:
                break;
            case R.id.add_petition:
                Intent intent = new Intent(getActivity(), AddPetitionActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
