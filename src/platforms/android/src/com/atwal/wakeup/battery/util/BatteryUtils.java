package com.atwal.wakeup.battery.util;

import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.text.format.DateUtils;


import com.thehotgame.bottleflip.R;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by sks on 2016/9/26.
 */
public class BatteryUtils {
    private static long USB_ON_GAP = 50L;
    private static long USB_ON_TIME = 12288400L;
    private static long AC_ON_GAP = 50L;
    private static long AC_ON_TIME = 3967350L;
    private static long NEW_USER_INSTALL_TIME_THRESHOLD = 60 * 60 * 24; //在该时间阈值内的用户认定为新用户

    private BatteryUtils() {
    }

    public static String getBatteryFacebookPlacementId(Context context) {
        return AdUtil.FBAD_ID;
    }

    public static String getNowTimeStr(Context context) {
        Date date = new Date();
        DateFormat mTimeFormat;
        if (android.text.format.DateFormat.is24HourFormat(context)) {
            mTimeFormat = new SimpleDateFormat("HH:mm");
        } else {
            mTimeFormat = new SimpleDateFormat("h:mm");
        }
        String timeValue = mTimeFormat.format(date);
        return timeValue;
    }

    public static String getDateStr(Context context) {
        String dateValue = DateUtils.formatDateTime(context, System.currentTimeMillis(),
                DateUtils.FORMAT_NO_YEAR | DateUtils.FORMAT_ABBREV_ALL
                        | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_WEEKDAY);
        return dateValue;
    }
    public static String getPhoneName(){
        String phoneName = android.os.Build.MANUFACTURER;
        return phoneName;
    }
    public static long getFullChargeTime(Intent intent, int percent) {
        long milliseconds = SystemClock.elapsedRealtime() / 1000L;
        long[] savedValues = new long[2];
        long[] definedValues = new long[2];
        int plugged = intent.getIntExtra("plugged", 0);
        long leftTime = 0L;
        switch (plugged) {
            case 0:
                return -1L;
            case 1:
                savedValues[0] = (long) percent;
                savedValues[1] = milliseconds;
                definedValues[0] = AC_ON_GAP;
                definedValues[1] = AC_ON_TIME;
                break;
            case 2:
                savedValues[0] = (long) percent;
                savedValues[1] = milliseconds;
                definedValues[0] = USB_ON_GAP;
                definedValues[1] = USB_ON_TIME;
        }

        long totalLevelGap = savedValues[0] + definedValues[0];
        long totalTimeGap = savedValues[1] + definedValues[1];
        double curChargingRate = 0.0D;
        if (totalLevelGap > 0L) {
            curChargingRate = (double) totalTimeGap / (double) totalLevelGap;
        }

        int scale = intent.getIntExtra("scale", 0);
        leftTime = (long) (curChargingRate * (double) (scale - percent)) / 1000L;
        return leftTime;
    }

    public static int getBatteryPercent(int level, int scale) {
        DecimalFormat formatter = new DecimalFormat();
        formatter.applyPattern("#");
        String levelStr = formatter.format((double) ((float) level / (float) scale * 100.0F));
        Locale.setDefault(Locale.ENGLISH);
        return getIntPercent(levelStr);
    }

    public static int getIntPercent(String progressStr) {
        int percent;
        try {
            String e = "[^0-9]";
            Pattern pattern = Pattern.compile(e);
            Matcher matcher = pattern.matcher(progressStr);
            String subStr = matcher.replaceAll("").trim();
            percent = Integer.parseInt(subStr);
        } catch (Exception var6) {
            percent = 10;
        }
        return percent;
    }

