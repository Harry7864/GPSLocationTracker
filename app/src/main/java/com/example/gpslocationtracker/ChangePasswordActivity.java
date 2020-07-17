package com.example.gpslocationtracker;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gpslocationtracker.network.RestCall;
import com.example.gpslocationtracker.network.RestClient;
import com.example.gpslocationtracker.networkresponce.CommonResponce;
import com.example.gpslocationtracker.utility.PreferenceManager;
import com.example.gpslocationtracker.utility.Tools;
import com.example.gpslocationtracker.utility.VariableBag;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;

import java.util.Objects;

import rx.Subscriber;
import rx.schedulers.Schedulers;

public class ChangePasswordActivity extends AppCompatActivity {

    private PreferenceManager preferenceManager;
    TextInputEditText otp, password, conPassword;
    Button login;
    boolean isotpnoValid, isPasswordValid, isConfPasswordValid;
    TextInputLayout otpError, passError, confpassError;
    RestCall restCall;
    Tools tools;
    ImageView ivBack;
    private Intent intent;
    private String userId=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        preferenceManager = new PreferenceManager(this);
        tools = new Tools(this);
        ivBack = findViewById(R.id.ivBack);
        otp = findViewById(R.id.tvotp);

        password = findViewById(R.id.password);
        conPassword = findViewById(R.id.confirm);
        login = findViewById(R.id.login);
        otpError = findViewById(R.id.otpError);
        passError = findViewById(R.id.passError);
        confpassError = findViewById(R.id.confirmPassError);
        Tools.setSystemBarColor(this, R.color.colorPrimary);
        intent=getIntent();
        if(intent!=null)
        {
            userId=intent.getStringExtra("regitered_userid");
        }
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SetValidation();
            }
        });

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ChangePasswordActivity.this,ForgotPasswordActivity.class));
                finish();
            }
        });
    }

    public void SetValidation() {
        // Check for a valid otp address.
        if (otp.getText().toString().isEmpty()) {
            otpError.setError(getResources().getString(R.string.otp_error));
            isotpnoValid = false;
        } else if (!Patterns.PHONE.matcher(otp.getText().toString()).matches()) {
            otpError.setError(getResources().getString(R.string.error_invalid_otp));
            isotpnoValid = false;
        } else {
            isotpnoValid = true;
            otpError.setErrorEnabled(false);
        }

        if (password.getText().toString().isEmpty()) {
            passError.setError(getResources().getString(R.string.password_error));
            isPasswordValid = false;
        } else if (password.getText().length() < 5) {
            passError.setError(getResources().getString(R.string.error_invalid_pass));
            isPasswordValid = false;
        } else {
            isPasswordValid = true;
            passError.setErrorEnabled(false);
        }

        if (conPassword.getText().toString().isEmpty()) {
            confpassError.setError(getResources().getString(R.string.password_con_error));
            isConfPasswordValid = false;
        } else if (!password.getText().toString().equalsIgnoreCase(conPassword.getText().toString())) {
            confpassError.setError(getResources().getString(R.string.error_invalid_confirmpass));
            isConfPasswordValid = false;
        } else {
            isConfPasswordValid = true;
            confpassError.setErrorEnabled(false);
        }

        if (isotpnoValid && isPasswordValid && isConfPasswordValid) {
            callNetwork();
        }


    }

    public void callNetwork() {

        restCall = RestClient.createService(RestCall.class, VariableBag.BASE_URL);
        tools.showLoading();
        restCall.ChangePassword("password2",userId, Objects.requireNonNull(otp.getText()).toString(), password.getText().toString().trim(),conPassword.getText().toString().toLowerCase())
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.newThread())
                .subscribe(new Subscriber<CommonResponce>() {
                    @Override
                    public void onCompleted() {
                        tools.stopLoading();
                    }

                    @Override
                    public void onError(Throwable e) {
                        tools.stopLoading();
                        Log.e("##", Objects.requireNonNull(e.getMessage()));
                    }

                    @Override

                    public void onNext(final CommonResponce CommonResponce) {
                        tools.stopLoading();
                        runOnUiThread(() -> {
                            new Gson().toJson(CommonResponce);
                            if (CommonResponce != null && CommonResponce.getSuccess() != null && CommonResponce.getSuccess().equals(VariableBag.SUCCESS)) {
                                Tools.toast(ChangePasswordActivity.this, CommonResponce.getMessage(), 2);

                                Intent intent = new Intent(ChangePasswordActivity.this, LoginActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            } else {
                                if (CommonResponce != null) {
                                    Tools.toast(ChangePasswordActivity.this, CommonResponce.getMessage(), 1);
                                }
                            }

                        });
                    }
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent myIntent = new Intent(ChangePasswordActivity.this, ForgotPasswordActivity.class);
        myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);// clear back stack
        startActivity(myIntent);
        finish();
        return;
    }
}