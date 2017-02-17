package com.atwal.wakeup.battery.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;

import com.thehotgame.bottleflip.R;

/**
 * Created by helloworld-android on 2017/1/16.
 */

public class MyScrollview extends ScrollView {
    public MyScrollview(Context context) {
        super(context);
        setOverScrollMode(View.OVER_SCROLL_NEVER);
    }

    public MyScrollview(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOverScrollMode(View.OVER_SCROLL_NEVER);
    }

    public MyScrollview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOverScrollMode(View.OVER_SCROLL_NEVER);
    }
}
