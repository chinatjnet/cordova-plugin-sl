package com.atwal.wakeup.battery.activity;

import android.Manifest;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.hardware.Camera;
import android.os.BatteryManager;
import android.os.Build;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.atwal.wakeup.splash.PermissionsActivity;
import com.atwal.wakeup.splash.PermissionsChecker;
import com.atwal.wakeup.splash.Utils;
import com.bumptech.glide.Glide;
import com.atwal.wakeup.battery.bean.FacebookNativeAdBean;
import com.atwal.wakeup.battery.fragment.BatterySettingFragment;
import com.atwal.wakeup.battery.receiver.LockerReceiver;
import com.atwal.wakeup.battery.receiver.LockerReceiverCallback;
import com.atwal.wakeup.battery.receiver.PhoneCallReceiver;
import com.atwal.wakeup.battery.util.AdUtil;
import com.atwal.wakeup.battery.util.BatteryUtils;
import com.atwal.wakeup.battery.util.FacebookAdCallbackDtail;
import com.atwal.wakeup.battery.util.SettingsHelper;
import com.atwal.wakeup.battery.util.Utilities;
import com.atwal.wakeup.battery.view.FlashTextView;
import com.atwal.wakeup.battery.view.SwipeBackActivity;
import com.atwal.wakeup.battery.view.SwipeBackLayout;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.NativeAd;
import com.thehotgame.bottleflip.R;
import android.net.Uri;
import android.widget.Toast;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by sks on 2016/9/26.
 */
public class BatteryActivity extends SwipeBackActivity implements LockerReceiverCallback {
    private TextView mTimeTv;
    private TextView mDateTv;
    private TextView mChargingTimeTv;
    private TextView mBatteryPgSpeedTv;
    private TextView mBatteryPgStorageTv;
    private TextView mBatteryPgFlashlightTv;
    private FlashTextView mUnlockTv;
    private ImageView mChargingBlurIv;
    private RelativeLayout mAdMainRl;
    private ImageView mAdCoverImg;
    private ImageView mAdIconImg;
    private TextView mAdTitle;
    private TextView mAdSubTitle;
    private Button mAdBtn;
    private FrameLayout mDragAdViewLayout = null;
    private LockerReceiver mLockerReceiver;
    private Resources mResources;
    private NativeAd nativeAd;
    private TextView mBatteryAppName;
    private static final String SHOW_ADS_KEY = "SHOW_ADS_KEY";
    private static long ONE_DAY = 1000 * 60 * 60 * 24;
    private static boolean isFlashLightOn = false;
    private TextView mToolbar;
    public static Camera cam = null;
    private static int PERMISSION_REQUEST_CODE = 1001;
    private static boolean bShowAdmob = false;
    private static boolean bFbAdFirst = false;

