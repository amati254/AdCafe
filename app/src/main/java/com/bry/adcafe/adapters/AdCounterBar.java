package com.bry.adcafe.adapters;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bry.adcafe.Constants;
import com.bry.adcafe.R;
import com.bry.adcafe.Variables;
import com.bry.adcafe.fragments.FeedbackFragment;
import com.mindorks.placeholderview.PlaceHolderView;
import com.mindorks.placeholderview.annotations.Click;
import com.mindorks.placeholderview.annotations.Layout;
import com.mindorks.placeholderview.annotations.LongClick;
import com.mindorks.placeholderview.annotations.Resolve;
import com.mindorks.placeholderview.annotations.View;

import butterknife.OnClick;

/**
 * Created by bryon on 26/08/2017.
 */

@Layout(R.layout.top_bar_view)
public class AdCounterBar {
    @View(R.id.adCounter)
    private TextView adCounter;
    @View(R.id.progressBarTimer)
    private ProgressBar progressBarTimer;
    @View(R.id.textViewTime)
    private TextView textViewTime;

    private Context mContext;
    private PlaceHolderView mPlaceHolderView;
    private boolean hasTimerMessageBeenSent;
    private boolean hasTimerStarted = false;
    private String mKey = "";

    private InitTask IT = null;



    public AdCounterBar(Context context, PlaceHolderView PlaceHolderView) {
        mContext = context;
        mPlaceHolderView = PlaceHolderView;
    }

    @Resolve
    private void onResolved() {
        adCounter.setText(Integer.toString(Variables.getAdTotal(mKey)));
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverToStartTimer, new IntentFilter(Constants.ADVERT_CARD_BROADCAST_TO_START_TIMER));
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverToUnregisterAllReceivers, new IntentFilter(Constants.UNREGISTER_ALL_RECEIVERS));
//        startTimer3();
    }

    private BroadcastReceiver mMessageReceiverToStartTimer = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("AD_COUNTER_BAR - ", "Broadcast has been received to start timer.");
