package com.bry.adcafe.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by bryon on 6/4/2017.
 */

public class User {
    private static String mKey = "";
    private static String mUid;
    private static int mClusterID;
    private HashMap <String,Integer>mAdCategorySubscriptionsAndCorrespondingCluster;
    private int TodaysTotalAds;
    private int TOTAL_NO_OF_ADS_SEEN_All_MONTH;
    private String DATE_IN_FIREBASE;


//    public User(int TodaysTotalAds,int MonthTotals,String Date,List mAdCategorySubscriptions){
//        this.TodaysTotalAds = TodaysTotalAds;
//        this.TOTAL_NO_OF_ADS_SEEN_All_MONTH = MonthTotals;
//        this.DATE_IN_FIREBASE = Date;
//        this.mAdCategorySubscriptions = mAdCategorySubscriptions;
//
//    }

    public static void setID(int number,String Key){
        if(Key == mKey){
            mClusterID = number;
        }
    }

    public static int getClusterID(String Key){
         if(Key == mKey){
             return mClusterID;
         }else{
             return 0;
         }
     }

    public static void setUid(String uid){
         mUid = uid;
     }

    public static String getUid(){
         return mUid;
     }

    public void setSubscriptionList(HashMap subscriptionList){
        this.mAdCategorySubscriptionsAndCorrespondingCluster = subscriptionList;
    }





    public int getTodaysTotalAds() {
        return TodaysTotalAds;
    }

    public void setTodaysTotalAds(int todaysTotalAds) {
        TodaysTotalAds = todaysTotalAds;
    }

    public int getTOTAL_NO_OF_ADS_SEEN_All_MONTH() {
        return TOTAL_NO_OF_ADS_SEEN_All_MONTH;
    }

    public void setTOTAL_NO_OF_ADS_SEEN_All_MONTH(int TOTAL_NO_OF_ADS_SEEN_All_MONTH) {
        this.TOTAL_NO_OF_ADS_SEEN_All_MONTH = TOTAL_NO_OF_ADS_SEEN_All_MONTH;
    }

    public String getDATE_IN_FIREBASE() {
        return DATE_IN_FIREBASE;
    }

    public void setDATE_IN_FIREBASE(String DATE_IN_FIREBASE) {
        this.DATE_IN_FIREBASE = DATE_IN_FIREBASE;
    }
}
