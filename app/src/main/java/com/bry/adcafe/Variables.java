package com.bry.adcafe;

import com.bry.adcafe.models.Advert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by bryon on 26/08/2017.
 */

public class Variables {
    private static String mKey = "";
    private static int numberOfAds;

    private static Integer todaysAdTotal = 0;
    private static int mMonthAdTotals = 0;
    private static String lastSeenAd;
    private static String lastAdOfList;
    public static int currentAdNumberForAllAdsList = 0;
    private static Advert currentAdvert;

    public static String mIsLastOrNotLast;
    public static boolean hasBeenPinned;
    public static boolean isMainActivityOnline;
    public static boolean isDashboardActivityOnline;

    public static boolean isVerifyingEmail;
    public static boolean isStartFromLogin = false;
    public static boolean hasTimerStarted = false;
    public static Advert adToBeShared;
    public static Advert adToBeViewed;
    public static Advert adToBeUnpinned;
    public static boolean isInfo = false;
    private static List<Advert> allAdsList = new ArrayList<>();

    public static LinkedHashMap<String,Integer> Subscriptions  = new LinkedHashMap<>();
    private static int currentSubscriptionIndex = 0;
    private static int currentAdInSubscription = 0;

    public static int nextSubscriptionIndex;
    public static String SelectedCategory;
    public static List<String> selectedCategoriesToSubscribeTo = new ArrayList<>();

    public static boolean isLockedBecauseOfFlagedAds = false;
    public static boolean isLockedBecauseOfNoMoreAds = false;
    public static String areYouSureText;
    public static Advert adToBeFlagged;




    public static void setNewNumberOfAds(int number){
        numberOfAds = number;
    }

    public static void removeAd(){
        numberOfAds-=1;
    }

    public static void  setAdTotal(int number,String key){
        if(key == mKey){
            todaysAdTotal = number;
        }
    }


    public static int getAdTotal(String key){
        if (key.equals(mKey)){
            return todaysAdTotal;
        }
        else{
            return 0;
        }
    }

    public static  void clearAdTotal(){
        todaysAdTotal = 0;
    }

    public static void adAdToTotal(String key){
        if(key==mKey){
            todaysAdTotal+=1;
        }
    }


    public static void setIsLastOrNotLast(String isLastOrNotLast){
        mIsLastOrNotLast = isLastOrNotLast;
    }

    public static void setMonthAdTotals(String key,int number){
        if(key == mKey){
            mMonthAdTotals = number;
        }
    }

    public static void adToMonthTotals(String key){
        if(key == mKey){
            mMonthAdTotals+=1;
        }
    }


    public static int getMonthAdTotals(String key){
        if(key == mKey){
            return mMonthAdTotals;
        }else{
            return 0;
        }
    }

    public static String getLastSeenAd(){
        return lastSeenAd;
    }

    public static void setLastSeenAd(String lastAd){
        lastSeenAd = lastAd;
    }

    public static String getLastAdOfList(){
        return lastAdOfList;
    }

    public static void setLastAdOfList(String lastAd){
        lastAdOfList = lastAd;
    }



    public static Advert getCurrentAdvert() {
        return currentAdvert;
    }

    public static void setCurrentAdvert(Advert currentAdvert) {
        Variables.currentAdvert = currentAdvert;
    }

    public static void adToVariablesAdList(Advert ad){
        allAdsList.add(ad);
    }



    public static void clearAllAdsFromAdList(){
        if(allAdsList.size()!=0) allAdsList.clear();
    }

    public static Advert getAdFromVariablesAdList(int i){
        return allAdsList.get(i);
    }

    public static void adOneToCurrentAdNumberForAllAdsList(){
        currentAdNumberForAllAdsList++;
    }



    public static void setCurrentAdNumberForAllAdsList(int number){
        currentAdNumberForAllAdsList = number;
    }

    public static int getCurrentAdNumberForAllAdsList(){
        return currentAdNumberForAllAdsList;
    }




    public static int getCurrentSubscriptionIndex() {
        return currentSubscriptionIndex;
    }

    public static void setCurrentSubscriptionIndex(int currentSubscriptionIndex) {
        Variables.currentSubscriptionIndex = currentSubscriptionIndex;
    }

    public static void setNextSubscriptionIndex(){
        currentSubscriptionIndex+=1;
    }



    public static int getCurrentAdInSubscription() {
        return currentAdInSubscription;
    }

    public static void setCurrentAdInSubscription(int currentAdInSubscription) {
        Variables.currentAdInSubscription = currentAdInSubscription;
    }

    public static void adOneToCurrentAdInSubscription(){
        currentAdInSubscription+=1;
    }
}
