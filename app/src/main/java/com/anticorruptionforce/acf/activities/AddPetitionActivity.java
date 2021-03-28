package com.anticorruptionforce.acf.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.anticorruptionforce.acf.utilities.Utils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;

import com.anticorruptionforce.acf.R;
import com.anticorruptionforce.acf.modelclasses.OfficesModel;
import com.anticorruptionforce.acf.modelclasses.SectionsModel;
import com.anticorruptionforce.acf.network.APIInterface;
import com.anticorruptionforce.acf.network.APIRetrofitClient;
import com.anticorruptionforce.acf.network.AppLocationService;
import com.anticorruptionforce.acf.network.ServiceCall;
import com.anticorruptionforce.acf.databinding.ActivityAddPetitionBinding;
import com.anticorruptionforce.acf.utilities.App;
//import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.android.gms.tasks.Task;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.pd.chocobar.ChocoBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class AddPetitionActivity extends BaseActivity implements View.OnClickListener {

    private static Uri contentUri;
    ActivityAddPetitionBinding binding;
    private int CAMERA = 1;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final String complaint_ImagePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/ACF/Petitions";
    Bitmap photo = null;
    private FusedLocationProviderClient locationProviderClient;
    private Geocoder geocoder;
    private double latitude, longitude;
    private List<Address> addresses;
    private int locationRequestCode = 1000;
    String lat, lang = "";
    String currentAddress = "";
    String strTitle/*,strComments*/ = "";
    AppLocationService appLocationService;
    APIRetrofitClient apiRetrofitClient;
    String strResponse = "";
    ServiceCall mServiceCall;

    OfficesModel myOfficesResponse;
    ArrayList<OfficesModel.Result> lstOfficesData;
    ArrayList<String> lstOfficesNames;
    HashMap<String, String> hshMapOffices;

    SectionsModel mySectionResponse;
    ArrayList<SectionsModel.Result> lstSectionData;
    ArrayList<String> lstSectionNames;
    HashMap<String, String> hshMapSection;
    String strSectionID = "-1";
    String strSPID = "-1";
    private String TAG = "AddPetitionActivity.class";
    private ArrayList<String> lstPathURI;
    String strOTP;
    String strPID;
    int PERMISSION_ID = 44;
    FusedLocationProviderClient mFusedLocationClient;

    Boolean bStatusVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_add_petition);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_petition);
        init();
    }

    private void init() {

        lstOfficesNames = new ArrayList<>();
        hshMapOffices = new HashMap<>();
        lstPathURI = new ArrayList<>();

        hshMapSection = new HashMap<>();

        mServiceCall = new ServiceCall();
        apiRetrofitClient = new APIRetrofitClient();
        appLocationService = new AppLocationService(AddPetitionActivity.this);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        binding.imageView1.setOnClickListener(this);
        binding.imageView2.setOnClickListener(this);
        binding.imageView3.setOnClickListener(this);
        binding.imageView4.setOnClickListener(this);
        binding.imageView5.setOnClickListener(this);
        //binding.imageView6.setOnClickListener(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.mipmap.ic_launcher_icon);
        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.gradient_theme));

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        getSupportActionBar().setTitle("Add Petition");

        requestMultiplePermissions();
        //getCurrentLocation();
        getLastLocation();
        binding.spOffice.setEnabled(false);
        binding.spSelection.setEnabled(false);

        binding.btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                strTitle = binding.etTitle.getText().toString();
                //strComments = binding.etCimment.getText().toString();
                int petitionID = -1;
                if (!bStatusVisible) {
                    if (!strTitle.equalsIgnoreCase("") && /*!strComments.equalsIgnoreCase("") &&*/ !strSectionID.equalsIgnoreCase("") && !strSPID.equalsIgnoreCase("")) {
                        callSubmit(petitionID);
                    } else
                        Toast.makeText(AddPetitionActivity.this, "Please fill all the fileds", Toast.LENGTH_SHORT).show();
                }else {
                    if (!strTitle.equalsIgnoreCase("") && /*!strComments.equalsIgnoreCase("") &&*/ !strSectionID.equalsIgnoreCase("-1") && !strSPID.equalsIgnoreCase("-1")) {
                        callSubmit(petitionID);
                    }
                } /*else
                    Toast.makeText(AddPetitionActivity.this, "Please fill all the fileds", Toast.LENGTH_SHORT).show();*/
            }
        });
    }

    private void callSubmit(int petitionID){
        String MemberID = getStringSharedPreference(AddPetitionActivity.this, "MemberID");
        if (!MemberID.equalsIgnoreCase("")) {
            int createBy = Integer.valueOf(MemberID);
            JSONObject jsonParam = new JSONObject();
            try {
                jsonParam.put("petitionID", petitionID);
                jsonParam.put("spid", strSPID);
                jsonParam.put("sectionID", strSectionID);
                jsonParam.put("title", binding.etTitle.getText().toString());
                jsonParam.put("image1", "");
                jsonParam.put("image2", "");
                jsonParam.put("image3", "");
                jsonParam.put("image4", "");
                jsonParam.put("image5", "");
                jsonParam.put("image6", "");
                jsonParam.put("createBy", createBy);
                jsonParam.put("location", currentAddress);
                jsonParam.put("latitude", lat);
                jsonParam.put("langitude", lang);
                jsonParam.put("remarks", "");
                Log.i("JSON", jsonParam.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (App.isNetworkAvailable())
                new AsyncSubmitPetition().execute(jsonParam.toString()/*,ADD_PETITION*/);
            else {
                ChocoBar.builder().setView(binding.mainLayout)
                        .setText("No Internet connection")
                        .setDuration(ChocoBar.LENGTH_INDEFINITE)
                        //.setActionText(android.R.string.ok)
                        .red()   // in built red ChocoBar
                        .show();
            }
        } else {
            Toast.makeText(AddPetitionActivity.this, "Member ID is Empty.Please logout and Re-Login to get your Member ID", Toast.LENGTH_SHORT).show();
        }
    }
    private void getOfficesbyGeoLocation(double lat, double lang) {
        showProgressDialog(AddPetitionActivity.this);
        apiRetrofitClient = new APIRetrofitClient();
        Retrofit retrofit = apiRetrofitClient.getRetrofit(APIInterface.BASE_URL);
        APIInterface api = retrofit.create(APIInterface.class);
        Call<OfficesModel> call = api.getOfficesbyGeo(String.valueOf(lat), String.valueOf(lang));

        call.enqueue(new Callback<OfficesModel>() {
            @Override
            public void onResponse(Call<OfficesModel> call, Response<OfficesModel> response) {
                System.out.println("getOfficesbyGeo::"+ response);
                hideProgressDialog(AddPetitionActivity.this);
                if (response != null) {
                    myOfficesResponse = response.body();
                    if (myOfficesResponse != null) {

                        binding.spOffice.setVisibility(View.VISIBLE);
                        binding.spSelection.setVisibility(View.VISIBLE);
                        bStatusVisible = true;
                        String status = myOfficesResponse.getStatus();
                        String msg = myOfficesResponse.getMessage();
                        if (msg.equalsIgnoreCase("SUCCESS")) {
                            lstOfficesData = myOfficesResponse.getResult();
                            loadOfficeAdapter(lstOfficesData);
                        } else {
                            bStatusVisible = false;
                            strSPID = "-1";
                            strSectionID = "-1";
                            binding.spOffice.setVisibility(View.GONE);
                            binding.spSelection.setVisibility(View.GONE);
                            //hideProgressDialog(AddPetitionActivity.this);
                        }
                    } else {
                        hideProgressDialog(AddPetitionActivity.this);
                    }
                }
            }

            @Override
            public void onFailure(Call<OfficesModel> call, Throwable t) {
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
                hideProgressDialog(AddPetitionActivity.this);
            }
        });
    }

    private void loadOfficeAdapter(ArrayList<OfficesModel.Result> lstOfficesData) {
        binding.spOffice.setEnabled(true);
        for (int i = 0; i < lstOfficesData.size(); i++) {
            lstOfficesNames.add(lstOfficesData.get(i).getName().toString().trim());
            hshMapOffices.put(lstOfficesData.get(i).getName().toString().trim(), lstOfficesData.get(i).getSPID().toString().trim());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(AddPetitionActivity.this,
                android.R.layout.simple_spinner_dropdown_item, lstOfficesNames);
        binding.spOffice.setAdapter(adapter);
        binding.spOffice.setThreshold(1);
        binding.spOffice.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                binding.spOffice.showDropDown();
                return false;
            }
        });
        binding.spOffice.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String strOfficeName = binding.spOffice.getAdapter().getItem(position).toString();
                strSPID = hshMapOffices.get(strOfficeName);
                if (App.isNetworkAvailable())
                    getSectionsbySPID(strSPID);
                else {
                    ChocoBar.builder().setView(binding.mainLayout)
                            .setText("No Internet connection")
                            .setDuration(ChocoBar.LENGTH_INDEFINITE)
                            //.setActionText(android.R.string.ok)
                            .red()   // in built red ChocoBar
                            .show();
                }
            }
        });

    }

    private void getSectionsbySPID(String strSPID) {
        showProgressDialog(AddPetitionActivity.this);
        apiRetrofitClient = new APIRetrofitClient();
        Retrofit retrofit = apiRetrofitClient.getRetrofit(APIInterface.BASE_URL);
        APIInterface api = retrofit.create(APIInterface.class);
        Call<SectionsModel> call = api.getSections(strSPID);

        call.enqueue(new Callback<SectionsModel>() {
            @Override
            public void onResponse(Call<SectionsModel> call, Response<SectionsModel> response) {
                System.out.println("getSections::"+ response);
                hideProgressDialog(AddPetitionActivity.this);
                if (response != null) {
                    mySectionResponse = response.body();
                    if (mySectionResponse != null) {
                        /*lstSectionData = new ArrayList<SectionsModel>();
                        for (Object object : mySectionResponse) {
                            lstSectionData.add((SectionsModel) object);
                        }*/
                        String status = mySectionResponse.getStatus();
                        String msg = mySectionResponse.getMessage();
                        if (msg.equalsIgnoreCase("SUCCESS")) {
                            lstSectionData = mySectionResponse.getResult();
                            loadSectionAdapter(lstSectionData);
                        } else {
                            hideProgressDialog(AddPetitionActivity.this);
                        }
                    } else {
                        hideProgressDialog(AddPetitionActivity.this);
                    }
                } else {
                    hideProgressDialog(AddPetitionActivity.this);
                }
            }

            @Override
            public void onFailure(Call<SectionsModel> call, Throwable t) {
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
                hideProgressDialog(AddPetitionActivity.this);
            }
        });
    }

    private void loadSectionAdapter(ArrayList<SectionsModel.Result> lstSectionData) {
        hideProgressDialog(AddPetitionActivity.this);
        binding.spSelection.setEnabled(true);

        lstSectionNames = new ArrayList<>();

        for (int i = 0; i < lstSectionData.size(); i++) {
            lstSectionNames.add(lstSectionData.get(i).getName().toString().trim());
            hshMapSection.put(lstSectionData.get(i).getName().toString().trim(), lstSectionData.get(i).getSectionID().toString().trim());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(AddPetitionActivity.this,
                android.R.layout.simple_spinner_dropdown_item, lstSectionNames);
        binding.spSelection.setAdapter(adapter);

        binding.spSelection.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String strSectionId = binding.spSelection.getAdapter().getItem(position).toString();
                strSectionID = hshMapSection.get(strSectionId);
            }
        });
        binding.spSelection.setThreshold(1);
        binding.spSelection.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                binding.spSelection.showDropDown();
                return false;
            }
        });
    }

    private class AsyncSubmitPetition extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showCustomProgressDialog(AddPetitionActivity.this, "Processing your data...", R.mipmap.ic_dataprocessing);
        }

        @Override
        protected String doInBackground(String... params) {
            String strResponse = "";
            if (App.isNetworkAvailable())
                strResponse = uploadPetition(params[0]/*,params[1]*/);
            else {
                ChocoBar.builder().setView(binding.mainLayout)
                        .setText("No Internet connection")
                        .setDuration(ChocoBar.LENGTH_INDEFINITE)
                        //.setActionText(android.R.string.ok)
                        .red()   // in built red ChocoBar
                        .show();
            }
            System.out.println("Response ::: " + strResponse);
            // Toast.makeText(AddPetitionActivity.this, strResponse, Toast.LENGTH_SHORT).show();
            return strResponse;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            hideCustomProgressDialog(AddPetitionActivity.this);
            if (!result.equalsIgnoreCase("") && result != null) {
                try {
                    JSONObject jobject = new JSONObject(result);
                    if (result.length() > 0) {
                        if (jobject.has("message")) {
                            if (jobject.getString("message").equalsIgnoreCase("SUCCESS")) {
                                if (jobject.has("result")) {
                                    JSONArray jsonArray = new JSONArray(jobject.getString("result"));
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                                        if (jsonObject.has("PID")) {
                                            int nPID = jsonObject.getInt("PID");
                                            strPID = String.valueOf(nPID);
                                        }
                                        if (jsonObject.has("OTP")) {
                                            int nOTP = jsonObject.getInt("OTP");
                                            strOTP = String.valueOf(nOTP);
                                        }
                                    }

                                    if (strPID != null) {
                                        if (App.isNetworkAvailable())
                                            new AsyncUploadImages().execute(strPID);
                                        else {
                                            ChocoBar.builder().setView(binding.mainLayout)
                                                    .setText("No Internet connection")
                                                    .setDuration(ChocoBar.LENGTH_INDEFINITE)
                                                    //.setActionText(android.R.string.ok)
                                                    .red()   // in built red ChocoBar
                                                    .show();
                                        }
                                    } else {
                                        Toast.makeText(AddPetitionActivity.this, "Invalid Petition ID", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        }
                    }

                } catch (Exception e) {
                    //FirebaseCrashlytics.getInstance().setCustomKey("AddPetitionActivity", e.getMessage());
                    e.printStackTrace();
                    Toast.makeText(AddPetitionActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                }
            }
            hideProgressDialog(AddPetitionActivity.this);
        }
    }

    private String uploadPetition(String jsonParam) {
        String reponse = "";
        try {
            URL url = new URL("http://api.ainext.in/petitions/addpetition");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            DataOutputStream os = new DataOutputStream(conn.getOutputStream());
            os.writeBytes(jsonParam.toString());
            os.flush();
            os.close();

            InputStream response = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(response));
            StringBuilder sb = new StringBuilder();
            String line = null;
            try {
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            reponse = sb.toString();
            Log.i("STATUS", String.valueOf(conn.getResponseCode()));
            Log.i("MSG", conn.getResponseMessage());
            conn.disconnect();
        } catch (Exception e) {
            //Crashlytics.logException(e);
            //FirebaseCrashlytics.getInstance().setCustomKey("AddPetitionActivity", e.getMessage());
            e.printStackTrace();
        }
        return reponse;
    }

    /*@Override
    protected void onResume() {
        super.onResume();
        getLastLocation();
    }*/

    private void getLastLocation() {
        // check if permissions are given
        if (checkPermissions()) {

            // check if location is enabled
            if (isLocationEnabled()) {

                // getting last location from FusedLocationClient object
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        Location location = task.getResult();
                        if (location == null) {
                            requestNewLocationData();
                        } else {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();

                            lat = String.valueOf(latitude);
                            lang = String.valueOf(longitude);

                            getAddressFromLocation(latitude,longitude);
                        }
                    }
                });
            } else {
                Toast.makeText(this, "Please turn on" + " your location...", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        } else {
            // if permissions aren't available,
            // request for permissions
            requestPermissions();
        }
    }

    @SuppressLint("MissingPermission")
    private void requestNewLocationData() {

        // Initializing LocationRequest
        // object with appropriate methods
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(5);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        // setting LocationRequest
        // on FusedLocationClient
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
    }

    private LocationCallback mLocationCallback = new LocationCallback() {

        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();

            System.out.println("Location::getLatitude" + mLastLocation.getLatitude() + " :::::::" + "Location::getLongitude" + mLastLocation.getLatitude() );
            if (mLastLocation != null) {
                latitude = mLastLocation.getLatitude();
                longitude = mLastLocation.getLongitude();
                getAddressFromLocation(latitude,longitude);
            }
        }
    };

    private void getAddressFromLocation(Double lat , Double lang) {
        try {
            geocoder = new Geocoder(this, Locale.getDefault());
            addresses = geocoder.getFromLocation(lat, lang, 1);
            if (addresses.size() > 0) {
                String addressLine1 = addresses.get(0).getAddressLine(0);
                Log.e("line1", addressLine1);
                String city = addresses.get(0).getLocality();
                Log.e("city", city);
                String state = addresses.get(0).getAdminArea();
                Log.e("state", state);
                String pinCode = addresses.get(0).getPostalCode();
                Log.e("pinCode", pinCode);

                String fullAddress = addressLine1 + ",  " + city + ",  " + state + ",  " + pinCode;
                currentAddress = addressLine1;
                getOfficesbyGeoLocation(latitude,longitude);
            }

        } catch (IOException e) {
            e.printStackTrace();
            Log.e("MainActivity", e.getMessage());
        }
    }

    // method to check for permissions
    private boolean checkPermissions() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        // If we want background location
        // on Android 10.0 and higher,
        // use:
        // ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    // method to request for permissions
    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ID);
    }

    // method to check
    // if location is enabled
    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    // If everything is alright then
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            }
        }
    }



    private void  requestMultiplePermissions(){
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        // check if all permissions are granted
                        if (report.areAllPermissionsGranted()) {
                        }

                        if (report.isAnyPermissionPermanentlyDenied()) {

                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).
                withErrorListener(new PermissionRequestErrorListener() {
                    @Override
                    public void onError(DexterError error) {
                        Toast.makeText(getApplicationContext(), "Some Error! ", Toast.LENGTH_SHORT).show();
                    }
                })
                .onSameThread()
                .check();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String imgcurTime = dateFormat.format(new Date());
        String dir = getFilesDir().getAbsolutePath();
        Bundle extras = data.getExtras();
        photo = (Bitmap) extras.get("data");
        //Bitmap resizedBitmap = Utils.getResizedBitmap(photo, 150, 300);

        switch(requestCode){
            case 1:
                if(resultCode == RESULT_OK){
                    binding.imageView1.setImageBitmap(photo);
                    binding.imageView1.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    saveFile(AddPetitionActivity.this,photo,imgcurTime + ".jpg");
                    String imagePath = dir + "/"+imgcurTime + ".jpg";
                    lstPathURI.add(imagePath);
                }
                break;
            case 2:
                if(resultCode == RESULT_OK) {
                    binding.imageView2.setImageBitmap(photo);
                    binding.imageView2.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    saveFile(AddPetitionActivity.this,photo,imgcurTime + ".jpg");
                    String imagePath = dir + "/"+imgcurTime + ".jpg";
                    lstPathURI.add(imagePath);
                }
                break;

            case 3:
                if(resultCode == RESULT_OK){
                    binding.imageView3.setImageBitmap(photo);
                    binding.imageView3.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    saveFile(AddPetitionActivity.this,photo,imgcurTime + ".jpg");
                    String imagePath = dir + "/"+imgcurTime + ".jpg";
                    lstPathURI.add(imagePath);
                }
                break;
            case 4:
                if(resultCode == RESULT_OK) {
                    binding.imageView4.setImageBitmap(photo);
                    binding.imageView4.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    saveFile(AddPetitionActivity.this,photo,imgcurTime + ".jpg");
                    String imagePath = dir + "/"+imgcurTime + ".jpg";
                    lstPathURI.add(imagePath);
                }
                break;
            case 5:
                if(resultCode == RESULT_OK){
                    binding.imageView5.setImageBitmap(photo);
                    binding.imageView5.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    saveFile(AddPetitionActivity.this,photo,imgcurTime + ".jpg");
                    String imagePath = dir + "/"+imgcurTime + ".jpg";
                    lstPathURI.add(imagePath);
                }
                break;
            /*case 6:
                if(resultCode == RESULT_OK) {
                    Bundle extras = data.getExtras();
                    photo = (Bitmap) extras.get("data");
                    binding.imageView6.setImageBitmap(photo);
                    saveFile(AddPetitionActivity.this,photo,imgcurTime + ".jpg");
                    String imagePath = dir + "/"+imgcurTime + ".jpg";
                    lstPathURI.add(imagePath);
                }
                break;*/
        }
    }


    @Override
    public void onClick(View v) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        switch (v.getId())
        {
            case R.id.imageView1:
                startActivityForResult(intent, 1);
                break;
            case R.id.imageView2:
                startActivityForResult(intent, 2);
                break;
            case R.id.imageView3:
                startActivityForResult(intent, 3);
                break;
            case R.id.imageView4:
                startActivityForResult(intent, 4);
                break;
            case R.id.imageView5:
                startActivityForResult(intent, 5);
                break;
            /*case R.id.imageView6:
                startActivityForResult(intent, 6);
                break;*/
        }
    }


    public void saveFile(Context context, Bitmap b, String picName){
        FileOutputStream fos = null;
        try {
            fos = context.openFileOutput(picName, Context.MODE_PRIVATE);
            b.compress(Bitmap.CompressFormat.PNG, 100, fos);
        }
        catch (FileNotFoundException e) {
            Log.d(TAG, "file not found");
            e.printStackTrace();
        }
        finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public class AsyncUploadImages extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showCustomProgressDialog(AddPetitionActivity.this,"Classifying your data...",R.mipmap.ic_classifydata);
        }
        @Override
        protected String doInBackground(String... strings) {
            System.out.println("lstPathURI ::" + lstPathURI);
            if(!lstPathURI.isEmpty())
            {
                uploadMultiFile(strings[0],lstPathURI);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            hideCustomProgressDialog(AddPetitionActivity.this);
        }
    }
    private void uploadMultiFile(String pid, ArrayList<String> filePaths) {
        Retrofit retrofit = apiRetrofitClient.getRetrofit(APIInterface.BASE_URL);
        APIInterface api = retrofit.create(APIInterface.class);

        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);
        File file= null;
        // Multiple Images
        for (int i = 0; i < filePaths.size(); i++) {
            if(!filePaths.get(i).equalsIgnoreCase("") && filePaths.get(i) != null) {
                file = new File(filePaths.get(i));
            }
            System.out.println("image"+(i+1));
            MultipartBody.Part fileToUpload = MultipartBody.Part.createFormData("image"+(i+1), file.getName(),
                    RequestBody.create(MediaType.parse("application/octet-stream"), file));
            builder.addPart(fileToUpload);
            /*MultipartBody.Part fileToUpload = MultipartBody.Part.createFormData("file", file.getName(),
                    RequestBody.create(MediaType.parse("application/octet-stream"), file));
            builder.addPart(fileToUpload);*/
        }

        MultipartBody requestBody = builder.build();

        Call<ResponseBody> call = api.uploadPetitionMultiFile(pid,requestBody);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                String strResponse = response.toString();
                System.out.println("uploadMultiFile :: "+strResponse);
                System.out.println( "AddPetitionActivity.this ::: uploadMultiFile :::" +response.toString());
                System.out.println( "uploadPetitionMultiFile::" +response.toString());
                if(strResponse != null && !strResponse.equalsIgnoreCase("")) {
                        try {
                            if(isJSONValid(strResponse)) {
                                JSONObject jobject = new JSONObject(strResponse);
                                if (strResponse.length() > 0) {
                                    if (jobject.has("message")) {
                                        if (jobject.getString("message").equalsIgnoreCase("SUCCESS")) {
                                            CustomDialog(AddPetitionActivity.this, "Thank You", "Your request has been successfully posted. We will process and keep in touch with you.", "");
                                            binding.etTitle.setText("");
                                            binding.spOffice.setText("");
                                            binding.spSelection.setText("");
                                            binding.imageView1.setImageResource(R.drawable.ic_add_item);
                                            binding.imageView1.setScaleType(ImageView.ScaleType.CENTER);
                                            binding.imageView2.setImageResource(R.drawable.ic_add_item);
                                            binding.imageView2.setScaleType(ImageView.ScaleType.CENTER);
                                            binding.imageView3.setImageResource(R.drawable.ic_add_item);
                                            binding.imageView3.setScaleType(ImageView.ScaleType.CENTER);
                                            binding.imageView4.setImageResource(R.drawable.ic_add_item);
                                            binding.imageView4.setScaleType(ImageView.ScaleType.CENTER);
                                            binding.imageView5.setImageResource(R.drawable.ic_add_item);
                                            binding.imageView5.setScaleType(ImageView.ScaleType.CENTER);

                                        }else
                                            showAlert(AddPetitionActivity.this, "Failed to upload Images", "Result:" + response.toString(), "OK");
                                    }
                                }
                            }else
                            {
                                if(response.code() == 200){
                                    CustomDialog(AddPetitionActivity.this, "Thank You", "Your request has been successfully posted. We will process and keep in touch with you.", "");
                                    binding.etTitle.setText("");
                                    binding.spOffice.setText("");
                                    binding.spSelection.setText("");
                                    binding.imageView1.setImageResource(R.drawable.ic_add_item);
                                    binding.imageView2.setImageResource(R.drawable.ic_add_item);
                                    binding.imageView3.setImageResource(R.drawable.ic_add_item);
                                    binding.imageView3.setImageResource(R.drawable.ic_add_item);
                                    binding.imageView4.setImageResource(R.drawable.ic_add_item);
                                    binding.imageView5.setImageResource(R.drawable.ic_add_item);
                                }else
                                    showAlert(AddPetitionActivity.this, "Failed to upload Images", "Result:" + response.toString(), "OK");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d(TAG, "Error " + t.getMessage());
                showAlert(AddPetitionActivity.this, "Failed to upload Images", "Result:" + t.getMessage(), "OK");
            }
        });
    }

    public boolean isJSONValid(String test) {
        try {
            new JSONObject(test);
        } catch (JSONException ex) {
            try {
                new JSONArray(test);
            } catch (JSONException ex1) {
                return false;
            }
        }

        return true;

    }
    private static boolean fileExists(String filePath) {
        File file = new File(filePath);

        return file.exists();
    }


    /**
     * Get full file path from external storage
     *
     * @param pathData The storage type and the relative path
     */
    private static String getPathFromExtSD(String[] pathData) {
        final String type = pathData[0];
        final String relativePath = "/" + pathData[1];
        String fullPath = "";

        // on my Sony devices (4.4.4 & 5.1.1), `type` is a dynamic string
        // something like "71F8-2C0A", some kind of unique id per storage
        // don't know any API that can get the root path of that storage based on its id.
        //
        // so no "primary" type, but let the check here for other devices
        if ("primary".equalsIgnoreCase(type)) {
            fullPath = Environment.getExternalStorageDirectory() + relativePath;
            if (fileExists(fullPath)) {
                return fullPath;
            }
        }

        // Environment.isExternalStorageRemovable() is `true` for external and internal storage
        // so we cannot relay on it.
        //
        // instead, for each possible path, check if file exists
        // we'll start with secondary storage as this could be our (physically) removable sd card
        fullPath = System.getenv("SECONDARY_STORAGE") + relativePath;
        if (fileExists(fullPath)) {
            return fullPath;
        }

        fullPath = System.getenv("EXTERNAL_STORAGE") + relativePath;
        if (fileExists(fullPath)) {
            return fullPath;
        }

        return fullPath;
    }

    private static String getDriveFilePath(Uri uri, Context context) {
        Uri returnUri = uri;
        Cursor returnCursor = context.getContentResolver().query(returnUri, null, null, null, null);
        /*
         * Get the column indexes of the data in the Cursor,
         *     * move to the first row in the Cursor, get the data,
         *     * and display it.
         * */
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
        returnCursor.moveToFirst();
        String name = (returnCursor.getString(nameIndex));
        String size = (Long.toString(returnCursor.getLong(sizeIndex)));
        File file = new File(context.getCacheDir(), name);
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            FileOutputStream outputStream = new FileOutputStream(file);
            int read = 0;
            int maxBufferSize = 1 * 1024 * 1024;
            int bytesAvailable = inputStream.available();

            //int bufferSize = 1024;
            int bufferSize = Math.min(bytesAvailable, maxBufferSize);

            final byte[] buffers = new byte[bufferSize];
            while ((read = inputStream.read(buffers)) != -1) {
                outputStream.write(buffers, 0, read);
            }
            Log.e("File Size", "Size " + file.length());
            inputStream.close();
            outputStream.close();
            Log.e("File Path", "Path " + file.getPath());
            Log.e("File Size", "Size " + file.length());
        } catch (Exception e) {
            Log.e("Exception", e.getMessage());
        }
        return file.getPath();
    }

    private static String getMediaFilePathForN(Uri uri, Context context) {
        Uri returnUri = uri;
        Cursor returnCursor = context.getContentResolver().query(returnUri, null, null, null, null);
        /*
         * Get the column indexes of the data in the Cursor,
         *     * move to the first row in the Cursor, get the data,
         *     * and display it.
         * */
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
        returnCursor.moveToFirst();
        String name = (returnCursor.getString(nameIndex));
        String size = (Long.toString(returnCursor.getLong(sizeIndex)));
        File file = new File(context.getFilesDir(), name);
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            FileOutputStream outputStream = new FileOutputStream(file);
            int read = 0;
            int maxBufferSize = 1 * 1024 * 1024;
            int bytesAvailable = inputStream.available();

            //int bufferSize = 1024;
            int bufferSize = Math.min(bytesAvailable, maxBufferSize);

            final byte[] buffers = new byte[bufferSize];
            while ((read = inputStream.read(buffers)) != -1) {
                outputStream.write(buffers, 0, read);
            }
            Log.e("File Size", "Size " + file.length());
            inputStream.close();
            outputStream.close();
            Log.e("File Path", "Path " + file.getPath());
            Log.e("File Size", "Size " + file.length());
        } catch (Exception e) {
            Log.e("Exception", e.getMessage());
        }
        return file.getPath();
    }


    private static String getDataColumn(Context context, Uri uri,
                                        String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection,
                    selection, selectionArgs, null);

            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }

        return null;
    }

    /**
     * @param uri - The Uri to check.
     * @return - Whether the Uri authority is ExternalStorageProvider.
     */
    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri - The Uri to check.
     * @return - Whether the Uri authority is DownloadsProvider.
     */
    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri - The Uri to check.
     * @return - Whether the Uri authority is MediaProvider.
     */
    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri - The Uri to check.
     * @return - Whether the Uri authority is Google Photos.
     */
    private static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Drive.
     */
    private static boolean isGoogleDriveUri(Uri uri) {
        return "com.google.android.apps.docs.storage".equals(uri.getAuthority()) || "com.google.android.apps.docs.storage.legacy".equals(uri.getAuthority());
    }


    public void CustomDialog(Context context,String title,String msg,String subMsg)
    {
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.custom_dialog);
        //dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        // set the custom dialog components - text, image and button
        TextView text = (TextView) dialog.findViewById(R.id.title);
        text.setText(title);

        TextView message = (TextView) dialog.findViewById(R.id.msg);
        message.setText(msg);

        if(!msg.equalsIgnoreCase("")) {
            message.setVisibility(View.VISIBLE);
            message.setText(msg);
        }else
            message.setVisibility(View.GONE);

        TextView sub_Msg = (TextView) dialog.findViewById(R.id.sub_msg);

        if(!subMsg.equalsIgnoreCase("")) {
            sub_Msg.setVisibility(View.VISIBLE);
            sub_Msg.setText(subMsg);
        }else
            sub_Msg.setVisibility(View.GONE);

        Button dialogButton = (Button) dialog.findViewById(R.id.btnOk);
        // if button is clicked, close the custom dialog
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                finish();
                
            }
        });

        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.tag_petition, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId())
        {
            case android.R.id.home:
                break;
            case R.id.help:
                Toast.makeText(AddPetitionActivity.this, "Help", Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
