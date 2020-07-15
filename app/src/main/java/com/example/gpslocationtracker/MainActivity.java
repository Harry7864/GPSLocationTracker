package com.example.gpslocationtracker;

import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gpslocationtracker.network.RestCall;
import com.example.gpslocationtracker.network.RestClient;
import com.example.gpslocationtracker.networkresponce.CommonResponce;
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

import java.text.DateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import rx.Subscriber;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements MainView {

    private static final DateFormat DATE_FORMAT = DateFormat.getDateTimeInstance();

    private TextView lastUpdate;
    private TextView locationText;
    private TextView addressText;
    String area=null,locality;
    RestCall restCall;
    Tools tools;
    PreferenceManager preferenceManager;
    private RxLocation rxLocation;

    private MainPresenter presenter;
    private double locLatitude,locLongitude;
    private String fullAddress=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferenceManager=new PreferenceManager(this);
        tools=new Tools(this);
        lastUpdate = findViewById(R.id.tv_last_update);
        locationText = findViewById(R.id.tv_current_location);
        addressText = findViewById(R.id.tv_current_address);

        rxLocation = new RxLocation(this);
        rxLocation.setDefaultTimeout(25, TimeUnit.SECONDS);

        presenter = new MainPresenter(rxLocation);

    }

    public void callNetwork() {
        restCall = RestClient.createService(RestCall.class, VariableBag.BASE_URL);
        tools.showLoading();
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
        presenter.attachView(this);
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
        presenter.detachView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyApplication.getRefWatcher().watch(presenter);
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
//        lastUpdate.setText(DATE_FORMAT.format(new Date()));
        locLatitude=location.getLatitude();
        locLongitude=location.getLongitude();
//        locationText.setText(location.getLatitude() + ", " + location.getLongitude());

    }

    @Override
    public void onAddressUpdate(Address address) {
//        addressText.setText(getAddressText(address));
        fullAddress=getAddressText(address);
        area=address.getSubLocality();
        locality=address.getLocality();
        callNetwork();
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

}