    public static String getBatteryFullTimeStr(Context context, Intent intent, int percent) {
        String timeStr = null;
        int changeTime = (int) getFullChargeTime(intent, percent);
        if (changeTime == -1) {
            return null;
        } else {
            int time = changeTime / 60;
            int hour = time / 60;
            int minute = time % 60;
            String startText = context.getResources().getString(R.string.battery_full_time_start_text);
            if (hour == 0 && minute > 0) {
                timeStr = startText + " " + context.getResources().getQuantityString(R.plurals.battery_full_time_minute, minute, new Object[]{Integer.valueOf(minute)});
            } else if (hour > 0 && minute == 0) {
                timeStr = startText + " " + context.getResources().getQuantityString(R.plurals.battery_full_time_hour, hour, new Object[]{Integer.valueOf(hour)});
            } else if (hour > 0 && minute > 0) {
                String hourStr = context.getResources().getQuantityString(R.plurals.battery_full_time_hour, hour, new Object[]{Integer.valueOf(hour)});
                String minuteStr = context.getResources().getQuantityString(R.plurals.battery_full_time_minute, minute, new Object[]{Integer.valueOf(minute)});
                timeStr = startText + " " + hourStr + " " + minuteStr;
            } else if (hour == 0 && minute == 0 && percent < 100) {
                timeStr = startText + " " + context.getResources().getQuantityString(R.plurals.battery_full_time_minute, 1, new Object[]{Integer.valueOf(1)});
            }
            if (percent == 100) {
                timeStr = context.getResources().getString(R.string.battery_is_full);
            }
            return timeStr;
        }
    }

    public static int getCurrentBatteryColor(Resources res, int percent) {
        return percent <= 10 ? res.getColor(R.color.battery_charging_color_red) : res.getColor(R.color.settings_set_def_text_color);
    }

    public static Drawable getDyeDrawable(Resources res, Drawable drawable, int percent) {
        drawable.setColorFilter(getCurrentBatteryColor(res, percent), PorterDuff.Mode.SRC_IN);
        return drawable;
    }

    public static Drawable getDyeWhiteDrawable(Drawable drawable) {
        drawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
        return drawable;
    }

    public static Bitmap getBlurBg(WallpaperManager mWallpaperManager) {
        Bitmap bitmap = null;

        try {
            Drawable wallpaperDrawable = mWallpaperManager.getDrawable();
            bitmap = ((BitmapDrawable) wallpaperDrawable).getBitmap();
        } catch (Exception e) {

        }
        return BoxBlurFilter(bitmap, 25);
    }

    public static Bitmap BoxBlurFilter(Bitmap bmp, int radius) {
        Bitmap blurBitmap = null;
        float scaleFactor = 4.0F;

        try {
            float sx = 1.0F / scaleFactor;
            float sy = 1.0F / scaleFactor;
            blurBitmap = Bitmap.createBitmap((int) ((float) bmp.getWidth() / scaleFactor), (int) ((float) bmp.getHeight() / scaleFactor), Bitmap.Config.ARGB_8888);
            int width = blurBitmap.getWidth();
            int height = blurBitmap.getHeight();
            Canvas canvas = new Canvas(blurBitmap);
            canvas.scale(sx, sy);
            Matrix matrix = new Matrix();
            matrix.setTranslate(0.0F, 0.0F);
            matrix.preTranslate((float) (width / 2), (float) (height / 2));
            matrix.preScale(sx, sy);
            matrix.preTranslate((float) (-width / 2), (float) (-height / 2));
            Paint paint = new Paint();
            paint.setFlags(2);
            canvas.drawBitmap(bmp, 0.0F, 0.0F, paint);
            blurBitmap = doBlur(blurBitmap, radius, true);
        } catch (Exception e) {
        }
        return blurBitmap;
    }

    public static Bitmap doBlur(Bitmap sentBitmap, int radius, boolean canReuseInBitmap) {
        Bitmap bitmap;
        if (canReuseInBitmap) {
            bitmap = sentBitmap;
        } else {
            bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);
        }

