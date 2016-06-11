package com.mmj.handdetectorcalling.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import com.mmj.handdetectorcalling.listeners.Listener;
import com.mmj.handdetectorcalling.listeners.UDPMessageListener;
import com.mmj.handdetectorcalling.listeners.interfaces.OnUDPReceiveMessage;
import com.mmj.handdetectorcalling.model.UDPMessage;
import com.mmj.handdetectorcalling.model.User;
import com.mmj.handdetectorcalling.ui.activity.VideoChatActivity;
import com.mmj.handdetectorcalling.utils.AppConstant;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 主要用来接收UDP消息，存储在messages中，当有消息来时，通知activity来获取
 */
public class ChatService extends Service implements OnUDPReceiveMessage {
    private final int SEND_FAILURE = 0XF0001;

    private final MyBinder myBinder = new MyBinder();
    //保存当前在线用户，键值为用户的ip
    final Map<String, User> users = new ConcurrentHashMap<String, User>();
    //保存用户发的消息，每个ip都会开启一个消息队列来缓存消息
    final Map<String, Queue<UDPMessage>> messages = new ConcurrentHashMap<String, Queue<UDPMessage>>();

    private UDPMessageListener listener = UDPMessageListener.getInstance(users, messages);

    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case SEND_FAILURE:
                    Toast.makeText(ChatService.this, "请检测wifi网络", Toast.LENGTH_SHORT).show();
                    break;
            }
        }

        ;
    };

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            listener.setOnReceiveMessage(this);
            listener.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected final void send(String msg, InetAddress destIp) {
        listener.send(msg, destIp);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }


    /**
     * 自定义的Binder类，
     * 通过这个类，让Activity获得与其绑定的Service对象
     */
    public final class MyBinder extends Binder {
        /**
         * 获得当前用户列表
         */
        public Map<String, User> getUsers() {
            return users;
        }

        /**
         * 获得当前缓存消息
         */
        public Map<String, Queue<UDPMessage>> getMessages() {
            return messages;
        }

        /**
         * 发送消息
         */
        public void sendMsg(UDPMessage msg, InetAddress destIp) {
            send(msg.toString(), destIp);
        }

        /**
         * 通知上线
         */
        public void noticeOnline() {
            listener.noticeOnline();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            listener.close();
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onReceive(int type) {
        switch (type) {
            case Listener.LOGIN_SUCC:
            case Listener.ADD_USER:
            case Listener.REMOVE_USER:
                sendBroadcast(new Intent(AppConstant.ACTION_ADD_USER));
                break;
            case Listener.ASK_VIDEO:
            case Listener.REPLAY_VIDEO_ALLOW:
            case Listener.REPLAY_VIDEO_NOT_ALLOW:
            case Listener.RECEIVE_MSG:
                sendBroadcast(new Intent(VideoChatActivity.MessageUpdateBroadcastReceiver.ACTION_NOTIFY_DATA));
                break;

        }
    }

    @Override
    public void sendFailure() {
        handler.sendEmptyMessage(SEND_FAILURE);
    }

}
