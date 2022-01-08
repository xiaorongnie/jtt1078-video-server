package com.transcodegroup.jtt1078.codec;

import org.springframework.util.StringUtils;

import com.transcodegroup.jtt1078.codec.algorithm.ADPCMCodec;
import com.transcodegroup.jtt1078.codec.algorithm.G711Codec;
import com.transcodegroup.jtt1078.codec.algorithm.G711UCodec;
import com.transcodegroup.jtt1078.codec.algorithm.PcmCodec;
import com.transcodegroup.jtt1078.codec.algorithm.SilenceCodec;
import com.transcodegroup.jtt1078.codec.algorithm.WavCodec;
import com.transcodegroup.jtt1078.codec.ffmpeg.G726Codec;
import com.transcodegroup.jtt1078.codec.ffmpeg.Mg726Codec;
import com.transcodegroup.jtt1078.codec.ffmpeg.aac.AACCodec;
import com.transcodegroup.jtt1078.common.entity.MediaEncoding;

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

    public static AudioCodec getCodec(int encoding) {
        if (MediaEncoding.Encoding.ADPCMA.ordinal() == encoding) {
            return new ADPCMCodec();
        } else if (MediaEncoding.Encoding.G711A.ordinal() == encoding) {
            return new G711Codec();
        } else if (MediaEncoding.Encoding.G711U.ordinal() == encoding) {
            return new G711UCodec();
        } else if (MediaEncoding.Encoding.G726.ordinal() == encoding) {
            return new G726Codec();
        } else if (MediaEncoding.Encoding.AAC.ordinal() == encoding) {
            return new AACCodec();
        } else if (MediaEncoding.getCustomEncoding(encoding).equals(MediaEncoding.Encoding.MG726)) {
            return new Mg726Codec();
        }
        return new SilenceCodec();
    }

    public static AudioCodec getCodec(String encoding) {
        if (StringUtils.isEmpty(encoding)) {
            return new PcmCodec();
        }
        if ("WAV".equalsIgnoreCase(encoding)) {
            return new WavCodec();
        } else if ("G711A".equalsIgnoreCase(encoding)) {
            return new G711Codec();
        } else if ("G711U".equalsIgnoreCase(encoding)) {
            return new G711UCodec();
        }
        return new PcmCodec();
    }

    public void open() {}

    public void close() {}

}
