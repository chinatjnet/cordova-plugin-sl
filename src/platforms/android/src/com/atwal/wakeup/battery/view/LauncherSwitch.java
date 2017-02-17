package com.atwal.wakeup.battery.view;

import android.content.Context;
import android.support.v7.widget.SwitchCompat;
import android.util.AttributeSet;

import com.thehotgame.bottleflip.R;


/**
 * Created by sks on 2016/9/13.
 */
public class LauncherSwitch extends SwitchCompat {
    public LauncherSwitch(Context context) {
        super(context);
    }

    public LauncherSwitch(Context context, AttributeSet attrs) {
        super(context, attrs);

        setButtonDrawable(context.getResources().getDrawable(R.drawable.switch_bg_selector));
        setTrackDrawable(null);
        setThumbDrawable(null);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
