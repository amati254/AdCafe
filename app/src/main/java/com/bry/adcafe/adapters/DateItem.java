package com.bry.adcafe.adapters;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.GridLayoutManager;
import android.util.Log;
import android.widget.TextView;

import com.bry.adcafe.Constants;
import com.bry.adcafe.R;
import com.bry.adcafe.Variables;
import com.bry.adcafe.models.Advert;
import com.bry.adcafe.models.User;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bryon on 27/12/2017.
 */

@NonReusable
@Layout(R.layout.date_item)
public class DateItem {
    @View(R.id.dateText) private TextView mDateTextView;

    private Context mContext;
    private PlaceHolderView mPlaceHolderView;
    private Long dateInDays;
    private String mDateText;
    private DateItem di;
    private DatabaseReference dbRef;
    private boolean isMessageReceivedForAddingBlank = false;
    private boolean haveListenersLoaded = false;

    public DateItem(Context context, PlaceHolderView PHView, long dateindays, String datetext){
        this.mContext = context;
        this.mPlaceHolderView = PHView;
        this.dateInDays = dateindays;
        this.mDateText = datetext;
//        loadListeners();
    }

    @Resolve
    private void onResolved(){
        mDateTextView.setText(mDateText);
        if(!haveListenersLoaded)loadListeners();
        di = this;
    }

    private void loadListeners() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        dbRef =  FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.PINNED_AD_LIST).child(Long.toString(dateInDays));
        dbRef.addChildEventListener(chil);

        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverToUnregisterAllReceivers,
                new IntentFilter("UNREGISTER"));
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverToAdBlankBeforeThis,
                new IntentFilter("ADD_BLANK_BEFORE_THIS"+dateInDays));
        haveListenersLoaded = true;
    }

    private BroadcastReceiver mMessageReceiverToAdBlankBeforeThis = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("DateItem--","Received broadcast to Add blank before this.");
            addBlankBeforeThis();
        }
    };

    private BroadcastReceiver mMessageReceiverToUnregisterAllReceivers = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("DateItem--","Received broadcast to Unregister all receivers");
            removeListeners();
        }
    };

    private ChildEventListener chil = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            Log.d("DateItem","OnChildRemoved listener has been called.");
            checkIfHasChildren();
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private void checkIfHasChildren() {
        Long days;
        try {
            days = dateInDays;
            String test = Long.toString(days);
        }catch (Exception e){
            e.printStackTrace();
            days = Variables.noOfDays;
            dateInDays = Variables.noOfDays;
        }
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Query query =  FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.PINNED_AD_LIST).child(Long.toString(days));
        DatabaseReference dbRef = query.getRef();
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){
                    removeThisView();
                    removeItem();
                    Log.d("DateItem -- ","Removing date since no ads are in date :"+mDateText);
                }else{
                    if(dataSnapshot.getChildrenCount()%3==0){
                        Intent intent = new Intent("REMOVE_PLACEHOLDER_BLANK_ITEM"+dateInDays);
                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                    }else{
//                        List<Advert> AdList = new ArrayList<>();
//                        for(DataSnapshot snap: dataSnapshot.getChildren()){
//                            Advert advert = snap.getValue(Advert.class);
//                            AdList.add(advert);
//                        }
//                        Advert adToBeNotified = AdList.get(AdList.size()-1);
//                        Log.d("DateItem","Sending message to ad blank item to ad: "+adToBeNotified.getPushId()+" for date: "+dateInDays);
//                        if(mPlaceHolderView!=null)Variables.placeHolderView = mPlaceHolderView;
//                        Intent intent = new Intent("ADD_BLANK"+dateInDays+adToBeNotified.getPushId());
//                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
//                        AdList.clear();

                        int posOfThisDate = Variables.daysArray.indexOf(dateInDays);
                        if(posOfThisDate+1!=Variables.daysArray.size()){
                            int nextPos = posOfThisDate+1;
                            long previousDate = Variables.daysArray.get(nextPos);
                            Variables.previousDaysNumber = previousDate;
                            Intent intent2 = new Intent("ADD_BLANK_BEFORE_THIS"+previousDate);
                            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent2);
                        }

                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void removeThisView() {
        Intent intent = new Intent("REMOVE_BLANK_ITEMS"+dateInDays);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);

    }

//    private void loadBroadcastListeners() {
//        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverForRemoveBlank,
//                new IntentFilter("REMOVE_BLANK_ITEMS"));
//        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverToUnregisterAllReceivers,
//                new IntentFilter("UNREGISTER"));
//    }
//
//    private BroadcastReceiver mMessageReceiverForRemoveBlank = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            Log.d("DateItemBlank","Received broadcast to Remove blank");
//            removeItem();
//            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(this);
//            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverToUnregisterAllReceivers);
//
//        }
//    };
//
//    private BroadcastReceiver mMessageReceiverToUnregisterAllReceivers = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            Log.d("DateItemBlank-","Received broadcast to Unregister all receivers");
//
//            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(this);
//            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForRemoveBlank);
//
//        }
//    };

    private void removeItem(){
        removeListeners();
        Variables.daysArray.remove(dateInDays);
        try{
            mPlaceHolderView.removeView(di);
        }catch (Exception e){
            e.printStackTrace();
            mPlaceHolderView = Variables.placeHolderView;
            try{
                mPlaceHolderView.removeView(di);
            }catch (Exception e2){
                e2.printStackTrace();
                Variables.placeHolderView.removeView(di);
            }
        }
    }

    private void removeListeners(){
        try{
            dbRef.removeEventListener(chil);
        }catch (Exception e){
            e.printStackTrace();
        }
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverToAdBlankBeforeThis);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverToUnregisterAllReceivers);
    }

    private void addBlankBeforeThis(){
        long daysNumber = Variables.previousDaysNumber;
        try{
            mPlaceHolderView.addViewBefore(di,
                    new BlankItem(mContext,mPlaceHolderView,daysNumber,"pineapples",false));
        }catch (Exception e){
            e.printStackTrace();
            mPlaceHolderView = Variables.placeHolderView;
            try{
                mPlaceHolderView.addViewBefore(di,
                        new BlankItem(mContext,mPlaceHolderView,daysNumber,"pineapples",false));
            }catch (Exception e2){
                e2.printStackTrace();
            }
        }
    }

}
