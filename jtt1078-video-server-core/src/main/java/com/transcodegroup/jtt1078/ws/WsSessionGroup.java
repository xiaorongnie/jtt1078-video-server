package com.transcodegroup.jtt1078.ws;

import java.util.concurrent.ConcurrentHashMap;

import javax.websocket.Session;

/**
 * 会话客户端
 * 
 * @author eason
 * @date 2021/05/18
 */
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
    public static WsSession getSession(Session session) {
        return sessionHashMap.get(session.getId());
    }

    /**
     * 查找设备
     * 
     * @param session
     */
    public static WsSession getSession(String imei) {
        for (WsSession wsSession : sessionHashMap.values()) {
            if (imei.equals(wsSession.getImei())) {
                return wsSession;
            }
        }
        return null;
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

}
