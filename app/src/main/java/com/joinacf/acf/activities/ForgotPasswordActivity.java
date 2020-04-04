package com.joinacf.acf.activities;

import androidx.databinding.DataBindingUtil;

import android.os.Bundle;
import android.view.View;

import com.joinacf.acf.R;
import com.joinacf.acf.databinding.ActivityForgotPasswordBinding;

public class ForgotPasswordActivity extends BaseActivity implements View.OnClickListener {

    ActivityForgotPasswordBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setActionBarTitle(getString(R.string.title_forgot_pswd));

        binding = DataBindingUtil.setContentView(this, R.layout.activity_forgot_password);
        binding.btnSubmit.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
         switch (view.getId()) {
                case R.id.btnSubmit:
                    finish();
            }
    }
}
