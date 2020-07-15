package com.example.gpslocationtracker.service;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        if (remoteMessage.getData() != null) {

            String title = remoteMessage.getData().get("title");
            final String message = remoteMessage.getData().get("body");

            Log.e("### Title", title);
            Log.e("### Message", message);


        }
    }
}
