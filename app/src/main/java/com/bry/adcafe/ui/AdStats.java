package com.bry.adcafe.ui;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bry.adcafe.Constants;
import com.bry.adcafe.R;
import com.bry.adcafe.adapters.MyAdStatsItem;
import com.bry.adcafe.models.Advert;
import com.bry.adcafe.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mindorks.placeholderview.PlaceHolderView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class AdStats extends AppCompatActivity {
    private static final String TAG = "AdStats";
    private List<String> mAdList = new ArrayList<>();
    private List<Advert> mUploadedAds = new ArrayList<>();

    private List<String> mAdList2 = new ArrayList<>();
    private List<Advert> mUploadedAds2 = new ArrayList<>();
    private Context mContext;
    @Bind(R.id.PlaceHolderViewInfo) PlaceHolderView DataListsView;
    @Bind(R.id.YesterdaysTitle) TextView YesterdayAdsTitle;
    @Bind(R.id.PlaceHolderViewInfoPrevious) PlaceHolderView yesterdayPlaceHolderView;
    private int cycleCount = 0;
    private int cycleCount2 = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad_stats);
        mContext = this.getApplicationContext();
        ButterKnife.bind(this);

        if(isNetworkConnected(mContext)){
            DataListsView.setVisibility(View.GONE);
            yesterdayPlaceHolderView.setVisibility(View.GONE);

            findViewById(R.id.topText).setVisibility(View.GONE);
            YesterdayAdsTitle.setVisibility(View.GONE);

            findViewById(R.id.LoadingViews).setVisibility(View.VISIBLE);

            loadAdsThatHaveBeenUploaded();
        }else{
            showNoConnectionView();
        }
    }

    private void showNoConnectionView() {
        DataListsView.setVisibility(View.GONE);
        yesterdayPlaceHolderView.setVisibility(View.GONE);
        YesterdayAdsTitle.setVisibility(View.GONE);
        findViewById(R.id.topText).setVisibility(View.GONE);

        findViewById(R.id.droppedInternetLayoutForAdStats).setVisibility(View.VISIBLE);
    }




    private void loadAdsThatHaveBeenUploaded() {
        Log.d(TAG,"Loading ads uploaded by user.");
        Query query = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(User.getUid()).child(Constants.UPLOADED_AD_LIST).child(getDate());
        DatabaseReference dbref = query.getRef();
        dbref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren()){
                    Log.d(TAG,"Children have been gotten from firebase");
                    for(DataSnapshot snap: dataSnapshot.getChildren()){
                        String pushValue = snap.getValue(String.class);
                        mAdList.add(pushValue);
                    }
                    loadAdsUploadedByUser();
                    Log.d(TAG,"Number of children is : "+mAdList.size());
                }else{
                    loadPreviousDaysAds();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG,"Database error in loading ; "+databaseError.getMessage());
                Toast.makeText(mContext,"We're having a hard time with your connection...",Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadAdsUploadedByUser() {
        for(int i = 0; i<mAdList.size(); i++){
            String adToBeLoaded = mAdList.get(i);

            Query query = FirebaseDatabase.getInstance().getReference(Constants.ADS_FOR_CONSOLE)
                    .child(getDate()).child(adToBeLoaded);
            DatabaseReference dbRef = query.getRef();
            dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Advert adUploadedByUser = dataSnapshot.getValue(Advert.class);
                    Log.d(TAG,"Gotten one ad from firebase. : "+adUploadedByUser.getPushRefInAdminConsole());
                    mUploadedAds.add(adUploadedByUser);
                    cycleCount++;
                    if(cycleCount == mAdList.size()){
                        Log.d(TAG,"All the ads have been handled.");
                        loadStats();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d(TAG,"Database error in loading ; "+databaseError.getMessage());
                    Toast.makeText(mContext,"We're having a few connectivity issues...",Toast.LENGTH_LONG).show();
                }
            });

        }

    }

    private void loadStats() {
        DataListsView.getBuilder().setLayoutManager(new GridLayoutManager(mContext,2));
        for(int i = 0; i<mUploadedAds.size();i++){
            DataListsView.addView(new MyAdStatsItem(mContext,DataListsView,mUploadedAds.get(i)));
        }

        loadPreviousDaysAds();
    }




    private void loadPreviousDaysAds() {
        Log.d(TAG,"Loading ads uploaded by user.");
        Query query = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(User.getUid()).child(Constants.UPLOADED_AD_LIST).child(getPreviousDay());
        DatabaseReference dbref = query.getRef();
        dbref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren()){
                    Log.d(TAG,"Children have been gotten from firebase");
                    for(DataSnapshot snap: dataSnapshot.getChildren()){
                        String pushValue = snap.getValue(String.class);
                        mAdList2.add(pushValue);
                    }
                    Log.d(TAG,"Number of children is : "+mAdList2.size());
                    loadAdsUploadedByUser2();
                }else{
                    if(mAdList.size()>0) {
                        findViewById(R.id.topText).setVisibility(View.VISIBLE);
                        findViewById(R.id.LoadingViews).setVisibility(View.GONE);
                        DataListsView.setVisibility(View.VISIBLE);
                    }else{
                        findViewById(R.id.topText).setVisibility(View.VISIBLE);
                        findViewById(R.id.LoadingViews).setVisibility(View.GONE);
                        findViewById(R.id.noAdsUploadedText).setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG,"Database error in loading ; "+databaseError.getMessage());
                Toast.makeText(mContext,"We're having a hard time with your connection...",Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadAdsUploadedByUser2() {
        for(int i = 0; i<mAdList2.size(); i++){
            String adToBeLoaded = mAdList2.get(i);

            Query query = FirebaseDatabase.getInstance().getReference(Constants.ADS_FOR_CONSOLE)
                    .child(getPreviousDay()).child(adToBeLoaded);
            DatabaseReference dbRef = query.getRef();
            dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Advert adUploadedByUser = dataSnapshot.getValue(Advert.class);
                    Log.d(TAG,"Gotten one ad from firebase. : "+adUploadedByUser.getPushRefInAdminConsole());
                    mUploadedAds2.add(adUploadedByUser);
                    cycleCount2++;
                    if(cycleCount2 == mAdList2.size()){
                        Log.d(TAG,"All the ads have been handled.");
                        loadStats2();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d(TAG,"Database error in loading ; "+databaseError.getMessage());
                    Toast.makeText(mContext,"We're having a few connectivity issues...",Toast.LENGTH_LONG).show();
                }
            });

        }
    }

    private void loadStats2() {
        DataListsView.setVisibility(View.VISIBLE);
        yesterdayPlaceHolderView.setVisibility(View.VISIBLE);
        YesterdayAdsTitle.setVisibility(View.VISIBLE);
        findViewById(R.id.topText).setVisibility(View.VISIBLE);
        findViewById(R.id.LoadingViews).setVisibility(View.GONE);

        yesterdayPlaceHolderView.getBuilder().setLayoutManager(new GridLayoutManager(mContext,2));
        for(int i = 0; i<mUploadedAds2.size();i++){
            yesterdayPlaceHolderView.addView(new MyAdStatsItem(mContext,yesterdayPlaceHolderView,mUploadedAds2.get(i)));
        }
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

    private boolean isNetworkConnected(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return (netInfo != null && netInfo.isConnected());
    }


}
