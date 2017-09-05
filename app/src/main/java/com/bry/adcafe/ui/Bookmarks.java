package com.bry.adcafe.ui;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.bry.adcafe.R;
import com.bry.adcafe.adapters.SavedAdsCard;
import com.bry.adcafe.models.Advert;
import com.bry.adcafe.services.SavedAdsUtils;
import com.bry.adcafe.services.Utils;
import com.mindorks.placeholderview.PlaceHolderView;

import java.util.List;

public class Bookmarks extends AppCompatActivity {
    private Context mContext;
    private PlaceHolderView mPlaceHolderView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmarks);

        loadPlaceHolderViews();
        loadBookmarkedAdsFromJSONFile();

    }

    private void loadPlaceHolderViews() {
        mPlaceHolderView = (PlaceHolderView) findViewById(R.id.PlaceHolderView);
        mContext = getApplicationContext();

    }


    private void loadBookmarkedAdsFromJSONFile() {
        if(mPlaceHolderView == null){
            loadPlaceHolderViews();
        }
        if((SavedAdsUtils.loadSavedAdverts(this.getApplicationContext()))!= null) {
            List<Advert> savedAdList = SavedAdsUtils.loadSavedAdverts(this.getApplicationContext());
            Log.d("BOOKMARKS_ACTIVITY","loading:"+savedAdList.size()+"ads");

            for(int i = 0; i<savedAdList.size() ; i++){
                mPlaceHolderView.addView(new SavedAdsCard(savedAdList.get(i),mContext,mPlaceHolderView));
            }
        }else{
            Toast.makeText(mContext, "You have no saved ads.", Toast.LENGTH_SHORT).show();
        }
    }
}
