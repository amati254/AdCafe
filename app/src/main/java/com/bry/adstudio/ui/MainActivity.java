package com.bry.adstudio.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;


import com.bry.adstudio.R;
import com.bry.adstudio.Variables;
import com.bry.adstudio.adapters.AdvertCard;
import com.bry.adstudio.adapters.AdCounterBar;
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

        loadAdsFromJSONFile();
        loadAdCounter();
    }

    private void loadAdCounter() {
        mAdCounterView = (PlaceHolderView)findViewById(R.id.adCounterView);
        mAdCounterView.addView(new AdCounterBar(this.getApplicationContext(),mAdCounterView));
    }

    public void removeAd(){
        loadAdCounter();
    }

    private void loadAdsFromJSONFile(){
        mSwipeView = (SwipePlaceHolderView)findViewById(R.id.swipeView);
        mContext = getApplicationContext();

        int bottomMargin = Utils.dpToPx(110);
        Point windowSize = Utils.getDisplaySize(getWindowManager());
        mSwipeView.getBuilder()
                .setDisplayViewCount(4)
                .setIsUndoEnabled(true)
                .setHeightSwipeDistFactor(10)
                .setWidthSwipeDistFactor(5)
                .setSwipeDecor(new SwipeDecor()
                        .setViewWidth(windowSize.x)
                        .setViewHeight(windowSize.y - bottomMargin)
                        .setViewGravity(Gravity.TOP)
                        .setPaddingTop(15)
                        .setRelativeScale(0.015f));

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

        findViewById(R.id.bookmarkBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                mSwipeView.doSwipe(true);
                Toast.makeText(mContext,"Bookmarked.",Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.profileImageView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSwipeView.doSwipe(true);
            }
        });

        findViewById(R.id.undoBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSwipeView.undoLastSwipe();
            }
        });

        findViewById(R.id.nextBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeAd();
                mSwipeView.doSwipe(true);
            }
        });
    }

}
