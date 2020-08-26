package com.joinacf.acf.activities;

import android.Manifest;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;


import com.crashlytics.android.Crashlytics;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.JsonObject;
import com.joinacf.acf.R;
import com.joinacf.acf.bottom_nav.BottomNavigationViewNew;
import com.joinacf.acf.databinding.ActivityMainBinding;
import com.joinacf.acf.fragments.CorruptionFragment;
import com.joinacf.acf.fragments.FindnFixFragment;
import com.joinacf.acf.fragments.HomeFragment;
import com.joinacf.acf.fragments.MoreGridFragment;
import com.joinacf.acf.fragments.MyPetitionListFragment;
import com.joinacf.acf.fragments.SocialEvilFragment;
import com.joinacf.acf.network.APIRetrofitClient;
import com.joinacf.acf.utilities.App;
import com.joinacf.acf.utilities.LocationHandler;
import com.pd.chocobar.ChocoBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity.Java";
    LocationHandler locationHandler;
    ActivityMainBinding binding;
    private FusedLocationProviderClient locationProviderClient;
    private Geocoder geocoder;
    private double latitude,longitude;
    private List<Address> addresses;
    private int locationRequestCode = 1000;
    String lat,lang = "";
    String currentAddress="";
    String currentLatLang ="";
    String LatLang;
    BottomNavigationViewNew navigation;
    APIRetrofitClient apiRetrofitClient;

    private BottomNavigationViewNew.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationViewNew.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    loadHomeFragment();
                    return true;
                case R.id.navigation_corruption:
                    loadCorruptionFragment();
                    return true;
                case R.id.navigation_findnfix:
                    loadFinnFixFragment();
                    return true;
                case R.id.navigation_mypetitions:
                    //loadSocialEvilFragment();
                    loadMyPetitionsFragment();
                    return true;
                case R.id.navigation_more:
                    loadMoreFragment();
                    return true;
            }
            return false;
        }

    };

    private void loadHomeFragment() {
        HomeFragment fragment = HomeFragment.newInstance();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_frame, fragment);
        ft.commit();
    }

    private void loadCorruptionFragment() {
        CorruptionFragment fragment = CorruptionFragment.newInstance();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_frame, fragment);
        ft.commit();
    }

    private void loadFinnFixFragment() {
        FindnFixFragment fragment = FindnFixFragment.newInstance();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_frame, fragment);
        ft.commit();
    }

    private void loadSocialEvilFragment() {
        SocialEvilFragment fragment = SocialEvilFragment.newInstance();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_frame, fragment);
        ft.commit();
    }

    private void loadMyPetitionsFragment() {
        MyPetitionListFragment fragment = MyPetitionListFragment.newInstance();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_frame, fragment);
        ft.commit();
    }

    private void loadMoreFragment() {
        MoreGridFragment fragment = MoreGridFragment.newInstance();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_frame, fragment);
        ft.commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        apiRetrofitClient = new APIRetrofitClient();

        navigation = (BottomNavigationViewNew) findViewById(R.id.bottom_navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setVisibility(View.VISIBLE);
        showBottomNavigation();

        getLatestVersion();

        loadHomeFragment();

        /*BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        //CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) bottomNavigationView.getLayoutParams();
        //layoutParams.setBehavior(new BottomNavigationViewBehavior());
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        loadHomeFragment();
                        return true;
                    case R.id.navigation_corruption:
                        loadCorruptionFragment();
                        return true;
                    case R.id.navigation_findnfix:
                        loadFinnFixFragment();
                        return true;
                    case R.id.navigation_socialevil:
                        loadSocialEvilFragment();
                        return true;
                    case R.id.navigation_more:
                        loadMoreFragment();
                        return true;
                }
                return true;
            }
        });*/

    }

    private String getCurrentVersion(){
        PackageManager pm = this.getPackageManager();
        PackageInfo pInfo = null;
        try {
            pInfo =  pm.getPackageInfo(this.getPackageName(),0);
        } catch (PackageManager.NameNotFoundException e1) {
            e1.printStackTrace();
        }
        String currentVersion = pInfo.versionName;
        return currentVersion;
    }

    private void getLatestVersion() {
        String latestVersion = "";
        String currentVersion = getCurrentVersion();
        Log.d(TAG, "Current version = " + currentVersion);
        try {
            latestVersion = new GetLatestVersion().execute().get();
            Log.d(TAG, "Latest version = " + latestVersion);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        //If the versions are not the same
        if(!currentVersion.equals(latestVersion)){
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("An Update is Available");
            builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //Click button action
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=your app package address")));
                    dialog.dismiss();
                }
            });

            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //Cancel button action
                }
            });

            builder.setCancelable(false);
            builder.show();
        }

        getLocationUpdate();
    }

    private void getLocationUpdate() {
        boolean isLocationEnabled = isLocationEnabled(MainActivity.this);
        boolean bFirst = getBooleanSharedPreference(MainActivity.this, "FirstTime");
        if(bFirst) {
            if (isLocationEnabled) {
                getCurrentLocation();
            } else {
                LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("GPS not found")  // GPS not found
                            .setMessage("Want to enable?") // Want to enable?
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                                }
                            })
                            .setNegativeButton("No", null)
                            .show();
                }
            }
        }
    }

    private class GetLatestVersion extends AsyncTask<String, String, String> {
        String latestVersion;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                //It retrieves the latest version by scraping the content of current version from play store at runtime
                String urlOfAppFromPlayStore = "https://play.google.com/store/apps/details?id= your app package address";
                Document doc = Jsoup.connect(urlOfAppFromPlayStore).get();
                latestVersion = doc.getElementsByAttributeValue("itemprop","softwareVersion").first().text();
            }catch (Exception e){
                e.printStackTrace();
            }
            return latestVersion;
        }
    }


    public void hideBottomNavigation()
    {
        navigation.setVisibility(View.GONE);
        /*TranslateAnimation animate = new TranslateAnimation(
                0,                 // fromXDelta
                0,                 // toXDelta
                0,                 // fromYDelta
                navigation.getHeight()); // toYDelta
        animate.setDuration(500);
        animate.setFillAfter(true);
        navigation.startAnimation(animate);*/
    }

    public void showBottomNavigation()
    {
        navigation.setVisibility(View.VISIBLE);
       /* TranslateAnimation animate = new TranslateAnimation(
                0,                 // fromXDelta
                0,                 // toXDelta
                navigation.getHeight(),  // fromYDelta
                0);                // toYDelta
        animate.setDuration(500);
        animate.setFillAfter(true);
        navigation.startAnimation(animate);*/
    }

    public class AsyncUpdateMembersLocation extends AsyncTask<String,String,String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog(MainActivity.this,"Updating Member location");
        }
        @Override
        protected String doInBackground(String... strings) {
            String latLang = strings[0];
            String split[] = latLang.split("#");
            return updateCurrentLocation(split[0].toString(),split[1].toString());
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            System.out.println(result);
            hideProgressDialog(MainActivity.this);
            showBottomNavigation();
            int nStatus = -2;
            if(result != null && !result.equalsIgnoreCase(""))
            {
                try {
                    JSONObject jobject = new JSONObject(result);
                    if (result.length() > 0) {
                        if (jobject.has("message")) {
                            if(jobject.getString("message").equalsIgnoreCase("SUCCESS")) {
                                if (jobject.has("result")) {
                                    JSONArray jsonArray = new JSONArray(jobject.getString("result"));
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                                        if(jsonObject.has("STATUS"))
                                            nStatus = jsonObject.getInt("STATUS");
                                    }
                                    if (nStatus == 1)
                                        Toast.makeText(MainActivity.this, "Location Updated successfully", Toast.LENGTH_SHORT).show();
                                    else if (nStatus == 0)
                                        Toast.makeText(MainActivity.this, "Error while updating Location", Toast.LENGTH_SHORT).show();
                                    else
                                        Toast.makeText(MainActivity.this, "failed to update Location", Toast.LENGTH_SHORT).show();

                                }
                            }
                        }
                    }
                }catch (JSONException e)
                {
                    e.printStackTrace();
                    Crashlytics.logException(e);
                }
            }
        }
    }

    private String updateCurrentLocation(String lat, String lang) {
        String reponse ="";
        JsonObject json = new JsonObject();
        try{
            try {
                json.addProperty("memberid",getStringSharedPreference(MainActivity.this, "MemberID"));
                json.addProperty("latitude", lat);
                json.addProperty("langitude", lang);
            }catch (Exception e)
            {
                e.printStackTrace();
                Crashlytics.logException(e);
            }
            URL url = new URL("http://api.ainext.in/emergency/updatemlocation");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            conn.setRequestProperty("Accept","application/json");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            DataOutputStream os = new DataOutputStream(conn.getOutputStream());
            os.writeBytes(json.toString());
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

        /*try{
            Retrofit retrofit = apiRetrofitClient.getRetrofit(APIInterface.BASE_URL);
            APIInterface api = retrofit.create(APIInterface.class);
            JsonObject json = new JsonObject();
            try {
                json.addProperty("memberid",getStringSharedPreference(MainActivity.this, "MemberID"));
                json.addProperty("latitude", lat);
                json.addProperty("langitude", lang);
            }catch (Exception e)
            {
                e.printStackTrace();
                Crashlytics.logException(e);
            }
            Call<ResponseBody> call = api.updateMemLocation(json);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    ResponseBody myProfileData = response.body();
                    System.out.println(myProfileData);
                }
                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }catch (Exception e)
        {
            Crashlytics.logException(e);
        }*/
    }

    public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        }else{
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
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
        //return currentLatLang;
    }

    public String getLocation()
    {
        locationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
        locationProviderClient.getLastLocation().addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
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

                        lat = String.valueOf(latitude);
                        lang = String.valueOf(longitude);
                        currentAddress = lat +"#"+lang;
                        if(App.isNetworkAvailable())
                            new AsyncUpdateMembersLocation().execute(currentAddress);
                        else{
                            ChocoBar.builder().setView(binding.container)
                                    .setText("No Internet connection")
                                    .setDuration(ChocoBar.LENGTH_INDEFINITE)
                                    //.setActionText(android.R.string.ok)
                                    .red()   // in built red ChocoBar
                                    .show();
                        }
                        //String fullAddress = addressLine1 + ",  " + city + ",  " + state + ",  " + pinCode;
                        //binding.currentLocation.setText(addressLine1);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e("MainActivity", e.getMessage());
                    }
                }
            }
        });
        return currentAddress;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK)
        {
            showAlert(MainActivity.this,"Exit Alert","Do you want to exit from the Application?",true,"Yes","No");
        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e("FromActivity","resume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.e("FromActivity","Pause");

    }
}



 /*extends BottomBarHolderActivity implements HomeFragment.OnFragmentInteractionListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NavigationPage page1 = new NavigationPage("Home", ContextCompat.getDrawable(this, R.drawable.ic_home_black_24dp), HomeFragment.newInstance());
        NavigationPage page2 = new NavigationPage("Corruption", ContextCompat.getDrawable(this, R.drawable.ic_corruption), CorruptionFragment.newInstance());
        //NavigationPage page3 = new NavigationPage("Adulteration", ContextCompat.getDrawable(this, R.drawable.ic_adulteration), AdulterationFragment.newInstance());
        NavigationPage page4 = new NavigationPage("Find n Fix", ContextCompat.getDrawable(this, R.drawable.ic_findnfix), FindnFixFragment.newInstance());
        NavigationPage page5 = new NavigationPage("Social Evil", ContextCompat.getDrawable(this, R.drawable.ic_social_evil), SocialEvilFragment.newInstance());
        NavigationPage page6 = new NavigationPage("More", ContextCompat.getDrawable(this, R.drawable.ic_more), MoreGridFragment.newInstance());
        List<NavigationPage> navigationPages = new ArrayList<>();
        navigationPages.add(page1);
        navigationPages.add(page2);
         navigationPages.add(page4);
        navigationPages.add(page5);
        navigationPages.add(page6);
        super.setupBottomBarHolderActivity(navigationPages);
    }
}*/