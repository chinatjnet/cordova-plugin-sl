package com.atwal.wakeup.battery.bean;

import com.facebook.ads.NativeAd;

/**
 * Created by miao on 2016/4/13.
 */

public class FacebookNativeAdBean {
    public String title;
    public String coverImgUrl;
    public String iconForAdUrl;
    public String textForAdBody;
    public String actionBtnText;
    public NativeAd nativeAd;
    public String subTitle;

    @Override
    public String toString() {
        return "FacebookNativeAdBean:[title:" + title + " coverImgUrl:" + coverImgUrl + " iconForAdUrl:" + iconForAdUrl + " body:" + textForAdBody +"]";
    }

}
