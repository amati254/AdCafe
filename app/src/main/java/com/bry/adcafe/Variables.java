package com.bry.adcafe;

/**
 * Created by bryon on 26/08/2017.
 */

public class Variables {
    private static String mKey = "";
    public static int numberOfAds;

    private static Integer todaysAdTotal = 0;
    private static int mMonthAdTotals = 0;

    public static String mIsLastOrNotLast;
    public static boolean hasBeenPinned;



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
        if (key==mKey){
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


}
