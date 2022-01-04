package com.transcodegroup.jtt1078.codec.ffmpeg;

import com.transcodegroup.jtt1078.codec.AudioCodec;
import com.transcodegroup.jtt1078.codec.ffmpeg.g726.Mg726Bit;

/**
 * 
 * @author eason
 * @date 2021/05/20
 */
public class Mg726Codec extends AudioCodec {

    // pcm采样率
    private static final int PCM_SAMPLE = 8000;

    // pcm采样点
    private static final int PCM_POINT = 320;

    // 音频通道数
    private static final int CHANNEL = 1;

    // 码率
    private static final int G726_BIT_RATE_16000 = 16000;
    private static final int G726_BIT_RATE_24000 = 24000;
    private static final int G726_BIT_RATE_32000 = 32000;
    private static final int G726_BIT_RATE_40000 = 40000;

    private volatile AudioCodec g726AudioCodec = null;

    @Override
    public byte[] toPCM(byte[] data) {
        // 如果前四字节是00 01 52 00，则是海思头，需要去掉
        if (data[0] == 0x00 && data[1] == 0x01 && (data[2] & 0xff) == (data.length - 4) / 2 && data[3] == 0x00) {
            byte[] newBuf = new byte[data.length - 4];
            System.arraycopy(data, 4, newBuf, 0, data.length - 4);
            data = newBuf;
            hisi = true;
        }
        if (g726AudioCodec == null) {
            synchronized (Mg726Codec.class) {
                if (g726AudioCodec == null) {
                    // 计算G726的码率
                    int rateBit = data.length * 8 * PCM_SAMPLE * CHANNEL / PCM_POINT;
                    if (rateBit == G726_BIT_RATE_40000) {
                        g726AudioCodec = new Mg726Bit(rateBit);
                    } else if (rateBit == G726_BIT_RATE_32000) {
                        g726AudioCodec = new Mg726Bit(rateBit);
                    } else if (rateBit == G726_BIT_RATE_24000) {
                        g726AudioCodec = new Mg726Bit(rateBit);
                    } else if (rateBit == G726_BIT_RATE_16000) {
                        g726AudioCodec = new Mg726Bit(rateBit);
                    } else {
                        return null;
                    }
                }
            }

        }
        if (g726AudioCodec == null) {
            return null;
        }
        return g726AudioCodec.toPCM(data);
    }

    @Override
    public byte[] fromPCM(byte[] data) {
        if (g726AudioCodec != null) {
            return g726AudioCodec.fromPCM(data);
        }
        return null;
    }

}
