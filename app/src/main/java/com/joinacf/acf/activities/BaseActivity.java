package com.joinacf.acf.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;

import com.joinacf.acf.R;


public class BaseActivity extends AppCompatActivity {
    private static final String TAG = "BaseActivity";
    SharedPreferences pref;
    ProgressDialog dialog;

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
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setMessage(Msg);
        dialog.show();
    }

}