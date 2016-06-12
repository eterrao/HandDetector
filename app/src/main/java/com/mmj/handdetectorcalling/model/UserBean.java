package com.mmj.handdetectorcalling.model;

import java.io.Serializable;

/**
 * 用户实体类
 */
public class UserBean implements Serializable {
    private static final long serialVersionUID = -5062775818842005386L;

    public static final int INTERVAL = 10 * 1000;//心跳包间隔时间
    public static final int TIMEOUT = (int) (2.1 * INTERVAL);//超时时间
    private String ip;            //ip地址
    private String heartTime;//记录心跳包的最后一次时间

    public UserBean() {
        heartTime = String.valueOf(System.currentTimeMillis());
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getHeartTime() {
        return heartTime;
    }

    public void setHeartTime(String heartTime) {
        this.heartTime = heartTime;
    }

    /**
     * 验证对方是否在线
     *
     * @return
     */
    public boolean checkOnline() {
        return !(System.currentTimeMillis() - Long.valueOf(heartTime) > TIMEOUT);
    }
}
