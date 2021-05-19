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
 * WebSocket对讲
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

    /**
     * 连接成功
     *
     * @param session
     * @param imei
     *            设备号
     */
    @OnOpen
    public void onOpen(Session session, @PathParam(value = "imei") String imei) {
        session.getUserProperties().put("imei", Utils.formatPhoneNumber(imei));
        WsSessionGroup.put(session);
        log.info("WebSocket open -> {}, {}", session.getId(), imei);
    }

    /**
     * 连接关闭
     *
     * @param session
     */
    @OnClose
    public void onClose(Session session) {
        log.info("WebSocket close -> {} {}", session.getId(), session.getUserProperties().get("imei"));
        WsSessionGroup.remove(session);
    }

    /**
     * 接收到消息
     *
     * @param text
     */
    @OnMessage
    public void onMsg(byte[] data, Session session) {
        String imei = String.valueOf(session.getUserProperties().get("imei"));
        // FileUtils.writeByteArrayToFile(data, "E:\\" + imei + ".wav");
        log.info("WebSocket msg -> {} {} {}", session.getId(), imei, data.length);
        PublishManager.getInstance().publishAudio(Arrays.copyOfRange(data, 44, data.length), imei);
    }

    /**
     * 异常处理
     * 
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.info("WebSocket error -> {} {}", session.getId(), error.getMessage());
        error.printStackTrace();
    }

}
