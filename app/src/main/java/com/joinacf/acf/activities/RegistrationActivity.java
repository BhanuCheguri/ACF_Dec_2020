package com.joinacf.acf.activities;

import android.content.Intent;
import androidx.databinding.DataBindingUtil;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import com.joinacf.acf.R;
import com.joinacf.acf.databinding.ActivityRegistrationBinding;


public class RegistrationActivity extends BaseActivity {

    ActivityRegistrationBinding binding;
    GradientDrawable gradientdrawable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_ACTION_BAR);

        setActionBarTitle(getString(R.string.sign_up));

        binding = DataBindingUtil.setContentView(this, R.layout.activity_registration);

        binding.tvLoginHere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegistrationActivity.this, LoginScreenActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
            }
        });

        binding.btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegistrationActivity.this,OTPVerificationActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });
    }
}
