package com.example.gpslocationtracker.utility;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class InternetConnection extends BroadcastReceiver {


    @Override
    public void onReceive(final Context context, final Intent intent) {


        int status = NetworkUtil.getConnectivityStatusString(context);
        if ("android.net.conn.CONNECTIVITY_CHANGE".equals(intent.getAction())) {

            if (status == NetworkUtil.NETWORK_STATUS_NOT_CONNECTED) {


                ConnectivityListner.getInstance().changeState(false);


            } else {

                ConnectivityListner.getInstance().changeState(true);


            }
        }
    }

}
