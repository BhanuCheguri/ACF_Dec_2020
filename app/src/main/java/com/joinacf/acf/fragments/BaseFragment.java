package com.joinacf.acf.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;

import com.joinacf.acf.R;


public class BaseFragment extends Fragment {
    SharedPreferences pref;
    ProgressDialog dialog;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pref = getContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //setUp(view);
    }

    public void setActionBarTitle(String strTitle)
    {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(strTitle);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.gradient_theme));
        ((AppCompatActivity) getActivity()).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ((AppCompatActivity) getActivity()).getWindow().getDecorView().setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS);
        }
        setHasOptionsMenu(true);
    }


    public boolean getBooleanSharedPreference(Activity activity, String key){
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

    public void putStringSharedPreference(Fragment activity,String key,String value)
    {
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(key,value); // Storing boolean - true/false
        editor.commit(); // commit changes
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

}