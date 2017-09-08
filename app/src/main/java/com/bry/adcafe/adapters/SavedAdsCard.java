package com.bry.adcafe.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bry.adcafe.Constants;
import com.bry.adcafe.R;
import com.bry.adcafe.models.Advert;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.mindorks.placeholderview.PlaceHolderView;
import com.mindorks.placeholderview.annotations.Layout;
import com.mindorks.placeholderview.annotations.LongClick;
import com.mindorks.placeholderview.annotations.NonReusable;
import com.mindorks.placeholderview.annotations.Resolve;
import com.mindorks.placeholderview.annotations.View;

import java.net.InetAddress;

/**
 * Created by bryon on 05/09/2017.
 */

@NonReusable
@Layout(R.layout.saved_ads_list_item)
public class SavedAdsCard {
    @View(R.id.SavedImageView) private ImageView imageView;
    @View(R.id.savedErrorImageView) private ImageView errorImageView;
    @View(R.id.pbSavedCardProgress) private ProgressBar mProgressBar;



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
        mProgressBar.setVisibility(android.view.View.VISIBLE);
        Glide.with(mContext).load(mAdvert.getImageUrl())
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                       mProgressBar.setVisibility(android.view.View.GONE);
                        errorImageView.setVisibility(android.view.View.VISIBLE);
                        if(!isInternetAvailable()){
                           Intent intent = new Intent(Constants.CONNECTION_OFFLINE);
                            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                        }
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        mProgressBar.setVisibility(android.view.View.GONE);
                        return false;
                    }
                })
                .into(imageView);
    }

    private boolean isInternetAvailable() {
        try {
            InetAddress ipAddr = InetAddress.getByName("google.com");
            return !ipAddr.toString().equals("");
        } catch (Exception e) {
            return false;
        }

    }

}
