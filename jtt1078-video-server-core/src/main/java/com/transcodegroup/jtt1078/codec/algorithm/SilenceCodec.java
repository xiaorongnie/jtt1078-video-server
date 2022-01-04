package com.transcodegroup.jtt1078.codec.algorithm;

import com.transcodegroup.jtt1078.codec.AudioCodec;

/**
 * Created by houcheng on 2019-12-11.
 */
public class SilenceCodec extends AudioCodec
{
    static final byte[] BLANK = new byte[0];

    @Override
    public byte[] toPCM(byte[] data)
    {
        return BLANK;
    }

    @Override
    public byte[] fromPCM(byte[] data)
    {
        return BLANK;
    }
}
