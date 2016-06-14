package com.mmj.handdetectorcalling.listeners.interfaces;

import android.graphics.Bitmap;

public interface OnCameraCallbackBitmapLoaded {
    /**
     * 视频传送过来图片加载完成的回调方法
     */
    void onCameraCallbackBitmapLoaded(Bitmap bitmap);
}
