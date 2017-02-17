/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.atwal.wakeup.battery.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.SearchManager;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.UnderlineSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Various utilities shared amongst the Launcher's classes.
 */
public final class Utilities {

    private static final String TAG = "Launcher.Utilities";
    public static final int UMENG_CHANNEL_MAX_LENGTH = 126;
    private static final String FIRST_INSTALL_TIME = "first_install_time";
    private static final Rect sOldBounds = new Rect();
    private static final Canvas sCanvas = new Canvas();
    private static final Pattern sTrimPattern =
            Pattern.compile("^[\\s|\\p{javaSpaceChar}]*(.*)[\\s|\\p{javaSpaceChar}]*$");

    static {
        sCanvas.setDrawFilter(new PaintFlagsDrawFilter(Paint.DITHER_FLAG,
                Paint.FILTER_BITMAP_FLAG));
    }

    static int sColors[] = {0xffff0000, 0xff00ff00, 0xff0000ff};
    static int sColorIndex = 0;

    private static final int[] sLoc0 = new int[2];
    private static final int[] sLoc1 = new int[2];

    // To turn on these properties, type
    // adb shell setprop log.tag.PROPERTY_NAME [VERBOSE | SUPPRESS]
    private static final String FORCE_ENABLE_ROTATION_PROPERTY = "launcher_force_rotate";
    private static boolean sForceEnableRotation = isPropertyEnabled(FORCE_ENABLE_ROTATION_PROPERTY);

    public static final String ALLOW_ROTATION_PREFERENCE_KEY = "pref_allowRotation";

    public static boolean isPropertyEnabled(String propertyName) {
        return Log.isLoggable(propertyName, Log.VERBOSE);
    }

