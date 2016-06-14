package com.mmj.handdetectorcalling.listeners;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;

import com.mmj.handdetectorcalling.utils.AppConstant;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;

/**
 * 语音收发的监听器，语音通过UDP的方式传输
 * UDP特点是连接快，传输快，因此用于语音的实时交流
 * 缺点是不稳定，切不保证数据的完整性，
 * 也就是说对方可能会收不到部分消息
 */
public class UDPVoiceIListener extends UDPIListener {

    //文本消息监听端口
    private final int port = AppConstant.VOICE_PORT;
    private final int BUFFER_SIZE = 1024 * 6;//6k的数据缓冲区

    private static final int AUDIO_SAMPLE_RATE = 44100; // 音频的采样率（Hz）
    private int recBufSize, playBufSize;
    private AudioRecord audioRecord;
    private AudioTrack audioTrack;
    private InetAddress address;

    private static UDPVoiceIListener instance;

    private boolean shouldContinue = true;

    // 实用单例模式获取当前UDPListener的实例
    public static UDPVoiceIListener getInstance(InetAddress address) {
        if (instance == null) {
            instance = new UDPVoiceIListener(address);
        }
        return instance;
    }

    private UDPVoiceIListener(InetAddress address) {
        this.address = address;
    }


    @Override
    void init() {
        setPort(port);
        setBufferSize(BUFFER_SIZE);
        recBufSize = AudioRecord.getMinBufferSize(AUDIO_SAMPLE_RATE, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT);
        playBufSize = AudioTrack.getMinBufferSize(AUDIO_SAMPLE_RATE, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT);

        audioRecord = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, AUDIO_SAMPLE_RATE,
                AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, recBufSize);
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, AUDIO_SAMPLE_RATE, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT, playBufSize, AudioTrack.MODE_STREAM);
        audioTrack.setStereoVolume(0.8f, 0.8f);//设置当前音量大小

        audioRecord.startRecording();//开始录制
        audioTrack.play();//开始播放

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (shouldContinue) {
                    sendVoiceStream();
                }
            }
        }).start();
    }

    /**
     * 发送语音流
     */
    void sendVoiceStream() {
        byte[] buffer = new byte[recBufSize];
        //从MIC保存数据到缓冲区  
        int bufferReadResult = audioRecord.read(buffer, 0, recBufSize);
        //写入数据即播放,发送数据
        if (bufferReadResult > 0)
            send(buffer, bufferReadResult, address, port);
    }


    @Override
    public void onReceive(byte[] data, DatagramPacket packet) {
        audioTrack.write(data, 0, packet.getLength());
    }

    @Override
    void noticeOffline() throws IOException {

    }

    @Override
    void noticeOnline() throws IOException {

    }

    @Override
    void sendMsgFailure() {

    }

    @Override
    public void openListener() throws IOException {
        super.openListener();
    }

    @Override
    public void closeListener() throws IOException {
        super.closeListener();
        shouldContinue = false;
        if (audioTrack != null)
            audioTrack.stop();
        if (audioRecord != null)
            audioRecord.stop();
        instance = null;
    }

}
