package com.bry.adcafe.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.widget.ImageView;

import com.bry.adcafe.R;
import com.bry.adcafe.models.Advert;
import com.bumptech.glide.Glide;
import com.mindorks.placeholderview.PlaceHolderView;
import com.mindorks.placeholderview.annotations.Layout;
import com.mindorks.placeholderview.annotations.LongClick;
import com.mindorks.placeholderview.annotations.NonReusable;
import com.mindorks.placeholderview.annotations.Resolve;
import com.mindorks.placeholderview.annotations.View;

/**
 * Created by bryon on 05/09/2017.
 */

@NonReusable
@Layout(R.layout.saved_ads_list_item)
public class SavedAdsCard {
    @View(R.id.imageView) private ImageView imageView;

    private Context mContext;
    private PlaceHolderView mPlaceHolderView;
    private Advert mAdvert;
    private ProgressDialog mAuthProgressDialog;



    public SavedAdsCard(Advert advert, Context context, PlaceHolderView placeHolderView) {
        mAdvert = advert;
        mContext = context;
        mPlaceHolderView = placeHolderView;
    }

    @Resolve
    private void onResolved() {
        Glide.with(mContext).load(mAdvert.getImageUrl()).into(imageView);
    }

}
