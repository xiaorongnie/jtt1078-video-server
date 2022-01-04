package com.transcodegroup.jtt1078.ws;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.websocket.Session;

import org.apache.commons.lang.ArrayUtils;

import com.transcodegroup.jtt1078.codec.algorithm.WavCodec;
import com.transcodegroup.jtt1078.common.util.ByteUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 会话客户端
 * 
 * @author eason
 * @date 2021/05/18
 */
@Slf4j
public class WsSessionGroup {

    /**
     * WEB端对讲客会话列表
     */
    private static ConcurrentHashMap<String, Session> sessionHashMap = new ConcurrentHashMap<>();

    /**
     * 加入会话会话
     * 
     * @param session
     */
    public static void put(Session session) {
        sessionHashMap.put(session.getId(), session);
    }

    /**
     * 移除对讲会话
     * 
     * @param session
     */
    public static void remove(Session session) {
        sessionHashMap.remove(session.getId());
    }

    /**
     * 收到音频数据
     * 
     * @param imei
     *            设备号
     * @param data
     *            pcm数据
     */
    public static void onAudioData(String imei, byte[] data) {
        for (Session session : sessionHashMap.values()) {
            String key = String.valueOf(session.getUserProperties().get("imei"));
            if (imei.equals(key)) {
                Object storagData = session.getUserProperties().get("pcm");
                byte[] pcmDate = null;
                if (storagData == null) {
                    session.getUserProperties().put("pcm", data);
                } else {
                    byte[] storagPcm = (byte[])storagData;
                    pcmDate = ByteUtils.concat(storagPcm, data);
                    session.getUserProperties().put("pcm", pcmDate);
                }
                // 1秒以上数据才发送一次,暂时的,前端得加缓存一直播放
                if (pcmDate != null && pcmDate.length > 16000) {
                    Object codecObject = session.getUserProperties().get("codec");
                    if (codecObject == null) {
                        codecObject = new WavCodec();
                        session.getUserProperties().put("codec", codecObject);
                    }
                    WavCodec wavCodec = (WavCodec)codecObject;
                    ByteBuffer wavBuffer = ByteBuffer.wrap(wavCodec.fromPCM(pcmDate));
                    try {
                        session.getBasicRemote().sendBinary(wavBuffer);
                        log.info("Session -> {}", wavBuffer.array().length);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    session.getUserProperties().remove("pcm");
                }
            }
        }
    }

    public static void onAudioData(byte[] data) {
        for (Session session : sessionHashMap.values()) {
            try {
                session.getBasicRemote().sendBinary(ByteBuffer.wrap(data));
                log.info("Session -> {} kb", data.length / 1024);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void onAudioData2(byte[] wavData) {
        byte[] pcmData = Arrays.copyOfRange(wavData, 44, wavData.length);
        // 一包包含320个16bit采样点
        int block = 320 * 25;
        int pcmBlock = block * 2;
        int times = pcmData.length / pcmBlock;
        for (int i = 0; i < times; i++) {
            byte[] data = ArrayUtils.subarray(pcmData, pcmBlock * i, pcmBlock * (i + 1));;
            sendWavData(data, i);
            try {
                TimeUnit.MILLISECONDS.sleep(block / 8);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public static void sendWavData(byte[] pcmData, int index) {
        for (Session session : sessionHashMap.values()) {
            try {
                byte[] newWavData = new WavCodec().fromPCM(pcmData);
                session.getBasicRemote().sendBinary(ByteBuffer.wrap(newWavData));
                try {
                    TimeUnit.MILLISECONDS.sleep(40);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                log.info("sendWavData -> {} / {}", newWavData.length, index);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
