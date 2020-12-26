package com.anticorruptionforce.acf.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.anticorruptionforce.acf.adapters.HomePageAdapter;
import com.anticorruptionforce.acf.databinding.ActivityModeratorBinding;
import com.bumptech.glide.Glide;
//import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.JsonObject;
import com.anticorruptionforce.acf.R;
import com.anticorruptionforce.acf.modelclasses.ModeratorListModel;
import com.anticorruptionforce.acf.modelclasses.ModeratorStatusModel;
import com.anticorruptionforce.acf.modelclasses.ProviderModel;
import com.anticorruptionforce.acf.modelclasses.ResultModel;
import com.anticorruptionforce.acf.network.APIInterface;
import com.anticorruptionforce.acf.network.APIRetrofitClient;
import com.anticorruptionforce.acf.utilities.App;
import com.pd.chocobar.ChocoBar;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static com.anticorruptionforce.acf.utilities.DataUtilities.getExtensionType;
import static com.anticorruptionforce.acf.utilities.DataUtilities.loadImagePath;
import static com.anticorruptionforce.acf.utilities.DataUtilities.openFile;

public class ModeratorActivity extends BaseActivity {

    ActivityModeratorBinding binding;
    APIRetrofitClient apiRetrofitClient;
    ModeratorListModel myResponse;
    ArrayList<ModeratorListModel.Result> lstModeratorData;
    ProviderModel myProviderResponse;
    ArrayList<ProviderModel.Result> lstProviderData;
    ArrayList<String> lstProvider = new ArrayList<>();
    ModeratorStatusModel myStatusResponse;
    ArrayList<ModeratorStatusModel.Result> lstStatusData;
    ArrayList<String> lstStatus;
    ResultModel myResultResponse;
    ArrayList<ResultModel.Result> lstResult = new ArrayList<>();
    HashMap<String,Integer> hshStatus = new HashMap<>();
    ArrayList<String> listStatus =new ArrayList<>();
    APIInterface api;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_moderator);
        init();
    }

    public void init()
    {
        apiRetrofitClient = new APIRetrofitClient();
        Retrofit retrofit = apiRetrofitClient.getRetrofit(APIInterface.BASE_URL);
        api = retrofit.create(APIInterface.class);

        hshStatus.put("ACB",1);
        hshStatus.put("Department –L1" , 2);
        hshStatus.put("Department –L2", 3);
        hshStatus.put("Expert/Legal",4);
        hshStatus.put("Health", 5);
        hshStatus.put("Pending", 6);
        hshStatus.put("Police", 7);
        hshStatus.put("Publish", 8);

        for ( String key : hshStatus.keySet() ) {
            System.out.println( key );
            listStatus.add(key);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.mipmap.ic_launcher_icon);
        setActionBarTitle("Moderator");
        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.gradient_theme));
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        new AsyncGetProviderDetails().execute();
    }

    public class AsyncGetModerationStatus extends AsyncTask<String,String,String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog(ModeratorActivity.this, "Please wait...\nFetching Service Provider Data");
        }

        @Override
        protected String doInBackground(String... strings) {
            if(App.isNetworkAvailable())
                getModerationStatus();
            else{
                ChocoBar.builder().setView(binding.mainLayout)
                        .setText("No Internet connection")
                        .setDuration(ChocoBar.LENGTH_INDEFINITE)
                        .red()
                        .show();
            }
            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            hideProgressDialog(ModeratorActivity.this);
            new AsyncGetModeratorDetails().execute();
        }
    }

    private void getModerationStatus()
    {
        Call<JsonObject> call = api.getmoderationstatus();

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                hideProgressDialog(ModeratorActivity.this);
                if (response != null) {
                    JsonObject myStatusResponse = response.body();
                    System.out.println(myStatusResponse);

                    try{
                        if (myStatusResponse.has("message")) {
                            if (myStatusResponse.get("message").toString().equalsIgnoreCase("Error")) {
                                if (myStatusResponse.has("result")) {
                                    JSONArray jsonArray = new JSONArray(myStatusResponse.get("result"));
                                    System.out.println(jsonArray);
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        JSONObject json = jsonArray.getJSONObject(i);
                                        Iterator<String> keys = json.keys();

                                        while (keys.hasNext()) {
                                            String key = keys.next();
                                            System.out.println("Key :" + key + "  Value :" + json.get(key));
                                            //hshStatus.put(key, json.get(key).toString());
                                            listStatus.add(key);
                                            System.out.println(listStatus);
                                        }

                                    }
                                }
                            }
                        }
                    }catch (Exception e)
                    {
                        e.printStackTrace();
                    }

                    /*if (myStatusResponse != null) {
                        String msg = myStatusResponse.getMessage();
                        if (msg.equalsIgnoreCase("SUCCESS")) {
                            lstStatusData = myStatusResponse.getResult();
                            for (int i =0;i < lstStatusData.size();i++)
                            {
                                ModeratorStatusModel.Result providerModel = lstStatusData.get(i);
                                lstStatusData.add(ModeratorStatusModel.getName().toString());
                            }
                        } else
                            Toast.makeText(ModeratorActivity.this, "No data", Toast.LENGTH_SHORT).show();
                    } else
                        Toast.makeText(ModeratorActivity.this, "No data", Toast.LENGTH_SHORT).show();*/
                } else
                    Toast.makeText(ModeratorActivity.this, "No data", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
                hideProgressDialog(ModeratorActivity.this);
            }
        });
    }

    public class AsyncGetProviderDetails extends AsyncTask<String,String,String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog(ModeratorActivity.this, "Please wait...\nFetching Service Provider Data");
        }

        @Override
        protected String doInBackground(String... strings) {
            if(App.isNetworkAvailable())
                getProviderDetails();
            else{
                ChocoBar.builder().setView(binding.mainLayout)
                        .setText("No Internet connection")
                        .setDuration(ChocoBar.LENGTH_INDEFINITE)
                        .red()
                        .show();
            }
            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            hideProgressDialog(ModeratorActivity.this);
            new AsyncGetModeratorDetails().execute();
        }
    }

    private void getProviderDetails()
    {
        Call<ProviderModel> call = api.getServiceProviders();

        call.enqueue(new Callback<ProviderModel>() {
            @Override
            public void onResponse(Call<ProviderModel> call, Response<ProviderModel> response) {
                hideProgressDialog(ModeratorActivity.this);
                if (response != null) {
                    myProviderResponse = response.body();
                    if (myProviderResponse != null) {
                        String msg = myProviderResponse.getMessage();
                        if (msg.equalsIgnoreCase("SUCCESS")) {
                            lstProviderData = myProviderResponse.getResult();
                            for (int i =0;i < lstProviderData.size();i++)
                            {
                                ProviderModel.Result providerModel = lstProviderData.get(i);
                                lstProvider.add(providerModel.getName().toString());
                            }
                        } else
                            Toast.makeText(ModeratorActivity.this, "No data", Toast.LENGTH_SHORT).show();
                    } else
                        Toast.makeText(ModeratorActivity.this, "No data", Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(ModeratorActivity.this, "No data", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onFailure(Call<ProviderModel> call, Throwable t) {
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
                hideProgressDialog(ModeratorActivity.this);
            }
        });
    }


    public class AsyncGetModeratorDetails extends AsyncTask<String,String,String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog(ModeratorActivity.this, "Please wait...\nFetching Service Provider Data");
        }

        @Override
        protected String doInBackground(String... strings) {
            if(App.isNetworkAvailable())
                getModeratorDetails();
            else{
                ChocoBar.builder().setView(binding.mainLayout)
                        .setText("No Internet connection")
                        .setDuration(ChocoBar.LENGTH_INDEFINITE)
                        .red()
                        .show();
            }
            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            hideProgressDialog(ModeratorActivity.this);
        }
    }
    private void getModeratorDetails() {

        Call<ModeratorListModel> call = api.getitemsformod("-1");

        call.enqueue(new Callback<ModeratorListModel>() {
            @Override
            public void onResponse(Call<ModeratorListModel> call, Response<ModeratorListModel> response) {
                hideProgressDialog(ModeratorActivity.this);
                if (response != null) {
                    binding.llNoData.setVisibility(View.GONE);
                    myResponse = response.body();
                    if (myResponse != null) {
                        String msg = myResponse.getMessage();
                        if (msg.equalsIgnoreCase("SUCCESS")) {
                            lstModeratorData = myResponse.getResult();
                            binding.lvModerator.setLayoutManager(new LinearLayoutManager(ModeratorActivity.this));
                            binding.lvModerator.setItemAnimator(new DefaultItemAnimator());
                            binding.lvModerator.setAdapter(new MyModeratorAdapter(ModeratorActivity.this,lstModeratorData,lstProvider,listStatus));
                        } else
                            binding.llNoData.setVisibility(View.VISIBLE);
                    } else
                        binding.llNoData.setVisibility(View.VISIBLE);
                } else
                    binding.llNoData.setVisibility(View.VISIBLE);
            }
            @Override
            public void onFailure(Call<ModeratorListModel> call, Throwable t) {
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
                hideProgressDialog(ModeratorActivity.this);
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
                putBooleanSharedPreference(ModeratorActivity.this, "AdminLoginForM", false);
                putBooleanSharedPreference(ModeratorActivity.this, "AdminLoginForSP", false);
                putBooleanSharedPreference(ModeratorActivity.this, "AdminLogin", false);
                Intent intent = new Intent(ModeratorActivity.this,NewLoginActivity.class);
                startActivity(intent);
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    public class MyModeratorAdapter extends RecyclerView.Adapter<MyModeratorAdapter.ViewHolder> {
        public ArrayList<ModeratorListModel.Result> dataSet;
        FragmentActivity context;
        ArrayList<String> lstStatus;
        ArrayList<String> lstProvider;
        ArrayAdapter statusAdapter;
        ArrayAdapter providerAdapter;
        int status ;
        int provider ;
        private ViewHolder finalHolder1;

        public MyModeratorAdapter(FragmentActivity context, ArrayList<ModeratorListModel.Result> data, ArrayList<String> provider, ArrayList<String> listStatus) {
            this.dataSet = data;
            this.context = context;
            lstProvider = provider;
            lstStatus = listStatus;
            //lstStatus  = new ArrayList<>();
            loadSPStatus();
            loadSPProvider();
        }

        private void loadSPStatus()
        {
            statusAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, lstStatus);
        }

        private void loadSPProvider() {
            providerAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, lstProvider);
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            TextView txtTitle;
            TextView txtDescription;
            TextView txtLocation;
            TextView txtDateTime;
            Spinner spStatus;
            Spinner spProvider;
            ImageView imgFilePath;
            LinearLayout linearLayout;
            LinearLayout ll_spinners;
            LinearLayout linearImages;
            Button submit;

            public ViewHolder(View rowView) {
                super(rowView);
                this.txtTitle = (TextView) rowView.findViewById(R.id.tv_title);
                this.txtDescription = (TextView) rowView.findViewById(R.id.tv_description);
                this.txtLocation = (TextView) rowView.findViewById(R.id.tv_location);
                this.txtDateTime = (TextView) rowView.findViewById(R.id.tv_DateTime);
                this.spStatus = (Spinner) rowView.findViewById(R.id.sp_ModeratorStatus);
                this.spProvider = (Spinner) rowView.findViewById(R.id.sp_ModeratorProvider);
                this.imgFilePath = (ImageView)rowView.findViewById(R.id.imgFilePath);
                this.linearLayout = (LinearLayout)rowView.findViewById(R.id.linear);
                this.ll_spinners = (LinearLayout)rowView.findViewById(R.id.ll_spinners);
                this.submit = (Button)rowView.findViewById(R.id.submit);
                this.imgFilePath.setBackgroundResource(R.drawable.rippleeffect);
                this.linearImages = (LinearLayout) rowView.findViewById(R.id.linearImages);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.custom_moderator_layout, parent, false);

            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final ModeratorListModel.Result dataModel = dataSet.get(position);
            holder.txtTitle.setText(dataModel.getTitle());
            holder.txtDescription.setText(dataModel.getDescription());
            holder.txtLocation.setText(dataModel.getLocation());
            holder.txtDateTime.setText(getPostedDate(dataModel.getPostedDate()));

            if(dataModel.getMODSATUS().equalsIgnoreCase("PENDING")) {

                holder.ll_spinners.setVisibility(View.VISIBLE);
                holder.submit.setVisibility(View.VISIBLE);

                holder.spStatus.setAdapter(statusAdapter);
                holder.spProvider.setAdapter(providerAdapter);

                holder.spStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        status = position;
                        String selectedItem = parent.getItemAtPosition(position).toString();
                        Log.i("TAG", selectedItem);
                        Toast.makeText(ModeratorActivity.this, "Successfully assigned to " + selectedItem, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

                holder.spProvider.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        provider = position;
                        String selectedItem = parent.getItemAtPosition(position).toString();
                        Log.i("TAG", selectedItem);
                        Toast.makeText(ModeratorActivity.this, "Successfully assigned to " + selectedItem, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            }else
            {
                holder.ll_spinners.setVisibility(View.GONE);
                holder.submit.setVisibility(View.GONE);
            }


            ArrayList<String> lstFilepaths = new ArrayList<>();
            String strFilePaths = dataModel.getFilePath();


            if (strFilePaths != null && !strFilePaths.equalsIgnoreCase("")) {
                holder.linearImages.setVisibility(View.VISIBLE);
                String[] strFilepathArray = strFilePaths.split(",");
                if (strFilepathArray.length > 0) {
                    if (strFilepathArray.length > 1) {

                        holder.linearLayout.setVisibility(View.VISIBLE);
                        if(holder.linearLayout.getChildCount() > 0)
                            holder.linearLayout.removeAllViews();

                        for (int i = 0; i < strFilepathArray.length; i++) {
                            System.out.println("Image URL ::: " + strFilepathArray[i].trim().toString());
                            final String strFile = strFilepathArray[i].trim().toString();
                            if (strFile != null && !strFile.equalsIgnoreCase("")) {
                                lstFilepaths.add(strFilepathArray[i]);
                                final ImageView imageView = new ImageView(context);
                                LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(400, 350);
                                imageView.setLayoutParams(parms);
                                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                                imageView.setBackgroundResource(R.drawable.rippleeffect);
                                imageView.setId(i);
                                imageView.setImageResource(R.mipmap.ic_refresh);
                                imageView.setContentDescription(strFilepathArray[i].trim().toString());
                                imageView.setPadding(5, 5, 5, 5);
                                if (i == 0)
                                    loadImages(strFile, imageView, holder.linearLayout, true, holder.imgFilePath, strFilepathArray.length);
                                else
                                    loadImages(strFile, imageView, holder.linearLayout, false, holder.imgFilePath, strFilepathArray.length);
                                final ImageView img = holder.imgFilePath;
                                finalHolder1 = holder;
                                imageView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        String url = imageView.getContentDescription().toString();
                                        loadImagePath(url, finalHolder1.imgFilePath,context);
                                    }
                                });
                            }
                        }
                    } else {
                        String strFile = strFilepathArray[0].trim().toString();
                        try {
                            if (strFile != null && !strFile.equalsIgnoreCase("")) {
                                loadImages(strFile, null, holder.linearLayout, true, holder.imgFilePath, strFilepathArray.length);
                            }
                        } catch (Exception e) {
                            System.out.println(e);
                        }
                    }
                } else
                    holder.linearImages.setVisibility(View.GONE);
            } else {
                holder.linearImages.setVisibility(View.GONE);
            }

            holder.imgFilePath.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String content = v.getContentDescription().toString();
                    Log.i("AdapeterFilePath" ,content);
                    try {
                        String strMimeType = getExtensionType(Uri.parse(content), context);
                        System.out.println("MimeType :: "+strMimeType);
                        //openFile(Uri.parse(content), content, context);
                        if(strMimeType.equalsIgnoreCase("jpg") || strMimeType.equalsIgnoreCase("mp4")){
                            Intent intent = new Intent(context, WatchItemActivity.class);
                            intent.putExtra("MimeType",strMimeType);
                            intent.putExtra("content",content);
                            context.startActivity(intent);
                        }else
                            openFile(Uri.parse(content), content, context);
                    } catch (ActivityNotFoundException e) {
                        e.printStackTrace();
                        Toast.makeText(context, "No Activity found to handle this file. Need to install supported Application", Toast.LENGTH_LONG).show();
                    }
                }
            });

            final String MemeberID = getStringSharedPreference(context,"MemberID");
            holder.submit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    postAddModeration(holder.submit,Integer.parseInt(dataModel.getItemID()),Integer.parseInt(MemeberID),status,provider);
                }
            });
        }

        public void loadImages(String url, final ImageView img, LinearLayout linearLayout, boolean first, final ImageView imgFilePath, int length) {
            String strMimeType = getExtensionType(Uri.parse(url), context);
            if (strMimeType != null && !strMimeType.equalsIgnoreCase("")) {
                if (strMimeType.equalsIgnoreCase("jpg") || strMimeType.equalsIgnoreCase("jpeg")  || strMimeType.equalsIgnoreCase("png")) {
                    //imageLoader.DisplayImage(url, img);
                    if (first) {
                        imgFilePath.setContentDescription(url);
                        //imageLoader.DisplayImage(url, imgFilePath);
                        Glide.with(context).load(url).into(imgFilePath);
                    }else
                        Glide.with(context).load(url).override(600, 300).into(img);

                    if (length == 1)
                        linearLayout.setVisibility(View.GONE);
                    else {
                        linearLayout.setVisibility(View.VISIBLE);
                        Glide.with(context).load(url).override(600, 300).into(img);
                    }
                } else if (strMimeType.equalsIgnoreCase("mp4")) {
                    if (first) {
                        Glide.with(context).load(R.drawable.mp4).into(imgFilePath);
                        imgFilePath.setContentDescription(url);
                    }
                    if (length == 1)
                        linearLayout.setVisibility(View.GONE);
                    else {
                        linearLayout.setVisibility(View.VISIBLE);
                        Glide.with(context).load(R.drawable.mp4).override(600, 300).into(img);
                    }
                } else if (strMimeType.equalsIgnoreCase("application/pdf") || strMimeType.equalsIgnoreCase("pdf")) {
                    if (first) {
                        Glide.with(context).load(R.drawable.pdf).into(imgFilePath);
                        imgFilePath.setContentDescription(url);
                    }
                    if (length == 1)
                        linearLayout.setVisibility(View.GONE);
                    else {
                        linearLayout.setVisibility(View.VISIBLE);
                        Glide.with(context).load(R.drawable.pdf).override(600, 300).into(img);
                    }
                } else if (strMimeType.equalsIgnoreCase("docx") || strMimeType.equalsIgnoreCase("doc") || strMimeType.equalsIgnoreCase("application/vnd.openxmlformats-officedocument.wordprocessingml.document") || strMimeType.equalsIgnoreCase("application/msword")) {
                    if (first) {
                        Glide.with(context).load(R.drawable.doc).into(imgFilePath);
                        imgFilePath.setContentDescription(url);
                    }
                    if (length == 1)
                        linearLayout.setVisibility(View.GONE);
                    else {
                        Glide.with(context).load(R.drawable.doc).override(600, 300).into(img);
                        linearLayout.setVisibility(View.VISIBLE);
                    }
                } else if (strMimeType.equalsIgnoreCase("xlsx") || strMimeType.equalsIgnoreCase("xls") || strMimeType.equalsIgnoreCase("application/vnd.ms-excel") || strMimeType.equalsIgnoreCase("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
                    if (first) {
                        Glide.with(context).load(R.drawable.excel).into(imgFilePath);
                        imgFilePath.setContentDescription(url);
                    }
                    if (length == 1)
                        linearLayout.setVisibility(View.GONE);
                    else {
                        linearLayout.setVisibility(View.VISIBLE);
                        Glide.with(context).load(R.drawable.excel).override(600, 300).into(img);
                    }
                } else if (strMimeType.equalsIgnoreCase("ppt") || strMimeType.equalsIgnoreCase("application/vnd.ms-powerpoint") || strMimeType.equalsIgnoreCase("application/vnd.openxmlformats-officedocument.presentationml.presentation")) {
                    if (first) {
                        Glide.with(context).load(R.drawable.powerpoint).into(imgFilePath);
                        imgFilePath.setContentDescription(url);
                    }
                    if (length == 1)
                        linearLayout.setVisibility(View.GONE);
                    else {
                        linearLayout.setVisibility(View.VISIBLE);
                        Glide.with(context).load(R.drawable.powerpoint).override(600, 300).into(img);
                    }
                } else if (strMimeType.equalsIgnoreCase("txt") || strMimeType.equalsIgnoreCase("text/plain")) {
                    if (first) {
                        Glide.with(context).load(R.drawable.txt).into(imgFilePath);
                        imgFilePath.setContentDescription(url);
                    }
                    if (length == 1)
                        linearLayout.setVisibility(View.GONE);
                    else {
                        linearLayout.setVisibility(View.VISIBLE);
                        Glide.with(context).load(R.drawable.txt).override(600, 300).into(img);
                    }
                } else if (strMimeType.equalsIgnoreCase("mp3") || strMimeType.equalsIgnoreCase("audio/mpeg")) {
                    if (first) {
                        Glide.with(context).load(R.drawable.mp3).into(imgFilePath);
                        imgFilePath.setContentDescription(url);
                    }
                    if (length == 1)
                        linearLayout.setVisibility(View.GONE);
                    else {
                        linearLayout.setVisibility(View.VISIBLE);
                        Glide.with(context).load(R.drawable.mp3).override(600, 300).into(img);
                    }
                }
                if (linearLayout != null) {
                    linearLayout.addView(img);
                    if (length == 1)
                        linearLayout.setVisibility(View.GONE);
                    else {
                        linearLayout.setVisibility(View.VISIBLE);
                    }
                }
            }
        }


        public void loadImagePath(String url, ImageView imgFilePath, Context context) {
            String strMimeType = getExtensionType(Uri.parse(url), context);
            imgFilePath.setContentDescription(url);
            if (strMimeType != null && !strMimeType.equalsIgnoreCase("")) {
                if (strMimeType.equalsIgnoreCase("jpg") || strMimeType.equalsIgnoreCase("jpeg") || strMimeType.equalsIgnoreCase("png")) {
                    Glide.with(context).load(url).into(imgFilePath);
                } else if (strMimeType.equalsIgnoreCase("mp4")) {
                    Glide.with(context).load(R.drawable.mp4).into(imgFilePath);
                } else if (strMimeType.equalsIgnoreCase("application/pdf") || strMimeType.equalsIgnoreCase("pdf")) {
                    Glide.with(context).load(R.drawable.pdf).into(imgFilePath);
                } else if (strMimeType.equalsIgnoreCase("docx") || strMimeType.equalsIgnoreCase("doc") || strMimeType.equalsIgnoreCase("application/vnd.openxmlformats-officedocument.wordprocessingml.document") || strMimeType.equalsIgnoreCase("application/msword")) {
                    Glide.with(context).load(R.drawable.doc).into(imgFilePath);
                } else if (strMimeType.equalsIgnoreCase("xlsx") || strMimeType.equalsIgnoreCase("xls") || strMimeType.equalsIgnoreCase("application/vnd.ms-excel") || strMimeType.equalsIgnoreCase("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
                    Glide.with(context).load(R.drawable.excel).into(imgFilePath);
                } else if (strMimeType.equalsIgnoreCase("ppt") || strMimeType.equalsIgnoreCase("application/vnd.ms-powerpoint") || strMimeType.equalsIgnoreCase("application/vnd.openxmlformats-officedocument.presentationml.presentation")) {
                    Glide.with(context).load(R.drawable.powerpoint).into(imgFilePath);
                } else if (strMimeType.equalsIgnoreCase("txt") || strMimeType.equalsIgnoreCase("text/plain")) {
                    Glide.with(context).load(R.drawable.txt).into(imgFilePath);
                } else if (strMimeType.equalsIgnoreCase("mp3") || strMimeType.equalsIgnoreCase("audio/mpeg")) {
                    Glide.with(context).load(R.drawable.mp3).into(imgFilePath);
                }
            }
        }

        private void postAddModeration(Button submit, int itemID, int memeberID, int status, int provider) {

            try {
                JsonObject json = new JsonObject();
                json.addProperty("itemid", itemID);
                json.addProperty("status", status);
                json.addProperty("moderationby", memeberID);
                json.addProperty("assignedto", provider);


                Call<ResultModel> registerCall = api.addModeration(json);
                registerCall.enqueue(new retrofit2.Callback<ResultModel>() {
                    @Override
                    public void onResponse(Call<ResultModel> registerCall, retrofit2.Response<ResultModel> response) {
                        try {
                            hideProgressDialog(ModeratorActivity.this);
                            if (response != null) {
                                //binding.llNoData.setVisibility(View.GONE);
                                myResultResponse = response.body();
                                if (myResultResponse != null) {
                                    String status = myResultResponse.getStatus();
                                    String msg = myResultResponse.getMessage();
                                    if (msg.equalsIgnoreCase("SUCCESS")) {
                                        lstResult = myResultResponse.getResult();
                                        for(int i = 0;i< lstResult.size();i++) {
                                            if (lstResult.get(i).getRES() != null) {
                                                int res = lstResult.get(i).getRES();
                                                if (res != -1) {
                                                    showModAlert(ModeratorActivity.this, "Success","Verified :" +res, "OK");
                                                } else
                                                    showAlert(ModeratorActivity.this, "Alert", "Item already added ", "OK");
                                            }
                                        }
                                    } else
                                        Log.e("Tag", "error=" );
                                } else
                                    Log.e("Tag", "error=" );
                            } else
                                Log.e("Tag", "error=" );

                        } catch (Exception e) {
                            e.printStackTrace();
                            try {
                                Log.e("Tag", "error=" + e.toString());
                            } catch (Resources.NotFoundException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                    @Override
                    public void onFailure(Call<ResultModel> call, Throwable t) {
                        try {
                            Log.e("Tag", "error" + t.toString());
                            Toast.makeText(ModeratorActivity.this, "error :" +t.toString(), Toast.LENGTH_SHORT).show();

                        } catch (Resources.NotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                });

            }catch (Exception e)
            {
                e.printStackTrace();
                //FirebaseCrashlytics.getInstance().setCustomKey("ModeratorActivity", e.getMessage());
            }
        }

        public void showModAlert(Activity activity, String Title, String strMsg, String Positive)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            // Get the layout inflater
            LayoutInflater inflater = (activity).getLayoutInflater();
            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the
            // dialog layout
            builder.setTitle(Title);
            builder.setMessage(strMsg);
            builder.setCancelable(false);
            builder.setPositiveButton(Positive, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int i) {
                    dialog.dismiss();
                    new AsyncGetModeratorDetails().execute();
                }
            });
            builder.create();
            builder.show();
        }

        @Override
        public int getItemCount() {
            return dataSet.size();
        }


        private String getPostedDate(String postedDate)
        {
            if(postedDate.contains("T"))
            {
                String[] strDateZ = null;
                String date ="";

                String[] strPostedDate = postedDate.split("T");
                String strDate = strPostedDate[0] + " " + strPostedDate[1];
                if(strDate.contains("Z"))
                {
                    strDateZ = strDate.split("Z");
                    date = strDateZ[0];
                }
                try {
                    if(strDateZ != null  && !date.equalsIgnoreCase("")) {
                        SimpleDateFormat spf = new SimpleDateFormat("yyyy-MM-dd hh:mm:sss");
                        try {

                            Date oldDate = spf.parse(date);
                            System.out.println(oldDate);

                            int day = 0;
                            int hh = 0;
                            int mm = 0;
                            Date currentDate = new Date();
                            Long timeDiff = currentDate.getTime() - oldDate.getTime();
                            day = (int) TimeUnit.MILLISECONDS.toDays(timeDiff);
                            hh = (int) (TimeUnit.MILLISECONDS.toHours(timeDiff) - TimeUnit.DAYS.toHours(day));
                            mm = (int) (TimeUnit.MILLISECONDS.toMinutes(timeDiff) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(timeDiff)));


                            if (mm <= 60 && hh!= 0) {
                                //if (hh <= 60 && day != 0) {
                                if (hh <= 60 && day != 0) {
                                    if(day > 5){
                                        spf = new SimpleDateFormat("dd MMM yyyy");
                                        date = spf.format(oldDate);
                                        return date;
                                    }
                                    else
                                        return day + " Days";
                                } else {
                                    return hh + " Hrs";
                                }
                            } else {
                                return mm + " Min";
                            }
                        } catch (ParseException e) {

                            e.printStackTrace();
                        }

                    }
                    return date;
                }catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            return postedDate;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK)
        {
            showAlert(ModeratorActivity.this,"Exit Alert","Do you want to exit from the Application?",true,"Yes","No");
        }
        return true;
    }
}
