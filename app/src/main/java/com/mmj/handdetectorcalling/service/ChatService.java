package com.mmj.handdetectorcalling.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import com.mmj.handdetectorcalling.listeners.IListener;
import com.mmj.handdetectorcalling.listeners.UDPMessageIListener;
import com.mmj.handdetectorcalling.listeners.interfaces.OnUDPReceiveMessage;
import com.mmj.handdetectorcalling.model.UDPMessageBean;
import com.mmj.handdetectorcalling.model.UserBean;
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

    private final CustomBinder customBinder = new CustomBinder();
    //保存当前在线用户，键值为用户的ip
    final Map<String, UserBean> users = new ConcurrentHashMap<String, UserBean>();
    //保存用户发的消息，每个ip都会开启一个消息队列来缓存消息
    final Map<String, Queue<UDPMessageBean>> messages = new ConcurrentHashMap<String, Queue<UDPMessageBean>>();

    private UDPMessageIListener listener = UDPMessageIListener.getInstance(users, messages);

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
            listener.openListener();
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
        return customBinder;
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
    public final class CustomBinder extends Binder {
        /**
         * 获得当前用户列表
         */
        public Map<String, UserBean> getUsers() {
            return users;
        }

        /**
         * 获得当前缓存消息
         */
        public Map<String, Queue<UDPMessageBean>> getMessages() {
            return messages;
        }

        /**
         * 发送消息
         */
        public void sendMsg(UDPMessageBean msg, InetAddress destIp) {
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
            listener.closeListener();
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onReceive(int type) {
        switch (type) {
            case IListener.LOGIN_SUCC:
            case IListener.ADD_USER:
            case IListener.REMOVE_USER:
                sendBroadcast(new Intent(AppConstant.ACTION_ADD_USER));
                break;
            case IListener.VIDEO_CHAT_REQUEST:
            case IListener.VIDEO_CHAT_ALLOW:
            case IListener.VIDEO_CHAT_NOT_ALLOW:
            case IListener.RECEIVE_MSG:
                sendBroadcast(new Intent(VideoChatActivity.MessageUpdateReceiver.ACTION_NOTIFY_DATA));
                break;

        }
    }

    @Override
    public void sendFailure() {
        handler.sendEmptyMessage(SEND_FAILURE);
    }

}
