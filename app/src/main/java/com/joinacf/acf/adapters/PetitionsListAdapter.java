package com.joinacf.acf.adapters;


import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.joinacf.acf.R;
import com.joinacf.acf.activities.AddPetitionActivity;
import com.joinacf.acf.custom_dialogs.CustomProgressDialog;
import com.joinacf.acf.modelclasses.PetitionModel;
import com.joinacf.acf.modelclasses.SectionsModel;
import com.joinacf.acf.modelclasses.StatusModel;
import com.joinacf.acf.modelclasses.WallPostsModel;
import com.joinacf.acf.network.APIInterface;
import com.joinacf.acf.network.APIRetrofitClient;
import com.joinacf.acf.utilities.App;
import com.pd.chocobar.ChocoBar;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static com.joinacf.acf.utilities.DataUtilities.openFile;

public class PetitionsListAdapter extends RecyclerView.Adapter<PetitionsListAdapter.ViewHolder> implements Filterable, View.OnClickListener{
    public ArrayList<PetitionModel.Result> dataSet;
    public ArrayList<PetitionModel.Result> filteredList;
    ArrayList<String> lstImages;
    Activity context;
    String petitions;
    APIRetrofitClient apiRetrofitClient;
    StatusModel myStatusResponse;
    ArrayList<StatusModel.Result> lstStatusData;
    String strVerifyStatus="-1";

