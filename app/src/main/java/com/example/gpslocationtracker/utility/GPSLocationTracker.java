package com.example.gpslocationtracker.utility;

import android.app.Application;


public class GPSLocationTracker extends Application {

    private static GPSLocationTracker mInstance;

    public static synchronized GPSLocationTracker getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    public void setConnectivityListener(ConnectivityReceiver.ConnectivityReceiverListener listener) {
        ConnectivityReceiver.connectivityReceiverListener = listener;
    }

}
