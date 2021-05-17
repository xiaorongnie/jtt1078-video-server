package cn.org.hentai.jtt1078.ws;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;

import javax.websocket.Session;

import cn.org.hentai.jtt1078.codec.WavCodec;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WsSessionGroup {

    private static ConcurrentHashMap<String, Session> sessionHashMap = new ConcurrentHashMap<>();

    private static WavCodec wavCodec = new WavCodec();

    public static void put(Session session) {
        sessionHashMap.put(session.getId(), session);
    }

    public static void remove(Session session) {
        sessionHashMap.remove(session.getId());
    }

    public static void onAudioData(String imei, byte[] data) {
        for (Session session : sessionHashMap.values()) {
            String key = String.valueOf(session.getUserProperties().get("imei"));
            if (imei.equals(key)) {
                ByteBuffer wavBuffer = ByteBuffer.wrap(wavCodec.fromPCM(data));
                try {
                    session.getBasicRemote().sendBinary(wavBuffer);
                    log.info("Session -> {}", wavBuffer.array().length);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
