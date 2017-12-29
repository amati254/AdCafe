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
    private boolean mIsBlankView;
    private DateItem di;

    public DateItem(Context context, PlaceHolderView PHView, long dateindays, String datetext,boolean isBlankView){
        this.mContext = context;
        this.mPlaceHolderView = PHView;
        this.dateInDays = dateindays;
        this.mDateText = datetext;
        this.mIsBlankView = isBlankView;
    }

    @Resolve
    private void onResolved(){
        mDateTextView.setText(mDateText);
        loadListeners();
//        loadBroadcastListeners();
        di = this;
    }

    private void loadListeners() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Query query =  FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.PINNED_AD_LIST).child(Long.toString(dateInDays));
        DatabaseReference dbRef = query.getRef();
        dbRef.addChildEventListener(new ChildEventListener() {
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
        });
    }

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
                if(!dataSnapshot.hasChildren()){
                    removeThisView();
                    removeItem();
                    Log.d("DateItem -- ","Removing date since no ads are in date :"+mDateText);
                }else{
                    if(dataSnapshot.getChildrenCount()%3==0){
                        Intent intent = new Intent("REMOVE_PLACEHOLDER_BLANK_ITEM"+dateInDays);
                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
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
        try{
            mPlaceHolderView.removeView(di);
        }catch (Exception e){
            e.printStackTrace();
            Variables.placeHolderView.removeView(di);
        }

    }

}
