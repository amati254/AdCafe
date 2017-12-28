package com.bry.adcafe.adapters;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.TextView;

import com.bry.adcafe.R;
import com.mindorks.placeholderview.PlaceHolderView;
import com.mindorks.placeholderview.annotations.Layout;
import com.mindorks.placeholderview.annotations.NonReusable;
import com.mindorks.placeholderview.annotations.Resolve;
import com.mindorks.placeholderview.annotations.View;

/**
 * Created by bryon on 28/12/2017.
 */

@NonReusable
@Layout(R.layout.blank_item)
public class BlankItem {
    private Context mContext;
    private PlaceHolderView mPlaceHolderView;
    private Long dateInDays;
    private String mDateText;
    private BlankItem bl;

    public BlankItem(Context context, PlaceHolderView PHView, long dateindays, String datetext){
        this.mContext = context;
        this.mPlaceHolderView = PHView;
        this.dateInDays = dateindays;
        this.mDateText = datetext;
    }

    @Resolve
    private void onResolved(){
        listenForRemoveViewBroadcast();
        bl = this;
    }

    private void listenForRemoveViewBroadcast(){
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverForRemoveBlank,
                new IntentFilter("REMOVE_BLANK_ITEMS"));
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverToUnregisterAllReceivers,
                new IntentFilter("UNREGISTER"));
    }

    private BroadcastReceiver mMessageReceiverForRemoveBlank = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("BlankItem","Received broadcast to Remove blank");
            removeItem();
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(this);
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverToUnregisterAllReceivers);

        }
    };

    private BroadcastReceiver mMessageReceiverToUnregisterAllReceivers = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("BlankItem-","Received broadcast to Unregister all receivers");
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(this);
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForRemoveBlank);

        }
    };

    private void removeItem(){
        mPlaceHolderView.removeView(bl);
    }

}
