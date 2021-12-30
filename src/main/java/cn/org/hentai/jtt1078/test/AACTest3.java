package cn.org.hentai.jtt1078.test;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameRecorder;

/**
 * PCM编码aac之后，将数据写入输出流，最后存成aac文件
 * 
 * @author eason
 * @date 2022/12/30
 */
public class AACTest3 {
    public static void main(String[] args) throws Exception {
        FileOutputStream fileOutputStream = new FileOutputStream("TestStream.aac");

        ByteArrayOutputStream fileOS = new ByteArrayOutputStream();
        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(fileOS, 1);
        // 不可变(固定)音频比特率
        recorder.setAudioOption("crf", "0");
        // 最高质量
        recorder.setAudioQuality(0);
        // 音频比特率
        recorder.setAudioBitrate(192000);
        // 音频采样率
        recorder.setSampleRate(44100);
        // 双声道、立体声
        recorder.setAudioChannels(1);
        // AAC
        recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
        // recorder.setSampleFormat(avutil.AV_SAMPLE_FMT_FLTP);
        recorder.setFormat("adts");
        recorder.start();

        // 麦克风数据
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
            // 指定采样点的音频采样率和声道数量
            if (recorder.recordSamples(sampleRate, numChannels, sBuff)) {
                System.out.println(fileOS.size());
                fileOutputStream.write(fileOS.toByteArray());
                fileOS.reset();
            }
        }
        fileOutputStream.flush();
        fileOutputStream.close();
        fileOS.flush();
        recorder.close();
    }

}