    public static void start(Context context) {
        int screenShowDelayTime = 3 * 60 * 60 * 1000;
        Log.d("screenLock", "now:" + new Date().getTime() +
                " first:" + Utils.getLongValue(context, Utils.KEY_SERVER_FIRST_TIME) +
                " div:" + (new Date().getTime() - Utils.getLongValue(context, Utils.KEY_SERVER_FIRST_TIME)) +
                " start:" + (new Date().getTime() - Utils.getLongValue(context, Utils.KEY_SERVER_FIRST_TIME) > screenShowDelayTime));
        //screen lock only show after install app 3 hours
        if (Utils.getLongValue(context, Utils.KEY_SERVER_FIRST_TIME) > 0 &&
                new Date().getTime() - Utils.getLongValue(context, Utils.KEY_SERVER_FIRST_TIME) > screenShowDelayTime) {
            if (SettingsHelper.isOpenBatteryService(context) && !PhoneCallReceiver.mIsUnlockedForCalling) {
                //if no other screen lock show
                if (Utils.hasScreenOpen(context)) {
                    return;
                }
                Intent batteryIntent = new Intent(context, BatteryActivity.class);
                batteryIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(batteryIntent);
            }
        }
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_battery_main;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PERMISSION_REQUEST_CODE && resultCode == PermissionsActivity.PERMISSIONS_DENIED) {
            //no permission
        }
    }

    @Override
    public void initViews() {
        initWindow();
        getSwipeBackLayout().setEnablePullToBack(true);
        setDragEdge(SwipeBackLayout.DragEdge.LEFT);
        mToolbar = (TextView) findViewById(R.id.battery_toolbar);
        mBatteryAppName = (TextView) findViewById(R.id.battery_appname);

        if (!SettingsHelper.isShowSetting(getApplicationContext())) {
            mToolbar.setVisibility(View.INVISIBLE);
            mBatteryAppName.setVisibility(View.INVISIBLE);
        }

        mResources = getResources();
        mTimeTv = (TextView) findViewById(R.id.battery_time);
        mDateTv = (TextView) findViewById(R.id.battery_date);
        mBatteryPgSpeedTv = (TextView) findViewById(R.id.battery_progress_speed);
        mBatteryPgStorageTv = (TextView) findViewById(R.id.battery_progress_storage);
        mBatteryPgFlashlightTv = (TextView) findViewById(R.id.battery_progress_flashlight);
        mChargingTimeTv = (TextView) findViewById(R.id.battery_charging_time);
        mChargingBlurIv = (ImageView) findViewById(R.id.battery_main_layout_bg);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && Utilities.hasNavBar(this)) {
            mUnlockTv = (FlashTextView) findViewById(R.id.unlock_tips_text);
            int marginTop = (int) getResources().getDimension(R.dimen.settings_item_margin);
            Utilities.setMargins(mUnlockTv, 0, marginTop, 0, Utilities.getNavigationBarHeight(this) + marginTop);
        }
        WallpaperManager mWallpaperManager = WallpaperManager.getInstance(getApplication());
        mChargingBlurIv.setImageBitmap(BatteryUtils.getBlurBg(mWallpaperManager));
        mBatteryAppName.setText(R.string.app_name);
        refreshTime();
        bFbAdFirst = Utils.isFbAdFirst(getApplicationContext());
        if (bFbAdFirst) {
            initAd();
        } else {
            initAd();
        }
        registerBatteryReceiver();
        initEvent();
    }

    private void initEvent() {
        mBatteryPgFlashlightTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TrackerUtil.pointEvent(getApplicationContext(), "screenlock", "click", "flashlight");
            }
        });

        mBatteryPgSpeedTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TrackerUtil.pointEvent(getApplicationContext(), "screenlock", "click", "battery");
            }
        });

        mBatteryPgStorageTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TrackerUtil.pointEvent(getApplicationContext(), "screenlock", "click", "storage");
            }
        });

        mToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TrackerUtil.pointEvent(getApplicationContext(), "screenlock", "click", "screen_setting");
                BatterySettingFragment.add(getFragmentManager(), R.id.battery_charging_content);
            }
        });

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Log.i("screenLock", "run task");
                BatteryActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mBatteryPgFlashlightTv.setText(String.format(getString(R.string.battery_cpu), Utils.getProcessCpuRate()));
                    }
                });
            }
        }, 0, 2 * 1000);
    }

    private void initAd() {
        Log.d("screenLock", "try to load fb ad");
        bShowAdmob = false;
        mAdMainRl = (RelativeLayout) findViewById(R.id.battery_ad_main_rl);
        mAdCoverImg = (ImageView) findViewById(R.id.battery_ad_iv);
        mAdIconImg = (ImageView) findViewById(R.id.battery_ad_icon);
        mAdTitle = (TextView) findViewById(R.id.battery_ad_title);
        mAdSubTitle = (TextView) findViewById(R.id.tv_ad_subtitle);
        mAdBtn = (Button) findViewById(R.id.btn_ad);
        if (mDragAdViewLayout == null) {
            mDragAdViewLayout = (FrameLayout) findViewById(R.id.battery_main_drag_layout);
        }
        mDragAdViewLayout.setVisibility(View.VISIBLE);
        String placementId = BatteryUtils.getBatteryFacebookPlacementId(this);
        if (placementId.equals("FBADID")) {
            Toast.makeText(this, "FB ad id error", Toast.LENGTH_LONG).show();
            return;
        }
        AdUtil.loadNativeAd(this, placementId, new FacebookAdCallbackDtail() {
            @Override
            public void onNativeAdLoaded(FacebookNativeAdBean facebookNativeAdBean) {
                try {
                    ViewGroup.LayoutParams params = mAdCoverImg.getLayoutParams();
                    int width = Utilities.getScreenWidth(getApplicationContext()) - Utilities.dip2px(getApplicationContext(), 48);
                    params.height = (int) (width * 9f / 16);
                    mAdCoverImg.setLayoutParams(params);
                    String textForAdTitle = facebookNativeAdBean.title;
                    String coverImgUrl = facebookNativeAdBean.coverImgUrl;
                    String iconForAdUrl = facebookNativeAdBean.iconForAdUrl;
                    String actionBtnText = facebookNativeAdBean.actionBtnText;
                    String subText = facebookNativeAdBean.subTitle;
                    nativeAd = facebookNativeAdBean.nativeAd;
                    mAdTitle.setText(textForAdTitle);
                    mAdSubTitle.setText(subText);
                    mAdBtn.setText(actionBtnText);
                    Glide.with(getApplicationContext()).load(coverImgUrl).into(mAdCoverImg);
                    Glide.with(getApplicationContext()).load(iconForAdUrl).into(mAdIconImg);
                    facebookNativeAdBean.nativeAd.registerViewForInteraction(mDragAdViewLayout);
                    showAdsAnim();
                } catch (Exception e) {
                    if (bFbAdFirst) {
                    }
                }
            }

            @Override
            public void onNativeAdLoadError(AdError adError) {
                Log.d("screenLock", "fb ad error:" + adError.getErrorMessage());
                if (bFbAdFirst) {
                }
            }

            @Override
            public void onNativeAdClick(Ad ad) {
                ad.destroy();
                removeAdsAnim(false);
            }
        });
    }

    private boolean isShowAd() {
        long current = System.currentTimeMillis();
        boolean isShowTime = (current - SettingsHelper.getPreferenceLong(getApplicationContext(), SHOW_ADS_KEY, 0)) > ONE_DAY;
        //return isShowTime;
        return true;
    }

    public void setEnablePullToBack(boolean isEnable) {
        if (getSwipeBackLayout() == null) {
            return;
        }
        getSwipeBackLayout().setEnablePullToBack(isEnable);
    }

    private void initWindow() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.transparent));
            getWindow().setNavigationBarColor(getResources().getColor(R.color.transparent));
        }
    }

    private void refreshTime() {
        mTimeTv.setText(BatteryUtils.getNowTimeStr(this));
        mDateTv.setText(BatteryUtils.getDateStr(this));
    }

    private void registerBatteryReceiver() {
        mLockerReceiver = new LockerReceiver(this, this);
    }

    private SpannableStringBuilder getTimeBuilder(String str) {
        SpannableStringBuilder builder = new SpannableStringBuilder(str);
        ForegroundColorSpan whiteSpan = new ForegroundColorSpan(Color.WHITE);
        ForegroundColorSpan blueSpan = new ForegroundColorSpan(mResources.getColor(R.color.settings_set_def_text_color));
        String startText = mResources.getString(R.string.battery_full_time_start_text);
        builder.setSpan(whiteSpan, 0, startText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.setSpan(blueSpan, startText.length(), str.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        return builder;
    }

    private void refreshBatteryData(Intent intent) {
        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 0);
        int percent = BatteryUtils.getBatteryPercent(level, scale);
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, 0);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING
                || status == BatteryManager.BATTERY_STATUS_FULL;
        getDyedDrawableToProgressTextView(percent);
        if (isCharging) {
            mChargingTimeTv.setVisibility(View.VISIBLE);
            String chargingTime = BatteryUtils.getBatteryFullTimeStr(getApplicationContext(), intent, percent);
            if (TextUtils.isEmpty(chargingTime)) {
                mChargingTimeTv.setVisibility(View.INVISIBLE);
            } else {
                if (mResources.getString(R.string.battery_is_full).equals(chargingTime)) {
                    mChargingTimeTv.setText(chargingTime);
                } else {
                    mChargingTimeTv.setText(getTimeBuilder(chargingTime));
                }
            }
        } else {
            mChargingTimeTv.setVisibility(View.INVISIBLE);
        }
    }


    private void showAdsAnim() {
        if (!bShowAdmob) {
            mAdMainRl.setVisibility(View.VISIBLE);
            mDragAdViewLayout.setBackgroundResource(R.drawable.bg_ad_screen);
            findViewById(R.id.iv_ad_txt).setVisibility(View.VISIBLE);
        }
//        Animator animator3 = AnimatorInflater.loadAnimator(this, R.animator.battery_pb_layout_trans_y);
//        animator3.setTarget(mChargingStateMainLl);
//        AnimatorSet animatorSet = new AnimatorSet();
//        animatorSet.playTogether(animator3);
//        animatorSet.start();
//        animatorSet.addListener(new AnimatorListenerAdapter() {
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                super.onAnimationEnd(animation);
//                mDragAdViewLayout.setVisibility(View.VISIBLE);
//            }
//        });
    }

    private void removeAdsAnim(final boolean isRemove) {
        if (!bShowAdmob) {
            mDragAdViewLayout.setBackgroundResource(android.R.color.transparent);
            mDragAdViewLayout.setVisibility(View.GONE);
        }
//        Animator animator3 = AnimatorInflater.loadAnimator(this, R.animator.battery_pb_layout_trans_y_reset);
//        animator3.setTarget(mChargingStateMainLl);
//
//        AnimatorSet animatorSet = new AnimatorSet();
//        animatorSet.playTogether(animator3);
//        animatorSet.start();
//        animatorSet.addListener(new AnimatorListenerAdapter() {
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                super.onAnimationEnd(animation);
//                if (isRemove) {
//                    SettingsHelper.setPreferenceLong(getApplicationContext(), SHOW_ADS_KEY, System.currentTimeMillis());
//                } else {
//                    mDragAdViewLayout.moveToOriginal();
//                }
//            }
//        });
    }

    private void getDyedDrawableToProgressTextView(int percent) {
        if (percent == 100) {
            mBatteryPgSpeedTv.setBackgroundResource(R.drawable.icon_screen_battery_full);
            mBatteryPgSpeedTv.setText("");
        } else {
            mBatteryPgSpeedTv.setBackgroundResource(R.drawable.icon_screen_battery);
            mBatteryPgSpeedTv.setText(percent + "%");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isFlashLightOn) {
            if (cam != null) {
                try {
                    cam.stopPreview();
                    cam.release();
                } catch (Exception e) {
                    Log.e("onDestroy", "error:" + e.getMessage());
                }
            }
            isFlashLightOn = false;
            mBatteryPgFlashlightTv.setBackgroundResource(R.drawable.icon_screen_flashlight);
        }
        mLockerReceiver.unregisterLockerReceiver(this);
        if (nativeAd != null) {
            nativeAd.destroy();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        popBatterySettingsFragment();
    }

    private void popBatterySettingsFragment() {
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
            setEnablePullToBack(true);
        }
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
            setEnablePullToBack(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //TrackerUtil.pointScreen(this, "ScreenLock");
    }

    @Override
    public void receiveTime(String time) {
        mTimeTv.setText(time);
        mBatteryPgStorageTv.setText(String.format(getString(R.string.battery_storage), Utilities.getStorageUsePercent()));
    }

    @Override
    public void receiveDate(String data) {
        mDateTv.setText(data);
    }

    @Override
    public void receiveBatteryData(Intent intent) {
        refreshBatteryData(intent);
    }

    @Override
    public void receiveHomeClick() {
        finish();
    }
}
