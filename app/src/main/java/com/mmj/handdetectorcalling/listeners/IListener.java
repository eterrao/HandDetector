package com.mmj.handdetectorcalling.listeners;

import java.io.IOException;

public abstract class IListener extends Thread {

    public static final int ADD_USER = 1;//增加用户
    public static final int LOGIN_SUCC = 2;//增加用户成功
    public static final int REMOVE_USER = 3;//删除用户
    public static final int RECEIVE_MSG = 4;//接收消息
    public static final int HEART_BEAT = 6;//发送心跳包
    public static final int HEART_BEAT_REPLY = 7;//心跳包回复
    public static final int VIDEO_CHAT_REQUEST = 11;// 请求视频聊天
    public static final int VIDEO_CHAT_ALLOW = 12;// 接受视频邀请
    public static final int VIDEO_CHAT_NOT_ALLOW = 13;//拒绝接受视频聊天
    public static final int TO_ALL_MESSAGE = 14;//所有在线信息

    /**
     * 打开监听器
     */
    abstract void openListener() throws IOException;

    /**
     * 关闭监听器
     */
    abstract void closeListener() throws IOException;
}
