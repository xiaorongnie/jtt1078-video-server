package com.transcodegroup.jtt1078.ws;

import javax.websocket.Session;

import com.transcodegroup.jtt1078.codec.AudioCodec;
import com.transcodegroup.jtt1078.codec.algorithm.WavCodec;

import lombok.Data;

/**
 * 自定义Session
 * 
 * @author eason
 * @date 2022/01/05
 */
@Data
public class WsSession {

    private Session session;

    private String imei;

    private AudioCodec audioCodec;

    public WsSession(Session session, String imei) {
        this.session = session;
        this.imei = imei;
        this.audioCodec = new WavCodec();
    }

    public WsSession(Session session, String imei, AudioCodec audioCodec) {
        this.session = session;
        this.imei = imei;
        this.audioCodec = audioCodec;
    }

}
