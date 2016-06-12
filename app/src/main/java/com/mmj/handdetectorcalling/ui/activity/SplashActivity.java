package com.mmj.handdetectorcalling.ui.activity;

import android.view.View;
import android.widget.ImageView;

import com.mmj.handdetectorcalling.R;
import com.mmj.handdetectorcalling.utils.SystUtils;

/**
 * Created by raomengyang on 6/5/16.
 */
public class SplashActivity extends BaseActivity implements View.OnClickListener {

    private ImageView callingIV;
    private ImageView videoIV;

    /**
     * 相应的onCreate等Activity的生命周期已经封装在BaseActivity中
     */
    @Override
    protected void initViews() {
        setContentView(R.layout.activity_splash);
        initWidget();
    }

    /**
     * 对UI控件进行初始化
     */
    private void initWidget() {
        callingIV = (ImageView) findViewById(R.id.btn_gesture_calling);
        videoIV = (ImageView) findViewById(R.id.btn_video);
    }

    /**
     * 设置UI控件的点击监听
     */
    @Override
    protected void setListeners() {
        callingIV.setOnClickListener(this);
        videoIV.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        super.initData();
    }


    /**
     * UI控件的点击事件
     * 进入对应的Activity
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_gesture_calling:
                SystUtils.intoActivity(SplashActivity.this, CallingActivity.class);
                break;

            case R.id.btn_video:
                SystUtils.intoActivity(SplashActivity.this, VideoChatActivity.class);
                break;
            default:
                break;
        }
    }
}
