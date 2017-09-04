package com.bry.adcafe.ui;

import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;


import com.bry.adcafe.Bookmarks;
import com.bry.adcafe.Constants;
import com.bry.adcafe.R;
import com.bry.adcafe.Variables;
import com.bry.adcafe.adapters.AdvertCard;
import com.bry.adcafe.adapters.AdCounterBar;
import com.bry.adcafe.fragments.ReportDialogFragment;
import com.bry.adcafe.models.Advert;
import com.bry.adcafe.services.Utils;
import com.mindorks.placeholderview.PlaceHolderView;
import com.mindorks.placeholderview.SwipeDecor;
import com.mindorks.placeholderview.SwipePlaceHolderView;

import java.util.List;

public class MainActivity extends AppCompatActivity{
    private SwipePlaceHolderView mSwipeView;
    private PlaceHolderView mAdCounterView;
    private Context mContext;
    private static final String TAG = "MainActivity";
    private int mNumberOfAdsSeen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverForAddingToSharedPreferences,new IntentFilter(Constants.ADD_TO_SHARED_PREFERENCES));

        loadFromSharedPreferences();
        setUpSwipeView();
        loadAdsFromJSONFile();
        hideNavBars();
    }

    @Override
    protected void onResume(){
        super.onResume();
        mSwipeView.removeAllViews();
        loadAdsFromJSONFile();
    }

    @Override
    protected void onDestroy(){
        sendBroadcast(Constants.STOP_TIMER);
        addToSharedPreferences();
        super.onDestroy();
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
            List<Advert> adList = Utils.loadProfiles(this.getApplicationContext());
            for(int i = 0 ; i < adList.size()-1 ; i++){
                if(Variables.adTotal>=adList.size()){
                    mSwipeView.addView(new AdvertCard(mContext,adList.get(adList.size()-1),mSwipeView,Constants.LAST));
                    Variables.setIsLastOrNotLast(Constants.LAST);
                    break;
                } else if(i >= Variables.adTotal){
                    mSwipeView.addView(new AdvertCard(mContext,adList.get(i),mSwipeView,Constants.NOT_LAST));
                }
            }
            loadAdCounter();
            Variables.setNewNumberOfAds(adList.size()-Variables.adTotal);
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

        if(findViewById(R.id.bookmark2Btn)!= null){
            findViewById(R.id.bookmark2Btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(mContext,"Bookmarked.",Toast.LENGTH_SHORT).show();
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


        findViewById(R.id.shareBtn).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(mContext,"This will share the app.",Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        if(findViewById(R.id.reportBtn)!=null){
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

    }


    private BroadcastReceiver mMessageReceiverForAddingToSharedPreferences = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("COUNTER_BAR_TO_MAIN- ","Broadcast has been received to add to shared preferences.");
            addToSharedPreferences();
            onclicks();
        }
    };

    private void sendBroadcast(String message){

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
            relativeScale = 0.009f;
        }else if(density >= 160){
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
        Variables.adAdToTotal();
        editor.putInt("adTotals",Variables.adTotal);
        Log.d("MAIN_ACTIVITY--","Adding adTotal to shared preferences - "+Integer.toString(Variables.adTotal));
        editor.commit();
    }

    private void loadFromSharedPreferences(){
        SharedPreferences prefs = getSharedPreferences(Constants.AD_TOTAL,MODE_PRIVATE);
        Variables.adTotal = prefs.getInt("adTotals",0);
//        Variables.adTotal =0;
        Log.d("MAIN_ACTIVITY-----","NUMBER GOTTEN FROM SHARED PREFERENCES IS - "+ Variables.adTotal);
    }
}
