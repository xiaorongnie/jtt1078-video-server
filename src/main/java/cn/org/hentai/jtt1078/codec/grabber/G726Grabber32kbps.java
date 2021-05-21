package cn.org.hentai.jtt1078.codec.grabber;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;

import cn.org.hentai.jtt1078.codec.AudioCodec;
import cn.org.hentai.jtt1078.ffmpeg.AudioStreamGrabber;
import cn.org.hentai.jtt1078.ffmpeg.AudioStreamGrabber.Exception;

/**
 * G726-16bps码率解码器
 * 
 * @author eason
 * @date 2021/05/21
 */
public class G726Grabber32kbps extends AudioCodec {

    private static AudioStreamGrabber audioStreamGrabber;
    static {
        audioStreamGrabber = new AudioStreamGrabber();
        audioStreamGrabber.setSampleRate(8000);
        audioStreamGrabber.setAudioChannels(1);
        audioStreamGrabber.setSampleFormat(avutil.AV_SAMPLE_FMT_S16);
        audioStreamGrabber.setAudioCodec(avcodec.AV_CODEC_ID_ADPCM_G726);
        audioStreamGrabber.setBitsPerCodedSample(4);
        try {
            audioStreamGrabber.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println();
    }

    @Override
    public byte[] toPCM(byte[] data) {
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