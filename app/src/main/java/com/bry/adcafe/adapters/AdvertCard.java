package com.bry.adcafe.adapters;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bry.adcafe.Constants;
import com.bry.adcafe.R;
import com.bry.adcafe.Variables;
import com.bry.adcafe.models.Advert;
import com.bry.adcafe.models.User;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mindorks.placeholderview.Animation;
import com.mindorks.placeholderview.SwipePlaceHolderView;
import com.mindorks.placeholderview.Utils;
import com.mindorks.placeholderview.annotations.Animate;
import com.mindorks.placeholderview.annotations.Click;
import com.mindorks.placeholderview.annotations.Layout;
import com.mindorks.placeholderview.annotations.Resolve;
import com.mindorks.placeholderview.annotations.View;
import com.mindorks.placeholderview.annotations.swipe.SwipeIn;
import com.mindorks.placeholderview.annotations.swipe.SwipeInState;
import com.mindorks.placeholderview.annotations.swipe.SwipeOut;
import com.mindorks.placeholderview.annotations.swipe.SwipeOutState;
import com.wang.avi.AVLoadingIndicatorView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

/**
 * Created by bryon on 6/11/2017.
 */

@Animate(Animation.CARD_RIGHT_IN_DESC)
@Layout(R.layout.ad_card_view)
public class AdvertCard{
    @View(R.id.profileImageView) private ImageView profileImageView;
    @View(R.id.errorImageView) private ImageView errorImageView;
    @View(R.id.adCardAvi) private AVLoadingIndicatorView mAvi;
    @View(R.id.WebsiteIcon) private ImageView webIcon;
    @View(R.id.websiteText) private TextView webText;
    @View(R.id.smallDot) private android.view.View Dot;

    private Advert mAdvert;
    private Context mContext;
    private SwipePlaceHolderView mSwipeView;
    private static final String START_TIMER= "startTimer";
    private String mKey = "";

    private static boolean clickable;
    private static String mLastOrNotLast;
    private static boolean mIsNoAds;
    private static boolean hasAdLoaded;
    private boolean hasBeenSwiped = true;
    private Bitmap bs;
    private String igsNein = "none";



    public AdvertCard(Context context, Advert advert, SwipePlaceHolderView swipeView,String lastOrNotLast){
        mContext = context;
        mAdvert = advert;
        mSwipeView = swipeView;
        mLastOrNotLast = lastOrNotLast;
    }

