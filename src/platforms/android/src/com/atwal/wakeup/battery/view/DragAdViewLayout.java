package com.atwal.wakeup.battery.view;

import android.content.Context;
import android.graphics.Point;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.thehotgame.bottleflip.R;


public class DragAdViewLayout extends FrameLayout {
    private ViewDragHelper mDragHelper;
    private View mAdView;
    private TextView mOpenTv;
    private TextView mRemoveTv;
    private float mDragOffset;
    private static float MAX_ALPHA = 1.0f;
    private float mOpenAdRange;
    private float mMaxVelocity;
    private Direction direction = Direction.LEFT;
    private OnDragListener mOnDragListener;
    public enum Direction {
       LEFT,RIGHT
    }
    private Point mAutoBackOriginPos = new Point();
    public DragAdViewLayout(Context context) {
        this(context, null);
    }

    public DragAdViewLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragAdViewLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mDragHelper = ViewDragHelper.create(this, 1f, new ViewDragCallback());
        mMaxVelocity = ViewConfiguration.get(context).getMaximumFlingVelocity() / 2;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mAdView = findViewById(R.id.battery_ad_main_rl);
        mOpenTv = (TextView) findViewById(R.id.open_tv);
        mRemoveTv = (TextView) findViewById(R.id.remove_tv);

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mDragHelper.processTouchEvent(event);
        return true;
    }
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        mAutoBackOriginPos.x = mAdView.getLeft();
        mAutoBackOriginPos.y = mAdView.getTop();
        mOpenAdRange = mAdView.getMeasuredWidth();
    }
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    private class ViewDragCallback extends ViewDragHelper.Callback {

        @Override
        public boolean tryCaptureView(View view, int i) {
            return view == mAdView;
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            super.onViewPositionChanged(changedView, left, top, dx, dy);
            mDragOffset = left;
            float width = mOpenAdRange / 4;
            float alpha =  MAX_ALPHA - (MAX_ALPHA - Math.abs(left) / width);
            mOpenTv.setAlpha(alpha);
            mRemoveTv.setAlpha(alpha);
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            int newLeft = 0;

            if(left > 0){
                int leftBound = getPaddingLeft();
                int rightBound = getWidth() - getPaddingRight();
                newLeft = Math.min(Math.max(left, leftBound), rightBound);
                direction = Direction.RIGHT;
                //mOpenTv.setVisibility(View.VISIBLE);
                mRemoveTv.setVisibility(View.INVISIBLE);
            }else if(left < 0){
                newLeft = Math.max(-child.getWidth(), Math.min(left, 0));
                direction = Direction.LEFT;
                mOpenTv.setVisibility(View.INVISIBLE);
                //mRemoveTv.setVisibility(View.VISIBLE);
            }
            return newLeft;
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);
            switch (direction){
                case RIGHT:

                    if (mDragOffset >= mOpenAdRange / 4|| xvel > mMaxVelocity / 4) {
                        if (mDragHelper.smoothSlideViewTo(releasedChild, getRight() - getPaddingRight(), 0)) {
                            ViewCompat.postInvalidateOnAnimation(DragAdViewLayout.this);
                            mOpenTv.setVisibility(View.INVISIBLE);
                            mRemoveTv.setVisibility(View.INVISIBLE);
                            if(mOnDragListener != null){
                                mOnDragListener.onDragOpen();
                            }
                        }
                    } else {
                        if (mDragHelper.smoothSlideViewTo(releasedChild,mAutoBackOriginPos.x, mAutoBackOriginPos.y)) {
                            mOpenTv.setVisibility(View.INVISIBLE);
                            mRemoveTv.setVisibility(View.INVISIBLE);
                            ViewCompat.postInvalidateOnAnimation(DragAdViewLayout.this);
                        }
                    }
                    break;
                case LEFT:
                    if (-mDragOffset >= mOpenAdRange / 3 || xvel > mMaxVelocity / 3) {
                        if (mDragHelper.smoothSlideViewTo(releasedChild,-(getRight() + getPaddingRight()), 0)) {
                            ViewCompat.postInvalidateOnAnimation(DragAdViewLayout.this);
                            mOpenTv.setVisibility(View.INVISIBLE);
                            mRemoveTv.setVisibility(View.INVISIBLE);
                            if(mOnDragListener != null){
                                mOnDragListener.onDragRemove();
                            }
                        }
                    } else {
                        if (mDragHelper.smoothSlideViewTo(releasedChild,mAutoBackOriginPos.x, mAutoBackOriginPos.y)) {
                            mOpenTv.setVisibility(View.INVISIBLE);
                            mRemoveTv.setVisibility(View.INVISIBLE);
                            ViewCompat.postInvalidateOnAnimation(DragAdViewLayout.this);
                        }
                    }
                    break;
            }

        }

        @Override
        public int getViewHorizontalDragRange(View child) {
            return getMeasuredWidth();
        }
    }

    @Override
    public void computeScroll() {
        if (mDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }
    public void setOnDragListener(OnDragListener onDragListener){
        this.mOnDragListener = onDragListener;
    }
    public void moveToOriginal(){
        try {
            if (mDragHelper.smoothSlideViewTo(mAdView, mAutoBackOriginPos.x, mAutoBackOriginPos.y)) {
                ViewCompat.postInvalidateOnAnimation(DragAdViewLayout.this);
            }
        }catch (Exception e){

        }
    }
    public interface OnDragListener{
        void onDragOpen();
        void onDragRemove();
    }
}
