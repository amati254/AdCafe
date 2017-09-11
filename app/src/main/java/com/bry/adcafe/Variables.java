package com.bry.adcafe;

/**
 * Created by bryon on 26/08/2017.
 */

public class Variables {
    public static int numberOfAds;
    public static Integer adTotal = 0;
    public static String mIsLastOrNotLast;
    public static boolean hasBeenPinned;


    public static void setNewNumberOfAds(int number){
        numberOfAds = number;
    }

    public static void removeAd(){
        numberOfAds-=1;
    }

    public static void  setAdTotal(int number){
        adTotal = number;
    }

    public static  void clearAdTotal(){
        adTotal = 0;
    }

    public static void adAdToTotal(){
        adTotal+=1;
    }

    public static void setIsLastOrNotLast(String isLastOrNotLast){
        mIsLastOrNotLast = isLastOrNotLast;
    }




}
