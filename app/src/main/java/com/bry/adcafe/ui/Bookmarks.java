package com.bry.adcafe.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bry.adcafe.Constants;
import com.bry.adcafe.R;
import com.bry.adcafe.adapters.SavedAdsCard;
import com.bry.adcafe.models.Advert;
import com.bry.adcafe.services.NetworkStateReceiver;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mindorks.placeholderview.PlaceHolderView;

import java.util.ArrayList;
import java.util.List;

import com.wang.avi.AVLoadingIndicatorView;

public class Bookmarks extends AppCompatActivity {
    private Context mContext;
    private PlaceHolderView mPlaceHolderView;
    private ChildEventListener mChildEventListener;
    private DatabaseReference mRef;

    private List<Advert> mSavedAds;
    private Runnable mViewRunnable;
    private AVLoadingIndicatorView mAvi;
    private TextView loadingText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmarks);
        mContext = getApplicationContext();

        loadPlaceHolderViews();
        registerReceivers();

        if(isNetworkConnected(mContext)){
            loadAdsFromThread();
        }else{
            Snackbar.make(findViewById(R.id.bookmarksCoordinatorLayout), R.string.connectionDropped,
                    Snackbar.LENGTH_INDEFINITE).show();
        }
    }


    @Override
    protected void onDestroy(){
        super.onDestroy();
        unregisterAllReceivers();
    }

    private void unregisterAllReceivers() {
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForUnpinned);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForReceivingUnableToPinAd);
    }

    private void registerReceivers(){
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverForUnpinned,new IntentFilter(Constants.REMOVE_PINNED_AD));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverForReceivingUnableToPinAd,new IntentFilter(Constants.UNABLE_TO_REMOVE_PINNED_AD));

    }



    private void loadAdsFromThread(){
        try{
            startGetAds();
        }catch (Exception e) {
            Log.e("BACKGROUND_PROC", e.getMessage());
        }
    }

    private void startGetAds() {
        mViewRunnable = new Runnable() {
            @Override
            public void run() {
                getAds();
            }
        };
        Thread thread =  new Thread(null, mViewRunnable, "Background");
        thread.start();
//        mProgressBar.setVisibility(View.VISIBLE);
        mAvi.setVisibility(View.VISIBLE);
        loadingText.setVisibility(View.VISIBLE);
    }

    private void getAds() {
        try{
            mSavedAds = new ArrayList<>();
            loadAdsFromFirebase();

            Thread.sleep(50);
            Log.i("ARRAY", ""+ mSavedAds.size());
        }catch (Exception e) {
            Log.e("BACKGROUND_PROC", e.getMessage());
        }
        runOnUiThread(returnRes);
    }



    private Runnable returnRes = new Runnable() {
        @Override
        public void run() {
//            loadBookmarkedAdsIntoCards();
        }
    };

    private void loadPlaceHolderViews() {
        mPlaceHolderView = (PlaceHolderView) findViewById(R.id.PlaceHolderView);
        mAvi = (AVLoadingIndicatorView) findViewById(R.id.avi);
        loadingText = (TextView) findViewById(R.id.loadingPinnedAdsMessage);
        mPlaceHolderView.getBuilder().setLayoutManager(new GridLayoutManager(mContext,1));

    }

    private void loadBookmarkedAdsIntoCards() {
        if(mPlaceHolderView == null){
            loadPlaceHolderViews();
        }
        if(mSavedAds!=null && mSavedAds.size()>0){
            for(int i = 0; i<mSavedAds.size();i++){
                mPlaceHolderView.addView(new SavedAdsCard(mSavedAds.get(i),mContext,mPlaceHolderView,mSavedAds.get(i).getPushId()));
            }
        }else{
            Toast.makeText(mContext,"You do not have any pinned ads.",Toast.LENGTH_LONG).show();
        }
        mAvi.setVisibility(View.GONE);
        loadingText.setVisibility(View.GONE);

    }




    private BroadcastReceiver mMessageReceiverForUnpinned = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("BOOKMARKS--","Message received to show toast for unpin action");
            Snackbar.make(findViewById(R.id.bookmarksCoordinatorLayout), R.string.unpinned,
                    Snackbar.LENGTH_SHORT).show();
        }
    };

    private BroadcastReceiver mMessageReceiverForReceivingUnableToPinAd = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("BOOKMARKS--","Unable to unpin ad.");
            Snackbar.make(findViewById(R.id.bookmarksCoordinatorLayout), R.string.failedUnpinned,
                    Snackbar.LENGTH_SHORT).show();
        }
    };


    private void loadAdsFromFirebase(){
        if(!mSavedAds.isEmpty()){
            mSavedAds.clear();
//            mPlaceHolderView.removeAllViews();
        }
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();
        Query query = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS).child(uid).child(Constants.PINNED_AD_LIST);
        DatabaseReference mRef = query.getRef();
        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot snap: dataSnapshot.getChildren()){
                    Advert advert = snap.getValue(Advert.class);
//                    advert.setPushId(advert.getPushId());
                    mSavedAds.add(advert);
                    Log.d("BOOKMARKS"," --Loaded ads from firebase.--"+advert.getPushId());
                }
                loadBookmarkedAdsIntoCards();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("UTILS","Failed to load ads from firebase.");
            }
        });
    }

    private boolean isNetworkConnected(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return (netInfo != null && netInfo.isConnected());
    }


}
