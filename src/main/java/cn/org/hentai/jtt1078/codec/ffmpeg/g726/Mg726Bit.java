package cn.org.hentai.jtt1078.codec.ffmpeg.g726;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;

import cn.org.hentai.jtt1078.codec.algorithm.AudioCodec;
import cn.org.hentai.jtt1078.codec.ffmpeg.javacv.AudioStreamGrabber;
import cn.org.hentai.jtt1078.codec.ffmpeg.javacv.AudioStreamRecorder;
import lombok.extern.slf4j.Slf4j;

/**
 * G726-16bps码率解码器
 * 
 * @author eason
 * @date 2021/05/21
 */
@Slf4j
public class Mg726Bit extends AudioCodec {

    private AudioStreamGrabber audioStreamGrabber = null;
    private AudioStreamRecorder audioStreamRecorder = null;

    public Mg726Bit(int bitRate) {
        try {
            tryLoadGrabber(bitRate);
            tryLoadRecorder(bitRate);
        } catch (Exception e) {
            log.error("tryLoadGrabber() tryLoadRecorder() error " + bitRate);
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

    public void tryLoadGrabber(int bitRate) throws Exception {
        audioStreamGrabber = new AudioStreamGrabber();
        audioStreamGrabber.setSampleRate(8000);
        audioStreamGrabber.setAudioChannels(1);
        audioStreamGrabber.setSampleFormat(avutil.AV_SAMPLE_FMT_S16);
        audioStreamGrabber.setAudioCodec(avcodec.AV_CODEC_ID_ADPCM_G726);
        audioStreamGrabber.setBitsPerCodedSample(bitRate / 8000);
        audioStreamGrabber.start();
    }

    public void tryLoadRecorder(int bitRate) throws Exception {
        audioStreamRecorder = new AudioStreamRecorder();
        audioStreamRecorder.setSampleRate(8000);
        audioStreamRecorder.setAudioChannels(1);
        audioStreamRecorder.setSampleFormat(avutil.AV_SAMPLE_FMT_S16);
        audioStreamRecorder.setAudioCodec(avcodec.AV_CODEC_ID_ADPCM_G726);
        audioStreamRecorder.setAudioBitrate(bitRate);
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