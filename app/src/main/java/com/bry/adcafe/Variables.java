package com.bry.adcafe;

/**
 * Created by bryon on 26/08/2017.
 */

public class Variables {
    public static int numberOfAds;
    public static boolean hasNumberOfAdsChanged = false;
    public static Integer adTotal = 0;
    public static String mIsLastOrNotLast;

    public static void setNewNumberOfAds(int number){
        numberOfAds = number;
    }

    public static void removeAd(){
        setHasNumberOfAdsChangedTrue();
        numberOfAds-=1;
    }

    public static void adAdToTotal(){
        adTotal+=1;
    }

    public static void setIsLastOrNotLast(String isLastOrNotLast){
        mIsLastOrNotLast = isLastOrNotLast;
    }

    public static void setHasNumberOfAdsChangedTrue() {
        hasNumberOfAdsChanged = true;
    }

    public static void  setHasNumberOfAdsChangedFalse(){
        hasNumberOfAdsChanged = false;
    }



}
