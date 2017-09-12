package com.bry.adcafe.adapters;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bry.adcafe.Constants;
import com.bry.adcafe.R;
import com.bry.adcafe.models.Advert;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mindorks.placeholderview.PlaceHolderView;
import com.mindorks.placeholderview.annotations.Layout;
import com.mindorks.placeholderview.annotations.LongClick;
import com.mindorks.placeholderview.annotations.NonReusable;
import com.mindorks.placeholderview.annotations.Resolve;
import com.mindorks.placeholderview.annotations.View;
import com.wang.avi.AVLoadingIndicatorView;

import java.net.InetAddress;

import butterknife.OnItemLongClick;

/**
 * Created by bryon on 05/09/2017.
 */

@NonReusable
@Layout(R.layout.saved_ads_list_item)
public class SavedAdsCard {
    @View(R.id.SavedImageView) private ImageView imageView;
    @View(R.id.savedErrorImageView) private ImageView errorImageView;
    @View(R.id.savedAdCardAvi) private AVLoadingIndicatorView mAvi;

    private Context mContext;
    private PlaceHolderView mPlaceHolderView;
    private Advert mAdvert;
    public String mId;
    private ProgressDialog mAuthProgressDialog;
    private boolean hasMessageBeenSeen = false;



    public SavedAdsCard(Advert advert, Context context, PlaceHolderView placeHolderView,String pinID) {
        mAdvert = advert;
        mContext = context;
        mPlaceHolderView = placeHolderView;
//        mId = pinID;
    }

    @Resolve
    private void onResolved() {
       loadImage();
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverForRemovePinnedAd,new IntentFilter(Constants.REMOVE_PINNED_AD));
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverToUnregisterAllReceivers,new IntentFilter(Constants.UNREGISTER_ALL_RECEIVERS));

    }

    @LongClick(R.id.SavedImageView)
    private void onLongClick(){
        Log.d("SAVED_ADS_CARD--","Push id is--  "+mAdvert.getPushId());
        unPin();
    }


    private void loadImage(){
        mAvi.setVisibility(android.view.View.VISIBLE);
//        Log.d("SAVED_ADS_CARD--","Push id is--"+mAdvert.getPushId());
        Glide.with(mContext).load(mAdvert.getImageUrl())
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        mAvi.setVisibility(android.view.View.GONE);
                        errorImageView.setVisibility(android.view.View.VISIBLE);
                        if(!isInternetAvailable()&& !hasMessageBeenSeen){
                            hasMessageBeenSeen = true;
                            Intent intent = new Intent(Constants.CONNECTION_OFFLINE);
                            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                        }
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        mAvi.setVisibility(android.view.View.GONE);
                        errorImageView.setVisibility(android.view.View.GONE);
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

    private BroadcastReceiver mMessageReceiverForRemovePinnedAd = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//            unPin();
        }
    };

    private BroadcastReceiver mMessageReceiverToUnregisterAllReceivers = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("SAVED_ADS_CARD--","Received broadcast to Unregister all receivers");
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForRemovePinnedAd);
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverToUnregisterAllReceivers);
        }
    };


    private void unPin(){
        String id = mAdvert.getPushId();
        mPlaceHolderView.removeView(this);
        Log.d("SAVED_ADS_CARD--","Removing pinned ad"+id);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();

        DatabaseReference adRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS).child(uid).child(Constants.PINNED_AD_LIST).child(id);
        adRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dataSnapshot.getRef().removeValue();
                Intent intent = new Intent(Constants.REMOVE_PINNED_AD);
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Intent intent = new Intent(Constants.UNABLE_TO_REMOVE_PINNED_AD);
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
            }
        });

    }

}
