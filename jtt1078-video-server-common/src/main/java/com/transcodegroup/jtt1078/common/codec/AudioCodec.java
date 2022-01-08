package com.transcodegroup.jtt1078.common.codec;

/**
 * 音频编解码抽象类
 * 
 * @author eason
 * @date 2021/05/13
 */
public abstract class AudioCodec {

    public boolean hisi = false;

    public abstract byte[] toPCM(byte[] data);

    public abstract byte[] fromPCM(byte[] data);

    public void open() {}

    public void close() {}

}
