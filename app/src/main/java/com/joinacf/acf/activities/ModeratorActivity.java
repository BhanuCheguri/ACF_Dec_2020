package com.joinacf.acf.activities;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
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
import android.widget.ListAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.joinacf.acf.R;
import com.joinacf.acf.databinding.ActivityModeratorBinding;
import com.joinacf.acf.modelclasses.ModeratorListModel;
import com.joinacf.acf.modelclasses.ModeratorStatusModel;
import com.joinacf.acf.modelclasses.MyPostingModel;
import com.joinacf.acf.modelclasses.ProviderModel;
import com.joinacf.acf.modelclasses.ResultModel;
import com.joinacf.acf.modelclasses.StatusModel;
import com.joinacf.acf.network.APIInterface;
import com.joinacf.acf.network.APIRetrofitClient;
import com.joinacf.acf.utilities.App;
import com.pd.chocobar.ChocoBar;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

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

        hshStatus.put("Pending", 6);
        hshStatus.put("Department –L1" , 2);
        hshStatus.put("Department –L2", 3);
        hshStatus.put("ACB",1);
        hshStatus.put("Police", 7);
        hshStatus.put("Health", 5);
        hshStatus.put("Expert/Legal",4);
        hshStatus.put("Publish", 8);


        for ( String key : hshStatus.keySet() ) {
            System.out.println( key );
            listStatus.add(key);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.mipmap.ic_launcher_icon);
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
            statusAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, lstStatus);/*{

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

            };*/
        }

        private void loadSPProvider() {
            providerAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, lstProvider);/*{

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

            };*/
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
                this.submit = (Button)rowView.findViewById(R.id.submit);
                this.imgFilePath.setBackgroundResource(R.drawable.rippleeffect);
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

            holder.spStatus.setAdapter(statusAdapter);
            holder.spProvider.setAdapter(providerAdapter);

            holder.spStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    status = position;
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            holder.spProvider.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    provider = position;
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });


            ArrayList<String> lstFilepaths = new ArrayList<>();
            String strFilePaths = dataModel.getFilePath();
            if(strFilePaths != null && !strFilePaths.equalsIgnoreCase(""))
            {
                String[] strFilepathArray = strFilePaths.split(",");
                if(strFilepathArray.length > 0) {
                    holder.imgFilePath.setVisibility(View.VISIBLE);
                    for (int i = 0; i < strFilepathArray.length; i++) {

                        System.out.println("Image URL ::: "+strFilepathArray[i].trim().toString());
                        String strFile = strFilepathArray[i].trim().toString();
                        lstFilepaths.add(strFilepathArray[i]);
                        final ImageView imageView = new ImageView(context);
                        imageView.setBackgroundResource(R.drawable.rippleeffect);
                        imageView.setId(i);
                        imageView.setContentDescription(strFilepathArray[i].trim().toString());
                        imageView.setPadding(5, 5, 5, 5);
                        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        //LinearLayout.MarginLayoutParams mlp = (LinearLayout.MarginLayoutParams)imageView.getLayoutParams();
                        //mlp.setMargins(5, 0, 5, 0);
                        Glide.with(context).load(strFile).override(500, 500).into(imageView);
                        holder.linearLayout.addView(imageView);
                        final ImageView img = holder.imgFilePath;
                        Glide.with(context).load(strFilepathArray[0].trim().toString()).into(img);
                        imageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String url = imageView.getContentDescription().toString();
                                Glide.with(context).load(url).into(img);
                            }
                        });
                    }
                }
                else
                    holder.imgFilePath.setVisibility(View.GONE);
            }else
                holder.imgFilePath.setVisibility(View.GONE);

            final String MemeberID = getStringSharedPreference(context,"MemberID");
            holder.submit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    postAddModeration(Integer.parseInt(dataModel.getItemID()),Integer.parseInt(MemeberID),status,provider);
                }
            });
        }

        private void postAddModeration(int itemID, int memeberID, int status, int provider) {

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
                                                    showAlert(ModeratorActivity.this, "Success","Verified :" +res, "OK");
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
            Crashlytics.logException(e);
        }
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
}
