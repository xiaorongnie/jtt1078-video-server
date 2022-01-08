package com.transcodegroup.jtt1078.common.util;

/**
 * FLV操作单元
 * 
 * @author eason
 * @date 2022/01/08
 */
public final class FLVUtils {
    /**
     * 重置FLV的时间戳
     * 
     * @param packet
     * @param timestamp
     */
    public static void resetTimestamp(byte[] packet, int timestamp) {
        // 0 1 2 3
        // 4 5 6 7
        // 只对视频类的TAG进行修改
        if (packet[0] != 9 && packet[0] != 8) {
            return;
        }

        packet[4] = (byte)((timestamp >> 16) & 0xff);
        packet[5] = (byte)((timestamp >> 8) & 0xff);
        packet[6] = (byte)((timestamp >> 0) & 0xff);
        packet[7] = (byte)((timestamp >> 24) & 0xff);
    }
}
