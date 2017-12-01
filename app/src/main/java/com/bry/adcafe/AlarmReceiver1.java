package com.bry.adcafe;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by bryon on 21/11/2017.
 */

public class AlarmReceiver1 extends BroadcastReceiver {
    private NotificationManager notificationManager;
    private PendingIntent pendingIntent;
    private static int NOTIFICATION_ID = 1;
    Notification notification;
    private Context mContext;
    private String mKey;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.w("AlarmReceiver","Broadcast has been received by alarm.");
        mContext = context;
        Intent service1 = new Intent(context, NotificationService1.class);
        service1.setData((Uri.parse("custom://"+System.currentTimeMillis())));
        checkForAdsInFirebase();
        LocalBroadcastManager.getInstance(mContext).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                cancelAlarm();
            }
        },new IntentFilter("CANCEL_ALARM"));
    }

    private void checkForAdsInFirebase() {
        Query query = FirebaseDatabase.getInstance().getReference(Constants.ADVERTS).child(getDate())
                .child("1");
        DatabaseReference dbRef = query.getRef();
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren() && !Variables.isMainActivityOnline){
                    Log.d("AlarmReceiver--","There are ads tomorrow ;notifying user thus.");
                    long childCount = dataSnapshot.getChildrenCount();
                    handleEverything(childCount);
                }else{
                    Log.d("AlarmReceiver---","There are no ads tomorrow");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("AlarmReceiver-","There was a database error :"+databaseError.getMessage());
            }
        });
    }

    private void handleEverything(long number) {
        String message;
        if (number > 1)message = Html.fromHtml("&#128077;") + "We've got " + number + " ads for you today.";
        else message = Html.fromHtml("&#128516;") + "We've got " + number + " ad for you today.";
        Context context = mContext;
        notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        Intent mIntent = new Intent(context, Splash.class);
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
}
