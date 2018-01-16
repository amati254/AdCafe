package com.bry.adcafe.ui;

import android.app.AlarmManager;
import android.app.FragmentManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.bry.adcafe.AlarmReceiver1;
import com.bry.adcafe.Constants;
import com.bry.adcafe.R;
import com.bry.adcafe.Variables;
import com.bry.adcafe.adapters.AdvertCard;
import com.bry.adcafe.adapters.AdCounterBar;
import com.bry.adcafe.fragments.ReportDialogFragment;
import com.bry.adcafe.models.Advert;
import com.bry.adcafe.models.User;
import com.bry.adcafe.services.DatabaseManager;
import com.bry.adcafe.services.NetworkStateReceiver;
import com.bry.adcafe.services.Utils;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mindorks.placeholderview.PlaceHolderView;
import com.mindorks.placeholderview.SwipeDecor;
import com.mindorks.placeholderview.SwipePlaceHolderView;
import com.wang.avi.AVLoadingIndicatorView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,NetworkStateReceiver.NetworkStateReceiverListener {
    private static final String TAG = "MainActivity";
    public String NOTIFICATION_ID = "notification_id";
    public String NOTIFICATION = "notification";
    private LinearLayout mFailedToLoadLayout;
    private Button mRetryButton;
    private ImageButton mLogoutButton;
    private SwipePlaceHolderView mSwipeView;
    private PlaceHolderView mAdCounterView;
    private Context mContext;
    private String mKey = "";

    private List<Advert> mAdList = new ArrayList<>();
    private Runnable mViewRunnable;
    private LinearLayout mBottomNavButtons;
    private AVLoadingIndicatorView mAvi;
    private AVLoadingIndicatorView mAviLoadingMoreAds;
    private ProgressBar spinner;
    private TextView mLoadingText;
    private boolean mIsBeingReset = false;

    private DatabaseReference dbRef;
    private int mChildToStartFrom = 0;
    Handler h = new Handler();
    Runnable r;

    private NetworkStateReceiver networkStateReceiver;
    boolean doubleBackToExitPressedOnce = false;
    private boolean isFirebaseResetNecessary = false;
    private boolean isOffline = false;
    private boolean isLastAd = false;

    private String igsNein = "none";
    private boolean isLoadingMoreAds = false;
    private boolean mDoublePressedToPin = false;
    private Advert lastAdSeen = null;
    private boolean isSeingNormalAds = true;

    private boolean hasLoadedAnnouncements = false;
    private String stage;
    private LinearLayout cannotLoadLayout;
    private Button retryLoadingFromCannotLoad;

    private int iterations = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = getApplicationContext();
        Variables.isMainActivityOnline = true;
        stage = "LOADING_ADS";
        registerReceivers();
        if (!Fabric.isInitialized()) Fabric.with(this, new Crashlytics());
        setUpAllTheViews();

        if(!isOnline()){
            mAvi.smoothToShow();
            mLoadingText.setVisibility(View.GONE);
            mBottomNavButtons.setVisibility(View.GONE);
            cannotLoadLayout.setVisibility(View.VISIBLE);
            retryLoadingFromCannotLoad.setOnClickListener(this);
        } else {
            loadAdsFromThread();
        }
        logUser();

        new DatabaseManager().setLastSeenDateInFirebase();
        mAviLoadingMoreAds.hide();

    }


    @Override protected void onStart() {
        super.onStart();
        setIsUserLoggedOnInSharedPrefs(true);
        lastAdSeen = Variables.lastAdSeen;
//        if (!getCurrentDateInSharedPreferences().equals("0") && getCurrentDateInSharedPreferences().equals(getDate())) {
//            loadAdsFromThread();
//        }
    }

    @Override protected void onResume() {
        Variables.isMainActivityOnline = true;
        try{
            onclicks();
        }catch (Exception e){
            e.printStackTrace();
        }
        super.onResume();
        if (!getCurrentDateInSharedPreferences().equals("0") && !getCurrentDateInSharedPreferences().equals(getDate())) {
            Log.d(TAG, "---Date in shared preferences does not match current date,therefore resetting everything.");
            sendBroadcastToUnregisterAllReceivers();
            removeAllViews();
            resetEverything();
            lastAdSeen = null;
            Variables.lastAdSeen = null;
        }else if (isAlmostMidNight() && Variables.isMainActivityOnline) {
            mIsBeingReset = true;
            sendBroadcastToUnregisterAllReceivers();
            removeAllViews();
            resetEverything();
            lastAdSeen = null;
            Variables.lastAdSeen = null;
        }else if(Variables.hasChangesBeenMadeToCategories && !mIsBeingReset){
            sendBroadcastToUnregisterAllReceivers();
            removeAllViews();
            loadAdsFromThread();
            lastAdSeen = null;
            Variables.lastAdSeen = null;
            Variables.hasChangesBeenMadeToCategories = false;
        }
        r = new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "---started the time checker for when it is almost midnight.");
                if (isAlmostMidNight() && Variables.isMainActivityOnline) {
                    mIsBeingReset = true;
                    resetEverything();
                    sendBroadcastToUnregisterAllReceivers();
                    removeAllViews();
                    lastAdSeen = null;
                    Variables.lastAdSeen = null;
                }
                h.postDelayed(r, 60000);
            }
        };
        h.postDelayed(r, 60000);
    }

    @Override protected void onPause() {
        super.onPause();
        h.removeCallbacks(r);
        setCurrentDateToSharedPrefs();
        setUserDataInSharedPrefs();
    }




    @Override protected void onStop() {
        super.onStop();
        if (dbRef != null) {
            dbRef.removeEventListener(val);
        }
        setUserDataInSharedPrefs();
        setAlarmForNotifications();
        Variables.lastAdSeen = lastAdSeen;
        Log.d(TAG, "---removing callback for checking time of day.");
    }

    @Override protected void onDestroy() {
        setLastUsedDateInFirebaseDate(User.getUid());
        unregisterAllReceivers();
        removeAllViews();
        Variables.clearAllAdsFromAdList();
        if (!Variables.isDashboardActivityOnline) Variables.clearAdTotal();
        if (networkStateReceiver != null) {
            networkStateReceiver.removeListener(this);
            this.unregisterReceiver(networkStateReceiver);
        }

        Variables.isMainActivityOnline = false;
        super.onDestroy();
    }

    private void setUserDataInSharedPrefs() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("TodayTotals", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.putInt("TodaysTotals", Variables.getAdTotal(mKey));
        Log.d("MAIN_ACTIVITY--", "Setting todays ad totals in shared preferences - " + Integer.toString(Variables.getAdTotal(mKey)));
        editor.apply();

        SharedPreferences pref2 = getApplicationContext().getSharedPreferences("MonthTotals", MODE_PRIVATE);
        SharedPreferences.Editor editor2 = pref2.edit();
        editor2.clear();
        editor2.putInt("MonthsTotals", Variables.getMonthAdTotals(mKey));
        Log.d("MAIN_ACTIVITY--", "Setting the month totals in shared preferences - " + Integer.toString(Variables.getMonthAdTotals(mKey)));
        editor2.apply();

        SharedPreferences pref4 = mContext.getSharedPreferences("UID", MODE_PRIVATE);
        SharedPreferences.Editor editor4 = pref4.edit();
        editor4.clear();
        editor4.putString("Uid", User.getUid());
        Log.d("MAIN_ACTIVITY---", "Setting the user uid in shared preferences - " + User.getUid());
        editor4.apply();

        SharedPreferences pref5 = mContext.getSharedPreferences("CurrentSubIndex", MODE_PRIVATE);
        SharedPreferences.Editor editor5 = pref5.edit();
        editor5.clear();
        editor5.putInt("CurrentSubIndex", Variables.getCurrentSubscriptionIndex());
        Log.d("MAIN_ACTIVITY---", "Setting the users current subscription index in shared preferences - " + Variables.getCurrentSubscriptionIndex());
        editor5.apply();

        SharedPreferences pref6 = mContext.getSharedPreferences("CurrentAdInSubscription", MODE_PRIVATE);
        SharedPreferences.Editor editor6 = pref6.edit();
        editor6.clear();
        editor6.putInt("CurrentAdInSubscription", Variables.getCurrentAdInSubscription());
        Log.d("MAIN_ACTIVITY---", "Setting the current ad in subscription in shared preferences - " + Variables.getCurrentAdInSubscription());
        editor6.apply();

        setSubsInSharedPrefs();
    }

    private void setSubsInSharedPrefs() {
        Gson gson = new Gson();
        String hashMapString = gson.toJson(Variables.Subscriptions);

        SharedPreferences prefs = getSharedPreferences("Subscriptions", MODE_PRIVATE);
        prefs.edit().putString("hashString", hashMapString).apply();
    }



    private void loadAdsFromThread() {
        if (dbRef != null) {
            dbRef.removeEventListener(val);
        }
        if (Variables.isStartFromLogin) {
            try {
                Log.d(TAG, "---Starting the getAds method...");
                startGetAds();
                Variables.isStartFromLogin = false;
            } catch (Exception e) {
                Log.e("BACKGROUND_PROC---", e.getMessage());
            }
        } else {
            loadUserDataFromSharedPrefs();
        }
    }

    private void loadUserDataFromSharedPrefs() {
        Log.d(TAG, "Loading user data from shared preferences first...");
        SharedPreferences prefs = getSharedPreferences("TodayTotals", MODE_PRIVATE);
        int number = prefs.getInt("TodaysTotals", 0);
        Log.d(TAG, "AD TOTAL NUMBER GOTTEN FROM SHARED PREFERENCES IS - " + number);
        if (mIsBeingReset || !getCurrentDateInSharedPreferences().equals(getDate())) {
            Variables.setAdTotal(0, mKey);
            Log.d(TAG, "Setting ad totals in firebase to 0 since is being reset...");
        } else {
            Variables.setAdTotal(number, mKey);
        }

        SharedPreferences prefs2 = getSharedPreferences("MonthTotals", MODE_PRIVATE);
        int number2 = prefs2.getInt("MonthsTotals", 0);
        Log.d(TAG, "MONTH AD TOTAL NUMBER GOTTEN FROM SHARED PREFERENCES IS - " + number2);
        Variables.setMonthAdTotals(mKey, number2);

        SharedPreferences prefs4 = getSharedPreferences("UID", MODE_PRIVATE);
        String uid = prefs4.getString("Uid", "");
        Log.d(TAG, "UID NUMBER GOTTEN FROM SHARED PREFERENCES IS - " + uid);
        User.setUid(uid);

        SharedPreferences prefs5 = getSharedPreferences("CurrentSubIndex", MODE_PRIVATE);
        int currentSubIndex = prefs5.getInt("CurrentSubIndex",0);
        Log.d(TAG, "CURRENT SUBSCRIPTION INDEX NUMBER GOTTEN FROM SHARED PREFERENCES IS - " + currentSubIndex);
        if (mIsBeingReset || !getCurrentDateInSharedPreferences().equals(getDate())) {
            Variables.setCurrentSubscriptionIndex(0);
            Log.d(TAG,"Setting current sub index to 0 since date in shared prefs doesnt match current date or is being reset");
        }else{
            Variables.setCurrentSubscriptionIndex(currentSubIndex);
        }

        SharedPreferences prefs6 = getSharedPreferences("CurrentAdInSubscription", MODE_PRIVATE);
        int currentAdInSubscription = prefs6.getInt("CurrentAdInSubscription",0);
        Log.d(TAG,"CURRENT AD IN SUBSCRIPTION GOTTEN FROM SHARED PREFERENCES IS : "+currentAdInSubscription);
        if (mIsBeingReset || !getCurrentDateInSharedPreferences().equals(getDate())) {
            Variables.setCurrentAdInSubscription(0);
        }else{
            Variables.setCurrentAdInSubscription(currentAdInSubscription);
        }

        loadSubsFromSharedPrefs();
        Variables.isStartFromLogin = false;
        try {
            Log.d(TAG, "---Starting the getAds method...");
            startGetAds();
        } catch (Exception e) {
            Log.e("BACKGROUND_PROC---", e.getMessage());
        }
    }

    private void loadSubsFromSharedPrefs() {
        if(!Variables.Subscriptions.isEmpty())Variables.Subscriptions.clear();
        Gson gson = new Gson();
        SharedPreferences prefs = getSharedPreferences("Subscriptions", MODE_PRIVATE);
        String storedHashMapString = prefs.getString("hashString", "nil");

        java.lang.reflect.Type type = new TypeToken<LinkedHashMap<String,Integer>>(){}.getType();
        Variables.Subscriptions = gson.fromJson(storedHashMapString, type);
    }

    private void startGetAds() {
        setUpAllTheViews();
        mAvi.setVisibility(View.VISIBLE);
        mLoadingText.setVisibility(View.VISIBLE);
        mBottomNavButtons.setVisibility(View.GONE);
        Log.d(TAG, "---Setting up mViewRunnable thread...");
        mViewRunnable = new Runnable() {
            @Override
            public void run() {
                getAds();
            }
        };
        Thread thread = new Thread(null, mViewRunnable, "Background");
        Log.d(TAG, "---Starting thread...");
        thread.start();

    } ///////////////////////////




    private void getAds() {
        try {
            Log.d(TAG, "---The getAdsFromFirebase method has been called...");
            getGetAdsFromFirebase();
            Thread.sleep(300);
        } catch (Exception e) {
            Log.e("BACKGROUND_PROC", e.getMessage());
        }
        runOnUiThread(returnRes);
    }//method for loading ads from thread.Contains sleep length...

    private void getGetAdsFromFirebase() {
        String date;
        date = mIsBeingReset ? getNextDay() : getDate();

        Variables.nextSubscriptionIndex = Variables.getCurrentSubscriptionIndex();
        Query query = FirebaseDatabase.getInstance().getReference(Constants.ADVERTS).child(date)
                .child(getSubscriptionValue(Variables.getCurrentSubscriptionIndex()))
                .child(Integer.toString(getClusterValue(Variables.getCurrentSubscriptionIndex())));
        Log.d(TAG, "---Query set up is : " + Constants.ADVERTS + " : " + date + " : " + getSubscriptionValue(Variables.getCurrentSubscriptionIndex())+ " : " + getClusterValue(Variables.getCurrentSubscriptionIndex()));
        dbRef = query.getRef();

        if (Variables.getCurrentAdInSubscription() == 0) {
            Log.d(TAG, "User current ad in subscription is 0, so is starting at 1");
            dbRef.orderByKey().startAt(Integer.toString(1)).limitToFirst(5).addValueEventListener(val);
        } else {
            Log.d(TAG, "User current ad in subscription is not 0, so starting at its value : " + Variables.getCurrentAdInSubscription());
            dbRef.orderByKey().startAt(Integer.toString(Variables.getCurrentAdInSubscription()))
                    .limitToFirst(5).addListenerForSingleValueEvent(val);
        }
        if(!mAdList.isEmpty()) mAdList.clear();
        Variables.clearAllAdsFromAdList();
    }

    ValueEventListener val = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if (dataSnapshot.hasChildren()) {
                if(dataSnapshot.getChildrenCount()==1){
                    //if only one ad has loaded.
                    Log.d(TAG,"Only one ad has loaded.");
                    for (DataSnapshot snap : dataSnapshot.getChildren()) {
                        Advert ad = snap.getValue(Advert.class);
                        DataSnapshot snpsht = snap.child("pushId");
                        String pushID = snpsht.getValue(String.class);
                        ad.setPushId(pushID);
                        ad.setPushIdNumber(Integer.parseInt(pushID));
                        if(!ad.isFlagged()) mAdList.add(ad);
                    }
                    if(mAdList.size()!=0){
                        Log.d(TAG,"The one ad was not flagged so its in the adlist");
                        if(Variables.getCurrentAdInSubscription()!=Integer.parseInt(mAdList.get(0).getPushId())){
                            //user hasn't seen the one ad that has been loaded
                            Log.d(TAG,"The user hasn't seen the one ad in the adlist.");
                            Log.d(TAG,"Since the currentAdInSubscription is : "+Variables.getCurrentAdInSubscription()+" and " +
                                    "the ad's pushID is : "+(mAdList.get(0).getPushId()));
                            mChildToStartFrom = Variables.getCurrentAdInSubscription()+ mAdList.size();
                            Log.d(TAG,"The child set to start from is : "+mChildToStartFrom);
                            Variables.setCurrentAdNumberForAllAdsList(0);
//                            mAvi.setVisibility(View.GONE);
//                            mLoadingText.setVisibility(View.GONE);
//                            mBottomNavButtons.setVisibility(View.VISIBLE);
                            loadAdsIntoAdvertCard();
                        }else{
                            //user has seen the one ad that has been loaded.
                            Log.d(TAG,"User has seen the one ad that has been loaded so going to the next subscription");
                            Log.d(TAG,"Since the currentAdInSubscription is : "+Variables.getCurrentAdInSubscription()+" and " +
                                    "the ad's pushID is : "+(mAdList.get(0).getPushId()));
                            lastAdSeen = mAdList.get(0);
                            mChildToStartFrom = Integer.parseInt(lastAdSeen.getPushId());
                            if (Variables.getCurrentSubscriptionIndex()+1 < Variables.Subscriptions.size()) {
                                Log.d(TAG,"Trying the next subscription.");
                                Variables.setNextSubscriptionIndex();
                                Variables.setCurrentAdInSubscription(0);
                                getGetAdsFromFirebase();
                            } else {
                                Log.d(TAG, "---There are no ads in any of the subscriptions");
//                                mAvi.setVisibility(View.GONE);
//                                mLoadingText.setVisibility(View.GONE);
//                                mBottomNavButtons.setVisibility(View.VISIBLE);
                                loadAdsIntoAdvertCard();
                            }
                        }
                    }else{
                        //the one ad may have been flagged so moving on to the next subscription
                        Log.d(TAG,"the one ad may have been flagged so moving on to the next subscription");
                        if (Variables.getCurrentSubscriptionIndex()+1 < Variables.Subscriptions.size()) {
                            Log.d(TAG,"Trying the next subscription.");
                            Variables.setNextSubscriptionIndex();
                            Variables.setCurrentAdInSubscription(0);
                            getGetAdsFromFirebase();
                        } else {
                            Log.d(TAG, "---There are no ads in any of the subscriptions");
//                            mAvi.setVisibility(View.GONE);
//                            mLoadingText.setVisibility(View.GONE);
//                            mBottomNavButtons.setVisibility(View.VISIBLE);
                            loadAdsIntoAdvertCard();
                        }
                    }

                }else{
                    //if multiple ads have loaded.
                    Log.d(TAG,"More than one ad has been loaded from firebase");
                    for (DataSnapshot snap : dataSnapshot.getChildren()) {
                            Advert ad = snap.getValue(Advert.class);
                            DataSnapshot snpsht = snap.child("pushId");
                            String pushID = snpsht.getValue(String.class);
                            ad.setPushId(pushID);
                            ad.setPushIdNumber(Integer.parseInt(pushID));
                            if(!ad.isFlagged()) mAdList.add(ad);
                    }
                    if(mAdList.size()==0){
                        //all the ads loaded may have been flagged so loading the next ads after those ones.
                        Log.d(TAG,"All the ads loaded have been flagged so loading the next batch");
                        Variables.setCurrentAdInSubscription(Variables.getCurrentAdInSubscription()+
                                (int)dataSnapshot.getChildrenCount());
                        getGetAdsFromFirebase();
                    }else{
                        Variables.setCurrentAdNumberForAllAdsList(0);
                        //removing the first ad if the user has seen it.
                        Log.d(TAG,"removing the first ad if the user has seen it.");
                        if(Variables.getCurrentAdInSubscription()==Integer.parseInt(mAdList.get(0).getPushId())) {
                            mAdList.remove(0);
                            Log.d(TAG,"First ad has been removed because it has been seen");
                        }
                        mChildToStartFrom = Variables.getCurrentAdInSubscription()+mAdList.size();
                        Log.d(TAG, "Child set to start from is -- " + mChildToStartFrom);
                        Log.d(TAG, "---All the ads have been handled.Total is " + mAdList.size());
//                        mAvi.setVisibility(View.GONE);
//                        mLoadingText.setVisibility(View.GONE);
//                        mBottomNavButtons.setVisibility(View.VISIBLE);
                        loadAdsIntoAdvertCard();
                    }
                }

            }else{
                if (Variables.getCurrentSubscriptionIndex()+1 < Variables.Subscriptions.size()) {
                    Log.d(TAG, "---There are no ads in subscription : " + getSubscriptionValue(Variables.getCurrentSubscriptionIndex()) + ". Retrying in the next category");
                    Variables.setNextSubscriptionIndex();
                    Variables.setCurrentAdInSubscription(0);
                    getGetAdsFromFirebase();
                } else {
                    Log.d(TAG, "---There are no ads in any of the subscriptions");
//                    mAvi.setVisibility(View.GONE);
//                    mLoadingText.setVisibility(View.GONE);
//                    mBottomNavButtons.setVisibility(View.VISIBLE);
                    loadAdsIntoAdvertCard();
                }
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            mAvi.setVisibility(View.GONE);
            mLoadingText.setVisibility(View.GONE);
            showFailedView();
        }
    };

    ValueEventListener val2 = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if (!dataSnapshot.hasChildren()) {
                if (Variables.getCurrentSubscriptionIndex()+1 < Variables.Subscriptions.size()) {
                    Log.d(TAG, "---There are no ads in subscription : " + getSubscriptionValue(Variables.getCurrentSubscriptionIndex()) + ". Retrying in the next category");
                    Variables.setNextSubscriptionIndex();
                    Variables.setCurrentAdInSubscription(0);
                    getGetAdsFromFirebase();
                } else {
                    Log.d(TAG, "---There are no ads in any of the subscriptions");
                    mAvi.setVisibility(View.GONE);
                    mLoadingText.setVisibility(View.GONE);
                    mBottomNavButtons.setVisibility(View.VISIBLE);
                    loadAdsIntoAdvertCard();
                }
            } else {
                Log.d(TAG, "---Children in dataSnapshot from firebase exist");
                for (DataSnapshot snap : dataSnapshot.getChildren()) {
                    Advert ad = snap.getValue(Advert.class);
                    DataSnapshot snpsht = snap.child("pushId");
                    String pushID = snpsht.getValue(String.class);
                    ad.setPushId(pushID);
                    if(!ad.isFlagged()) mAdList.add(ad);
                }
                if(mAdList.size()!=0){
                    if (Variables.getCurrentAdInSubscription() != 0) {
                        //this means user has seen some ads in current subscription index.
                        //removing first ad from ad list if there are more than one ads.
                        //this is because user has seen the first ad in the ad list.
                        if (mAdList.size() > 1) mAdList.remove(0);
                        //setting the child to start from to the number of children gotten to current ad in sub + children count minus one.
                        //this is because snapshot contains child that has been seen by user.
                        mChildToStartFrom = Variables.getCurrentAdInSubscription() + (int) dataSnapshot.getChildrenCount() - 1;
                        Log.d(TAG,"user has seen some of the ads from current subscription index.");

                    } else {
                        //this means that user has not seen any ad in current subscription index.
                        mChildToStartFrom = (int) dataSnapshot.getChildrenCount();
                        Log.d(TAG,"User has seen none of the ads gotten from current subscription index.");
                        //setting the child to start from to number of children gotten.
                    }
                    Variables.setCurrentAdNumberForAllAdsList(0);
                    Log.d(TAG, "Child set to start from is -- " + mChildToStartFrom);
                    Log.d(TAG, "---All the ads have been handled.Total is " + mAdList.size());
                    mAvi.setVisibility(View.GONE);
                    mLoadingText.setVisibility(View.GONE);
                    mBottomNavButtons.setVisibility(View.VISIBLE);
                    loadAdsIntoAdvertCard();
                }else{
                    if (Variables.getCurrentSubscriptionIndex()+1 < Variables.Subscriptions.size()) {
                        Log.d(TAG, "---There was 1 flagged ad subscription : " +
                                getSubscriptionValue(Variables.getCurrentSubscriptionIndex()) + ". Retrying in the next category");
                        Variables.setNextSubscriptionIndex();
                        Variables.setCurrentAdInSubscription(0);
                        getGetAdsFromFirebase();
                    } else {
                        Log.d(TAG, "---There are no ads in any other subscriptions");
                        mAvi.setVisibility(View.GONE);
                        mLoadingText.setVisibility(View.GONE);
                        mBottomNavButtons.setVisibility(View.VISIBLE);
                        loadAdsIntoAdvertCard();
                    }
                }

            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            mAvi.setVisibility(View.GONE);
            mLoadingText.setVisibility(View.GONE);
            showFailedView();
        }
    };




    private void showFailedView() {
        mFailedToLoadLayout.setVisibility(View.VISIBLE);
        mRetryButton.setOnClickListener(this);
    }

    private Runnable returnRes = new Runnable() {
        @Override
        public void run() {
//            loadAdsIntoAdvertCard();
        }
    };

    private void unregisterAllReceivers() {
        Log.d("MAIN_ACTIVITY--", "Unregistering all receivers");
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForAddingToSharedPreferences);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForConnectionOffline);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForConnectionOnline);

        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForLastAd);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForLoadMoreAds);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForTimerHasStarted);

        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForOnSwiped);

        sendBroadcastToUnregisterAllReceivers();
    }

    private void sendBroadcastToUnregisterAllReceivers() {
        Intent intent = new Intent(Constants.UNREGISTER_ALL_RECEIVERS);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }



    private void setUpAllTheViews() {
        mSwipeView = (SwipePlaceHolderView) findViewById(R.id.swipeView);
        mBottomNavButtons = (LinearLayout) findViewById(R.id.bottomNavButtons);

        mAvi = (AVLoadingIndicatorView) findViewById(R.id.mainActivityAvi);
        mAviLoadingMoreAds = (AVLoadingIndicatorView) findViewById(R.id.aviLoadingNextAds);
        spinner = (ProgressBar)findViewById(R.id.progressBar1);

        mLoadingText = (TextView) findViewById(R.id.loadingAdsMessage);
        mFailedToLoadLayout = (LinearLayout) findViewById(R.id.failedLoadAdsLayout);
        mRetryButton = (Button) findViewById(R.id.retryLoadingAds);
        mLogoutButton = (ImageButton) findViewById(R.id.logoutBtn);

        cannotLoadLayout = (LinearLayout) findViewById(R.id.noInternetLayout);
        retryLoadingFromCannotLoad =(Button) findViewById(R.id.btn_retry);

        int bottomMargin = Utils.dpToPx(90);
        Point windowSize = Utils.getDisplaySize(getWindowManager());
        float relativeScale = density();

        mSwipeView.getBuilder()
                .setDisplayViewCount(4)
                .setIsUndoEnabled(false)
                .setHeightSwipeDistFactor(10)
                .setWidthSwipeDistFactor(5)
                .setSwipeDecor(new SwipeDecor()
                        .setViewWidth(windowSize.x)
                        .setViewHeight(windowSize.y - bottomMargin)
                        .setSwipeRotationAngle(0)
                        .setSwipeAnimTime(200)
                        .setViewGravity(Gravity.TOP)
                        .setPaddingTop(15)
                        .setRelativeScale(relativeScale));
    }

    private void removeAllViews() {
        if (mSwipeView != null) {
            mSwipeView.removeAllViews();
        }
        if (mAdCounterView != null) {
            mAdCounterView.removeAllViews();
        }
    }

    private void loadAdCounter() {
        mAdCounterView = (PlaceHolderView) findViewById(R.id.adCounterView);
        mAdCounterView.addView(new AdCounterBar(this.getApplicationContext(), mAdCounterView));

    }

    private void loadAdsIntoAdvertCard(){
        String date;
        date = mIsBeingReset ? getNextDay() : getDate();

       if(!mAdList.isEmpty()){
           //This will load all the ads images.
           for(final Advert ad: mAdList){
               String pushRefInAdminConsole = ad.getPushRefInAdminConsole();
               DatabaseReference adRef = FirebaseDatabase.getInstance().getReference(Constants.ADS_FOR_CONSOLE).child(date)
                       .child(pushRefInAdminConsole).child("imageUrl");
               adRef.addListenerForSingleValueEvent(new ValueEventListener() {
                   @Override
                   public void onDataChange(DataSnapshot dataSnapshot) {
                       String imageUrl = dataSnapshot.getValue(String.class);
                       mAdList.get(mAdList.indexOf(ad)).setImageUrl(imageUrl);
                       iterations++;
                       if(iterations == mAdList.size()){
                           iterations = 0;
                           loadAdsIntoAdvertCard2();
                       }
                   }

                   @Override
                   public void onCancelled(DatabaseError databaseError) {
                        Log.d(TAG,"An error occured, "+databaseError.getDetails());
                   }
               });
           }

       }else if(lastAdSeen!=null){
           //this will load the image of the last ad only.
           String pushRefInAdminConsole = lastAdSeen.getPushRefInAdminConsole();
           DatabaseReference adRef = FirebaseDatabase.getInstance().getReference(Constants.ADS_FOR_CONSOLE).child(date)
                   .child(pushRefInAdminConsole).child("imageUrl");
           adRef.addListenerForSingleValueEvent(new ValueEventListener() {
               @Override
               public void onDataChange(DataSnapshot dataSnapshot) {
                   String imageUrl = dataSnapshot.getValue(String.class);
                   lastAdSeen.setImageUrl(imageUrl);
                   loadAdsIntoAdvertCard2();
               }

               @Override
               public void onCancelled(DatabaseError databaseError) {
                    Log.d(TAG,"An error occurred while loading data, "+databaseError.getDetails());
               }
           });

       } else{
         loadAdsIntoAdvertCard2();
       }
    }

    private void loadAdsIntoAdvertCard2() {
        boolean loadMoreAds = false;
        stage = "VIEWING_ADS";
        if (mAdCounterView == null) {
            Log.d(TAG, "---Setting up AdCounter...");
            loadAdCounter();
        }
        if (mSwipeView == null) {
            Log.d(TAG, "---Setting up Swipe views..");
            setUpAllTheViews();
        }
        if (mSwipeView.getChildCount() != 0) {
            Log.d(TAG, "Removing existing children from swipeView...");
            mSwipeView.removeAllViews();
        }
        if (mAdCounterView.getChildCount() == 0) {
            Log.d(TAG, "Loading the top timer now...");
            loadAdCounter();
        }
        if (mAdList != null && mAdList.size() > 0) {
            if (mAdList.size() == 1 && mChildToStartFrom == Variables.getCurrentAdInSubscription()) {
                Log.d(TAG, "---User has seen all the ads, thus will load only last ad...");
                Log.d(TAG,"The child to start from is : "+mChildToStartFrom+" and currentAdInSubscriptionIs : "+
                        Variables.getCurrentAdInSubscription());
                mSwipeView.lockViews();
                mSwipeView.addView(new AdvertCard(mContext, mAdList.get(0), mSwipeView, Constants.LAST));
                Variables.adToVariablesAdList(mAdList.get(0));
                Variables.setIsLastOrNotLast(Constants.LAST);
                Variables.setCurrentAdvert(mAdList.get(0));
                Variables.setCurrentSubscriptionIndex(getPositionOf(mAdList.get(0).getCategory()));
                try{
                    Variables.setCurrentAdInSubscription(Integer.parseInt(mAdList.get(0).getPushId()));
                }catch (Exception e){
                    e.printStackTrace();
                    Variables.setCurrentAdInSubscription(mAdList.get(0).getPushIdNumber());
                }
                if(mAdList.get(0).isFlagged())mAdList.get(0).setWebsiteLink(igsNein);
                if(mAdList.get(0).getWebsiteLink().equals(igsNein)){
                    findViewById(R.id.smallDot).setVisibility(View.INVISIBLE);
                }else{
                    findViewById(R.id.smallDot).setVisibility(View.VISIBLE);
                }

                if(Variables.didAdCafeRemoveCategory)informUserOfSubscriptionChanges();
                if(Variables.didAdCafeAddNewCategory) tellUserOfNewSubscription();
                Toast.makeText(mContext, "We've got no more stuff for you today.", Toast.LENGTH_SHORT).show();
                isLastAd = true;
                Variables.isLockedBecauseOfNoMoreAds = true;
                loadAnyAnnouncements();
            } else {
                if (mAdList.size() == 1 && Variables.getCurrentSubscriptionIndex() + 1 < Variables.Subscriptions.size()) {
                    Variables.nextSubscriptionIndex = Variables.getCurrentSubscriptionIndex() + 1;
                    mChildToStartFrom = 0;
//                    loadMoreAds = true;
//                    loadMoreAds();
                }
                for (Advert ad : mAdList) {
                    mSwipeView.addView(new AdvertCard(mContext, ad, mSwipeView, Constants.NOT_LAST));
                    Log.d(TAG, "Loading ad " + ad.getPushRefInAdminConsole());
                    Variables.adToVariablesAdList(ad);
                    Variables.setIsLastOrNotLast(Constants.NOT_LAST);
                }
            }
            mAdList.clear();
            Log.d(TAG,"cleared the adlist");
        } else {
            if(Variables.didAdCafeRemoveCategory)informUserOfSubscriptionChanges();
            if(Variables.didAdCafeAddNewCategory) tellUserOfNewSubscription();

            if(lastAdSeen!=null){
                Log.d(TAG, "---Loading only last ad from lastAdSeen that was initialised...");
                mSwipeView.lockViews();
                mSwipeView.addView(new AdvertCard(mContext, lastAdSeen, mSwipeView, Constants.LAST));
                Variables.adToVariablesAdList(lastAdSeen);
                Variables.setIsLastOrNotLast(Constants.LAST);
                Variables.setCurrentAdvert(lastAdSeen);
                Variables.setCurrentSubscriptionIndex(getPositionOf(lastAdSeen.getCategory()));
                try{
                    Variables.setCurrentAdInSubscription(Integer.parseInt(lastAdSeen.getPushId()));
                }catch (Exception e){
                    e.printStackTrace();
                    Variables.setCurrentAdInSubscription(lastAdSeen.getPushIdNumber());
                }
                if(lastAdSeen.isFlagged())lastAdSeen.setWebsiteLink(igsNein);
                if(lastAdSeen.getWebsiteLink().equals(igsNein)){
                    findViewById(R.id.smallDot).setVisibility(View.INVISIBLE);
                }else{
                    findViewById(R.id.smallDot).setVisibility(View.VISIBLE);
                }
                Toast.makeText(mContext, R.string.lastAd, Toast.LENGTH_SHORT).show();
                isLastAd = true;
            }else {
                Advert noAds = new Advert();
                noAds.setWebsiteLink(igsNein);
                noAds.setCategory("NoAds");
                mSwipeView.addView(new AdvertCard(mContext, noAds, mSwipeView, Constants.NO_ADS));
                Variables.adToVariablesAdList(noAds);
                Variables.setIsLastOrNotLast(Constants.NO_ADS);
                findViewById(R.id.WebsiteIcon).setAlpha(0.4f);
                findViewById(R.id.websiteText).setAlpha(0.4f);
                findViewById(R.id.smallDot).setVisibility(View.INVISIBLE);
//                loadAnyAnnouncements();
            }
            Variables.isLockedBecauseOfNoMoreAds = true;
            loadAnyAnnouncements();
        }
        mAvi.setVisibility(View.GONE);
        mLoadingText.setVisibility(View.GONE);
        mBottomNavButtons.setVisibility(View.VISIBLE);

        Log.d(TAG, "---Setting up On click listeners...");
        onclicks();
        Log.d(TAG,"Todays ad total is : "+Variables.getAdTotal(mKey));
        Log.d(TAG,"The month Ad Total is : "+Variables.getMonthAdTotals(mKey));
        networkStateReceiver = new NetworkStateReceiver();
        networkStateReceiver.addListener(this);
        this.registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
        if(loadMoreAds) loadMoreAds();
    }




    private void registerReceivers() {
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverForAddingToSharedPreferences, new IntentFilter(Constants.ADD_TO_SHARED_PREFERENCES));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverForConnectionOffline, new IntentFilter(Constants.CONNECTION_OFFLINE));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverForConnectionOnline, new IntentFilter(Constants.CONNECTION_ONLINE));

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverForLastAd, new IntentFilter(Constants.LAST));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverForLoadMoreAds, new IntentFilter(Constants.LOAD_MORE_ADS));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverForTimerHasStarted, new IntentFilter(Constants.ADVERT_CARD_BROADCAST_TO_START_TIMER));

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverForOnSwiped, new IntentFilter("SWIPED"));

    }

    private void onclicks() {
        findViewById(R.id.logoutBtn).setOnClickListener(this);
        findViewById(R.id.WebsiteIcon).setOnClickListener(this);
        if (findViewById(R.id.bookmark2Btn) != null) {
            findViewById(R.id.bookmark2Btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Variables.mIsLastOrNotLast.equals(Constants.NOT_LAST) || Variables.mIsLastOrNotLast.equals(Constants.LAST) && isSeingNormalAds) {
                        if (!Variables.hasBeenPinned) {
                            Snackbar.make(findViewById(R.id.mainCoordinatorLayout), R.string.pinning,
                                    Snackbar.LENGTH_SHORT).show();
                            pinAd();
                        } else {
                            Snackbar.make(findViewById(R.id.mainCoordinatorLayout), R.string.hasBeenPinned,
                                    Snackbar.LENGTH_SHORT).show();
                        }
                    } else {
                        Snackbar.make(findViewById(R.id.mainCoordinatorLayout), "You can't pin that..",
                                Snackbar.LENGTH_SHORT).show();
                    }

                }
            });
        }
        findViewById(R.id.bookmarkBtn).setOnClickListener(this);

        if (findViewById(R.id.profileImageView) != null) {
            findViewById(R.id.profileImageView).setOnClickListener(this);

            findViewById(R.id.profileImageView).setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    findViewById(R.id.reportBtn).callOnClick();
                    return false;
                }
            });
        }


        findViewById(R.id.dashboard).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Dashboard.class);
                startActivity(intent);
            }
        });


        findViewById(R.id.shareBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vibrator s = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                s.vibrate(50);
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "Hey, check out this cool new app called The AdCafé.");
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.shareText)));
            }
        });

        if (findViewById(R.id.reportBtn) != null) {
            findViewById(R.id.reportBtn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Variables.mIsLastOrNotLast == Constants.NO_ADS || !isSeingNormalAds) {
                        Snackbar.make(findViewById(R.id.mainCoordinatorLayout), "You can't report that..",
                                Snackbar.LENGTH_SHORT).show();
                    } else {
                        FragmentManager fm = getFragmentManager();
                        ReportDialogFragment reportDialogFragment = new ReportDialogFragment();
                        reportDialogFragment.setMenuVisibility(false);
                        reportDialogFragment.show(fm, "Report dialog fragment.");
                        reportDialogFragment.setfragcontext(mContext);
                    }

                }
            });
        }

        if (findViewById(R.id.shareImageIcon) != null) {
            findViewById(R.id.shareImageIcon).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Variables.mIsLastOrNotLast.equals(Constants.NO_ADS) || !isSeingNormalAds) {
                        Snackbar.make(findViewById(R.id.mainCoordinatorLayout), "You can't share that..",
                                Snackbar.LENGTH_SHORT).show();
                    } else {
                        Variables.adToBeShared = Variables.getCurrentAdvert();
                        isStoragePermissionGranted();
                    }
                }
            });
        }


    }

    @Override public void onClick(View v) {
        if(v==findViewById(R.id.profileImageView)){
            if(mDoublePressedToPin) {
//                findViewById(R.id.bookmark2Btn).callOnClick();
            }else{
                mSwipeView.doSwipe(true);
            }
            mDoublePressedToPin = true;
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    mDoublePressedToPin=false;
                }
            }, 1000);
        }

        if(v == retryLoadingFromCannotLoad){
            if(isOnline()){
                cannotLoadLayout.setVisibility(View.GONE);
                loadAdsFromThread();
            }else{
                Toast.makeText(mContext,"Connect to the internet first.",Toast.LENGTH_SHORT).show();
            }
        }

        if (v == mLogoutButton) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Are you sure you want to log out?")
                    .setCancelable(true)
                    .setPositiveButton("Yes.", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            logoutUser();
                        }
                    })
                    .setNegativeButton("No!!", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    }).show();
        }
        if (v == mRetryButton) {
            Log.d(TAG, "Retrying to load ads...");
            mAvi.setVisibility(View.VISIBLE);
            mLoadingText.setVisibility(View.VISIBLE);
            mFailedToLoadLayout.setVisibility(View.GONE);
            Toast.makeText(mContext, "Retrying...", Toast.LENGTH_SHORT).show();
            loadAdsFromThread();
        }
        if (v == findViewById(R.id.WebsiteIcon) && Variables.getCurrentAdvert() != null) {
            if (!Variables.getCurrentAdvert().getWebsiteLink().equals(igsNein) && !Variables.getCurrentAdvert().isFlagged()) {
                Vibrator b = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                b.vibrate(30);
                try {
                    String url = Variables.getCurrentAdvert().getWebsiteLink();
                    if (!url.startsWith("http://") && !url.startsWith("https://")) url = "http://" + url;
                    Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(webIntent);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(mContext, "There's something wrong with the link", Toast.LENGTH_SHORT).show();
                }
            }
        }
        if(v==findViewById(R.id.bookmarkBtn)){
            Intent intent = new Intent(MainActivity.this, Bookmarks.class);
            startActivity(intent);
        }

    }

    private void setIsUserLoggedOnInSharedPrefs(boolean bol){
        SharedPreferences pref = getApplicationContext().getSharedPreferences("IsSignedIn", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("isSignedIn",bol);
        editor.apply();
    }

    private void logoutUser() {
        setLastUsedDateInFirebaseDate(User.getUid());
        if (dbRef != null) {
            dbRef.removeEventListener(val);
        }
        User.setID(0, mKey);
        unregisterAllReceivers();
        if (FirebaseAuth.getInstance() != null) {
            FirebaseAuth.getInstance().signOut();
        }
        setIsUserLoggedOnInSharedPrefs(false);

        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }




    private BroadcastReceiver mMessageReceiverForAddingToSharedPreferences = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("COUNTER_BAR_TO_MAIN- ", "Broadcast has been received to add to shared preferences.");
            updateData();

        }
    };

    private void updateData() {
        Variables.adAdToTotal(mKey);
        Variables.adToMonthTotals(mKey);
        Variables.adOneToCurrentAdNumberForAllAdsList();
        addToSharedPreferences();
        adDayAndMonthTotalsToFirebase();
        onclicks();

        getNumberOfTimesAndSetNewNumberOfTimes();
        getAndSetAllAdsThatHaveBeenSeenEver();

        try{
            Variables.setCurrentAdInSubscription(Integer.parseInt(Variables.getCurrentAdvert().getPushId()));
        }catch (Exception e){
            e.printStackTrace();
            Log.d(TAG,"Something went wrong setting the current ad in subscription");
            Log.d(TAG,"Setting using the getPushIdNumber instead. Number is : "+Variables.getCurrentAdvert().getPushIdNumber());
            Variables.setCurrentAdInSubscription(Variables.getCurrentAdvert().getPushIdNumber());
        }
        Variables.setCurrentSubscriptionIndex(getPositionOf(Variables.getCurrentAdvert().getCategory()));
        Log.d(TAG, "Setting current subscription to : " + getPositionOf(Variables.getCurrentAdvert().getCategory()));
        Log.d(TAG, "Setting Current ad in subscription to : " + Variables.getCurrentAdvert().getPushId());
        setCurrentAdInSubscriptionAndCurrentSubscriptionIndexInFireBase();

        if(Variables.didAdCafeRemoveCategory)informUserOfSubscriptionChanges();
        if(Variables.didAdCafeAddNewCategory) tellUserOfNewSubscription();
    }


    private BroadcastReceiver mMessageReceiverForConnectionOffline = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("CONNECTION_C-MAIN_A", "Connection has been dropped");
            Snackbar.make(findViewById(R.id.mainCoordinatorLayout), R.string.connectionDropped2,
                    Snackbar.LENGTH_INDEFINITE).show();
        }
    };

    private BroadcastReceiver mMessageReceiverForConnectionOnline = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("CONNECTION_C-MAIN_A", "Connection has come online");
        }
    };

    private BroadcastReceiver mMessageReceiverForLastAd = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!mIsBeingReset && !isLoadingMoreAds) {
                Toast.makeText(mContext, R.string.lastAd, Toast.LENGTH_SHORT).show();
                loadAnyAnnouncements();
            }
        }
    };

    private BroadcastReceiver mMessageReceiverForTimerHasStarted = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!Variables.getCurrentAdvert().getWebsiteLink().equals(igsNein)) {
                        mSwipeView.findViewById(R.id.WebsiteIcon).setAlpha(1.0f);
                        mSwipeView.findViewById(R.id.websiteText).setAlpha(1.0f);
                        mSwipeView.findViewById(R.id.smallDot).setVisibility(View.VISIBLE);
                    } else {
                        findViewById(R.id.WebsiteIcon).setAlpha(0.4f);
                        findViewById(R.id.websiteText).setAlpha(0.4f);
                        findViewById(R.id.smallDot).setVisibility(View.INVISIBLE);
                        Variables.hasBeenPinned = false;
                    }
                    onclicks();
                }
            }, 300);

        }
    };

    private BroadcastReceiver mMessageReceiverForOnSwiped = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(Variables.getCurrentAdNumberForAllAdsList()+1<Variables.getSizeOfAdlist()){
                        if(Variables.getAdFromVariablesAdList(Variables.getCurrentAdNumberForAllAdsList()+1)
                                .getNatureOfBanner().equals(Constants.IS_ANNOUNCEMENT)){
                            findViewById(R.id.WebsiteIcon).setAlpha(0.3f);
                            findViewById(R.id.websiteText).setAlpha(0.3f);
                            findViewById(R.id.smallDot).setVisibility(View.INVISIBLE);

                            findViewById(R.id.bookmark2Btn).setAlpha(0.3f);
                            findViewById(R.id.reportBtn).setAlpha(0.3f);
                            isSeingNormalAds = false;
                            onclicks();
                        }
                    }
                }
            }, 300);

        }
    };

    private BroadcastReceiver mMessageReceiverForLoadMoreAds = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!mIsBeingReset && !isLoadingMoreAds && Variables.nextSubscriptionIndex + 1 < Variables.Subscriptions.size()) {
//                Variables.nextSubscriptionIndex += 1;
                loadMoreAds();
            }
        }
    };


    private void loadMoreAds() {
        isLoadingMoreAds = true;
        mAviLoadingMoreAds.smoothToShow();
//        spinner.setVisibility(View.VISIBLE);
        Log.d("MAIN-ACTIVITY---", "Loading more ads since user has seen almost all....");
        String date;
        date = isAlmostMidNight() ? getNextDay() : getDate();

        Query query = FirebaseDatabase.getInstance().getReference(Constants.ADVERTS).child(date)
                .child(getSubscriptionValue(Variables.nextSubscriptionIndex))
                .child(Integer.toString(getClusterValue(Variables.nextSubscriptionIndex)));


        Log.d(TAG, "---Query set up is : " + Constants.ADVERTS + " : " + date + " : "
                + getSubscriptionValue(Variables.nextSubscriptionIndex)
                + " : "
                + Integer.toString(getClusterValue(Variables.nextSubscriptionIndex)));


        dbRef = query.getRef();
        Log.d(TAG,"Dbref starts at "+(mChildToStartFrom + 1));
        dbRef.orderByKey().startAt(Integer.toString(mChildToStartFrom + 1))
                .limitToFirst(5).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    Log.d(TAG, "---More children in dataSnapshot from firebase exist");
                    for (DataSnapshot snap : dataSnapshot.getChildren()) {
                        Advert ad = snap.getValue(Advert.class);
                        DataSnapshot snpsht = snap.child("pushId");
                        String pushID = snpsht.getValue(String.class);
                        ad.setPushId(pushID);
                        ad.setPushIdNumber(Integer.parseInt(pushID));
                        Log.d(TAG,"setting push id to : "+ ad.getPushId());
                        if(!ad.isFlagged()) {
                            mAdList.add(ad);
                            Log.d(TAG,"Loaded ad : "+ad.getPushRefInAdminConsole());
                        }
                    }
                    Log.d(TAG, "---All the new ads have been handled.Total is " + mAdList.size());
                    if(mAdList.size()!=0){
                        loadMoreAdsIntoAdvertCard();
                        mChildToStartFrom += (int) dataSnapshot.getChildrenCount();
                        isLoadingMoreAds = false;
//                        mAviLoadingMoreAds.smoothToHide();
//                        spinner.setVisibility(View.VISIBLE);
//                        if(Variables.isLockedBecauseOfNoMoreAds){
//                            mSwipeView.unlockViews();
//                            Variables.isLockedBecauseOfNoMoreAds = false;
//                        }
                    }else{
                        Log.d(TAG,"Loaded no ad, loading more ads...");
                        if(Variables.nextSubscriptionIndex+1<Variables.Subscriptions.size()){
                            mChildToStartFrom=0;
                            Variables.nextSubscriptionIndex+=1;
                            loadMoreAds();
                        }else{
                            Log.d(TAG,"No more ads are available from the rest of the subscriptions");
                            isLoadingMoreAds = false;
                            mAviLoadingMoreAds.smoothToHide();
                            if(mSwipeView.getChildCount()==1){
                                Toast.makeText(mContext, R.string.lastAd, Toast.LENGTH_SHORT).show();
                                loadAnyAnnouncements();
                            }
//                            spinner.setVisibility(View.VISIBLE);
                        }
                    }
                } else {
                    //no ads were found in the subscription
                    Log.d(TAG, "----No ads are available in subscription: "+getSubscriptionValue(Variables.nextSubscriptionIndex));
                    if(Variables.nextSubscriptionIndex+1<Variables.Subscriptions.size()){
                        mChildToStartFrom=0;
                        Variables.nextSubscriptionIndex+=1;
                        loadMoreAds();
                    }else{
                        Log.d(TAG,"No more ads are available from the rest of the subscriptions");
                        isLoadingMoreAds = false;
                        mAviLoadingMoreAds.smoothToHide();
                        if(mSwipeView.getChildCount()==1){
                            Toast.makeText(mContext, R.string.lastAd, Toast.LENGTH_SHORT).show();
                            loadAnyAnnouncements();
                        }
//                        spinner.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "Unable to load more ads for some issue." + databaseError.getMessage());
            }
        });
    }

    private void loadMoreAdsIntoAdvertCard(){
        String date = isAlmostMidNight() ? getNextDay() : getDate();
        for(final Advert ad: mAdList){
            String pushRefInAdminConsole = ad.getPushRefInAdminConsole();
            DatabaseReference adRef = FirebaseDatabase.getInstance().getReference(Constants.ADS_FOR_CONSOLE).child(date)
                    .child(pushRefInAdminConsole).child("imageUrl");
            adRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String imageUrl = dataSnapshot.getValue(String.class);
                    mAdList.get(mAdList.indexOf(ad)).setImageUrl(imageUrl);
                    iterations++;
                    if(iterations == mAdList.size()){
                        iterations = 0;
                        loadMoreAdsIntoAdvertCard2();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d(TAG,"An error occured, "+databaseError.getDetails());
                }
            });
        }
    }

    private void loadMoreAdsIntoAdvertCard2() {
        for (Advert ad : mAdList) {
            ad.setNatureOfBanner(Constants.IS_AD);
            mSwipeView.addView(new AdvertCard(mContext, ad, mSwipeView, Constants.LOAD_MORE_ADS));
            Variables.adToVariablesAdList(ad);
            Variables.setIsLastOrNotLast(Constants.NOT_LAST);
        }
        if(Variables.isLockedBecauseOfNoMoreAds){
            mSwipeView.unlockViews();
            Variables.isLockedBecauseOfNoMoreAds = false;
        }
        mAviLoadingMoreAds.smoothToHide();
        mAdList.clear();
    }

    private void loadAnyAnnouncements() {
        if(!hasLoadedAnnouncements){
            hasLoadedAnnouncements = true;
            mAviLoadingMoreAds.smoothToShow();
            Log.d("MAIN-ACTIVITY---", "Now loading announcements since there are no more ads....");
            String date = isAlmostMidNight() ? getNextDay() : getDate();
            Query query = FirebaseDatabase.getInstance().getReference(Constants.ANNOUNCEMENTS).child(date);

            Log.d(TAG, "---Query set up is : " + Constants.ANNOUNCEMENTS + " : " + date);
            dbRef = query.getRef();
            dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChildren()) {
                        for (DataSnapshot snap : dataSnapshot.getChildren()) {
                            Advert ad = snap.getValue(Advert.class);
                            DataSnapshot imgSnap = snap.child("imageUrl");
                            String img = imgSnap.getValue(String.class);
                            ad.setImageUrl(img);
                            mAdList.add(ad);
                        }
                        for (Advert ad : mAdList) {
                            ad.setWebsiteLink(igsNein);
                            ad.setNatureOfBanner(Constants.IS_ANNOUNCEMENT);
                            Variables.adToVariablesAdList(ad);
                            mSwipeView.addView(new AdvertCard(mContext, ad, mSwipeView, Constants.ANNOUNCEMENTS));
                        }
                        if(Variables.isLockedBecauseOfNoMoreAds){
                            mSwipeView.unlockViews();
                            Log.d(TAG,"Unlocking views since isLockedBecauseOfNoMoreAds is : "+Variables.isLockedBecauseOfNoMoreAds);
                            Variables.isLockedBecauseOfNoMoreAds = false;
                        }
                        mAviLoadingMoreAds.smoothToHide();
                        mAdList.clear();
                    } else {
                        mAviLoadingMoreAds.smoothToHide();
                        Log.d(TAG, "There are no announcements today...");
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d(TAG, "Unable to load announcements...");
                    hasLoadedAnnouncements = false;
                }
            });
        }
    }

    private void hideNavBars() {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        decorView.setSystemUiVisibility(uiOptions);
    }



    public float density() {
        double constant = 0.000046875;
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int density = metrics.densityDpi;
        float relativeScale;

        if (density >= 560) {
            Log.d("DENSITY---", "HIGH... Density is " + String.valueOf(density));
            relativeScale = 0.005f;
        } else if (density >= 460) {
            Log.d("DENSITY---", "MEDIUM-HIGH... Density is " + String.valueOf(density));
            relativeScale = 0.009f;
        } else if (density >= 360) {
            Log.d("DENSITY---", "MEDIUM-LOW... Density is " + String.valueOf(density));
            relativeScale = 0.013f;
        } else if (density >= 260) {
            Log.d("DENSITY---", "LOW... Density is " + String.valueOf(density));
            relativeScale = 0.015f;
        } else {
            relativeScale = 0.02f;
        }
        return relativeScale;
    }

    private void addToSharedPreferences() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Constants.AD_TOTAL, MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.putInt("adTotals", Variables.getAdTotal(mKey));
        Log.d("MAIN_ACTIVITY--", "Adding 1 to shared preferences adTotal is - " + Integer.toString(Variables.getAdTotal(mKey)));
        editor.commit();

        SharedPreferences pref2 = getApplicationContext().getSharedPreferences(Constants.TOTAL_NO_OF_ADS_SEEN_All_MONTH, MODE_PRIVATE);
        SharedPreferences.Editor editor2 = pref2.edit();
        editor2.clear();
        editor.putInt(Constants.TOTAL_NO_OF_ADS_SEEN_All_MONTH, Variables.getMonthAdTotals(mKey));
        Log.d("MAIN_ACTIVITY--", "Adding 1 to shared preferences Month ad totals is - " + Integer.toString(Variables.getMonthAdTotals(mKey)));
        editor2.commit();
    }

    private boolean isAlmostMidNight() {
        Calendar c = Calendar.getInstance();
        int hours = c.get(Calendar.HOUR_OF_DAY);
        int minutes = c.get(Calendar.MINUTE);
        int seconds = c.get(Calendar.SECOND);

        Log.d(TAG, "Current time is " + hours + ":" + minutes + ":" + seconds);
        if (hours == 23 && (minutes == 59) && (seconds >= 0)) {
            Log.d(TAG, "---Day is approaching midnight,returning true to reset the activity and values. Time is:" + hours + " : " + minutes + " : " + seconds);
            return true;
        } else {
            Log.d(TAG, "---Day is not approaching midnight,so activity will continue normally.");
            return false;
        }
    }



    private void adDayAndMonthTotalsToFirebase() {
        String uid = User.getUid();
        DatabaseReference adRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS).child(uid).child(Constants.TOTAL_NO_OF_ADS_SEEN_TODAY);
        adRef.setValue(Variables.getAdTotal(mKey));

        DatabaseReference adRef2 = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS).child(uid).child(Constants.TOTAL_NO_OF_ADS_SEEN_All_MONTH);
        adRef2.setValue(Variables.getMonthAdTotals(mKey));

    }

    private void setCurrentAdInSubscriptionAndCurrentSubscriptionIndexInFireBase() {
        String uid = User.getUid();
        DatabaseReference adRef3 = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.CURRENT_SUBSCRIPTION_INDEX);
        Log.d(TAG,"Setting current subscription index in firebase to :"+Variables.getCurrentSubscriptionIndex());
        adRef3.setValue(Variables.getCurrentSubscriptionIndex());

        DatabaseReference adRef4 = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.CURRENT_AD_IN_SUBSCRIPTION);
        Log.d(TAG,"Setting current ad in subscription index in firebase to : "+Variables.getCurrentAdInSubscription());
        adRef4.setValue(Variables.getCurrentAdInSubscription());
    }

    private String getDate() {
        long date = System.currentTimeMillis();
        SimpleDateFormat sdfMonth = new SimpleDateFormat("MM");
        String MonthString = sdfMonth.format(date);

        SimpleDateFormat sdfDay = new SimpleDateFormat("dd");
        String dayString = sdfDay.format(date);

        SimpleDateFormat sdfYear = new SimpleDateFormat("yyyy");
        String yearString = sdfYear.format(date);

        Calendar c = Calendar.getInstance();
        String yy = Integer.toString(c.get(Calendar.YEAR));
        String mm = Integer.toString(c.get(Calendar.MONTH) + 1);
        String dd = Integer.toString(c.get(Calendar.DAY_OF_MONTH));

        String todaysDate = (dd + ":" + mm + ":" + yy);

        return todaysDate;
    }

    private void resetAdTotalSharedPreferencesAndDayAdTotals() {
        SharedPreferences prefs = getSharedPreferences(Constants.AD_TOTAL, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.commit();

        Variables.setAdTotal(0, mKey);
        Variables.setCurrentAdInSubscription(0);
        Variables.setCurrentSubscriptionIndex(0);
        resetAdTotalsInFirebase();
    }



    private void setLastUsedDateInFirebaseDate(String uid) {
        DatabaseReference adRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS).child(uid).child(Constants.DATE_IN_FIREBASE);
        adRef.setValue(getDate());
    }

    private void resetAdTotalsInFirebase() {
        String uid = User.getUid();
        DatabaseReference adRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS).child(uid).child(Constants.TOTAL_NO_OF_ADS_SEEN_TODAY);
        adRef.setValue(0).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (isOffline) {
                    isFirebaseResetNecessary = true;
                }
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                isFirebaseResetNecessary = false;
            }
        });

        DatabaseReference adRef3 = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.CURRENT_SUBSCRIPTION_INDEX);
        adRef3.setValue(0);

        DatabaseReference adRef4 = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.CURRENT_AD_IN_SUBSCRIPTION);
        adRef4.setValue(0);
    }

    private void resetEverything() {
        resetAdTotalSharedPreferencesAndDayAdTotals();
        Variables.clearAllAdsFromAdList();
        lastAdSeen = null;
        loadAdsFromThread();
    }




    private String getNextDay() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, 1);
        String yy = Integer.toString(c.get(Calendar.YEAR));
        String mm = Integer.toString(c.get(Calendar.MONTH) + 1);
        String dd = Integer.toString(c.get(Calendar.DAY_OF_MONTH));

        String tomorrowsDate = (dd + ":" + mm + ":" + yy);

        Log.d(TAG, "Tomorrows date is : " + tomorrowsDate);
        return tomorrowsDate;

    }

    private void setCurrentDateToSharedPrefs() {
        Log.d(TAG, "---Setting current date in shared preferences.");
        SharedPreferences prefs = getSharedPreferences(Constants.DATE, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        if(isAlmostMidNight()) editor.putString("date",getNextDay());
        else editor.putString("date", getDate());
        editor.apply();
    }

    private String getCurrentDateInSharedPreferences() {
        Log.d(TAG, "---Getting current date in shared preferences.");
        SharedPreferences prefs = getSharedPreferences(Constants.DATE, MODE_PRIVATE);
        String date = prefs.getString("date", "0");
        return date;
    }



    @Override public void networkAvailable() {
        Log.d(TAG, "User is connected to the internet via wifi or cellular data");
        isOffline = false;
        if(stage.equals("VIEWING_ADS")){
            //Sets these views if activity has already loaded the ads.
            findViewById(R.id.droppedInternetLayout).setVisibility(View.GONE);
            mBottomNavButtons.setVisibility(View.VISIBLE);
            mSwipeView.setVisibility(View.VISIBLE);
            mAdCounterView.setVisibility(View.VISIBLE);
            if (isFirebaseResetNecessary) {
                resetAdTotalsInFirebase();
            }
        }
//        else{
            //Sets these views if activity has already loaded the ads.
//            findViewById(R.id.droppedInternetLayout).setVisibility(View.GONE);
//            mBottomNavButtons.setVisibility(View.VISIBLE);
//            mSwipeView.setVisibility(View.VISIBLE);
//            mAdCounterView.setVisibility(View.VISIBLE);
//            if (isFirebaseResetNecessary) {
//                resetAdTotalsInFirebase();
//            }
//        }
//
//        findViewById(R.id.droppedInternetLayout).setVisibility(View.GONE);
//        mBottomNavButtons.setVisibility(View.VISIBLE);
//        mSwipeView.setVisibility(View.VISIBLE);
//        mAdCounterView.setVisibility(View.VISIBLE);
//        if (isFirebaseResetNecessary) {
//            resetAdTotalsInFirebase();
//        }
    }

    @Override public void networkUnavailable() {
        Log.d(TAG, "User has gone offline...");
        isOffline = true;
        if(stage.equals("VIEWING_ADS")){
            mBottomNavButtons.setVisibility(View.GONE);
            mSwipeView.setVisibility(View.GONE);
            mAdCounterView.setVisibility(View.GONE);
            findViewById(R.id.droppedInternetLayout).setVisibility(View.VISIBLE);
        }
//        else{
//            mBottomNavButtons.setVisibility(View.GONE);
//            mSwipeView.setVisibility(View.GONE);
//            mAdCounterView.setVisibility(View.GONE);
//            findViewById(R.id.droppedInternetLayout).setVisibility(View.VISIBLE);
//        }
//        mBottomNavButtons.setVisibility(View.GONE);
//        mSwipeView.setVisibility(View.GONE);
//        mAdCounterView.setVisibility(View.GONE);
//        findViewById(R.id.droppedInternetLayout).setVisibility(View.VISIBLE);

    }

    @Override public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Press BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }



    private void pinAd() {
        Log.d(TAG, "Pinning ad from main activity");
        Advert ad = Variables.getCurrentAdvert();
        String uid = User.getUid();
        DatabaseReference adRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.PINNED_AD_LIST).child(getDateInDays());

        DatabaseReference pushRef = adRef.push();
        String pushId = pushRef.getKey();

        Log.d(TAG, "pinning the selected ad.");
        ad.setImageBitmap(null);
        ad.setPushId(pushId);

        long currentTimeMillis = System.currentTimeMillis();
        long currentDay = -(currentTimeMillis+1000*60*60*3)/(1000*60*60*24);

        ad.setDateInDays(currentDay);

        DatabaseReference adRef2 = FirebaseDatabase.getInstance().getReference(Constants.PINNED_AD_POOL)
                .child(getDateInDays()).child(ad.getPushRefInAdminConsole());

        adRef2.setValue(ad.getImageUrl());
        ad.setImageUrl(null);
        pushRef.setValue(ad).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d(TAG, "Pinning is complete.");
                Variables.hasBeenPinned = true;
            }
        });
    }

    private void logUser() {
        Crashlytics.setUserIdentifier(User.getUid());
        Crashlytics.setUserEmail(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        Crashlytics.setUserName("Test User");
    }

    private void getNumberOfTimesAndSetNewNumberOfTimes() {
        Log.d(TAG, "Getting the current ad's numberOfTimesSeen from firebase");
        final String datte;
        //ad gotten will be previous since the broadcast receiver added one to currentAdNumberForAllAdsList
        final Advert ad = Variables.getAdFromVariablesAdList(Variables.getCurrentAdNumberForAllAdsList() - 1);
        datte = isAlmostMidNight() ? getNextDay() : getDate();

        Log.d(TAG, "Push ref for current Advert is : " + ad.getPushRefInAdminConsole());
        Query query = FirebaseDatabase.getInstance().getReference(Constants.ADS_FOR_CONSOLE)
                .child(datte)
                .child(ad.getPushRefInAdminConsole())
                .child("numberOfTimesSeen");
        Log.d(TAG, "Query set up is :" + Constants.ADS_FOR_CONSOLE + " : " + datte + " : " + ad.getPushRefInAdminConsole() + " : numberOfTimesSeen");
        DatabaseReference dbRef = query.getRef();
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    int number = dataSnapshot.getValue(int.class);
                    int newNumber = number + 1;
                    setNewNumberOfTimesSeen(newNumber, datte, ad);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "Unable to get number of times seen");
            }
        });
    }

    private void setNewNumberOfTimesSeen(int number, String date, Advert advert) {
        Log.d(TAG, "Setting the new number of times seen in firebase.");
        Query query = FirebaseDatabase.getInstance().getReference(Constants.ADS_FOR_CONSOLE)
                .child(date).child(Variables.getCurrentAdvert().getPushRefInAdminConsole()).child("numberOfTimesSeen");

        Log.d(TAG, "Query set up is :" + Constants.ADS_FOR_CONSOLE + " : " + date + " : " + advert.getPushRefInAdminConsole() + " : numberOfTimesSeen");
        DatabaseReference dbRef = query.getRef();
        dbRef.setValue(number).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "The new number has been set.");
            }
        });
    }


