package com.atwal.wakeup.battery.view;

/**
 * Created by wang li on 16/6/12.
 *
 * 自定义SwitchPreference 解决多个SwitchPreference 事件联结冲突
 */

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Build;
import android.preference.TwoStatePreference;
import android.support.annotation.StringRes;
import android.support.v7.widget.SwitchCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.Checkable;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.thehotgame.bottleflip.R;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class FixedSwitchPreference extends TwoStatePreference {

    private static Method sSyncSummaryViewMethod;

    static {
        try {
            sSyncSummaryViewMethod = TwoStatePreference.class.getDeclaredMethod("syncSummaryView", View.class);
            sSyncSummaryViewMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            sSyncSummaryViewMethod = null;
        }
    }

    // Switch text for on and off states
    private CharSequence mSwitchOn;
    private CharSequence mSwitchOff;

    private final Listener mListener = new Listener();

    private class Listener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (!callChangeListener(isChecked)) {
                // Listener didn't like it, change it back.
                // CompoundButton will make sure we don't recurse.
                buttonView.setChecked(!isChecked);
                return;
            }

            FixedSwitchPreference.this.setChecked(isChecked);
        }
    }

    public FixedSwitchPreference(Context context) {
        super(context);
        init(context, null, 0, 0);
    }

    public FixedSwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, 0);
    }

    public FixedSwitchPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public FixedSwitchPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    public void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FixedSwitchPreference, defStyleAttr, defStyleRes);
        setSummaryOn(a.getString(R.styleable.FixedSwitchPreference_summaryOn));
        setSummaryOff(a.getString(R.styleable.FixedSwitchPreference_summaryOff));
        setSwitchTextOn(a.getString(R.styleable.FixedSwitchPreference_switchTextOn));
        setSwitchTextOff(a.getString(R.styleable.FixedSwitchPreference_switchTextOff));
        setDisableDependentsState(a.getBoolean(R.styleable.FixedSwitchPreference_disableDependentsState, false));
        a.recycle();
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        View checkableView = view.findViewById(R.id.switchWidget);
        if (checkableView != null && checkableView instanceof Checkable) {
            if (checkableView instanceof LauncherSwitch) {
                final SwitchCompat switchView = (LauncherSwitch) checkableView;
                switchView.setOnCheckedChangeListener(null);
            }

            ((Checkable) checkableView).setChecked(isChecked());

            if (checkableView instanceof LauncherSwitch) {
                final LauncherSwitch switchView = (LauncherSwitch) checkableView;
                switchView.setTextOn(mSwitchOn);
                switchView.setTextOff(mSwitchOff);
                switchView.setOnCheckedChangeListener(mListener);
            }
        }

        if (sSyncSummaryViewMethod != null) {
            try {
                sSyncSummaryViewMethod.invoke(this, view);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        TextView titleTv = (TextView) view.findViewById(android.R.id.title);
        if (titleTv != null) {
            titleTv.setTextColor(getContext().getResources().getColor(android.R.color.white));
        }
        TextView summaryTv = (TextView) view.findViewById(android.R.id.summary);
        if(summaryTv != null){
            summaryTv.setTextColor(getContext().getResources().getColor(R.color.settings_summary_color));
        }
        Resources mResources = Resources.getSystem();  //getResources()测试也可以
        int id = mResources.getIdentifier("icon_frame", "id", "android");
        LinearLayout iconLl = (LinearLayout) view.findViewById(id);
        if(iconLl != null){
            iconLl.setMinimumWidth(0);
            iconLl.setGravity(Gravity.CENTER);
            iconLl.setPadding(10,0,45,0);
        }
    }


    /**
     * Set the text displayed on the switch widget in the on state.
     * This should be a very short string; one word if possible.
     *
     * @param onText Text to display in the on state
     */
    public void setSwitchTextOn(CharSequence onText) {
        mSwitchOn = onText;
        notifyChanged();
    }

    /**
     * Set the text displayed on the switch widget in the off state.
     * This should be a very short string; one word if possible.
     *
     * @param offText Text to display in the off state
     */
    public void setSwitchTextOff(CharSequence offText) {
        mSwitchOff = offText;
        notifyChanged();
    }

    /**
     * Set the text displayed on the switch widget in the on state.
     * This should be a very short string; one word if possible.
     *
     * @param resId The text as a string resource ID
     */
    public void setSwitchTextOn(@StringRes int resId) {
        setSwitchTextOn(getContext().getString(resId));
    }

    /**
     * Set the text displayed on the switch widget in the off state.
     * This should be a very short string; one word if possible.
     *
     * @param resId The text as a string resource ID
     */
    public void setSwitchTextOff(@StringRes int resId) {
        setSwitchTextOff(getContext().getString(resId));
    }

    /**
     * @return The text that will be displayed on the switch widget in the on state
     */
    public CharSequence getSwitchTextOn() {
        return mSwitchOn;
    }

    /**
     * @return The text that will be displayed on the switch widget in the off state
     */
    public CharSequence getSwitchTextOff() {
        return mSwitchOff;
    }
}