package com.anticorruptionforce.acf.activities;

import android.os.Bundle;

import com.anticorruptionforce.acf.R;


public class ChangePasswordActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        setActionBarTitle("Change Password");

    }
}
