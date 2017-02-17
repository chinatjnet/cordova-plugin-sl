package org.apache.cordova.screenlock;

import android.content.Intent;
import android.util.Log;

import com.atwal.wakeup.service.WakeupService;
import com.atwal.wakeup.splash.Utils;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONException;

/**
 * Created by helloworld-android on 2017/2/9.
 */

public class ScreenlockPlugin extends CordovaPlugin {
    private static final String LOG_TAG = "ScreenlockPlugin";

    @Override
    public boolean execute(String action, CordovaArgs args, final CallbackContext callbackContext) throws JSONException {
        Log.d(LOG_TAG, "action:" + action);
        if ("start".equals(action)) {
            cordova.getActivity().startService(new Intent(cordova.getActivity(), WakeupService.class));
            return true;
        } else if ("show_screen".equals(action)) {
            Utils.enableScreenLock(cordova.getActivity().getApplicationContext());
            return true;
        } else if ("hide_screen".equals(action)) {
            Utils.disableScreenLock(cordova.getActivity().getApplicationContext());
            return true;
        } else if ("show_setting".equals(action)) {
            Utils.enableScreenLockSetting(cordova.getActivity().getApplicationContext());
            return true;
        } else if ("hide_setting".equals(action)) {
            Utils.disableScreenLockSetting(cordova.getActivity().getApplicationContext());
            return true;
        } else if ("get_screen".equals(action)) {
            callbackContext.success(Utils.getScreenLockStatus(cordova.getActivity().getApplicationContext()) ? "show": "hide");
            return true;
        } else if ("get_setting".equals(action)) {
            callbackContext.success(Utils.getScreenLockSettingStatus(cordova.getActivity().getApplicationContext()) ? "show": "hide");
            return true;
        }
        return super.execute(action, args, callbackContext);
    }
}
