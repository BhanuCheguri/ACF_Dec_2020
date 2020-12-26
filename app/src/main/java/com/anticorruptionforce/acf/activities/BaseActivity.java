package com.anticorruptionforce.acf.activities;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;

import com.anticorruptionforce.acf.R;


public class BaseActivity extends AppCompatActivity {
    private static final String TAG = "BaseActivity";
    SharedPreferences pref;
    ProgressDialog dialog;
    Dialog customDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, this.getClass().toString());
        pref = getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
    }


    public void showToast(String strMessage)
    {
        //Toast.makeText(OTPVerificationActivity.this, strMessage, Toast.LENGTH_LONG).show();
    }

    public void setActionBarTitle(String strTitle)
    {
        getSupportActionBar().setTitle(strTitle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.gradient_theme));
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getWindow().getDecorView().setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS);
        }
    }

    public boolean getBooleanSharedPreference(Activity activity,String key){
        return pref.getBoolean(key, false);
    }

    public void putBooleanSharedPreference(Activity activity,String key,boolean value)
    {
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(key,value); // Storing boolean - true/false
        editor.commit(); // commit changes
    }

    public String getStringSharedPreference(Activity activity,String key){
        return pref.getString(key, "");
    }

    public void putStringSharedPreference(Activity activity,String key,String value)
    {
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(key,value); // Storing boolean - true/false
        editor.commit(); // commit changes
    }



    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showErrorAlert(Activity activity, String strMsg)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        // Get the layout inflater
        LayoutInflater inflater = (activity).getLayoutInflater();
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the
        // dialog layout
        builder.setTitle("Alert");
        builder.setMessage(strMsg);
        builder.setCancelable(false);
        builder.setIcon(R.drawable.alert);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.create();
        builder.show();
    }

    public void showAlert(Activity activity, String Title, String strMsg, final boolean bExit, String Positive, String Negative)
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
                if (bExit)
                    finish();
            }
        });

        builder.setNegativeButton(Negative, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create();
        builder.show();
    }

    public void showAlert(Activity activity, String Title,String strMsg,String Positive)
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
            }
        });
        builder.create();
        builder.show();
    }

    public void showProgressDialog(Activity activity)
    {
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB){
            dialog = new ProgressDialog(new ContextThemeWrapper(activity, android.R.style.Theme_Holo_Light_Dialog));
        }else{
            dialog = new ProgressDialog(activity);
        }
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setMessage("Loading... Please wait");
        dialog.show();
    }

    public void hideProgressDialog(Activity activity)
    {
        if(dialog.isShowing())
            dialog.dismiss();
    }

    public void showProgressDialog(Activity activity,String Msg)
    {
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB){
            dialog = new ProgressDialog(new ContextThemeWrapper(activity, android.R.style.Theme_Holo_Light_Dialog));
        }else{
            dialog = new ProgressDialog(activity);
        }
        dialog.setIcon(R.mipmap.ic_dataprocessing);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setMessage(Msg);
        dialog.show();
    }


    public void hideCustomProgressDialog(Activity activity)
    {
        if(customDialog.isShowing())
            customDialog.dismiss();
    }

    public void showCustomProgressDialog(Activity activity,String strText,int res)
    {
        customDialog = new Dialog(activity);
        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        customDialog.setContentView(R.layout.dialog_processdats_layout);
        TextView txtdata = (TextView)customDialog.findViewById(R.id.tvData);
        ImageView Imgdata = (ImageView)customDialog.findViewById(R.id.imgdata);
        Imgdata.setImageResource(res);
        txtdata.setText(strText);
        customDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        //customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        customDialog.setCancelable(false);
        customDialog.show();
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