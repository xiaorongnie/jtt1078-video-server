package com.transcodegroup.jtt1078.ws;

import java.util.Arrays;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.springframework.stereotype.Component;

import com.transcodegroup.jtt1078.common.util.Utils;
import com.transcodegroup.jtt1078.publisher.PublishManager;

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
        log.info("WebSocket open (WAV 8k 16bit) -> {}, {}", session.getId(), imei);
        WsSessionGroup.put(new WsSession(session, Utils.formatPhoneNumber(imei)));
    }

    @OnClose
    public void onClose(Session session) {
        log.info("WebSocket close (WAV 8k 16bit) -> {} {}", session.getId(), session.getUserProperties().get("imei"));
        WsSessionGroup.remove(session.getId());
    }

    @OnMessage
    public void onMsg(byte[] data, Session session) {
        PublishManager.getInstance().publishAudio(Arrays.copyOfRange(data, 44, data.length),
            WsSessionGroup.getImei(session));
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket error " + session.getId(), error);
    }

}
