package com.mmj.handdetectorcalling.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.mmj.handdetectorcalling.ui.activity.VideoChatActivity;


/**
 * 心跳包检测，只检测通信双方
 */
public class HeartBeatBroaadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent intent2 = new Intent();
        intent2.setAction(VideoChatActivity.MessageUpdateReceiver.ACTION_HEARTBEAT);
        context.sendBroadcast(intent2);
//		intent.getData().getSchemeSpecificPart();
        intent.getData();
    }
}
