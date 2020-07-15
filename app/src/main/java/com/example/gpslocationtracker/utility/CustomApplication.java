package com.example.gpslocationtracker.utility;

import android.content.Context;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;


/**
 * Created by Supun on 6/13/2016.
 */

//class for context activity for DB access
public class CustomApplication extends MultiDexApplication {
    // Ankit
    private static Context context;

    public static Context getCustomAppContext() {
        return context;
    }

    public void onCreate() {
        super.onCreate();
        MultiDex.install(this);
        context = getApplicationContext();
    }
}
