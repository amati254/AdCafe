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
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.bry.adcafe.R;
import com.bry.adcafe.services.SliderPrefManager;
import com.crashlytics.android.Crashlytics;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import io.fabric.sdk.android.Fabric;

public class Splash extends AppCompatActivity {
    private final int SPLASH_DISPLAY_LENGTH = 3021;
    private SliderPrefManager myPrefManager;
    private boolean isUserSeeingAcivity;
    private boolean isClearToMoveToNextActivity;
    private TextView LSEText;
    private TextView LogoText;



    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);

        isUserSeeingAcivity=true;
        isClearToMoveToNextActivity = false;
        LSEText = (TextView) findViewById(R.id.LSEText);
        LogoText = (TextView) findViewById(R.id.logoText);

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
        //for background window
        ObjectAnimator colorFade = ObjectAnimator.ofObject(findViewById(R.id.pageID)
                , "backgroundColor" /*view attribute name*/,
                new ArgbEvaluator(),
                getApplicationContext().getResources().getColor(R.color.colorPrimaryDark2) /*from color*/
                , getApplicationContext().getResources().getColor(R.color.white) /*to color*/);
        colorFade.setDuration(600);

        //for the line
        ObjectAnimator colorFade2 = ObjectAnimator.ofObject(findViewById(R.id.lineView)
                , "backgroundColor" /*view attribute name*/,
                new ArgbEvaluator(),
                getApplicationContext().getResources().getColor(R.color.white) /*from color*/
                , getApplicationContext().getResources().getColor(R.color.colorPrimaryDark2) /*to color*/);
        colorFade2.setDuration(600);

        //for the bottom text
        ObjectAnimator colorFade3 = ObjectAnimator.ofObject(LSEText
                , "textColor" /*view attribute name*/,
                new ArgbEvaluator(),
                getApplicationContext().getResources().getColor(R.color.white) /*from color*/
                , getApplicationContext().getResources().getColor(R.color.colorPrimaryDark2) /*to color*/);
        colorFade3.setDuration(600);

        //for the logotext
        ObjectAnimator colorFade4 = ObjectAnimator.ofObject(LogoText
                , "textColor" /*view attribute name*/,
                new ArgbEvaluator(),
                getApplicationContext().getResources().getColor(R.color.white) /*from color*/
                , getApplicationContext().getResources().getColor(R.color.colorPrimaryDark2) /*to color*/);
        colorFade4.setDuration(600);

        colorFade.start();
        colorFade2.start();
        colorFade3.start();
        colorFade4.start();

        startNextActivity();
//        long currentTimeMillis = System.currentTimeMillis();
//        long extraTimeFromMidnight = currentTimeMillis%(1000*60*60*24);
//        long currentDay = (currentTimeMillis-extraTimeFromMidnight)/(1000*60*60*24);
//        getDateFromDays(currentDay);
    }

    private void startNextActivity(){
        //for the line
        long duration = 200;
        ObjectAnimator colorFade2 = ObjectAnimator.ofObject(findViewById(R.id.lineView)
                , "backgroundColor" /*view attribute name*/,
                new ArgbEvaluator(),
                getApplicationContext().getResources().getColor(R.color.colorPrimaryDark2) /*from color*/
                , getApplicationContext().getResources().getColor(R.color.white) /*to color*/);
        colorFade2.setDuration(duration);
        colorFade2.setStartDelay(600);

        //for the bottom text
        ObjectAnimator colorFade3 = ObjectAnimator.ofObject(LSEText
                , "textColor" /*view attribute name*/,
                new ArgbEvaluator(),
                getApplicationContext().getResources().getColor(R.color.colorPrimaryDark2) /*from color*/
                , getApplicationContext().getResources().getColor(R.color.white) /*to color*/);
        colorFade3.setDuration(duration);
        colorFade3.setStartDelay(600);

        //for the logotext
        ObjectAnimator colorFade4 = ObjectAnimator.ofObject(LogoText
                , "textColor" /*view attribute name*/,
                new ArgbEvaluator(),
                getApplicationContext().getResources().getColor(R.color.colorPrimaryDark2) /*from color*/
                , getApplicationContext().getResources().getColor(R.color.white) /*to color*/);
        colorFade4.setDuration(duration);
        colorFade4.setStartDelay(600);

        colorFade2.start();
        colorFade3.start();
        colorFade4.start();

        NowReallyStartNextActivity();
    }

    private void NowReallyStartNextActivity(){
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
        },790);
    }

    private void hideNavBars() {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        decorView.setSystemUiVisibility(uiOptions);
    }

    private void getDateFromDays(long days){
        long currentTimeInMills = days*(1000*60*60*24);

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(currentTimeInMills);
        int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
        int monthOfYear = cal.get(Calendar.MONTH);
        int year = cal.get(Calendar.YEAR);

        String monthName = new DateFormatSymbols().getMonths()[monthOfYear];

        Log.d("Splash","Date is : "+dayOfMonth+" "+monthName+" "+year);
    }


}
