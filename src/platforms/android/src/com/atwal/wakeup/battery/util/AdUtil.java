package com.atwal.wakeup.battery.util;

import android.content.Context;
import android.util.Log;

import com.atwal.wakeup.battery.bean.FacebookNativeAdBean;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.AdSettings;
import com.facebook.ads.ImpressionListener;
import com.facebook.ads.InterstitialAdListener;
import com.facebook.ads.NativeAd;

/**
 * Created by yoyo on 2016/4/13.
 */
public class AdUtil implements InterstitialAdListener {
    private static final String TAG = "AdUtilyy";
    public static String FBAD_ID = "219387178532030_219390011865080";
    public static String ADMOB_ID = "ca-app-pub-6491984961722312/1203696589";
    private static AdUtil sFacebookAdUtil;

    public static AdUtil getInstance(){
        if (sFacebookAdUtil == null){
            sFacebookAdUtil = new AdUtil();
        }
        return sFacebookAdUtil;
    }

    public AdUtil(){}

    public static void loadNativeAd(final Context context, final String adPlacementId, final FacebookAdCallback callback){
        Log.d(TAG, "try to load ad");
        final NativeAd nativeAd = new NativeAd(context, adPlacementId);
        nativeAd.setAdListener(new AdListener() {
            @Override
            public void onError(Ad ad, AdError adError) {
                Log.d(TAG, "ad view onCropError:" + adError.getErrorMessage());
                if(callback instanceof FacebookAdCallbackDtail){
                    ((FacebookAdCallbackDtail) callback).onNativeAdLoadError(adError);
                }
            }

            @Override
            public void onAdLoaded(Ad ad) {
                Log.d(TAG, "ad view onAdLoaded");
                if (ad != nativeAd) {
                    return;
                }

                String titleForAd = nativeAd.getAdTitle();
                NativeAd.Image coverImage = nativeAd.getAdCoverImage();
                NativeAd.Image iconForAd = nativeAd.getAdIcon();
                String actionForAd = nativeAd.getAdCallToAction();
                String subTitle = nativeAd.getAdSubtitle();
                String textForAdBody = nativeAd.getAdBody();
                FacebookNativeAdBean nativeAdBean = new FacebookNativeAdBean();

                if (titleForAd == null || coverImage == null || iconForAd == null) {
                    return;
                }

                nativeAdBean.title = titleForAd;
                nativeAdBean.coverImgUrl = coverImage.getUrl();
                nativeAdBean.iconForAdUrl = iconForAd.getUrl();
                nativeAdBean.textForAdBody = textForAdBody;
                nativeAdBean.actionBtnText = actionForAd;
                nativeAdBean.nativeAd = nativeAd;
                nativeAdBean.subTitle = subTitle;
                callback.onNativeAdLoaded(nativeAdBean);
            }

            @Override
            public void onAdClicked(Ad ad) {
                Log.d(TAG, "ad view onAdClicked");
                if(callback instanceof FacebookAdCallbackDtail){
                    ((FacebookAdCallbackDtail) callback).onNativeAdClick(ad);
                    loadNativeAd(context, adPlacementId,callback);
                }

            }
        });
        nativeAd.setImpressionListener(new ImpressionListener() {
            @Override
            public void onLoggingImpression(Ad ad) {
                Log.d(TAG, "on ad impression");
            }
        });

        nativeAd.loadAd(NativeAd.MediaCacheFlag.ALL);
    }

    @Override
    public void onInterstitialDisplayed(Ad ad) {
        Log.d(TAG, "onInterstitialDisplayed: ");
    }

    @Override
    public void onInterstitialDismissed(Ad ad) {
        Log.d(TAG, "onInterstitialDismissed: ");
    }

    @Override
    public void onError(Ad ad, AdError adError) {
        Log.d(TAG, "onCropError: ");
    }

    @Override
    public void onAdLoaded(Ad ad) {
        Log.d(TAG, "onAdLoaded: ");
    }

    @Override
    public void onAdClicked(Ad ad) {
        Log.d(TAG, "onAdClicked: ");
    }
}
