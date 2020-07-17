package com.example.gpslocationtracker;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.manager.ConnectivityMonitor;
import com.example.gpslocationtracker.network.RestCall;
import com.example.gpslocationtracker.network.RestClient;
import com.example.gpslocationtracker.networkresponce.CommonResponce;
import com.example.gpslocationtracker.service.GoogleService;
import com.example.gpslocationtracker.service.LocationReciver;
import com.example.gpslocationtracker.utility.ConnectivityListner;
import com.example.gpslocationtracker.utility.InternetConnection;
import com.example.gpslocationtracker.utility.PreferenceManager;
import com.example.gpslocationtracker.utility.Tools;
import com.example.gpslocationtracker.utility.VariableBag;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;
import com.patloew.rxlocation.RxLocation;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import rx.Subscriber;
import rx.schedulers.Schedulers;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

public class InOutActivity extends AppCompatActivity implements ConnectivityListner.OnCustomStateListener {

    private static final DateFormat DATE_FORMAT = DateFormat.getDateTimeInstance();
    private static final int REQUEST_PERMISSIONS = 100;
    private final BroadcastReceiver mybroadcast = new InternetConnection();
    private TextView lastUpdate;
    private TextView locationText;
    private TextView addressText;
    String area = null, locality;
    RestCall restCall;
    Tools tools;
    Button btnIn, btnOut;
    ImageView ivLogout;
    PreferenceManager preferenceManager;
    private RxLocation rxLocation;

    BottomSheetDialog dialog;
    private MainPresenter presenter;
    private double locLatitude, locLongitude;
    private String fullAddress = null;
    private boolean boolean_permission;
    SharedPreferences mPref;
    SharedPreferences.Editor medit;
    GoogleService mService;
    boolean mBound = false;
    private long interval;
    String currentLat = "", currentLangi = "", currentAddress = "";

    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    Location location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferenceManager = new PreferenceManager(this);
        tools = new Tools(this);

        if (!preferenceManager.getKeyValueString("LocationTimeInterval").equalsIgnoreCase("")){
            interval= Long.parseLong(preferenceManager.getKeyValueString("LocationTimeInterval"));
        }else {
            interval=60000;
        }

