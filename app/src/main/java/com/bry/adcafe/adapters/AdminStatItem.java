package com.bry.adcafe.adapters;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bry.adcafe.Constants;
import com.bry.adcafe.R;
import com.bry.adcafe.models.Advert;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by bryon on 19/11/2017.
 */

@NonReusable
@Layout(R.layout.admin_stats_item)
public class AdminStatItem {
    @View(R.id.EmailText) private TextView mEmail;
    @View(R.id.TargetedNumber) private TextView mTargetedNumber;
    @View(R.id.NumberReached) private TextView mNumberReached;
    @View(R.id.AmountToReemburse) private TextView mAmountToReimburse;
    @View(R.id.hasBeenReimbursed) private TextView mHasBeenReimbursed;
    @View(R.id.setReimbursed) private Button mSetReimbursed;

    private Context mContext;
    private PlaceHolderView mPlaceHolderView;
    private Advert mAdvert;
    private DatabaseReference dbRef;

    public AdminStatItem(Context Context, PlaceHolderView PlaceHolderView, Advert Advert){
        this.mContext = Context;
        this.mPlaceHolderView = PlaceHolderView;
        this.mAdvert = Advert;
    }

    @Resolve
    private void onResolved(){
        mEmail.setText(String.format("Advertiser :%s", mAdvert.getUserEmail()));
        mTargetedNumber.setText(String.format("Targeted : %d users", mAdvert.getNumberOfUsersToReach()));
        mNumberReached.setText(String.format("Number Reached : %d users.", mAdvert.getNumberOfTimesSeen()));

        int numberOfUsersWhoDidntSeeAd = (mAdvert.getNumberOfUsersToReach()- mAdvert.getNumberOfTimesSeen())*4;
        String number = Integer.toString(numberOfUsersWhoDidntSeeAd);
        mAmountToReimburse.setText("Amount to reimburse : "+number+" Ksh.");
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

    private void loadListeners() {
        Query query = FirebaseDatabase.getInstance().getReference(Constants.ADS_FOR_CONSOLE)
                .child(getPreviousDay()).child(mAdvert.getPushRefInAdminConsole());
        dbRef = query.getRef();
        dbRef.addChildEventListener(chil);

        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverForRemovingEventListeners
                ,new IntentFilter("REMOVE-LISTENER"));
    }

    ChildEventListener chil = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            Log.d("ADMIN_STAT_ITEM","Listener from firebase has responded.Updating has been reimbursed.");
            try{
                boolean newValue = dataSnapshot.getValue(boolean.class);
                Log.d("ADMIN_STAT_ITEM","New value gotten from firebase --"+newValue);
                mAdvert.setHasBeenReimbursed(newValue);
                try{
                    if(mAdvert.isHasBeenReimbursed()) {
                        mHasBeenReimbursed.setText("Status: Reimbursed.");
                        mAmountToReimburse.setText("Reimbursing amount:  0 Ksh");
                    }else{
                        int numberOfUsersWhoDidntSeeAd = (mAdvert.getNumberOfUsersToReach()- mAdvert.getNumberOfTimesSeen())*4;
                        String number = Integer.toString(numberOfUsersWhoDidntSeeAd);
                        mHasBeenReimbursed.setText("Status: NOT Reimbursed.");
                        mAmountToReimburse.setText("Reimbursing amount: "+number+" Ksh");
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

    @Click(R.id.setReimbursed)
    private void onClick(){
        Toast.makeText(mContext,"Updating hasBeenReimbursed..",Toast.LENGTH_SHORT).show();
        boolean bol = !mAdvert.isHasBeenReimbursed();
        DatabaseReference  mRef = FirebaseDatabase.getInstance().getReference(Constants.ADS_FOR_CONSOLE)
                .child(getPreviousDay())
                .child(mAdvert.getPushRefInAdminConsole())
                .child("hasBeenReimbursed");
        mRef.setValue(bol).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(mContext,"Updating complete..",Toast.LENGTH_SHORT).show();
            }
        });

    }

    private String getPreviousDay(){
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH,-1);
        String yy = Integer.toString(c.get(Calendar.YEAR));
        String mm = Integer.toString(c.get(Calendar.MONTH)+1);
        String dd = Integer.toString(c.get(Calendar.DAY_OF_MONTH));

        String tomorrowsDate = (dd+":"+mm+":"+yy);

        Log.d("ADMIN_STAT_ITEM","Tomorrows date is : "+tomorrowsDate);
        return tomorrowsDate;

    }

}
