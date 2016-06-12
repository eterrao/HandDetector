package com.mmj.handdetectorcalling.application;

import android.app.Application;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.mmj.handdetectorcalling.model.UDPMessageBean;
import com.mmj.handdetectorcalling.utils.SystUtils;

import org.apache.http.conn.util.InetAddressUtils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Created by raomengyang on 6/5/16.
 */
public class CustomApplication extends Application {

    private static Context mContext = null; // 构造一个全局的Application级别的全局Context
    public static CustomApplication customApplication = null;
    private String localIp;
    private String deviceCode;

    @Override
    public void onCreate() {
        super.onCreate();
        initContext();
        localIp = getLocalIpAddress();
        getDeviceId();
    }

    private void initContext() {
        this.mContext = getApplicationContext();
        this.customApplication = this;
    }

    public static Context getContext() {
        if (mContext != null) return mContext;
        else throw new NullPointerException("Log: application context is null!");
    }

    public static CustomApplication getCustomApplication() {
        if (customApplication != null) return customApplication;
        else throw new NullPointerException("Log: CustomApplication is null!");
    }


    /**
     * 得到本机IP地址
     */
    public String getLocalIpAddress() {
        try {
            //获得当前可用的wifi网络
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            while (en.hasMoreElements()) {
                NetworkInterface nif = en.nextElement();
                Enumeration<InetAddress> enumIpAddr = nif.getInetAddresses();
                while (enumIpAddr.hasMoreElements()) {
                    InetAddress mInetAddress = enumIpAddr.nextElement();
                    if (!mInetAddress.isLoopbackAddress() && InetAddressUtils.isIPv4Address(mInetAddress.getHostAddress())) {
                        return mInetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
            Toast.makeText(this, "获取本机IP地址失败", Toast.LENGTH_SHORT).show();
        }
        return null;
    }

    /**
     * 获取设备唯一标识
     */
    private void getDeviceId() {
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
//		 String imsi=telephonyManager.getSubscriberId();
        deviceCode = telephonyManager.getDeviceId();
        SystUtils.o("DeviceId  :" + deviceCode);
        if (deviceCode == null) {
            WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            WifiInfo info = wifi.getConnectionInfo();
            deviceCode = info.getMacAddress();
        }
        if (deviceCode == null) {
            deviceCode = getSharedPreferences("me", 0).getString("deviceCode", System.currentTimeMillis() + "");
            getSharedPreferences("me", 0).edit().putString("deviceCode", deviceCode).commit();
        }
    }

    public UDPMessageBean getMyUdpMessage(String msg, int type) {
        UDPMessageBean message = new UDPMessageBean();
        message.setType(type);
        message.setMsg(msg);
        message.setOwn(true);
        return message;
    }

    public String getLocalIp() {
        if (localIp == null)
            localIp = getLocalIpAddress();
        return localIp;
    }

    public void setLocalIp(String localIp) {
        this.localIp = localIp;
    }

}
