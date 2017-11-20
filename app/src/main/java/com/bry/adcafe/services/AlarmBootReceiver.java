package com.bry.adcafe.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by bryon on 15/11/2017.
 */

public class AlarmBootReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationHelper.scheduleRepeatingElapsedNotification(context);

    }
}
