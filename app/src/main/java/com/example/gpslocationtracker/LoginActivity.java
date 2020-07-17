package com.example.gpslocationtracker;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gpslocationtracker.network.RestCall;
import com.example.gpslocationtracker.network.RestClient;
import com.example.gpslocationtracker.networkresponce.LoginResponce;
import com.example.gpslocationtracker.utility.PreferenceManager;
import com.example.gpslocationtracker.utility.Tools;
import com.example.gpslocationtracker.utility.VariableBag;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;

import java.util.Objects;

import rx.Subscriber;
import rx.schedulers.Schedulers;

public class LoginActivity extends AppCompatActivity {

    TextInputEditText mobile;
    EditText password;
    Button login;
    boolean isMobilenoValid, isPasswordValid;
    TextInputLayout emailError, passError;
    RestCall restCall;
    Tools tools;
    PreferenceManager preferenceManager;
    private String tokenStr = null;
    String model = Build.MODEL;
    String brand = Build.BRAND;
    TextView tvforgotPass;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        preferenceManager=new PreferenceManager(this);
        tools=new Tools(this);
        mobile = findViewById(R.id.mobile);
        password = findViewById(R.id.password);
        login = findViewById(R.id.login);
        tvforgotPass=findViewById(R.id.tvForgotPassword);
        emailError = findViewById(R.id.emailError);
        passError = findViewById(R.id.passError);
        Tools.setSystemBarColor(this, R.color.colorPrimary);
        tvforgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,ForgotPasswordActivity.class));
                finish();
            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SetValidation();
            }
        });
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.e("firebase", "getInstanceId failed", task.getException());
                        return;
                    }
                    tokenStr = Objects.requireNonNull(task.getResult()).getToken();

                    Log.e("firebase", "" + tokenStr);

                    preferenceManager.setKeyValueString("token", tokenStr);

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

        // Check for a valid Stpassword.
        if (password.getText().toString().isEmpty()) {
            passError.setError(getResources().getString(R.string.password_error));
            isPasswordValid = false;
        } else if (password.getText().length() < 5) {
            passError.setError(getResources().getString(R.string.error_invalid_password));
            isPasswordValid = false;
        } else {
            isPasswordValid = true;
            passError.setErrorEnabled(false);
        }

        if (isMobilenoValid /*&& isPasswordValid*/) {
            callNetwork();
        }


    }

    public void callNetwork() {

        restCall = RestClient.createService(RestCall.class, VariableBag.BASE_URL);
        tools.showLoading();
        restCall.LoginCheck("login_action", Objects.requireNonNull(mobile.getText()).toString(), password.getText().toString(),"android",brand,model,tokenStr)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.newThread())
                .subscribe(new Subscriber<LoginResponce>() {
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

                    public void onNext(final LoginResponce loginResponce) {
                        tools.stopLoading();
                        runOnUiThread(() -> {
                            new Gson().toJson(loginResponce);
                            if (loginResponce != null && loginResponce.getSuccess() != null && loginResponce.getSuccess().equals(VariableBag.SUCCESS)) {


                                preferenceManager.setLoginSession();
                                preferenceManager.setRegistredUSerID(loginResponce.getUser_id());

                                preferenceManager.setKeyValueString("fullname", loginResponce.getUser_name());
                                preferenceManager.setKeyValueString("email", loginResponce.getUser_email());
                                preferenceManager.setKeyValueString("mobile", loginResponce.getUser_mobile());
                                preferenceManager.setKeyValueString("roleid", loginResponce.getRole_id());
                                preferenceManager.setKeyValueString("user_profile_pick", loginResponce.getUser_profile());
                                preferenceManager.setKeyValueString("LocationTimeInterval", "1000");

                                Tools.toast(LoginActivity.this, loginResponce.getMessage(), 2);

                                Intent intent = new Intent(LoginActivity.this,InOutActivity .class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            } else {
                                if (loginResponce != null) {
                                    Tools.toast(LoginActivity.this, loginResponce.getMessage(), 1);
                                }
                            }

                        });
                    }
                });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exitByApplication();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void exitByApplication() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton(Html.fromHtml("<font color='#000000'>Yes</font>"), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        LoginActivity.this.finish();
                    }
                })
                .setNegativeButton(Html.fromHtml("<font color='#000000'>No</font>"), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

}