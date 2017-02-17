package com.atwal.wakeup.battery.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.FontMetrics;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.atwal.wakeup.battery.util.Utilities;
import com.thehotgame.bottleflip.R;


public class CircularProgress extends View {
    private int progress = 0;
    private static final int DEFAULT_BORDER_WIDTH = 6, DEFAULT_BG_WIDTH = 6;
    private final RectF fBounds = new RectF();
    private Paint progressPaint;
    private Paint textPaint;
    private Paint bgPaint;
    private float mBorderWidth, bgBorderWidth;
    private boolean isShowPercentText = true;
    private boolean mRunning;
    private int wheelAngles = 250;
    private int startAngles = 250;
    private int which;
    private Context context;
    private int centerTextSize = 25;
    private boolean isShowPercent = true;

    public CircularProgress(Context context) {
        this(context, null);
        this.context = context;
    }

    public CircularProgress(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        this.context = context;
    }

    public CircularProgress(Context context, AttributeSet attrs,
                            int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;

        float density = context.getResources().getDisplayMetrics().density;
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.CircularProgress, defStyleAttr, 0);
        mBorderWidth = a.getDimension(
                R.styleable.CircularProgress_borderCircularWidth,
                DEFAULT_BORDER_WIDTH * density);
        bgBorderWidth = a.getDimension(
                R.styleable.CircularProgress_bgBorderCircularWidth,
                DEFAULT_BG_WIDTH * density);
        isShowPercentText = a.getBoolean(
                R.styleable.CircularProgress_showPercentText, true);
        wheelAngles = a.getInteger(R.styleable.CircularProgress_wheelAngles,
                250);
        startAngles = a.getInteger(R.styleable.CircularProgress_startAngles,
                -(wheelAngles / 2 + 90));
        a.recycle();
        progressPaint = new Paint();
        progressPaint.setAntiAlias(true);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeCap(Cap.ROUND);
        progressPaint.setStrokeWidth(mBorderWidth);
        progressPaint.setColor(Color.WHITE);

        bgPaint = new Paint();
        bgPaint.setAntiAlias(true);
        bgPaint.setStyle(Paint.Style.STROKE);
        bgPaint.setStrokeCap(Cap.ROUND);
        bgPaint.setStrokeWidth(bgBorderWidth);
        bgPaint.setColor(getContext().getResources().getColor(R.color.weather_search_bg_color));

        textPaint = new Paint();
        textPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        setCenterTextSize(50);
        textPaint.setTextSize(getCenterTextSize());
        textPaint.setColor(Color.WHITE);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    }

    /**
     * @return
     */
    public boolean isRunning() {
        return mRunning;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        fBounds.left = mBorderWidth / 2f + .5f;
        fBounds.right = w - mBorderWidth / 2f - .5f;
        fBounds.top = mBorderWidth / 2f + .5f;
        fBounds.bottom = h - mBorderWidth / 2f - .5f;

        fBounds.left = bgBorderWidth / 2f + .5f;
        fBounds.right = w - bgBorderWidth / 2f - .5f;
        fBounds.top = bgBorderWidth / 2f + .5f;
        fBounds.bottom = h - bgBorderWidth / 2f - .5f;
    }

    /**
     * @param progress
     */
    public void setCircularProgress(int progress) {
        this.progress = progress;
        invalidate();
    }

    public int getCenterTextSize() {
        return centerTextSize;
    }

    public void setCenterTextSize(int centerTextSize) {
        this.centerTextSize = Utilities.dip2px(context, centerTextSize);
    }



    /**
     * @return
     */
    public boolean isShowPercent() {
        return isShowPercent;
    }

    public void setShowPercent(boolean isShowPercent) {
        this.isShowPercent = isShowPercent;
    }

    /**
     * @return
     */
    public int getCircularProgress() {
        return progress;
    }

    private String getStringProgress() {
        return String.valueOf(getCircularProgress()) + "%";
    }

    /**
     *
     *
     */
    private int getProgressAngle() {
        int progressAngle = wheelAngles * progress / 100;
        return progressAngle;
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        float startAngle = startAngles;
        float sweepAngle = 0;
        sweepAngle += getProgressAngle();
        canvas.drawArc(fBounds, startAngle, wheelAngles, false, bgPaint);
        canvas.drawArc(fBounds, startAngle, sweepAngle, false, progressPaint);
        if (isShowPercentText) {
            String text = isShowPercent() ? getStringProgress() : String
                    .valueOf(getCircularProgress());
            textPaint.setTextSize(getCenterTextSize());
            float x = (getWidth() - textPaint.measureText(text)) / 2;
            float y = (getHeight() - getFontHeight(textPaint)) / 2
                    + getFontLeading(textPaint);
            canvas.drawText(text, x, y, textPaint);
        }

    }

    /**
     * @param color
     */
    public void setProgressColor(int color) {
        if (progressPaint != null) {
            progressPaint.setColor(color);
            invalidate();
        }
    }

    /**
     * @param color
     */
    public void setBgProgressColor(int color) {
        if (bgPaint != null) {
            bgPaint.setColor(color);
            invalidate();
        }
    }

    /**
     * @return
     */
    public static float getFontHeight(Paint paint) {
        FontMetrics fm = paint.getFontMetrics();
        return fm.descent - fm.ascent;
    }

    /**
     * @return
     */
    public static float getFontLeading(Paint paint) {
        FontMetrics fm = paint.getFontMetrics();
        return fm.leading - fm.ascent;
    }

    public int getCircularRadius() {
        return (int) ((getHeight() - mBorderWidth) / 2);
    }

    /**
     * @param which
     */
    public void setInterpolator(int which) {
        this.which = which;
    }

    public int getInterpolator() {
        return which;
    }

}
