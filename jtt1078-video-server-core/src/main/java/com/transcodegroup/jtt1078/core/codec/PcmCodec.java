package com.transcodegroup.jtt1078.core.codec;

import com.transcodegroup.jtt1078.common.codec.AudioCodec;

public class PcmCodec extends AudioCodec {

    @Override
    public byte[] toPCM(byte[] data) {
        return data;
    }

    @Override
    public byte[] fromPCM(byte[] data) {
        return data;
    }

}
