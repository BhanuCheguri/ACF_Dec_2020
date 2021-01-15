package com.anticorruptionforce.acf.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.os.AsyncTask;
import android.os.Bundle;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.anticorruptionforce.acf.R;
import com.anticorruptionforce.acf.custom_dialogs.CustomProgressDialog;
import com.anticorruptionforce.acf.modelclasses.OTPResponse;
import com.anticorruptionforce.acf.network.APIInterface;
import com.anticorruptionforce.acf.network.APIRetrofitClient;
import com.anticorruptionforce.acf.network.ServiceCall;
import com.anticorruptionforce.acf.sms_verification.OtpReceivedInterface;
import com.anticorruptionforce.acf.sms_verification.SmsBroadcastReceiver;
import com.anticorruptionforce.acf.databinding.ActivityOtpverificationBinding;
import com.android.volley.RequestQueue;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.HintRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
//import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.JsonObject;
import com.anticorruptionforce.acf.utilities.App;
import com.pd.chocobar.ChocoBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;


public class OTPVerificationActivity extends BaseActivity implements View.OnClickListener , GoogleApiClient.ConnectionCallbacks,
        OtpReceivedInterface, GoogleApiClient.OnConnectionFailedListener{

    String TAG = "OTPVerificationActivity.class" ;
    ActivityOtpverificationBinding binding;
    GoogleApiClient mGoogleApiClient;
    SmsBroadcastReceiver mSmsBroadcastReceiver;
    private int RESOLVE_HINT = 2;
    APIRetrofitClient apiRetrofitClient;
    ServiceCall mServiceCall;
    private RequestQueue requestQueue;
    JsonObject gsonObject;
    private static final int CREDENTIAL_PICKER_REQUEST = 1;  // Set to an unused request code
    private int MY_PERMISSIONS_REQUEST_SMS_RECEIVE = 10;
    String ADD_MEMBERS =  "http://api.ainext.in/members/addmember";
    String message = "";
    String email = "";
    //CustomProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_ACTION_BAR);


        Bundle bundle = getIntent().getExtras();
        if(bundle != null)
        {
            email = bundle.getString("email");
        }

        setActionBarTitle(getString(R.string.title_OTPVerification));

        gsonObject = new JsonObject();
        mServiceCall = new ServiceCall();
        apiRetrofitClient = new APIRetrofitClient();
        binding = DataBindingUtil.setContentView(this, R.layout.activity_otpverification);
        binding.btnContinue.setOnClickListener(this);
        binding.btnSubmit.setOnClickListener(this);
        binding.changeMobileNo.setOnClickListener(this);


        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.RECEIVE_SMS},
                MY_PERMISSIONS_REQUEST_SMS_RECEIVE);

        binding.otpCode.addTextChangedListener(textWatcher);
    }

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String  otpCodeInput =  binding.otpCode.getText().toString().trim();
            binding.btnSubmit.setBackgroundResource(R.drawable.button_style);
            binding.btnSubmit.setEnabled(!otpCodeInput.isEmpty());
        }
        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    @Override public void onConnected(@Nullable Bundle bundle) {

    }

    @Override public void onConnectionSuspended(int i) {

    }

    @Override public void onOtpReceived(String otp) {
        //Toast.makeText(this, "Otp Received " + otp, Toast.LENGTH_LONG).show();
        binding.etMobileNo.setText(otp);
    }

    @Override public void onOtpTimeout() {
        Toast.makeText(this, "Time out, please resend", Toast.LENGTH_LONG).show();
    }

    @Override public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    public void getHintPhoneNumber() {
        HintRequest hintRequest =
                new HintRequest.Builder()
                        .setPhoneNumberIdentifierSupported(true)
                        .build();
        PendingIntent mIntent = Auth.CredentialsApi.getHintPickerIntent(mGoogleApiClient, hintRequest);
        try {
            startIntentSenderForResult(mIntent.getIntentSender(), RESOLVE_HINT, null, 0, 0, 0);
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[]
            permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_SMS_RECEIVE) {
            Log.d("TAG", "My permission request sms received successfully");
            /*if(progressDialog.isShowing())
                progressDialog.dismiss();*/
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Result if we want hint number
        if (requestCode == RESOLVE_HINT) {
            if (resultCode == Activity.RESULT_OK) {

                Credential credential = data.getParcelableExtra(Credential.EXTRA_KEY);
                // credential.getId();  <-- will need to process phone number string
                binding.etMobileNo.setText(credential.getId());
            }
        }
    }

    @Override
    public void onClick(View view) {

        switch (view.getId())
        {
            case R.id.btnContinue:
                GetOTPAsyncTask getOTPtask = new GetOTPAsyncTask();
                if(!binding.etMobileNo.getText().toString().equalsIgnoreCase("")) {
                    if(App.isNetworkAvailable())
                        getOTPtask.execute(binding.etMobileNo.getText().toString());
                    else{
                        ChocoBar.builder().setView(binding.mainLayout)
                                .setText("No Internet connection")
                                .setDuration(ChocoBar.LENGTH_INDEFINITE)
                                //.setActionText(android.R.string.ok)
                                .red()   // in built red ChocoBar
                                .show();
                    }
                    putStringSharedPreference(OTPVerificationActivity.this,"mobile",binding.etMobileNo.getText().toString());
                }
                else
                    Toast.makeText(this,"Mobile Number cannot be empty",Toast.LENGTH_LONG).show();

                break;
            case R.id.change_mobile_no:
                binding.llVerifyingOtp.setVisibility(View.GONE);
                binding.llVerifyMobileNo.setVisibility(View.VISIBLE);
                binding.btnSubmit.setEnabled(false);
                binding.btnSubmit.setBackgroundResource(R.drawable.inactive_button_style);

                break;
            case R.id.btnSubmit:
                if((binding.otpCode.getText().toString()).equalsIgnoreCase(""))
                {
                    Toast.makeText(this,"OTP Code cannot be empty",Toast.LENGTH_LONG).show();
                    binding.btnSubmit.setEnabled(false);
                    binding.btnSubmit.setBackgroundResource(R.drawable.inactive_button_style);
                }else
                {
                    binding.btnSubmit.setEnabled(true);
                    binding.btnSubmit.setBackgroundResource(R.drawable.button_style);
                    GetOTPVerifiedAsyncTask getOTPVerifiedAsyncTask = new GetOTPVerifiedAsyncTask();
                    String strOTPCode = getStringSharedPreference(OTPVerificationActivity.this,"OTPCode");
                    if (strOTPCode.equalsIgnoreCase("")) {
                        strOTPCode = binding.otpCode.getText().toString();
                    }
                    if(App.isNetworkAvailable())
                        getOTPVerifiedAsyncTask.execute(binding.etMobileNo.getText().toString(), strOTPCode);
                    else{
                        ChocoBar.builder().setView(binding.mainLayout)
                                .setText("No Internet connection")
                                .setDuration(ChocoBar.LENGTH_INDEFINITE)
                                //.setActionText(android.R.string.ok)
                                .red()   // in built red ChocoBar
                                .show();
                    }
                }
        }
    }

    public static boolean isValidMobile(String phone) {
        boolean check = false;
        if (!Pattern.matches("[a-zA-Z]+", phone)) {
            if (phone.length() < 9 ) {
                // if(phone.length() != 10) {
                check = false;
                // txtPhone.setError("Not Valid Number");
            } else {
                check = android.util.Patterns.PHONE.matcher(phone).matches();
            }
        } else {
            check = false;
        }
        return check;
    }




    public class GetOTPAsyncTask extends AsyncTask<String, String, String> {

        private String resp;
        CustomProgressDialog progressDialog;
        List<OTPResponse.OTPResult> myOTPResult = new ArrayList<>();

        @Override
        protected void onPreExecute() {
            progressDialog = new CustomProgressDialog(OTPVerificationActivity.this);
            progressDialog.setTitle("Please wait...Receiving SMS");
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            try{
                Retrofit retrofit = apiRetrofitClient.getRetrofit(APIInterface.BASE_URL);
                APIInterface api = retrofit.create(APIInterface.class);
                if(!params[0].equalsIgnoreCase("")) {
                    if (!isValidMobile(params[0].trim())) {
                        Toast.makeText(getApplicationContext(), "Please enter the valid Mobile Number to get OTP", Toast.LENGTH_SHORT).show();
                    } else {
                        Call<OTPResponse> call = api.getSMSOTP(params[0].toString(),email);

                        call.enqueue(new Callback<OTPResponse>() {
                            @Override
                            public void onResponse(Call<OTPResponse> call, Response<OTPResponse> response) {
                                if(response.code() == 200)
                                {
                                    OTPResponse myOTPData = response.body();
                                    if(myOTPData != null) {
                                        String status = myOTPData.getStatus();
                                        String msg = myOTPData.getMessage();
                                        if (msg.equalsIgnoreCase("SUCCESS")) {
                                            myOTPResult = myOTPData.getResult();
                                            for (int i = 0; i < myOTPResult.size(); i++) {
                                                String strOTP = myOTPResult.get(i).getOTP();
                                                //binding.otpCode.setText(strOTP);
                                            }
                                        }
                                    }
                                }else
                                    Toast.makeText(getApplicationContext(), response.body().toString(), Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFailure(Call<OTPResponse> call, Throwable t) {
                                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
                else{
                    Toast.makeText(getApplicationContext(), "Mobile Number shouldn't be empty", Toast.LENGTH_SHORT).show();
                }
            }catch (Exception e)
            {
               // FirebaseCrashlytics.getInstance().setCustomKey("OTPVerificationActivity", e.getMessage());
            }
            return resp;
        }


        @Override
        protected void onPostExecute(String result) {
            // execution of result of Long time consuming operation
            if (progressDialog.isShowing()){
                progressDialog.dismiss();
            }
            binding.llVerifyMobileNo.setVisibility(View.GONE);
            binding.llVerifyingOtp.setVisibility(View.VISIBLE);
            binding.btnSubmit.setEnabled(false);
            binding.btnSubmit.setBackgroundResource(R.drawable.inactive_button_style);
        }

    }

    private void postAddMemberDetails(/*String fullname, String mobile, String email, String gender, String photo*/) {

        String body = "{\"fullname\":\"Rama Krishna Reddy\",\n" +
                "                   \"mobile\":\"8989898899\",\n" +
                "                   \"email\":\"rama@gmail.com\",\n" +
                "                   \"gender\":\"M\",\n" +
                "                   \"Photo\":\"default.jpg\"}";
        try {
            /*gsonObject.addProperty("fullname", "Rama K Eddy");
            gsonObject.addProperty("mobile", "7678765897");
            gsonObject.addProperty("email", "ghfhf@gmail.com");
            gsonObject.addProperty("gender", "M");
            gsonObject.addProperty("photo", "rama.jpg");*/

            //new AsyncAddMember().execute(body.toString(),ADD_MEMBERS);

            //String strResponse = mServiceCall.sendPost(body,);
            //System.out.println(strResponse);

        }catch (Exception e)
        {
            e.printStackTrace();
        }


        /**/
    }

    @Override
    public void onResume() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("otp");
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //UI update here
                if (intent != null)
                    message = intent.getStringExtra("number");
                    //Toast.makeText(OTPVerificationActivity.this, "OTP is :" + message, Toast.LENGTH_SHORT).show();
                // message is the fetching OTP
                    if(message != null && !message.equalsIgnoreCase("")) {
                        binding.otpCode.setText(message);
                        binding.btnSubmit.setBackgroundResource(R.drawable.button_style);
                        binding.btnSubmit.setEnabled(true);
                    }else {
                        binding.btnSubmit.setBackgroundResource(R.drawable.inactive_button_style);
                        binding.btnSubmit.setEnabled(false);
                    }

                  putStringSharedPreference(OTPVerificationActivity.this, "OTPCode", message);
            }
        };
        /*progressDialog = new CustomProgressDialog(OTPVerificationActivity.this);
        progressDialog.setTitle("Please wait...Retrieving OTP");
        progressDialog.show();*/
        registerReceiver(receiver, filter);
        //LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter("otp"));
        super.onResume();
    }



    private String sendPost(String jsonParam, String strUrl) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        try {
            URL url = new URL(strUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            //urlConnection.setDoOutput(true);
            // is output buffer writter
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            urlConnection.setRequestProperty("Accept", "application/x-www-form-urlencoded");
            //set headers and method
            Writer writer = new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream(), "UTF-8"));
            writer.write(jsonParam);
            // json data
            writer.close();
            InputStream inputStream = urlConnection.getInputStream();
            //input stream
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String inputLine;
            while ((inputLine = reader.readLine()) != null)
                buffer.append(inputLine + "\n");
            if (buffer.length() == 0) {
                // Stream was empty. No point in parsing.
                return null;
            }
            String JsonResponse = buffer.toString();
                //response data
            Log.i(TAG,JsonResponse);
            return JsonResponse;
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(TAG, "Error closing stream", e);
                }
            }
        }
        return null;
    }

    @Override
    public void onPause() {
        super.onPause();
        IntentFilter filter = new IntentFilter();
        filter.addAction("otp");
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //UI update here
                if (intent != null)
                    message = intent.getStringExtra("number");
                //Toast.makeText(OTPVerificationActivity.this, "OTP is :" + message, Toast.LENGTH_SHORT).show();
                // message is the fetching OTP
                if(message != null && !message.equalsIgnoreCase(""))
                    binding.otpCode.setText(message);

                putStringSharedPreference(OTPVerificationActivity.this, "OTPCode", message);
            }
        };
        registerReceiver(receiver, filter);
        //LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equalsIgnoreCase("otp")) {
                message = intent.getStringExtra("number");
                //Toast.makeText(OTPVerificationActivity.this, "OTP is :" + message, Toast.LENGTH_SHORT).show();
                // message is the fetching OTP
                if(message != null && !message.equalsIgnoreCase(""))
                    binding.otpCode.setText(message);

                /*if(progressDialog.isShowing())
                    progressDialog.dismiss();*/

                putStringSharedPreference(OTPVerificationActivity.this, "OTPCode", message);

            }
        }
    };

    public class GetOTPVerifiedAsyncTask extends AsyncTask<String, String, String> {

        private String resp;
        CustomProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = new CustomProgressDialog(OTPVerificationActivity.this);
            progressDialog.setTitle("Please wait...Verifying your OTP");
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            try{
                Retrofit retrofit = apiRetrofitClient.getRetrofit(APIInterface.BASE_URL);
                APIInterface api = retrofit.create(APIInterface.class);
                Call<ResponseBody> call = api.getValidateOTPStatus(params[0].toString(),params[1].toString());

                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        ResponseBody myOTPStatus = response.body();
                        try {
                            String  result = new String(response.body().bytes());
                            Log.v("bodyString ::: ",result);
                            int nStatus = -2;
                            if (response.isSuccessful()) {
                                if(!result.equalsIgnoreCase("") && result != null) {
                                    JSONObject jobject = new JSONObject(result);
                                    if (result.length() > 0) {
                                        if (jobject.has("message")) {
                                            if(jobject.getString("message").equalsIgnoreCase("SUCCESS")) {
                                                if (jobject.has("result")) {
                                                    JSONArray jsonArray = new JSONArray(jobject.getString("result"));
                                                    for (int i=0; i<jsonArray.length();i++)
                                                    {
                                                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                                                        nStatus = jsonObject.getInt("Status");
                                                        if(nStatus == 1) {
                                                            putBooleanSharedPreference(OTPVerificationActivity.this, "FirstTime", true);
                                                            putBooleanSharedPreference(OTPVerificationActivity.this, "LoggedIn", true);
                                                            Intent intent = new Intent(OTPVerificationActivity.this, MainActivity.class);
                                                            startActivity(intent);
                                                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                                            finish();
                                                        }else if(nStatus == 0){
                                                            putBooleanSharedPreference(OTPVerificationActivity.this, "FirstTime", false);
                                                            putBooleanSharedPreference(OTPVerificationActivity.this, "LoggedIn", false);
                                                            showAlert(OTPVerificationActivity.this,"Failed","Something wrong from our end","OK");
                                                        }
                                                        else if(nStatus == -1){
                                                            putBooleanSharedPreference(OTPVerificationActivity.this, "FirstTime", false);
                                                            putBooleanSharedPreference(OTPVerificationActivity.this, "LoggedIn", false);
                                                            showAlert(OTPVerificationActivity.this,"Error","Error in Validating OTP","OK");
                                                        }
                                                    }
                                                }
                                            }else if (jobject.getString("message").equalsIgnoreCase("ERROR")) {
                                                showAlert(OTPVerificationActivity.this, "Error", jobject.getString("result"), "OK");
                                            } else if (jobject.getString("message").equalsIgnoreCase("FAILURE")) {
                                                showAlert(OTPVerificationActivity.this, "Failed",jobject.getString("result"), "OK");
                                            }
                                        }
                                    }
                                }
                            }
                        }catch (IOException ex) {
                            ex.printStackTrace();
                        }catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    public void UpdateMobileNo(String MobileNO, String MemberID){

                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }catch (Exception e)
            {
                e.printStackTrace();
            }
            return resp;
        }


        @Override
        protected void onPostExecute(String result) {
            // execution of result of Long time consuming operation
            if (progressDialog.isShowing()){
                progressDialog.dismiss();
            }
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK)
        {
            putBooleanSharedPreference(OTPVerificationActivity.this, "FirstTime", false);
            putBooleanSharedPreference(OTPVerificationActivity.this, "LoggedIn", false);
            Intent intent = new Intent(OTPVerificationActivity.this,NewLoginActivity.class);
            startActivity(intent);
            finish();
        }
        return super.onKeyDown(keyCode, event);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("onDestroy","onDestroy");
    }
}
