package com.joinacf.acf.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.joinacf.acf.R;
import com.joinacf.acf.adapters.PetitionsListAdapter;
import com.joinacf.acf.databinding.ActivityServiceProviderBinding;
import com.joinacf.acf.modelclasses.PetitionModel;
import com.joinacf.acf.modelclasses.SPLoginModel;
import com.joinacf.acf.modelclasses.PetitionModel;
import com.joinacf.acf.modelclasses.PetitionModel;
import com.joinacf.acf.network.APIInterface;
import com.joinacf.acf.network.APIRetrofitClient;
import com.joinacf.acf.network.AppLocationService;
import com.joinacf.acf.utilities.App;
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
    PetitionsListAdapter adapter;
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

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        strSectionID = getStringSharedPreference(ServiceProviderActivity.this,"SectionID");
        strSPID =  getStringSharedPreference(ServiceProviderActivity.this,"SPID");
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
    private void getSPPetitions(String spid, String sectionid)
    {
        //showProgressDialog(ServiceProviderActivity.this);
        apiRetrofitClient = new APIRetrofitClient();
        Retrofit retrofit = apiRetrofitClient.getRetrofit(APIInterface.BASE_URL);
        APIInterface api = retrofit.create(APIInterface.class);
        Call<PetitionModel> call = api.getSPpetitions(spid,sectionid);

        call.enqueue(new Callback<PetitionModel>() {
            @Override
            public void onResponse(Call<PetitionModel> call, Response<PetitionModel> response) {
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
        adapter = new PetitionsListAdapter(ServiceProviderActivity.this,PetitionData,"SPPetitions");
        binding.lvSPpetitions.setAdapter(adapter);
        hideProgressDialog(ServiceProviderActivity.this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.logout, menu);
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
}
