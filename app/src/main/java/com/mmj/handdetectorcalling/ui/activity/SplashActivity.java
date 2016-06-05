package com.mmj.handdetectorcalling.ui.activity;

import android.view.View;
import android.widget.Button;

import com.mmj.handdetectorcalling.R;
import com.mmj.handdetectorcalling.utils.SystUtils;

/**
 * Created by raomengyang on 6/5/16.
 */
public class SplashActivity extends BaseActivity implements View.OnClickListener {

    private Button cameraBtn;
    private Button callingBtn;


    @Override
    protected void initViews() {
        setContentView(R.layout.activity_splash);
        initWidget();
    }

    private void initWidget() {
        cameraBtn = (Button) findViewById(R.id.btn_gesture_camera);
        callingBtn = (Button) findViewById(R.id.btn_gesture_calling);
    }

    @Override
    protected void setListeners() {
        cameraBtn.setOnClickListener(this);
        callingBtn.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        super.initData();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_gesture_camera:
                SystUtils.intoActivity(SplashActivity.this, CameraGestureControlActivity.class);
                break;
            case R.id.btn_gesture_calling:
                SystUtils.intoActivity(SplashActivity.this, MainActivity.class);
                break;

            default:
                break;
        }
    }
}
