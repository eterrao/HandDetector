package com.mmj.handdetectorcalling.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mmj.handdetectorcalling.R;
import com.mmj.handdetectorcalling.service.DaemonService;
import com.mmj.handdetectorcalling.utils.AppConstant;

/**
 * APP设计思路：
 * 1.打开APP后，启动Service，常驻后台；
 * 2.即使关闭屏幕，Service也依旧在后台运行，需要考虑电量消耗；
 * 3.可设置需要拨号的号码；
 * 4.可校准晃动的精准度；
 * 5.可设置熄屏状态下
 */

public class MainActivity extends Activity implements View.OnClickListener, SensorEventListener {

    private TextView phoneNumTV;
    private EditText phoneNumET;
    private Button okBtn;

    private SensorManager mSensorManager = null;
    private Vibrator mVibrator = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        setListener();
        String phoneNumTemp = getData();
        if (!TextUtils.isEmpty(phoneNumTemp)) {
            phoneNumTV.setText(phoneNumTemp);
        }
        initSensor();
        startDaemonService();
    }

    private void initView() {
        phoneNumTV = (TextView) findViewById(R.id.tv_phone_number);
        phoneNumET = (EditText) findViewById(R.id.et_phone_number_input);
        okBtn = (Button) findViewById(R.id.btn_ok);
    }

    private void setListener() {
        okBtn.setOnClickListener(this);
    }

    /**
     * 从偏好设置取数据
     */
    private String getData() {
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(AppConstant.PhoneNumberSP, MODE_PRIVATE);
        String phoneNumTemp = sharedPreferences.getString(AppConstant.PHONE_KEY, "0");
        return phoneNumTemp;
    }

    private void initSensor() {
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
    }

    private void startDaemonService() {
        Intent serviceIntent = new Intent(MainActivity.this, DaemonService.class);
        startService(serviceIntent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_ok:
                savePhoneNumber();
                break;
            default:
                break;
        }
    }


    /**
     * 保存电话号到偏好设置（数据持久化）
     */
    private void savePhoneNumber() {
        String phoneNumber = phoneNumET.getText().toString();
        if (!TextUtils.isEmpty(phoneNumber)) {
            SharedPreferences preferences = getApplicationContext().getSharedPreferences(AppConstant.PhoneNumberSP, MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(AppConstant.PHONE_KEY, phoneNumber);
            editor.commit();
            phoneNumTV.setText(phoneNumber + "");
        }
    }

    private void sensorRegister() {
        if (mSensorManager != null) {
            mSensorManager.registerListener(this,
                    mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    private void sensorUnregister() {
        if (mSensorManager != null) mSensorManager.unregisterListener(this);
    }

    private void callingPhone() {
        String phoneNumber = phoneNumTV.getText().toString();
        if (!TextUtils.isEmpty(phoneNumber)
                && !phoneNumber.equals("110")
                || !phoneNumber.equals("0")) {
            Intent callingIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber));
            startActivity(callingIntent);
        } else {
            Toast.makeText(MainActivity.this, "呼叫的号码不正确", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int sensorType = event.sensor.getType();
        //values[0]:X轴，values[1]：Y轴，values[2]：Z轴
        float[] values = event.values;
        if (sensorType == Sensor.TYPE_ACCELEROMETER) {
            if ((Math.abs(values[0]) > 17 || Math.abs(values[1]) > 17 || Math
                    .abs(values[2]) > 17)) {
                Log.e("sensor x ", "============ values[0] = " + values[0]);
                Log.e("sensor y ", "============ values[1] = " + values[1]);
                Log.e("sensor z ", "============ values[2] = " + values[2]);
                // 摇动手机后震动提示
                mVibrator.vibrate(500);
                // 随之呼叫号码
                callingPhone();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 注册sensor
        sensorRegister();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 解绑sensor
        sensorUnregister();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    public SensorManager getSensorManager() {
        return mSensorManager;
    }

    public Vibrator getVibrator() {
        return mVibrator;
    }
}
