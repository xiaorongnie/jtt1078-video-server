package com.transcodegroup.jtt1078.ws;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.ByteBuffer;

import javax.websocket.Session;

import com.transcodegroup.jtt1078.codec.AudioCodec;
import com.transcodegroup.jtt1078.codec.algorithm.PcmCodec;
import com.transcodegroup.jtt1078.publisher.Channel;
import com.transcodegroup.jtt1078.publisher.PublishManager;

import lombok.Data;

/**
 * 自定义Session
 * 
 * @author eason
 * @date 2022/01/05
 */
@Data
public class WsSession {

    /**
     * 前端会话
     */
    private Session session;

    /**
     * 设备号
     */
    private String imei;

    /**
     * 频道Tag,通常等于imei-chn
     */
    private String tag;

    /**
     * 前端编解码器
     */
    private AudioCodec audioCodec;

    /**
     * 前端数据流缓存
     */
    PipedOutputStream pipedOutputStream;
    PipedInputStream pipedInputStream;

    public WsSession(Session session, String imei) {
        this(session, imei, new PcmCodec());
    }

    public WsSession(Session session, String imei, AudioCodec audioCodec) {
        this.session = session;
        this.imei = imei;
        this.tag = imei + "-" + "0";
        this.audioCodec = audioCodec;
    }

    public void start() {
        this.pipedOutputStream = new PipedOutputStream();
        try {
            this.pipedInputStream = new PipedInputStream(pipedOutputStream, 320);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 启动一个线程发送前端消息
        new Thread(() -> {
            try {
                byte[] data = new byte[320];
                while (pipedInputStream.read(data) != -1) {
                    Channel channel = PublishManager.getInstance().getChannel(this.tag);
                    if (channel != null) {
                        channel.publishAudio(data);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * 终端音频数据转发平台
     * 
     * @param pcmBytes
     */
    public void onAudioDataOfDevicePcm(byte[] pcmBytes) {
        ByteBuffer wavBuffer = ByteBuffer.wrap(audioCodec.fromPCM(pcmBytes));
        try {
            session.getBasicRemote().sendBinary(wavBuffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 平台给设备发送音频数据
     * 
     * @param data
     *            PCM数据
     */
    public void onAudioDataOfPlatform(byte[] data) {
        Channel channel = PublishManager.getInstance().getChannel(this.tag);
        if (channel != null) {
            try {
                // 平台格式解码PCM
                byte[] pcmBytes = audioCodec.toPCM(data);
                // PCM编码终端音频格式
                pipedOutputStream.write(channel.getAudioCodec().fromPCM(pcmBytes));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
