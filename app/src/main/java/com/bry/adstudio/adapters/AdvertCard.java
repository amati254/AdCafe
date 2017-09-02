package com.bry.adstudio.adapters;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.constraint.solver.widgets.ConstraintAnchor;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bry.adstudio.Constants;
import com.bry.adstudio.R;
import com.bry.adstudio.Variables;
import com.bry.adstudio.models.Advert;
import com.bry.adstudio.ui.MainActivity;
import com.bumptech.glide.Glide;
import com.mindorks.placeholderview.SwipePlaceHolderView;
import com.mindorks.placeholderview.Utils;
import com.mindorks.placeholderview.annotations.Click;
import com.mindorks.placeholderview.annotations.Layout;
import com.mindorks.placeholderview.annotations.Resolve;
import com.mindorks.placeholderview.annotations.View;
import com.mindorks.placeholderview.annotations.swipe.SwipeCancelState;
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
    @View(R.id.reportBtn) private ImageButton reportButton;
    @View(R.id.bookmark2Btn) private ImageButton bookmarkButton;

    private Advert mAdvert;
    private Context mContext;
    private SwipePlaceHolderView mSwipeView;
    private Integer adTotal = 0;
    private static final String START_TIMER= "startTimer";
    private static final String AD_TO_TOTAL= "adToTotal";
    private static boolean clickable;


    public AdvertCard(Context context, Advert advert, SwipePlaceHolderView swipeView){
        mContext = context;
        mAdvert = advert;
        mSwipeView = swipeView;
    }


    @Resolve
    private void onResolved(){
        Glide.with(mContext).load(mAdvert.getImageUrl()).bitmapTransform(new RoundedCornersTransformation(mContext, Utils.dpToPx(4), 0,
                RoundedCornersTransformation.CornerType.TOP))
                .into(profileImageView);
        sendBroadcast(START_TIMER);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiver,new IntentFilter(Constants.AD_COUNTER_BROADCAST));
        clickable=false;

    }

    @Click(R.id.profileImageView)
    private void onClick(){
        Log.d("EVENT", "profileImageView click");
        if(clickable == true){
            mSwipeView.enableTouchSwipe();
//            mSwipeView.addView(this);
        }
    }


    @SwipeOut
    private void onSwipedOut(){
        Log.d("EVENT----", "onSwipedOut");
        Variables.removeAd();
        Variables.adAdToTotal();
        sendBroadcast(AD_TO_TOTAL);
        sendBroadcast(START_TIMER);
    }

    @SwipeCancelState
    private void onSwipeCancelState(){
        Log.d("EVENT", "onSwipeCancelState");
    }

    @SwipeIn
    private void onSwipeIn(){
        Log.d("EVENT----", "onSwipedIn");
        Variables.removeAd();
        sendBroadcast(AD_TO_TOTAL);
        Variables.adAdToTotal();
        sendBroadcast(START_TIMER);
        bookmarkButton.callOnClick();
    }

    private void sendBroadcast(String message ) {
        Intent intent = new Intent(Constants.ADVERT_CARD_BROADCAST);

        if(message == AD_TO_TOTAL){
            Log.d("AdvertCard - ","Sending message to add to total");
            intent.putExtra(Constants.ADVERT_CARD_BROADCAST,Constants.AD_COUNTER_BROADCAST);
            intent.putExtra(Constants.AD_TOTAL,Integer.toString(Variables.adTotal));

            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        }else if(message == START_TIMER){
            Log.d("AdvertCard - ","Sending message to start timer");
            intent.putExtra(Constants.ADVERT_CARD_BROADCAST,Constants.AD_TIMER_BROADCAST);
            mSwipeView.lockViews();
            clickable = false;
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        }
    }


    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("AD_COUNTER_BAR - ","Broadcast has been received.");
            String ExtraMessage = intent.getStringExtra(Constants.AD_COUNTER_BROADCAST);

            if (ExtraMessage == Constants.TIMER_HAS_ENDED) {
               Log.d("ADVERT_CARD--","message from adCounterBar that timer has ended.");
                mSwipeView.unlockViews();
                clickable = true;
                sendBroadcast(AD_TO_TOTAL);
            }
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
