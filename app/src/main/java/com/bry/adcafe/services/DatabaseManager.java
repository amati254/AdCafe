package com.bry.adcafe.services;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.bry.adcafe.Constants;
import com.bry.adcafe.Variables;
import com.bry.adcafe.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by bryon on 24/11/2017.
 */

public class DatabaseManager {
    public static final String TAG = DatabaseManager.class.getSimpleName();
    private String mKey = "";
    private int numberOfSubs = 0;
    private int iterations = 0;
    private Context context;

    ////Create user methods////////////////////////////////////////

    public void createUserSpace(final Context mContext){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();
        context = mContext;

        //Creates nodes for totals seen today and sets them to 0;
        DatabaseReference adRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.TOTAL_NO_OF_ADS_SEEN_TODAY);
        adRef.setValue(0);


        //Creates nodes for totals seen all month and sets them to 0;
        DatabaseReference adRef2 = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.TOTAL_NO_OF_ADS_SEEN_All_MONTH);
        adRef2.setValue(0);

        //Creates node for users current subscription being viewed and setting it to 0.
        DatabaseReference adRef3 = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.CURRENT_SUBSCRIPTION_INDEX);
        adRef3.setValue(0);

        //Creates node for the current ad being seen by user in specific subscription and setting it to 0.
        DatabaseReference adRef4 = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.CURRENT_AD_IN_SUBSCRIPTION);
        adRef4.setValue(0);

        //sets the date for when last used in firebase.
        DatabaseReference adRef7 = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.DATE_IN_FIREBASE);
        adRef7.setValue(getDate()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Intent intent = new Intent(Constants.CREATE_USER_SPACE_COMPLETE);
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
            }
        });
    }



    public void setUpUserSubscriptions(List<String> subscriptions){
        numberOfSubs = subscriptions.size();
        for(String sub:subscriptions){
            generateClusterIDFromCategoryFlaggedClusters(sub);
        }
    }

    public void generateClusterIDFromCategoryFlaggedClusters(final String AdvertCategory){
        Variables.setMonthAdTotals(mKey,0);
        Variables.setAdTotal(0,mKey);
        Log.d(TAG,"--Generating clusterID from flagged ads.");

        DatabaseReference dbRef =FirebaseDatabase.getInstance().getReference(Constants.FLAGGED_CLUSTERS).child(AdvertCategory);
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren()){
                    Log.d(TAG,"Flagged clusters has got children in it.");
                    for(DataSnapshot snap: dataSnapshot.getChildren()){
                        int clusterIDInCategory = snap.getValue(int.class);
                        Log.d(TAG,"Cluster id gotten from Flagged cluster is --"+clusterIDInCategory);
                        User.setID(clusterIDInCategory,mKey);
                        removeIdThenSubscribeUser(snap.getKey(),AdvertCategory,clusterIDInCategory);
                        break;
                    }
                }else{
                    Log.d(TAG,"--Flagged cluster in Category has got no children in it. Generating normally");
                    generateClusterIDFromCategory(AdvertCategory);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void generateClusterIDFromCategory(final String AdvertCategory){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference(Constants.CLUSTERS)
                .child(Constants.CLUSTERS_LIST).child(AdvertCategory);
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //This loads the current cluster in AdCategory
                long currentCluster;
                if(dataSnapshot.getChildrenCount() == 0){
                    currentCluster = dataSnapshot.getChildrenCount()+1;
                    Log.d(TAG,"The adCategory is empty.Setting current cluster to 1");
                }else{
                    currentCluster = dataSnapshot.getChildrenCount();
                    Log.d(TAG,"The latest current cluster in "+AdvertCategory+" category is :"+currentCluster);
                }

                //this loads number of users in the current cluster.
                DataSnapshot UsersInCurrentCluster = dataSnapshot.child(Integer.toString((int)currentCluster));
                long numberOfUsersInCurrentCluster;
                if(UsersInCurrentCluster.getChildrenCount()==0){
                    numberOfUsersInCurrentCluster = UsersInCurrentCluster.getChildrenCount();
                    Log.d(TAG,"--number of users in current clusters category is --"+numberOfUsersInCurrentCluster);
                }else{
                    numberOfUsersInCurrentCluster = UsersInCurrentCluster.getChildrenCount()+1;
                    Log.d(TAG,"--number of users in current clusters category is --"+numberOfUsersInCurrentCluster);
                }

                //this checks if the number of users in cluster exceeds limit
                int clusterIDForSpecificCategory;
                if(numberOfUsersInCurrentCluster<1000){
                    Log.d(TAG,"--Number of users in the current cluster is less than limit.setting AdCategory cluster to --"+currentCluster);
                    clusterIDForSpecificCategory = (int)currentCluster;
                }else {
                    Log.d(TAG, "--Number of users in the current cluster exceeds limit.setting AdCategory cluster to --" + (currentCluster + 1));
                    clusterIDForSpecificCategory = (int)currentCluster+1;
                }
                subscribeUserToAdvertCategoryAndAddCategoryToUserList(AdvertCategory,clusterIDForSpecificCategory);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void removeIdThenSubscribeUser(String key, final String AdvertCategory, final int clusterIDInCategory) {
        DatabaseReference dbr = FirebaseDatabase.getInstance().getReference(Constants.FLAGGED_CLUSTERS).child(AdvertCategory).child(key);
        dbr.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG,"---Removed ClusterId From Flagged Clusters. Subscribing user to AdCategory.");
                subscribeUserToAdvertCategoryAndAddCategoryToUserList(AdvertCategory,clusterIDInCategory);
            }
        });
    }

    public void subscribeUserToAdvertCategoryAndAddCategoryToUserList(String AdvertCategory,int Cluster){
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        //this subscribes user to Cluster in Advert Category
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference(Constants.CLUSTERS)
                .child(Constants.CLUSTERS_LIST).child(AdvertCategory).child(Integer.toString(Cluster)).child(uid);
        dbRef.setValue(uid);

        //this ads the advertCategory subscribed to by user to user space
        DatabaseReference dbRefUser = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.SUBSCRIPTION_lIST).child(AdvertCategory);
        dbRefUser.setValue(Cluster).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                iterations++;
                if(iterations == numberOfSubs){
                    Variables.setCurrentAdInSubscription(0);
                    Variables.setCurrentSubscriptionIndex(0);
                    Intent intent = new Intent(Constants.SET_UP_USERS_SUBSCRIPTION_LIST);
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                }
            }
        });

        Variables.Subscriptions.put(AdvertCategory,Cluster);

    }

    ////create User Methods.//////////////////////////////////////////////////////////////////////

    ////Load users data methods.////////////////////////////////////////

    public void loadUserData(final Context mContext){
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        User.setUid(uid);

        DatabaseReference adRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS).child(uid);
        adRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //this loads month totals
                DataSnapshot monthAdTotalSnap = dataSnapshot.child(Constants.TOTAL_NO_OF_ADS_SEEN_All_MONTH);
                int monthTotal = monthAdTotalSnap.getValue(int.class);
                Variables.setMonthAdTotals(mKey,monthTotal);
                Log.d(TAG,"Setting month total to : "+monthTotal);

                //this loads the users subscription list
                DataSnapshot subscriptionListSnap = dataSnapshot.child(Constants.SUBSCRIPTION_lIST);
                for(DataSnapshot snap: subscriptionListSnap.getChildren()){
                    String category = snap.getKey();
                    Integer cluster = snap.getValue(Integer.class);
                    Log.d(TAG,"Key category gotten from firebase is : "+category+" Value : "+cluster);
                    Variables.Subscriptions.put(category,cluster);
                }

                //this loads the last seen date from firebase
                DataSnapshot dateSnap = dataSnapshot.child(Constants.DATE_IN_FIREBASE);
                String date = dateSnap.getValue(String.class);
                Log.d(TAG,"Date gotten from firebase is : "+date);

                if(date.equals(getDate())){
                    Log.d(TAG,"---Date in firebase matches date in system,thus User was last online today");
                    Log.d(TAG,"Setting all the normal values from firebase");

                    //this loads today's ad totals.
                    DataSnapshot adTotalSnap = dataSnapshot.child(Constants.TOTAL_NO_OF_ADS_SEEN_TODAY);
                    int adTotal = adTotalSnap.getValue(int.class);
                    Variables.setAdTotal(adTotal,mKey);
                    Log.d(TAG,"Setting ad total to : "+adTotal);

                    //this loads the current category index
                    DataSnapshot currentSubIndexSnap = dataSnapshot.child(Constants.CURRENT_SUBSCRIPTION_INDEX);
                    int currentSubIndex = currentSubIndexSnap.getValue(int.class);
                    Variables.setCurrentSubscriptionIndex(currentSubIndex);
                    Log.d(TAG,"Setting the current Ad category index to :"+currentSubIndex);

                    //this loads the current ad being seen in the category
                    DataSnapshot currentAdInSubSnap = dataSnapshot.child(Constants.CURRENT_AD_IN_SUBSCRIPTION);
                    int currentAdInSubscription = currentAdInSubSnap.getValue(int.class);
                    Variables.setCurrentAdInSubscription(currentAdInSubscription);
                    Log.d(TAG,"Setting the current ad being seen in subscription to : "+currentAdInSubscription);

                }else{
                    Log.d(TAG,"---Date in firebase  does not match date in system , thus User was not online last today");
                    Log.d(TAG,"---Date from firebase is--"+date+"--while date in system is "+getDate());
                    Log.d(TAG,"Setting ad total, subscription index and current ad subscription to 0.");

                    Variables.setAdTotal(0,mKey);
                    Variables.setCurrentSubscriptionIndex(0);
                    Variables.setCurrentAdInSubscription(0);
                    resetTotalsInFirebase();
                }

                Intent intent = new Intent(Constants.LOADED_USER_DATA_SUCCESSFULLY);
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Intent intent = new Intent(Constants.FAILED_TO_LOAD_USER_DATA);
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
            }
        });

    }

    private void resetTotalsInFirebase() {
        Log.d(TAG,"---Resetting adtotal,current subscription and current ad in category in firebase to 0 due to it being a new day.");
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference adRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.TOTAL_NO_OF_ADS_SEEN_TODAY);
        adRef.setValue(0);

        DatabaseReference adRef3 = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.CURRENT_SUBSCRIPTION_INDEX);
        adRef3.setValue(0);

        DatabaseReference adRef4 = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.CURRENT_AD_IN_SUBSCRIPTION);
        adRef4.setValue(0);

        setLastSeenDateInFirebase();
    }

    public void setLastSeenDateInFirebase() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference adRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.DATE_IN_FIREBASE);
        adRef.setValue(getDate());
    }

    ////load user data methods.//////////////////////////////////////////////////////////////////////////


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

}
