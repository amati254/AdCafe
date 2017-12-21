package com.bry.adcafe.ui;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.bry.adcafe.R;
import com.bry.adcafe.services.SliderPrefManager;
import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

public class Splash extends AppCompatActivity {
    private final int SPLASH_DISPLAY_LENGTH = 2500;
    private SliderPrefManager myPrefManager;
    private boolean isUserSeeingAcivity;
    private boolean isClearToMoveToNextActivity;



    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);

        isUserSeeingAcivity=true;
        isClearToMoveToNextActivity = false;

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                isClearToMoveToNextActivity = true;
                if(isUserSeeingAcivity) goToNextActivity();
            }
        },SPLASH_DISPLAY_LENGTH);
    }

    @Override
    protected void onPause() {
        isUserSeeingAcivity = false;
        super.onPause();
    }

    @Override
    protected void onResume() {
        isUserSeeingAcivity = true;
        if(isClearToMoveToNextActivity) goToNextActivity();
        super.onResume();
    }

    private void goToNextActivity(){
        ObjectAnimator colorFade = ObjectAnimator.ofObject(findViewById(R.id.pageID)
                , "backgroundColor" /*view attribute name*/,
                new ArgbEvaluator(),
                getApplicationContext().getResources().getColor(R.color.colorPrimaryDark) /*from color*/
                , getApplicationContext().getResources().getColor(R.color.colorPrimaryDark2) /*to color*/);
        colorFade.setDuration(1100);
        colorFade.start();

        startNextActivity();

    }

    private void startNextActivity(){
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
        },1110);

    }

    private void hideNavBars() {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        decorView.setSystemUiVisibility(uiOptions);
    }

}
