package cn.org.hentai.jtt1078.codec.grabber;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;

import cn.org.hentai.jtt1078.codec.AudioCodec;
import cn.org.hentai.jtt1078.ffmpeg.AudioStreamGrabber;
import cn.org.hentai.jtt1078.ffmpeg.AudioStreamGrabber.Exception;

/**
 * ADPCM 和 PCM转换
 * </p>
 * IMA-ADPCM测试到AV_CODEC_ID_ADPCM_IMA_ISS解码音质最好
 * </p>
 * DVI4-ADPCM测试到自己java解码效果最好
 */
public final class AdpcmGrabber extends AudioCodec {
    private static AudioStreamGrabber audioStreamGrabber;
    static {
        audioStreamGrabber = new AudioStreamGrabber();
        audioStreamGrabber.setSampleRate(8000);
        audioStreamGrabber.setAudioChannels(1);
        audioStreamGrabber.setSampleFormat(avutil.AV_SAMPLE_FMT_S16);
        // 一般
        // audioStreamGrabber.setAudioCodec(avcodec.AV_CODEC_ID_ADPCM_IMA_WAV);
        // 一般
        // audioStreamGrabber.setAudioCodec(avcodec.AV_CODEC_ID_ADPCM_IMA_DK4);
        // 杂音大
        // audioStreamGrabber.setAudioCodec(avcodec.AV_CODEC_ID_ADPCM_IMA_WS);
        // 哒哒哒声音
        // audioStreamGrabber.setAudioCodec(avcodec.AV_CODEC_ID_ADPCM_IMA_SMJPEG);
        // 杂音大,断断续续
        // audioStreamGrabber.setAudioCodec(avcodec.AV_CODEC_ID_ADPCM_IMA_AMV);
        // audioStreamGrabber.setAudioCodec(avcodec.AV_CODEC_ID_ADPCM_IMA_EA_SEAD);
        // 一般
        // audioStreamGrabber.setAudioCodec(avcodec.AV_CODEC_ID_ADPCM_IMA_ISS);
        // 杂音大,断断续续
        // audioStreamGrabber.setAudioCodec(avcodec.AV_CODEC_ID_ADPCM_IMA_APC);
        // audioStreamGrabber.setAudioCodec(avcodec.AV_CODEC_ID_ADPCM_IMA_OKI);
        // 声音小
        // audioStreamGrabber.setAudioCodec(avcodec.AV_CODEC_ID_ADPCM_IMA_DAT4);
        // 杂音大,断断续续
        // audioStreamGrabber.setAudioCodec(avcodec.AV_CODEC_ID_ADPCM_IMA_SSI);
        audioStreamGrabber.setAudioCodec(avcodec.AV_CODEC_ID_ADPCM_VIMA);
        audioStreamGrabber.setBitsPerCodedSample(4);
        try {
            audioStreamGrabber.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public byte[] toPCM(byte[] data) {
        // 如果前四字节是00 01 52 00，则是海思头，需要去掉
        if (data[0] == 0x00 && data[1] == 0x01 && (data[2] & 0xff) == (data.length - 4) / 2 && data[3] == 0x00) {
            byte[] newBuf = new byte[data.length - 4];
            System.arraycopy(data, 4, newBuf, 0, data.length - 4);
            data = newBuf;
        }
        return audioStreamGrabber.grabPcm(data);
    }

    @Override
    public byte[] fromPCM(byte[] data) {
        return null;
    }

    public void close() {
        try {
            audioStreamGrabber.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}