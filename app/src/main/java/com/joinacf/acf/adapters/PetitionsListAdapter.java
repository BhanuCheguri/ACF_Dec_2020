package com.joinacf.acf.adapters;


import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.view.ContextThemeWrapper;

import com.joinacf.acf.R;
import com.joinacf.acf.activities.AddPetitionActivity;
import com.joinacf.acf.custom_dialogs.CustomProgressDialog;
import com.joinacf.acf.modelclasses.PetitionModel;
import com.joinacf.acf.modelclasses.SectionsModel;
import com.joinacf.acf.modelclasses.StatusModel;
import com.joinacf.acf.network.APIInterface;
import com.joinacf.acf.network.APIRetrofitClient;
import com.joinacf.acf.utilities.App;
import com.pd.chocobar.ChocoBar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class PetitionsListAdapter extends ArrayAdapter<PetitionModel.Result> {
    public ArrayList<PetitionModel.Result> dataSet;
    Activity context;
    String petitions;
    APIRetrofitClient apiRetrofitClient;
    ViewHolder holder = null; // view lookup cache stored in tag
    StatusModel myStatusResponse;
    ArrayList<StatusModel.Result> lstStatusData;
    String strVerifyStatus="-1";

    public PetitionsListAdapter(Activity context, ArrayList<PetitionModel.Result> data, String Petitions) {
        super(context, R.layout.custom_petition_list_items, data);
        this.dataSet = data;
        this.context = context;
        this.petitions = Petitions;
    }

    public class ViewHolder
    {
        TextView txtPetitionNo;
        TextView txtPetitionier;
        TextView txtStatus;
        TextView txtComplaint;
        TextView txtComplaintDate;
        TextView txtSPattachmentsNo;
        TextView txtAattachmentsNo;
        Button btnVerified;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {


        View rowView = convertView;
        if (convertView == null) {
            holder = new ViewHolder();
            PetitionModel.Result dataModel = (PetitionModel.Result)getItem(position);
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.custom_petition_list_items, null);

            holder.txtPetitionNo = (TextView) rowView.findViewById(R.id.tv_petitionNo);
            holder.txtPetitionier = (TextView) rowView.findViewById(R.id.tv_petitioner);
            holder.txtStatus = (TextView) rowView.findViewById(R.id.tv_status);
            holder.txtComplaint = (TextView) rowView.findViewById(R.id.tv_complaint);
            holder.txtComplaintDate = (TextView) rowView.findViewById(R.id.tv_complaintDate);
            holder.txtSPattachmentsNo = (TextView) rowView.findViewById(R.id.tv_SPattachmentsNo);
            holder.txtAattachmentsNo = (TextView) rowView.findViewById(R.id.tv_attachmentsNo);
            holder.btnVerified = (Button) rowView.findViewById(R.id.btnVerified);

            if(petitions.equalsIgnoreCase("MyPetitions")) {
                holder.txtPetitionier.setText("OTP : "+dataModel.getVerificationCode());
                holder.btnVerified.setVisibility(View.GONE);
                holder.txtSPattachmentsNo.setVisibility(View.GONE);
                holder.txtAattachmentsNo.setVisibility(View.VISIBLE);

                if(dataModel.getVerificationDate().equalsIgnoreCase("")) {
                    holder.txtStatus.setText(Html.fromHtml("<font color=\"#000000\">" + "Status : " + "</font>" + "<font color=\"#47a842\">" + "Pending"+ "</font>"));
                }
                else {
                    holder.txtStatus.setText(Html.fromHtml("<font color=\"#000000\">" + "Status : " + "Verified"));
                }
            }else if(petitions.equalsIgnoreCase("SPPetitions")) {
                holder.txtPetitionier.setText(dataModel.getFullName());
                holder.txtSPattachmentsNo.setVisibility(View.VISIBLE);
                holder.txtAattachmentsNo.setVisibility(View.GONE);

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
            holder.txtComplaint.setText(Html.fromHtml("<b>Title :</b> "+ dataModel.getTitle()));
            holder.txtComplaintDate.setText(finalDate);

        }




        return rowView;
    }

    private void showVerifyOTPDialog(final String petitionID)
    {
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_verify_otp);
        //dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        // set the custom dialog components - text, image and button
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
