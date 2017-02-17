package com.atwal.wakeup.battery.fragment;


import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.atwal.wakeup.battery.activity.BatteryActivity;
import com.atwal.wakeup.battery.util.SettingsHelper;
import com.atwal.wakeup.battery.view.FixedSwitchPreference;
import com.atwal.wakeup.splash.Utils;
import com.thehotgame.bottleflip.R;


/**
 * Created by passion on 16/9/26.
 */

public class BatterySettingFragment extends PreferenceFragment implements View.OnClickListener, SharedPreferences.OnSharedPreferenceChangeListener {

    private FixedSwitchPreference mPrefBatterySetting;

    //add by wl
    public static void add(FragmentManager fragmentManager, int containerViewId){
        BatterySettingFragment fragment = new BatterySettingFragment();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(containerViewId, fragment, "lock setting");
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceManager manager = getPreferenceManager();
        addPreferencesFromResource(R.xml.battery_setting_preferences);

        mPrefBatterySetting = (FixedSwitchPreference) manager.findPreference(SettingsHelper.BATTERY_LOCK_KEY);
        mPrefBatterySetting.setChecked(SettingsHelper.isOpenBatteryService(getActivity()));
        setEnablePullToBack(false);

        SettingsHelper.getSettingsSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.battery_layout_preference, container, false);
        ImageButton back = (ImageButton) view.findViewById(R.id.title_back);
        back.setOnClickListener(this);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void updateScreenStatus() {
        Utils.setValue(getActivity().getApplicationContext(), Utils.KEY_SHOW_SCREEN_LOCK, mPrefBatterySetting.isChecked());
        Utils.updateScreenStatus(getActivity().getApplicationContext());
    }

    @Override
    public void onDestroy() {
        updateScreenStatus();
        super.onDestroy();
    }

    private void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            updateScreenStatus();
            getFragmentManager().popBackStack();
            setEnablePullToBack(true);
        }
    }

    private void setEnablePullToBack(boolean isEnable){
        if(getActivity() instanceof BatteryActivity){
            ((BatteryActivity)getActivity()).setEnablePullToBack(isEnable);
        }
    }

    @Override
    public void onClick(View v) {

        onBackPressed();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(SettingsHelper.BATTERY_LOCK_KEY)){
            if(mPrefBatterySetting != null && getActivity() != null) {
                boolean isOpen = SettingsHelper.isOpenBatteryService(getActivity());
                mPrefBatterySetting.setChecked(SettingsHelper.isOpenBatteryService(getActivity()));
            }
        }
    }
}
