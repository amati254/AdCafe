package com.bry.adstudio.ui;

import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;


import com.bry.adstudio.Bookmarks;
import com.bry.adstudio.Constants;
import com.bry.adstudio.R;
import com.bry.adstudio.Variables;
import com.bry.adstudio.adapters.AdvertCard;
import com.bry.adstudio.adapters.AdCounterBar;
import com.bry.adstudio.fragments.ReportDialogFragment;
import com.bry.adstudio.models.Advert;
import com.bry.adstudio.services.Utils;
import com.mindorks.placeholderview.PlaceHolderView;
import com.mindorks.placeholderview.SwipeDecor;
import com.mindorks.placeholderview.SwipePlaceHolderView;

public class MainActivity extends AppCompatActivity{
    private SwipePlaceHolderView mSwipeView;
    private PlaceHolderView mAdCounterView;
    private Context mContext;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,new IntentFilter(Constants.AD_COUNTER_BROADCAST));

        setUpSwipeView();
        loadAdsFromJSONFile();
        loadAdCounter();
        hideNavBars();
    }

    private void setUpSwipeView() {
        mSwipeView = (SwipePlaceHolderView)findViewById(R.id.swipeView);
        mContext = getApplicationContext();

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

    private void loadAdCounter() {
        mAdCounterView = (PlaceHolderView)findViewById(R.id.adCounterView);
        mAdCounterView.addView(new AdCounterBar(this.getApplicationContext(),mAdCounterView));
        while(Variables.hasNumberOfAdsChanged){
            mAdCounterView.removeAllViews();
            mAdCounterView.addView(new AdCounterBar(this.getApplicationContext(),mAdCounterView));
            Variables.setHasNumberOfAdsChangedFalse();
        }


    }

    private void loadAdsFromJSONFile(){


        if((Utils.loadProfiles(this.getApplicationContext()))!= null) {
            for (Advert ads : Utils.loadProfiles(this.getApplicationContext())) {
                mSwipeView.addView(new AdvertCard(mContext,ads,mSwipeView));
                addToNumberOfAds(ads.getNumberOfAds());
            }
            onclicks();
        }else{
            Toast.makeText(mContext, "No Ads are available", Toast.LENGTH_SHORT).show();
        }
    }

    private void addToNumberOfAds(int number) {
        Variables.numberOfAds = number;
    }

    private void onclicks() {
        findViewById(R.id.settingsBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,settings.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.bookmark2Btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext,"Bookmarked.",Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.bookmarkBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Bookmarks.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.profileImageView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSwipeView.doSwipe(true);
            }
        });

        findViewById(R.id.dashboard).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Dashboard.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.profileImageView).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                findViewById(R.id.bookmark2Btn).callOnClick();
                return false;
            }
        });


        findViewById(R.id.reportBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getFragmentManager();
                ReportDialogFragment reportDialogFragment = new ReportDialogFragment();
                reportDialogFragment.show(fm,"Report dialog fragment.");
                reportDialogFragment.setfragcontext(mContext);
            }
        });

    }

    @Override
    protected void onDestroy(){
        sendBroadcast(Constants.STOP_TIMER);
        super.onDestroy();
    }

    @Override
    protected void onStop(){
        sendBroadcast(Constants.STOP_TIMER);
        super.onStop();
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("COUNTER_BAR_TO_MAIN- ","Broadcast has been received.");
            onclicks();
        }
    };

    private void sendBroadcast(String message){
        if(message == Constants.STOP_TIMER){
            Log.d("MAIN_ACTIVITY","Sending broadcast to stop timer.");
            Intent intent = new Intent(Constants.ADVERT_CARD_BROADCAST);
            intent.putExtra(Constants.AD_COUNTER_BROADCAST,Constants.STOP_TIMER);

            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }

    private void hideNavBars() {
//        View decorView = getWindow().getDecorView();
//        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
//        decorView.setSystemUiVisibility(uiOptions);
    }

    public float density(){
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int density = metrics.densityDpi;
        float relativeScale;

        if (density >= 560) {
            Log.d("DENSITY---","HIGH... Density is " + String.valueOf(density));
            relativeScale = 0.005f;
        }else if(density >= 360){
            Log.d("DENSITY---","MEDIUM... Density is " + String.valueOf(density));
            relativeScale = 0.01f;
        }else if(density >= 160){
            Log.d("DENSITY---","LOW... Density is " + String.valueOf(density));
            relativeScale = 0.015f;
        }else{
            relativeScale = 0.02f;
        }
        return relativeScale;
    }

}
