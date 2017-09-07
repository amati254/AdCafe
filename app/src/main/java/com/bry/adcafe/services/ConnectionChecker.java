package com.bry.adcafe.services;

import android.content.Context;
import android.net.ConnectivityManager;

import java.net.InetAddress;

/**
 * Created by bryon on 05/09/2017.
 */

public class ConnectionChecker {

//    public static boolean checkConnection(Context context) {
//        if(isNetworkConnected(context) && isInternetAvailable()){
//            return true;
//        }else{
//            return false;
//        }
//    }

    private static boolean isNetworkConnected(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    private static boolean isInternetAvailable() {
        try {
            InetAddress ipAddr = InetAddress.getByName("google.com");
            return !ipAddr.equals("");

        } catch (Exception e) {
            return false;
        }

    }
}
