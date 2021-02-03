package com.anticorruptionforce.acf.activities;

import androidx.annotation.NonNull;
import androidx.core.view.MenuItemCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.SearchView;
import android.widget.Toast;

import com.anticorruptionforce.acf.R;
import com.anticorruptionforce.acf.adapters.PetitionsListAdapter;
import com.anticorruptionforce.acf.adapters.SPPetitionsListAdapter;
import com.anticorruptionforce.acf.databinding.ActivityServiceProviderBinding;
import com.anticorruptionforce.acf.modelclasses.PetitionModel;
import com.anticorruptionforce.acf.network.APIInterface;
import com.anticorruptionforce.acf.network.APIRetrofitClient;
import com.anticorruptionforce.acf.utilities.App;
import com.pd.chocobar.ChocoBar;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ServiceProviderActivity extends BaseActivity {

    ActivityServiceProviderBinding binding;
    APIRetrofitClient apiRetrofitClient;
    PetitionModel myResponse;
    ArrayList<PetitionModel.Result> lstPetitionData;
    String strSPID="",strSectionID= "";
    SPPetitionsListAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_service_provider);
        init();
    }

    public void init()
    {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.mipmap.ic_launcher_icon);
        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.gradient_theme));
        setActionBarTitle(getStringSharedPreference(ServiceProviderActivity.this,"ProviderName"));
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        strSectionID = getStringSharedPreference(ServiceProviderActivity.this,"SectionID");
        strSPID =  getStringSharedPreference(ServiceProviderActivity.this,"SPID");


        binding.swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                new AsyncGetSPpetitions().execute();
            }
        });
        // Configure the refreshing colors
        binding.swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        new AsyncGetSPpetitions().execute();
    }

    public class AsyncGetSPpetitions extends AsyncTask<String,String,String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog(ServiceProviderActivity.this, "Please wait...\nFetching Service Provider Data");
        }

        @Override
        protected String doInBackground(String... strings) {
            if(App.isNetworkAvailable())
                getSPPetitions(strSPID,strSectionID);
            else{
                ChocoBar.builder().setView(binding.linearLayout)
                        .setText("No Internet connection")
                        .setDuration(ChocoBar.LENGTH_INDEFINITE)
                        //.setActionText(android.R.string.ok)
                        .red()   // in built red ChocoBar
                        .show();
            }
            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            hideProgressDialog(ServiceProviderActivity.this);

        }
    }
    public void getSPPetitions(String spid, String sectionid)
    {
        //showProgressDialog(ServiceProviderActivity.this);
        apiRetrofitClient = new APIRetrofitClient();
        Retrofit retrofit = apiRetrofitClient.getRetrofit(APIInterface.BASE_URL);
        APIInterface api = retrofit.create(APIInterface.class);
        Call<PetitionModel> call = api.getSPpetitions(spid,sectionid);

        call.enqueue(new Callback<PetitionModel>() {
            @Override
            public void onResponse(Call<PetitionModel> call, Response<PetitionModel> response) {
                System.out.println("getSPpetitions::"+ response);
                hideProgressDialog(ServiceProviderActivity.this);
                if (response != null) {
                    binding.llNoData.setVisibility(View.GONE);
                    myResponse = response.body();
                    if (myResponse != null) {
                        String status = myResponse.getStatus();
                        String msg = myResponse.getMessage();
                        if (msg.equalsIgnoreCase("SUCCESS")) {
                            lstPetitionData = myResponse.getResult();
                            populateListView(lstPetitionData);
                        } else
                            binding.llNoData.setVisibility(View.VISIBLE);
                    } else
                        binding.llNoData.setVisibility(View.VISIBLE);
                } else
                    binding.llNoData.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(Call<PetitionModel> call, Throwable t) {
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
                hideProgressDialog(ServiceProviderActivity.this);
            }
        });
    }

    private void populateListView(ArrayList<PetitionModel.Result> PetitionData) {
        adapter = new SPPetitionsListAdapter(ServiceProviderActivity.this,PetitionData);
        binding.lvSPpetitions.setHasFixedSize(true);
        binding.lvSPpetitions.setAdapter(adapter);
        binding.lvSPpetitions.setLayoutManager(new LinearLayoutManager(this));
        hideProgressDialog(ServiceProviderActivity.this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.logout, menu);

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
                stopSwipeRefresh();
                if (adapter!=null)
                    adapter.getFilter().filter(query);
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId())
        {
            case android.R.id.home:
                break;
            case R.id.logout:
                putBooleanSharedPreference(ServiceProviderActivity.this, "AdminLoginForM", false);
                putBooleanSharedPreference(ServiceProviderActivity.this, "AdminLoginForSP", false);
                putBooleanSharedPreference(ServiceProviderActivity.this, "AdminLogin", false);
                Intent intent = new Intent(ServiceProviderActivity.this,NewLoginActivity.class);
                startActivity(intent);
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void stopSwipeRefresh()
    {
        if(binding.swipeContainer.isRefreshing()){
            binding.swipeContainer.post(new Runnable() {
                @Override
                public void run() {
                    binding.swipeContainer.setRefreshing(true);
                }
            });
        }
    }
}
