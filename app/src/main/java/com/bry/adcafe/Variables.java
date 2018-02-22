package com.bry.adcafe;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.util.Log;

import com.braintreepayments.cardform.view.CardForm;
import com.bry.adcafe.adapters.AdvertCard;
import com.bry.adcafe.adapters.SavedAdsCard;
import com.bry.adcafe.models.Advert;
import com.mindorks.placeholderview.PlaceHolderView;

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
    private static int mTotalReimbursementAmount = 0;
    private static String lastSeenAd;
    private static String lastAdOfList;
    private static int currentAdNumberForAllAdsList = 0;
    private static Advert currentAdvert;

    public static String mIsLastOrNotLast;
    public static boolean hasBeenPinned = false;
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
    public static String areYouSureTakeDownText;
    public static Advert lastAdSeen;

    public static boolean didAdCafeRemoveCategory = false;
    public static boolean didAdCafeAddNewCategory = false;
    public static PlaceHolderView placeHolderView;
    public static long noOfDays;
    public static int position;

    public static boolean isLoginOnline = false;
    public static boolean hasChangesBeenMadeToCategories = false;
    public static SavedAdsCard adView;
    public static long lastNoOfDays;
    public static List<Long> daysArray = new ArrayList<>();
    public static long previousDaysNumber;

    public static int width;
    public static LinkedHashMap<Long,List> VariablesHashOfAds = new LinkedHashMap<>();
    public static boolean isLocked = false;
    public static boolean isAllClearToContinueCountDown = true;
    public static boolean hasFinishedLoadingBlurredImages = false;
    public static Advert firstAd;
    public static Point windowSize;

    public static String imageToBeShared;
    public static int numberOfUnpinns = 0;
    public static boolean isNormalAdsBeingSeen = true;
    public static int constantAmountPerView = 3;
    public static int amountToPayPerTargetedView = 5;
    public static boolean doesUserWantNotifications = true;

    public static int preferredHourOfNotf = 7;
    public static int preferredMinuteOfNotf = 30;
    public static String userName;

    public static String cardNumber;
    public static String expiration;
    public static String cvv;
    public static String postalCode;

    public static String phoneNo;
    private static String password;
    public static double amountToPayForUpload;
    public static Advert adToBeReimbursed;
    public static boolean isGottenNewPasswordFromLogInOrSignUp = false;

    public static void resetAllValues(){
         todaysAdTotal = 0;
         mMonthAdTotals = 0;
         mTotalReimbursementAmount = 0;
         lastSeenAd = null;
         lastAdOfList = null;
         currentAdNumberForAllAdsList = 0;
         currentAdvert = null;
         allAdsList.clear();
         Subscriptions.clear();
         currentAdNumberForAllAdsList = 0;
         currentSubscriptionIndex = 0;
         currentAdInSubscription = 0;
         lastAdSeen = null;
         VariablesHashOfAds.clear();
         firstAd = null;
         constantAmountPerView = 0;
         amountToPayPerTargetedView = 0;
         userName = null;
         phoneNo = null;
         password = null;
         amountToPayForUpload = 0;
         adToBeReimbursed = null;
    }



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
        currentAdNumberForAllAdsList = 0;
    }

    public static Advert getAdFromVariablesAdList(int i){
        return allAdsList.get(i);
    }

    public static void adOneToCurrentAdNumberForAllAdsList(){
        currentAdNumberForAllAdsList++;
    }

    public static int getSizeOfAdlist(){
        return allAdsList.size();
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
        Log.d("Variables","Set CurrentAdInSubscription to : "+currentAdInSubscription);
    }

    public static void adOneToCurrentAdInSubscription(){
        currentAdInSubscription+=1;
    }



    public static int getTotalReimbursementAmount() {
        return mTotalReimbursementAmount;
    }

    public static void setTotalReimbursementAmount(int mTotalReimbursementAmount) {
        Variables.mTotalReimbursementAmount = mTotalReimbursementAmount;
    }

    public static void addOneToTotalReimbursementAmount(String k) {
        if(k.equals(mKey)) mTotalReimbursementAmount+=(constantAmountPerView);
    }

    public static String getPassword() {
        return password;
    }

    public static void setPassword(String password) {
        Variables.password = password;
    }
}
