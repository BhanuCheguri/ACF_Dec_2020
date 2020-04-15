package com.joinacf.acf.activities;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
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
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
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
    private double latitude,longitude;
    private List<Address> addresses;
    private int locationRequestCode = 1000;
    String lat,lang = "";
    String currentAddress="";
    String strTitle,strComments ="";
    AppLocationService appLocationService;
    APIRetrofitClient apiRetrofitClient;
    String strResponse = "";
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
    private String TAG = "AddPetitionActivity.class";
    private ArrayList<String> lstPathURI;

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
        lstPathURI = new ArrayList<>();


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

        getSupportActionBar().setTitle("Tag your Petition");

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
                    new AsyncUploadImages().execute();


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
        String imgcurTime = dateFormat.format(new Date());
        String dir = getFilesDir().getAbsolutePath();

        switch(requestCode){
            case 1:
                if(resultCode == RESULT_OK){
                    Bundle extras = data.getExtras();
                    photo = (Bitmap) extras.get("data");
                    binding.imageView1.setImageBitmap(photo);
                    saveFile(AddPetitionActivity.this,photo,imgcurTime + ".jpg");
                    String imagePath = dir + "/"+imgcurTime + ".jpg";
                    lstPathURI.add(imagePath);
                }
                break;
            case 2:
                if(resultCode == RESULT_OK) {
                    Bundle extras = data.getExtras();
                    photo = (Bitmap) extras.get("data");
                    binding.imageView2.setImageBitmap(photo);
                    saveFile(AddPetitionActivity.this,photo,imgcurTime + ".jpg");
                    String imagePath = dir + "/"+imgcurTime + ".jpg";
                    lstPathURI.add(imagePath);
                }
                break;

            case 3:
                if(resultCode == RESULT_OK){
                    Bundle extras = data.getExtras();
                    photo = (Bitmap) extras.get("data");
                    binding.imageView3.setImageBitmap(photo);
                    saveFile(AddPetitionActivity.this,photo,imgcurTime + ".jpg");
                    String imagePath = dir + "/"+imgcurTime + ".jpg";
                    lstPathURI.add(imagePath);
                }
                break;
            case 4:
                if(resultCode == RESULT_OK) {
                    Bundle extras = data.getExtras();
                    photo = (Bitmap) extras.get("data");
                    binding.imageView4.setImageBitmap(photo);
                    saveFile(AddPetitionActivity.this,photo,imgcurTime + ".jpg");
                    String imagePath = dir + "/"+imgcurTime + ".jpg";
                    lstPathURI.add(imagePath);
                }
                break;
            case 5:
                if(resultCode == RESULT_OK){
                    Bundle extras = data.getExtras();
                    photo = (Bitmap) extras.get("data");
                    binding.imageView5.setImageBitmap(photo);
                    saveFile(AddPetitionActivity.this,photo,imgcurTime + ".jpg");
                    String imagePath = dir + "/"+imgcurTime + ".jpg";
                    lstPathURI.add(imagePath);
                }
                break;
            case 6:
                if(resultCode == RESULT_OK) {
                    Bundle extras = data.getExtras();
                    photo = (Bitmap) extras.get("data");
                    binding.imageView6.setImageBitmap(photo);
                    saveFile(AddPetitionActivity.this,photo,imgcurTime + ".jpg");
                    String imagePath = dir + "/"+imgcurTime + ".jpg";
                    lstPathURI.add(imagePath);
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
        catch (IOException e) {
            Log.d(TAG, "io exception");
            e.printStackTrace();
        } finally {
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
            showProgressDialog(AddPetitionActivity.this,"Please wait.. We are uploading your images");
        }
        @Override
        protected String doInBackground(String... strings) {
            String result = null;
            if(!lstPathURI.isEmpty())
            {
                result = mulipleFileUploadFile(lstPathURI);
            }

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            hideProgressDialog(AddPetitionActivity.this);

            if(result != null && result.equalsIgnoreCase("")) {
                CustomDialog(AddPetitionActivity.this, "Thank You", "Your request has been successfully posted. We will process and keep in touch with you.", "");
            }else {
                showAlert(AddPetitionActivity.this, "Failed to upload data", "Result:" + result, "OK");
            }
            binding.imageView1.setImageResource(R.drawable.ic_add_item);
            binding.imageView2.setImageResource(R.drawable.ic_add_item);
            binding.imageView3.setImageResource(R.drawable.ic_add_item);
            binding.imageView3.setImageResource(R.drawable.ic_add_item);
            binding.imageView4.setImageResource(R.drawable.ic_add_item);
            binding.imageView5.setImageResource(R.drawable.ic_add_item);
            binding.imageView6.setImageResource(R.drawable.ic_add_item);
        }
    }


    private String mulipleFileUploadFile(ArrayList<String> fileUri) {
        try {
            OkHttpClient okHttpClient = new OkHttpClient();
            OkHttpClient clientWith30sTimeout = okHttpClient.newBuilder()
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();

            Retrofit retrofit = apiRetrofitClient.getRetrofit(APIInterface.BASE_URL);
            APIInterface api = retrofit.create(APIInterface.class);
            Map<String, RequestBody> maps = new HashMap<>();


            if (fileUri != null && fileUri.size() > 0) {
                for (int i = 0; i < fileUri.size(); i++) {

                    Uri contentURI = Uri.fromFile(new File(fileUri.get(i).toString()));
                    String filePath = getPathFromUri(AddPetitionActivity.this,contentURI );
                    File file1 = new File(filePath);

                    if (filePath != null && filePath.length() > 0) {
                        if (file1.exists()) {
                            okhttp3.RequestBody requestFile = okhttp3.RequestBody.create(okhttp3.MediaType.parse("multipart/form-data"), file1);
                            String filename = "imagePath" + i; //key for upload file like : imagePath0
                            maps.put(filename + "\"; filename=\"" + file1.getName(), requestFile);
                        }
                    }
                }
            }

            //hear is the your json request
            Call<ResponseBody> call = api.uploadImages(maps);
            call.enqueue(new Callback<ResponseBody>() {
                @SuppressLint("LongLogTag")
                @Override
                public void onResponse(Call<ResponseBody> call,
                                       Response<ResponseBody> response) {
                    try {
                        Log.i(TAG, "success");
                        strResponse = response.body().toString();
                        Log.d("body==>", response.body().toString() + "");
                        System.out.println("body==>" + response.body().toString());

                    }catch (Exception e)
                    {
                        strResponse = "";
                        e.printStackTrace();
                        Crashlytics.logException(e);
                        hideProgressDialog(AddPetitionActivity.this);
                        showErrorAlert(AddPetitionActivity.this,"Failed to upload images");
                    }
                }

                @SuppressLint("LongLogTag")
                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.e(TAG, t.getMessage());
                    hideProgressDialog(AddPetitionActivity.this);
                    showErrorAlert(AddPetitionActivity.this,t.toString());
                }
            });
        }catch (Exception e)
        {
            hideProgressDialog(AddPetitionActivity.this);
            e.printStackTrace();
            Crashlytics.logException(e);
        }
        return strResponse;
    }

    public static String getPathFromUri(final Context context, final Uri uri) {

        // check here to KITKAT or new version
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        String selection = null;
        String[] selectionArgs = null;
        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                String fullPath = getPathFromExtSD(split);
                if (fullPath != "") {
                    return fullPath;
                } else {
                    return null;
                }
            }

            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    final String id;
                    Cursor cursor = null;
                    try {
                        cursor = context.getContentResolver().query(uri, new String[]{MediaStore.MediaColumns.DISPLAY_NAME}, null, null, null);
                        if (cursor != null && cursor.moveToFirst()) {
                            String fileName = cursor.getString(0);
                            String path = Environment.getExternalStorageDirectory().toString() + "/Download/" + fileName;
                            if (!TextUtils.isEmpty(path)) {
                                return path;
                            }
                        }
                    } finally {
                        if (cursor != null)
                            cursor.close();
                    }
                    id = DocumentsContract.getDocumentId(uri);
                    if (!TextUtils.isEmpty(id)) {
                        if (id.startsWith("raw:")) {
                            return id.replaceFirst("raw:", "");
                        }
                        String[] contentUriPrefixesToTry = new String[]{
                                "content://downloads/public_downloads",
                                "content://downloads/my_downloads"
                        };
                        for (String contentUriPrefix : contentUriPrefixesToTry) {
                            try {
                                final Uri contentUri = ContentUris.withAppendedId(Uri.parse(contentUriPrefix), Long.valueOf(id));

                         /*   final Uri contentUri = ContentUris.withAppendedId(
                                    Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));*/

                                return getDataColumn(context, contentUri, null, null);
                            } catch (NumberFormatException e) {
                                //In Android 8 and Android P the id is not a number
                                return uri.getPath().replaceFirst("^/document/raw:", "").replaceFirst("^raw:", "");
                            }
                        }


                    }

                } else {
                    final String id = DocumentsContract.getDocumentId(uri);
                    final boolean isOreo = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
                    if (id.startsWith("raw:")) {
                        return id.replaceFirst("raw:", "");
                    }
                    try {
                        contentUri = ContentUris.withAppendedId(
                                Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    if (contentUri != null) {
                        return getDataColumn(context, contentUri, null, null);
                    }
                }


            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;

                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                selection = "_id=?";
                selectionArgs = new String[]{split[1]};


                return getDataColumn(context, contentUri, selection,
                        selectionArgs);
            } else if (isGoogleDriveUri(uri)) {
                return getDriveFilePath(uri, context);
            }
        }


        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            if (isGooglePhotosUri(uri)) {
                return uri.getLastPathSegment();
            }

            if (isGoogleDriveUri(uri)) {
                return getDriveFilePath(uri, context);
            }
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N) {
                // return getFilePathFromURI(context,uri);
                return getMediaFilePathForN(uri, context);
                // return getRealPathFromURI(context,uri);
            } else {

                return getDataColumn(context, uri, null, null);
            }


        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Check if a file exists on device
     *
     * @param filePath The absolute file path
     */
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
            }
        });

        dialog.show();
    }
}
