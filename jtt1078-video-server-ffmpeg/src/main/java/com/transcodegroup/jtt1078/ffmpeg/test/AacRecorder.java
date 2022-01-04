package com.transcodegroup.jtt1078.ffmpeg.test;

import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameRecorder;

public class AacRecorder {
    public static void main(String[] args) throws Exception {
        // wav文件
        recorderWav();
        // 麦克风数据
        recorderMic();
    }

    /**
     * 将wav数据包装再传给前端
     * 
     * @throws Exception
     */
    public static void recorderWav() throws Exception {
        try (FFmpegFrameRecorder recorder = wavRecoreder();
            FileInputStream fis = new FileInputStream("audio/test.wav")) {
            recorder.start();
            int len = -1;
            byte[] buffer = new byte[1024];
            while ((len = fis.read(buffer)) > -1) {
                int nSamplesRead = len / 2;
                short[] samples = new short[nSamplesRead];
                ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(samples);
                ShortBuffer sBuff = ShortBuffer.wrap(samples, 0, nSamplesRead);
                // 指定采样点的音频采样率和声道数量
                recorder.recordSamples(8000, 1, sBuff);
            }
        }
    }

    /**
     * 创建WAV录制器，录制成44.1HZ，双声道，192kbps码流
     * 
     * @return
     */
    public static FFmpegFrameRecorder wavRecoreder() {
        // 输出到根目录下
        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(new File("WavRecoreder.aac"), 2);
        // 不可变(固定)音频比特率
        recorder.setAudioOption("crf", "0");
        // 最高质量
        recorder.setAudioQuality(0);
        // 音频比特率
        recorder.setAudioBitrate(192000);
        // 音频采样率
        recorder.setSampleRate(44100);
        // 双声道、立体声
        recorder.setAudioChannels(2);
        // AAC
        recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
        return recorder;
    }

    /**
     * 编码麦克风数据
     * 
     * @param recorder
     * @throws Exception
     */
    private static void recorderMic() throws Exception {
        try (FFmpegFrameRecorder recorder = micRecoreder()) {
            recorder.start();
            AudioFormat format = new AudioFormat(44100.0F, 16, 2, true, false);
            TargetDataLine line = (TargetDataLine)AudioSystem.getLine(new DataLine.Info(TargetDataLine.class, format));
            line.open(format);
            line.start();

            int sampleRate = (int)format.getSampleRate();
            int numChannels = format.getChannels();
            byte[] buffer = new byte[sampleRate * numChannels];
            int count = 0;
            while (count++ < 100) {
                System.out.println(count);
                int nBytesRead = 0;
                while (nBytesRead == 0) {
                    nBytesRead = line.read(buffer, 0, line.available());
                }
                int nSamplesRead = nBytesRead / 2;
                short[] samples = new short[nSamplesRead];
                ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(samples);
                ShortBuffer sBuff = ShortBuffer.wrap(samples, 0, nSamplesRead);
                // 指定采样点的音频采样率和声道数量
                recorder.recordSamples(sampleRate, numChannels, sBuff);
            }
        }
    }

    /**
     * 创建WAV录制器，录制成44.1HZ，双声道，192kbps码流
     * 
     * @return
     */
    public static FFmpegFrameRecorder micRecoreder() {
        // 输出到根目录下
        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(new File("MicRecoreder.aac"), 2);
        // 不可变(固定)音频比特率
        recorder.setAudioOption("crf", "0");
        // 最高质量
        recorder.setAudioQuality(0);
        // 音频比特率
        recorder.setAudioBitrate(192000);
        // 音频采样率
        recorder.setSampleRate(44100);
        // 双声道、立体声
        recorder.setAudioChannels(2);
        // AAC
        recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
        return recorder;
    }
}
