package com.bry.adcafe.models;

import android.graphics.Bitmap;

import com.google.firebase.database.Exclude;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by bryon on 6/4/2017.
 */

public class Advert {
    @SerializedName("url")
    @Expose
    private String imageUrl;

    private int numberOfAds = 0 ;
    private String pushId;
    private int pushIdNumber;
    private Bitmap imageBitmap;
    private int numberOfTimesSeen;
    private int numberOfUsersToReach;
    private String pushRefInAdminConsole;
    private String userEmail;
    private String websiteLink;
    private String category;
    private boolean isFlagged = false;
    private boolean isAdminFlagged = false;
    public LinkedHashMap<Integer,Integer> clusters  = new LinkedHashMap<>();
    private boolean hasBeenReimbursed;

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


    public void setNumberOfTimesSeen(int number){
        numberOfTimesSeen = number;
    }

    public int getNumberOfTimesSeen(){
        return  numberOfTimesSeen;
    }




    public String getPushRefInAdminConsole() {
        return pushRefInAdminConsole;
    }

    public void setPushRefInAdminConsole(String pushRefInAdminConsole) {
        this.pushRefInAdminConsole = pushRefInAdminConsole;
    }



    public int getNumberOfUsersToReach() {
        return numberOfUsersToReach;
    }

    public void setNumberOfUsersToReach(int numberOfUsersToReach) {
        this.numberOfUsersToReach = numberOfUsersToReach;
    }



    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }



    public String getWebsiteLink() {
        if(websiteLink == null){
            return null;
        }else{
            return websiteLink;
        }
    }

    public void setWebsiteLink(String websiteLink) {
        this.websiteLink = websiteLink;
    }


    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isFlagged() {
        return isFlagged;
    }

    public void setFlagged(boolean flagged) {
        isFlagged = flagged;
    }

    public int getPushIdNumber() {
        return pushIdNumber;
    }

    public void setPushIdNumber(int pushIdNumber) {
        this.pushIdNumber = pushIdNumber;
    }

    public boolean isAdminFlagged() {
        return isAdminFlagged;
    }

    public void setAdminFlagged(boolean adminFlagged) {
        isAdminFlagged = adminFlagged;
    }

    public boolean isHasBeenReimbursed() {
        return hasBeenReimbursed;
    }

    public void setHasBeenReimbursed(boolean hasBeenReimbursed) {
        this.hasBeenReimbursed = hasBeenReimbursed;
    }
}
