package com.mmj.handdetectorcalling.utils;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.mmj.handdetectorcalling.application.CustomApplication;

/**
 * Created by raomengyang on 6/5/16.
 */
public class SystUtils {

    private static final boolean ENABLE_LOG = true; // Debug开关
    public static final String TAG = SystUtils.class.getSimpleName() + "debug==>";
    public static final String O_TAG = "xxxxxxxxx, ";

    public static void showToast(String toastMessage) {
        Toast.makeText(CustomApplication.getContext(), toastMessage, Toast.LENGTH_SHORT).show();
    }

    public static void intoActivity(Activity thisActivity, Class<?> nextActivity) {
        Intent it = new Intent(thisActivity, nextActivity);
        thisActivity.startActivity(it);
    }


    public static void debugLog(String log) {
        if (log == null || !ENABLE_LOG) {
            return;
        }
        Log.e(TAG, log);
    }

    public static void o(String log) {
        debugLog(O_TAG + log);
    }
}
