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

public class ForgotPasswordActivity extends AppCompatActivity {

    private PreferenceManager preferenceManager;
    TextInputEditText mobile;
    Button btnContinue;
    boolean isMobilenoValid;
    TextInputLayout emailError, passError;
    RestCall restCall;
    Tools tools;
    ImageView imgBack;
    private TextView tvresend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        preferenceManager = new PreferenceManager(this);
        tools = new Tools(this);
        imgBack = findViewById(R.id.ivBack);
        tvresend = findViewById(R.id.tvResend);
        mobile = findViewById(R.id.mobile);
        btnContinue = findViewById(R.id.btnContinue);
        emailError = findViewById(R.id.emailError);
        passError = findViewById(R.id.passError);
        Tools.setSystemBarColor(this, R.color.colorPrimary);

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SetValidation();
            }
        });
        tvresend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callNetwork();
            }
        });
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ForgotPasswordActivity.this,LoginActivity.class));
                finish();
            }
        });
    }

    public void SetValidation() {
        // Check for a valid mobile address.
        if (mobile.getText().toString().isEmpty()) {
            emailError.setError(getResources().getString(R.string.mobile_error));
            isMobilenoValid = false;
        } else if (!Patterns.PHONE.matcher(mobile.getText().toString()).matches()) {
            emailError.setError(getResources().getString(R.string.error_invalid_mobile));
            isMobilenoValid = false;
        } else {
            isMobilenoValid = true;
            emailError.setErrorEnabled(false);
        }

        if (isMobilenoValid /*&& isPasswordValid*/) {
            callNetwork();
        }


    }

    public void callNetwork() {
        restCall = RestClient.createService(RestCall.class, VariableBag.BASE_URL);
        tools.showLoading();
        restCall.forgotPassword("forgot_password", Objects.requireNonNull(mobile.getText()).toString())
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
                                Tools.toast(ForgotPasswordActivity.this, CommonResponce.getMessage(), 2);
                                Intent intent = new Intent(ForgotPasswordActivity.this,ChangePasswordActivity .class);
                                intent.putExtra("regitered_userid",CommonResponce.getForgot_id());
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();  }

                        });
                    }
                });
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent myIntent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
        myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);// clear back stack
        startActivity(myIntent);
        finish();
        return;
    }
}