package com.bry.adcafe.adapters;

import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import com.bry.adcafe.Constants;
import com.bry.adcafe.R;
import com.bry.adcafe.models.User;
import com.google.firebase.auth.FirebaseAuth;
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

    public DateItem(Context context, PlaceHolderView PHView, long dateindays, String datetext){
        this.mContext = context;
        this.mPlaceHolderView = PHView;
        this.dateInDays = dateindays;
        this.mDateText = datetext;
    }

    @Resolve
    private void onResolved(){
        mDateTextView.setText(mDateText);
        loadListeners();
    }

    private void loadListeners() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Query query =  FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.PINNED_AD_LIST);
        DatabaseReference dbRef = query.getRef();
        dbRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                if(!dataSnapshot.hasChildren()){
                    mPlaceHolderView.removeView(this);
                    Log.d("DateItem -- ","Removing date since no ads are in date :"+mDateText);
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChildren()){
                    mPlaceHolderView.removeView(this);
                    Log.d("DateItem -- ","Removing date since no ads are in date :"+mDateText);
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
