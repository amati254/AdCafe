package com.bry.adstudio.ui;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


import com.bry.adstudio.R;
import com.bry.adstudio.models.Advert;
import com.bry.adstudio.services.Utils;
import com.mindorks.placeholderview.SwipeDecor;
import com.mindorks.placeholderview.SwipePlaceHolderView;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity{
    private SwipePlaceHolderView mSwipeView;
    private Context mContext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSwipeView = (SwipePlaceHolderView) findViewById(R.id.swipeView);
        mContext = getApplication();
        mSwipeView.getBuilder()
                .setDisplayViewCount(2)
                .setSwipeDecor(new SwipeDecor()
                .setPaddingLeft(10)
                .setRelativeScale(0.01f));

        for(Advert advert : Utils.loadAds(this.getApplicationContext())){
            mSwipeView.addView(new AdvertCard(mContext,advert,mSwipeView));
        }


    }



}
