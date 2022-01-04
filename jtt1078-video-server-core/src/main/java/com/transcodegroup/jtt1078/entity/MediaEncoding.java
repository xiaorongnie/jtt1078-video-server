package com.transcodegroup.jtt1078.entity;

/**
 * Created by matrixy on 2019/12/20.
 */
public final class MediaEncoding {
    public enum Encoding {
        RESERVED, G721, G722, G723, G728, G729, G711A, G711U, G726, G729A, DVI4_3, DVI4_4, DVI4_8K, DVI4_16K, LPC,
        S16BE_STEREO, S16BE_MONO, MPEGAUDIO, LPCM, AAC, WMA9STD, HEAAC, PCM_VOICE, PCM_AUDIO, AACLC, MP3, ADPCMA,
        MP4AUDIO, AMR, // 28

        H264, // 98
        H265, AVS, SVAC, UNKNOWN, MG726,
    }

    /**
     * 获取自定义类型
     * 
     * @param type
     * @return
     */
    public static Encoding getCustomEncoding(int type) {
        switch (type) {
            case 111:
                return Encoding.MG726;
            default:
                return Encoding.UNKNOWN;
        }
    }

    public static Encoding getEncoding(Media.Type type, int pt) {
        if (type.equals(Media.Type.Audio)) {
            if (pt == 111) {
                return Encoding.MG726;
            }
            if (pt >= 0 && pt <= 28) {
                return Encoding.values()[pt];
            }
            return Encoding.UNKNOWN;
        }
        if (pt >= 98 && pt <= 101) {
            return Encoding.values()[pt - 98 + 29];
        }
        return Encoding.UNKNOWN;
    }
}
