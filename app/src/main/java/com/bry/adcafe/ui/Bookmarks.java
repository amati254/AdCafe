package com.bry.adcafe.ui;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bry.adcafe.Constants;
import com.bry.adcafe.R;
import com.bry.adcafe.adapters.SavedAdsCard;
import com.bry.adcafe.models.Advert;
import com.bry.adcafe.services.ConnectionChecker;
import com.bry.adcafe.services.SavedAdsUtils;
import com.bry.adcafe.services.Utils;
import com.mindorks.placeholderview.PlaceHolderView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RunnableFuture;

public class Bookmarks extends AppCompatActivity {
    private Context mContext;
    private PlaceHolderView mPlaceHolderView;

    private ArrayList<Advert> mSavedAds = null;
    private Runnable mViewRunnable;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmarks);
        loadPlaceHolderViews();
        ConnectionChecker.StartNetworkChecker(mContext);

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverForConnectionOffline,new IntentFilter(Constants.CONNECTION_OFFLINE));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverForConnectionOnline,new IntentFilter(Constants.CONNECTION_ONLINE));

        mSavedAds = new ArrayList<Advert>();
        loadAdsFromThread();

//        loadFromAsynchTask();
    }

//    private void loadFromAsynchTask() {
//        new Task().execute();
//    }

//    class Task extends AsyncTask<String, Integer, Boolean>{
//
//        @Override
//        protected void onPreExecute() {
//            mProgressBar.setVisibility(View.VISIBLE);
//            mPlaceHolderView.setVisibility(View.GONE);
//            super.onPreExecute();
//        }
//
//        @Override
//        protected void onPostExecute(Boolean result) {
//            mProgressBar.setVisibility(View.GONE);
//            mPlaceHolderView.setVisibility(View.VISIBLE);
//            super.onPostExecute(result);
//        }
//
//        @Override
//        protected Boolean doInBackground(String... params) {
//
//
//            try {
//                Thread.sleep(3000);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        return null;
//        }
//    }

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
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void getAds() {
        try{
            mSavedAds = new ArrayList<Advert>();
            for(Advert ad: SavedAdsUtils.loadSavedAdverts(this.getApplicationContext())){
                mSavedAds.add(ad);
            }
            Thread.sleep(1500);
            Log.i("ARRAY", ""+ mSavedAds.size());
        }catch (Exception e) {
            Log.e("BACKGROUND_PROC", e.getMessage());
        }
        runOnUiThread(returnRes);
    }
    private Runnable returnRes = new Runnable() {
        @Override
        public void run() {
            loadBookmarkedAdsFromJSONFile();
        }
    };


    private void loadPlaceHolderViews() {
        mProgressBar = (ProgressBar) findViewById(R.id.pbHeaderProgress);
        mPlaceHolderView = (PlaceHolderView) findViewById(R.id.PlaceHolderView);
        mContext = getApplicationContext();

    }


    private void loadBookmarkedAdsFromJSONFile() {
        if(mPlaceHolderView == null){
            loadPlaceHolderViews();
        }
        if(mSavedAds!=null && mSavedAds.size()>0){
            for(int i = 0; i<mSavedAds.size();i++)
                mPlaceHolderView.addView(new SavedAdsCard(mSavedAds.get(i),mContext,mPlaceHolderView));
        }
        mProgressBar.setVisibility(View.GONE);
    }

    private BroadcastReceiver mMessageReceiverForConnectionOffline = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("CONNECTION_C-Bookmarks","Connection has been dropped");
            Snackbar.make(findViewById(R.id.bookmarksCoordinatorLayout), R.string.connectionDropped,
                    Snackbar.LENGTH_INDEFINITE).show();
        }
    };

    private BroadcastReceiver mMessageReceiverForConnectionOnline = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("CONNECTION_C-Bookmarks","Connection has come online");
        }
    };

}
