package com.bry.adcafe.adapters;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.GridLayoutManager;
import android.util.Log;
import android.widget.TextView;

import com.bry.adcafe.Constants;
import com.bry.adcafe.R;
import com.bry.adcafe.Variables;
import com.bry.adcafe.models.Advert;
import com.bry.adcafe.services.Utils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mindorks.placeholderview.PlaceHolderView;
import com.mindorks.placeholderview.annotations.Layout;
import com.mindorks.placeholderview.annotations.NonReusable;
import com.mindorks.placeholderview.annotations.Resolve;
import com.mindorks.placeholderview.annotations.View;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

/**
 * Created by bryon on 10/01/2018.
 */

@NonReusable
@Layout(R.layout.saved_ads_list_container)
public class SAContainer {
    private final String TAG = "SAContainer";
    @View(R.id.dayText) private TextView dateTextView;
    @View(R.id.PHViewForSpecificDay) private PlaceHolderView PHViewForSpecificDay;

    private Context mContext;
    private PlaceHolderView mPlaceHolderView;
    private List<Advert> adList;
    private long noOfDays;
    private boolean hasLoaded = false;
    private SAContainer ths;
    private DatabaseReference dbRef;

    public SAContainer(List<Advert> adlist, Context context, PlaceHolderView placeHolderView, long noOfDayss) {
        this.adList = adlist;
        mContext = context;
        mPlaceHolderView = placeHolderView;
        noOfDays = noOfDayss;
    }

    @Resolve
    private void onResolved() {
        dateTextView.setText(getDateFromDays(noOfDays));
        if(!hasLoaded) addAdsIntoViews();
        ths = this;
    }



    private void addAdsIntoViews() {
        int width = Variables.width;
        int calculatedSpanCount = width/ Utils.dpToPx(87);
        int spanCount = 4;

        GridLayoutManager glm = new GridLayoutManager(mContext,calculatedSpanCount);
        PHViewForSpecificDay.getBuilder().setLayoutManager(glm);

        PHViewForSpecificDay.setNestedScrollingEnabled(false);

        for (Advert ad: adList) {
            PHViewForSpecificDay.addView(new SavedAdsCard(ad,mContext,PHViewForSpecificDay,ad.getPushId(),noOfDays,false));
        }
        hasLoaded = true;
        loadListeners();
    }

    private void loadListeners() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        dbRef =  FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.PINNED_AD_LIST).child(Long.toString(noOfDays));
        dbRef.addChildEventListener(chil);

        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverToUnregisterAllReceivers,
                new IntentFilter("UNREGISTER"));
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverToCheckIfIsEmpty,
                new IntentFilter("CHECK_IF_IS_EMPTY"+noOfDays));

    }



    private BroadcastReceiver mMessageReceiverToUnregisterAllReceivers = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG,"Received broadcast to Unregister all receivers");
            unregisterAllReceivers();
        }
    };

    private BroadcastReceiver mMessageReceiverToCheckIfIsEmpty = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG,"Received broadcast to check if is empty.");
//            checkIfIsEmpty();
        }
    };




    private void checkIfIsEmpty() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Removing self since date has no children");
//                dateTextView.setVisibility(android.view.View.GONE);
                mPlaceHolderView.removeView(ths);
                unregisterAllReceivers();
            }
        }, 300);

    }

    private ChildEventListener chil = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            Log.d(TAG,"OnChildRemoved listener has been called.");
//            checkIfHasChildren();
            checkIfHasChildren2();
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private void checkIfHasChildren2() {
        if(Variables.VariablesHashOfAds.get(noOfDays).isEmpty()){
            removeThisView();
        }
    }

    private void checkIfHasChildren() {
        Long days;
        try {
            days = noOfDays;
            String test = Long.toString(days);
        }catch (Exception e){
            e.printStackTrace();
            days = Variables.noOfDays;
            noOfDays = Variables.noOfDays;
        }
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Query query =  FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.PINNED_AD_LIST).child(Long.toString(days));
        DatabaseReference dbRef = query.getRef();
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()) {
                    removeThisView();
                    Log.d("DateItem -- ", "Removing date since no ads are in date :" + getDateFromDays(noOfDays));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }




    private void removeThisView() {
        unregisterAllReceivers();
        Variables.VariablesHashOfAds.remove(noOfDays);
        if(Variables.VariablesHashOfAds.isEmpty()){
            Intent intent2 = new Intent("SHOW_NO_ADS_TEXT");
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent2);
        }
        try{
            mPlaceHolderView.removeView(ths);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void unregisterAllReceivers() {
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverToUnregisterAllReceivers);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverToCheckIfIsEmpty);
        if(dbRef!=null) dbRef.removeEventListener(chil);
    }




    private String getDateFromDays(long days){
        long currentTimeInMills = -days*(1000*60*60*24);

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(currentTimeInMills);
        int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
        int monthOfYear = cal.get(Calendar.MONTH);
        int year = cal.get(Calendar.YEAR);

//        String monthName = new DateFormatSymbols().getMonths()[monthOfYear];
        String monthName = getMonthName_Abbr(monthOfYear);

        Log.d(TAG,"Date gotten is : "+dayOfMonth+" "+monthName+" "+year);

        Calendar cal2 = Calendar.getInstance();
        int year2 = cal2.get(Calendar.YEAR);
        int day2 = cal2.get(Calendar.DAY_OF_MONTH);
        int month2 = cal2.get(Calendar.MONTH);

        String yearName;
        String dayWord;

        if(year == year2){
            Log.d(TAG,"Ad was pined this year...");
            yearName = "";
        }else if(year2 == year+1){
            Log.d(TAG,"Ad was pined last year...");
            yearName =", "+Integer.toString(year);
        }else{
            yearName =", "+ Integer.toString(year);
        }

        if(day2 == (dayOfMonth-1) && month2 == monthOfYear){
            return "Yesterday";
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

}
