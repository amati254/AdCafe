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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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
import com.wang.avi.AVLoadingIndicatorView;

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
//    @View(R.id.pbCardProgress) private ProgressBar mProgressBar;
    @View(R.id.adCardAvi) private AVLoadingIndicatorView mAvi;

    private Advert mAdvert;
    private Context mContext;
    private SwipePlaceHolderView mSwipeView;
    private static final String START_TIMER= "startTimer";

    private static boolean clickable;
    private static String mLastOrNotLast;
    private static boolean hasAdLoaded;
    private boolean hasBeenSwiped = true;


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
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverToPinAd,new IntentFilter(Constants.PIN_AD));
        Variables.hasBeenPinned = false;
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverForTimerHasEnded,new IntentFilter(Constants.TIMER_HAS_ENDED));
    }

    private void loadAllAds(){
        Log.d("ADVERT_CARD--","LOADING ALL ADS NORMALLY.");

        mAvi.setVisibility(android.view.View.VISIBLE);
        Glide.with(mContext).load(mAdvert.getImageUrl()).bitmapTransform(new RoundedCornersTransformation(mContext,Utils.dpToPx(4),0,
                RoundedCornersTransformation.CornerType.TOP))
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        Log.d("ADVERT_CARD--","The image has failed to load due to network error."+e.getMessage());
                        errorImageView.setVisibility(android.view.View.VISIBLE);
                        mAvi.setVisibility(android.view.View.GONE);
//                        mSwipeView.unlockViews();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                       Log.d("ADVERT_CARD--","The image has loaded successfully");
                        mAvi.setVisibility(android.view.View.GONE);
                        errorImageView.setVisibility(android.view.View.GONE);

                        sendBroadcast(START_TIMER);
                        clickable=false;
                        return false;
                    }
                })
                .into(profileImageView);
    }

    private void loadOnlyLastAd(){
        Log.d("ADVERT_CARD--","LOADING ONLY LAST AD.");
        mAvi.setVisibility(android.view.View.VISIBLE);
        Glide.with(mContext).load(mAdvert.getImageUrl()).bitmapTransform(new RoundedCornersTransformation(mContext, Utils.dpToPx(4), 0,
                RoundedCornersTransformation.CornerType.TOP))
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        errorImageView.setVisibility(android.view.View.VISIBLE);
//                        mProgressBar.setVisibility(android.view.View.GONE);
                        mAvi.setVisibility(android.view.View.GONE);
                        hasAdLoaded = false;
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
//                        mProgressBar.setVisibility(android.view.View.GONE);
                        mAvi.setVisibility(android.view.View.GONE);
                        errorImageView.setVisibility(android.view.View.GONE);
                        hasAdLoaded = true;
                        return false;
                    }
                })
                .into(profileImageView);
        mSwipeView.lockViews();
        clickable=false;
        sendBroadcast(Constants.LAST);
    }

    @Click(R.id.profileImageView)
    private void onClick(){
        Log.d("EVENT", "profileImageView click");
            if (clickable) {
                mSwipeView.enableTouchSwipe();
                hasBeenSwiped = true;
            }
    }

    @SwipeOut
    private void onSwipedOut(){
        Log.d("EVENT----", "onSwipedOut");
            Variables.removeAd();
            hasBeenSwiped = true;
            sendBroadcast(START_TIMER);
    }

    @SwipeIn
    private void onSwipeIn(){
        Log.d("EVENT----", "onSwipedIn");
            Variables.removeAd();
            hasBeenSwiped = true;
            sendBroadcast(START_TIMER);
    }

    private void sendBroadcast(String message ) {
        if(message == START_TIMER && hasBeenSwiped){
            Log.d("AdvertCard - ","Sending message to start timer");
            mSwipeView.lockViews();
            clickable = false;
                Intent intent = new Intent(Constants.ADVERT_CARD_BROADCAST_TO_START_TIMER);
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);

        }else if(message == Constants.LAST){
            Intent intent = new Intent(Constants.LAST);
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
                    hasBeenSwiped = false;
                }else if(mSwipeView.getChildCount()==1){
                    sendBroadcast(Constants.LAST);
                }
        }
    };

    private BroadcastReceiver mMessageReceiverToUnregisterAllReceivers = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("ADVERT_CARD--","Received broadcast to Unregister all receivers");
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForTimerHasEnded);
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverToPinAd);

        }
    };

    private BroadcastReceiver mMessageReceiverToPinAd = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("ADVERT_CARD--","Received broadcast to Pin ad.");
            if(!Variables.hasBeenPinned){
                pinAd();
                Variables.hasBeenPinned = true;
            }
        }
    };

    private void pinAd() {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();

        DatabaseReference adRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS).child(uid).child(Constants.PINNED_AD_LIST);
        DatabaseReference pushRef = adRef.push();
        String pushId  = pushRef.getKey();
        mAdvert.setPushId(pushId);
        pushRef.setValue(mAdvert);

    }

    @SwipeInState
    private void onSwipeInState(){
        Log.d("EVENT", "onSwipeInState");
    }

    @SwipeOutState
    private void onSwipeOutState(){
        Log.d("EVENT", "onSwipeOutState");
    }

}
