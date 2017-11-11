package com.bry.adcafe.services;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by bryon on 05/11/2017.
 */

public class SliderPrefManager {
    SharedPreferences myPref;
    SharedPreferences myPref2;
    SharedPreferences.Editor myEditor;
    SharedPreferences.Editor myEditor2;

    Context myContext;

    // shared pref mode
    int PRIVATE_MODE = 0;

    // Shared preferences file name
    private static final String PREF_NAME = "rich_wizzie-welcome";
    private static final String PREF_NAME2 = "dice_in_11_airbags";
    private static final String IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch";
    private static final String IS_FIRST_TIME_LAUNCH_IN_ADVERTISE = "isFirstTimeLaunchForAdvertising";

    public SliderPrefManager(Context context){
        this.myContext = context;
        myPref = myContext.getSharedPreferences(PREF_NAME,PRIVATE_MODE);
        myPref2 = myContext.getSharedPreferences(PREF_NAME2,PRIVATE_MODE);
        myEditor2 = myPref2.edit();
        myEditor = myPref.edit();
    }

    public void setFirstTimeLaunch(boolean isFirstTime){
        myEditor.putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime);
        myEditor.commit();
    }

    public void setIsFirstTimeLaunchInAdvertise(boolean isFirstTimeLaunchInAdvertise){
        myEditor2.putBoolean(IS_FIRST_TIME_LAUNCH_IN_ADVERTISE,isFirstTimeLaunchInAdvertise);
        myEditor2.commit();
    }

    public boolean isFirstTimeLaunch(){
        return myPref.getBoolean(IS_FIRST_TIME_LAUNCH,true);
    }

    public boolean isFirstTimeLaunchForAdvertisers(){
        return myPref2.getBoolean(IS_FIRST_TIME_LAUNCH_IN_ADVERTISE,true);
    }

}
