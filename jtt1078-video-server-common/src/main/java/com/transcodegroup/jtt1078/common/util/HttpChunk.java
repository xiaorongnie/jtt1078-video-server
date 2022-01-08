package com.transcodegroup.jtt1078.common.util;

/**
 * Http数据块
 * 
 * @author eason
 * @date 2022/01/08
 */
public final class HttpChunk {
    public static byte[] make(byte[] data) {
        Packet p = Packet.create(data.length + 64);
        p.addBytes(String.format("%x\r\n", data.length).getBytes());
        p.addBytes(data);
        p.addByte((byte)'\r');
        p.addByte((byte)'\n');
        return p.getBytes();
    }
}