    @Resolve
    private void onResolved(){
        if(mLastOrNotLast == Constants.LAST){
            mIsNoAds = false;
           loadOnlyLastAd();
        }else if(mLastOrNotLast == Constants.NO_ADS){
            mIsNoAds = true;
            loadAdPlaceHolderImage();
        }else{
            mIsNoAds = false;
            loadAllAds();
        }
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverToUnregisterAllReceivers,new IntentFilter(Constants.UNREGISTER_ALL_RECEIVERS));
        Variables.hasBeenPinned = false;
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverForTimerHasEnded,new IntentFilter(Constants.TIMER_HAS_ENDED));
    }

    private void loadAdPlaceHolderImage() {
        Glide.with(mContext).load(R.drawable.noadstoday2).into(profileImageView);
        mSwipeView.lockViews();
        clickable=false;
        Variables.setCurrentAdvert(mAdvert);
    }

    private void loadAllAds(){
        Log.d("ADVERT_CARD--","LOADING ALL ADS NORMALLY.");

        mAvi.setVisibility(android.view.View.VISIBLE);
        try {
            bs = decodeFromFirebaseBase64(mAdvert.getImageUrl());
            mAdvert.setImageBitmap(bs);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Glide.with(mContext).load(bitmapToByte(mAdvert.getImageBitmap())).listener(new RequestListener<byte[], GlideDrawable>() {
            @Override
            public boolean onException(Exception e, byte[] model, Target<GlideDrawable> target, boolean isFirstResource) {
                Log.d("ADVERT_CARD--","The image has failed to load due to error."+e.getMessage());
                errorImageView.setVisibility(android.view.View.VISIBLE);
                mAvi.setVisibility(android.view.View.GONE);
                if(isFirstResource) {mSwipeView.unlockViews();}
                return false;
            }

            @Override
            public boolean onResourceReady(GlideDrawable resource, byte[] model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                Log.d("ADVERT_CARD--","The image has loaded successfully");
                mAvi.setVisibility(android.view.View.GONE);
                errorImageView.setVisibility(android.view.View.GONE);
                if(isFirstResource && mLastOrNotLast==Constants.NOT_LAST) {
                    Log.d("ADVERT_CARD---","sending broadcast to start timer...");
                    if(mLastOrNotLast!=Constants.ANNOUNCEMENTS) sendBroadcast(START_TIMER);
                    setLastAdSeen();
                }
                clickable=false;
                return false;
            }
        }).into(profileImageView);
    }

    private void loadOnlyLastAd(){
        Log.d("ADVERT_CARD--","LOADING ONLY LAST AD.");
        mAvi.setVisibility(android.view.View.VISIBLE);
        try {
            Bitmap bm = decodeFromFirebaseBase64(mAdvert.getImageUrl());
            mAdvert.setImageBitmap(bm);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Glide.with(mContext).load(bitmapToByte(mAdvert.getImageBitmap())).listener(new RequestListener<byte[], GlideDrawable>() {
            @Override
            public boolean onException(Exception e, byte[] model, Target<GlideDrawable> target, boolean isFirstResource) {
                errorImageView.setVisibility(android.view.View.VISIBLE);
                mAvi.setVisibility(android.view.View.GONE);
                hasAdLoaded = false;
                return false;
            }

            @Override
            public boolean onResourceReady(GlideDrawable resource, byte[] model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                mAvi.setVisibility(android.view.View.GONE);
                errorImageView.setVisibility(android.view.View.GONE);
                hasAdLoaded = true;
                return false;
            }
        }).into(profileImageView);
        mSwipeView.lockViews();
        clickable=false;
        Variables.setCurrentAdvert(mAdvert);
        if(!mAdvert.getWebsiteLink().equals(igsNein)){
            webIcon.setAlpha(1.0f);
            webText.setAlpha(1.0f);
        }
        sendBroadcast(Constants.LAST);
    }

    @Click(R.id.profileImageView)
    private void onClick(){
        Log.d("EVENT", "profileImageView click");
        if (clickable) {
            mSwipeView.enableTouchSwipe();
            hasBeenSwiped = true;
        }
        if(mLastOrNotLast == Constants.ANNOUNCEMENTS ||mLastOrNotLast == Constants.LAST){
            mSwipeView.enableTouchSwipe();
        }

    }

    @SwipeOut
    private void onSwipedOut(){
        Log.d("EVENT----", "onSwipedOut");
        if(mLastOrNotLast!=Constants.ANNOUNCEMENTS){
            Variables.removeAd();
            hasBeenSwiped = true;
            sendBroadcast(START_TIMER);
        }
        if(mSwipeView.getChildCount()==2 && mLastOrNotLast==Constants.ANNOUNCEMENTS){
            Toast.makeText(mContext,"That's all we have today.",Toast.LENGTH_SHORT).show();
            mSwipeView.lockViews();
        }
    }

    @SwipeIn
    private void onSwipeIn(){
        Log.d("EVENT----", "onSwipedIn");
        if(mLastOrNotLast!=Constants.ANNOUNCEMENTS){
            Variables.removeAd();
            hasBeenSwiped = true;
            sendBroadcast(START_TIMER);
        }
        if(mSwipeView.getChildCount()==2 && mLastOrNotLast==Constants.ANNOUNCEMENTS){
            Toast.makeText(mContext,"That's all we have today.",Toast.LENGTH_SHORT).show();
            mSwipeView.lockViews();
        }
    }

    private void sendBroadcast(String message ) {
        if(message == START_TIMER && hasBeenSwiped){
            Log.d("AdvertCard - ","Sending message to start timer");
            mSwipeView.lockViews();
            Variables.hasBeenPinned = false;
//            mAdvert.setImageBitmap(null);
            clickable = false;
            Intent intent = new Intent(Constants.ADVERT_CARD_BROADCAST_TO_START_TIMER);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
            setLastAdSeen();
            if(mSwipeView.getChildCount()==3) sendBroadcast(Constants.LOAD_MORE_ADS);
        }else if(message == Constants.LAST){
            Intent intent = new Intent(Constants.LAST);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
            setLastAdSeen();
        }else if(message == Constants.LOAD_MORE_ADS){
            Intent intent = new Intent(Constants.LOAD_MORE_ADS);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        }
    }

    private void setLastAdSeen(){
        Variables.setLastSeenAd(Variables.getAdFromVariablesAdList(Variables.getCurrentAdNumberForAllAdsList()).getPushId());
        Variables.setCurrentAdvert(Variables.getAdFromVariablesAdList(Variables.getCurrentAdNumberForAllAdsList()));
        if(!Variables.getCurrentAdvert().getWebsiteLink().equals(igsNein)){
            Log.d("ADVERT_CARD---","Advert has a website link. Setting icon and dot to be visible");
            mSwipeView.findViewById(R.id.smallDot).setVisibility(android.view.View.VISIBLE);
            webIcon.setAlpha(1.0f);
            webText.setAlpha(1.0f);
        }else{
            Log.d("ADVERT_CARD---","Advert doesnt have website link. Setting icon and dot to be invisible");
            mSwipeView.findViewById(R.id.smallDot).setVisibility(android.view.View.INVISIBLE);
            webIcon.setAlpha(0.4f);
            webText.setAlpha(0.4f);
        }

        Log.d("ADVERT_CARD---","Setting the last ad seen in Variables class... "+Variables.getCurrentAdvert().getPushRefInAdminConsole());

    }

    private BroadcastReceiver mMessageReceiverForTimerHasEnded = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
               Log.d("ADVERT_CARD--","message from adCounterBar that timer has ended has been received.");
                if(mSwipeView.getChildCount() > 1){
                    mSwipeView.unlockViews();
                    clickable = true;
                    hasBeenSwiped = false;
                }else if(mSwipeView.getChildCount()==1 && mLastOrNotLast!=Constants.ANNOUNCEMENTS){
                    sendBroadcast(Constants.LAST);
                }
        }
    };

    private BroadcastReceiver mMessageReceiverToUnregisterAllReceivers = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("ADVERT_CARD--","Received broadcast to Unregister all receivers");
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForTimerHasEnded);
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverToUnregisterAllReceivers);
        }
    };



    private byte[] bitmapToByte(Bitmap bitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
        byte[] byteArray = baos.toByteArray();
        return byteArray;
    }

    private static Bitmap decodeFromFirebaseBase64(String image) throws IOException {
        byte[] decodedByteArray = android.util.Base64.decode(image, Base64.DEFAULT);
        Bitmap bitm = BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);
        Bitmap newBm = getResizedBitmap(bitm,700);
        return newBm;
    }


    private static Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }

        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    private void readImageBitmapDimensions(){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
//        BitmapFactory.decodeResource(getResources(), R.id.myimage, options);
        int imageHeight = options.outHeight;
        int imageWidth = options.outWidth;
        String imageType = options.outMimeType;
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}
