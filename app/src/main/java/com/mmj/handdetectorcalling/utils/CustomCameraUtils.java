package com.mmj.handdetectorcalling.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Created by raomengyang on 6/5/16.
 */
public class CustomCameraUtils {
    /**
     * 得到压缩图片
     * @param data
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static Bitmap decodeVideoBitmap(byte[] data,int reqHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 10 ;
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeByteArray(data, 0, data.length, options);
    }
}
