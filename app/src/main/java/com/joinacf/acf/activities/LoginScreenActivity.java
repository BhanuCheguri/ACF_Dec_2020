package com.joinacf.acf.activities;

import android.content.Intent;
import androidx.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import com.joinacf.acf.R;
import com.joinacf.acf.databinding.ActivityLoginBinding;


public class LoginScreenActivity extends BaseActivity implements View.OnClickListener {

    ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_login);
         binding = DataBindingUtil.setContentView(this, R.layout.activity_login);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getWindow().getDecorView().setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS);
        }

        binding.tvForgotPswd.setOnClickListener(this);
        binding.tvSignup.setOnClickListener(this);
        binding.btnLogin.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        Intent intent = null;
        switch (view.getId())
        {
            case R.id.tv_Signup:

                intent = new Intent(LoginScreenActivity.this,RegistrationActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();

                break;
            case R.id.btnLogin:

                intent = new Intent(LoginScreenActivity.this,MainActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();

                break;
            case R.id.tv_forgot_pswd:
                intent = new Intent(LoginScreenActivity.this,ForgotPasswordActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
                break;
        }

    }
}
