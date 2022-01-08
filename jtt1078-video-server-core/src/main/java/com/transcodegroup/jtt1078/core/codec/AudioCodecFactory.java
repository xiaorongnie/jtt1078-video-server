package com.transcodegroup.jtt1078.core.codec;

import org.springframework.util.StringUtils;

import com.transcodegroup.jtt1078.common.codec.AudioCodec;
import com.transcodegroup.jtt1078.common.media.MediaEncoding;
import com.transcodegroup.jtt1078.ffmpeg.aac.AACCodec;
import com.transcodegroup.jtt1078.ffmpeg.g726.G726Codec;
import com.transcodegroup.jtt1078.ffmpeg.g726.Mg726Codec;

/**
 * 音视频编解码工厂
 * 
 * @author eason
 * @date 2021/05/13
 */
public class AudioCodecFactory {

    public static final String WAV = "WAV";
    public static final String G711A = "G711A";
    public static final String G711U = "G711U";

    /**
     * 1078音频ID获取编解码器
     * 
     * @param encoding
     * @return
     */
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

    /**
     * 前端字符串获取音频编解码器
     * 
     * @param encoding
     * @return
     */
    public static AudioCodec getCodec(String encoding) {
        if (StringUtils.isEmpty(encoding)) {
            return new PcmCodec();
        }
        if (WAV.equalsIgnoreCase(encoding)) {
            return new WavCodec();
        } else if (G711A.equalsIgnoreCase(encoding)) {
            return new G711Codec();
        } else if (G711U.equalsIgnoreCase(encoding)) {
            return new G711UCodec();
        }
        return new PcmCodec();
    }

}