//    public static void scheduleRepeatingRTCNotification(Context context, String hour, String min) {
//        //get calendar instance to be able to select what time notification should be scheduled
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTimeInMillis(System.currentTimeMillis());
//        //Setting time of the day (8am here) when notification will be sent every day (default)
//        calendar.set(Calendar.HOUR_OF_DAY,
//                Integer.getInteger(hour, 8),
//                Integer.getInteger(min, 0));
//
//        //Setting intent to class where Alarm broadcast message will be handled
//        Intent intent = new Intent(context, AlarmReceiver.class);
//        //Setting alarm pending intent
//        PendingIntent alarmIntentRTC = PendingIntent.getBroadcast(context, 100, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//
//        //getting instance of AlarmManager service
//        AlarmManager alarmManagerRTC = (AlarmManager)context.getSystemService(ALARM_SERVICE);
//        //Setting alarm to wake up device every day for clock time.
//        //AlarmManager.RTC_WAKEUP is responsible to wake up device for sure, which may not be good practice all the time.
//        // Use this when you know what you're doing.
//        //Use RTC when you don't need to wake up device, but want to deliver the notification whenever device is woke-up
//        //We'll be using RTC.WAKEUP for demo purpose only
//        alarmManagerRTC.setInexactRepeating(AlarmManager.RTC, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, alarmIntentRTC);
//    }

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted");
                shareImage(Variables.getCurrentAdvert().getImageBitmap());
                return true;
            } else {

                Log.v(TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted");
            shareImage(Variables.adToBeShared.getImageBitmap());
            return true;
        }
    }

    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.v(TAG, "Permission: " + permissions[0] + "was " + grantResults[0]);
            //resume tasks needing this permission
            shareImage(Variables.adToBeShared.getImageBitmap());
        }
    }

    private void shareImage(Bitmap icon) {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(30);
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("image/jpeg");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        icon.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        File f = new File(Environment.getExternalStorageDirectory() + File.separator + "temporary_file.jpg");
        try {
            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
        share.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:///sdcard/temporary_file.jpg"));
        startActivity(Intent.createChooser(share, "Share Image"));
    }




    private void getAndSetAllAdsThatHaveBeenSeenEver() {
        Query query = FirebaseDatabase.getInstance().getReference(Constants.TOTAL_ALL_TIME_ADS);
        DatabaseReference dbRef = query.getRef();
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long number;
                if (dataSnapshot.getValue(long.class) != null)
                    number = dataSnapshot.getValue(long.class);
                else number = 0;
                Log.d(TAG, "number gotten for global ad totals is : " + number);
                long newNumber = number + 1;
                setNewAllAdsThatHaveBeenSeenEver(newNumber);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "Unable to update totals");
            }
        });
    }

    private void setNewAllAdsThatHaveBeenSeenEver(long number) {
        Query query = FirebaseDatabase.getInstance().getReference(Constants.TOTAL_ALL_TIME_ADS);
        DatabaseReference dbRef = query.getRef();
        dbRef.setValue(number).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "New value has been set");
            }
        });
    }

    private void setAlarmForNotifications() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent alarmIntent = new Intent(mContext, AlarmReceiver1.class); // AlarmReceiver1 = broadcast receiver

        PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmIntent.setData((Uri.parse("custom://" + System.currentTimeMillis())));
        try{
            alarmManager.cancel(pendingIntent);
        }catch(Exception e){
            e.printStackTrace();
        }

        Calendar alarmStartTime = Calendar.getInstance();
        Calendar now = Calendar.getInstance();
        alarmStartTime.set(Calendar.HOUR_OF_DAY, 4);
        alarmStartTime.set(Calendar.MINUTE, 21);
        alarmStartTime.set(Calendar.SECOND, 0);
        if (now.after(alarmStartTime)) {
            Log.d(TAG, "Setting alarm to tomorrow morning.");
            alarmStartTime.add(Calendar.DATE, 1);
        }
        try {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alarmStartTime.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
            Log.w("Alarm", "Alarms set for everyday 04:15 hrs.");
        }catch (Exception e){
            e.printStackTrace();
        }

    }




    private void RateAppIntent() {
        try {
            Intent rateIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + mContext.getPackageName()));
            startActivity(rateIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private int getClusterValue(int index) {
        LinkedHashMap map = Variables.Subscriptions;
        int cluster = (new ArrayList<Integer>(map.values())).get(index);
        Log.d(TAG, "Cluster gotten from current subscription is : " + cluster);
        return cluster;
    }

    private String getSubscriptionValue(int index) {
        LinkedHashMap map = Variables.Subscriptions;
        String Sub = (new ArrayList<String>(map.keySet())).get(index);
        Log.d(TAG, "Subscription gotten from getCurrent Subscription method is :" + Sub);
        return Sub;
    }




    private int getPositionOf(String subscription) {
        LinkedHashMap map = Variables.Subscriptions;
        List<String> indexes = new ArrayList<String>(map.keySet());
        return indexes.indexOf(subscription);
    }

    private String getDateInDays(){
        long currentTimeMillis = System.currentTimeMillis();
        long currentDay = (currentTimeMillis+1000*60*60*3)/(1000*60*60*24);
        Log.d(TAG,"The current day is : "+currentDay);
        return Long.toString(-currentDay);
    }

    private void informUserOfSubscriptionChanges(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("We removed one or more of your interests that we no longer support.")
                .setCancelable(true)
                .setPositiveButton("Cool.", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Variables.didAdCafeRemoveCategory = false;
                        dialog.cancel();
                    }
                }).show();
    }




    private void tellUserOfNewSubscription(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("We now support a couple more ad categories you may be interested in.")
                .setCancelable(true)
                .setPositiveButton("Cool.", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Variables.didAdCafeAddNewCategory = false;
                        dialog.cancel();
                    }
                }).show();
    }

    private String getDateFromDays(long days){
        long currentTimeInMills = -days*(1000*60*60*24);

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(currentTimeInMills);
        int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
        int monthOfYear = cal.get(Calendar.MONTH);
        int year = cal.get(Calendar.YEAR);

//        String monthName = new DateFormatSymbols().getMonths()[monthOfYear];
        String monthName = getMonthName_Abbr(monthOfYear);

        Log.d(TAG,"Date gotten is : "+dayOfMonth+" "+monthName+" "+year);

        Calendar cal2 = Calendar.getInstance();
        int year2 = cal2.get(Calendar.YEAR);
        String yearName;

        if(year == year2){
            Log.d(TAG,"Ad was pined this year...");
            yearName = "";
        }else if(year2 == year+1){
            Log.d(TAG,"Ad was pined last year...");
            yearName =", "+Integer.toString(year);
        }else{
            yearName =", "+ Integer.toString(year);
        }

        return dayOfMonth+" "+monthName+yearName;
    }

    private String getMonthName_Abbr(int month) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, month);
        SimpleDateFormat month_date = new SimpleDateFormat("MMM");
        String month_name = month_date.format(cal.getTime());
        return month_name;
    }


    public boolean isOnline() {
        Context context = getApplicationContext();
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        //should check null because in airplane mode it will be null
        return (netInfo != null && netInfo.isConnected());
    }

}
