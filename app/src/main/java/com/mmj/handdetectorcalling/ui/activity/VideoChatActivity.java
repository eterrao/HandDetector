package com.mmj.handdetectorcalling.ui.activity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.os.Handler;
import android.os.IBinder;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.mmj.handdetectorcalling.R;
import com.mmj.handdetectorcalling.application.CustomApplication;
import com.mmj.handdetectorcalling.listeners.IListener;
import com.mmj.handdetectorcalling.listeners.TCPVideoReceiveIListener;
import com.mmj.handdetectorcalling.listeners.UDPVoiceIListener;
import com.mmj.handdetectorcalling.listeners.interfaces.OnBitmapLoaded;
import com.mmj.handdetectorcalling.model.UDPMessageBean;
import com.mmj.handdetectorcalling.model.UserBean;
import com.mmj.handdetectorcalling.service.ChatService;
import com.mmj.handdetectorcalling.service.HeartBeatBroaadcastReceiver;
import com.mmj.handdetectorcalling.ui.view.CustomVideoView;
import com.mmj.handdetectorcalling.utils.AppConstant;
import com.mmj.handdetectorcalling.utils.CustomCameraUtils;
import com.mmj.handdetectorcalling.utils.ScreenUtils;
import com.mmj.handdetectorcalling.utils.SystUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by raomengyang on 6/11/16.
 */
public class VideoChatActivity extends BaseActivity implements View.OnClickListener, SurfaceHolder.Callback, Camera.PreviewCallback, OnBitmapLoaded {

    private SurfaceView mSurfaceView;
    private CustomVideoView mCustomVideoView;
    private PopupWindow popupWindow;
    private Button connectBtn;
    private TextView connectIPTV;

    private SurfaceHolder mSurfaceHolder;
    private Camera mCamera;

    private CustomServiceConnection connection;
    private UserBean chatter;//对方聊天人
    private ChatService.MyBinder binder;

    private final int SHOW_DIALOG = 0XF1001;
    private final int REFRESH = 0XF1002;


    private String ipAddress;//记录当前用户ip

    private List<UDPMessageBean> myMessages = new ArrayList<UDPMessageBean>();//保存聊天信息
    private List<UserBean> userBeen = new ArrayList<UserBean>();

    private AlarmManager heartBeatManager; // 用来发送心跳包
    private PendingIntent pendingIntent;
    private boolean viewRenderFinished; // 用来标识控件是否渲染完毕
    private boolean activityNotFocused; // 标识activity被遮挡
    private boolean binded;

    private int width;  //宽度
    private int height;
    private int previewFormat;
    private boolean shouldFullScreen = false;

    private TCPVideoReceiveIListener videoReceiveListener;
    private UDPVoiceIListener voiceListener;
    private boolean voiceListenerOpened = false;

    private MessageUpdateReceiver messageUpdateReceiver = new MessageUpdateReceiver();

    private UserBroadcastReceiver userBroadcastReceiver = new UserBroadcastReceiver();


