package com.bry.adcafe.adapters;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mindorks.placeholderview.PlaceHolderView;
import com.mindorks.placeholderview.annotations.Animate;
import com.mindorks.placeholderview.annotations.Click;
import com.mindorks.placeholderview.annotations.Layout;
import com.mindorks.placeholderview.annotations.LongClick;
import com.mindorks.placeholderview.annotations.NonReusable;
import com.mindorks.placeholderview.annotations.Resolve;
import com.mindorks.placeholderview.annotations.View;
import com.wang.avi.AVLoadingIndicatorView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
    private boolean onDoublePressed = false;



    public SavedAdsCard(Advert advert, Context context, PlaceHolderView placeHolderView,String pinID) {
        mAdvert = advert;
        mContext = context;
        mPlaceHolderView = placeHolderView;
    }

    @Resolve
    private void onResolved() {
       loadImage();
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverToUnregisterAllReceivers,new IntentFilter("UNREGISTER"));
    }

    private BroadcastReceiver mMessageReceiverToUnregisterAllReceivers = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("ADVERT_CARD--","Received broadcast to Unregister all receivers");
            try{
                LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForUnpin);
            }catch (Exception e){
                e.printStackTrace();
            }
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverToUnregisterAllReceivers);
        }
    };

    private BroadcastReceiver mMessageReceiverForUnpin = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("ADVERT_CARD--","Received broadcast to Unpin ad");
            unPin();
            removeReceiver();
        }
    };

    @LongClick(R.id.SavedImageView)
    private void onLongClick(){
        unPin();
    }

    @Click(R.id.SavedImageView)
    private void onClick(){
        if(onDoublePressed){
            shareAd();
        }else{
            viewAd();
        }
        onDoublePressed = true;
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                onDoublePressed=false;
            }
        }, 1000);
    }

    private void viewAd() {
        Log.d("SavedAdsCard","Setting the ad to be viewed.");
        Variables.adToBeViewed = mAdvert;
        Intent intent = new Intent("VIEW");
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        setUpReceiver();
    }

    private void setUpReceiver(){
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverForUnpin,new IntentFilter(mAdvert.getPushRefInAdminConsole()));
    }

    private void removeReceiver(){
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForUnpin);
    }

    private void shareAd() {
        Log.d("SavedAdsCard","Setting the ad to be shared.");
        Variables.adToBeShared = mAdvert;
        Intent intent = new Intent("SHARE");
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);

    }


    private void loadImage(){
//        mAvi.setVisibility(android.view.View.VISIBLE);
        Log.d("SavedAdCard--","Setting avi to be visible");
        try {
            Bitmap bm = decodeFromFirebaseBase64(mAdvert.getImageUrl());
            mAdvert.setImageBitmap(bm);
            Log.d("SavedAdsCard---","Image has been converted to bitmap and set in model instance.");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Glide.with(mContext).load(bitmapToByte(getResizedBitmap(mAdvert.getImageBitmap(),170))).listener(new RequestListener<byte[], GlideDrawable>() {
            @Override
            public boolean onException(Exception e, byte[] model, Target<GlideDrawable> target, boolean isFirstResource) {
                mAvi.setVisibility(android.view.View.GONE);
                errorImageView.setVisibility(android.view.View.VISIBLE);
                Log.d("SavedAdsCard---",e.getMessage());
                if(!isInternetAvailable()&& !hasMessageBeenSeen){
                    hasMessageBeenSeen = true;
                    Intent intent = new Intent(Constants.CONNECTION_OFFLINE);
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                }
                return false;
            }

            @Override
            public boolean onResourceReady(GlideDrawable resource, byte[] model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                mAvi.setVisibility(android.view.View.GONE);
                errorImageView.setVisibility(android.view.View.GONE);
                return false;
            }
        }).into(imageView);
    }

    private boolean isInternetAvailable() {
        try {
            InetAddress ipAddr = InetAddress.getByName("google.com");
            return !ipAddr.toString().equals("");
        } catch (Exception e) {
            return false;
        }

    }


    private void unPin(){
        String id = mAdvert.getPushId();
        try{
            mPlaceHolderView.removeView(this);
        }catch (Exception e){
            Toast.makeText(mContext,"Something went wrong while unpinning that.",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        Log.d("SAVED_ADS_CARD--","Removing pinned ad"+id);
        String uid = User.getUid();

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

    private byte[] bitmapToByte(Bitmap bitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
        byte[] byteArray = baos.toByteArray();
        return byteArray;
    }

    private static Bitmap decodeFromFirebaseBase64(String image) throws IOException {
        byte[] decodedByteArray = android.util.Base64.decode(image, Base64.DEFAULT);
        Bitmap bitm = BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);
        Bitmap newBm = getResizedBitmap(bitm,500);
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

}

