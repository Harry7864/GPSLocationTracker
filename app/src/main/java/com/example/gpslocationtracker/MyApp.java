package com.example.gpslocationtracker;

import android.app.Application;

import com.example.gpslocationtracker.networkmanager.DroidNet;


public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        DroidNet.init(this);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        DroidNet.getInstance().removeAllInternetConnectivityChangeListeners();
    }
}
