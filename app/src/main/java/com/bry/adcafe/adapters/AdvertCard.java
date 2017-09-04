package com.bry.adcafe.adapters;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.bry.adcafe.Constants;
import com.bry.adcafe.R;
import com.bry.adcafe.Variables;
import com.bry.adcafe.models.Advert;
import com.bumptech.glide.Glide;
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
    @View(R.id.bookmark2Btn) private ImageButton bookmarkButton;

    private Advert mAdvert;
    private Context mContext;
    private SwipePlaceHolderView mSwipeView;
    private Integer adTotal;
    private static final String START_TIMER= "startTimer";
    private static final String AD_TO_TOTAL= "adToTotal";
    private static boolean clickable;
    private static String mLastOrNotLast;

    public AdvertCard(Context context, Advert advert, SwipePlaceHolderView swipeView,String lastOrNotLast){
        mContext = context;
        mAdvert = advert;
        mSwipeView = swipeView;
        mLastOrNotLast = lastOrNotLast;
    }


    @Resolve
    private void onResolved(){
        if(mLastOrNotLast == Constants.LAST){
            Log.d("ADVERT_CARD--","LOADING ONLY LAST AD.");
            Glide.with(mContext).load(mAdvert.getImageUrl()).bitmapTransform(new RoundedCornersTransformation(mContext, Utils.dpToPx(4), 0,
                    RoundedCornersTransformation.CornerType.TOP))
                    .into(profileImageView);
            mSwipeView.lockViews();
            clickable=false;
        }else{
            Log.d("ADVERT_CARD--","LOADING ALL ADS NORMALLY.");
            Glide.with(mContext).load(mAdvert.getImageUrl()).bitmapTransform(new RoundedCornersTransformation(mContext, Utils.dpToPx(4), 0,
                    RoundedCornersTransformation.CornerType.TOP))
                    .into(profileImageView);
            sendBroadcast(START_TIMER);
            LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverForTimerHasEnded,new IntentFilter(Constants.TIMER_HAS_ENDED));
            clickable=false;
        }
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverToUnregisterAllReceivers,new IntentFilter(Constants.UNREGISTER_ALL_RECEIVERS));


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
            Intent intent = new Intent(Constants.ADVERT_CARD_BROADCAST_TO_START_TIMER);
            mSwipeView.lockViews();
            clickable = false;

            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
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
