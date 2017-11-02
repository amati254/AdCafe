package com.bry.adcafe.adapters;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.CountDownTimer;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bry.adcafe.Constants;
import com.bry.adcafe.R;
import com.bry.adcafe.Variables;
import com.mindorks.placeholderview.PlaceHolderView;
import com.mindorks.placeholderview.annotations.Layout;
import com.mindorks.placeholderview.annotations.LongClick;
import com.mindorks.placeholderview.annotations.Resolve;
import com.mindorks.placeholderview.annotations.View;

/**
 * Created by bryon on 26/08/2017.
 */

@Layout(R.layout.top_bar_view)
public class AdCounterBar {
    @View(R.id.adCounter) private TextView adCounter;
    @View(R.id.progressBarTimer) private ProgressBar progressBarTimer;
    @View(R.id.textViewTime) private TextView textViewTime;

    private Context mContext;
    private PlaceHolderView mPlaceHolderView;
    private boolean hasTimerMessageBeenSent;
    private boolean hasTimerStarted;
    private String mKey = "";

    public AdCounterBar(Context context, PlaceHolderView PlaceHolderView){
        mContext = context;
        mPlaceHolderView = PlaceHolderView;
    }
    @Resolve
    private void onResolved() {
        adCounter.setText(Integer.toString(Variables.getAdTotal(mKey)));
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverToStartTimer,new IntentFilter(Constants.ADVERT_CARD_BROADCAST_TO_START_TIMER));
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverToUnregisterAllReceivers,new IntentFilter(Constants.UNREGISTER_ALL_RECEIVERS));


        if(Variables.mIsLastOrNotLast==Constants.NOT_LAST){
//            startTimer();
        }

    }


    @LongClick(R.id.adCounter)
    private void onLongClick(){

    }

    private BroadcastReceiver mMessageReceiverToStartTimer = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("AD_COUNTER_BAR - ","Broadcast has been received to start timer.");
            startTimer();
        }
    };


    private BroadcastReceiver mMessageReceiverToUnregisterAllReceivers = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("AD_COUNTER_BAR--","Received broadcast to Unregister all receivers");
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverToStartTimer);
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverToUnregisterAllReceivers);

        }
    };


    private void startTimer(){
        if(!hasTimerStarted){
            Variables.hasTimerStarted = true;
            hasTimerStarted = true;
             new CountDownTimer(7*1000,400) {
                @Override
                public void onTick(long millisUntilFinished) {
                    hasTimerMessageBeenSent = false;
                    long timeLeftInSeconds = millisUntilFinished/1000;
                    progressBarTimer.setProgress((int)timeLeftInSeconds*7);
                    textViewTime.setText(Integer.toString((int)timeLeftInSeconds));
                }

                @Override
                public void onFinish() {
                    progressBarTimer.setProgress(7*1000);
                    Log.d("Timer --- ","Timer has finnished");
                        sendBroadcast(Constants.TIMER_HAS_ENDED);
                        addToSharedPreferencesViaBroadcast();
                        hasTimerStarted = false;
                    adCounter.setText(Integer.toString(Variables.getAdTotal(mKey)+1));
                    textViewTime.setText(Integer.toString(7));

                }
            }.start();
        }

    }


    private void sendBroadcast(String message){
        if(message == Constants.TIMER_HAS_ENDED && !hasTimerMessageBeenSent){
            Log.d("AD_COUNTER_BAR---","sending message that timer has ended.");
            Intent intent = new Intent(Constants.TIMER_HAS_ENDED);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
            Variables.hasTimerStarted = false;
            hasTimerMessageBeenSent = true;
        }
    }

    private void addToSharedPreferencesViaBroadcast() {
        Log.d("ADVERT_CARD_SP","add To Shared Preferences Via Broadcast in Advert Card");
        Intent intent = new Intent(Constants.ADD_TO_SHARED_PREFERENCES);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

}