    //线程池，用来发送图片数据
    private ExecutorService executors = Executors.newFixedThreadPool(TCPVideoReceiveIListener.THREAD_COUNT);
    private int port = AppConstant.VIDEO_PORT;

    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case SHOW_DIALOG:
                    if (popupWindow != null)
                        popupWindow.showAtLocation(mSurfaceView, Gravity.CENTER, 0, 0);
                    break;
                case REFRESH:
                    mCustomVideoView.setDisplayCallBackBitmap((Bitmap) msg.obj);
                    break;
            }
        }
    };

    @Override
    protected void initViews() {
        setContentView(R.layout.activity_video);
        findViews();
    }

    private void findViews() {
        mSurfaceView = (SurfaceView) findViewById(R.id.sv_video_me);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mSurfaceHolder.addCallback(this);
        mCustomVideoView = (CustomVideoView) findViewById(R.id.cvv_video_u);
        connectBtn = (Button) findViewById(R.id.btn_connect);
        connectIPTV = (TextView) findViewById(R.id.tv_connect_ip);
    }

    @Override
    protected void setListeners() {
        connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMsg(CustomApplication.getCustomApplication().getMyUdpMessage("", IListener.ASK_VIDEO));
                SystUtils.showToast("已发送请求，对方同意后自动进行视屏聊天");
            }
        });
        mSurfaceView.setOnClickListener(this);
        mCustomVideoView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sv_video_me:
                break;

            case R.id.cvv_video_u:
                if (!shouldFullScreen) {
                    shouldFullScreen = true;
                    ViewGroup.LayoutParams layoutParams = mCustomVideoView.getLayoutParams();
                    layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
                    layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                    mCustomVideoView.setLayoutParams(layoutParams);
                } else {
                    shouldFullScreen = false;
                    ViewGroup.LayoutParams layoutParams = mCustomVideoView.getLayoutParams();
                    layoutParams.height = ScreenUtils.px2dp(ScreenUtils.getScreenHeight(this) / 4, this);
                    layoutParams.width = ScreenUtils.px2dp(ScreenUtils.getScreenWidth(this) / 4, this);
                    mCustomVideoView.setLayoutParams(layoutParams);
                }
                break;
        }
    }

    @Override
    protected void initData() {
        super.initData();
        init();
        String ip = CustomApplication.getCustomApplication().getLocalIp();
        if (ip == null) {
            SystUtils.showToast("请检测wifi");
            return;
        } else ipAddress = ip;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    videoReceiveListener = TCPVideoReceiveIListener.getInstance();
                    videoReceiveListener.setBitmapLoaded(VideoChatActivity.this);
                    if (!videoReceiveListener.isRunning())
                        videoReceiveListener.open();//先监听端口，然后连接
                } catch (IOException e1) {
                    e1.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            SystUtils.showToast("非常抱歉,视屏连接失败");
                            finish();
                        }
                    });
                }
            }
        }).start();
    }


    private void init() {
        //绑定到service
        Intent intent = new Intent(VideoChatActivity.this, ChatService.class);
        bindService(intent, connection = new CustomServiceConnection(), Context.BIND_AUTO_CREATE);
        //注册更新广播
        IntentFilter broadcastFilter = new IntentFilter();
        broadcastFilter.addAction(MessageUpdateReceiver.ACTION_NOTIFY_DATA);
        broadcastFilter.addAction(MessageUpdateReceiver.ACTION_HEARTBEAT);
        IntentFilter addUserFilter = new IntentFilter(AppConstant.ACTION_ADD_USER);
        registerReceiver(messageUpdateReceiver, broadcastFilter); // 注册对应的两个广播接收者
        registerReceiver(userBroadcastReceiver, addUserFilter);

        heartBeatManager = (AlarmManager) getSystemService(ALARM_SERVICE); //开启心跳包
        pendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(this, HeartBeatBroaadcastReceiver.class), 0);
        heartBeatManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), UserBean.INTERVAL, pendingIntent);
    }


    /**
     * 实现SurfaceHolder的三个抽象方法
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        int cameras = Camera.getNumberOfCameras();
        Camera.CameraInfo info = new Camera.CameraInfo();
        for (int i = 0; i < cameras; i++) {
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                mCamera = Camera.open(i);
                break;
            }
        }
        //没有前置摄像头
        if (mCamera == null) mCamera = Camera.open();
        try {
            mCamera.setPreviewDisplay(mSurfaceHolder);
            mCamera.setPreviewCallback(this);
        } catch (Exception e) {
            mCamera.release();//释放资源
            mCamera = null;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Camera.Parameters parameters = mCamera.getParameters();//得到相机设置参数
        Camera.Size size = mCamera.getParameters().getPreviewSize(); //获取预览大小
        this.width = size.width;
        this.height = size.height;
        parameters.setPictureFormat(PixelFormat.JPEG);//设置图片格式
        previewFormat = parameters.getPreviewFormat();
        setDisplayOrientation(mCamera, 90);
        mCamera.setPreviewCallback(this);
        mCamera.setParameters(parameters);
        mCamera.startPreview();//开始预览
    }


    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    protected void setDisplayOrientation(Camera camera, int angle) {
        try {
            Method downPolymorphic = camera.getClass().getMethod("setDisplayOrientation", new Class[]{int.class});
            if (downPolymorphic != null)
                downPolymorphic.invoke(camera, new Object[]{angle});
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }


    @Override
    public void onPreviewFrame(final byte[] data, Camera camera) {
        executors.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket socket = new Socket(InetAddress.getByName(ipAddress), port);
                    OutputStream out = socket.getOutputStream();

                    YuvImage image = new YuvImage(data, previewFormat, width, height, null);
                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    Rect rect = new Rect(0, 0, width, height);
                    //1：将YUV数据格式转化成jpeg
                    if (!image.compressToJpeg(rect, 100, os)) return;
                    //2：将得到的字节数组压缩成bitmap
                    Bitmap bmp = CustomCameraUtils.decodeVideoBitmap(os.toByteArray(), 200);
                    Matrix matrix = new Matrix();
                    matrix.setRotate(-90);
                    //3：旋转90
                    bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
                    //4：将最后的bitmap转化为字节流发送
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    out.write(baos.toByteArray());
                    out.flush();
                    out.close();
                    socket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onBitmapLoaded(Bitmap bitmap) {
        handler.obtainMessage(REFRESH, bitmap).sendToTarget();
        if (activityNotFocused) {
            try {
                //代码实现模拟用户按下back键
                String keyCommand = "input keyevent " + KeyEvent.KEYCODE_BACK;
                Runtime runtime = Runtime.getRuntime();
                runtime.exec(keyCommand);
                activityNotFocused = false;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * socket池，用来缓存
     */
    @Deprecated
    class SocketPool extends Thread {
        private List<Socket> sockets = new LinkedList<Socket>();
        private final int poolSize = 30;
        private boolean go = true;

        @Override
        public void run() {
            InetAddress address = null;
            try {
                address = InetAddress.getByName(ipAddress);
                while (go) {
                    int count = sockets.size();
                    if (count < poolSize) {
                        for (int i = 0; i < poolSize - count; i++) {
                            sockets.add(new Socket(address, port));
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public Socket getSocket() {
            if (!sockets.isEmpty()) {
                Socket socket = sockets.get(0);
                sockets.remove(0);
                return socket;
            }
            return null;
        }

        public void close() {
            go = false;
            for (Socket socket : sockets) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 显示提醒框
     */
    private void showDialog(String txt, View.OnClickListener ok, View.OnClickListener cancl, boolean buttonShow) {
        if (popupWindow != null)
            popupWindow.dismiss();
        popupWindow = new PopupWindow(getApplicationContext());
        int windowWdith = ScreenUtils.getScreenWidth(CustomApplication.getContext()) * 3 / 4;
        int windowHeight = ScreenUtils.getScreenHeight(CustomApplication.getContext()) * 2 / 7;
        popupWindow.setWidth(windowWdith);
//        popupWindow.setHeight(windowHeight);
        popupWindow.setFocusable(false);
        popupWindow.setOutsideTouchable(false);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());// 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景
        View view = getLayoutInflater().inflate(R.layout.dialog_notice, null);
        TextView textView = (TextView) view.findViewById(R.id.tv_push_notice_title);
        TextView cancle = (TextView) view.findViewById(R.id.btn_ignore_push_notice);
        TextView confirm = (TextView) view.findViewById(R.id.btn_into_push_notice);
        if (!buttonShow) {
            confirm.setVisibility(View.INVISIBLE);
            cancle.setVisibility(View.INVISIBLE);
        } else {
            confirm.setOnClickListener(ok);
            cancle.setOnClickListener(cancl);
        }
        popupWindow.setContentView(view);
        textView.setText(txt);
        if (viewRenderFinished)//Activity已经渲染完毕
            popupWindow.showAtLocation(mSurfaceView, Gravity.CENTER, 0, 0);
        else {//Activity还未渲染完毕
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (!viewRenderFinished) {
                            Thread.sleep(500);
                        }
                        handler.sendEmptyMessage(SHOW_DIALOG);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    /**
     * 从后台遍历消息
     *
     * @param queue
     */
    private void ergodicMessage(Queue<UDPMessageBean> queue) {
        Iterator<UDPMessageBean> iterator = queue.iterator();
        UDPMessageBean message;
        while (iterator.hasNext()) {
            message = iterator.next();
            switch (message.getType()) {
                case IListener.RECEIVE_MSG:
                    myMessages.add(message);
                    break;
                case IListener.ASK_VIDEO:
                    showDialog("对方请求视屏,同意吗？", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            sendMsg(CustomApplication.getCustomApplication().getMyUdpMessage("", IListener.REPLAY_VIDEO_ALLOW));
                            if (popupWindow != null) popupWindow.dismiss();
                        }
                    }, new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            sendMsg(CustomApplication.getCustomApplication().getMyUdpMessage("", IListener.REPLAY_VIDEO_NOT_ALLOW));
                            if (popupWindow != null) popupWindow.dismiss();
                        }
                    }, true);
                    break;
                case IListener.REPLAY_VIDEO_ALLOW:
                    if (popupWindow != null) popupWindow.dismiss();
                    break;
                case IListener.REPLAY_VIDEO_NOT_ALLOW:
                    SystUtils.showToast("对方拒绝视屏");
                    break;
            }
        }
        queue.clear();
    }

    /**
     * 发送消息
     */
    private void sendMsg(UDPMessageBean msg) {
        if (binder != null) {
            if (CustomApplication.getCustomApplication().getLocalIp().equals(ipAddress)) {
                if (chatter == null) {
                    chatter = new UserBean();
                    chatter.setIp(ipAddress);
                }
            } else {
                chatter = binder.getUsers().get(ipAddress);
            }
            //对方下线 ||（在线&&心跳包检测超时）—>网络断开
            if (chatter == null || (chatter != null && !chatter.checkOnline())) {
                SystUtils.showToast("对方已不在线");
                binder.getUsers().remove(ipAddress);
                sendBroadcast(new Intent(AppConstant.ACTION_ADD_USER));
            }
            try {
                if (chatter != null)
                    binder.sendMsg(msg, InetAddress.getByName(ipAddress));
                if (IListener.RECEIVE_MSG == Integer.valueOf(msg.getType()))//如果是文本消息
                    myMessages.add(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            unbindService(connection);
            Intent intent = new Intent(VideoChatActivity.this, ChatService.class);
            bindService(intent, connection = new CustomServiceConnection(), Context.BIND_AUTO_CREATE);
            Toast.makeText(this, "未发送出去,请重新发送", Toast.LENGTH_SHORT).show();
        }
    }

    class UserBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (binder != null) {
                userBeen.clear();
                Set<Map.Entry<String, UserBean>> set = binder.getUsers().entrySet();
                for (Map.Entry<String, UserBean> entry : set)
                    userBeen.add(entry.getValue());
                if (userBeen.size() > 0) {
                    connectIPTV.setText(userBeen.get(0).getIp() + "");
                    ipAddress = userBeen.get(0).getIp();
                    if (!voiceListenerOpened) openVoice();
                }
            } else {
                unbindService(connection);
                binded = false;
                bindService(new Intent(VideoChatActivity.this, ChatService.class), connection = new CustomServiceConnection(), Context.BIND_AUTO_CREATE);
            }
        }

    }

    public class CustomServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            binder = (ChatService.MyBinder) service;
            if (CustomApplication.getCustomApplication().getLocalIp().equals(ipAddress)) {
                chatter = new UserBean();
                chatter.setIp(ipAddress);
            } else chatter = binder.getUsers().get(ipAddress);
            Queue<UDPMessageBean> queue = binder.getMessages().get(chatter.getIp());
            if (queue != null) ergodicMessage(queue);//从后台遍历读取数据
            viewRenderFinished = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }

    }

    public class MessageUpdateReceiver extends BroadcastReceiver {
        public static final String ACTION_HEARTBEAT = "com.mmj.handdetectorcalling.heartbeat";
        public static final String ACTION_NOTIFY_DATA = "com.mmj.handdetectorcalling.notifydata";

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && ACTION_HEARTBEAT.equals(intent.getAction())) {//心跳包检测
                if (binder != null)
                    try {
                        binder.sendMsg(CustomApplication.getCustomApplication()
                                        .getMyUdpMessage("", IListener.HEART_BEAT),
                                InetAddress.getByName(ipAddress));//发送心跳包
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }
                return;
            } else if (ACTION_NOTIFY_DATA.equals(intent.getAction())) {//刷新消息
                if (binder != null) {
                    Queue<UDPMessageBean> queue = binder.getMessages().get(ipAddress);
                    if (queue != null)//从后台遍历读取数据
                        ergodicMessage(queue);
                } else {
                    unbindService(connection);
                    Intent intent1 = new Intent(VideoChatActivity.this, ChatService.class);
                    bindService(intent1, connection = new CustomServiceConnection(), Context.BIND_AUTO_CREATE);
                }
            }
        }
    }

    @Override
    protected void onPause() {
        activityNotFocused = true;
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            videoReceiveListener.close();
            voiceListener.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (binded)
            unbindService(connection);
        stopService(new Intent(VideoChatActivity.this, ChatService.class));
        unregisterReceiver(messageUpdateReceiver);
    }

    private void openVoice() {
        try {
            voiceListener = UDPVoiceIListener.getInstance(InetAddress.getByName(ipAddress));
            voiceListener.open();
            voiceListenerOpened = true;
        } catch (Exception e) {
            e.printStackTrace();
            SystUtils.showToast("抱歉，语音打开失败");
            try {
                voiceListener.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }
}
