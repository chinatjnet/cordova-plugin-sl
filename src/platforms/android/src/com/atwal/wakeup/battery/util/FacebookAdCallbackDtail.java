package com.atwal.wakeup.battery.util;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;

/**
 * Created by sks on 2016/10/8.
 */
public interface FacebookAdCallbackDtail extends FacebookAdCallback {
     void onNativeAdLoadError(AdError adError);
     void onNativeAdClick(Ad ad);
}
