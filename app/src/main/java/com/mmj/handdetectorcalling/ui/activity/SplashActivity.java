package com.mmj.handdetectorcalling.ui.activity;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.mmj.handdetectorcalling.R;
import com.mmj.handdetectorcalling.utils.SystUtils;

/**
 * Created by raomengyang on 6/5/16.
 */
public class SplashActivity extends BaseActivity implements View.OnClickListener {

    private ImageView callingIV;
    private ImageView videoIV;


    @Override
    protected void initViews() {
        setContentView(R.layout.activity_splash);
        initWidget();
    }

    private void initWidget() {
        callingIV = (ImageView) findViewById(R.id.btn_gesture_calling);
        videoIV = (ImageView) findViewById(R.id.btn_video);
    }

    @Override
    protected void setListeners() {
        callingIV.setOnClickListener(this);
        videoIV.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        super.initData();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_gesture_camera:

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
