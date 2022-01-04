package cn.org.hentai.jtt1078.codec.ffmpeg.aac;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;

import cn.org.hentai.jtt1078.codec.algorithm.AudioCodec;
import cn.org.hentai.jtt1078.codec.ffmpeg.javacv.AudioStreamGrabber;
import cn.org.hentai.jtt1078.codec.ffmpeg.javacv.AudioStreamRecorder;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author eason
 * @date 2022/12/28
 */
@Slf4j
public class AACCodec extends AudioCodec {

    private AudioStreamGrabber audioStreamGrabber = null;
    private AudioStreamRecorder audioStreamRecorder = null;

    public AACCodec() {
        try {
            tryLoadGrabber();
            tryLoadRecorder();
        } catch (Exception e) {
            log.error("tryLoadGrabber() tryLoadRecorder() error ");
        }
    }

    @Override
    public byte[] toPCM(byte[] data) {
        return audioStreamGrabber.grabPcm(data);
    }

    @Override
    public byte[] fromPCM(byte[] data) {
        return audioStreamRecorder.recordShortSamples(data);
    }

    public void tryLoadGrabber() throws Exception {
        audioStreamGrabber = new AudioStreamGrabber();
        audioStreamGrabber.setSampleRate(8000);
        audioStreamGrabber.setAudioChannels(1);
        audioStreamGrabber.setSampleFormat(avutil.AV_SAMPLE_FMT_FLTP);
        audioStreamGrabber.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
        audioStreamGrabber.start();
    }

    public void tryLoadRecorder() throws Exception {
        audioStreamRecorder = new AudioStreamRecorder();
        audioStreamRecorder.setSampleRate(8000);
        audioStreamRecorder.setAudioChannels(1);
        audioStreamRecorder.setSampleFormat(avutil.AV_SAMPLE_FMT_FLTP);
        audioStreamRecorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
        audioStreamRecorder.start();
    }

    public void close() {
        try {
            audioStreamGrabber.stop();
            audioStreamRecorder.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}