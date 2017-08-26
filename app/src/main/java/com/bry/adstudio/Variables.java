package com.bry.adstudio;

/**
 * Created by bryon on 26/08/2017.
 */

public class Variables {
    public static int numberOfAds;

    public static void setNewNumberOfAds(int number){
        numberOfAds = number;
    }

    public static void removeAd(){
        numberOfAds-=1;
    }

}
