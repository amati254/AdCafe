package com.bry.adcafe;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.util.Log;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.bry.adcafe.models.User;
import com.bry.adcafe.ui.LoginActivity;
import com.bry.adcafe.ui.Splash;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by bryon on 21/11/2017.
 */

public class AlarmReceiver1 extends BroadcastReceiver {
    private static final String TAG = "AlarmReceiver - ";
    private NotificationManager notificationManager;
    private PendingIntent pendingIntent;
    private static int NOTIFICATION_ID = 1;
    Notification notification;
    private Context mContext;
    private String mKey;
    private int numberOfSubsFromFirebase = 0;
    private int iterations = 0;
    private int numberOfAdsInTotal = 0;
    private LinkedHashMap<String,Integer> Subscriptions  = new LinkedHashMap<>();



    @Override
    public void onReceive(Context context, Intent intent) {
        Log.w("AlarmReceiver","Broadcast has been received by alarm.");
        mContext = context;
        Intent service1 = new Intent(context, NotificationService1.class);
        service1.setData((Uri.parse("custom://"+System.currentTimeMillis())));
        if(isUserLoggedIn()) checkIfUserWasLastOnlineToday();
//        LocalBroadcastManager.getInstance(mContext).registerReceiver(new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                cancelAlarm();
//            }
//        },new IntentFilter("CANCEL_ALARM"));
    }

