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
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import com.joinacf.acf.R;
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

import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import static com.joinacf.acf.network.APIInterface.ADD_PETITION;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_add_petition);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_petition);
        init();

        requestMultiplePermissions();

        binding.imageView1.setOnClickListener(this);
        binding.imageView2.setOnClickListener(this);
        binding.imageView3.setOnClickListener(this);
        binding.imageView4.setOnClickListener(this);
        binding.imageView5.setOnClickListener(this);
        binding.imageView6.setOnClickListener(this);

        binding.btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                strTitle = binding.etComment.getText().toString();
                strComments = binding.etTitle.getText().toString();
                int petitionID = -1;
                int spid = 1;
                int sectionID = 1;
                int createBy = 1;
                JSONObject jsonParam = new JSONObject();
                try{
                    jsonParam.put("petitionID", petitionID);
                    jsonParam.put("spid", spid);
                    jsonParam.put("sectionID", sectionID);
                    jsonParam.put("title", "dffgfg");
                    jsonParam.put("image1", "");
                    jsonParam.put("image2", "");
                    jsonParam.put("image3", "");
                    jsonParam.put("image4", "");
                    jsonParam.put("image5", "");
                    jsonParam.put("image6", "");
                    jsonParam.put("createBy", 1);
                    jsonParam.put("location", currentAddress);
                    jsonParam.put("latitude", lat);
                    jsonParam.put("langitude", lang);
                    jsonParam.put("remarks", strComments);
                    Log.i("JSON", jsonParam.toString());
                }catch (Exception e){
                    e.printStackTrace();
                }

                new AsyncCaller().execute(jsonParam.toString()/*,ADD_PETITION*/);
            }
        });
    }


    private void  init() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.mipmap.ic_launcher_icon);
        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.gradient_theme));

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        getSupportActionBar().setTitle("Add Petition");
        getCurrentLocation();

        mServiceCall = new ServiceCall();
        apiRetrofitClient  = new APIRetrofitClient();
        appLocationService = new AppLocationService(AddPetitionActivity.this);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(AddPetitionActivity.this,
                android.R.layout.simple_spinner_dropdown_item) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                View v = super.getView(position, convertView, parent);
                if (position == getCount()) {
                    ((TextView)v.findViewById(android.R.id.text1)).setText("");
                    ((TextView)v.findViewById(android.R.id.text1)).setHint(getItem(getCount())); //"Hint to be displayed"
                }
                return v;
            }

            @Override
            public int getCount() {
                return super.getCount()-1; // you dont display last item. It is used as hint.
            }
        };

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter.add("Daily");
        adapter.add("Two Days");
        adapter.add("Weekly");
        adapter.add("Monthly");
        adapter.add("Three Months");
        adapter.add("Select Office at your Current Location"); //This is the text that will be displayed as hint.


        binding.spOffice.setAdapter(adapter);
        binding.spOffice.setSelection(adapter.getCount()); //set the hint the default selection so it appears on launch.
        binding.spSelection.setAdapter(adapter);
        binding.spSelection.setSelection(adapter.getCount()); //set the hint the default selection so it appears on launch.

    }

    private class AsyncCaller extends AsyncTask<String, Void, String>
    {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog(AddPetitionActivity.this);
        }
        @Override
        protected String doInBackground(String... params) {

            String strResponse = mServiceCall.sendPost(params[0]/*,params[1]*/);
            System.out.println("Response ::: " + strResponse);
           // Toast.makeText(AddPetitionActivity.this, strResponse, Toast.LENGTH_SHORT).show();
            return strResponse;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            hideProgressDialog(AddPetitionActivity.this);
        }
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
