package com.anticorruptionforce.acf.adapters;


import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
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

import com.anticorruptionforce.acf.R;
import com.anticorruptionforce.acf.activities.BaseActivity;
import com.anticorruptionforce.acf.activities.ServiceProviderActivity;
import com.anticorruptionforce.acf.activities.ShowAttachmentsGrid;
import com.anticorruptionforce.acf.modelclasses.PetitionModel;
import com.anticorruptionforce.acf.modelclasses.StatusModel;
import com.anticorruptionforce.acf.network.APIInterface;
import com.anticorruptionforce.acf.network.APIRetrofitClient;
import com.anticorruptionforce.acf.utilities.App;
import com.anticorruptionforce.acf.utilities.Utils;
import com.bumptech.glide.Glide;
import com.pd.chocobar.ChocoBar;

import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static com.anticorruptionforce.acf.utilities.DataUtilities.openFile;

public class SPPetitionsListAdapter extends RecyclerView.Adapter<SPPetitionsListAdapter.ViewHolder> implements Filterable{
    public ArrayList<PetitionModel.Result> dataSet;
    public ArrayList<PetitionModel.Result> filteredList;
    ArrayList<String> lstImages;
    Activity context;
    String petitions;
    APIRetrofitClient apiRetrofitClient;
    StatusModel myStatusResponse;
    ArrayList<StatusModel.Result> lstStatusData;
    String strVerifyStatus="-1";

