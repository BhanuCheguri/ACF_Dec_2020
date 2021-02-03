package com.anticorruptionforce.acf.fragments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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
import android.widget.Toast;


import com.anticorruptionforce.acf.R;
import com.anticorruptionforce.acf.activities.AddPetitionActivity;
import com.anticorruptionforce.acf.activities.MainActivity;
import com.anticorruptionforce.acf.activities.NewComplaintActivity;
import com.anticorruptionforce.acf.databinding.ActivityPetetionBinding;
import com.anticorruptionforce.acf.network.APIInterface;
import com.anticorruptionforce.acf.network.APIRetrofitClient;
import com.anticorruptionforce.acf.modelclasses.PetitionModel;
import com.anticorruptionforce.acf.adapters.PetitionsListAdapter;
import com.anticorruptionforce.acf.utilities.App;
import com.pd.chocobar.ChocoBar;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static android.app.Activity.RESULT_OK;

public class MyPetitionListFragment extends BaseFragment {

    ArrayList<PetitionModel> model;
    String strCategoryID = "";
    String strName = "";
    ActivityPetetionBinding dataBiding;
    APIRetrofitClient apiRetrofitClient;
    ArrayList<PetitionModel.Result> lstPetitionData;
    PetitionsListAdapter adapter;

    public static MyPetitionListFragment newInstance() {
        return new MyPetitionListFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        dataBiding = DataBindingUtil.inflate(inflater, R.layout.activity_petetion, null, false);
        //dataBiding = DataBindingUtil.setContentView(getActivity(), R.layout.activity_petetion);
        setActionBarTitle(getString(R.string.title_socialEvil));

        init();

        LoadAdapter();

        return dataBiding.getRoot();
    }

    private void LoadAdapter() {
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
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void populateListView(ArrayList<PetitionModel.Result> PetitionData) {
        adapter = new PetitionsListAdapter(getActivity(),PetitionData, "MyPetitions");
        dataBiding.lvPetitions.setLayoutManager(new LinearLayoutManager(getActivity()));
        dataBiding.lvPetitions.setAdapter(adapter);
        hideProgressDialog(getActivity());
    }

    private void  init() {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setHomeAsUpIndicator(R.mipmap.ic_launcher_icon);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.gradient_theme));

        ((AppCompatActivity) getActivity()).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Petitions");

        ((MainActivity)getActivity()).showBottomNavigation();

        dataBiding.lvPetitions.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    Log.e(getClass().toString(), "scrolling up");
                    ((MainActivity)getActivity()).hideBottomNavigation();
                } else {
                    Log.e(getClass().toString(), "scrolling down");
                    ((MainActivity)getActivity()).showBottomNavigation();
                }
            }
        });

        dataBiding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getActivity(), AddPetitionActivity.class);
                getActivity().startActivity(intent);

            }
        });

        // Setup refresh listener which triggers new data loading
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

    public void getMyPetitions(String MemberID) {
        apiRetrofitClient = new APIRetrofitClient();
        Retrofit retrofit = apiRetrofitClient.getRetrofit(APIInterface.BASE_URL);
        APIInterface api = retrofit.create(APIInterface.class);
        Call<PetitionModel> call = api.getMyPetitions(MemberID);

        call.enqueue(new Callback<PetitionModel>() {
            @Override
            public void onResponse(Call<PetitionModel> call, Response<PetitionModel> response) {
                System.out.println("getMyPetitions::"+ response);
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
                //Toast.makeText(getActivity(), t.getMessage(), Toast.LENGTH_SHORT).show();
                hideProgressDialog(getActivity());
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.add_petition, menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSubmitButtonEnabled(true);
        searchView.setQueryHint("Search with Title");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (adapter!=null)
                    adapter.getFilter().filter(newText);
                return true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId())
        {
            case android.R.id.home:
                break;
            /*case R.id.add_petition:
                Intent intent = new Intent(getActivity(), AddPetitionActivity.class);
                getActivity().startActivity(intent);
                break;*/
            case R.id.help:
                Toast.makeText(getActivity(), "Help", Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
