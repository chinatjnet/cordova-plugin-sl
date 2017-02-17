package com.atwal.wakeup.service;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;


import com.atwal.wakeup.battery.activity.BatteryActivity;
import com.atwal.wakeup.battery.util.SettingsHelper;
import com.atwal.wakeup.splash.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class WakeupService extends Service {
    private final static int GOHNSON_ID = 1000;
    private Timer mTimer;
    private static String TAG = "WakeupService";
    private MyHandler mHandler = new MyHandler(this);
    private static final int WAKEUP_MSG_SETTING = 1001;

    @Override
    public void onCreate() {
        super.onCreate();
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Log.i(TAG, "run task");
                //is user not set
                if (!Utils.getBooleanValue(getApplicationContext(), Utils.KEY_USER_INIT)) {
                    //get from server per hour
                    getServerSetting();
                }
            }
            // TODO: 2017/1/18 change 1 houre when releaase
        }, 0, 1 * 60 * 1000);

        IntentFilter mScreenOffFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        mScreenOffFilter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mScreenOffReceiver, mScreenOffFilter);

        if (!Utils.getBooleanValue(getApplicationContext(), Utils.KEY_INIT)) {
            Utils.setValue(getApplicationContext(), Utils.KEY_SHOW_SCREEN_LOCK, false);
            Utils.updateScreenStatus(getApplicationContext());
            Utils.setValue(getApplicationContext(), Utils.KEY_SHOW_SCREEN_LOCK_SETTING, true);
            Utils.setValue(getApplicationContext(), Utils.KEY_INIT, true);
        }
    }

    private void getServerSetting() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                URL url = null;
                HttpURLConnection urlConnection = null;
                try {
                    url = new URL(Utils.URL_SETTING);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    String result = getStringByBytes(getBytesByInputStream(in));
                    mHandler.obtainMessage(WAKEUP_MSG_SETTING, result).sendToTarget();
                } catch (IOException e) {
                    Log.e(TAG, "error:" + e.getMessage());
                    e.printStackTrace();
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }
            }
        }).start();
    }

    private String getStringByBytes(byte[] bytes) {
        String str = "";
        try {
            str = new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return str;
    }

    private byte[] getBytesByInputStream(InputStream is) {
        byte[] bytes = null;
        BufferedInputStream bis = new BufferedInputStream(is);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BufferedOutputStream bos = new BufferedOutputStream(baos);
        byte[] buffer = new byte[1024 * 8];
        int length = 0;
        try {
            while ((length = bis.read(buffer)) > 0) {
                bos.write(buffer, 0, length);
            }
            bos.flush();
            bytes = baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                bis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return bytes;
    }

    private static class MyHandler extends Handler {
        WeakReference<WakeupService> wr;

        public MyHandler(WakeupService activity) {
            this.wr = new WeakReference<WakeupService>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            WakeupService activity = wr.get();
            if (activity != null)
                activity.handleMessage(msg);
        }
    }

    private void handleMessage(Message msg) {
        switch (msg.what) {
            case WAKEUP_MSG_SETTING://
                String result = (String) msg.obj;
                if (!"".equals(result)) {
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        boolean screenLockShow = "1".equals(jsonObject.getString("screen"));
                        boolean settingShow = "1".equals(jsonObject.getString("setting"));
                        if (jsonObject.has("apps")) {
                            String apps = jsonObject.getString("apps");
                            if (!TextUtils.isEmpty(apps)) {
                                Utils.setValue(getApplicationContext(), Utils.KEY_SHOW_SCREEN_APPS, apps);
                            }
                        }
                        if (Utils.getLongValue(getApplicationContext(), Utils.KEY_SERVER_FIRST_TIME) == 0) {
                            Utils.updateScreenStatus(getApplicationContext());
                            if (Utils.getLongValue(getApplicationContext(), Utils.KEY_SERVER_LAST_TIME) > 0) {
                                Utils.setValue(getApplicationContext(), Utils.KEY_SERVER_FIRST_TIME, 1);
                            } else {
                                Utils.setValue(getApplicationContext(), Utils.KEY_SERVER_FIRST_TIME, new Date().getTime());
                            }
                        }
                        Utils.setValue(getApplicationContext(), Utils.KEY_SERVER_LAST_TIME, new Date().getTime());
                        Utils.setValue(getApplicationContext(), Utils.KEY_SHOW_SCREEN_LOCK_SETTING, settingShow);
                        // TODO: 2017/1/18 remove when release
                        Toast.makeText(getApplicationContext(), "screen:" + screenLockShow + ", setting:" + settingShow, Toast.LENGTH_LONG).show();
                        if (Utils.getScreenLockStatus(getApplicationContext()) && !screenLockShow) {
                            return;
                        }
                        Utils.setValue(getApplicationContext(), Utils.KEY_SHOW_SCREEN_LOCK, screenLockShow);
                        Utils.updateScreenStatus(getApplicationContext());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    private BroadcastReceiver mScreenOffReceiver = new BroadcastReceiver() {
        @SuppressWarnings("deprecation")
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                if (SettingsHelper.isOpenBatteryService(context)) {
                    BatteryActivity.start(context);
                }
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT < 18) {
            startForeground(GOHNSON_ID, new Notification());
        } else {
            Intent innerIntent = new Intent(this, WakeupInnerService.class);
            startService(innerIntent);
            startForeground(GOHNSON_ID, new Notification());
        }
        return START_REDELIVER_INTENT;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    class MyBinder extends Binder {
        public WakeupService getService() {
            return WakeupService.this;
        }
    }

    public static class WakeupInnerService extends Service {

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            startForeground(GOHNSON_ID, new Notification());
            stopForeground(true);
            stopSelf();
            return super.onStartCommand(intent, flags, startId);
        }

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }
    }
}
