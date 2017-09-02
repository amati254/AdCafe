package com.bry.adstudio.adapters;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.CountDownTimer;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bry.adstudio.Bookmarks;
import com.bry.adstudio.Constants;
import com.bry.adstudio.R;
import com.bry.adstudio.Variables;
import com.bry.adstudio.ui.MainActivity;
import com.mindorks.placeholderview.Animation;
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
//    private CountDownTimer mCountDownTimer;
    public static final String TIMER_HAS_ENDED = "TIMER_HAS_ENDED";

    public AdCounterBar(Context context, PlaceHolderView PlaceHolderView){
        mContext = context;
        mPlaceHolderView = PlaceHolderView;
    }
    @Resolve
    private void onResolved() {
        adCounter.setText(Integer.toString(Variables.adTotal));
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiver,new IntentFilter(Constants.ADVERT_CARD_BROADCAST));
        mCountDownTimer.start();
    }


    @LongClick(R.id.adCounter)
    private void onLongClick(){

    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("AD_COUNTER_BAR - ","Broadcast has been received.");
            String ExtraMessage = intent.getStringExtra(Constants.ADVERT_CARD_BROADCAST);

            if (ExtraMessage == Constants.AD_COUNTER_BROADCAST) {
                String adTotal = intent.getStringExtra(Constants.AD_TOTAL);
                adCounter.setText(adTotal);
            }else if(ExtraMessage == Constants.AD_TIMER_BROADCAST){
                mCountDownTimer.start();
            }else if(ExtraMessage == Constants.STOP_TIMER){
                mCountDownTimer.cancel();
            }

        }
    };


       private CountDownTimer mCountDownTimer = new CountDownTimer(7*1000,1) {
            @Override
            public void onTick(long millisUntilFinished) {
                long timeLeftInSeconds = millisUntilFinished/1000;
                progressBarTimer.setProgress((int)timeLeftInSeconds*7);
                textViewTime.setText(Integer.toString((int)timeLeftInSeconds));
            }

            @Override
            public void onFinish() {
                progressBarTimer.setProgress(7*1000);
                Log.d("Timer --- ","Timer has finnished");
                sendBroadcast(TIMER_HAS_ENDED);
                textViewTime.setText(Integer.toString(7));

            }
        };

    private void sendBroadcast(String message){
        if(message == TIMER_HAS_ENDED){
            Log.d("AD_COUNTER_BAR---","sending message that timer has ended.");
            Intent intent = new Intent(Constants.AD_COUNTER_BROADCAST);
            intent.putExtra(Constants.AD_COUNTER_BROADCAST,Constants.TIMER_HAS_ENDED);

            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        }
    }
}
