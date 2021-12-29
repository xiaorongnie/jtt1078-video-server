package cn.org.hentai.jtt1078.test;

import java.io.File;
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
        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(new File("Test01.aac"), 2);
        recorder.setAudioOption("crf", "0");
        recorder.setAudioQuality(0);
        recorder.setAudioBitrate(192000);
        recorder.setSampleRate(44100);
        recorder.setAudioChannels(2);
        // AAC
        recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
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
            int nBytesRead = 0;
            while (nBytesRead == 0) {
                nBytesRead = line.read(buffer, 0, line.available());
            }
            int nSamplesRead = nBytesRead / 2;
            short[] samples = new short[nSamplesRead];
            ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(samples);
            ShortBuffer sBuff = ShortBuffer.wrap(samples, 0, nSamplesRead);
            recorder.recordSamples(sampleRate, numChannels, sBuff);
        }
        recorder.close();
    }
}
