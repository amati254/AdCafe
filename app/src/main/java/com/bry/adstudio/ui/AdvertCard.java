package com.bry.adstudio.ui;

import android.content.Context;
import android.util.Log;
import android.widget.ImageView;

import com.bry.adstudio.R;
import com.bry.adstudio.models.Advert;
import com.bumptech.glide.Glide;
import com.mindorks.placeholderview.SwipePlaceHolderView;
import com.mindorks.placeholderview.annotations.Layout;
import com.mindorks.placeholderview.annotations.NonReusable;
import com.mindorks.placeholderview.annotations.Resolve;
import com.mindorks.placeholderview.annotations.View;
import com.mindorks.placeholderview.annotations.swipe.SwipeCancelState;
import com.mindorks.placeholderview.annotations.swipe.SwipeIn;
import com.mindorks.placeholderview.annotations.swipe.SwipeInState;
import com.mindorks.placeholderview.annotations.swipe.SwipeOut;
import com.mindorks.placeholderview.annotations.swipe.SwipeOutState;

/**
 * Created by bryon on 6/11/2017.
 */

@NonReusable
@Layout(R.layout.ad_card_view)

public class AdvertCard {
    @View(R.id.adImageView) private ImageView adImageView;

    private Advert mAdvert;
    private Context mContext;
    private SwipePlaceHolderView mSwipeView;

    public AdvertCard(Context context, Advert advert, SwipePlaceHolderView swipeView){
        mContext = context;
        mAdvert = advert;
        mSwipeView = swipeView;
    }

    @Resolve
    private void onResolved(){
        Glide.with(mContext).load(mAdvert.getImageUrl()).into(adImageView);
    }

    @SwipeOut
    private void onSwipedOut(){
        Log.d("EVENT", "onSwipedOut");
        mSwipeView.addView(this);
    }

    @SwipeIn
    private void onSwipeIn(){
        Log.d("EVENT", "onSwipedIn");
    }

    @SwipeCancelState
    private void onSwipeCancelState(){
        Log.d("EVENT", "onSwipeCancelState");
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