//            startTimer();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startTimer2();
                }
            }, 40);
        }
    };


    private BroadcastReceiver mMessageReceiverToUnregisterAllReceivers = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("AD_COUNTER_BAR--", "Received broadcast to Unregister all receivers");
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverToStartTimer);
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverToUnregisterAllReceivers);
            if(hasTimerStarted) IT.cancel(true);

        }
    };

    @Click(R.id.textViewTime)
    private void onClick() {
        Log.d("AdCounterBar", "Clicked text view time.");
//        startTimer2();
    }


    private void startTimer() {
        if (!hasTimerStarted) {
            Variables.hasTimerStarted = true;
            hasTimerStarted = true;
            new CountDownTimer(7 * 1000, 400) {
                @Override
                public void onTick(long millisUntilFinished) {
                    hasTimerMessageBeenSent = false;
                    long timeLeftInSeconds = millisUntilFinished / 1000;
                    progressBarTimer.setProgress((int) timeLeftInSeconds * 7);
                    textViewTime.setText(Integer.toString((int) timeLeftInSeconds));
                }

                @Override
                public void onFinish() {
                    progressBarTimer.setProgress(7 * 1000);
                    Log.d("Timer --- ", "Timer has finnished");
                    sendBroadcast(Constants.TIMER_HAS_ENDED);
                    addToSharedPreferencesViaBroadcast();
                    hasTimerStarted = false;
                    adCounter.setText(Integer.toString(Variables.getAdTotal(mKey) + 1));
                    textViewTime.setText(Integer.toString(7));

                }
            }.start();
        }

    }

    private void startTimer2() {
        if (!hasTimerStarted) {
            hasTimerStarted = true;
            Log.d("AdCounterBar", "Starting timer from asynch task");
            hasTimerMessageBeenSent = false;
            if(IT!=null) IT = null;
            IT = new InitTask();
            IT.execute();
        }
    }

    private void startTimer3(){
        if (!hasTimerStarted) {
            Log.d("AdCounterBar", "Starting timer from ui thread version of asynch task");
            hasTimerMessageBeenSent = false;
            tryStartTimerFromUiThread();
        }
    }


    private void sendBroadcast(String message) {
        if (message.equals(Constants.TIMER_HAS_ENDED) && !hasTimerMessageBeenSent) {
            hasTimerMessageBeenSent = true;
            Log.d("AD_COUNTER_BAR---", "sending message that timer has ended.");
            Intent intent = new Intent(Constants.TIMER_HAS_ENDED);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
            Variables.hasTimerStarted = false;
        }
    }

    private void addToSharedPreferencesViaBroadcast() {
        Log.d("ADVERT_CARD_SP", "add To Shared Preferences Via Broadcast in Advert Card");
        Intent intent = new Intent(Constants.ADD_TO_SHARED_PREFERENCES);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    private void tryStartTimerFromUiThread(){
        Variables.hasTimerStarted = true;
        hasTimerStarted = true;
        startTimerFromUiThread();
    }




    private void startTimerFromUiThread() {
        int i = 7000;
        while (i > 0) {
            try {
                wait(100);
//                Thread.sleep(100);
                if(Variables.isAllClearToContinueCountDown){
                    i -= 100;
                    publishProgressUI(i);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        resetTimer();
        Log.d("Timer --- ", "Timer has finnished");
//        sendBroadcast(Constants.TIMER_HAS_ENDED);
//        addToSharedPreferencesViaBroadcast();
//        hasTimerStarted = false;
//        adCounter.setText(Integer.toString(Variables.getAdTotal(mKey)+1));
//        textViewTime.setText(Integer.toString(7));
    }

    private void publishProgressUI(int i) {
        progressBarTimer.incrementProgressBy(-1);
        if (i % 1000 == 0) textViewTime.setText(Integer.toString(i / 1000));
    }



    protected class InitTask extends AsyncTask<Context, Integer, String> {
        // -- gets called just before thread begins
        @Override
        protected void onPreExecute() {
            Log.d("AdCounterBar","Preparing to start timer");
            Variables.hasTimerStarted = true;
            hasTimerStarted = true;
            Variables.hasBeenPinned = false;
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Context... params) {
            int i = 7000;
            while (i > 0) {
                try {
                    Thread.sleep(50);
                    if(Variables.isAllClearToContinueCountDown){
                        i -= 50;
                        publishProgress(i);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return "COMPLETE!";
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            int value = values[0];
            progressBarTimer.incrementProgressBy(-1);

            if (value % 1000 == 0) textViewTime.setText(Integer.toString(value / 1000));

        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            resetTimer();
            Log.d("Timer --- ", "Timer has finnished");
//            sendBroadcast(Constants.TIMER_HAS_ENDED);
//            addToSharedPreferencesViaBroadcast();
//            hasTimerStarted = false;
//            adCounter.setText(Integer.toString(Variables.getAdTotal(mKey)+1));
//            textViewTime.setText(Integer.toString(7));
        }
    }

    private void resetTimer() {
        new InitTask2().execute();
    }

    protected class InitTask2 extends AsyncTask<Context, Integer, String> {
        // -- gets called just before thread begins
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Context... params) {
            int i = 0;
            while (i <= 140) {
                try {
                    Thread.sleep(50);
                    i += 20;
                    publishProgress(i);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return "COMPLETE!";
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressBarTimer.incrementProgressBy(20);

        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressBarTimer.setProgress(140);
            sendBroadcast(Constants.TIMER_HAS_ENDED);
            addToSharedPreferencesViaBroadcast();
            hasTimerStarted = false;
            adCounter.setText(Integer.toString(Variables.getAdTotal(mKey)+1));
            textViewTime.setText(Integer.toString(7));
        }

    }
}
