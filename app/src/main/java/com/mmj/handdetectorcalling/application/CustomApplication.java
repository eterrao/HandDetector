package com.mmj.handdetectorcalling.application;

import android.app.Application;
import android.content.Context;

/**
 * Created by raomengyang on 6/5/16.
 */
public class CustomApplication extends Application {

//    // 加载JNI的依赖包: 名为libopencv_java3.so
//    static {
//        System.loadLibrary("opencv_java3");
//    }

    private static Context mContext = null; // 构造一个全局的Application级别的全局Context

    @Override
    public void onCreate() {
        super.onCreate();
        initContext();
    }

    private void initContext() {
        this.mContext = getApplicationContext();
    }

    public static Context getContext() {
        if (mContext != null) return mContext;
        else throw new NullPointerException("Log: application context is null!");
    }


}
