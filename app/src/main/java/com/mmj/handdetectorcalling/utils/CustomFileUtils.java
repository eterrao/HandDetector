package com.mmj.handdetectorcalling.utils;

import android.os.Environment;

import com.mmj.handdetectorcalling.application.CustomApplication;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.os.Environment.isExternalStorageRemovable;

/**
 * Created by raomengyang on 6/5/16.
 */
public class CustomFileUtils {

    public static String createImageFile() {
        File file = new File(getCacheDir() + File.separator + getImageFileName());
        return file.getPath();
    }

    private static String getCacheDir() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !isExternalStorageRemovable()
                ? CustomApplication.getContext().getExternalCacheDir().getPath() :
                CustomApplication.getContext().getCacheDir().getPath();
    }

    private static String getImageFileName() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return "JPEG_" + timeStamp + ".jpg";
    }
}