    /**
     * whether launcher has installed more than specify time
     */
    public static boolean hasInstalledMoreThan(Context context, long hasInstallTimes) {
        try {
            long firstInstallTime = SettingsHelper.getPreferenceLong(context, FIRST_INSTALL_TIME, -1);

            if (firstInstallTime == -1) {
                PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                firstInstallTime =  info.firstInstallTime;
                SettingsHelper.setPreferenceLong(context, FIRST_INSTALL_TIME, firstInstallTime);
            }

            long elapsedTimes =  (System.currentTimeMillis() - firstInstallTime) / 1000;

            if (elapsedTimes > hasInstallTimes) {
                return true;
            } else {
                return false;
            }

        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Indicates if the device is running LMP or higher.
     */
    public static boolean isLmpOrAbove() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public static boolean isLmpMR1OrAbove() {
        return Build.VERSION.SDK_INT >= 22;
    }

    public static boolean isLmpMR1() {
        return Build.VERSION.SDK_INT == 22;
    }



    // compose new image use mask icon by miao
    public static Bitmap composeIcon(Drawable icon, Drawable maskIcon, int width, int height) {
        Bitmap app_mask_icon_scaled = Bitmap.createScaledBitmap(((BitmapDrawable) maskIcon).getBitmap(), width, height, false);
        Bitmap icon_scaled = Bitmap.createScaledBitmap(((BitmapDrawable) icon).getBitmap(), width, height, false);
        Paint paint = new Paint();
        PorterDuffXfermode porterDuffXfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(app_mask_icon_scaled, 0, 0, paint);
        paint.setXfermode(porterDuffXfermode);
        canvas.drawBitmap(icon_scaled, 0, 0, paint);
        canvas.setBitmap(null);
        return bitmap;
    }

    /**
     * remove bitmap white around white section by miao
     */
    public static Bitmap removeAroundWhiteSection(Bitmap icon) {
        int xStart = 0;
        int xEnd = 0;
        int yStart = 0;
        int yEnd = 0;
        int width = icon.getWidth();
        int height = icon.getHeight();
        Bitmap copyIcon = icon.copy(Bitmap.Config.ARGB_8888, false);
        copyIcon = Bitmap.createScaledBitmap(copyIcon, width, 1, false);
        for (int i = 0; i < width; i++) {
            int rgb = copyIcon.getPixel(i, 0);
            if (rgb != 0 && xStart == 0) {
                xStart = i;
                continue;
            }
            if (rgb == 0 && xStart != 0) {
                xEnd = i;
            }
        }

        copyIcon = icon.copy(Bitmap.Config.ARGB_8888, false);
        copyIcon = Bitmap.createScaledBitmap(copyIcon, 1, height, false);
        for (int i = 0; i < height; i++) {
            int rgb = copyIcon.getPixel(0, i);
            if (rgb != 0 && yStart == 0) {
                yStart = i;
                continue;
            }
            if (rgb == 0 && yStart != 0) {
                yEnd = i;
            }
        }
        if (xStart < xEnd && yStart < yEnd) {
            icon = Bitmap.createBitmap(icon, xStart, yStart, xEnd - xStart, yEnd - yStart);
            Log.d(TAG, "success remove white section");
        } else {
            Log.d(TAG, "fail remove white section");
        }
        return icon;
    }


    /**
     * Given a coordinate relative to the descendant, find the coordinate in a parent view's
     * coordinates.
     *
     * @param descendant        The descendant to which the passed coordinate is relative.
     * @param root              The root view to make the coordinates relative to.
     * @param coord             The coordinate that we want mapped.
     * @param includeRootScroll Whether or not to account for the scroll of the descendant:
     *                          sometimes this is relevant as in a child's coordinates within the descendant.
     * @return The factor by which this descendant is scaled relative to this DragLayer. Caution
     * this scale factor is assumed to be equal in X and Y, and so if at any point this
     * assumption fails, we will need to return a pair of scale factors.
     */
    public static float getDescendantCoordRelativeToParent(View descendant, View root,
                                                           int[] coord, boolean includeRootScroll) {
        ArrayList<View> ancestorChain = new ArrayList<View>();

        float[] pt = {coord[0], coord[1]};

        View v = descendant;
        while (v != root && v != null) {
            ancestorChain.add(v);
            v = (View) v.getParent();
        }
        ancestorChain.add(root);

        float scale = 1.0f;
        int count = ancestorChain.size();
        for (int i = 0; i < count; i++) {
            View v0 = ancestorChain.get(i);
            // For TextViews, scroll has a meaning which relates to the text position
            // which is very strange... ignore the scroll.
            if (v0 != descendant || includeRootScroll) {
                pt[0] -= v0.getScrollX();
                pt[1] -= v0.getScrollY();
            }

            v0.getMatrix().mapPoints(pt);
            pt[0] += v0.getLeft();
            pt[1] += v0.getTop();
            scale *= v0.getScaleX();
        }

        coord[0] = (int) Math.round(pt[0]);
        coord[1] = (int) Math.round(pt[1]);
        return scale;
    }

    /**
     * Inverse of .
     */
    public static float mapCoordInSelfToDescendent(View descendant, View root,
                                                   int[] coord) {
        ArrayList<View> ancestorChain = new ArrayList<View>();

        float[] pt = {coord[0], coord[1]};

        View v = descendant;
        while (v != root) {
            ancestorChain.add(v);
            v = (View) v.getParent();
        }
        ancestorChain.add(root);

        float scale = 1.0f;
        Matrix inverse = new Matrix();
        int count = ancestorChain.size();
        for (int i = count - 1; i >= 0; i--) {
            View ancestor = ancestorChain.get(i);
            View next = i > 0 ? ancestorChain.get(i - 1) : null;

            pt[0] += ancestor.getScrollX();
            pt[1] += ancestor.getScrollY();

            if (next != null) {
                pt[0] -= next.getLeft();
                pt[1] -= next.getTop();
                next.getMatrix().invert(inverse);
                inverse.mapPoints(pt);
                scale *= next.getScaleX();
            }
        }

        coord[0] = (int) Math.round(pt[0]);
        coord[1] = (int) Math.round(pt[1]);
        return scale;
    }
    //add by luxin
    public static void setMargins (View v, int left, int top, int right, int bottom) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(left, top, right, bottom);
            v.requestLayout();
        }
    }
    //add by luxin
    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }
    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }
    public static int getScreenWidth(Context context) {
        WindowManager manager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        return display.getWidth();
    }
    public static int getScreenHeight(Context context) {
        WindowManager manager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        return display.getHeight();
    }
    /**
     * Utility method to determine whether the given point, in local coordinates,
     * is inside the view, where the area of the view is expanded by the slop factor.
     * This method is called while processing touch-move events to determine if the event
     * is still within the view.
     */
    public static boolean pointInView(View v, float localX, float localY, float slop) {
        return localX >= -slop && localY >= -slop && localX < (v.getWidth() + slop) &&
                localY < (v.getHeight() + slop);
    }

    public static void scaleRect(Rect r, float scale) {
        if (scale != 1.0f) {
            r.left = (int) (r.left * scale + 0.5f);
            r.top = (int) (r.top * scale + 0.5f);
            r.right = (int) (r.right * scale + 0.5f);
            r.bottom = (int) (r.bottom * scale + 0.5f);
        }
    }

    public static int[] getCenterDeltaInScreenSpace(View v0, View v1, int[] delta) {
        v0.getLocationInWindow(sLoc0);
        v1.getLocationInWindow(sLoc1);

        sLoc0[0] += (v0.getMeasuredWidth() * v0.getScaleX()) / 2;
        sLoc0[1] += (v0.getMeasuredHeight() * v0.getScaleY()) / 2;
        sLoc1[0] += (v1.getMeasuredWidth() * v1.getScaleX()) / 2;
        sLoc1[1] += (v1.getMeasuredHeight() * v1.getScaleY()) / 2;

        if (delta == null) {
            delta = new int[2];
        }

        delta[0] = sLoc1[0] - sLoc0[0];
        delta[1] = sLoc1[1] - sLoc0[1];

        return delta;
    }

    public static void scaleRectAboutCenter(Rect r, float scale) {
        int cx = r.centerX();
        int cy = r.centerY();
        r.offset(-cx, -cy);
        Utilities.scaleRect(r, scale);
        r.offset(cx, cy);
    }


    public static void startActivityForResultSafely(
            Activity activity, Intent intent, int requestCode) {
        try {
            activity.startActivityForResult(intent, requestCode);
        } catch (ActivityNotFoundException e) {

        } catch (SecurityException e) {

            Log.e(TAG, "Launcher does not have the permission to launch " + intent +
                    ". Make sure to create a MAIN intent-filter for the corresponding activity " +
                    "or use the exported attribute for this activity.", e);
        }
    }

    //start activity for result safely from fragment, by miao
    public static void startActivityForResultSafely(Fragment fragment, Intent intent, int requestCode) {
        try {
            fragment.startActivityForResult(intent, requestCode);
        } catch (ActivityNotFoundException e) {
        } catch (SecurityException e) {
            Log.e(TAG, "Launcher does not have the permission to launch " + intent +
                    ". Make sure to create a MAIN intent-filter for the corresponding activity " +
                    "or use the exported attribute for this activity.", e);
        }
    }

    //start activity, by miao
    public static void startActivitySafely(Context context, Intent intent) {
        try {
            context.startActivity(intent);

        } catch (ActivityNotFoundException e) {

        } catch (SecurityException e) {

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static boolean isSystemApp(Context context, Intent intent) {
        PackageManager pm = context.getPackageManager();
        ComponentName cn = intent.getComponent();
        String packageName = null;
        if (cn == null) {
            ResolveInfo info = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
            if ((info != null) && (info.activityInfo != null)) {
                packageName = info.activityInfo.packageName;
            }
        } else {
            packageName = cn.getPackageName();
        }
        if (packageName != null) {
            try {
                PackageInfo info = pm.getPackageInfo(packageName, 0);
                return (info != null) && (info.applicationInfo != null) &&
                        ((info.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
            } catch (NameNotFoundException e) {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * This picks a dominant color, looking for high-saturation, high-value, repeated hues.
     *
     * @param bitmap  The bitmap to scan
     * @param samples The approximate max number of samples to use.
     */
    public static int findDominantColorByHue(Bitmap bitmap, int samples) {
        final int height = bitmap.getHeight();
        final int width = bitmap.getWidth();
        int sampleStride = (int) Math.sqrt((height * width) / samples);
        if (sampleStride < 1) {
            sampleStride = 1;
        }

        // This is an out-param, for getting the hsv values for an rgb
        float[] hsv = new float[3];

        // First get the best hue, by creating a histogram over 360 hue buckets,
        // where each pixel contributes a score weighted by saturation, value, and alpha.
        float[] hueScoreHistogram = new float[360];
        float highScore = -1;
        int bestHue = -1;

        for (int y = 0; y < height; y += sampleStride) {
            for (int x = 0; x < width; x += sampleStride) {
                int argb = bitmap.getPixel(x, y);
                int alpha = 0xFF & (argb >> 24);
                if (alpha < 0x80) {
                    // Drop mostly-transparent pixels.
                    continue;
                }
                // Remove the alpha channel.
                int rgb = argb | 0xFF000000;
                Color.colorToHSV(rgb, hsv);
                // Bucket colors by the 360 integer hues.
                int hue = (int) hsv[0];
                if (hue < 0 || hue >= hueScoreHistogram.length) {
                    // Defensively avoid array bounds violations.
                    continue;
                }
                float score = hsv[1] * hsv[2];
                hueScoreHistogram[hue] += score;
                if (hueScoreHistogram[hue] > highScore) {
                    highScore = hueScoreHistogram[hue];
                    bestHue = hue;
                }
            }
        }

        SparseArray<Float> rgbScores = new SparseArray<Float>();
        int bestColor = 0xff000000;
        highScore = -1;
        // Go theme_icon_back over the RGB colors that match the winning hue,
        // creating a histogram of weighted s*v scores, for up to 100*100 [s,v] buckets.
        // The highest-scoring RGB color wins.
        for (int y = 0; y < height; y += sampleStride) {
            for (int x = 0; x < width; x += sampleStride) {
                int rgb = bitmap.getPixel(x, y) | 0xff000000;
                Color.colorToHSV(rgb, hsv);
                int hue = (int) hsv[0];
                if (hue == bestHue) {
                    float s = hsv[1];
                    float v = hsv[2];
                    int bucket = (int) (s * 100) + (int) (v * 10000);
                    // Score by cumulative saturation * value.
                    float score = s * v;
                    Float oldTotal = rgbScores.get(bucket);
                    float newTotal = oldTotal == null ? score : oldTotal + score;
                    rgbScores.put(bucket, newTotal);
                    if (newTotal > highScore) {
                        highScore = newTotal;
                        // All the colors in the winning bucket are very similar. Last in wins.
                        bestColor = rgb;
                    }
                }
            }
        }
        return bestColor;
    }

    /*
     * Finds a system apk which had a broadcast receiver listening to a particular action.
     * @param action intent action used to find the apk
     * @return a pair of apk package name and the resources.
     */
    static Pair<String, Resources> findSystemApk(String action, PackageManager pm) {
        final Intent intent = new Intent(action);
        for (ResolveInfo info : pm.queryBroadcastReceivers(intent, 0)) {
            if (info.activityInfo != null &&
                    (info.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                final String packageName = info.activityInfo.packageName;
                try {
                    final Resources res = pm.getResourcesForApplication(packageName);
                    return Pair.create(packageName, res);
                } catch (NameNotFoundException e) {
                    Log.w(TAG, "Failed to find resources for " + packageName);
                }
            }
        }
        return null;
    }

    // by miao
    public static PackageInfo getPackageInfo(Context context, String packageName) {
        PackageInfo packageInfo = null;
        PackageManager pm = context.getPackageManager();
        try {
            packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
        } catch (NameNotFoundException e) {
        }
        return packageInfo;
    }

    public static boolean isPackageInstalled(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    public static void openGooglePlayForSpecialPage(Context context, String url){
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (isPackageInstalled(context, "com.android.vending")){
            intent.setPackage("com.android.vending");
            context.startActivity(intent);
        } else {
            context.startActivity(intent);
        }
    }

    public static void openSpecialAppByPkgName(Context context, String packageName){
        PackageManager pm = context.getPackageManager();
        Intent launchIntentForPackage = pm.getLaunchIntentForPackage(packageName);
        if (launchIntentForPackage != null) {
            launchIntentForPackage.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivitySafely(context, launchIntentForPackage);
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static boolean isViewAttachedToWindow(View v) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return v.isAttachedToWindow();
        } else {
            // A proxy call which returns null, if the view is not attached to the window.
            return v.getKeyDispatcherState() != null;
        }
    }

    /**
     * Returns a widget with category {@link AppWidgetProviderInfo#WIDGET_CATEGORY_SEARCHBOX}
     * provided by the same package which is set to be global search activity.
     * If widgetCategory is not supported, or no such widget is found, returns the first widget
     * provided by the package.
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static AppWidgetProviderInfo getSearchWidgetProvider(Context context) {
        SearchManager searchManager =
                (SearchManager) context.getSystemService(Context.SEARCH_SERVICE);
        ComponentName searchComponent = searchManager.getGlobalSearchActivity();
        if (searchComponent == null) return null;
        String providerPkg = searchComponent.getPackageName();

        AppWidgetProviderInfo defaultWidgetForSearchPackage = null;

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        for (AppWidgetProviderInfo info : appWidgetManager.getInstalledProviders()) {
            if (info.provider.getPackageName().equals(providerPkg)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    if ((info.widgetCategory & AppWidgetProviderInfo.WIDGET_CATEGORY_SEARCHBOX) != 0) {
                        return info;
                    } else if (defaultWidgetForSearchPackage == null) {
                        defaultWidgetForSearchPackage = info;
                    }
                } else {
                    return info;
                }
            }
        }
        return defaultWidgetForSearchPackage;
    }

    /**
     * Compresses the bitmap to a byte array for serialization.
     */
    public static byte[] flattenBitmap(Bitmap bitmap) {
        // Try go guesstimate how much space the icon will take when serialized
        // to avoid unnecessary allocations/copies during the write.
        int size = bitmap.getWidth() * bitmap.getHeight() * 4;
        ByteArrayOutputStream out = new ByteArrayOutputStream(size);
        try {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            return out.toByteArray();
        } catch (IOException e) {
            Log.w(TAG, "Could not write bitmap");
            return null;
        }
    }

    /**
     * Find the first vacant cell, if there is one.
     *
     * @param vacant Holds the x and y coordinate of the vacant cell
     * @param spanX  Horizontal cell span.
     * @param spanY  Vertical cell span.
     * @return true if a vacant cell was found
     */
    public static boolean findVacantCell(int[] vacant, int spanX, int spanY,
                                         int xCount, int yCount, boolean[][] occupied) {

        for (int y = 0; (y + spanY) <= yCount; y++) {
            for (int x = 0; (x + spanX) <= xCount; x++) {
                boolean available = !occupied[x][y];
                out:
                for (int i = x; i < x + spanX; i++) {
                    for (int j = y; j < y + spanY; j++) {
                        available = available && !occupied[i][j];
                        if (!available) break out;
                    }
                }

                if (available) {
                    vacant[0] = x;
                    vacant[1] = y;
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Trims the string, removing all whitespace at the beginning and end of the string.
     * Non-breaking whitespaces are also removed.
     */
    public static String trim(CharSequence s) {
        if (s == null) {
            return null;
        }

        // Just strip any sequence of whitespace or java space characters from the beginning and end
        Matcher m = sTrimPattern.matcher(s);
        return m.replaceAll("$1");
    }

    /**
     * Calculates the height of a given string at a specific text size.
     */
    public static float calculateTextHeight(float textSizePx) {
        Paint p = new Paint();
        p.setTextSize(textSizePx);
        Paint.FontMetrics fm = p.getFontMetrics();
        return -fm.top + fm.bottom;
    }

    /**
     * Convenience println with multiple args.
     */
    public static void println(String key, Object... args) {
        StringBuilder b = new StringBuilder();
        b.append(key);
        b.append(": ");
        boolean isFirstArgument = true;
        for (Object arg : args) {
            if (isFirstArgument) {
                isFirstArgument = false;
            } else {
                b.append(", ");
            }
            b.append(arg);
        }
        System.out.println(b.toString());
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static boolean isRtl(Resources res) {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) &&
                (res.getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL);
    }

    public static float dpiFromPx(int size, DisplayMetrics metrics) {
        float densityRatio = (float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT;
        return (size / densityRatio);
    }

    public static int pxFromDp(float size, DisplayMetrics metrics) {
        return (int) Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                size, metrics));
    }

    public static int pxFromSp(float size, DisplayMetrics metrics) {
        return (int) Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                size, metrics));
    }

    public static String createDbSelectionQuery(String columnName, Iterable<?> values) {
        return String.format(Locale.ENGLISH, "%s IN (%s)", columnName, TextUtils.join(", ", values));
    }

    // sampled bitmap before decode by miao
    public static Bitmap decodeSampledBitmap(Resources resources, int resId, int width, int height) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(resources, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, width, height);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;

        return BitmapFactory.decodeResource(resources, resId, options);
    }

    // sampled bitmap before decode by miao
    public static Bitmap decodeSampledBitmap(InputStream is, int width, int height) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
//        BitmapFactory.decodeStream(is, null, options);
//        options.inSampleSize = calculateInSampleSize(options, width, height);
        options.inJustDecodeBounds = false;
        options.inPurgeable = true;
        return BitmapFactory.decodeStream(is, null, options);
    }

    // sampled bitmap before decode by miao
    public static Bitmap decodeSampledBitmap(String filePath, int width, int height) {
        try {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(filePath, options);
            options.inSampleSize = calculateInSampleSize(options, width, height);
            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeFile(filePath, options);
        } catch (OutOfMemoryError e) {
            return null;
        }
    }

    // calculate sample size before decode by miao
    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            // Calculate ratios of height and width to requested height and
            // width
            final int heightRatio = Math.round((float) height
                    / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            //inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
            inSampleSize = heightRatio;
        }
        return inSampleSize;
    }

    public static String getChannelString(HashMap<String, String> referrer) {
        String mUtmSource = "nature";
        String mUtmContent = "nature";
        String mChannelStr = "nature_nature";
        if (referrer == null || referrer.size() == 0) {
            return mChannelStr;
        }

        if (referrer.containsKey("utm_source")) {
            mUtmSource = referrer.get("utm_source");
        }

        if (referrer.containsKey("utm_content")) {
            mUtmContent = referrer.get("utm_content");
        }

        mChannelStr = new StringBuffer().append(mUtmSource).append("_").append(mUtmContent).toString();
        if (mChannelStr != null && mChannelStr.length() >= UMENG_CHANNEL_MAX_LENGTH) {
            mChannelStr = mChannelStr.substring(0, UMENG_CHANNEL_MAX_LENGTH);
        }
        return mChannelStr;
    }


    public static void showSoftInput(InputMethodManager inputManager, View v) {
        inputManager.showSoftInput(v, InputMethodManager.SHOW_FORCED);
    }

    public static void hideSoftInput(InputMethodManager inputManager, View v) {
        if (inputManager.isActive()) {
            inputManager.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
        }
    }

    //by highlight keywords, only action on first match
    public static SpannableString highlightKeywords(int color, String text, String keywords, boolean actionFirstMatch) {

        if (text != null && keywords != null && text.trim().length() == keywords.trim().length()) {
            return SpannableString.valueOf(text.trim());
        }

        SpannableString s = new SpannableString(text);
        Pattern p = Pattern.compile(keywords, Pattern.LITERAL);
        Matcher m = p.matcher(s);
        while (m.find()) {
            int end = m.end();
            try {
                if (s.charAt(end) != ' ') {
                    s.setSpan(new UnderlineSpan(), end, s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                } else {
                    s.setSpan(new UnderlineSpan(), end + 1, s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (actionFirstMatch) {
                break;
            }
        }
        return s;
    }


    public static boolean isIntentAvailable(Context context, Intent intent) {
        final PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent,
                PackageManager.GET_ACTIVITIES);
        return list.size() > 0;
    }

    public static int getStatusHeight(Context context) {

        int statusHeight = -1;
        try {
            Class<?> clazz = Class.forName("com.android.internal.R$dimen");
            Object object = clazz.newInstance();
            int height = Integer.parseInt(clazz.getField("status_bar_height")
                    .get(object).toString());
            statusHeight = context.getResources().getDimensionPixelSize(height);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statusHeight;
    }
    /**
     * 判断坐标是否在target View 中
     *
     * @param view
     * @param ev
     * @return
     */
    public static boolean inRangeOfView(View view, MotionEvent ev) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int x = location[0];
        int y = location[1];
        if (ev.getX() < x || ev.getX() > (x + view.getWidth()) || ev.getY() < y || ev.getY() > (y + view.getHeight())) {
            return false;
        }
        return true;
    }
    @TargetApi(14)
    public static boolean hasNavBar(Context context) {
        final String SHOW_NAV_BAR_RES_NAME = "config_showNavigationBar";
        String sNavBarOverride = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                Class c = Class.forName("android.os.SystemProperties");
                Method m = c.getDeclaredMethod("get", String.class);
                m.setAccessible(true);
                sNavBarOverride = (String) m.invoke(null, "qemu.hw.mainkeys");
            } catch (Throwable e) {
                sNavBarOverride = null;
            }
        }
        Resources res = context.getResources();
        int resourceId = res.getIdentifier(SHOW_NAV_BAR_RES_NAME, "bool",
                "android");
        if (resourceId != 0) {
            boolean hasNav = res.getBoolean(resourceId);
            // check override flag (see static block)
            if ("1".equals(sNavBarOverride)) {
                hasNav = false;
            } else if ("0".equals(sNavBarOverride)) {
                hasNav = true;
            }
            return hasNav;
        } else { // fallback
            return !ViewConfiguration.get(context).hasPermanentMenuKey();
        }
    }
    public static int getNavigationBarHeight(Context context) {
        int result = 0;
        try {
            final String NAV_BAR_HEIGHT_RES_NAME = "navigation_bar_height";
            final String NAV_BAR_HEIGHT_LANDSCAPE_RES_NAME = "navigation_bar_height_landscape";

            Resources res = context.getResources();
            final boolean mInPortrait = (res.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                if (hasNavBar(context)) {
                    String key;
                    if (mInPortrait) {
                        key = NAV_BAR_HEIGHT_RES_NAME;
                    } else {
                        key = NAV_BAR_HEIGHT_LANDSCAPE_RES_NAME;
                    }
                    return getInternalDimensionSize(res, key);
                }
            }
        }catch (Exception e){
            return 0;
        }
        return result;
    }
    public static int getInternalDimensionSize(Resources res, String key) {
        int result = 0;
        int resourceId = res.getIdentifier(key, "dimen", "android");
        if (resourceId > 0) {
            result = res.getDimensionPixelSize(resourceId);
        }
        return result;
    }
    /**
     * 生成范围Min到Max的随机双精度浮点数，如生成0.2323-10.90的数字，包含0.2323, 注意：不包含10.09。
     *
     * @param min 随机数范围下限
     * @param max 随机数范围上限
     * @return
     */
    public static double randomDouble(double min, double max) {
        return min + (max - min) * Math.random();
    }

    /**
     *
     * @param percent 百分之多少几率 如0.8就是百分之80
     * @return
     */
    public static boolean isInPercent(double percent){

        double randomResult = randomDouble(0.0,1.0);
        return percent >= randomResult;
    }
    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bd = (BitmapDrawable) drawable;
            return bd.getBitmap();
        }
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);
        return bitmap;
    }
    public static Bitmap getFbBitmap(Drawable drawable, int width, int height){
        Bitmap bitmap = drawableToBitmap(drawable);
        Bitmap resizeBitmap = ThumbnailUtils.extractThumbnail(bitmap,width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        return resizeBitmap;
    }
    /**
     * 判断程序是否安装
     */
    public static boolean isApkInstalled(Context context, String packageName) {
        if (TextUtils.isEmpty(packageName))
            return false;
        try {
            @SuppressWarnings("unused")
            ApplicationInfo info = context.getPackageManager().getApplicationInfo(packageName,
                    PackageManager.GET_UNINSTALLED_PACKAGES);
            return true;
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    public static boolean isForeground(Context context, String className) {
        if (context == null || TextUtils.isEmpty(className)) {
            return false;
        }

        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(1);
        if (list != null && list.size() > 0) {
            ComponentName cpn = list.get(0).topActivity;
            if (className.equals(cpn.getClassName())) {
                return true;
            }
        }

        return false;
    }


    public static final String PACKAGE_GP = "com.android.vending";
    public static final String CLASS_GP = "com.android.vending.AssetBrowserActivity";
    public static void startGpMarket(Context context, String url){
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        browserIntent.setClassName(PACKAGE_GP, CLASS_GP);
        browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(browserIntent);
    }
    /**
     * 根据包名启动app
     * @param context
     * @param packageName
     */
    public static void startActivityByPackageName(Context context, String packageName){
        try {
            Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
            context.startActivity(intent);
        }catch (Exception e){
            return;
        }
    }

    public static boolean externalMemoryAvailable() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }

    public static long getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        return stat.getAvailableBlocks() * blockSize;
    }

    public static long getTotalInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        return totalBlocks * blockSize;
    }

    public static long getAvailableExternalMemorySize() {
        if (externalMemoryAvailable()) {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSize();
            long availableBlocks = stat.getAvailableBlocks();
            return availableBlocks * blockSize;
        } else {
            return 0;
        }
    }

    public static long getTotalExternalMemorySize() {
        if (externalMemoryAvailable()) {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSize();
            long totalBlocks = stat.getBlockCount();
            return totalBlocks * blockSize;
        } else {
            return 0;
        }
    }

    public static int getStorageUsePercent() {
        long avaliableInternal = getAvailableInternalMemorySize();
        long totalInternal = getTotalInternalMemorySize();
        long avaliableExternal = getAvailableExternalMemorySize();
        long totalExternal = getTotalExternalMemorySize();
        int percent = 100 - (int) ((avaliableInternal + avaliableExternal) * 100 / (totalInternal + totalExternal));
        return percent;
    }
}
