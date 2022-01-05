package com.transcodegroup.jtt1078.ws;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.websocket.Session;

import org.apache.commons.lang.ArrayUtils;

import com.transcodegroup.jtt1078.codec.algorithm.WavCodec;

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
    private static ConcurrentHashMap<String, WsSession> sessionHashMap = new ConcurrentHashMap<>();

    /**
     * 加入会话会话
     * 
     * @param wsSession
     */
    public static void put(WsSession wsSession) {
        sessionHashMap.put(wsSession.getSession().getId(), wsSession);
    }

    /**
     * 移除对讲会话
     * 
     * @param session
     */
    public static void remove(String id) {
        sessionHashMap.remove(id);
    }

    /**
     * 查找设备
     * 
     * @param session
     */
    public static String getImei(Session session) {
        return getImei(session.getId());
    }

    /**
     * 查找设备
     * 
     * @param session
     */
    public static String getImei(String id) {
        WsSession wsSession = sessionHashMap.get(id);
        return wsSession == null ? null : wsSession.getImei();
    }

    public static void onAudioData(String target, byte[] data) {
        for (WsSession wsSession : sessionHashMap.values()) {
            if (target.equals(wsSession.getImei())) {
                ByteBuffer wavBuffer = ByteBuffer.wrap(wsSession.getAudioCodec().fromPCM(data));
                try {
                    wsSession.getSession().getBasicRemote().sendBinary(wavBuffer);
                    log.info("Session -> {}", wavBuffer.array().length);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 音频分小块发送,
     * 
     * @param wavData
     */
    public static void broadcastWavDataChunks(byte[] wavData) {
        byte[] pcmData = Arrays.copyOfRange(wavData, 44, wavData.length);
        // 一包包含320个16bit采样点
        int block = 320 * 25;
        int pcmBlock = block * 2;
        int times = pcmData.length / pcmBlock;
        for (int i = 0; i < times; i++) {
            byte[] data = ArrayUtils.subarray(pcmData, pcmBlock * i, pcmBlock * (i + 1));;
            broadcastPcmDataChunks(data, i);
            try {
                TimeUnit.MILLISECONDS.sleep(block / 8);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 广播PCM数据到前端或者APP
     * 
     * @param pcmData
     * @param index
     */
    public static void broadcastPcmDataChunks(byte[] pcmData, int index) {
        for (WsSession wsSession : sessionHashMap.values()) {
            try {
                byte[] newWavData = new WavCodec().fromPCM(pcmData);
                wsSession.getSession().getBasicRemote().sendBinary(ByteBuffer.wrap(newWavData));
                try {
                    TimeUnit.MILLISECONDS.sleep(40);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                log.info("sendWavData -> {} / {}", newWavData.length, index);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
