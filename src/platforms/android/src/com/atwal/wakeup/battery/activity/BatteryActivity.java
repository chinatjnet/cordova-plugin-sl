package com.atwal.wakeup.battery.activity;

import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
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
import com.atwal.wakeup.splash.Utils;
import com.atwal.wakeup.battery.fragment.BatterySettingFragment;
import com.atwal.wakeup.battery.receiver.LockerReceiver;
import com.atwal.wakeup.battery.receiver.LockerReceiverCallback;
import com.atwal.wakeup.battery.receiver.PhoneCallReceiver;
import com.atwal.wakeup.battery.util.BatteryUtils;
import com.atwal.wakeup.battery.util.SettingsHelper;
import com.atwal.wakeup.battery.util.Utilities;
import com.atwal.wakeup.battery.view.FlashTextView;
import com.atwal.wakeup.battery.view.SwipeBackActivity;
import com.atwal.wakeup.battery.view.SwipeBackLayout;
import com.thehotgame.bottleflip.R;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.NativeExpressAdView;
import com.google.android.gms.ads.VideoController;
import com.google.android.gms.ads.VideoOptions;

import android.widget.Toast;

import java.util.Date;


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
    private TextView mBatteryAppName;
    private static final String SHOW_ADS_KEY = "SHOW_ADS_KEY";
    private static long ONE_DAY = 1000 * 60 * 60 * 24;
    private static boolean isFlashLightOn = false;
    private TextView mToolbar;
    public static Camera cam = null;
    private static int PERMISSION_REQUEST_CODE = 1001;

    public static void start(Context context) {
        int screenShowDelayTime = 60 * 60 * 1000;
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
        initAdmob();
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

        mBatteryPgFlashlightTv.setText(String.format(getString(R.string.battery_cpu), Utils.getProcessCpuRate()));
    }

    private void initAdmob() {
        Log.d("screenLock", "try to load admob ad");
        if (mDragAdViewLayout == null) {
            mDragAdViewLayout = (FrameLayout) findViewById(R.id.battery_main_drag_layout);
        }
        mDragAdViewLayout.setVisibility(View.GONE);
        LinearLayout adWrap = (LinearLayout) findViewById(R.id.ad_wrap);
        final NativeExpressAdView adView = new NativeExpressAdView(BatteryActivity.this);
        adView.setAdUnitId("ca-app-pub-6491984961722312/1203696589");
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenWidth = dm.widthPixels;
        float density1 = dm.density;
        int screenWidthDp = (int) (screenWidth / density1 + 0.5f) - 32;
        AdSize adSize = new AdSize(screenWidthDp, 340);
        adView.setAdSize(adSize);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, (int) (20 * density1), 0, 0);
        adWrap.addView(adView, lp);

        adView.setVideoOptions(new VideoOptions.Builder().setStartMuted(true).build());
        final VideoController mVideoController = adView.getVideoController();
        mVideoController.setVideoLifecycleCallbacks(new VideoController.VideoLifecycleCallbacks() {
            @Override
            public void onVideoEnd() {
                Log.d("screenLock", "Video playback is finished.");
                super.onVideoEnd();
            }
        });

        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                if (mVideoController.hasVideoContent()) {
                    Log.d("screenLock", "Received an ad that contains a video asset.");
                } else {
                    Log.d("screenLock", "Received an ad that does not contain a video asset.");
                }
            }

            @Override
            public void onAdFailedToLoad(int i) {
            }
        });

        AdRequest request = new AdRequest.Builder().build();
        adView.loadAd(request);
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
        mBatteryPgFlashlightTv.setText(String.format(getString(R.string.battery_cpu), Utils.getProcessCpuRate()));
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
