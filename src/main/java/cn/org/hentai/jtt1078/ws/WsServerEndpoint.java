package cn.org.hentai.jtt1078.ws;

import java.util.Arrays;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.springframework.stereotype.Component;

import cn.org.hentai.jtt1078.publisher.PublishManager;
import cn.org.hentai.jtt1078.util.Utils;
import lombok.extern.slf4j.Slf4j;

/**
 * WebSocket对讲, 默认接收WAV格式数据,每一包数据都封装了WAV头部数据
 * 
 * @param imei
 *            设备号
 * @author eason
 * @date 2021/04/30
 */
@Component
@ServerEndpoint("/talk/{imei}")
@Slf4j
public class WsServerEndpoint {

    @OnOpen
    public void onOpen(Session session, @PathParam(value = "imei") String imei) {
        session.getUserProperties().put("imei", Utils.formatPhoneNumber(imei));
        WsSessionGroup.put(session);
        log.info("WebSocket open -> {}, {}", session.getId(), imei);
    }

    @OnClose
    public void onClose(Session session) {
        log.info("WebSocket close -> {} {}", session.getId(), session.getUserProperties().get("imei"));
        WsSessionGroup.remove(session);
    }

    @OnMessage
    public void onMsg(byte[] data, Session session) {
        String imei = String.valueOf(session.getUserProperties().get("imei"));
        // FileUtils.writeByteArrayToFile(data, "E:\\" + imei + ".wav");
        // log.info("WebSocket msg -> {} {} {}", session.getId(), imei, data.length);
        PublishManager.getInstance().publishAudio(Arrays.copyOfRange(data, 44, data.length), imei);
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket error " + session.getId(), error);
    }

}
