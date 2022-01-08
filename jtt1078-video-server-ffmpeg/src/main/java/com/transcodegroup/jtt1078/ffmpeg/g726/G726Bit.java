package com.transcodegroup.jtt1078.ffmpeg.g726;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;

import com.transcodegroup.jtt1078.common.codec.AudioCodec;
import com.transcodegroup.jtt1078.ffmpeg.javacv.AudioStreamGrabber;
import com.transcodegroup.jtt1078.ffmpeg.javacv.AudioStreamRecorder;

import lombok.extern.slf4j.Slf4j;

/**
 * G726-16bps码率解码器
 * 
 * @author eason
 * @date 2021/05/21
 */
@Slf4j
public class G726Bit extends AudioCodec {

    private AudioStreamGrabber audioStreamGrabber = null;
    private AudioStreamRecorder audioStreamRecorder = null;

    public G726Bit(int bitRate) {
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
        audioStreamGrabber.setAudioCodec(avcodec.AV_CODEC_ID_ADPCM_G726LE);
        audioStreamGrabber.setBitsPerCodedSample(bitRate / 8000);
        audioStreamGrabber.start();
    }

    public void tryLoadRecorder(int bitRate) throws Exception {
        audioStreamRecorder = new AudioStreamRecorder();
        audioStreamRecorder.setSampleRate(8000);
        audioStreamRecorder.setAudioChannels(1);
        audioStreamRecorder.setSampleFormat(avutil.AV_SAMPLE_FMT_S16);
        audioStreamRecorder.setAudioCodec(avcodec.AV_CODEC_ID_ADPCM_G726LE);
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