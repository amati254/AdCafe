package com.bry.adcafe.services;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;

import com.bry.adcafe.Constants;

import java.net.InetAddress;

/**
 * Created by bryon on 05/09/2017.
 */

public class ConnectionChecker {
    private static boolean hasNetworkCheckerStarted;

//    public static boolean checkConnection(Context context) {
//        if(isNetworkConnected(context) && isInternetAvailable()){
//            return true;
//        }else{
//            return false;
//        }
//    }

    public static void StartNetworkChecker(final Context context){
        Handler handler=new Handler();
        if(!hasNetworkCheckerStarted){
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    hasNetworkCheckerStarted = true;
                    if(!isNetworkConnected(context)){
                        Intent intent = new Intent(Constants.CONNECTION_OFFLINE);
                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                    }else if(isNetworkConnected(context)){
                        Intent intent = new Intent(Constants.CONNECTION_ONLINE);
                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                    }
                }
            },10000);
        }
    }

    private static boolean isNetworkConnected(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return (netInfo != null && netInfo.isConnected());
    }

    private static boolean isInternetAvailable() {
        try {
            InetAddress ipAddr = InetAddress.getByName("google.com");
            return !ipAddr.toString().equals("");
        } catch (Exception e) {
            return false;
        }

    }
}
