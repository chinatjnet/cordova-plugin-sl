package com.atwal.wakeup.battery.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.thehotgame.bottleflip.R;


public class FlashTextView extends TextView {

    private static int BACK_DURATION = 15;        //回退动画时间间隔值
    private static float VE_HORIZONTAL = 0.065f;        //水平方向前进速率

    private boolean mStarted = false;
    private Paint mPaint;
    private Bitmap mBitmap;
    private Matrix matrix;
    private float mLastMoveX = 0;
    private int wordw = 420;
    private int mScreenWidth;

    public FlashTextView(Context context) {
        this(context, null);
    }

    public FlashTextView(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public FlashTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView();
        startAnimation();
    }

    public void startAnimation() {
        if (!mStarted) {
            mStarted = true;
            mHandler.sendEmptyMessageDelayed(1, BACK_DURATION);
        }
    }

    public void stopAnimation() {
        if (mStarted) {
            mStarted = false;
            invalidate();
        }
    }

    private void refresh() {
        if (mBitmap != null) {
            mLastMoveX = mLastMoveX + BACK_DURATION * VE_HORIZONTAL;
            updateMatrix(mLastMoveX, 0);
            if (getAlpha() == 0f || mLastMoveX > wordw + mBitmap.getWidth()) {
                mLastMoveX = -mBitmap.getWidth();
            }
            mHandler.sendEmptyMessageDelayed(1, BACK_DURATION);
        }
    }

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if (mStarted) {
                        refresh();
                        invalidate();
                    }
                    break;
            }
            return false;
        }

    });

    private void updateMatrix(float x, float y) {
        float x1 = x - mBitmap.getWidth() / 2;
        matrix.reset();
        matrix.postTranslate(x1, 0);
        invalidate();
    }

    private void initView() {
        setLayerType(LAYER_TYPE_HARDWARE, null);
        matrix = new Matrix();
        mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.lc_bg_sweep);
//        mBitmap = ThemeManager.getInstance(getContext()).getBitmap("lc_bg_sweep");

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));

        mScreenWidth = getContext().getResources().getDisplayMetrics().widthPixels;
        if (mScreenWidth != 0) {
            wordw = mScreenWidth * 2 / 3;
        }

        VE_HORIZONTAL = wordw / 1000.0f;
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (mBitmap != null) {
//            Log.d(this.getClass().getSimpleName(), "draw" + matrix.toString() + ":" + mBitmap.getWidth());
            canvas.drawBitmap(mBitmap, matrix, mPaint);
        }

    }

    @Override
    public void setVisibility(int visibility) {
        if (visibility != View.VISIBLE) {
            stopAnimation();
        } else {
            startAnimation();
        }
        super.setVisibility(visibility);
    }

}
