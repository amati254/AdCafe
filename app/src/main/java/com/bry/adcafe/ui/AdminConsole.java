package com.bry.adcafe.ui;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.bry.adcafe.Constants;
import com.bry.adcafe.R;
import com.bry.adcafe.Variables;
import com.bry.adcafe.adapters.AdminAdsItem;
import com.bry.adcafe.adapters.AdminStatItem;
import com.bry.adcafe.models.Advert;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mindorks.placeholderview.PlaceHolderView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import okhttp3.internal.DiskLruCache;

public class AdminConsole extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = "AdminConsole";
    private Context mContext;
    @Bind(R.id.LoadAdsWhichHaveBeenSeenLess) Button adsWhichHaveBeenSeenLess;
    @Bind(R.id.PlaceHolderViewData) PlaceHolderView DataListsView;
    @Bind(R.id.LoadTomorrowsAds) Button mLoadTomorrowsAdsButton;
    @Bind(R.id.PlaceHolderViewTomorrowsAds) PlaceHolderView TomorrowsAdsListView;
    private ProgressDialog mAuthProgressDialog;
    public List<Integer> clusters = new ArrayList<>();
    private int runCount = 0;
    private int numberOfClusters =0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_console);
        ButterKnife.bind(this);
        mContext = this.getApplicationContext();

        adsWhichHaveBeenSeenLess.setOnClickListener(this);
        mLoadTomorrowsAdsButton.setOnClickListener(this);
        registerReceivers();
        createProgressDialog();
    }

    @Override
    protected void onDestroy() {
        unregisterReceivers();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        if(v == adsWhichHaveBeenSeenLess){
            loadAdsWhichHaveBeenSeenLess();
        }else if(v == mLoadTomorrowsAdsButton){
            loadTomorrowsads();
        }
    }

    private void registerReceivers(){
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverForTakeDownAd,
                new IntentFilter("TAKE_DOWN"));

    }

    private void unregisterReceivers(){
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiverForTakeDownAd);
    }

    private void loadTomorrowsads() {
        Query query = FirebaseDatabase.getInstance().getReference(Constants.ADS_FOR_CONSOLE)
                .child(getNextDay());
        DatabaseReference dbref = query.getRef();
        dbref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren()){
                    for(DataSnapshot snap:dataSnapshot.getChildren()) {
                        Advert ad = snap.getValue(Advert.class);
                        DataSnapshot clusters = snap.child("clustersToUpLoadTo");
                        for(DataSnapshot clusterSnap : clusters.getChildren()){
                            int cluster = Integer.parseInt(clusterSnap.getKey());
                            int pushId = clusterSnap.getValue(int.class);
                            ad.clusters.put(cluster,pushId);
                        }
                        TomorrowsAdsListView.addView(new AdminAdsItem(mContext,TomorrowsAdsListView,ad));
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(mContext,"Something went wrong loading"+databaseError.getMessage(),Toast.LENGTH_LONG).show();
                Log.d(TAG,"Something went wrong loading the ads"+databaseError.getMessage());
            }
        });
    }

    private void loadAdsWhichHaveBeenSeenLess() {
        Query query = FirebaseDatabase.getInstance().getReference(Constants.ADS_FOR_CONSOLE)
                .child(getPreviousDay());

        DatabaseReference dbRef = query.getRef();
        dbRef.orderByChild("numberOfTimesSeen").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren()){
                    for(DataSnapshot snap:dataSnapshot.getChildren()){
                        Advert ad = snap.getValue(Advert.class);
                        if(ad.getNumberOfTimesSeen()<ad.getNumberOfUsersToReach()){
                            DataListsView.addView(new AdminStatItem(mContext,DataListsView,ad));
                        }
                    }
                }else{
                    Toast.makeText(mContext,"No children from database exist",Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(mContext,"Something went wrong loading"+databaseError.getMessage(),Toast.LENGTH_LONG).show();
                Log.d(TAG,"Something went wrong loading the ads"+databaseError.getMessage());
            }
        });

    }

    private String getPreviousDay(){
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH,-1);
        String yy = Integer.toString(c.get(Calendar.YEAR));
        String mm = Integer.toString(c.get(Calendar.MONTH)+1);
        String dd = Integer.toString(c.get(Calendar.DAY_OF_MONTH));

        String tomorrowsDate = (dd+":"+mm+":"+yy);

        Log.d(TAG,"Tomorrows date is : "+tomorrowsDate);
        return tomorrowsDate;

    }

    private String getNextDay(){
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH,1);
        String yy = Integer.toString(c.get(Calendar.YEAR));
        String mm = Integer.toString(c.get(Calendar.MONTH)+1);
        String dd = Integer.toString(c.get(Calendar.DAY_OF_MONTH));

        String tomorrowsDate = (dd+":"+mm+":"+yy);

        Log.d(TAG,"Tomorrows date is : "+tomorrowsDate);
        return tomorrowsDate;

    }

    private BroadcastReceiver mMessageReceiverForTakeDownAd = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Message received for taking down ad.");
            showConfirmSubscribeMessage();
        }
    };

    private void showConfirmSubscribeMessage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("AdCafe.");
        builder.setMessage("Are you sure you want to take down the ad?")
                .setCancelable(true)
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {

                    }
                })
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                       takeDownAd();
                    }
                })
                .setNegativeButton("No.", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).show();
    }

    private void takeDownAd() {
        mAuthProgressDialog.show();
        Advert ad = Variables.adToBeFlagged;
        Log.d(TAG,"Flagging ad : "+ad.getPushRefInAdminConsole());
         numberOfClusters = ad.clusters.size();
        for(Integer cluster : ad.clusters.keySet()){
            int pushId = ad.clusters.get(cluster);
            DatabaseReference  mRef3 = FirebaseDatabase.getInstance().getReference(Constants.ADVERTS)
                    .child(getNextDay())
                    .child(ad.getCategory())
                    .child(Integer.toString(cluster))
                    .child(Integer.toString(pushId))
                    .child("flagged");
            mRef3.setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    runCount++;
                    if(runCount==numberOfClusters){
                        runCount = 0;
                        numberOfClusters = 0;
                        mAuthProgressDialog.dismiss();
                    }
                }
            });
        }
    }

    private void createProgressDialog(){
        mAuthProgressDialog = new ProgressDialog(this);
        mAuthProgressDialog.setTitle("AdCafe.");
        mAuthProgressDialog.setMessage("Taking down ad...");
        mAuthProgressDialog.setCancelable(false);
    }

}
