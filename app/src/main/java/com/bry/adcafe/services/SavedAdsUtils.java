package com.bry.adcafe.services;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.bry.adcafe.Constants;
import com.bry.adcafe.models.Advert;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by bryon on 05/09/2017.
 */

public class SavedAdsUtils {
    private static final String TAG = "SavedAdsUtils";

    public static List<Advert> loadSavedAdverts(Context context){
        try{
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            JSONArray array = new JSONArray(loadJSONFromAsset(context, "SavedAds.json"));
            List<Advert> adList = new ArrayList<>();
            for(int i=0;i<array.length();i++){
                Advert profile = gson.fromJson(array.getString(i), Advert.class);
                adList.add(profile);
                profile.setNumberOfAds(array.length());
            }
            return adList;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    private static String loadJSONFromAsset(Context context, String jsonFileName) {
        String json = null;
        InputStream is=null;
        try {
            AssetManager manager = context.getAssets();
            Log.d(TAG,"path "+jsonFileName);
            is = manager.open(jsonFileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    public static List<Advert> loadAdsFromFirebase(){
        try{
            final List<Advert> adList = new ArrayList<>();
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            String uid = user.getUid();
            Query query = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS).child(uid).child(Constants.PINNED_AD_LIST);
            DatabaseReference mRef = query.getRef();
            mRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot snap: dataSnapshot.getChildren()){
                        Advert advert = snap.getValue(Advert.class);
                        advert.getImageUrl();
                        adList.add(advert);
                        Log.d("UTILS"," --Loaded ads from firebase.--"+advert.getImageUrl());
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d("UTILS","Failed to load ads from firebase.");
                }
            });

            return adList;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
