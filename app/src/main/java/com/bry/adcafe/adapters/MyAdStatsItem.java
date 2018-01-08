package com.bry.adcafe.adapters;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
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
    @View(R.id.hasBeenReimbursed) private TextView mHasBeenReimbursed;
    @View(R.id.dateUploaded) private TextView mDateUploaded;

    private Context mContext;
    private PlaceHolderView mPlaceHolderView;
    private Advert mAdvert;
    private DatabaseReference dbRef;
    private byte[] mImageBytes;

    public MyAdStatsItem(Context Context, PlaceHolderView PlaceHolderView, Advert Advert){
        this.mContext = Context;
        this.mPlaceHolderView = PlaceHolderView;
        this.mAdvert = Advert;
    }

    @Resolve
    public void onResolved(){
        if(mImageBytes==null) new LongOperationFI().execute("");
        mEmail.setText(String.format("Uploaded by : %s", mAdvert.getUserEmail()));
        mTargetedNumber.setText(String.format("No. of users targeted : %d", mAdvert.getNumberOfUsersToReach()));
        mDateUploaded.setText(String.format("Uploaded on %s", getDateFromDays(mAdvert.getDateInDays())));
        if(!mAdvert.isFlagged()){
            mUsersReachedSoFar.setText("Users reached : "+mAdvert.getNumberOfTimesSeen());
        }else{
            mUsersReachedSoFar.setText("Taken Down.No users to be reached.");
        }

        int numberOfUsersWhoDidntSeeAd = mAdvert.getNumberOfUsersToReach()- mAdvert.getNumberOfTimesSeen();
        String number = Long.toString(numberOfUsersWhoDidntSeeAd*Constants.CONSTANT_AMOUNT_PER_AD);
        mAmountToReimburse.setText("Reimbursing amount: "+number+" Ksh");
        try{
            if(mAdvert.isHasBeenReimbursed()) {
                mHasBeenReimbursed.setText("Status: Reimbursed.");
                mAmountToReimburse.setText("Reimbursing amount:  0 Ksh");
            } else {
                mHasBeenReimbursed.setText("Status: NOT Reimbursed.");
                mAmountToReimburse.setText("Reimbursing amount: "+number+" Ksh");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        loadListeners();
    }

    private void setImage() {
        try {
            Bitmap bm = getResizedBitmap(decodeFromFirebaseBase64(mAdvert.getImageUrl()),150);
            Log.d("SavedAdsCard---","Image has been converted to bitmap.");
            mImageBytes = bitmapToByte(bm);
            mAdvert.setImageBitmap(bm);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadImage2(){
        Glide.with(mContext).load(mImageBytes).into(mAdImage);
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
        dbRef = query.getRef();
        dbRef.addChildEventListener(chil);

        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverForRemovingEventListeners
                ,new IntentFilter("REMOVE-LISTENERS"));
    }

    ChildEventListener chil = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            Log.d("MY_AD_STAT_ITEM","Listener from firebase has responded.Updating users reached so far");
//                Advert refreshedAd = dataSnapshot.getValue(Advert.class);
//                int newValue = refreshedAd.getNumberOfTimesSeen();
            try{
                int newValue = dataSnapshot.getValue(int.class);
                Log.d("MY_AD_STAT_ITEM","New value gotten from firebase --"+newValue);
                mAdvert.setNumberOfTimesSeen(newValue);
                mUsersReachedSoFar.setText("Users reached so far : "+newValue);
                int numberOfUsersWhoDidntSeeAd = mAdvert.getNumberOfUsersToReach()- newValue;
                String number = Long.toString(numberOfUsersWhoDidntSeeAd*Constants.CONSTANT_AMOUNT_PER_AD);
                mAmountToReimburse.setText("Amount to be reimbursed : "+number+" Ksh");
            }catch (Exception e){
                e.printStackTrace();
            }
            try{
                boolean newValue = dataSnapshot.getValue(boolean.class);
                Log.d("ADMIN_STAT_ITEM","New value gotten from firebase --"+newValue);
                mAdvert.setHasBeenReimbursed(newValue);
                try{
                    if(mAdvert.isHasBeenReimbursed()) {
                        mHasBeenReimbursed.setText("Status: Reimbursed.");
                        mAmountToReimburse.setText("Reimbursing amount:  0 Ksh");
                    }else{
                        mHasBeenReimbursed.setText("Status: NOT Reimbursed.");
                        int numberOfUsersWhoDidntSeeAd = mAdvert.getNumberOfUsersToReach()- mAdvert.getNumberOfTimesSeen();
                        String number = Long.toString(numberOfUsersWhoDidntSeeAd*Constants.CONSTANT_AMOUNT_PER_AD);
                        mAmountToReimburse.setText("Amount to be reimbursed : "+number+" Ksh");
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }catch (Exception e){
                e.printStackTrace();
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
    };

    private BroadcastReceiver mMessageReceiverForRemovingEventListeners = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
          dbRef.removeEventListener(chil);
          LocalBroadcastManager.getInstance(mContext).unregisterReceiver(this);
        }
    };


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

    private String getDateFromDays(long days){
        long currentTimeInMills = days*(1000*60*60*24);

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(currentTimeInMills);
        int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
        int monthOfYear = cal.get(Calendar.MONTH);
        int year = cal.get(Calendar.YEAR);

//        String monthName = new DateFormatSymbols().getMonths()[monthOfYear];
        String monthName = getMonthName_Abbr(monthOfYear);

        Log.d("Splash","Date gotten is : "+dayOfMonth+" "+monthName+" "+year);

        Calendar cal2 = Calendar.getInstance();
        int year2 = cal2.get(Calendar.YEAR);
        String yearName;

        if(year == year2){
            Log.d("My_ad_stat_item","Ad was pined this year...");
            yearName = "";
        }else if(year2 == year+1){
            Log.d("My_ad_stat_item","Ad was pined last year...");
            yearName =", "+Integer.toString(year);
        }else{
            yearName =", "+ Integer.toString(year);
        }

        return dayOfMonth+" "+monthName+yearName;
    }

    private String getMonthName_Abbr(int month) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, month);
        SimpleDateFormat month_date = new SimpleDateFormat("MMM");
        String month_name = month_date.format(cal.getTime());
        return month_name;
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
            if(mImageBytes!=null) loadImage2();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
    }
}
