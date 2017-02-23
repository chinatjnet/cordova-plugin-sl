package org.apache.cordova.screenlock;

import android.content.Intent;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.atwal.wakeup.service.WakeupService;
import com.atwal.wakeup.splash.Utils;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
import com.thehotgame.bottleflip.BuildConfig;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONException;


/**
 * Created by helloworld-android on 2017/2/9.
 */

public class ScreenlockPlugin extends CordovaPlugin {
    private static final String LOG_TAG = "ScreenlockPlugin";
    private AdView adView;

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
        } else if ("fb_banner".equals(action)) {
            String id = args.getString(0);
            if (BuildConfig.DEBUG) {
                Toast.makeText(cordova.getActivity().getApplicationContext(), "id:" + id, Toast.LENGTH_LONG).show();
            }

            adView = new AdView(cordova.getActivity().getApplicationContext(), id, AdSize.BANNER_HEIGHT_50);
//            final FrameLayout layout = (FrameLayout) webView.getView().getParent();
//            cordova.getActivity().runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    FrameLayout.LayoutParams lpBottom = new FrameLayout.LayoutParams(
//                            FrameLayout.LayoutParams.MATCH_PARENT,
//                            FrameLayout.LayoutParams.WRAP_CONTENT);
//                    lpBottom.gravity =  Gravity.BOTTOM;
//                    layout.addView(adView, lpBottom);
//                }
//            });

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            try {
                Toast.makeText(cordova.getActivity().getApplicationContext(), "try add view", Toast.LENGTH_LONG).show();
                ((ViewGroup) (((View) webView.getClass().getMethod("getView").invoke(webView)).getParent())).addView(adView, params);
            } catch (Exception e) {
                Toast.makeText(cordova.getActivity().getApplicationContext(), "catch add view", Toast.LENGTH_LONG).show();
                ((ViewGroup) webView).addView(adView, params);
            }

            adView.setAdListener(new AdListener() {
                @Override
                public void onError(Ad ad, AdError adError) {
                    if (BuildConfig.DEBUG) {
                        Toast.makeText(cordova.getActivity().getApplicationContext(), "error:" + adError.getErrorCode(), Toast.LENGTH_LONG).show();
                    }
                    Log.d("screenLock", "error:" + adError.getErrorMessage());
                    callbackContext.error(adError.getErrorCode());
                }

                @Override
                public void onAdLoaded(Ad ad) {
                    if (BuildConfig.DEBUG) {
                        Toast.makeText(cordova.getActivity().getApplicationContext(), "success", Toast.LENGTH_LONG).show();
                    }
                    Log.d("screenLock", "success");
                    callbackContext.success("success");
                }

                @Override
                public void onAdClicked(Ad ad) {

                }
            });
            adView.loadAd();
            callbackContext.error("error");
            return true;
        }
        return super.execute(action, args, callbackContext);
    }
}
