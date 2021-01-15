package com.anticorruptionforce.acf.activities;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.anticorruptionforce.acf.R;
import com.anticorruptionforce.acf.databinding.ActivityServiceProviderLoginBinding;
import com.anticorruptionforce.acf.modelclasses.SPLoginModel;
import com.anticorruptionforce.acf.network.APIInterface;
import com.anticorruptionforce.acf.network.APIRetrofitClient;
import com.anticorruptionforce.acf.network.AppLocationService;
import com.anticorruptionforce.acf.utilities.App;
import com.pd.chocobar.ChocoBar;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class AdminLogin extends BaseActivity {
    AppLocationService appLocationService;
    APIRetrofitClient apiRetrofitClient;
    SPLoginModel myResponse;
    ArrayList<SPLoginModel.Result> lstLoginData;
    ActivityServiceProviderLoginBinding binding;
    String UType="";
    String strSPID="";
    String strSectionID="";
    String strProviderName="";
    boolean isRememberMeChecked = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_service_provider_login);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_service_provider_login);
        init();
    }
    
    public void init(){
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.mipmap.ic_launcher_icon);
        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.gradient_theme));

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        putBooleanSharedPreference(AdminLogin.this, "FirstTime", false);
        putBooleanSharedPreference(AdminLogin.this, "LoggedIn", false);

        apiRetrofitClient  = new APIRetrofitClient();
        appLocationService = new AppLocationService(AdminLogin.this);
        binding.btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailId = binding.emailid.getText().toString();
                String password = binding.oldPassword.getText().toString();
                if(!emailId.equalsIgnoreCase("") && emailId != null && !password.equalsIgnoreCase("") && password != null)
                {
                    if(App.isNetworkAvailable())
                        getAuthenticateModSP(emailId,password);
                    else{
                        ChocoBar.builder().setView(binding.linearLayout)
                                .setText("No Internet connection")
                                .setDuration(ChocoBar.LENGTH_INDEFINITE)
                                //.setActionText(android.R.string.ok)
                                .red()   // in built red ChocoBar
                                .show();
                    }
                }else
                    Toast.makeText(AdminLogin.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
            }
        });

        binding.chkRememberMe.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    isRememberMeChecked = true;
                    putBooleanSharedPreference(AdminLogin.this, "AdminLogin", true);
                }else {
                    isRememberMeChecked = false;
                    putBooleanSharedPreference(AdminLogin.this, "AdminLogin", false);
                }
            }
        });
    }

    private void getAuthenticateModSP(String mobile, String password)
    {
        showProgressDialog(AdminLogin.this);
        apiRetrofitClient = new APIRetrofitClient();
        Retrofit retrofit = apiRetrofitClient.getRetrofit(APIInterface.BASE_URL);
        APIInterface api = retrofit.create(APIInterface.class);
        Call<SPLoginModel> call = api.getAuthenticateModSP(String.valueOf(mobile), String.valueOf(password));

        call.enqueue(new Callback<SPLoginModel>() {
            @Override
            public void onResponse(Call<SPLoginModel> call, Response<SPLoginModel> response) {
                hideProgressDialog(AdminLogin.this);
                if (response != null) {
                    myResponse = response.body();
                    if (myResponse != null) {
                        String status = myResponse.getStatus();
                        String msg = myResponse.getMessage();
                        if (msg.equalsIgnoreCase("SUCCESS")) {
                            lstLoginData = myResponse.getResult();
                            for (SPLoginModel.Result i : lstLoginData){
                                System.out.print(i.getUType() + " ");
                                UType = i.getUType();
                                strSectionID = i.getSectionID();
                                strSPID = i.getSPID();
                                strProviderName = i.getName();

                                putStringSharedPreference(AdminLogin.this,"ProviderName",strProviderName);
                                putStringSharedPreference(AdminLogin.this,"SectionID",strSectionID);
                                putStringSharedPreference(AdminLogin.this,"SPID",strSPID);
                            }
                            if(!UType.equalsIgnoreCase("") && UType != null) {
                                putStringSharedPreference(AdminLogin.this, "UType", UType);
                                if(UType.equalsIgnoreCase("M"))
                                {
                                    if(isRememberMeChecked){
                                        putBooleanSharedPreference(AdminLogin.this, "AdminLoginForM", true);
                                        putBooleanSharedPreference(AdminLogin.this, "AdminLoginForSP", false);
                                    }
                                    Intent intent = new Intent(AdminLogin.this,ModeratorActivity.class);
                                    startActivity(intent);
                                    finish();
                                }else
                                {
                                    if(isRememberMeChecked) {
                                        putBooleanSharedPreference(AdminLogin.this, "AdminLoginForM", false);
                                        putBooleanSharedPreference(AdminLogin.this, "AdminLoginForSP", true);
                                    }
                                    Intent intent = new Intent(AdminLogin.this,ServiceProviderActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            }
                        } else {
                            hideProgressDialog(AdminLogin.this);
                            Toast.makeText(AdminLogin.this, msg, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        hideProgressDialog(AdminLogin.this);
                    }
                }
            }

            @Override
            public void onFailure(Call<SPLoginModel> call, Throwable t) {
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
                hideProgressDialog(AdminLogin.this);
            }
        });
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
                putBooleanSharedPreference(AdminLogin.this, "AdminLoginForM", false);
                putBooleanSharedPreference(AdminLogin.this, "AdminLoginForSP", false);
                putBooleanSharedPreference(AdminLogin.this, "AdminLogin", false);
                Intent intent = new Intent(AdminLogin.this,NewLoginActivity.class);
                startActivity(intent);
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
