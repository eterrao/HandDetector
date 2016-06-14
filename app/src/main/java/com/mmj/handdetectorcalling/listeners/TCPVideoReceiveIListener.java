package com.mmj.handdetectorcalling.listeners;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.mmj.handdetectorcalling.listeners.interfaces.OnCameraCallbackBitmapLoaded;
import com.mmj.handdetectorcalling.utils.AppConstant;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 视频监听
 * 通过TCP传输，避免UDP传输会丢帧的问题
 */
public class TCPVideoReceiveIListener extends TCPIListener {
    public static final int THREAD_COUNT = 80;//线程数

    private int port = AppConstant.VIDEO_PORT;
    //用来加载图片
    private ExecutorService executors = Executors.newFixedThreadPool(THREAD_COUNT);

    private OnCameraCallbackBitmapLoaded onCameraCallbackBitmapLoaded;

    boolean isReceived;//刚进来默认是正在接收数据的

    private static TCPVideoReceiveIListener instance;

    private TCPVideoReceiveIListener() {
    }

    public static TCPVideoReceiveIListener getInstance() {
        return instance == null ? instance = new TCPVideoReceiveIListener() : instance;
    }

    @Override
    void init() {
        setPort(port);
    }

    public void onReceiveData(final Socket socket) throws IOException {
        connectionReceive(socket);
    }

    private void connectionReceive(final Socket socket) {
        executors.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Bitmap bitmap = BitmapFactory.decodeStream(socket.getInputStream());
                    onCameraCallbackBitmapLoaded.onCameraCallbackBitmapLoaded(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public OnCameraCallbackBitmapLoaded getOnCameraCallbackBitmapLoaded() {
        return onCameraCallbackBitmapLoaded;
    }

    public void setOnCameraCallbackBitmapLoaded(OnCameraCallbackBitmapLoaded onCameraCallbackBitmapLoaded) {
        this.onCameraCallbackBitmapLoaded = onCameraCallbackBitmapLoaded;
    }

    @Override
    public void noticeReceiveError(Exception e) {
    }


    @Override
    public void noticeSendFileError(IOException e) {
    }

    @Override
    public void closeListener() throws IOException {
        super.closeListener();
        isReceived = false;
        executors.shutdownNow();
        instance = null;
    }
}