        if (radius < 1) {
            return null;
        } else {
            int w = bitmap.getWidth();
            int h = bitmap.getHeight();
            int[] pix = new int[w * h];
            bitmap.getPixels(pix, 0, w, 0, 0, w, h);
            int wm = w - 1;
            int hm = h - 1;
            int wh = w * h;
            int div = radius + radius + 1;
            int[] r = new int[wh];
            int[] g = new int[wh];
            int[] b = new int[wh];
            int[] vmin = new int[Math.max(w, h)];
            int divsum = div + 1 >> 1;
            divsum *= divsum;
            int[] dv = new int[256 * divsum];

            int i;
            for (i = 0; i < 256 * divsum; ++i) {
                dv[i] = i / divsum;
            }

            int yi = 0;
            int yw = 0;
            int[][] stack = new int[div][3];
            int r1 = radius + 1;

            int rsum;
            int gsum;
            int bsum;
            int x;
            int y;
            int p;
            int stackpointer;
            int stackstart;
            int[] sir;
            int rbs;
            int routsum;
            int goutsum;
            int boutsum;
            int rinsum;
            int ginsum;
            int binsum;
            for (y = 0; y < h; ++y) {
                bsum = 0;
                gsum = 0;
                rsum = 0;
                boutsum = 0;
                goutsum = 0;
                routsum = 0;
                binsum = 0;
                ginsum = 0;
                rinsum = 0;

                for (i = -radius; i <= radius; ++i) {
                    p = pix[yi + Math.min(wm, Math.max(i, 0))];
                    sir = stack[i + radius];
                    sir[0] = (p & 16711680) >> 16;
                    sir[1] = (p & '\uff00') >> 8;
                    sir[2] = p & 255;
                    rbs = r1 - Math.abs(i);
                    rsum += sir[0] * rbs;
                    gsum += sir[1] * rbs;
                    bsum += sir[2] * rbs;
                    if (i > 0) {
                        rinsum += sir[0];
                        ginsum += sir[1];
                        binsum += sir[2];
                    } else {
                        routsum += sir[0];
                        goutsum += sir[1];
                        boutsum += sir[2];
                    }
                }

                stackpointer = radius;

                for (x = 0; x < w; ++x) {
                    r[yi] = dv[rsum];
                    g[yi] = dv[gsum];
                    b[yi] = dv[bsum];
                    rsum -= routsum;
                    gsum -= goutsum;
                    bsum -= boutsum;
                    stackstart = stackpointer - radius + div;
                    sir = stack[stackstart % div];
                    routsum -= sir[0];
                    goutsum -= sir[1];
                    boutsum -= sir[2];
                    if (y == 0) {
                        vmin[x] = Math.min(x + radius + 1, wm);
                    }

                    p = pix[yw + vmin[x]];
                    sir[0] = (p & 16711680) >> 16;
                    sir[1] = (p & '\uff00') >> 8;
                    sir[2] = p & 255;
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                    rsum += rinsum;
                    gsum += ginsum;
                    bsum += binsum;
                    stackpointer = (stackpointer + 1) % div;
                    sir = stack[stackpointer % div];
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                    rinsum -= sir[0];
                    ginsum -= sir[1];
                    binsum -= sir[2];
                    ++yi;
                }

                yw += w;
            }

            for (x = 0; x < w; ++x) {
                bsum = 0;
                gsum = 0;
                rsum = 0;
                boutsum = 0;
                goutsum = 0;
                routsum = 0;
                binsum = 0;
                ginsum = 0;
                rinsum = 0;
                int yp = -radius * w;

                for (i = -radius; i <= radius; ++i) {
                    yi = Math.max(0, yp) + x;
                    sir = stack[i + radius];
                    sir[0] = r[yi];
                    sir[1] = g[yi];
                    sir[2] = b[yi];
                    rbs = r1 - Math.abs(i);
                    rsum += r[yi] * rbs;
                    gsum += g[yi] * rbs;
                    bsum += b[yi] * rbs;
                    if (i > 0) {
                        rinsum += sir[0];
                        ginsum += sir[1];
                        binsum += sir[2];
                    } else {
                        routsum += sir[0];
                        goutsum += sir[1];
                        boutsum += sir[2];
                    }

                    if (i < hm) {
                        yp += w;
                    }
                }
                yi = x;
                stackpointer = radius;
                for (y = 0; y < h; ++y) {
                    pix[yi] = -16777216 & pix[yi] | dv[rsum] << 16 | dv[gsum] << 8 | dv[bsum];
                    rsum -= routsum;
                    gsum -= goutsum;
                    bsum -= boutsum;
                    stackstart = stackpointer - radius + div;
                    sir = stack[stackstart % div];
                    routsum -= sir[0];
                    goutsum -= sir[1];
                    boutsum -= sir[2];
                    if (x == 0) {
                        vmin[y] = Math.min(y + r1, hm) * w;
                    }

                    p = x + vmin[y];
                    sir[0] = r[p];
                    sir[1] = g[p];
                    sir[2] = b[p];
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                    rsum += rinsum;
                    gsum += ginsum;
                    bsum += binsum;
                    stackpointer = (stackpointer + 1) % div;
                    sir = stack[stackpointer];
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                    rinsum -= sir[0];
                    ginsum -= sir[1];
                    binsum -= sir[2];
                    yi += w;
                }
            }
            bitmap.setPixels(pix, 0, w, 0, 0, w, h);
            return bitmap;
        }
    }
}
