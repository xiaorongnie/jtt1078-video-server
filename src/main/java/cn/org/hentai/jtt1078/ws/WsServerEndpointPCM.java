package cn.org.hentai.jtt1078.ws;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.springframework.stereotype.Component;

import cn.org.hentai.jtt1078.entity.MediaEncoding;
import cn.org.hentai.jtt1078.publisher.PublishManager;
import cn.org.hentai.jtt1078.util.Utils;
import lombok.extern.slf4j.Slf4j;

/**
 * WebSocket对讲
 * 
 * @param imei
 *            设备号
 * @author eason
 * @date 2021/04/30
 */
@Component
@ServerEndpoint("/talk/pcm/{imei}")
@Slf4j
public class WsServerEndpointPCM {

    @OnOpen
    public void onOpen(Session session, @PathParam(value = "imei") String imei) {
        log.info("WebSocket open (PCM 8k 16bit) -> {}, {}", session.getId(), imei);
        session.getUserProperties().put("imei", Utils.formatPhoneNumber(imei));
        session.getUserProperties().put("encoding", MediaEncoding.Encoding.PCM_AUDIO.toString());
        WsSessionGroup.put(session);
    }

    @OnClose
    public void onClose(Session session) {
        log.info("WebSocket close (PCM 8k 16bit) -> {} {}", session.getId(), session.getUserProperties().get("imei"));
        WsSessionGroup.remove(session);
    }

    @OnMessage
    public void onMsg(byte[] data, Session session) {
        String imei = String.valueOf(session.getUserProperties().get("imei"));
        PublishManager.getInstance().publishAudio(data, imei);
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket error " + session.getId(), error);
    }

}
