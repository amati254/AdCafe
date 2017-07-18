package com.bry.adstudio.services;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.bry.adstudio.models.Advert;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by bryon on 6/11/2017.
 */

public class Utils {
    private static final String TAG = "Utils";

    public static List<Advert> loadAds(Context context){
        try{
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            JSONArray array = new JSONArray(loadJSONFromAsset(context, "Adverts.json"));
            List <Advert> AdvertsList = new ArrayList<>();

            for(int i = 0; i < array.length(); i++ ){
                Advert advert = gson.fromJson(array.getString(i), Advert.class);
                AdvertsList.add(advert);
            }
            return AdvertsList;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    private static String loadJSONFromAsset(Context context, String jsonFileName) {
        String json = null;
        InputStream is= null;
        try{
            AssetManager manager = context.getAssets();
            Log.d(TAG,"path "+ jsonFileName);
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
}
