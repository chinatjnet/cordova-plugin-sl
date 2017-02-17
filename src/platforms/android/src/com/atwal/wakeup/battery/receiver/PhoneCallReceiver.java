package com.atwal.wakeup.battery.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PhoneCallReceiver extends BroadcastReceiver {

    public static boolean mIsUnlockedForCalling = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        // 如果正在锁屏，则为拨打电话，启动TopAcivity监控
        String state = intent.getStringExtra("state");
        if(!mIsUnlockedForCalling && "RINGING".equals(state)) {
            // 来电话，如果在PHONE层可以不处理
                mIsUnlockedForCalling = true;
        } else if(!mIsUnlockedForCalling && "OFFHOOK".equals(state)) {
                mIsUnlockedForCalling = true;
        } else if(mIsUnlockedForCalling && "IDLE".equals(state)) {
            mIsUnlockedForCalling = false;
        }
    }

}