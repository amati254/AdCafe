package com.bry.adcafe.adapters;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bry.adcafe.Constants;
import com.bry.adcafe.R;
import com.bry.adcafe.Variables;
import com.bry.adcafe.models.Advert;
import com.bry.adcafe.services.ConnectionChecker;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.target.Target;
import com.mindorks.placeholderview.SwipePlaceHolderView;
import com.mindorks.placeholderview.Utils;
import com.mindorks.placeholderview.annotations.Click;
import com.mindorks.placeholderview.annotations.Layout;
import com.mindorks.placeholderview.annotations.Resolve;
import com.mindorks.placeholderview.annotations.View;
import com.mindorks.placeholderview.annotations.swipe.SwipeIn;
import com.mindorks.placeholderview.annotations.swipe.SwipeInState;
import com.mindorks.placeholderview.annotations.swipe.SwipeOut;
import com.mindorks.placeholderview.annotations.swipe.SwipeOutState;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

/**
 * Created by bryon on 6/11/2017.
 */


@Layout(R.layout.ad_card_view)
public class AdvertCard{
    @View(R.id.profileImageView) private ImageView profileImageView;
//    @View(R.id.reportBtn) private ImageButton reportButton;
//    @View(R.id.bookmark2Btn) private ImageButton bookmarkButton;

    @View(R.id.errorImageView) private ImageView errorImageView;
    @View(R.id.pbCardProgress) private ProgressBar mProgressBar;

    private Advert mAdvert;
    private Context mContext;
    private SwipePlaceHolderView mSwipeView;
    private Integer adTotal;
    private static final String START_TIMER= "startTimer";
    private static final String AD_TO_TOTAL= "adToTotal";
    private static boolean clickable;
    private static String mLastOrNotLast;
    private static boolean hasAdLoaded;

    public AdvertCard(Context context, Advert advert, SwipePlaceHolderView swipeView,String lastOrNotLast){
        mContext = context;
        mAdvert = advert;
        mSwipeView = swipeView;
        mLastOrNotLast = lastOrNotLast;
    }


    @Resolve
    private void onResolved(){
        if(mLastOrNotLast == Constants.LAST){
           loadOnlyLastAd();
        }else{
            loadAllAds();
        }
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverToUnregisterAllReceivers,new IntentFilter(Constants.UNREGISTER_ALL_RECEIVERS));

    }

    private void loadAllAds(){
        Log.d("ADVERT_CARD--","LOADING ALL ADS NORMALLY.");

        mProgressBar.setVisibility(android.view.View.VISIBLE);
        Glide.with(mContext).load(mAdvert.getImageUrl()).bitmapTransform(new RoundedCornersTransformation(mContext,Utils.dpToPx(4),0,
                RoundedCornersTransformation.CornerType.TOP))
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        errorImageView.setVisibility(android.view.View.VISIBLE);
                        mProgressBar.setVisibility(android.view.View.GONE);
                        hasAdLoaded = false;
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        mProgressBar.setVisibility(android.view.View.GONE);
                        sendBroadcast(START_TIMER);
                        hasAdLoaded = true;
                        return false;
                    }
                })
                .into(profileImageView);
//        mProgressBar.setVisibility(android.view.View.GONE);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverForTimerHasEnded,new IntentFilter(Constants.TIMER_HAS_ENDED));
        clickable=false;

    }

    private void loadOnlyLastAd(){
        Log.d("ADVERT_CARD--","LOADING ONLY LAST AD.");
        Glide.with(mContext).load(mAdvert.getImageUrl()).bitmapTransform(new RoundedCornersTransformation(mContext, Utils.dpToPx(4), 0,
                RoundedCornersTransformation.CornerType.TOP))
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        errorImageView.setVisibility(android.view.View.VISIBLE);
                        mProgressBar.setVisibility(android.view.View.GONE);
                        hasAdLoaded = false;
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        mProgressBar.setVisibility(android.view.View.GONE);
                        hasAdLoaded = true;
                        return false;
                    }
                })
                .into(profileImageView);
        mSwipeView.lockViews();
        clickable=false;
    }

    @Click(R.id.profileImageView)
    private void onClick(){
        Log.d("EVENT", "profileImageView click");
        if(clickable){
            mSwipeView.enableTouchSwipe();
        }
    }


    @SwipeOut
    private void onSwipedOut(){
        Log.d("EVENT----", "onSwipedOut");
        Variables.removeAd();
        sendBroadcast(START_TIMER);
    }

    @SwipeIn
    private void onSwipeIn(){
        Log.d("EVENT----", "onSwipedIn");
        Variables.removeAd();
        sendBroadcast(START_TIMER);
    }

    private void sendBroadcast(String message ) {
        if(message == START_TIMER && mLastOrNotLast == Constants.NOT_LAST){
            Log.d("AdvertCard - ","Sending message to start timer");
            mSwipeView.lockViews();
            clickable = false;
            if(hasAdLoaded){
                Intent intent = new Intent(Constants.ADVERT_CARD_BROADCAST_TO_START_TIMER);
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
            }
        }
    }


    private BroadcastReceiver mMessageReceiverForTimerHasEnded = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
               Log.d("ADVERT_CARD--","message from adCounterBar that timer has ended has been received.");
                if(mSwipeView.getChildCount() > 1){
                    mSwipeView.unlockViews();
                    clickable = true;
                }
        }
    };

    private BroadcastReceiver mMessageReceiverToUnregisterAllReceivers = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("ADVERT_CARD--","Received broadcast to Unregister all receivers");
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForTimerHasEnded);
        }
    };

    @SwipeInState
    private void onSwipeInState(){
        Log.d("EVENT", "onSwipeInState");
    }

    @SwipeOutState
    private void onSwipeOutState(){
        Log.d("EVENT", "onSwipeOutState");
    }
}