        if (dialog == null) {
            dialog = new BottomSheetDialog(InOutActivity.this);
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.O_MR1)
            {
                tools.setWhiteNavigationBar(dialog);
            }
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.no_internet_view);
            dialog.setCancelable(false);
        }

        lastUpdate = findViewById(R.id.tv_last_update);
        locationText = findViewById(R.id.tv_current_location);
        addressText = findViewById(R.id.tv_current_address);

        btnIn = findViewById(R.id.btnIn);
        btnOut = findViewById(R.id.btnOut);
        ivLogout = findViewById(R.id.ivLogout);

        SendLocation();


        btnIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLangi.equalsIgnoreCase("") || currentLat.equalsIgnoreCase("")) {
                    Toast.makeText(InOutActivity.this, "Location is off..2", Toast.LENGTH_SHORT).show();
                    CheckLocation();
                } else {
                    InCall();
                }
            }
        });
        btnOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        ivLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(InOutActivity.this);
                builder.setMessage("Are you sure you want to logout?")
                        .setCancelable(false)
                        .setPositiveButton(Html.fromHtml("<font color='#000000'>Yes</font>"), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                preferenceManager.clearPrefrence();
                                preferenceManager.setLoginSessionFalse();
                                Intent intent = new Intent(InOutActivity.this, LoginActivity.class);
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

    private void init() {

        Intent intent = new Intent(getApplicationContext(), LocationReciver.class);
        intent.setAction("com.example.backgroundproccessdemo.ACTION");
        final PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(),
                0, intent, PendingIntent.FLAG_CANCEL_CURRENT);


        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        final AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        if (alarm != null) {
            Log.e("##", "alaram cancel");
            alarm.cancel(pendingIntent);
        }
        if (alarm != null) {
            Log.e("##", "Set alarm");
            alarm.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), interval, pendingIntent);
        }
    }

    private void SendLocation() {
        getCurrentLocation(InOutActivity.this);
    }

    private void getCurrentLocation(final Context context) {


        fusedLocationProviderClient = new FusedLocationProviderClient(context);
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setFastestInterval(10000);
        locationRequest.setInterval(1000);

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, new LocationCallback() {

            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                location = locationResult.getLastLocation();

                Log.e("##", "Location is set");

            }
        }, Looper.getMainLooper());


        try {
            new Handler().postDelayed(() -> updateLocation(location), 3000);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Location is not Getting...", Toast.LENGTH_SHORT).show();
        }

    }

    private void updateLocation(Location llocation) {

        try {
            currentLat = "" + llocation.getLatitude();
            currentLangi = "" + llocation.getLongitude();
        } catch (Exception e) {
            e.printStackTrace();
        }


        locationText.setText("Latitude:\t" + currentLat + "  " + "Longitude:\t" + currentLangi);

        try {

            try {
                currentAddress = getAddress(llocation);
            } catch (Exception e) {
                e.printStackTrace();
            }

            addressText.setText("Address :\t " + currentAddress);


        } catch (Exception e) {

        }


    }


    public String getAddress(Location llocation) {
        Geocoder geocoder;
        List<Address> addresses;
        String temp = "";

        geocoder = new Geocoder(InOutActivity.this, Locale.getDefault());
        try {


            addresses = geocoder.getFromLocation(llocation.getLatitude(), llocation.getLongitude(), 1);

            locality = addresses.get(0).getSubLocality();
            area = addresses.get(0).getSubAdminArea();
            if (addresses.get(0).getAddressLine(1) != null) {
                return addresses.get(0).getAddressLine(0);
            } else {
                if (addresses.get(0).getSubLocality() != null) {

                    temp = temp + " " + addresses.get(0).getSubLocality();

                    if (addresses.get(0).getLocality() != null) {

                        temp = temp + " ," + addresses.get(0).getLocality();

                        if (addresses.get(0).getPostalCode() != null) {
                            temp = temp + " ," + addresses.get(0).getPostalCode();
                        }
                    }
                } else if (addresses.get(0).getLocality() != null) {
                    temp = temp + " " + addresses.get(0).getLocality();

                    if (addresses.get(0).getPostalCode() != null) {
                        temp = temp + " ," + addresses.get(0).getPostalCode();
                    }
                } else {
                    temp = temp + " " + addresses.get(0).getPostalCode();

                }

                return temp;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public void CheckLocation() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = Objects.requireNonNull(lm).isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            network_enabled = Objects.requireNonNull(lm).isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (!gps_enabled && !network_enabled) {
            // notify user
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setMessage("Location is Off1..")
                    .setPositiveButton("Open Location Setting", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            try {
                                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }).setCancelable(false)
                    .show();

        }

    }
    private void InCall() {
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
                                Tools.toast(InOutActivity.this, CommonResponce.getMessage(), 2);
                                init();
                            } else {
                                if (CommonResponce != null) {
                                    Tools.toast(InOutActivity.this, CommonResponce.getMessage(), 1);
                                }
                            }

                        });
                    }
                });

    }


//    public void callNetwork(Double latitude, Double longitude, String cityName, String adminArea, String addressText) {
//        restCall = RestClient.createService(RestCall.class, VariableBag.BASE_URL);
//        tools.showLoading();
//        restCall.getLatLong("user_location", preferenceManager.getRegistredUSerID(), addressText, cityName, adminArea, String.valueOf(latitude), String.valueOf(longitude))
//                .subscribeOn(Schedulers.io())
//                .observeOn(Schedulers.newThread())
//                .subscribe(new Subscriber<CommonResponce>() {
//                    @Override
//                    public void onCompleted() {
//                        tools.stopLoading();
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        tools.stopLoading();
//                        Log.e("##", Objects.requireNonNull(e.getMessage()));
//
//
//                    }
//
//                    @Override
//
//                    public void onNext(final CommonResponce CommonResponce) {
//                        tools.stopLoading();
//                        runOnUiThread(() -> {
//                            new Gson().toJson(CommonResponce);
//                            if (CommonResponce != null && CommonResponce.getSuccess() != null && CommonResponce.getSuccess().equals(VariableBag.SUCCESS)) {
//                                Tools.toast(InOutActivity.this, CommonResponce.getMessage(), 2);
//                            } else {
//                                if (CommonResponce != null) {
//                                    Tools.toast(InOutActivity.this, CommonResponce.getMessage(), 1);
//                                }
//                            }
//
//                        });
//                    }
//                });
//    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            IntentFilter intentFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
            registerReceiver(mybroadcast, intentFilter);
            ConnectivityListner.getInstance().setListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Override
    protected void onStop() {
        super.onStop();
        try {
            unregisterReceiver(mybroadcast);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }



    @Override
    public void stateChanged() {
        try {
            if (!InOutActivity.this.isDestroyed() && !InOutActivity.this.isFinishing()) {

                boolean modelState = ConnectivityListner.getInstance().getState();

                if (modelState) {

                    if (dialog != null) {
                        dialog.cancel();
                    }


                } else {

                    if (dialog != null) {
                        dialog.show();
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
