package com.mmj.handdetectorcalling.listeners;

import com.mmj.handdetectorcalling.application.CustomApplication;
import com.mmj.handdetectorcalling.listeners.interfaces.OnUDPReceiveMessage;
import com.mmj.handdetectorcalling.model.UDPMessageBean;
import com.mmj.handdetectorcalling.model.UserBean;
import com.mmj.handdetectorcalling.utils.AppConstant;
import com.mmj.handdetectorcalling.utils.SystUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 文本消息收发器
 */
public class UDPMessageIListener extends UDPIListener {

    //文本消息监听端口
    private final int port = AppConstant.MESSAGE_PORT;
    private final int BUFFER_SIZE = 1024 * 3;//3k的数据缓冲区
    private OnUDPReceiveMessage onReceiveMessage;
    //保存当前在线用户，键值为用户的ip
    final Map<String, UserBean> users;
    //保存用户发的消息，每个ip都会开启一个消息队列来缓存消息
    final Map<String, Queue<UDPMessageBean>> messages;

    private static UDPMessageIListener instance;


    private UDPMessageIListener(Map<String, UserBean> users, Map<String, Queue<UDPMessageBean>> messages) {
        this.users = users;
        this.messages = messages;
    }

    public static UDPMessageIListener getInstance(Map<String, UserBean> users, Map<String, Queue<UDPMessageBean>> messages) {
        return instance == null ? instance = new UDPMessageIListener(users, messages) : instance;
    }

    @Override
    void init() {
        setPort(port);
        setBufferSize(BUFFER_SIZE);
    }

    @Override
    public void onReceive(byte[] data, DatagramPacket packet) {
        try {
            String temp = new String(data, 0, packet.getLength(), AppConstant.ENCODE_FORMAT);//得到接收的消息
            UDPMessageBean msg = new UDPMessageBean(new JSONObject(temp));
            SystUtils.o("收到消息：" + msg.toString());
            String sourceIp = packet.getAddress().getHostAddress();//对方ip
            int type = msg.getType();
            switch (type) {
                case ADD_USER://增加一个用户
                    UserBean userBean = new UserBean();
                    userBean.setIp(sourceIp);
                    //构造回送报文内容
                    if (!CustomApplication.getCustomApplication().getLocalIp().equals(userBean.getIp())) {
                        users.put(sourceIp, userBean);
                        send(CustomApplication.getCustomApplication().getMyUdpMessage("", LOGIN_SUCC).toString(), packet.getAddress());
                    }
                    break;

                case LOGIN_SUCC://在对方登陆成功后返回的验证消息
                    userBean = new UserBean();
                    userBean.setIp(sourceIp);
                    users.put(sourceIp, userBean);
                    break;

                case REMOVE_USER://删除用户
                    users.remove(sourceIp);
                    break;

                case ASK_VIDEO:
                case REPLAY_VIDEO_ALLOW:
                case REPLAY_VIDEO_NOT_ALLOW:
                case RECEIVE_MSG://接收到消息
                    if (messages.containsKey(sourceIp)) {
                        messages.get(sourceIp).add(msg);//更新现有
                    } else {
                        Queue<UDPMessageBean> queue = new ConcurrentLinkedQueue<UDPMessageBean>();
                        queue.add(msg);
                        messages.put(sourceIp, queue);//新增
                    }
                    break;

                case TO_ALL_MESSAGE://message to all
                    if (messages.containsKey(AppConstant.ALL_ADDRESS)) {
                        messages.get(AppConstant.ALL_ADDRESS).add(msg);//更新现有
                    } else {
                        Queue<UDPMessageBean> queue = new ConcurrentLinkedQueue<UDPMessageBean>();
                        queue.add(msg);
                        messages.put(AppConstant.ALL_ADDRESS, queue);//新增
                    }
                    break;

                case HEART_BEAT://心跳包检测
                    send(CustomApplication.getCustomApplication().getMyUdpMessage("", HEART_BEAT_REPLY).toString(), packet.getAddress());//回复心跳包
                    userBean = users.get(sourceIp);
                    if (userBean != null) {
                        userBean.setHeartTime(System.currentTimeMillis() + "");
                        SystUtils.o("接收心跳包：" + userBean.getHeartTime());
                    }
                    break;

                case HEART_BEAT_REPLY://接收到心跳包
                    userBean = users.get(sourceIp);
                    if (userBean != null)
                        userBean.setHeartTime(System.currentTimeMillis() + "");//更新心跳包的最后时间
                    break;
            }
            if (onReceiveMessage != null)
                onReceiveMessage.onReceive(type);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @Override
    void noticeOffline() {
        try {
            send(CustomApplication.getCustomApplication().getMyUdpMessage("", REMOVE_USER).toString(), InetAddress.getByName(AppConstant.ALL_ADDRESS));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void noticeOnline() {
        try {
            send(CustomApplication.getCustomApplication().getMyUdpMessage("", ADD_USER).toString(), InetAddress.getByName(AppConstant.ALL_ADDRESS));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送UDP数据包
     *
     * @param msg      消息
     * @param destIp   目标地址
     * @param destPort 目标端口
     * @throws IOException
     */
    public void send(String msg, InetAddress destIp) {
        SystUtils.o("发送消息：" + msg);
        send(msg, destIp, AppConstant.MESSAGE_PORT);
    }

    public OnUDPReceiveMessage getOnReceiveMessage() {
        return onReceiveMessage;
    }

    public void setOnReceiveMessage(OnUDPReceiveMessage onReceiveMessage) {
        this.onReceiveMessage = onReceiveMessage;
    }

    @Override
    public void close() throws IOException {
        super.close();
        //这个一定要置空，不然会出现already start的bug,因为instance是static的，程序退出后，当前dvm还在，还是会保持对原有变量的引用
        instance = null;
        if (users != null) users.clear();
        if (messages != null) messages.clear();
    }

    @Override
    void sendMsgFailure() {
        if (onReceiveMessage != null)
            onReceiveMessage.sendFailure();
    }

}
