package com.bry.adcafe.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.bry.adcafe.Constants;
import com.bry.adcafe.R;
import com.bry.adcafe.models.Advert;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.mindorks.placeholderview.PlaceHolderView;
import com.mindorks.placeholderview.annotations.Layout;
import com.mindorks.placeholderview.annotations.NonReusable;
import com.mindorks.placeholderview.annotations.Resolve;
import com.mindorks.placeholderview.annotations.View;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by bryon on 19/11/2017.
 */


@NonReusable
@Layout(R.layout.my_ad_stat_item)
public class MyAdStatsItem {
    @View(R.id.adImage) private ImageView mAdImage;
    @View(R.id.EmailText) private TextView mEmail;
    @View(R.id.TargetedNumber) private TextView mTargetedNumber;
    @View(R.id.usersReachedSoFar) private TextView mUsersReachedSoFar;
    @View(R.id.AmountToReimburse) private TextView mAmountToReimburse;

    private Context mContext;
    private PlaceHolderView mPlaceHolderView;
    private Advert mAdvert;

    public MyAdStatsItem(Context Context, PlaceHolderView PlaceHolderView, Advert Advert){
        this.mContext = Context;
        this.mPlaceHolderView = PlaceHolderView;
        this.mAdvert = Advert;
    }

    @Resolve
    public void onResolved(){
        loadImage();
        mEmail.setText(String.format("Uploaded by : %s", mAdvert.getUserEmail()));
        mTargetedNumber.setText(String.format("No. of users targeted : %d", mAdvert.getNumberOfUsersToReach()));

        if(!mAdvert.isFlagged()){
            mUsersReachedSoFar.setText("Users reached : "+mAdvert.getNumberOfTimesSeen());
        }else{
            mUsersReachedSoFar.setText("No users to be reached.");
        }


        int numberOfUsersWhoDidntSeeAd = mAdvert.getNumberOfUsersToReach()- mAdvert.getNumberOfTimesSeen();
        String number = Long.toString(numberOfUsersWhoDidntSeeAd*Constants.CONSTANT_AMOUNT_PER_AD);
        mAmountToReimburse.setText("Reimbursing amount: "+number+" Ksh");

        loadListeners();
    }

    private void loadImage(){
        try {
            Bitmap bm = decodeFromFirebaseBase64(mAdvert.getImageUrl());
            mAdvert.setImageBitmap(bm);
            Log.d("MY_AD_STAT_ITEM---","Image has been converted to bitmap and set in model instance.");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Glide.with(mContext).load(bitmapToByte(getResizedBitmap(mAdvert.getImageBitmap(),150))).into(mAdImage);
    }


    private void loadListeners() {
        Query query = FirebaseDatabase.getInstance().getReference(Constants.ADS_FOR_CONSOLE)
                .child(getDate()).child(mAdvert.getPushRefInAdminConsole());
        DatabaseReference dbRef = query.getRef();
        dbRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.d("MY_AD_STAT_ITEM","Listener from firebase has responded.Updating users reached so far");
//                Advert refreshedAd = dataSnapshot.getValue(Advert.class);
//                int newValue = refreshedAd.getNumberOfTimesSeen();
                int newValue = dataSnapshot.getValue(int.class);
                Log.d("MY_AD_STAT_ITEM","New value gotten from firebase --"+newValue);
                mUsersReachedSoFar.setText("Users reached so far : "+newValue);

                int numberOfUsersWhoDidntSeeAd = mAdvert.getNumberOfUsersToReach()- newValue;
                String number = Long.toString(numberOfUsersWhoDidntSeeAd*Constants.CONSTANT_AMOUNT_PER_AD);
                mAmountToReimburse.setText("Amount to be reimbursed : "+number+" Ksh");
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private String getDate(){
        long date = System.currentTimeMillis();
        SimpleDateFormat sdfMonth = new SimpleDateFormat("MM");
        String MonthString = sdfMonth.format(date);

        SimpleDateFormat sdfDay = new SimpleDateFormat("dd");
        String dayString = sdfDay.format(date);

        SimpleDateFormat sdfYear = new SimpleDateFormat("yyyy");
        String yearString = sdfYear.format(date);

        Calendar c = Calendar.getInstance();
        String yy = Integer.toString(c.get(Calendar.YEAR));
        String mm = Integer.toString(c.get(Calendar.MONTH)+1);
        String dd = Integer.toString(c.get(Calendar.DAY_OF_MONTH));

        String todaysDate = (dd+":"+mm+":"+yy);

        return todaysDate;
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
