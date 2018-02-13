package com.bry.adcafe.services;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.bry.adcafe.Constants;
import com.bry.adcafe.Variables;
import com.bry.adcafe.models.User;
import com.bry.adcafe.ui.LoginActivity;
import com.bry.adcafe.ui.MainActivity;
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
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by bryon on 24/11/2017.
 */

public class DatabaseManager {
    public static final String TAG = DatabaseManager.class.getSimpleName();
    private String mKey = "";
    private int numberOfSubs = 0;
    private int iterations = 0;
    private Context context;
    private List<String> categoryList = new ArrayList<>();
    private boolean isUserAddingANewCategory = false;
    private int iterationsForResettingCPV = 0;

    ////Create user methods//////

    public void createUserSpace(final Context mContext){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();
        context = mContext;

        //Creates nodes for totals seen today and sets them to 0;
        DatabaseReference adRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.TOTAL_NO_OF_ADS_SEEN_TODAY);
        adRef.setValue(0);

        //Creates node for totals amount earned so far and sets it to 0;
        DatabaseReference adRef9 = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.REIMBURSEMENT_TOTALS);
        adRef9.setValue(0);

        DatabaseReference adRef10 = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.RESET_ALL_SUBS_BOOLEAN);
        adRef10.setValue(false);

        DatabaseReference adRef11 = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.PREFERRED_NOTIF);
        adRef11.setValue(true);

        //Creates node for indicating users email.
        DatabaseReference adRef8 = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child("Email");
        adRef8.setValue(user.getEmail());

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

    public void setContext(Context context){
        this.context = context;
    }

    public void unSubscribeUserFormAdvertCategory(String AdvertCategory, int clusterIDInCategory){
        FlagSubscriptionThenUnsubscribeUser(AdvertCategory,clusterIDInCategory);
    }

    private void FlagSubscriptionThenUnsubscribeUser(final String AdvertCategory, final int clusterIDInCategory){
        Log.d(TAG,"Unsubscribing user from cluster "+ AdvertCategory);
        DatabaseReference dbRef =FirebaseDatabase.getInstance().getReference(Constants.FLAGGED_CLUSTERS)
                .child(Integer.toString(Variables.constantAmountPerView)).child(AdvertCategory);
        DatabaseReference dbref = dbRef.push();
        dbref.setValue(clusterIDInCategory).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                removeSpecificAdCategryFromUserSpaceAndSubscriptions(AdvertCategory,clusterIDInCategory);
            }
        });
    }

    private void removeSpecificAdCategryFromUserSpaceAndSubscriptions(final String AdvertCategory, int Cluster){
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Log.d(TAG,"Removing user from subscription");
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference(Constants.CLUSTERS).child(Constants.CLUSTERS_LIST)
                .child(Integer.toString(Variables.constantAmountPerView))
                .child(AdvertCategory)
                .child(Integer.toString(Cluster)).child(uid);
        dbRef.removeValue();

        Log.d(TAG,"Removing category "+AdvertCategory+" from users categories list.");
        DatabaseReference dbRefUser = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.SUBSCRIPTION_lIST).child(AdvertCategory);
        dbRefUser.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(getPositionOf(AdvertCategory)==Variables.getCurrentSubscriptionIndex()){
                    Log.d(TAG,"The category being removed is currently being viewed");
                    if(Variables.getCurrentSubscriptionIndex()>0)
                        Variables.setCurrentSubscriptionIndex((Variables.getCurrentSubscriptionIndex()-1));
                    else Variables.setCurrentSubscriptionIndex((Variables.getCurrentSubscriptionIndex()+1));
                }
                String categoryBeingViewed = getSubscriptionValue(Variables.getCurrentSubscriptionIndex());
                Log.d(TAG,"the current category being removed is "+categoryBeingViewed);
                Variables.Subscriptions.remove(AdvertCategory);

                int newIndex = getPositionOf(categoryBeingViewed);
                Log.d(TAG,"Its new index position is : "+newIndex);
                Variables.setCurrentSubscriptionIndex(newIndex);

                updateCurrentSubIndex();
                setUserDataInSharedPrefs(context);
                Variables.hasChangesBeenMadeToCategories = true;

                Intent intent = new Intent(Constants.FINISHED_UNSUBSCRIBING);
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            }
        });

    }




    public void subscribeUserToSpecificCategory(String AdvertCategory){
        numberOfSubs = 1;
        isUserAddingANewCategory = true;
        generateClusterIDFromCategoryFlaggedClusters(AdvertCategory);
    }

    public void setUpUserSubscriptions(List<String> subscriptions){
        numberOfSubs = subscriptions.size();
        setUsersPreferredChargePerView();
        Variables.Subscriptions.clear();
        for(String sub:subscriptions){
            generateClusterIDFromCategoryFlaggedClusters(sub);
        }
    }

    private void setUsersPreferredChargePerView(){
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference adRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.CONSTANT_AMMOUNT_PER_VIEW);
        adRef.setValue(Variables.constantAmountPerView);
    }




    private void generateClusterIDFromCategoryFlaggedClusters(final String AdvertCategory){
        Variables.setMonthAdTotals(mKey,0);
        Variables.setAdTotal(0,mKey);
        Log.d(TAG,"--Generating clusterID from flagged ads.");

        DatabaseReference dbRef =FirebaseDatabase.getInstance().getReference(Constants.FLAGGED_CLUSTERS)
                .child(Integer.toString(Variables.constantAmountPerView)).child(AdvertCategory);
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren()){
                    Log.d(TAG,"Flagged clusters has got children in it.");
                    for(DataSnapshot snap: dataSnapshot.getChildren()){
                        int clusterIDInCategory = snap.getValue(int.class);
                        Log.d(TAG,"Cluster id gotten from Flagged cluster is --"+clusterIDInCategory);
//                        User.setID(clusterIDInCategory,mKey);
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

    private void generateClusterIDFromCategory(final String AdvertCategory){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference(Constants.CLUSTERS)
                .child(Constants.CLUSTERS_LIST).child(Integer.toString(Variables.constantAmountPerView))
                .child(AdvertCategory);
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
        DatabaseReference dbr = FirebaseDatabase.getInstance().getReference(Constants.FLAGGED_CLUSTERS)
                .child(Integer.toString(Variables.constantAmountPerView)).child(AdvertCategory).child(key);
        dbr.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG,"---Removed ClusterId From Flagged Clusters. Subscribing user to AdCategory.");
                subscribeUserToAdvertCategoryAndAddCategoryToUserList(AdvertCategory,clusterIDInCategory);
            }
        });
    }

    private void subscribeUserToAdvertCategoryAndAddCategoryToUserList(final String AdvertCategory, final int Cluster){
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        //this subscribes user to Cluster in Advert Category
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference(Constants.CLUSTERS)
                .child(Constants.CLUSTERS_LIST).child(Integer.toString(Variables.constantAmountPerView))
                .child(AdvertCategory)
                .child(Integer.toString(Cluster)).child(uid);
        dbRef.setValue(uid);

        //this ads the advertCategory subscribed to by user to user space
        DatabaseReference dbRefUser = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.SUBSCRIPTION_lIST).child(AdvertCategory);
        dbRefUser.setValue(Cluster).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                iterations++;
                if(iterations == numberOfSubs){
                    if(isUserAddingANewCategory){
                        loadNewSubList();
                        isUserAddingANewCategory = false;
                    }else{
                        setDateInSharedPrefs(getDate(),context);
                        reloadUsersSubscriptions(Constants.SET_UP_USERS_SUBSCRIPTION_LIST);
                    }
                }
            }
        });

        if(!isUserAddingANewCategory) Variables.Subscriptions.put(AdvertCategory,Cluster);

    }




    public void setNumberOfSubscriptionsUserKnowsAbout(int number){
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference adRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.NO_OF_CATEGORIES_KNOWN);
        adRef.setValue(number);
    }

    ////create User Methods.//////////////////////////////////////////////////////////////////////




    ////Load users data methods.///

    public void loadUserData(final Context mContext){
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference(Constants.CATEGORY_LIST);
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot snap:dataSnapshot.getChildren()) {
                    String category = snap.getKey();
                    categoryList.add(category);
                }
                loadUserDataNow(mContext);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(mContext,"Please check your internet connection.",Toast.LENGTH_LONG).show();
                Log.d(TAG,"There was a database error "+databaseError.getMessage());
            }
        });
    }

    private void loadUserDataNow(final Context mContext){
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        User.setUid(uid);
        Log.d(TAG,"Starting to load users data");
        DatabaseReference adRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS).child(uid);
        adRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //this loads month totals
                DataSnapshot monthAdTotalSnap = dataSnapshot.child(Constants.TOTAL_NO_OF_ADS_SEEN_All_MONTH);
                int monthTotal = monthAdTotalSnap.getValue(int.class);
                Variables.setMonthAdTotals(mKey,monthTotal);
                Log.d(TAG,"Setting month total to : "+monthTotal);

                //this loads the users reimbursement totals.
                DataSnapshot reimbursementAmountSnap = dataSnapshot.child(Constants.REIMBURSEMENT_TOTALS);
                int reimbursementAmount = reimbursementAmountSnap.getValue(int.class);
                Variables.setTotalReimbursementAmount(reimbursementAmount);
                Log.d(TAG,"Setting reimbursement total to : "+reimbursementAmount);

                //this loads the users preferred amount per ad view.
                DataSnapshot amountPerViewSnap = dataSnapshot.child(Constants.CONSTANT_AMMOUNT_PER_VIEW);
                if(amountPerViewSnap.exists()){
                    int amountPerView = amountPerViewSnap.getValue(int.class);
                    Variables.constantAmountPerView = amountPerView;
                    Log.d(TAG,"Setting the amount per ad to total to : "+amountPerView);
                }

                DataSnapshot notPrefSnap = dataSnapshot.child(Constants.PREFERRED_NOTIF);
                Variables.doesUserWantNotifications = notPrefSnap.getValue(Boolean.class);
                Log.d(TAG,"Set the preferred value for receivin morning notifications to :"+Variables.doesUserWantNotifications);

                //this loads the users no Of Categories known
                DataSnapshot subNoKnown = dataSnapshot.child(Constants.NO_OF_CATEGORIES_KNOWN);
                int subNumberKnown = subNoKnown.getValue(int.class);
                if(subNumberKnown<categoryList.size()){
                    Variables.didAdCafeAddNewCategory = true;
                    setNumberOfSubscriptionsUserKnowsAbout(categoryList.size());
                }

                //this loads the users subscription list
                DataSnapshot subscriptionListSnap = dataSnapshot.child(Constants.SUBSCRIPTION_lIST);
                Variables.Subscriptions.clear();
                for(DataSnapshot snap: subscriptionListSnap.getChildren()){
                    String category = snap.getKey();
                    Integer cluster = snap.getValue(Integer.class);
                    Log.d(TAG,"Key category gotten from firebase is : "+category+" Value : "+cluster);
                    if(categoryList.contains(category)){
                        Variables.Subscriptions.put(category,cluster);
                    }else{
                        unSubscribeUserFormAdvertCategory(category,cluster);
                        Variables.didAdCafeRemoveCategory = true;
                        setNumberOfSubscriptionsUserKnowsAbout(categoryList.size());
                    }
                }

                //this loads the last seen date from firebase
                DataSnapshot dateSnap = dataSnapshot.child(Constants.DATE_IN_FIREBASE);
                String date = dateSnap.getValue(String.class);
                Log.d(TAG,"Date gotten from firebase is : "+date);
                setDateInSharedPrefs(getDate(),mContext);
                boolean isNewDay = false;

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
                    isNewDay = true;
                }
                DataSnapshot isNeedToResetSubsSnap = dataSnapshot.child(Constants.RESET_ALL_SUBS_BOOLEAN);
                if(isNewDay && isNeedToResetSubsSnap.getValue(Boolean.class)){
                    DataSnapshot newConstantCPVSnap = dataSnapshot.child(Constants.NEW_CPV);
                    int newConstantCPV = newConstantCPVSnap.getValue(Integer.class);
                    int oldConstantCPV = Variables.constantAmountPerView;
                    resetUsersSubscriptionsForNewPrice(oldConstantCPV,newConstantCPV);
                }else{
                    setUserDataInSharedPrefs(mContext);
                    Intent intent = new Intent(Constants.LOADED_USER_DATA_SUCCESSFULLY);
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                }

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


    ////Other stuff.///
    private void setDateInSharedPrefs(String date,Context context){
        Log.d(TAG, "---Setting current date in shared preferences.");
        SharedPreferences prefs = context.getSharedPreferences(Constants.DATE, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("date", date);
        editor.apply();
    }

    private void setUserDataInSharedPrefs(Context context) {
        SharedPreferences pref5 = context.getSharedPreferences("CurrentSubIndex", MODE_PRIVATE);
        SharedPreferences.Editor editor5 = pref5.edit();
        editor5.clear();
        editor5.putInt("CurrentSubIndex", Variables.getCurrentSubscriptionIndex());
        Log.d("DatabaseManager---", "Setting the users current subscription index in shared preferences - " + Variables.getCurrentSubscriptionIndex());
        editor5.apply();

        SharedPreferences pref6 = context.getSharedPreferences("CurrentAdInSubscription", MODE_PRIVATE);
        SharedPreferences.Editor editor6 = pref6.edit();
        editor6.clear();
        editor6.putInt("CurrentAdInSubscription", Variables.getCurrentAdInSubscription());
        Log.d("DatabaseManager---", "Setting the current ad in subscription in shared preferences - " + Variables.getCurrentAdInSubscription());
        editor6.apply();

        SharedPreferences pref = context.getSharedPreferences("TodayTotals", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.putInt("TodaysTotals", Variables.getAdTotal(mKey));
        Log.d("DatabaseManager--", "Setting todays ad totals in shared preferences - " + Integer.toString(Variables.getAdTotal(mKey)));
        editor.apply();

        SharedPreferences pref2 = context.getSharedPreferences("MonthTotals", MODE_PRIVATE);
        SharedPreferences.Editor editor2 = pref2.edit();
        editor2.clear();
        editor2.putInt("MonthsTotals", Variables.getMonthAdTotals(mKey));
        Log.d("DatabaseManager--", "Setting the month totals in shared preferences - " + Integer.toString(Variables.getMonthAdTotals(mKey)));
        editor2.apply();

        SharedPreferences pref3 = context.getSharedPreferences("ReimbursementTotals",MODE_PRIVATE);
        SharedPreferences.Editor editor3 = pref3.edit();
        editor3.clear();
        editor3.putInt(Constants.REIMBURSEMENT_TOTALS,Variables.getTotalReimbursementAmount());
        Log.d("DatabaseManager","Setting the Reimbursement totals in shared preferences - "+Integer.toString(Variables.getTotalReimbursementAmount()));
        editor3.apply();

        SharedPreferences pref4 = context.getSharedPreferences(Constants.CONSTANT_AMMOUNT_PER_VIEW,MODE_PRIVATE);
        SharedPreferences.Editor editor4 = pref4.edit();
        editor4.clear();
        editor4.putInt(Constants.CONSTANT_AMMOUNT_PER_VIEW,Variables.constantAmountPerView);
        Log.d("DatabaseManager","Setting the constant amount per view in shared preferences - "+Integer.toString(Variables.constantAmountPerView));
        editor4.apply();

        SharedPreferences pref7 = context.getSharedPreferences(Constants.PREFERRED_NOTIF,MODE_PRIVATE);
        SharedPreferences.Editor editor7 = pref7.edit();
        editor7.clear();
        editor7.putBoolean(Constants.PREFERRED_NOTIF,Variables.doesUserWantNotifications);
        Log.d(TAG,"Set the users preference for seing notifications to : "+Variables.doesUserWantNotifications);
        editor7.apply();

        setSubsInSharedPrefs(context);
    }

    private void setSubsInSharedPrefs(Context context) {
        Gson gson = new Gson();
        String hashMapString = gson.toJson(Variables.Subscriptions);

        SharedPreferences prefs = context.getSharedPreferences("Subscriptions", MODE_PRIVATE);
        prefs.edit().putString("hashString", hashMapString).apply();
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




    private String getSubscriptionValue(int index) {
        LinkedHashMap map = Variables.Subscriptions;
        String Sub = (new ArrayList<String>(map.keySet())).get(index);
        Log.d("SubscriptionManagerItem", "Subscription gotten from getCurrent Subscription method is :" + Sub);
        return Sub;
    }

    private int getPositionOf(String subscription) {
        LinkedHashMap map = Variables.Subscriptions;
        List<String> indexes = new ArrayList<String>(map.keySet());
        return indexes.indexOf(subscription);
    }

    private void updateCurrentSubIndex(){
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference adRef3 = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.CURRENT_SUBSCRIPTION_INDEX);
        Log.d(TAG,"Setting current subscription index in firebase to :"+Variables.getCurrentSubscriptionIndex());
        adRef3.setValue(Variables.getCurrentSubscriptionIndex());
    }

    private void loadNewSubList(){
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        User.setUid(uid);
        Log.d(TAG,"Starting to load users data");
        DatabaseReference adRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.SUBSCRIPTION_lIST);
        adRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String categoryBeingViewed = getSubscriptionValue(Variables.getCurrentSubscriptionIndex());
                Log.d(TAG,"the current category being added is "+categoryBeingViewed);
                Log.d(TAG,"its index position is : "+getPositionOf(categoryBeingViewed));
                Variables.Subscriptions.clear();
                for(DataSnapshot snap: dataSnapshot.getChildren()){
                    String category = snap.getKey();
                    Integer cluster = snap.getValue(Integer.class);
                    Log.d(TAG,"Key category gotten from firebase is : "+category+" Value : "+cluster);
                    Variables.Subscriptions.put(category,cluster);
                }
                int newIndex = getPositionOf(categoryBeingViewed);

                Log.d(TAG,"Its new index position is : "+newIndex);
                Variables.setCurrentSubscriptionIndex(newIndex);
                updateCurrentSubIndex();

                Intent intent = new Intent(Constants.SET_UP_USERS_SUBSCRIPTION_LIST);
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                setUserDataInSharedPrefs(context);
                Variables.hasChangesBeenMadeToCategories = true;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }




    //called in main activity
    public void checkIfNeedToResetUsersSubscriptions(final Context myContext){
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference adRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS).child(uid);
        adRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DataSnapshot isNeedToResetSubsSnap = dataSnapshot.child(Constants.RESET_ALL_SUBS_BOOLEAN);
                if(isNeedToResetSubsSnap.getValue(Boolean.class)){
                    DataSnapshot newConstantCPVSnap = dataSnapshot.child(Constants.NEW_CPV);
                    int newConstantCPV = newConstantCPVSnap.getValue(Integer.class);
                    int oldConstantCPV = Variables.constantAmountPerView;
                    resetUsersSubscriptionsForNewPrice(oldConstantCPV,newConstantCPV);
                }else{
                    setUserDataInSharedPrefs(myContext);
                    Intent intent = new Intent(Constants.LOADED_USER_DATA_SUCCESSFULLY);
                    LocalBroadcastManager.getInstance(myContext).sendBroadcast(intent);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void resetUsersSubscriptionsForNewPrice(int oldConstant, int newConstant){
        for(String AdvertCategory: Variables.Subscriptions.keySet()){
            int clusterIDInCategory = Variables.Subscriptions.get(AdvertCategory);
            removeSpecificCategoryForResettingNewPrice(newConstant,oldConstant,AdvertCategory,clusterIDInCategory);
        }
    }

    //This will flag the category first...
    private void removeSpecificCategoryForResettingNewPrice(final int newConstant,final int oldConstant, final String AdvertCategory, final int clusterIDInCategory){
        Log.d(TAG,"Unsubscribing user from cluster "+ AdvertCategory);
        DatabaseReference dbRef =FirebaseDatabase.getInstance().getReference(Constants.FLAGGED_CLUSTERS)
                .child(Integer.toString(oldConstant)).child(AdvertCategory);
        DatabaseReference dbref = dbRef.push();
        dbref.setValue(clusterIDInCategory).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                removeSpecificAdCategryFromUserSpaceAndSubscriptionsForReset(newConstant,oldConstant,AdvertCategory,clusterIDInCategory);
            }
        });
    }

    //this then removes from cluster and users cluster list...
    private void removeSpecificAdCategryFromUserSpaceAndSubscriptionsForReset(final int newConstant, final int oldConstant, final String AdvertCategory, int Cluster) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Log.d(TAG,"Removing user from subscription");
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference(Constants.CLUSTERS).child(Constants.CLUSTERS_LIST)
                .child(Integer.toString(oldConstant))
                .child(AdvertCategory)
                .child(Integer.toString(Cluster)).child(uid);
        dbRef.removeValue();

        Log.d(TAG,"Removing category "+AdvertCategory+" from users categories list.");
        DatabaseReference dbRefUser = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.SUBSCRIPTION_lIST).child(AdvertCategory);
        dbRefUser.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
               iterationsForResettingCPV++;
               if(iterationsForResettingCPV==Variables.Subscriptions.size()){
                   startResubscriptionForAllUsersSubscriptions(newConstant);
               }
            }
        });
    }




    //this will start resubscription for users subscriptions
    private void startResubscriptionForAllUsersSubscriptions(final int newConstant) {
        List<String> subList = new ArrayList<>();
        subList.addAll(Variables.Subscriptions.keySet());
        setUpUsersNewSubList(subList,newConstant);
    }

    private void setUpUsersNewSubList(List<String> subscriptions, int newConstant){
        numberOfSubs = subscriptions.size();
        iterations = 0;
        Variables.Subscriptions.clear();
        for(String sub:subscriptions){
            generateClusterIDFromCategoryFlaggedClustersForCategroyReset(sub,newConstant);
        }
    }

    private void generateClusterIDFromCategoryFlaggedClustersForCategroyReset(final String AdvertCategory,final int newConstant) {
        DatabaseReference dbRef =FirebaseDatabase.getInstance().getReference(Constants.FLAGGED_CLUSTERS)
                .child(Integer.toString(newConstant)).child(AdvertCategory);
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren()){
                    Log.d(TAG,"Flagged clusters has got children in it.");
                    for(DataSnapshot snap: dataSnapshot.getChildren()){
                        int clusterIDInCategory = snap.getValue(int.class);
                        Log.d(TAG,"Cluster id gotten from Flagged cluster is --"+clusterIDInCategory);
                        removeIdThenSubscribeUserForCategoryReset(newConstant,snap.getKey(),AdvertCategory,clusterIDInCategory);
                        break;
                    }
                }else{
                    Log.d(TAG,"--Flagged cluster in Category has got no children in it. Generating normally");
                    generateClusterIDFromCategoryForCategoryReset(newConstant,AdvertCategory);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void generateClusterIDFromCategoryForCategoryReset(final int newConstant, final String AdvertCategory) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference(Constants.CLUSTERS)
                .child(Constants.CLUSTERS_LIST).child(Integer.toString(newConstant))
                .child(AdvertCategory);
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //This loads the current cluster in AdCategory
                long currentCluster;
                if(dataSnapshot.getChildrenCount() == 0){
                    currentCluster = 1;
                    Log.d(TAG,"The adCategory is empty.Setting current cluster to 1");
                }else{
                    currentCluster = dataSnapshot.getChildrenCount();
                    Log.d(TAG,"The latest current cluster in "+AdvertCategory+" category is :"+currentCluster);
                }

                //this loads number of users in the current cluster.
                DataSnapshot UsersInCurrentCluster = dataSnapshot.child(Long.toString(currentCluster));
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
                subscribeUserToAdvertCategoryAndAddCategoryToUserListForCategoryReset(newConstant,AdvertCategory,clusterIDForSpecificCategory);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void removeIdThenSubscribeUserForCategoryReset(final int newConstant, String key, final String AdvertCategory, final int clusterIDInCategory) {
        DatabaseReference dbr = FirebaseDatabase.getInstance().getReference(Constants.FLAGGED_CLUSTERS)
                .child(Integer.toString(newConstant)).child(AdvertCategory).child(key);
        dbr.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG,"---Removed ClusterId From Flagged Clusters. Subscribing user to AdCategory.");
                subscribeUserToAdvertCategoryAndAddCategoryToUserListForCategoryReset(newConstant,AdvertCategory,clusterIDInCategory);
            }
        });
    }

    private void subscribeUserToAdvertCategoryAndAddCategoryToUserListForCategoryReset(final int newConstant, String AdvertCategory, int Cluster) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        //this subscribes user to Cluster in Advert Category
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference(Constants.CLUSTERS)
                .child(Constants.CLUSTERS_LIST).child(Integer.toString(newConstant))
                .child(AdvertCategory)
                .child(Integer.toString(Cluster)).child(uid);
        dbRef.setValue(uid);

        //this ads the advertCategory subscribed to by user to user space
        DatabaseReference dbRefUser = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.SUBSCRIPTION_lIST).child(AdvertCategory);
        dbRefUser.setValue(Cluster).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                iterations++;
                if(iterations == numberOfSubs){
                    setDateInSharedPrefs(getDate(),context);
                    Variables.constantAmountPerView = newConstant;
                    setNewConstantAsConstantInDatabase();
                    reloadUsersSubscriptions(Constants.LOADED_USER_DATA_SUCCESSFULLY);
                }
            }
        });
    }




    private void setNewConstantAsConstantInDatabase() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        setUsersPreferredChargePerView();
        DatabaseReference adRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.RESET_ALL_SUBS_BOOLEAN);
        adRef.setValue(false);

        DatabaseReference adRef2 = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.NEW_CPV);
        adRef2.setValue(Variables.constantAmountPerView);
    }

    private void reloadUsersSubscriptions(final String intentString){
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        User.setUid(uid);
        Log.d(TAG,"Starting to load users data");
        DatabaseReference adRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.SUBSCRIPTION_lIST);
        adRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!Variables.Subscriptions.isEmpty())Variables.Subscriptions.clear();
                for(DataSnapshot snap: dataSnapshot.getChildren()){
                    String category = snap.getKey();
                    Integer cluster = snap.getValue(Integer.class);
                    Log.d(TAG,"Key category gotten from firebase is : "+category+" Value : "+cluster);
                    Variables.Subscriptions.put(category,cluster);
                }
                setUserDataInSharedPrefs(context);
                Intent intent = new Intent(intentString);
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void setBooleanForResetSubscriptions(int newValue, final Context myContext){
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference adRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.RESET_ALL_SUBS_BOOLEAN);
        adRef.setValue(true);

        DatabaseReference adRef2 = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.NEW_CPV);
        adRef2.setValue(newValue).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Intent intent = new Intent(Constants.FINISHED_SETUP_FOR_RESETTING_SUBS);
                LocalBroadcastManager.getInstance(myContext).sendBroadcast(intent);
            }
        });
    }


    ////Other stuff.////////////////////////////////////////////////////////////////////////////////////////

}