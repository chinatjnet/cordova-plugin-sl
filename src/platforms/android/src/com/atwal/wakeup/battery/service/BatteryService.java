package com.atwal.wakeup.battery.service;

import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.atwal.wakeup.battery.activity.BatteryActivity;
import com.atwal.wakeup.battery.util.SettingsHelper;


/**
 * Created by sks on 2016/9/18.
 */
public class BatteryService extends Service {
    private boolean isCharging;
    private boolean isFirstAutoLaunch;
    private static final String BATTERY_SERVICE_ACTION = "com.abclauncher.battery.Service";
    private final BroadcastReceiver launcherBatteryBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String getAction = intent.getAction();
            if (getAction.equals(Intent.ACTION_SCREEN_OFF)) {
                if (SettingsHelper.isOpenBatteryService(BatteryService.this)) {
                    BatteryActivity.start(BatteryService.this);
                }
            } else if (getAction.equals(Intent.ACTION_BATTERY_CHANGED)) {
                int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, 0);
                isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING
                        || status == BatteryManager.BATTERY_STATUS_FULL;
                isFirstAutoLaunch = SettingsHelper.getPreferenceBoolean(BatteryService.this, "AUTO_LAUNCH_KEY", true);
                if (isFirstAutoLaunch && BatteryService.this.isCharging
                        && SettingsHelper.isOpenBatteryAuto(BatteryService.this)) {
                    BatteryActivity.start(BatteryService.this);
                    SettingsHelper.setPreferenceBoolean(BatteryService.this, "AUTO_LAUNCH_KEY", false);
                }
                if (!isCharging) {
                    SettingsHelper.setPreferenceBoolean(BatteryService.this, "AUTO_LAUNCH_KEY", true);
                }
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
            intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
            //intentFilter.addAction(SimpleLockerActivity.SHOW_ZIPPER_ADS_DIALOG_ACTION);
            this.registerReceiver(this.launcherBatteryBroadcastReceiver, intentFilter);
        } catch (Exception e) {

        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    private static boolean isServiceWork(Class<?> serviceClass, Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static void startBatteryService(Context context) {
        Intent intent = new Intent();
        intent.setAction(BATTERY_SERVICE_ACTION);
        intent.setPackage(context.getPackageName());
        context.startService(intent);
    }

    public static void stopBatteryService(Context context) {
        try {
            Intent intent = new Intent();
            intent.setAction(BATTERY_SERVICE_ACTION);
            intent.setPackage(context.getPackageName());
            context.stopService(intent);
        } catch (Exception e) {

        }

    }
}
