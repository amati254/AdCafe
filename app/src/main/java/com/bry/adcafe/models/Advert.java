package com.bry.adcafe.models;

import android.graphics.Bitmap;

import com.google.firebase.database.Exclude;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by bryon on 6/4/2017.
 */

public class Advert {
    @SerializedName("url")
    @Expose
    private String imageUrl;

    private int numberOfAds = 0 ;
    private String pushId;
    private Bitmap imageBitmap;

    public Advert(String ImageUrl){
        this.imageUrl = ImageUrl;
    }
    public Advert(){}

    public int getNumberOfAds(){
        return numberOfAds;
    }

    public void setNumberOfAds(int setnumberOfAds){
        numberOfAds = setnumberOfAds;
    }



    public void removeAd(){
        numberOfAds -=1;
    }



    public String getPushId(){ return pushId; }

    public void setPushId(String pushId){ this.pushId = pushId; }



    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Bitmap getImageBitmap(){
        return imageBitmap;
    }

    public void setImageBitmap(Bitmap bm){
        imageBitmap = bm;
    }
}
