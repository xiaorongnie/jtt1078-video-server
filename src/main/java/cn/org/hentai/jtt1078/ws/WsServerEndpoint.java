package cn.org.hentai.jtt1078.ws;

import java.io.IOException;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.springframework.stereotype.Component;

@ServerEndpoint("/talk")
@Component
public class WsServerEndpoint {

    /**
     * 连接成功
     *
     * @param session
     * @throws IOException
     */
    @OnOpen
    public void onOpen(Session session) throws IOException {
        System.out.println("连接成功");
    }

    /**
     * 连接关闭
     *
     * @param session
     */
    @OnClose
    public void onClose(Session session) {
        System.out.println("连接关闭");
    }

    /**
     * 接收到消息
     *
     * @param text
     */
    @OnMessage
    public String onMsg(String text) throws IOException {
        System.out.println("收到消息" + text);
        return "servet 发送：" + text;
    }
}
