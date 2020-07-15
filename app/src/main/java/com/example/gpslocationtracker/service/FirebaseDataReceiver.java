package com.example.gpslocationtracker.service;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;
import androidx.legacy.content.WakefulBroadcastReceiver;

import com.example.gpslocationtracker.R;
import com.example.gpslocationtracker.SplashActivity;
import com.example.gpslocationtracker.utility.PreferenceManager;
import com.google.firebase.messaging.RemoteMessage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class FirebaseDataReceiver extends WakefulBroadcastReceiver {

    private PreferenceManager preferenceManager;
    NotificationManager notificationManager;
    static int numMessages = 0;


    @Override
    public void onReceive(Context context, Intent var1) {


        String TAG = "FirebaseDataReceiver";
        Log.e(TAG, "I'm in!!!");
        String var2 = var1.getAction();

        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        preferenceManager = new PreferenceManager(context);

        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String currentDateandTime = sdf.format(new Date());

        if ("com.google.android.c2dm.intent.RECEIVE".equals(var2)) {

            Bundle var20;

            if ((var20 = var1.getExtras()) == null) {
                var20 = new Bundle();
            }

            var20.remove("android.support.content.wakelockid");

            RemoteMessage remoteMessage = new RemoteMessage(var20);

            final String title = remoteMessage.getData().get("title");

            final String message = remoteMessage.getData().get("body");

            final String clickAction = remoteMessage.getData().get("click_action");


            sendNotification(context,title,message,clickAction);

        }

    }


    @SuppressLint("StaticFieldLeak")
    class ForegroundCheckTask extends AsyncTask<Context, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Context... params) {
            final Context context = params[0].getApplicationContext();
            return isAppOnForeground(context);
        }

        private boolean isAppOnForeground(Context context) {
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
            if (appProcesses == null) {
                return false;
            }
            final String packageName = context.getPackageName();
            for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
                if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(packageName)) {
                    return true;
                }
            }
            return false;
        }
    }

    // Use like this:

    private void sendNotification(Context context, String title,String message,String clickAction) {

        try {


            int notificationId = 1;
            String channelId = "channel-01";
            String channelName = "Channel Name";
            int importance = 0;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                importance = NotificationManager.IMPORTANCE_HIGH;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel mChannel = new NotificationChannel(
                        channelId, channelName, importance);
                if (notificationManager != null) {
                    notificationManager.createNotificationChannel(mChannel);
                }
            }
            Intent intent;


            intent = new Intent(context, SplashActivity.class);

            PendingIntent pendingIntent = PendingIntent.getActivity(context, 11, intent,
                    PendingIntent.FLAG_ONE_SHOT);

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(R.mipmap.app_icon)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setLights(Color.RED, 1000, 1000)
                    .setVibrate(new long[]{0, 400, 250, 400})
                    .setAutoCancel(true)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .setContentIntent(pendingIntent);

            if (notificationManager != null) {
                notificationManager.notify(notificationId, mBuilder.build());
            }


        } catch (Exception e) {
            e.printStackTrace();
        }


    }
    private void sendNotificationWithImage(Context context, String title,String message,String clickAction,String imageLink)
    {
        try {
            int notificationId = 1;
            String channelId = "channel-01";
            String channelName = "Channel Name";
            int importance = 0;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                importance = NotificationManager.IMPORTANCE_HIGH;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel mChannel = new NotificationChannel(
                        channelId, channelName, importance);
                if (notificationManager != null) {
                    notificationManager.createNotificationChannel(mChannel);
                }
            }
            Intent intent;

            RemoteViews remoteCollapsedViews = new RemoteViews(context.getPackageName(), R.layout.normal_notification);
            RemoteViews remoteExpandedViews = new RemoteViews(context.getPackageName(), R.layout.expanded_notification);
            intent = new Intent(context, SplashActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent mainPendingIntent=PendingIntent.getActivity(context,0,intent,PendingIntent.FLAG_ONE_SHOT);

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(R.mipmap.app_icon)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentTitle(title)
                    .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                    .setCustomContentView(remoteCollapsedViews)
                    .setCustomBigContentView(remoteExpandedViews)
                    .setContentText(message)
                    .setLights(Color.RED, 1000, 1000)
                    .setVibrate(new long[]{0, 400, 250, 400})
                    .setAutoCancel(true)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .setContentIntent(mainPendingIntent);

            if (notificationManager != null) {
                notificationManager.notify(notificationId, mBuilder.build());
            }



        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendVisitorNotification(Context context,String title,String message,String clickAction,String society_id,String visitorId,String visitorName){

     /*  Intent intentCallReject = new Intent(context, HandleVisitorAcceptBroadcastReceiver.class);
        PendingIntent pIntentCallReject = PendingIntent.getBroadcast(context, 0, intentCallReject, 0);


        //Create Notification using NotificationCompat.Builder
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                // Set Icon
                .setSmallIcon(R.drawable.logo_white)
                // Set Title
                .setContentTitle(title)
                // Add an Action Button below Notification
                .addAction(android.R.drawable.ic_menu_call, "Accept", pIntentCallAccept)
                .addAction(android.R.drawable.sym_action_email, "Hold", pIntentCallHold)
                .addAction(android.R.drawable.sym_action_email, "Reject", pIntentCallReject)
                // Set PendingIntent into Notification
                .setContentIntent(pIntentCallAccept)
                .setContentIntent(pIntentCallHold)
                .setContentIntent(pIntentCallReject)
                // showing action button on notification
                .setPriority(Notification.PRIORITY_MAX)
                .setWhen(0)
                .setColor(0xFF000000)
                *//* Increase notification number every time a new notification arrives *//*
                .setNumber(++numMessages)

                // Dismiss Notification
                .setAutoCancel(true);

        // Create Notification Manager
        NotificationManager notificationmanager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // Build Notification with Notification Manager
        if (notificationmanager != null) {
            notificationmanager.notify(0, builder.build());
        }
*/
    }

}
