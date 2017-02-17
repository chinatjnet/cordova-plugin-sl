package com.atwal.wakeup.battery.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;


/**
 * Created by sks on 2016/9/18.
 */
public class SettingsHelper {
    private static final String SHAREPREFERENCE_NAME = "com.abclauncher.battery.locker.prefs";
    public static final String BATTERY_LOCK_KEY = "settings_battery_lock_key";
    public static final String SIMPLE_LOCKER_KEY = "simple_locker_open_key";
    public static final String SETTINGS_WALLPAPER_IS_SCROLL_KEY = "SETTINGS_WALLPAPER_IS_SCROLL_KEY";
    private static final String BATTERY_SETTING_KEY = "settings_battery_lock_key_setting";

    public SettingsHelper() {
    }

    public static SharedPreferences getSettingsSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static SharedPreferences.Editor getSettingsEditor(Context context) {
        SharedPreferences sp = getSettingsSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        return editor;
    }

    public static String getPreferenceString(Context context, String name, String def) {
        SharedPreferences prefs = getSettingsSharedPreferences(context);
        return prefs.getString(name, def);
    }

    public static void setPreferenceString(Context context, String name, String value) {
        if(context != null) {
            SharedPreferences.Editor editPrefs = getSettingsEditor(context);
            editPrefs.putString(name, value);
            editPrefs.commit();
        }
    }

    public static int getPreferenceInt(Context context, String name, int def) {
        SharedPreferences prefs = getSettingsSharedPreferences(context);
        return prefs.getInt(name, def);
    }

    public static void setPreferenceInt(Context context, String name, int value) {
        SharedPreferences.Editor editPrefs = getSettingsEditor(context);
        editPrefs.putInt(name, value);
        editPrefs.commit();
    }

    public static long getPreferenceLong(Context context, String name, long def) {
        SharedPreferences prefs = getSettingsSharedPreferences(context);
        return prefs.getLong(name, def);
    }

    public static void setPreferenceLong(Context context, String name, long value) {
        SharedPreferences.Editor editPrefs = getSettingsEditor(context);
        editPrefs.putLong(name, value);
        editPrefs.commit();
    }

    public static Float getPreferenceFloat(Context context, String name, Float def) {
        SharedPreferences prefs = getSettingsSharedPreferences(context);
        return Float.valueOf(prefs.getFloat(name, def.floatValue()));
    }

    public static void setPreferenceFloat(Context context, String name, Float value) {
        SharedPreferences.Editor editPrefs = getSettingsEditor(context);
        editPrefs.putFloat(name, value.floatValue());
        editPrefs.commit();
    }

    public static boolean getPreferenceBoolean(Context context, String name, boolean def) {
        SharedPreferences prefs = getSettingsSharedPreferences(context);
        return prefs.getBoolean(name, def);
    }

    public static void setPreferenceBoolean(Context context, String name, boolean value) {
        SharedPreferences.Editor editPrefs = getSettingsEditor(context);
        editPrefs.putBoolean(name, value);
        editPrefs.commit();
    }

    public static boolean isOpenBatteryAuto(Context context) {
        return getPreferenceBoolean(context, BATTERY_LOCK_KEY, true);
    }

    public static boolean isOpenBatteryService(Context context) {
        return getPreferenceBoolean(context, BATTERY_LOCK_KEY, false);
    }
    public static boolean isOpenSimpleLocker(Context context) {
        return getPreferenceBoolean(context, SIMPLE_LOCKER_KEY, false);
    }

    public static boolean isShowSetting(Context context) {
        return getPreferenceBoolean(context, BATTERY_SETTING_KEY, true);
    }
}
