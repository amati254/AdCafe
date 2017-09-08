package com.bry.adcafe.ui;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.bry.adcafe.Constants;
import com.bry.adcafe.R;

public class Dashboard extends AppCompatActivity {
    private TextView mTotalAdsSeenToday;
    private TextView mTotalAdsSeenAllTime;
    private int mTotalNumberOfAds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        loadNumberOfAdsBySharedPref();
        loadViews();
        setValues();
    }

    private void loadNumberOfAdsBySharedPref() {
        SharedPreferences prefs = getSharedPreferences(Constants.AD_TOTAL,MODE_PRIVATE);
        int number = prefs.getInt("adTotals",0);
        mTotalNumberOfAds =+ number;
    }

    private void loadViews() {
        mTotalAdsSeenAllTime = (TextView) findViewById(R.id.AdsSeenAllTimeNumber);
        mTotalAdsSeenToday = (TextView) findViewById(R.id.AdsSeenTodayNumber);
    }

    private void setValues() {
        mTotalAdsSeenToday.setText(Integer.toString(mTotalNumberOfAds));
    }
}
