package com.anticorruptionforce.acf.custom_dialogs;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import com.anticorruptionforce.acf.R;


public class CustomProgressDialog extends Dialog {

    public CustomProgressDialog(Context context) {
        super(context);

        WindowManager.LayoutParams wlmp = getWindow().getAttributes();

        wlmp.gravity = Gravity.CENTER_HORIZONTAL;
        getWindow().setAttributes(wlmp);
        setTitle(null);
        setCancelable(false);
        setOnCancelListener(null);
        View view = LayoutInflater.from(context).inflate(
                R.layout.dialog_custom_loading, null);
        setContentView(view);
    }
}