package com.mmj.handdetectorcalling.utils;

import android.content.Context;
import android.util.DisplayMetrics;

/**
 * Created by raomengyang on 6/11/16.
 */
public class ScreenUtils {
    public static int getScreenWidth(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        dm = context.getResources().getDisplayMetrics();
        return dm.widthPixels;
    }

    public static int getScreenHeight(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        dm = context.getResources().getDisplayMetrics();
        return dm.heightPixels;
    }
}
