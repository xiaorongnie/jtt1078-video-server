package com.transcodegroup.jtt1078.codec.algorithm;

import com.transcodegroup.jtt1078.codec.AudioCodec;

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
