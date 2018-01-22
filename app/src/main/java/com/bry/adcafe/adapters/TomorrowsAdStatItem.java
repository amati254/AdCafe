package com.bry.adcafe.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bry.adcafe.Constants;
import com.bry.adcafe.R;
import com.bry.adcafe.Variables;
import com.bry.adcafe.models.Advert;
import com.bumptech.glide.Glide;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.mindorks.placeholderview.PlaceHolderView;
import com.mindorks.placeholderview.annotations.Click;
import com.mindorks.placeholderview.annotations.Layout;
import com.mindorks.placeholderview.annotations.NonReusable;
import com.mindorks.placeholderview.annotations.Resolve;
import com.mindorks.placeholderview.annotations.View;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;

/**
 * Created by bryon on 20/12/2017.
 */

@NonReusable
@Layout(R.layout.tomorrows_ads_item)
public class TomorrowsAdStatItem {
    @View(R.id.adImage) private ImageView mImage;
    @View(R.id.EmailText) private TextView mEmail;
    @View(R.id.TargetedNumber) private TextView mTargetedNumber;
    @View(R.id.AmountToReimburse) private TextView mAmountToReimburse;
    @View(R.id.category) private TextView mCategory;
    @View(R.id.isFlagged) private TextView mFlagged;
    @View(R.id.takeDownButton) private Button mTakeDown;

    private Context mContext;
    private PlaceHolderView mPlaceHolderView;
    private Advert mAdvert;
    private byte[] mImageBytes;

    public TomorrowsAdStatItem(Context Context, PlaceHolderView PlaceHolderView, Advert Advert){
        this.mContext = Context;
        this.mPlaceHolderView = PlaceHolderView;
        this.mAdvert = Advert;
    }

    @Resolve
    private void onResolved(){
        if(mImageBytes==null) new  LongOperationFI().execute("");
        else Glide.with(mContext).load(mImageBytes).into(mImage);

        //dddddddddddd
        mEmail.setText("Uploaded by : "+mAdvert.getUserEmail());
        mTargetedNumber.setText(String.format("No. of users targeted : %d", mAdvert.getNumberOfUsersToReach()));
        mCategory.setText("Category : "+mAdvert.getCategory());
        String amount = Integer.toString((int)(mAdvert.getNumberOfUsersToReach()*Constants.CONSTANT_AMOUNT_PER_AD));
        mAmountToReimburse.setText(String.format("Paid amount : %s Ksh.", amount));
        if(mAdvert.isFlagged()) {
            mTakeDown.setText("Put Up.");
            mFlagged.setText("Status : Taken Down.");
        } else {
            mTakeDown.setText("Take Down.");
            mFlagged.setText("Status : Uploaded.");
        }
        loadListeners();
    }

    private void setImage(){
        try {
            Bitmap bm = getResizedBitmap(decodeFromFirebaseBase64(mAdvert.getImageUrl()),250);
            Log.d("TomorrowsAdItem---","Image has been converted to bitmap.");
            mImageBytes = bitmapToByte(bm);
            mAdvert.setImageBitmap(bm);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadListeners() {
        Query query = FirebaseDatabase.getInstance().getReference(Constants.ADS_FOR_CONSOLE)
                .child(getNextDay()).child(mAdvert.getPushRefInAdminConsole());
        DatabaseReference dbRef = query.getRef();
        dbRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.d("TOMORROWS_AD_STAT_ITEM","Listener from firebase has responded. Updating data.");
                boolean newValue = dataSnapshot.getValue(boolean.class);
                Log.d("TOMORROWS_AD_STAT_ITEM","New value gotten from firebase --"+newValue);
                mAdvert.setFlagged(newValue);
                if(mAdvert.isFlagged()) {
                    mTakeDown.setText("Put Up.");
                    mFlagged.setText("Status : Taken Down.");
                } else {
                    mTakeDown.setText("Take Down.");
                    mFlagged.setText("Status : Uploaded.");
                }
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

    @Click(R.id.takeDownButton)
    private void onClick(){
        if(!mAdvert.isAdminFlagged()) {
            Variables.adToBeFlagged = mAdvert;
            Intent intent = new Intent("TAKE_DOWN");
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
            if (!mAdvert.isFlagged()) {
                Variables.areYouSureTakeDownText = "Are you sure you want to take down your ad?";
            } else {
                Variables.areYouSureTakeDownText = "Are you sure you want to put back up your ad?";
            }
        }else{
            Toast.makeText(mContext,"You can't put back up your ad, it was taken down by the admin.",Toast.LENGTH_SHORT).show();
        }
    }

    private String getNextDay(){
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH,1);
        String yy = Integer.toString(c.get(Calendar.YEAR));
        String mm = Integer.toString(c.get(Calendar.MONTH)+1);
        String dd = Integer.toString(c.get(Calendar.DAY_OF_MONTH));

        String tomorrowsDate = (dd+":"+mm+":"+yy);

        Log.d("AdminAdsItem - ","Tomorrows date is : "+tomorrowsDate);
        return tomorrowsDate;

    }

    private byte[] bitmapToByte(Bitmap bitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
        byte[] byteArray = baos.toByteArray();
        return byteArray;
    }

    private static Bitmap decodeFromFirebaseBase64(String image) throws IOException {
        byte[] decodedByteArray = android.util.Base64.decode(image, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);
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

    private class LongOperationFI extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            try{
                setImage();
            }catch (Exception e){
                e.printStackTrace();
            }
            return "executed";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if(mImageBytes!=null) Glide.with(mContext).load(mImageBytes).into(mImage);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
    }

}
