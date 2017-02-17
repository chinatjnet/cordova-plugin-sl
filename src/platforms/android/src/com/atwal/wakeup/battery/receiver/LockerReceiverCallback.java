package com.atwal.wakeup.battery.receiver;

import android.content.Intent;

/**
 * Created by sks on 2016/10/28.
 */

public interface LockerReceiverCallback {
    void receiveTime(String time);
    void receiveDate(String data);
    void receiveBatteryData(Intent intent);
    void receiveHomeClick();
}