    public PetitionsListAdapter(Activity context, ArrayList<PetitionModel.Result> data, String Petitions) {
        //super(context, R.layout.custom_petition_list_items, data);
        this.dataSet = data;
        this.filteredList = data;
        this.context = context;
        this.petitions = Petitions;
        lstImages = new ArrayList<>();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    filteredList = dataSet;
                } else {
                    ArrayList<PetitionModel.Result> list = new ArrayList<>();
                    for (PetitionModel.Result petitionResult : dataSet) {
                        if (petitionResult.getTitle().toLowerCase().contains(charString)) {
                            list.add(petitionResult);
                        }
                    }
                    filteredList = list;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                filteredList = (ArrayList<PetitionModel.Result>) filterResults.values;
                PetitionsListAdapter.this.notifyDataSetChanged();
            }
        };
    }

    @NonNull
    @Override
    public PetitionsListAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = null;
        if(petitions.equalsIgnoreCase("MyPetitions")){
        itemView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.custom_my_petition_list_items, viewGroup, false);
        }else if(petitions.equalsIgnoreCase("SPPetitions")) {
            itemView = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.custom_sp_petition_list_items, viewGroup, false);
        }
        return new ViewHolder(itemView);
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        TextView txtPetitionNo;
        TextView txtPetitionier;
        TextView txtStatus;
        TextView txtComplaint;
        TextView txtComplaintDate;
        TextView txtSPattachmentsNo;
        TextView txtAattachmentsNo;
        Button btnVerified;
        ImageView imageView1;
        ImageView imageView2;
        ImageView imageView3;
        ImageView imageView4;
        ImageView imageView5;

        public ViewHolder(View rowView) {
            super(rowView);

            txtPetitionNo = (TextView) rowView.findViewById(R.id.tv_petitionNo);
            txtPetitionier = (TextView) rowView.findViewById(R.id.tv_petitioner);
            txtStatus = (TextView) rowView.findViewById(R.id.tv_status);
            txtComplaint = (TextView) rowView.findViewById(R.id.tv_complaint);
            txtComplaintDate = (TextView) rowView.findViewById(R.id.tv_complaintDate);
            txtSPattachmentsNo = (TextView) rowView.findViewById(R.id.tv_SPattachmentsNo);
            txtAattachmentsNo = (TextView) rowView.findViewById(R.id.tv_attachmentsNo);
            btnVerified = (Button) rowView.findViewById(R.id.btnVerified);
            imageView1 = (ImageView) rowView.findViewById(R.id.imageView1);
            imageView2 = (ImageView) rowView.findViewById(R.id.imageView2);
            imageView3 = (ImageView) rowView.findViewById(R.id.imageView3);
            imageView4 = (ImageView) rowView.findViewById(R.id.imageView4);
            imageView5 = (ImageView) rowView.findViewById(R.id.imageView5);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        final PetitionModel.Result dataModel = filteredList.get(position);

        if(petitions.equalsIgnoreCase("MyPetitions")) {
            holder.txtPetitionier.setText("OTP : "+ dataModel.getVerificationCode());
            if(dataModel.getVerificationDate().equalsIgnoreCase("")) {
                holder.txtStatus.setText(Html.fromHtml("<font color=\"#000000\">" + "Status : " + "</font>" + "<font color=\"#47a842\">" + "Pending"+ "</font>"));
            }
            else {
                holder.txtStatus.setText(Html.fromHtml("<font color=\"#000000\">" + "Status : " + "Verified"));
            }
            if(!(dataModel.getImage1()).equalsIgnoreCase("")|| !ImagesExists(dataModel.getImage1()))
                Glide.with(context).load(R.drawable.logo).into(holder.imageView1);
            else
                Glide.with(context).load(dataModel.getImage1()).into(holder.imageView1);
            if(!(dataModel.getImage2()).equalsIgnoreCase("")|| !ImagesExists(dataModel.getImage2()))
                Glide.with(context).load(R.drawable.logo).into(holder.imageView2);
            else
                Glide.with(context).load(dataModel.getImage2()).into(holder.imageView2);
            if(!(dataModel.getImage3()).equalsIgnoreCase("")|| !ImagesExists(dataModel.getImage3()))
                Glide.with(context).load(R.drawable.logo).into(holder.imageView3);
            else
                Glide.with(context).load(dataModel.getImage3()).into(holder.imageView3);
            if(!(dataModel.getImage4()).equalsIgnoreCase("")|| !ImagesExists(dataModel.getImage4()))
                Glide.with(context).load(R.drawable.logo).into(holder.imageView4);
            else
                Glide.with(context).load(dataModel.getImage4()).into(holder.imageView4);
            if(!(dataModel.getImage5()).equalsIgnoreCase("")|| !ImagesExists(dataModel.getImage5()))
                Glide.with(context).load(R.drawable.logo).into(holder.imageView5);
            else
                Glide.with(context).load(dataModel.getImage5()).into(holder.imageView5);

            holder.imageView1.setContentDescription(dataModel.getImage1());
            holder.imageView2.setContentDescription(dataModel.getImage2());
            holder.imageView3.setContentDescription(dataModel.getImage3());
            holder.imageView4.setContentDescription(dataModel.getImage4());
            holder.imageView5.setContentDescription(dataModel.getImage5());

            holder.imageView1.setOnClickListener(this);
            holder.imageView2.setOnClickListener(this);
            holder.imageView3.setOnClickListener(this);
            holder.imageView4.setOnClickListener(this);
            holder.imageView5.setOnClickListener(this);

        }else if(petitions.equalsIgnoreCase("SPPetitions")) {
            holder.txtPetitionier.setText(dataModel.getFullName());
            holder.txtSPattachmentsNo.setVisibility(View.VISIBLE);
            if(dataModel.getVerificationDate().equalsIgnoreCase("")) {
                //holder.txtStatus.setText(Html.fromHtml("<font color=\"#000000\">" + "Status : " + "</font>" + "<font color=\"#47a842\">" + "Pending"+ "</font>"));
                holder.btnVerified.setVisibility(View.VISIBLE);
            }
            else {
                holder.txtStatus.setText(Html.fromHtml("<font color=\"#000000\">" + "Status : " + "Verified"));
            }

            holder.btnVerified.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showVerifyOTPDialog(holder.txtPetitionNo.getText().toString());
                }
            });
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        Date myDate = null;
        try {
            myDate = dateFormat.parse(dataModel.getCreatedDate());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd  HH:mm");
        String finalDate = timeFormat.format(myDate);
        System.out.println(finalDate);

        holder.txtPetitionNo.setText(dataModel.getPetitionID());
        holder.txtComplaint.setText(Html.fromHtml(dataModel.getTitle()));
        holder.txtComplaintDate.setText(finalDate);
    }

    public static boolean ImagesExists(String URLName){
        try {
            HttpURLConnection.setFollowRedirects(false);
            // note : you may also need
            //        HttpURLConnection.setInstanceFollowRedirects(false)
            HttpURLConnection con =
                    (HttpURLConnection) new URL(URLName).openConnection();
            con.setRequestMethod("HEAD");
            return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    @Override
    public void onClick(View v) {
        String url  = v.getContentDescription().toString();
        switch (v.getId()){
            case R.id.imageView1:
            case R.id.imageView2:
            case R.id.imageView3:
            case R.id.imageView4:
            case R.id.imageView5:
                openFile(Uri.parse(url),url,context);
                break;
        }
    }



    private void showVerifyOTPDialog(final String petitionID)
    {
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_verify_otp);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        final EditText etEnterOtp = (EditText) dialog.findViewById(R.id.etEnterOtp);
        final LinearLayout linearLayout = (LinearLayout) dialog.findViewById(R.id.linearLayout);

        Button btnVerify = (Button) dialog.findViewById(R.id.btnVerify);
        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if(!etEnterOtp.getText().toString().equalsIgnoreCase("")) {
                    if (App.isNetworkAvailable())
                        new AsyncVerifyOTP().execute(petitionID,etEnterOtp.getText().toString());
                    else {
                        ChocoBar.builder().setView(linearLayout)
                                .setText("No Internet connection")
                                .setDuration(ChocoBar.LENGTH_INDEFINITE)
                                //.setActionText(android.R.string.ok)
                                .red()   // in built red ChocoBar
                                .show();
                    }
                }
            }
        });

        dialog.show();
    }


    public class AsyncVerifyOTP extends AsyncTask<String, String, String> {

        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB){
                dialog = new ProgressDialog(new ContextThemeWrapper(context, android.R.style.Theme_Holo_Light_Dialog));
            }else{
                dialog = new ProgressDialog(context);
            }
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(false);
            dialog.setMessage("Please wait.. Verifying your OTP");
            dialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            String result = getVerifyOTP(strings[0],strings[1],context);
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            dialog.dismiss();
        }
    }

    private String getVerifyOTP(String petitionID, String strOTP, final Activity context) {
        apiRetrofitClient = new APIRetrofitClient();
        Retrofit retrofit = apiRetrofitClient.getRetrofit(APIInterface.BASE_URL);
        APIInterface api = retrofit.create(APIInterface.class);
        Call<StatusModel> call = api.getVerifyOTP("2","402416");
        call.enqueue(new Callback<StatusModel>() {
            @Override
            public void onResponse(Call<StatusModel> call, Response<StatusModel> response) {
                if (response != null) {
                    myStatusResponse = response.body();
                    if (myStatusResponse != null) {
                        String msg = myStatusResponse.getMessage();
                        if (msg.equalsIgnoreCase("SUCCESS")) {
                            lstStatusData = myStatusResponse.getResult();
                            for (int i =0 ; i < lstStatusData.size(); i++){
                                StatusModel.Result statusModel = lstStatusData.get(i);
                                if(statusModel.getSTATUS() == 1){
                                    strVerifyStatus = "1";
                                    Toast.makeText(context, "OTP Verified", Toast.LENGTH_SHORT).show();
                                    /*if(strVerifyStatus.equalsIgnoreCase("1"))
                                    {
                                        holder.btnVerified.setVisibility(View.GONE);
                                        holder.txtStatus.setText(Html.fromHtml("<font color=\"#000000\">" + "Status : " + "Verified"));
                                    }*/
                                }else
                                    strVerifyStatus= "0";
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<StatusModel> call, Throwable t) {
                Toast.makeText(context, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        return strVerifyStatus;
    }
}
