package com.example.gpslocationtracker;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.gpslocationtracker.network.RestCall;
import com.example.gpslocationtracker.network.RestClient;
import com.example.gpslocationtracker.networkresponce.CommonResponce;
import com.example.gpslocationtracker.service.GoogleService;
import com.example.gpslocationtracker.utility.PreferenceManager;
import com.example.gpslocationtracker.utility.Tools;
import com.example.gpslocationtracker.utility.VariableBag;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;
import com.patloew.rxlocation.RxLocation;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import rx.Subscriber;
import rx.schedulers.Schedulers;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

public class MainActivity extends AppCompatActivity implements MainView {

    private static final DateFormat DATE_FORMAT = DateFormat.getDateTimeInstance();
    private static final int REQUEST_PERMISSIONS = 100;

    private TextView lastUpdate;
    private TextView locationText;
    private TextView addressText;
    String area=null,locality;
    RestCall restCall;
    Tools tools;
    Button btnIn,btnOut;
    ImageView ivLogout;
    PreferenceManager preferenceManager;
    private RxLocation rxLocation;

    private MainPresenter presenter;
    private double locLatitude,locLongitude;
    private String fullAddress=null;
    private boolean boolean_permission;
    SharedPreferences mPref;
    SharedPreferences.Editor medit;
    GoogleService mService;
    boolean mBound = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferenceManager=new PreferenceManager(this);
        tools=new Tools(this);

        mPref = getDefaultSharedPreferences(getApplicationContext());
        medit = mPref.edit();

        lastUpdate = findViewById(R.id.tv_last_update);
        locationText = findViewById(R.id.tv_current_location);
        addressText = findViewById(R.id.tv_current_address);
        btnIn=findViewById(R.id.btnIn);
        btnOut=findViewById(R.id.btnOut);
        ivLogout=findViewById(R.id.ivLogout);

        rxLocation = new RxLocation(this);
        rxLocation.setDefaultTimeout(25, TimeUnit.SECONDS);
        presenter = new MainPresenter(rxLocation);
        fn_permission();
        btnIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (boolean_permission) {
                    initService();
                } else {
                    Toast.makeText(getApplicationContext(), "Please enable the gps", Toast.LENGTH_SHORT).show();
                }

//                Tools.toast(MainActivity.this, "GPS Start", 2);
//                presenter.attachView(MainActivity.this);
            }
        });
        btnOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tools.toast(MainActivity.this, "GPS stop", 2);
//               presenter.detachView();
            }
        });
        ivLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("Are you sure you want to logout?")
                        .setCancelable(false)
                        .setPositiveButton(Html.fromHtml("<font color='#000000'>Yes</font>"), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                preferenceManager.clearPrefrence();
                                preferenceManager.setLoginSessionFalse();
                                Intent intent = new Intent(MainActivity.this,LoginActivity .class);
                                startActivity(intent);
                                finish();
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
        });
    }

    private void initService() {
        if (mBound) {
            mService.getLocation();
            presenter.attachView(this);
        }
    }




    private void fn_permission() {
        if ((ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {

            if ((ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION))) {


            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION

                        },
                        REQUEST_PERMISSIONS);

            }
        } else {
            boolean_permission = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_PERMISSIONS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    boolean_permission = true;

                } else {
                    Toast.makeText(getApplicationContext(), "Please allow the permission", Toast.LENGTH_LONG).show();

                }
            }
        }
    }

    public void callNetwork(Double latitude, Double longitude, String cityName, String adminArea, String addressText) {
        restCall = RestClient.createService(RestCall.class, VariableBag.BASE_URL);
        tools.showLoading();
        restCall.getLatLong("user_location", preferenceManager.getRegistredUSerID(), addressText, cityName, adminArea, String.valueOf(latitude), String.valueOf(longitude))
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
                                Tools.toast(MainActivity.this, CommonResponce.getMessage(), 2);
                            } else {
                                if (CommonResponce != null) {
                                    Tools.toast(MainActivity.this, CommonResponce.getMessage(), 1);
                                }
                            }

                        });
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, GoogleService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
//        presenter.attachView(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServicesAvailable();

    }

    private void checkPlayServicesAvailable() {
        final GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        final int status = apiAvailability.isGooglePlayServicesAvailable(this);

        if (status != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(status)) {
                apiAvailability.getErrorDialog(this, status, 1).show();
            } else {
                Snackbar.make(lastUpdate, "Google Play Services unavailable. This app will not work", Snackbar.LENGTH_INDEFINITE).show();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
//        presenter.detachView();
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        MyApplication.getRefWatcher().watch(presenter);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_licenses) {
            new LibsBuilder()
                    .withFields(Libs.toStringArray(R.string.class.getFields()))
                    .withActivityStyle(Libs.ActivityStyle.LIGHT_DARK_TOOLBAR)
                    .withActivityTitle("Open Source Licenses")
                    .withLicenseShown(true)
                    .start(this);

            return true;
        }

        return false;
    }

    // View Interface

    @Override
    public void onLocationUpdate(Location location) {
        lastUpdate.setText(DATE_FORMAT.format(new Date()));
        locLatitude=location.getLatitude();
        locLongitude=location.getLongitude();
        locationText.setText(location.getLatitude() + ", " + location.getLongitude());
        callNetwork1();
    }

    @Override
    public void onAddressUpdate(Address address) {
        addressText.setText(getAddressText(address));
        fullAddress=getAddressText(address);
        area=address.getSubLocality();
        locality=address.getLocality();
    }

    private void callNetwork1() {
        restCall = RestClient.createService(RestCall.class, VariableBag.BASE_URL);
        restCall.getLatLong("user_location", preferenceManager.getRegistredUSerID(), fullAddress, area, locality, String.valueOf(locLatitude), String.valueOf(locLongitude))
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.newThread())
                .subscribe(new Subscriber<CommonResponce>() {
                    @Override
                    public void onCompleted() {
                        tools.stopLoading();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("##", Objects.requireNonNull(e.getMessage()));


                    }

                    @Override

                    public void onNext(final CommonResponce CommonResponce) {
                        runOnUiThread(() -> {
                            new Gson().toJson(CommonResponce);
                            if (CommonResponce != null && CommonResponce.getSuccess() != null && CommonResponce.getSuccess().equals(VariableBag.SUCCESS)) {
                                Tools.toast(MainActivity.this, CommonResponce.getMessage(), 2);
                            } else {
                                if (CommonResponce != null) {
                                    Tools.toast(MainActivity.this, CommonResponce.getMessage(), 1);
                                }
                            }

                        });
                    }
                });
    }

    @Override
    public void onLocationSettingsUnsuccessful() {
        Snackbar.make(lastUpdate, "Location settings requirements not satisfied. Showing last known location if available.", Snackbar.LENGTH_INDEFINITE)
                .setAction("Retry", view -> presenter.startLocationRefresh())
                .show();
    }

    private String getAddressText(Address address) {
        String addressText = "";
        final int maxAddressLineIndex = address.getMaxAddressLineIndex();

        for (int i = 0; i <= maxAddressLineIndex; i++) {
            addressText += address.getAddressLine(i);
            if (i != maxAddressLineIndex) {
                addressText += "\n";
            }
        }

        return addressText;
    }
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get
            // LocalService instance
            GoogleService.LocalBinder binder = (GoogleService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
//            presenter.attachView(MainActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
}
