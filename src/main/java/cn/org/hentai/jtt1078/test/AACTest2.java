package cn.org.hentai.jtt1078.test;

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

public class AACTest2 {
    public static void main(String[] args) throws Exception {
        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(new File("test.aac"), 2);
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
        recorder.start();
        // wav文件
        recorderWav(recorder);
        // 麦克风数据
        // recorderMic(recorder);
        recorder.close();
    }

    /**
     * 将wav数据包装再传给前端
     * 
     * @throws Exception
     */
    public static void recorderWav(FFmpegFrameRecorder recorder) throws Exception {
        int len = -1;
        byte[] buffer = new byte[1024];
        FileInputStream fis = new FileInputStream("test.wav");
        while ((len = fis.read(buffer)) > -1) {
            int nSamplesRead = len / 2;
            short[] samples = new short[nSamplesRead];
            ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(samples);
            ShortBuffer sBuff = ShortBuffer.wrap(samples, 0, nSamplesRead);
            // 指定采样点的音频采样率和声道数量
            recorder.recordSamples(8000, 1, sBuff);
        }
        fis.close();
    }

    /**
     * 编码麦克风数据
     * 
     * @param recorder
     * @throws Exception
     */
    private static void recorderMic(FFmpegFrameRecorder recorder) throws Exception {
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
