package com.example.gpslocationtracker.service;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.gpslocationtracker.MainActivity;
import com.example.gpslocationtracker.R;
import com.example.gpslocationtracker.network.RestCall;
import com.example.gpslocationtracker.network.RestClient;
import com.example.gpslocationtracker.networkresponce.CommonResponce;
import com.example.gpslocationtracker.utility.PreferenceManager;
import com.example.gpslocationtracker.utility.Tools;
import com.example.gpslocationtracker.utility.VariableBag;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;


import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import rx.Subscriber;
import rx.schedulers.Schedulers;

import static android.app.Service.START_STICKY;

public class LocationReciver extends BroadcastReceiver {

    private static final DateFormat DATE_FORMAT = DateFormat.getDateTimeInstance();
    Context context;
    String cdate, pdate;
    PreferenceManager prefrenceManager;
    RestCall restCall;
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    Notification.Builder mBuilder;
    NotificationManager notificationManager;
    Notification notification = null;
    Location location;
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
    private String area, locality;
    private LocationCallback mLocationCallback;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        prefrenceManager = new PreferenceManager(context);
        cdate = simpleDateFormat.format(new Date());

        pdate = prefrenceManager.getKeyValueString("last_in_date");

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);



        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1000);
        locationRequest.setSmallestDisplacement(5.0f);
        locationRequest.setFastestInterval(10000);
        locationRequest.setInterval(1000);

        requestLocationUpdate();

        


        if (cdate.equalsIgnoreCase(pdate)) {
            Log.e("##", "if");
            SendLocation();
        }


    }
    private void requestLocationUpdate() {
        Toast.makeText(context, "Service start", Toast.LENGTH_SHORT).show();
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onSuccess(Location location) {
//               updateLocation(location);
            }
        });

        fusedLocationProviderClient.requestLocationUpdates(locationRequest,new LocationCallback() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                updateLocation(locationResult.getLastLocation());
            }
        },Looper.myLooper());
    }




    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void SendLocation() {

    }

    public String getAddress(Location llocation) {
        Geocoder geocoder;
        List<Address> addresses;
        String temp = "";

        geocoder = new Geocoder(context, Locale.getDefault());
        try {


            addresses = geocoder.getFromLocation(llocation.getLatitude(), llocation.getLongitude(), 1);
            area = addresses.get(0).getSubAdminArea();
            locality = addresses.get(0).getLocality();

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

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void updateLocation(Location currentlocation) {
        Log.e("## Not UpdateLocation :", String.valueOf(currentlocation.getLatitude()));
        if (currentlocation != null) {

            Log.e("## Lat :", String.valueOf(currentlocation.getLatitude()));

            Log.e("## Lati :", String.valueOf(currentlocation.getLatitude()));

//            createNotification();

            String address = null;
            try {
                address = getAddress(currentlocation);
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {

                Log.e("## Data On Server :", prefrenceManager.getRegistredUSerID()+" "+
                        address+" "+ area);

                restCall = RestClient.createService(RestCall.class, VariableBag.BASE_URL);
                restCall.getLatLong(
                        prefrenceManager.getKeyValueString("user_location"),
                        prefrenceManager.getRegistredUSerID(),
                        address, area, locality, String.valueOf(currentlocation.getLatitude()), String.valueOf(currentlocation.getLongitude()))
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.newThread())
                        .subscribe(new Subscriber<CommonResponce>() {
                            @Override
                            public void onCompleted() {
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.d("Location call Error",e.getLocalizedMessage());
//                                notificationManager.cancel(0);
                            }

                            @Override

                            public void onNext(final CommonResponce CommonResponce) {
                                Log.d("Location Updated  ",CommonResponce.getMessage());
//                                notificationManager.cancel(0);
                            }
                        });
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Log.e("##", "else");
            Toast.makeText(context, "Service Error...", Toast.LENGTH_SHORT).show();
        }
    }



    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void createNotification() {

        mBuilder = new Notification.Builder(
                context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notification = mBuilder.setSmallIcon(R.drawable.app_icon).setTicker("Tracking").setWhen(0)
                    .setAutoCancel(false)
                    .setCategory(Notification.EXTRA_BIG_TEXT)
                    .setContentTitle("Location is Updating..")
                    .setColor(ContextCompat.getColor(context, R.color.colorPrimaryDark))
                    .setStyle(new Notification.BigTextStyle()
                            .bigText(cdate))
                    .setChannelId("track_marty")
                    .setProgress(100, 50, true)
                    .setShowWhen(true)
                    .setOngoing(true)
                    .build();
        } else {
            notification = mBuilder.setSmallIcon((Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) ? R.drawable.app_icon : R.mipmap.ic_launcher).setTicker("Tracking").setWhen(0)
                    .setAutoCancel(false)
                    .setCategory(Notification.EXTRA_BIG_TEXT)
                    .setContentTitle("Location is Updating..")
                    .setContentText(cdate)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .setColor(ContextCompat.getColor(context, R.color.colorPrimaryDark))
                    .setStyle(new Notification.BigTextStyle()
                            .bigText(cdate))
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setShowWhen(true)
                    .setOngoing(true)
                    .build();
        }
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel("track_marty", "Track", NotificationManager.IMPORTANCE_HIGH);
            assert notificationManager != null;
            notificationManager.createNotificationChannel(mChannel);
        }

        /*assert notificationManager != null;*/

        if (notificationManager != null) {
            notificationManager.notify(0, notification);
        }

    }

}
