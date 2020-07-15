package com.example.gpslocationtracker.Firebase;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.gpslocationtracker.LoginActivity;
import com.example.gpslocationtracker.MainActivity;
import com.example.gpslocationtracker.R;
import com.example.gpslocationtracker.SplashActivity;
import com.example.gpslocationtracker.utility.CustomApplication;
import com.example.gpslocationtracker.utility.PreferenceManager;
import com.google.firebase.BuildConfig;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    public static final String NOTIFICATION_CHANNEL_ID = "10001";
    private static final String TAG = "MyFirebaseMsgService";
    String title, message, newsImage, click_action, type;
    PreferenceManager preferenceManager;
    Notification.MediaStyle style;
    Bitmap bitmap;
    NotificationManager notificationManager;
    private NotificationCompat.Builder mBuilder;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        preferenceManager = new PreferenceManager(CustomApplication.getCustomAppContext());
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        title = remoteMessage.getData().get("Title");
        message = remoteMessage.getData().get("Message");
        type = remoteMessage.getData().get("Type");
        click_action = remoteMessage.getData().get("click_action");



        Log.e("##", title);
        Log.e("##", message);

//        try {
//            if (Delegate.mainActivity!=null && !Delegate.mainActivity.isDestroyed()){
//                Delegate.mainActivity.logoutUser();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }




        if (title.equalsIgnoreCase("Logout")) {
            preferenceManager.clearPrefrence();
            Intent dialogIntent = new Intent(this, LoginActivity.class);
            dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(dialogIntent);
        }

        if(click_action!=null && click_action.length()>0) {

            JSONObject mainObject = null;
            try {
                mainObject = new JSONObject(click_action);

                String type = mainObject.getString("type");

                if (type!=null && type.length()>0 && type.equalsIgnoreCase("login")) {

                    try {
//                        if (Delegate.mobileVerificationActivity.isDestroyed()) {
//
//                            sendNotification(title, message, click_action, bitmap);
//
//                        } else {
//                            Delegate.mobileVerificationActivity.callNetwork();
//                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        sendNotification(title, message, click_action, bitmap);

                    }

                } else {
                    sendNotification(title, message, click_action, bitmap);
                }


            } catch (JSONException e) {
                e.printStackTrace();
                sendNotification(title, message, click_action, bitmap);

            }

        }else {
            sendNotification(title, message, click_action, bitmap);
        }
    }

    private void sendNotification(String title, String message, String click_action, Bitmap bitmap) {
        int notificationId = 1;
        String channelId = "channel-01";
        String channelName = "Channel Name";
        Intent resultIntent = null;
        JSONObject mainObject = null;
        int importance = 0;

        Uri NOTIFICATION_SOUND_URI = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + BuildConfig.APPLICATION_ID + "/" + R.raw.iphone_notification);
        int NOTIFICATION_COLOR = getResources().getColor(R.color.colorPrimary);
        long[] VIBRATE_PATTERN    = {0, 500};

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            importance = NotificationManager.IMPORTANCE_HIGH;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                    channelId, channelName, importance);

            mChannel.setSound(NOTIFICATION_SOUND_URI, new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build());

            mChannel.setLightColor(NOTIFICATION_COLOR);
            mChannel.setVibrationPattern(VIBRATE_PATTERN);
            mChannel.setShowBadge(true);
            mChannel.enableVibration(true);

            if (notificationManager != null) {
                notificationManager.createNotificationChannel(mChannel);
            }
        }

        try {
            mainObject = new JSONObject(click_action);
            String click_type = mainObject.getString("type");
            String value = mainObject.getString("value");
            JSONObject subObject = null;

            try {
                subObject = new JSONObject(value);
                if (click_type.length()>0 && click_type.equalsIgnoreCase("Login")){
                    resultIntent = new Intent(this, MainActivity.class);
                    resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    resultIntent.putExtra("userName",subObject.getString("Name"));
                    resultIntent.putExtra("userProfile",subObject.getString("Photo"));
                    resultIntent.putExtra("userId",subObject.getString("id"));
                    resultIntent.putExtra("userMobile","0");
                }else {
                    resultIntent = new Intent(this, MainActivity.class);
                    resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    resultIntent.putExtra("MenuName",subObject.getString("MenuName"));
                    resultIntent.putExtra("ClickName",click_type);
                }
            }catch(JSONException e) {
                e.printStackTrace();
               resultIntent = new Intent(this, SplashActivity.class);

            }

        } catch (JSONException e) {
            e.printStackTrace();
            resultIntent = new Intent(this, SplashActivity.class);
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 11, resultIntent,
                PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setLights(Color.RED, 1000, 1000)
                .setVibrate(new long[]{0, 400, 250, 400})
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);


        if (notificationManager != null) {
            notificationManager.notify(notificationId, mBuilder.build());
        }
    }
}
