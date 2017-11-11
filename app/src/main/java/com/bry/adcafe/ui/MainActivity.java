package com.bry.adcafe.ui;

import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.bry.adcafe.Constants;
import com.bry.adcafe.R;
import com.bry.adcafe.Variables;
import com.bry.adcafe.adapters.AdvertCard;
import com.bry.adcafe.adapters.AdCounterBar;
import com.bry.adcafe.fragments.ReportDialogFragment;
import com.bry.adcafe.models.Advert;
import com.bry.adcafe.models.User;
import com.bry.adcafe.services.NetworkStateReceiver;
import com.bry.adcafe.services.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mindorks.placeholderview.PlaceHolderView;
import com.mindorks.placeholderview.SwipeDecor;
import com.mindorks.placeholderview.SwipePlaceHolderView;
import com.wang.avi.AVLoadingIndicatorView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,NetworkStateReceiver.NetworkStateReceiverListener {
    private static final String TAG = "MainActivity";
    private LinearLayout mFailedToLoadLayout;
    private Button mRetryButton;
    private ImageButton mLogoutButton;
    private SwipePlaceHolderView mSwipeView;
    private PlaceHolderView mAdCounterView;
    private Context mContext;
    private String mKey = "";

    private List<Advert> mAdList = new ArrayList<>();
    private List<Advert> mFailedAdList = new ArrayList<>();
    private Runnable mViewRunnable;
    private LinearLayout mLinearLayout;
    private AVLoadingIndicatorView mAvi;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = getApplicationContext();
        Variables.isMainActivityOnline = true;
        registerReceivers();

        setUpSwipeView();
        loadAdsFromThread();
    }


    @Override
    protected void onStart(){
        super.onStart();
    }


    @Override
    protected void onResume(){
        Variables.isMainActivityOnline = true;
        super.onResume();
        if(!getCurrentDateInSharedPreferences().equals(getDate())){
            Log.d(TAG,"---Date in shared preferences does not match current date,therefore resetting everything.");
            resetEverything();
            sendBroadcastToUnregisterAllReceivers();
            removeAllViews();
        }
        if(isAlmostMidNight()&&Variables.isMainActivityOnline){
            resetEverything();
            sendBroadcastToUnregisterAllReceivers();
            removeAllViews();
        }
        r = new Runnable() {
            @Override
            public void run() {
                Log.d(TAG,"---started the time checker for when it is almost midnight.");
                if(isAlmostMidNight()&&Variables.isMainActivityOnline){
                    resetEverything();
                    sendBroadcastToUnregisterAllReceivers();
                    removeAllViews();
                }
                h.postDelayed(r,60000);
            }
        };
        h.postDelayed(r,60000);
    }

    @Override
    protected void onPause(){
        super.onPause();
        h.removeCallbacks(r);
        setCurrentTimeToSharedPrefs();
    }

    @Override
    protected void onStop(){
        super.onStop();
        if(dbRef!=null){
            dbRef.removeEventListener(val);
        }
        setUserDataInSharedPrefs();
        Log.d(TAG,"---removing callback for checking time of day.");
    }

    private void setUserDataInSharedPrefs() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("TodayTotals", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.putInt("TodaysTotals",Variables.getAdTotal(mKey));
        Log.d("MAIN_ACTIVITY--","Setting todays ad totals in shared preferences - "+Integer.toString(Variables.getAdTotal(mKey)));
        editor.apply();

        SharedPreferences pref2 = getApplicationContext().getSharedPreferences("MonthTotals", MODE_PRIVATE);
        SharedPreferences.Editor editor2 = pref2.edit();
        editor2.clear();
        editor2.putInt("MonthsTotals",Variables.getMonthAdTotals(mKey));
        Log.d("MAIN_ACTIVITY--","Setting the month totals in shared preferences - "+Integer.toString(Variables.getMonthAdTotals(mKey)));
        editor2.apply();

        SharedPreferences pref3 = getApplicationContext().getSharedPreferences("ClusterID", MODE_PRIVATE);
        SharedPreferences.Editor editor3 = pref3.edit();
        editor3.clear();
        editor3.putInt("Cluster",User.getClusterID(mKey));
        Log.d("MAIN_ACTIVITY--","Setting the users cluster id in shared preferences - "+Integer.toString(User.getClusterID(mKey)));
        editor3.apply();

        SharedPreferences pref4 = mContext.getSharedPreferences("UID", MODE_PRIVATE);
        SharedPreferences.Editor editor4 = pref4.edit();
        editor4.clear();
        editor4.putString("Uid",User.getUid());
        Log.d("MAIN_ACTIVITY---","Setting the user uid in shared preferences - "+User.getUid());
        editor4.apply();

    }

    @Override
    protected void onDestroy(){
        setLastUsedDateInFirebaseDate(User.getUid());
        unregisterAllReceivers();
        removeAllViews();
        if(!Variables.isDashboardActivityOnline) Variables.clearAdTotal();
        if(networkStateReceiver!=null) networkStateReceiver.removeListener(this);
        this.unregisterReceiver(networkStateReceiver);
        Variables.isMainActivityOnline = false;
        super.onDestroy();
    }



    private void loadAdsFromThread(){
        if(dbRef!=null){
            dbRef.removeEventListener(val);
        }
        if(Variables.isStartFromLogin) {
            try {
                Log.d(TAG, "---Starting the getAds method...");
                startGetAds();
            } catch (Exception e) {
                Log.e("BACKGROUND_PROC---", e.getMessage());
            }
        }else{
            loadUserDataFromSharedPrefs();
        }
    }

    private void loadUserDataFromSharedPrefs() {
        Log.d(TAG,"Loading user data from shared preferences first...");
        SharedPreferences prefs = getSharedPreferences("TodayTotals",MODE_PRIVATE);
        int number = prefs.getInt("TodaysTotals",0);
        Log.d(TAG,"AD TOTAL NUMBER GOTTEN FROM SHARED PREFERENCES IS - "+ number);
        if(mIsBeingReset || !getCurrentDateInSharedPreferences().equals(getDate())){
            Variables.setAdTotal(0,mKey);
            Log.d(TAG,"Setting ad totals in firebase to 0 since is being reset...");
        }else{
            Variables.setAdTotal(number,mKey);
        }

        SharedPreferences prefs2 = getSharedPreferences("MonthTotals",MODE_PRIVATE);
        int number2 = prefs2.getInt("MonthsTotals",0);
        Log.d(TAG,"MONTH AD TOTAL NUMBER GOTTEN FROM SHARED PREFERENCES IS - "+ number2);
        Variables.setMonthAdTotals(mKey,number2);

        SharedPreferences prefs3 = getSharedPreferences("ClusterID",MODE_PRIVATE);
        int number3 = prefs3.getInt("Cluster",0);
        Log.d(TAG,"CLUSTER ID NUMBER GOTTEN FROM SHARED PREFERENCES IS - "+ number3);
        User.setID(number3,mKey);

        SharedPreferences prefs4 = getSharedPreferences("UID",MODE_PRIVATE);
        String uid = prefs4.getString("Uid","");
        Log.d(TAG,"UID NUMBER GOTTEN FROM SHARED PREFERENCES IS - "+ uid);
        User.setUid(uid);

        Variables.isStartFromLogin = false;
        try {
            Log.d(TAG, "---Starting the getAds method...");
            startGetAds();
        } catch (Exception e) {
            Log.e("BACKGROUND_PROC---", e.getMessage());
        }
    }

    private void startGetAds() {
        setUpSwipeView();
        mAvi.setVisibility(View.VISIBLE);
        mLoadingText.setVisibility(View.VISIBLE);
        mLinearLayout.setVisibility(View.GONE);
        Log.d(TAG,"---Setting up mViewRunnable thread...");
        mViewRunnable = new Runnable() {
            @Override
            public void run() {
                getAds();
            }
        };
        Thread thread =  new Thread(null, mViewRunnable, "Background");
        Log.d(TAG,"---Starting thread...");
        thread.start();

    } ///////////////////////////

    //method for loading ads from thread.Contains sleep length...
    private void getAds() {
        try{
            Log.d(TAG,"---The getAdsFromFirebase method has been called...");
            getGetAdsFromFirebase();
            Thread.sleep(3000);
        }catch (Exception e) {
            Log.e("BACKGROUND_PROC", e.getMessage());
        }
        runOnUiThread(returnRes);
    }




    private void getGetAdsFromFirebase(){
        if(mAdList.size()!=0){
            mAdList.clear();
        }
        Log.d(TAG,"---Setting up firebase query...");
        if(mIsBeingReset){
            Log.d(TAG,"---Day is almost over,so loading tomorrows ads now.");
            Query query = FirebaseDatabase.getInstance().getReference(Constants.ADVERTS).child(getNextDay()).child(Integer.toString(User.getClusterID(mKey)));
            dbRef = query.getRef();
            Log.d(TAG,"---Adding value event listener...");
            dbRef.orderByKey().startAt(Integer.toString(1)).limitToFirst(5).addValueEventListener(val);
            mIsBeingReset = false;
        }else{
            Query query = FirebaseDatabase.getInstance().getReference(Constants.ADVERTS).child(getDate()).child(Integer.toString(User.getClusterID(mKey)));
            Log.d(TAG,"---Query set up is : "+Constants.ADVERTS+" : "+getDate()+" : "+User.getClusterID(mKey));
            dbRef = query.getRef();
            Log.d(TAG,"---Adding value event listener...");
            if(Variables.getAdTotal(mKey)==0){
                dbRef.orderByKey().startAt(Integer.toString(1)).limitToFirst(5).addValueEventListener(val);
            }else{
                dbRef.orderByKey().startAt(Integer.toString(Variables.getAdTotal(mKey))).limitToFirst(5).addListenerForSingleValueEvent(val);
            }
        }

    }

    ValueEventListener val = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if(dataSnapshot.hasChildren()){
                Log.d(TAG,"---Children in dataSnapshot from firebase exist");
                for(DataSnapshot snap:dataSnapshot.getChildren()){
                    Advert ad = snap.getValue(Advert.class);
                    mAdList.add(ad);
                }
                if(Variables.getAdTotal(mKey)!=0){
                    if(mAdList.size()>1) mAdList.remove(0);
                    mChildToStartFrom = Variables.getAdTotal(mKey) + (int)dataSnapshot.getChildrenCount()-1;
                }else{
                    mChildToStartFrom = Variables.getAdTotal(mKey) + (int)dataSnapshot.getChildrenCount()-1;
                }

                Log.d(TAG,"Child set to start from is -- "+mChildToStartFrom);
                Log.d(TAG,"---All the ads have been handled.Total is "+mAdList.size());
            }else{
                Log.d(TAG,"----No ads are available today");
            }
            mAvi.setVisibility(View.GONE);
            mLoadingText.setVisibility(View.GONE);
            mLinearLayout.setVisibility(View.VISIBLE);
            loadAdsIntoAdvertCard();
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




    private void unregisterAllReceivers(){
        Log.d("MAIN_ACTIVITY--","Unregistering all receivers");
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForAddingToSharedPreferences);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForConnectionOffline);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForConnectionOnline);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForLastAd);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForLoadMoreAds);
        sendBroadcastToUnregisterAllReceivers();
    }

    private void sendBroadcastToUnregisterAllReceivers() {
        Intent intent = new Intent(Constants.UNREGISTER_ALL_RECEIVERS);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }




    private void setUpSwipeView() {
        mSwipeView = (SwipePlaceHolderView)findViewById(R.id.swipeView);
        mLinearLayout = (LinearLayout) findViewById(R.id.bottomNavButtons);
        mAvi = (AVLoadingIndicatorView) findViewById(R.id.mainActivityAvi);
        mLoadingText = (TextView) findViewById(R.id.loadingAdsMessage);
        mFailedToLoadLayout = (LinearLayout) findViewById(R.id.failedLoadAdsLayout);
        mRetryButton = (Button) findViewById(R.id.retryLoadingAds);
        mLogoutButton = (ImageButton) findViewById(R.id.logoutBtn);

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
//        mSwipeView.s;
    }

    private void removeAllViews(){
        if(mSwipeView!=null){
            mSwipeView.removeAllViews();
        }
        if(mAdCounterView!=null){
            mAdCounterView.removeAllViews();
        }
    }

    private void loadAdCounter() {
        mAdCounterView = (PlaceHolderView)findViewById(R.id.adCounterView);
        mAdCounterView.addView(new AdCounterBar(this.getApplicationContext(),mAdCounterView));

    }

    private void loadAdsIntoAdvertCard(){
        if(mAdCounterView==null){
            Log.d(TAG,"---Setting up AdCounter...");
            loadAdCounter();
        }
        if(mSwipeView == null){
            Log.d(TAG,"---Setting up Swipe views..");
            setUpSwipeView();
        }
        if(mSwipeView.getChildCount()!=0){
            Log.d(TAG,"Removing existing children from swipeView...");
            mSwipeView.removeAllViews();
        }
        if(mAdCounterView.getChildCount()==0){
            Log.d(TAG,"Loading the top timer now...");
            loadAdCounter();
        }
        if(mAdList!=null && mAdList.size()>0){
            if(mAdList.size() == 1 && mChildToStartFrom==Variables.getAdTotal(mKey)){
                Log.d(TAG,"---User has seen all the ads, thus will load only last ad...");
                mSwipeView.lockViews();
                mSwipeView.addView(new AdvertCard(mContext,mAdList.get(0),mSwipeView,Constants.LAST));
                Variables.setIsLastOrNotLast(Constants.LAST);
                isLastAd = true;
            }else{
                for(Advert ad: mAdList){
                    mSwipeView.addView(new AdvertCard(mContext,ad,mSwipeView,Constants.NOT_LAST));
                    Variables.setIsLastOrNotLast(Constants.NOT_LAST);
                }
            }
            mAdList.clear();
        }else{
            Advert noAds = new Advert();
            mSwipeView.addView(new AdvertCard(mContext,noAds,mSwipeView,Constants.NO_ADS));
            Variables.setIsLastOrNotLast(Constants.NO_ADS);
            loadAnyAnnouncements();
        }
        Log.d(TAG,"---Setting up On click listeners...");
        onclicks();
        networkStateReceiver = new NetworkStateReceiver();
        networkStateReceiver.addListener(this);
        this.registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
    }




    private void registerReceivers(){
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverForAddingToSharedPreferences,new IntentFilter(Constants.ADD_TO_SHARED_PREFERENCES));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverForConnectionOffline,new IntentFilter(Constants.CONNECTION_OFFLINE));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverForConnectionOnline,new IntentFilter(Constants.CONNECTION_ONLINE));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverForLastAd,new IntentFilter(Constants.LAST));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverForLoadMoreAds,new IntentFilter(Constants.LOAD_MORE_ADS));

    }

    private void onclicks() {
        findViewById(R.id.logoutBtn).setOnClickListener(this);
        if(findViewById(R.id.bookmark2Btn)!= null){
            findViewById(R.id.bookmark2Btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                        if(!Variables.hasBeenPinned ){
                            if(Variables.mIsLastOrNotLast == Constants.NOT_LAST && !isLastAd){
                                Snackbar.make(findViewById(R.id.mainCoordinatorLayout), R.string.pinning,
                                        Snackbar.LENGTH_SHORT).show();
                                pinAd();
                            }else{
                                Snackbar.make(findViewById(R.id.mainCoordinatorLayout),"You can't pin this..",
                                        Snackbar.LENGTH_SHORT).show();
                            }
                        }else{
                            Snackbar.make(findViewById(R.id.mainCoordinatorLayout), R.string.hasBeenPinned,
                                    Snackbar.LENGTH_SHORT).show();
                        }

                }
            });
        }

        findViewById(R.id.bookmarkBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Bookmarks.class);
                startActivity(intent);
            }
        });

        if(findViewById(R.id.profileImageView)!= null){
            findViewById(R.id.profileImageView).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSwipeView.doSwipe(true);
                }
            });

            findViewById(R.id.profileImageView).setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    findViewById(R.id.bookmark2Btn).callOnClick();
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
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "Hey, have you heard of this cool app called AdCafé?");
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent,getResources().getText(R.string.shareText)));
            }
        });

        if(findViewById(R.id.reportBtn)!=null){
            findViewById(R.id.reportBtn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(Variables.mIsLastOrNotLast == Constants.NO_ADS || isLastAd) {
                        Snackbar.make(findViewById(R.id.mainCoordinatorLayout),"You can't report this..",
                                Snackbar.LENGTH_SHORT).show();
                    }else{
                        FragmentManager fm = getFragmentManager();
                        ReportDialogFragment reportDialogFragment = new ReportDialogFragment();
                        reportDialogFragment.show(fm, "Report dialog fragment.");
                        reportDialogFragment.setfragcontext(mContext);
                    }

                }
            });
        }

    }

    private void logoutUser() {
        setLastUsedDateInFirebaseDate(User.getUid());
        if(dbRef!=null){
            dbRef.removeEventListener(val);
        }
        User.setID(0,mKey);
        unregisterAllReceivers();
        if(FirebaseAuth.getInstance()!=null){
            FirebaseAuth.getInstance().signOut();
        }
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }




    private BroadcastReceiver mMessageReceiverForAddingToSharedPreferences = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("COUNTER_BAR_TO_MAIN- ","Broadcast has been received to add to shared preferences.");
            Variables.adAdToTotal(mKey);
            Variables.adToMonthTotals(mKey);
            addToSharedPreferences();
            adDayAndMonthTotalsToFirebase();
            onclicks();
        }
    };



    private BroadcastReceiver mMessageReceiverForConnectionOffline = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("CONNECTION_C-MAIN_A","Connection has been dropped");
            Snackbar.make(findViewById(R.id.mainCoordinatorLayout), R.string.connectionDropped2,
                    Snackbar.LENGTH_INDEFINITE).show();
        }
    };

    private BroadcastReceiver mMessageReceiverForConnectionOnline = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("CONNECTION_C-MAIN_A","Connection has come online");
        }
    };

    private BroadcastReceiver mMessageReceiverForLastAd = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(mContext,R.string.lastAd,Toast.LENGTH_SHORT).show();
            loadAnyAnnouncements();
        }
    };

    private BroadcastReceiver mMessageReceiverForLoadMoreAds = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
           if(!mIsBeingReset) loadMoreAds();
        }
    };




    private void loadMoreAds() {
        Log.d("MAIN-ACTIVITY---","Loading more ads since user has seen almost all....");
        Query query;
        if(isAlmostMidNight()){
            query = FirebaseDatabase.getInstance().getReference(Constants.ADVERTS).child(getNextDay()).child(Integer.toString(User.getClusterID(mKey)));
            Log.d(TAG,"---Query set up is : "+Constants.ADVERTS+" : "+getNextDay()+" : "+User.getClusterID(mKey));
        }else{
            query = FirebaseDatabase.getInstance().getReference(Constants.ADVERTS).child(getDate()).child(Integer.toString(User.getClusterID(mKey)));
            Log.d(TAG,"---Query set up is : "+Constants.ADVERTS+" : "+getDate()+" : "+User.getClusterID(mKey));
        }
        dbRef = query.getRef();
        dbRef.orderByKey().startAt(Integer.toString(mChildToStartFrom+1)).limitToFirst(5).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren()){
                    Log.d(TAG,"---More children in dataSnapshot from firebase exist");
                    for(DataSnapshot snap:dataSnapshot.getChildren()){
                        Advert ad = snap.getValue(Advert.class);
                        mAdList.add(ad);
                    }
                    loadMoreAdsIntoAdvertCard();
                    mChildToStartFrom +=(int)dataSnapshot.getChildrenCount();
                    Log.d(TAG,"---All the new ads have been handled.Total is "+mAdList.size());
                }else{
                    Log.d(TAG,"----No more ads are available today");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG,"Unable to load more ads for some issue."+databaseError.getMessage());
            }
        });
    }

    private void loadMoreAdsIntoAdvertCard() {
        for(Advert ad: mAdList){
            mSwipeView.addView(new AdvertCard(mContext,ad,mSwipeView,Constants.LOAD_MORE_ADS));
            Variables.setIsLastOrNotLast(Constants.NOT_LAST);
        }
        mAdList.clear();
    }

    private void loadAnyAnnouncements() {
        Log.d("MAIN-ACTIVITY---","Now loading announcements since there are no more ads....");
        Query query;
        if(isAlmostMidNight()){
            query = FirebaseDatabase.getInstance().getReference(Constants.ANNOUNCEMENTS).child(getNextDay());
            Log.d(TAG,"---Query set up is : "+Constants.ANNOUNCEMENTS+" : "+getNextDay());
        }else{
            query = FirebaseDatabase.getInstance().getReference(Constants.ANNOUNCEMENTS).child(getDate());
            Log.d(TAG,"---Query set up is : "+Constants.ANNOUNCEMENTS+" : "+getDate());
        }

        dbRef = query.getRef();
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren()){
                    for(DataSnapshot snap:dataSnapshot.getChildren()){
                        Advert ad = snap.getValue(Advert.class);
                        mAdList.add(ad);
                    }
                    for(Advert ad: mAdList){
                        mSwipeView.addView(new AdvertCard(mContext,ad,mSwipeView,Constants.ANNOUNCEMENTS));
                    }
                        Toast.makeText(mContext,"Before you leave though, a few messages from the dev team...",Toast.LENGTH_SHORT).show();
                        mSwipeView.unlockViews();
                        findViewById(R.id.bookmark2Btn).setAlpha(0.3f);
                        findViewById(R.id.reportBtn).setAlpha(0.3f);
                        mAdList.clear();
                }else{
                    Log.d(TAG,"There are no announcements today...");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG,"Unable to load announcements...");
            }
        });
    }

    private void hideNavBars() {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        decorView.setSystemUiVisibility(uiOptions);
    }




    public float density(){
        double constant = 0.000046875;
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int density = metrics.densityDpi;
        float relativeScale;

        if (density >= 560) {
            Log.d("DENSITY---","HIGH... Density is " + String.valueOf(density));
            relativeScale = 0.005f;
        }else if(density >= 460){
            Log.d("DENSITY---","MEDIUM-HIGH... Density is " + String.valueOf(density));
            relativeScale = 0.009f;
        }else if(density >= 360){
            Log.d("DENSITY---","MEDIUM-LOW... Density is " + String.valueOf(density));
            relativeScale = 0.013f;
        }else if(density >= 260){
            Log.d("DENSITY---","LOW... Density is " + String.valueOf(density));
            relativeScale = 0.015f;
        }else{
            relativeScale = 0.02f;
        }
        return relativeScale;
    }

    private void addToSharedPreferences(){
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Constants.AD_TOTAL, MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.putInt("adTotals",Variables.getAdTotal(mKey));
        Log.d("MAIN_ACTIVITY--","Adding 1 to shared preferences adTotal is - "+Integer.toString(Variables.getAdTotal(mKey)));
        editor.commit();

        SharedPreferences pref2 = getApplicationContext().getSharedPreferences(Constants.TOTAL_NO_OF_ADS_SEEN_All_MONTH, MODE_PRIVATE);
        SharedPreferences.Editor editor2 = pref2.edit();
        editor2.clear();
        editor.putInt(Constants.TOTAL_NO_OF_ADS_SEEN_All_MONTH,Variables.getMonthAdTotals(mKey));
        Log.d("MAIN_ACTIVITY--","Adding 1 to shared preferences Month ad totals is - "+Integer.toString(Variables.getMonthAdTotals(mKey)));
        editor2.commit();
    }

    private boolean isAlmostMidNight() {
        Calendar c = Calendar.getInstance();
        int hours = c.get(Calendar.HOUR_OF_DAY);
        int minutes = c.get(Calendar.MINUTE);
        int seconds = c.get(Calendar.SECOND);

        Log.d(TAG,"Current time is " + hours + ":"+minutes + ":"+seconds);
        if(hours == 23 && (minutes == 59) && (seconds>=0)){
            Log.d(TAG,"---Day is approaching midnight,returning true to reset the activity and values.");
            return true;
        }else{
            Log.d(TAG,"---Day is not approaching midnight,so activity will continue normally.");
            return false;
        }
    }



    private void adDayAndMonthTotalsToFirebase(){
        String uid = User.getUid();
        DatabaseReference adRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS).child(uid).child(Constants.TOTAL_NO_OF_ADS_SEEN_TODAY);
        adRef.setValue(Variables.getAdTotal(mKey));

        DatabaseReference adRef2 = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS).child(uid).child(Constants.TOTAL_NO_OF_ADS_SEEN_All_MONTH);
        adRef2.setValue(Variables.getMonthAdTotals(mKey));

        Variables.currentAdNumber++;

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

    private void resetAdTotalSharedPreferencesAndDayAdTotals(){
        SharedPreferences prefs = getSharedPreferences(Constants.AD_TOTAL,MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.commit();

        Variables.setAdTotal(0,mKey);
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
                if(isOffline){
                    isFirebaseResetNecessary = true;
                }
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                isFirebaseResetNecessary = false;
            }
        });
    }

    private void resetEverything() {
        mIsBeingReset = true;
        resetAdTotalSharedPreferencesAndDayAdTotals();
        loadAdsFromThread();
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




    @Override
    public void onClick(View v) {
        if(v == mLogoutButton){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Are you sure you want to log out?")
                    .setCancelable(true)
                    .setPositiveButton("Yes,I want to", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            logoutUser();
                        }
                    })
                    .setNegativeButton("No,I'm staying", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    }).show();
        }
        if(v == mRetryButton){
            Log.d(TAG,"Retrying to load ads...");
            mAvi.setVisibility(View.VISIBLE);
            mLoadingText.setVisibility(View.VISIBLE);
            mFailedToLoadLayout.setVisibility(View.GONE);
            Toast.makeText(mContext,"Retrying...",Toast.LENGTH_SHORT).show();
            loadAdsFromThread();
        }

    }

    private void setCurrentTimeToSharedPrefs() {
        Log.d(TAG,"---Setting current date in shared preferences.");
        SharedPreferences prefs = getSharedPreferences(Constants.DATE,MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("date",getDate());
        editor.apply();
    }

    private String getCurrentDateInSharedPreferences(){
        Log.d(TAG,"---Getting current date in shared preferences.");
        SharedPreferences prefs = getSharedPreferences(Constants.DATE,MODE_PRIVATE);
        String date = prefs.getString("date","nill");
        return date;
    }




    @Override
    public void networkAvailable() {
        Log.d(TAG, "User is connected to the internet via wifi or cellular data");
        isOffline = false;
        findViewById(R.id.droppedInternetLayout).setVisibility(View.GONE);
        findViewById(R.id.bottomNavButtons).setVisibility(View.VISIBLE);
        mSwipeView.setVisibility(View.VISIBLE);
        mAdCounterView.setVisibility(View.VISIBLE);
        if(isFirebaseResetNecessary){
            resetAdTotalsInFirebase();
        }
    }

    @Override
    public void networkUnavailable() {
        Log.d(TAG, "User has gone offline...");
        isOffline = true;
        findViewById(R.id.bottomNavButtons).setVisibility(View.GONE);
        mSwipeView.setVisibility(View.GONE);
        mAdCounterView.setVisibility(View.GONE);
        findViewById(R.id.droppedInternetLayout).setVisibility(View.VISIBLE);

    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Press BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }


    private void pinAd(){
        Log.d(TAG,"Pinning ad from main activity");
        int adNumber;
        String datte;
        if(Variables.hasTimerStarted){
            adNumber = Variables.getAdTotal(mKey)+1;
        }else{
            adNumber = Variables.getAdTotal(mKey);
        }
        if(isAlmostMidNight()){
            datte = getNextDay();
        }else{
            datte = getDate();
        }
        Query query = FirebaseDatabase.getInstance().getReference(Constants.ADVERTS).child(datte)
                .child(Integer.toString(User.getClusterID(mKey))).child(Integer.toString(adNumber));
        Log.d(TAG,"Query set up is :"+Constants.ADVERTS+" : "+getDate()+" : "+User.getClusterID(mKey)+" : "+adNumber);
        DatabaseReference dbRef = query.getRef();
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren()){
                    Advert ad = dataSnapshot.getValue(Advert.class);
                    uploadToUserList(ad);
                }else{
                 Toast.makeText(mContext,"data snapshot is empty..",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(mContext,"Pinning may have failed.",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadToUserList(Advert ad) {
        String uid = User.getUid();

        DatabaseReference adRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS).child(uid).child(Constants.PINNED_AD_LIST);
        DatabaseReference pushRef = adRef.push();
        String pushId = pushRef.getKey();

        Log.d(TAG, "pinning the selected ad.");
        ad.setPushId(pushId);

        pushRef.setValue(ad).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d(TAG,"Pinning is complete.");
                Variables.hasBeenPinned = true;
            }
        });
    }
}
