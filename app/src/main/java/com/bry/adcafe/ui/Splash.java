package com.bry.adcafe.ui;

import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.bry.adcafe.R;
import com.bry.adcafe.services.SliderPrefManager;
import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

public class Splash extends AppCompatActivity {
    private final int SPLASH_DISPLAY_LENGTH = 3700;
    private SliderPrefManager myPrefManager;



    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_splash);
//        hideNavBars();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                myPrefManager = new SliderPrefManager(getApplicationContext());
                if (myPrefManager.isFirstTimeLaunch()){
                    Intent intent = new Intent(Splash.this,TutorialActivity.class);
                    startActivity(intent);
                    finish();
                }else{
                    Intent intent = new Intent(Splash.this,LoginActivity.class);
                    startActivity(intent);
                    finish();
                }

            }
        },SPLASH_DISPLAY_LENGTH);
    }

    private void hideNavBars() {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        decorView.setSystemUiVisibility(uiOptions);
    }

}
