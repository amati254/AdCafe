package com.bry.adstudio.adapters;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.animation.RotateAnimation;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bry.adstudio.Constants;
import com.bry.adstudio.R;
import com.bry.adstudio.Variables;
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
    @View(R.id.adCounter)
    private TextView adCounter;

    private Context mContext;
    private PlaceHolderView mPlaceHolderView;

    public AdCounterBar(Context context, PlaceHolderView PlaceHolderView){
        mContext = context;
        mPlaceHolderView = PlaceHolderView;
    }
    @Resolve
    private void onResolved() {
        adCounter.setText(Integer.toString(0));
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiver,new IntentFilter(Constants.AD_COUNTER_BROADCAST));

    }

    @LongClick(R.id.adCounter)
    private void onLongClick(){

    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
           String message = "Broadcast has been received.";
            String adTotal = intent.getStringExtra(Constants.AD_TOTAL);
            adCounter.setText(adTotal);
            Log.d("AD_COUNTER_BAR - ",message);

        }
    };
}