    public SPPetitionsListAdapter(Activity context, ArrayList<PetitionModel.Result> data) {
        //super(context, R.layout.custom_petition_list_items, data);
        this.dataSet = data;
        this.filteredList = data;
        this.context = context;
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
                        if (petitionResult.getTitle().contains(charString)) {
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
                SPPetitionsListAdapter.this.notifyDataSetChanged();
            }
        };
    }

    @NonNull
    @Override
    public SPPetitionsListAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(context)
                    .inflate(R.layout.custom_sp_petition_list_items, viewGroup, false);
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
        public ViewHolder(View rowView) {
            super(rowView);

            txtPetitionNo = (TextView) rowView.findViewById(R.id.tv_petitionNo);
            txtPetitionier = (TextView) rowView.findViewById(R.id.tv_petitioner);
            txtComplaint = (TextView) rowView.findViewById(R.id.tv_complaint);
            txtComplaintDate = (TextView) rowView.findViewById(R.id.tv_complaintDate);
            txtSPattachmentsNo = (TextView) rowView.findViewById(R.id.tv_SPattachmentsNo);
            txtAattachmentsNo = (TextView) rowView.findViewById(R.id.tv_attachmentsNo);
            txtStatus = (TextView) rowView.findViewById(R.id.tv_status);
            btnVerified = (Button) rowView.findViewById(R.id.btnVerified);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        final PetitionModel.Result dataModel = filteredList.get(position);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        Date myDate = null;
        try {
            myDate = dateFormat.parse(dataModel.getCreatedDate());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SimpleDateFormat timeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        String finalDate = timeFormat.format(myDate);
        System.out.println(finalDate);

        holder.txtPetitionier.setText("By " + dataModel.getFullName());
        System.out.println("By " + dataModel.getFullName());
        holder.txtPetitionNo.setText(dataModel.getPetitionID());
        holder.txtComplaint.setText(Html.fromHtml(dataModel.getTitle()));
        holder.txtComplaintDate.setText(finalDate);
        if(dataModel.getVerificationDate().equalsIgnoreCase("")) {
            holder.txtStatus.setText("Status: Not Verified");
            holder.btnVerified.setVisibility(View.VISIBLE);
            holder.txtAattachmentsNo.setVisibility(View.GONE);
            holder.txtSPattachmentsNo.setVisibility(View.VISIBLE);
        }
        else {
            holder.txtStatus.setText("Status: \nVerified on " + dataModel.getVerificationDate());
            holder.btnVerified.setVisibility(View.GONE);
            holder.txtAattachmentsNo.setVisibility(View.VISIBLE);
            holder.txtSPattachmentsNo.setVisibility(View.GONE);
        }


        holder.btnVerified.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showVerifyOTPDialog(dataModel.getPetitionID());
            }
        });

        lstImages = new ArrayList<>();

        if(!dataModel.getImage1().equalsIgnoreCase("") && !dataModel.getImage1().equalsIgnoreCase("null") && dataModel.getImage1() != null)
            lstImages.add(dataModel.getImage1());
        if(!dataModel.getImage2().equalsIgnoreCase("") && !dataModel.getImage2().equalsIgnoreCase("null") && dataModel.getImage2() != null)
            lstImages.add(dataModel.getImage2());
        if(!dataModel.getImage3().equalsIgnoreCase("") && !dataModel.getImage3().equalsIgnoreCase("null") && dataModel.getImage3() != null)
            lstImages.add(dataModel.getImage3());
        if(!dataModel.getImage4().equalsIgnoreCase("") && !dataModel.getImage4().equalsIgnoreCase("null") && dataModel.getImage4() != null)
            lstImages.add(dataModel.getImage4());
        if(!dataModel.getImage5().equalsIgnoreCase("") && !dataModel.getImage5().equalsIgnoreCase("null") && dataModel.getImage5() != null)
            lstImages.add(dataModel.getImage5());
        if(!dataModel.getImage5().equalsIgnoreCase("") && !dataModel.getImage6().equalsIgnoreCase("null") && dataModel.getImage6() != null)
            lstImages.add(dataModel.getImage6());

        holder.txtSPattachmentsNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(lstImages.size() > 0) {
                    Intent i = new Intent(context, ShowAttachmentsGrid.class);
                    i.putExtra("Images", lstImages);
                    i.putExtra("Title", dataModel.getTitle());
                    context.startActivity(i);
                }else
                    Toast.makeText(context, "No Images attached to this petition", Toast.LENGTH_SHORT).show();
            }
        });

        holder.txtAattachmentsNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(lstImages.size() > 0) {
                    Intent i = new Intent(context, ShowAttachmentsGrid.class);
                    i.putExtra("Images", lstImages);
                    i.putExtra("Title", dataModel.getTitle());
                    context.startActivity(i);
                }else
                    Toast.makeText(context, "No Images attached to this petition", Toast.LENGTH_SHORT).show();

            }
        });
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
                if(!etEnterOtp.getText().toString().equalsIgnoreCase("")) {
                    if(etEnterOtp.getText().toString().length() == 6) {
                        dialog.dismiss();
                        etEnterOtp.setError(null);
                        if (App.isNetworkAvailable())
                            new AsyncVerifyOTP().execute(petitionID, etEnterOtp.getText().toString());
                        else {
                            ChocoBar.builder().setView(linearLayout)
                                    .setText("No Internet connection")
                                    .setDuration(ChocoBar.LENGTH_INDEFINITE)
                                    //.setActionText(android.R.string.ok)
                                    .red()   // in built red ChocoBar
                                    .show();
                        }
                    }else
                        etEnterOtp.setError("Please enter your 6 digit Verification Code");
                }else {
                    Toast.makeText(context, "Please enter Verification Code", Toast.LENGTH_SHORT).show();
                    etEnterOtp.setError("Please enter Verification Code");
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
        Call<StatusModel> call = api.getVerifyOTP(petitionID,strOTP);
        call.enqueue(new Callback<StatusModel>() {
            @Override
            public void onResponse(Call<StatusModel> call, Response<StatusModel> response) {
                System.out.println("getVerifyOTP::"+ response);
                if (response != null) {
                    myStatusResponse = response.body();
                    if (myStatusResponse != null) {
                        String msg = myStatusResponse.getMessage();
                        if (msg.equalsIgnoreCase("SUCCESS")) {
                            lstStatusData = myStatusResponse.getResult();
                            for (int i =0 ; i < lstStatusData.size(); i++){
                                StatusModel.Result statusModel = lstStatusData.get(i);
                                if(statusModel.getSTATUS() == 1){
                                    //strVerifyStatus = "1";
                                    //Toast.makeText(context, "OTP Verified Successfully", Toast.LENGTH_SHORT).show();
                                    Utils.showOKAlert(context,"OTP Verified Successfully");
                                    SharedPreferences pref = context.getSharedPreferences("MyPref", 0); // 0 - for private mode
                                    String strSectionID = pref.getString("SectionID", "");
                                    String strSPID = pref.getString("SPID", "");
                                    if(App.isNetworkAvailable())
                                        ((ServiceProviderActivity)context).getSPPetitions(strSPID, strSectionID);
                                    else{
                                        Toast.makeText(context, "No Internet Connection", Toast.LENGTH_SHORT).show();
                                    }
                                }else
                                    Utils.showErrorAlert(context,"Error in OTP Verification");

                                //Toast.makeText(context, "OTP Verified " + myStatusResponse.getMessage(), Toast.LENGTH_SHORT).show();
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
