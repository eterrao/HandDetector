package com.mmj.handdetectorcalling.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.IBinder;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.mmj.handdetectorcalling.R;
import com.mmj.handdetectorcalling.ui.activity.MainActivity;
import com.mmj.handdetectorcalling.utils.AppConstant;

/**
 * Created by raomengyang on 5/10/16.
 */
public class DaemonService extends Service implements SensorEventListener {


    private SensorManager mSensorManager = null;
    private Vibrator mVibrator = null;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        initNotification();
        initSensor();
        sensorRegister();

    }

    private void initSensor() {
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
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
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(AppConstant.PhoneNumberSP, MODE_PRIVATE);
        String phoneNumber = sharedPreferences.getString(AppConstant.PHONE_KEY, "0");
        Log.e("tag", phoneNumber);
        if (!TextUtils.isEmpty(phoneNumber)
                && !phoneNumber.equals("110")
                || !phoneNumber.equals("0")) {
            Intent callingIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber));
            callingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(callingIntent);
        } else {
            Toast.makeText(getApplicationContext(), "呼叫的号码不正确", Toast.LENGTH_SHORT).show();
        }
    }

    private void initNotification() {
        Notification notification = new Notification(R.drawable.icon, "HandDetectorCalling", System.currentTimeMillis());
        Intent notificationIT = new Intent(DaemonService.this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIT, 0);
        notification.setLatestEventInfo(this, "HandDetectorCalling", "正在后台运行", pendingIntent);
        startForeground(1, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }


                }
            }
        }).start();
        return super.onStartCommand(intent, flags, startId);
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
    public void onDestroy() {
        super.onDestroy();
        sensorUnregister();
    }
}
