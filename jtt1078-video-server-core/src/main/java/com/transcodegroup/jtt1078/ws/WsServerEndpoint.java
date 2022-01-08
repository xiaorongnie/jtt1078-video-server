package com.transcodegroup.jtt1078.ws;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.transcodegroup.jtt1078.codec.AudioCodec;
import com.transcodegroup.jtt1078.common.util.Utils;

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
        Map<String, List<String>> requestParameterMap = session.getRequestParameterMap();
        List<String> formats = requestParameterMap.get("format");
        String format = CollectionUtils.isEmpty(formats) ? "pcm" : formats.get(0);
        log.info("WebSocket open ({} 8k 16bit) -> {}, {}", format, session.getId(), imei);
        WsSession wsSession = new WsSession(session, Utils.formatPhoneNumber(imei), AudioCodec.getCodec(format));
        WsSessionGroup.put(wsSession);
        wsSession.start();
    }

    @OnClose
    public void onClose(Session session) {
        log.info("WebSocket close -> {} {}", session.getId(), WsSessionGroup.getImei(session));
        WsSessionGroup.remove(session.getId());
    }

    @OnMessage
    public void onMsg(byte[] data, Session session) throws IOException {
        synchronized (session) {
            WsSession wsSession = WsSessionGroup.getSession(session);
            wsSession.onAudioDataOfPlatform(data);
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket error " + session.getId(), error);
    }

}
