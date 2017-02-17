package com.atwal.wakeup.battery.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.atwal.wakeup.battery.util.BatteryUtils;


/**
 * Created by sks on 2016/10/28.
 */

public class LockerReceiver {
    private static final String SYSTEM_DIALOG_REASON_KEY = "reason";
    private static final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
    private BroadcastReceiver lockerReceiver;

    public LockerReceiver(Context context, final LockerReceiverCallback lockerRefreshCallback) {
        lockerReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String getAction = intent.getAction();
                if (getAction.equals(Intent.ACTION_TIMEZONE_CHANGED) || getAction.equals(Intent.ACTION_TIME_TICK)) {
                    if (lockerRefreshCallback != null) {
                        lockerRefreshCallback.receiveTime(BatteryUtils.getNowTimeStr(context));
                        lockerRefreshCallback.receiveDate(BatteryUtils.getDateStr(context));
                    }
                } else if (getAction.equals(Intent.ACTION_BATTERY_CHANGED)) {
                    if (lockerRefreshCallback != null) {
                        lockerRefreshCallback.receiveBatteryData(intent);
                    }
                } else if (getAction.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                    if (lockerRefreshCallback != null) {
                        String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
                        if (SYSTEM_DIALOG_REASON_HOME_KEY.equals(reason)) {
                            lockerRefreshCallback.receiveHomeClick();
                        }
                    }
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        intentFilter.addAction(Intent.ACTION_TIME_TICK);
        intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        intentFilter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        if (lockerRefreshCallback != null) {
            lockerRefreshCallback.receiveTime(BatteryUtils.getNowTimeStr(context));
            lockerRefreshCallback.receiveDate(BatteryUtils.getDateStr(context));
        }
        context.registerReceiver(lockerReceiver, intentFilter);
    }

    public void unregisterLockerReceiver(Context context) {
        if (lockerReceiver != null) {
            context.unregisterReceiver(lockerReceiver);
        }
    }
}
