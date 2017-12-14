package com.bry.adcafe.adapters;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.bry.adcafe.Constants;
import com.bry.adcafe.R;
import com.bry.adcafe.Variables;
import com.bry.adcafe.services.DatabaseManager;
import com.mindorks.placeholderview.PlaceHolderView;
import com.mindorks.placeholderview.annotations.Click;
import com.mindorks.placeholderview.annotations.Layout;
import com.mindorks.placeholderview.annotations.NonReusable;
import com.mindorks.placeholderview.annotations.Resolve;
import com.mindorks.placeholderview.annotations.View;

/**
 * Created by bryon on 29/11/2017.
 */

@NonReusable
@Layout(R.layout.select_category_item)
public class SubscriptionManagerItem {
    @View(R.id.cat_name) private TextView categoryName;
    @View(R.id.cat_details) private TextView categoryDetails;
    @View(R.id.cat_select) private CheckBox checkBox;

    private Context mContext;
    private PlaceHolderView mPlaceHolderView;
    private String category;
    private String details;
    private boolean isChecked;
    private boolean isSubscribing;
    private boolean areListenersActive;

    public SubscriptionManagerItem(Context context, PlaceHolderView placeHV,String Category,String Details,boolean isChecked){
        this.mContext = context;
        this.mPlaceHolderView = placeHV;
        this.category = Category;
        this.details = Details;
        this.isChecked = isChecked;
    }

    @Resolve
    private void onResolved() {
        categoryName.setText(category);
        categoryDetails.setText(details);
        checkBox.setChecked(isChecked);

        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverForUnregisterAllReceivers,
                new IntentFilter("UNREGISTER_ALL"));

    }

    @Click(R.id.cat_select)
    private void onClick(){
        if(isChecked){
            if(isOnline(mContext)) removeSubscription();
            else Toast.makeText(mContext, "You might need an internet connection to un-subscribe.", Toast.LENGTH_SHORT).show();
        }else{
            if(isOnline(mContext)) addSubscription();
            else Toast.makeText(mContext, "You might need an internet connection to subscribe.", Toast.LENGTH_SHORT).show();
        }
    }

    private void addSubscription() {
        Variables.areYouSureText = "Are you sure you want to subscribe to "+category+"?";
        Intent intent = new Intent(Constants.CONFIRM_START);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        isSubscribing = true;
        setUpTransactionListeners();
    }

    private void removeSubscription() {
        Variables.areYouSureText = "Are you sure you want to unsubscribe to "+category+"?";
        Intent intent = new Intent(Constants.CONFIRM_START);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        isSubscribing = false;
        setUpTransactionListeners();
    }

    private BroadcastReceiver mMessageReceiverForAllClear = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(isSubscribing){
                new DatabaseManager().subscribeUserToSpecificCategory(category);
                Log.d("SelectCategoryItem - ", "Adding category - " + category);
            }else{
                int SubscriptionClusterID = Variables.Subscriptions.get(category);
                new DatabaseManager().unSubscribeUserFormAdvertCategory(category,SubscriptionClusterID);
                Log.d("SelectCategoryItem - ", "Removing category - " + category);
            }

        }
    };

    private BroadcastReceiver mMessageReceiverForFinishedSubscribing = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            checkBox.setChecked(true);
            isChecked = true;
            removeTransactionListeners();
        }
    };

    private BroadcastReceiver mMessageReceiverForFinishedUnSubscribing = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            checkBox.setChecked(false);
            isChecked = false;
            removeTransactionListeners();
        }
    };

    private BroadcastReceiver mMessageReceiverForUnregisterAllReceivers = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
          if(areListenersActive)removeTransactionListeners();
          LocalBroadcastManager.getInstance(mContext).unregisterReceiver(this);
        }
    };

    private BroadcastReceiver mMessageReceiverForUserCanceled = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            removeTransactionListeners();
            checkBox.setChecked(isChecked);
        }
    };

    private void setUpTransactionListeners(){
        areListenersActive = true;
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverForAllClear,
                new IntentFilter(Constants.ALL_CLEAR)); //this receives broadcast for all clear.

        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverForFinishedSubscribing,
                new IntentFilter(Constants.SET_UP_USERS_SUBSCRIPTION_LIST)); //this receives broadcast for finishing adding subscription.

        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverForFinishedUnSubscribing,
                new IntentFilter(Constants.FINISHED_UNSUBSCRIBING)); //this receives broadcast for finishing unsubscribing user.

        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverForUserCanceled,
                new IntentFilter(Constants.CANCELLED));

    }

    private void removeTransactionListeners(){
        areListenersActive = false;
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForAllClear);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForFinishedSubscribing);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForFinishedUnSubscribing);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForUserCanceled);

    }

    private boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        //should check null because in airplane mode it will be null
        return (netInfo != null && netInfo.isConnected());
    }

}
