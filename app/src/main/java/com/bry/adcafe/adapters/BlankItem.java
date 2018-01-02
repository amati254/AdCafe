package com.bry.adcafe.adapters;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.TextView;

import com.bry.adcafe.R;
import com.bry.adcafe.Variables;
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
    private boolean mIsLastElement;

    public BlankItem(Context context, PlaceHolderView PHView, long dateindays, String datetext,boolean isLastElement){
        this.mContext = context;
        this.mPlaceHolderView = PHView;
        this.dateInDays = dateindays;
        this.mDateText = datetext;
        this.mIsLastElement = isLastElement;
    }

    @Resolve
    private void onResolved(){
        listenForRemoveViewBroadcast();
        bl = this;
        if(mDateText.equals("pineapples")){
            listenForRemoveSelf();
        }
        if(mIsLastElement){
            Intent intent = new Intent("DONE!!");
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        }

    }

    private void listenForRemoveSelf() {
        try{
            LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverToRemoveSelf,
                    new IntentFilter("REMOVE_PLACEHOLDER_BLANK_ITEM"+dateInDays));
        }catch (Exception e){
            e.printStackTrace();
            LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverToRemoveSelf,
                    new IntentFilter("REMOVE_PLACEHOLDER_BLANK_ITEM"+ Variables.noOfDays));
        }
    }

    private void listenForRemoveViewBroadcast(){

        try{
            LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverForRemoveBlank,
                    new IntentFilter("REMOVE_BLANK_ITEMS"+dateInDays));
        }catch (Exception e){
            e.printStackTrace();
            LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverForRemoveBlank,
                    new IntentFilter("REMOVE_BLANK_ITEMS"+ Variables.noOfDays));
        }

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
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverToRemoveSelf);

        }
    };

    private BroadcastReceiver mMessageReceiverToRemoveSelf = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("BlankItem-","Received broadcast to remove self because of multiple");
            removeItem();
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(this);

        }
    };

    private void removeItem(){
        try{
            mPlaceHolderView.removeView(bl);
        }catch (Exception e){
            e.printStackTrace();
            Variables.placeHolderView.removeView(bl);
        }

    }

}
