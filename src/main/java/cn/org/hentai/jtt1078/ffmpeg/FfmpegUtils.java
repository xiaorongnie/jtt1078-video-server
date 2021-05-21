package cn.org.hentai.jtt1078.ffmpeg;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;

import lombok.Data;

/**
 * 设备参数(参数值由1078协议设定)
 * 
 * @author zhen.lin
 * @date 2019年3月26日
 */
@Data
public class FfmpegUtils {

    /**
     * 获取FFMPEG格式的采样率 音频采样率 0=8000 1=22050 2=44100 3=48000 100=16000 其他暂时按8k算
     * 
     * @return
     */
    public static int getSampleRateForFFMpeg(int samplerate) {
        int ret = 8000;
        switch (samplerate) {
            case 0:
                ret = 8000;
                break;
            case 1:
                ret = 22050;
                break;
            case 2:
                ret = 44100;
                break;
            case 3:
                ret = 48000;
                break;
            case 100: // 自定义
                ret = 16000;
                break;
        }
        return ret;
    }

    /**
     * 获取FFMPEG格式的音频编码 海思方案有两种G726 </br>
     * ASF格式与RFC3551 ASF对应AV_CODEC_ID_ADPCM_G726LE </br>
     * RFC3551对应 AV_CODEC_ID_ADPCM_G726LE </br>
     * 对应通力终端 ASF=MG726 RFC3551=G726 </br>
     * 通力终端音频默认为 采样率8000 采样格式16位 G726波特率(BPS)设为32k
     * 
     * @return
     */
    public int getCodecFFMpeg(int codec) {
        int ret = avcodec.AV_CODEC_ID_ADPCM_G726LE;
        switch (codec) {
            case 6:
                // G711-a
                ret = avcodec.AV_CODEC_ID_PCM_ALAW;
                break;
            case 7:
                // G711-u
                ret = avcodec.AV_CODEC_ID_PCM_MULAW;
                break;
            case 8:
                // G726
                ret = avcodec.AV_CODEC_ID_ADPCM_G726LE;
                break;
            case 19:
                // AAC
                ret = avcodec.AV_CODEC_ID_AAC;
                break;
            case 111:
                // MG726 自定义 针对海思的 asf格式g726
                ret = avcodec.AV_CODEC_ID_ADPCM_G726;
                break;
        }
        return ret;
    }

    /**
     * 获取FFMPEG格式的音频采样格式
     * 
     * @return
     */
    public static int getSampleFormatFFMpeg(int sampleformat) {
        int ret = avutil.AV_SAMPLE_FMT_S16;
        switch (sampleformat) {
            case 0:
                ret = avutil.AV_SAMPLE_FMT_U8;
                break;
            case 1:
                ret = avutil.AV_SAMPLE_FMT_S16;
                break;
            case 2:
                ret = avutil.AV_SAMPLE_FMT_S32;
                break;
            case 8:
                // AAC FFMPEG固定采用FLTP采样格式
                ret = avutil.AV_SAMPLE_FMT_FLTP;
                break;
        }
        return ret;
    }

}