    private void checkIfUserWasLastOnlineToday(){
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        User.setUid(uid);
        Log.d(TAG,"Starting to check if user was last online today.");
        DatabaseReference adRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.DATE_IN_FIREBASE);
        adRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String date = dataSnapshot.getValue(String.class);
                if(!date.equals(getDate())){
                    Log.d(TAG,"User was not last online today,checking if there are any ads today.");
                    loadSubscriptionsThenCheckForAds();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void loadSubscriptionsThenCheckForAds(){
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.d(TAG,"Starting to load users data to check if there are ads");
        DatabaseReference adRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.SUBSCRIPTION_lIST);
        adRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot snap: dataSnapshot.getChildren()){
                    numberOfSubsFromFirebase = (int)dataSnapshot.getChildrenCount();
                    String category = snap.getKey();
                    Integer cluster = snap.getValue(Integer.class);
                    Log.d(TAG,"Key category gotten from firebase is : "+category+" Value : "+cluster);
                    Subscriptions.put(category,cluster);
//                    checkInForEachCategory(category,cluster);
                }
                checkNumberForEach();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG,"Something went wrong : "+databaseError.getMessage());
            }
        });
    }

    private void checkNumberForEach(){
        int currentClusterToBeChecked = getClusterValue(iterations);
        String Subscription = getSubscriptionValue(iterations);
        checkInForEachCategory(Subscription,currentClusterToBeChecked);
    }

    private void checkInForEachCategory(String category,int cluster){
        Query query = FirebaseDatabase.getInstance().getReference(Constants.ADVERTS).child(getDate())
                .child(category).child(Integer.toString(cluster));
        DatabaseReference dbRef = query.getRef();
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren()){
                    int numberOfAds = (int)dataSnapshot.getChildrenCount();
                    Log.d(TAG,"adding "+numberOfAds+" to number of ada list.");
                    numberOfAdsInTotal+=numberOfAds;
//                    if(iterations==numberOfSubsFromFirebase){
//                        Log.d(TAG,"All the categories have been handled, total is : "+numberOfAdsInTotal);
//                        if(numberOfAds>0) handleEverything(numberOfAdsInTotal);
//                    }
                }
                iterations++;
                if(iterations<numberOfSubsFromFirebase){
                    checkNumberForEach();
                }else{
                    Log.d(TAG,"All the categories have been handled, total is : "+numberOfAdsInTotal);
                    if(numberOfAdsInTotal>0) beforeHandlingEverything(numberOfAdsInTotal);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG,"Something went wrong : "+databaseError.getMessage());
            }
        });
    }

    private void beforeHandlingEverything(final int number){
        if(!Variables.isLoginOnline){
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            User.setUid(uid);
            Log.d(TAG,"Starting to check if user was last online today.");
            DatabaseReference adRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                    .child(uid).child(Constants.DATE_IN_FIREBASE);
            adRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String date = dataSnapshot.getValue(String.class);
                    if(!date.equals(getDate())){
                        Log.d(TAG,"User was not last online today, continuing to notify user.");
                        handleEverything(number);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    private void handleEverything(int number) {
        String message;
        if (number > 1)message = Html.fromHtml("&#128077;") + "We've got " + number + " ads for you today.";
        else message = Html.fromHtml("&#128516;") + "We've got " + number + " ad for you today.";
        Context context = mContext;
        notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        Intent mIntent = new Intent(context, Splash.class);
        mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        Bundle bundle = new Bundle();
        bundle.putString("Test","test");
        mIntent.putExtras(bundle);
        pendingIntent = pendingIntent.getActivity(context,0,mIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        Resources res = context.getResources();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        notification = new NotificationCompat.Builder(context)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_stat_notification2)
                .setTicker("ticker value")
                .setColor(mContext.getResources().getColor(R.color.colorPrimaryDark))
                .setAutoCancel(true)
                .setPriority(8)
                .setSound(soundUri)
                .setContentTitle("AdCafé.")
                .setContentText(message).build();
        notification.flags|=Notification.FLAG_AUTO_CANCEL|Notification.FLAG_SHOW_LIGHTS;
        notification.defaults|=Notification.DEFAULT_SOUND|Notification.DEFAULT_VIBRATE;
        notification.ledARGB = 0xFFFFA500;
        notification.ledOnMS = 800;
        notification.ledOffMS = 1000;
        notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID,notification);
        Log.i("notif","Notification sent");

    }

    private String getDate(){
        long date = System.currentTimeMillis();
        SimpleDateFormat sdfMonth = new SimpleDateFormat("MM");
        String MonthString = sdfMonth.format(date);

        SimpleDateFormat sdfDay = new SimpleDateFormat("dd");
        String dayString = sdfDay.format(date);

        SimpleDateFormat sdfYear = new SimpleDateFormat("yyyy");
        String yearString = sdfYear.format(date);

        Calendar c = Calendar.getInstance();
        String yy = Integer.toString(c.get(Calendar.YEAR));
        String mm = Integer.toString(c.get(Calendar.MONTH)+1);
        String dd = Integer.toString(c.get(Calendar.DAY_OF_MONTH));

        String todaysDate = (dd+":"+mm+":"+yy);

        return todaysDate;
    }

    private String getNextDay(){
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH,1);
        String yy = Integer.toString(c.get(Calendar.YEAR));
        String mm = Integer.toString(c.get(Calendar.MONTH)+1);
        String dd = Integer.toString(c.get(Calendar.DAY_OF_MONTH));

        String tomorrowsDate = (dd+":"+mm+":"+yy);

        Log.d("AlarmReceiver","Tomorrows date is : "+tomorrowsDate);
        return tomorrowsDate;

    }

    private void cancelAlarm(){
        if(notificationManager!=null)
        notificationManager.cancelAll();
    }

    private int getClusterValue(int index) {
        LinkedHashMap map = Subscriptions;
        int cluster = (new ArrayList<Integer>(map.values())).get(index);
        Log.d(TAG, "Cluster gotten from current subscription is : " + cluster);
        return cluster;
    }

    private String getSubscriptionValue(int index) {
        LinkedHashMap map = Subscriptions;
        String Sub = (new ArrayList<String>(map.keySet())).get(index);
        Log.d(TAG, "Subscription gotten from getCurrent Subscription method is :" + Sub);
        return Sub;
    }

    private boolean isUserLoggedIn(){
        SharedPreferences prefs4 = mContext.getSharedPreferences("IsSignedIn", MODE_PRIVATE);
        return prefs4.getBoolean("isSignedIn", false);
    }

}
