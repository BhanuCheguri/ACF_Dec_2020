package com.joinacf.acf.activities;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import com.joinacf.acf.R;
import com.joinacf.acf.modelclasses.OfficesModel;
import com.joinacf.acf.modelclasses.SectionsModel;
import com.joinacf.acf.network.APIInterface;
import com.joinacf.acf.network.APIRetrofitClient;
import com.joinacf.acf.network.AppLocationService;
import com.joinacf.acf.network.ServiceCall;
import com.joinacf.acf.databinding.ActivityAddPetitionBinding;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class AddPetitionActivity extends BaseActivity implements View.OnClickListener {

    ActivityAddPetitionBinding binding;
    private int CAMERA = 1;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final String complaint_ImagePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/ACF/Petitions";
    Bitmap photo = null;
    private FusedLocationProviderClient locationProviderClient;
    private Geocoder geocoder;
    private double latitude,longitude;
    private List<Address> addresses;
    private int locationRequestCode = 1000;
    String lat,lang = "";
    String currentAddress="";
    String strTitle,strComments ="";
    AppLocationService appLocationService;
    APIRetrofitClient apiRetrofitClient;
    private String data = "";
    ServiceCall mServiceCall ;

    List<OfficesModel> myOfficesResponse;
    ArrayList<OfficesModel> lstOfficesData;
    ArrayList<String> lstOfficesNames;
    HashMap<String,String> hshMapOffices;

    List<SectionsModel> mySectionResponse;
    ArrayList<SectionsModel> lstSectionData;
    ArrayList<String> lstSectionNames;
    HashMap<String,String> hshMapSection;
    String strSectionID = "";
    String strSPID = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_add_petition);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_petition);
        init();
    }

    private void  init() {

        myOfficesResponse= new ArrayList<>();
        lstOfficesNames = new ArrayList<>();
        hshMapOffices = new HashMap<>();

        mySectionResponse= new ArrayList<>();
        lstSectionNames = new ArrayList<>();
        hshMapSection = new HashMap<>();

        mServiceCall = new ServiceCall();
        apiRetrofitClient  = new APIRetrofitClient();
        appLocationService = new AppLocationService(AddPetitionActivity.this);

        binding.imageView1.setOnClickListener(this);
        binding.imageView2.setOnClickListener(this);
        binding.imageView3.setOnClickListener(this);
        binding.imageView4.setOnClickListener(this);
        binding.imageView5.setOnClickListener(this);
        binding.imageView6.setOnClickListener(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.mipmap.ic_launcher_icon);
        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.gradient_theme));

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        getSupportActionBar().setTitle("Add Petition");

        requestMultiplePermissions();
        getCurrentLocation();

        binding.spOffice.setEnabled(false);
        binding.spSelection.setEnabled(false);

        binding.btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                strTitle = binding.etComment.getText().toString();
                strComments = binding.etTitle.getText().toString();
                int petitionID = -1;
                if(!strTitle.equalsIgnoreCase("") && !strComments.equalsIgnoreCase("") && !strSectionID.equalsIgnoreCase("") && !strSPID.equalsIgnoreCase("")) {
                    String MemberID = getStringSharedPreference(AddPetitionActivity.this, "MemberID");
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
                        jsonParam.put("remarks", strComments);
                        Log.i("JSON", jsonParam.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    new AsyncSubmitPetition().execute(jsonParam.toString()/*,ADD_PETITION*/);
                }else
                    Toast.makeText(AddPetitionActivity.this, "Please fill all the fileds", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private ArrayList<OfficesModel> getOfficesbyGeoLocation(double lat, double lang)
    {
        showProgressDialog(AddPetitionActivity.this);
        apiRetrofitClient = new APIRetrofitClient();
        Retrofit retrofit = apiRetrofitClient.getRetrofit(APIInterface.BASE_URL);
        APIInterface api = retrofit.create(APIInterface.class);
        Call<List<OfficesModel>> call = api.getOfficesbyGeo(String.valueOf(lat), String.valueOf(lang));

        call.enqueue(new Callback<List<OfficesModel>>() {
            @Override
            public void onResponse(Call<List<OfficesModel>> call, Response<List<OfficesModel>> response) {
                if(response != null) {
                    myOfficesResponse = response.body();
                    if(myOfficesResponse.size() > 0) {
                        lstOfficesData = new ArrayList<OfficesModel>();
                        for (Object object : myOfficesResponse) {
                            lstOfficesData.add((OfficesModel) object);
                        }
                        loadOfficeAdapter(lstOfficesData);
                        hideProgressDialog(AddPetitionActivity.this);

                    }else {
                        hideProgressDialog(AddPetitionActivity.this);
                    }
                }else {
                    hideProgressDialog(AddPetitionActivity.this);
                }
            }

            @Override
            public void onFailure(Call<List<OfficesModel>> call, Throwable t) {
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
                hideProgressDialog(AddPetitionActivity.this);
            }
        });
        return lstOfficesData;
    }

    private void loadOfficeAdapter(ArrayList<OfficesModel> lstOfficesData)
    {
        binding.spOffice.setEnabled(true);
        for(int i=0; i<lstOfficesData.size(); i++) {
            lstOfficesNames.add(lstOfficesData.get(i).getName().toString().trim());
            hshMapOffices.put(lstOfficesData.get(i).getName().toString().trim(),lstOfficesData.get(i).getSPID().toString().trim());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(AddPetitionActivity.this,
                android.R.layout.simple_spinner_dropdown_item,lstOfficesNames);
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
                getSectionsbySPID(strSPID);
            }
        });

    }

    private ArrayList<SectionsModel> getSectionsbySPID(String strSPID)
    {
        showProgressDialog(AddPetitionActivity.this);
        apiRetrofitClient = new APIRetrofitClient();
        Retrofit retrofit = apiRetrofitClient.getRetrofit(APIInterface.BASE_URL);
        APIInterface api = retrofit.create(APIInterface.class);
        Call<List<SectionsModel>> call = api.getSections(strSPID);

        call.enqueue(new Callback<List<SectionsModel>>() {
            @Override
            public void onResponse(Call<List<SectionsModel>> call, Response<List<SectionsModel>> response) {
                if(response != null) {
                    mySectionResponse = response.body();
                    if(mySectionResponse.size() > 0) {
                        lstSectionData = new ArrayList<SectionsModel>();
                        for (Object object : mySectionResponse) {
                            lstSectionData.add((SectionsModel) object);
                        }
                        loadSectionAdapter(lstSectionData);
                        hideProgressDialog(AddPetitionActivity.this);

                    }else {
                        hideProgressDialog(AddPetitionActivity.this);
                    }
                }else {
                    hideProgressDialog(AddPetitionActivity.this);
                }
            }

            @Override
            public void onFailure(Call<List<SectionsModel>> call, Throwable t) {
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
                hideProgressDialog(AddPetitionActivity.this);
            }
        });
        return lstSectionData;
    }

    private void loadSectionAdapter(ArrayList<SectionsModel> lstSectionData) {
        binding.spSelection.setEnabled(true);
        for(int i=0; i<lstSectionData.size(); i++) {
            lstSectionNames.add(lstSectionData.get(i).getName().toString().trim());
            hshMapSection.put(lstSectionData.get(i).getName().toString().trim(),lstSectionData.get(i).getSectionID().toString().trim());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(AddPetitionActivity.this,
                android.R.layout.simple_spinner_dropdown_item,lstSectionNames);
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

    private class AsyncSubmitPetition extends AsyncTask<String, Void, String>
    {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog(AddPetitionActivity.this);
        }
        @Override
        protected String doInBackground(String... params) {

            String strResponse = uploadPetition(params[0]/*,params[1]*/);
            System.out.println("Response ::: " + strResponse);
           // Toast.makeText(AddPetitionActivity.this, strResponse, Toast.LENGTH_SHORT).show();
            return strResponse;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if(!result.equalsIgnoreCase("") && result != null){
                try {
                    JSONArray jsonArray = new JSONArray(result);
                    for (int i=0; i<jsonArray.length();i++)
                    {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        int nPID = jsonObject.getInt("PID");
                        int nOTP = jsonObject.getInt("OTP");
                        CustomDialog(AddPetitionActivity.this,"Petition added Successfully","",String.valueOf("OTP : " +nOTP));
                    }
                    binding.etTitle.setText("");
                    binding.etComment.setText("");
                    binding.spOffice.setText("");
                    binding.spSelection.setText("");

                }catch (Exception e)
                {
                    Crashlytics.logException(e);
                    e.printStackTrace();
                    Toast.makeText(AddPetitionActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                }
            }
            hideProgressDialog(AddPetitionActivity.this);
        }
    }

    private String uploadPetition(String jsonParam) {
       String reponse ="";
        try{
             URL url = new URL("http://api.ainext.in/petitions/addpetition");
             HttpURLConnection conn = (HttpURLConnection) url.openConnection();
             conn.setRequestMethod("POST");
             conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
             conn.setRequestProperty("Accept","application/json");
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
             }
             finally {
                 try {
                     response.close();
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             }
             reponse = sb.toString();
             Log.i("STATUS", String.valueOf(conn.getResponseCode()));
             Log.i("MSG" , conn.getResponseMessage());
             conn.disconnect();
        }catch (Exception e)
        {
            Crashlytics.logException(e);
        }
        return reponse;
    }

    private void getCurrentLocation() {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        locationRequestCode);
            } else {
                getLocation();
            }

        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void getLocation()
    {
        locationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        geocoder = new Geocoder(AddPetitionActivity.this, Locale.getDefault());
        locationProviderClient.getLastLocation().addOnSuccessListener(AddPetitionActivity.this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {

                if (location != null) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    try {
                        addresses = geocoder.getFromLocation(latitude, longitude, 1);
                        String addressLine1 = addresses.get(0).getAddressLine(0);
                        Log.e("line1", addressLine1);
                        String city = addresses.get(0).getLocality();
                        Log.e("city", city);
                        String state = addresses.get(0).getAdminArea();
                        Log.e("state", state);
                        String pinCode = addresses.get(0).getPostalCode();
                        Log.e("pinCode", pinCode);

                        currentAddress = addressLine1;
                        lat = String.valueOf(latitude);
                        lang = String.valueOf(longitude);

                        //String fullAddress = addressLine1 + ",  " + city + ",  " + state + ",  " + pinCode;
                        //binding.currentLocation.setText(addressLine1);
                        getOfficesbyGeoLocation(17.363336,78.5270394);

                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e("MainActivity", e.getMessage());
                    }

                }
            }
        });
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
        switch(requestCode){
            case 1:
                if(resultCode == RESULT_OK){
                    Bundle extras = data.getExtras();
                    photo = (Bitmap) extras.get("data");
                    binding.imageView1.setImageBitmap(photo);
                }
                break;
            case 2:
                if(resultCode == RESULT_OK) {
                    Bundle extras = data.getExtras();
                    photo = (Bitmap) extras.get("data");
                    binding.imageView2.setImageBitmap(photo);
                }
                break;

            case 3:
                if(resultCode == RESULT_OK){
                    Bundle extras = data.getExtras();
                    photo = (Bitmap) extras.get("data");
                    binding.imageView3.setImageBitmap(photo);
                }
                break;
            case 4:
                if(resultCode == RESULT_OK) {
                    Bundle extras = data.getExtras();
                    photo = (Bitmap) extras.get("data");
                    binding.imageView4.setImageBitmap(photo);
                }
                break;
            case 5:
                if(resultCode == RESULT_OK){
                    Bundle extras = data.getExtras();
                    photo = (Bitmap) extras.get("data");
                    binding.imageView5.setImageBitmap(photo);
                }
                break;
            case 6:
                if(resultCode == RESULT_OK) {
                    Bundle extras = data.getExtras();
                    photo = (Bitmap) extras.get("data");
                    binding.imageView6.setImageBitmap(photo);
                }
                break;
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
            case R.id.imageView6:
                startActivityForResult(intent, 6);
                break;
        }
    }
}
