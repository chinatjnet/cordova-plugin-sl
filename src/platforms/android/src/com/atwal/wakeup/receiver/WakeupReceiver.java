package com.atwal.wakeup.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.atwal.wakeup.service.WakeupService;

public class WakeupReceiver extends BroadcastReceiver {
    private static String TAG = "WakeupReceiver";

    public WakeupReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "action:" + intent.getAction());
        startService(context);
    }

    private void startService(Context context) {
        Intent serviceIntent = new Intent(context, WakeupService.class);
        context.startService(serviceIntent);
    }
}
