package com.mmj.handdetectorcalling.listeners;

import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public abstract class TCPIListener extends IListener {

    private int port;
    private boolean go;
    //用来接收数据
    private ServerSocket server;
    //标识是否开启
    private boolean running;

    /**
     * 初始化操作
     * 设置保存路径
     * 端口初始化
     */
    abstract void init();

    private void createServer() throws IOException {
        init();
        server = new ServerSocket(port);
        go = true;
        running = true;
        start();
    }


    @Override
    public void run() {
        Log.d("TCPListener", "开启TCP监听器");
        while (go) {
            try {
                onReceiveData(server.accept());
            } catch (Exception e) {
                e.printStackTrace();
                noticeReceiveError(e);
            }
        }
        running = false;
        try {
            if (server != null)
                server.close();
            server = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public abstract void onReceiveData(Socket socket) throws IOException;

    /**
     * 通知用户接收文件出错
     */
    public abstract void noticeReceiveError(Exception e);

    /**
     * 通知用户发送文件出错
     */
    public abstract void noticeSendFileError(IOException e);


    @Override
    public void openListener() throws IOException {
        createServer();
        setPriority(MAX_PRIORITY);
    }

    @Override
    public void closeListener() throws IOException {
        go = false;
        running = false;
        interrupt();
        if (server != null) server.close();
        server = null;
    }

    public boolean isRunning() {
        return running;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

}
