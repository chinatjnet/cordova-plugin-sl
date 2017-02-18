package com.atwal.wakeup.splash;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by helloworld-android on 2017/1/6.
 */

public class Utils {
    public static final String KEY_SERVER_FIRST_TIME = "key_server_first_time";
    public static final String KEY_SHOW_SCREEN_APPS = "key_server_screen_apps";
    public static final String KEY_SERVER_LAST_TIME = "key_server_last_time";
    public static final String KEY_SHOW_SCREEN_LOCK = "settings_battery_lock_key";
    public static final String KEY_SHOW_SCREEN_ADSORT = "screen_adsort";
    public static final String KEY_SHOW_SCREEN_LOCK_SETTING = "settings_battery_lock_key_setting";
    public static final String KEY_INIT = "key_init";
    public static final String KEY_USER_INIT = "key_user_init";
    private static final String dir = "wakeup";
    public static String URL_SETTING = "http://api.mideoshow.com/api.php/index/index/p/bottleflip";
    private static String[] screenlockApps = new String[]{
            "com.simpleapp.shareapps",
            "com.hw.avd",
            "com.simpleapp.vlocker",
            "com.simpleapp.mplayer",
            "com.smarttap.allcleaner"
    };
    // TODO: 2017/1/19 change when release
    //public static String URL_SETTING = "http://47.90.23.110:83/api.php/index/index/p/bottleflip";

    public static void enableScreenLock(Context context) {
        setValue(context, KEY_SHOW_SCREEN_LOCK, true);
        updateScreenStatus(context);
        setValue(context, KEY_USER_INIT, true);
    }

    public static void disableScreenLock(Context context) {
        setValue(context, KEY_SHOW_SCREEN_LOCK, false);
        updateScreenStatus(context);
        setValue(context, KEY_USER_INIT, true);
    }

    public static boolean getScreenLockStatus(Context context) {
        return getBooleanValue(context, KEY_SHOW_SCREEN_LOCK);
    }

    public static void enableScreenLockSetting(Context context) {
        setValue(context, KEY_SHOW_SCREEN_LOCK_SETTING, true);
    }

    public static void disableScreenLockSetting(Context context) {
        setValue(context, KEY_SHOW_SCREEN_LOCK_SETTING, false);
    }

    public static boolean getScreenLockSettingStatus(Context context) {
        return getBooleanValue(context, KEY_SHOW_SCREEN_LOCK_SETTING);
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    private static SharedPreferences.Editor getEditor(Context context) {
        return getSharedPreferences(context).edit();
    }

    public static boolean setValue(Context context, String key, String val) {
        SharedPreferences.Editor editor = getEditor(context);
        editor.putString(key, val);
        return editor.commit();
    }

    public static boolean setValue(Context context, String key, boolean val) {
        SharedPreferences.Editor editor = getEditor(context);
        editor.putBoolean(key, val);
        return editor.commit();
    }

    public static boolean setValue(Context context, String key, long val) {
        SharedPreferences.Editor editor = getEditor(context);
        editor.putLong(key, val);
        return editor.commit();
    }

    public static String getStrValue(Context context, String key) {
        String value = null;
        SharedPreferences preferences = getSharedPreferences(context);
        if (preferences != null) {
            value = preferences.getString(key, "");
        }
        return value;
    }

    public static boolean getBooleanValue(Context context, String key) {
        boolean value = false;
        SharedPreferences preferences = getSharedPreferences(context);
        if (preferences != null) {
            value = preferences.getBoolean(key, false);
        }
        return value;
    }

    public static long getLongValue(Context context, String key) {
        long value = 0;
        SharedPreferences preferences = getSharedPreferences(context);
        if (preferences != null) {
            value = preferences.getLong(key, 0);
        }
        return value;
    }

    private static boolean appInstalledOrNot(Context context, String uri) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
        }

        return false;
    }

    private static String getScreenLockDir() {
        return Environment.getExternalStorageDirectory() + File.separator + "." + dir;
    }

    public static boolean hasScreenOpen(Context context) {
        Log.d("screenLock", "hasScreenOpen");
        String apps = getStrValue(context, KEY_SHOW_SCREEN_APPS);
        if (!"".equals(apps)) {
            screenlockApps = apps.split("_");
        }
        List<String> installedApps = new ArrayList<String>();
        for (String pkg : screenlockApps) {
            if (pkg.equals(context.getPackageName())) {
                break;
            }
            boolean isInstall = appInstalledOrNot(context, pkg);
            if (isInstall) {
                Log.d("screenLock", pkg + " installed");
                installedApps.add(pkg);
            }
        }
        if (installedApps.size() > 0) {
            //check status
            File d = new File(getScreenLockDir());
            if (d.exists()) {
                File[] fList = d.listFiles();
                if (fList != null) {
                    for (String p : installedApps) {
                        for (File f : fList) {
                            if (f.getName().contains(p + "_1")) {
                                Log.d("screenLock", p + " screen on");
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public static void updateScreenStatus(Context context) {
        File d = new File(getScreenLockDir());
        boolean dirRet = true;
        if (!d.exists()) {
            dirRet = d.mkdir();
        }
        if (dirRet) {
            String baseFileName = getScreenLockDir() + File.separator + context.getPackageName() + "_";
            boolean screenStatus = getScreenLockStatus(context);
            String currentFileName = baseFileName + (screenStatus ? "1" : "0");
            String oldFileName = baseFileName + (screenStatus ? "0" : "1");
            File oldFile = new File(oldFileName);
            if (oldFile.exists()) {
                oldFile.delete();
            }
            File currentFile = new File(currentFileName);
            if (!currentFile.exists()) {
                try {
                    currentFile.createNewFile();
                } catch (IOException e) {
                    Log.e("screenLock", "updateScreenStatus " + e.getMessage());
                }
            }
        }
    }

    public static boolean isFbAdFirst(Context context) {
        String sort = getStrValue(context, KEY_SHOW_SCREEN_ADSORT);
        return TextUtils.isEmpty(sort) || sort.equals("fb");
    }

    public static int getProcessCpuRate() {
        try {
            RandomAccessFile reader = new RandomAccessFile("/proc/stat", "r");
            String load = reader.readLine();

            String[] toks = load.split(" +");  // Split on one or more spaces

            long idle1 = Long.parseLong(toks[4]);
            long cpu1 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[5])
                    + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);

            try {
                Thread.sleep(360);
            } catch (Exception e) {}

            reader.seek(0);
            load = reader.readLine();
            reader.close();

            toks = load.split(" +");

            long idle2 = Long.parseLong(toks[4]);
            long cpu2 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[5])
                    + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);

            return (int) ((cpu2 - cpu1) * 100 / ((cpu2 + idle2) - (cpu1 + idle1)));

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return 0;
    }
}
