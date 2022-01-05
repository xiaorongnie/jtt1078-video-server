package com.transcodegroup.jtt1078.ffmpeg;

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
 * 录制的PCM数据,编码到输出流，取出来之后，再存到aac文件内
 * 
 * @author eason
 * @date 2022/01/04
 */
public class RecorderAacStream {
    public static void main(String[] args) throws Exception {
        FileOutputStream fileOutputStream = new FileOutputStream("AacRecorderStream.aac");

        ByteArrayOutputStream fileOS = new ByteArrayOutputStream();
        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(fileOS, 1);
        // 不可变(固定)音频比特率
        recorder.setAudioOption("crf", "0");
        // 最高质量
        recorder.setAudioQuality(0);
        // 音频比特率
        // recorder.setAudioBitrate(192000);
        // 音频采样率
        recorder.setSampleRate(8000);
        // 双声道、立体声
        recorder.setAudioChannels(1);
        // AAC
        recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
        // recorder.setSampleFormat(avutil.AV_SAMPLE_FMT_FLTP);
        recorder.setFormat("adts");
        recorder.start();

        // 麦克风数据
        AudioFormat format = new AudioFormat(8000, 16, 1, true, false);
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
            System.out.println("PCM样本 " + nSamplesRead);
            short[] samples = new short[nSamplesRead];
            ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(samples);
            ShortBuffer sBuff = ShortBuffer.wrap(samples, 0, nSamplesRead);
            // 指定采样点的音频采样率和声道数量
            if (recorder.recordSamples(sampleRate, numChannels, sBuff)) {
                fileOS.writeTo(fileOutputStream);
                System.out.println("编码AAC长度 " + fileOS.size());
                fileOS.reset();
                // byte[] fileOSArray = fileOS.toByteArray();
                // fileOS.reset();
                // if (fileOSArray.length > 1) {
                // System.out.println("fileOS 1" + fileOSArray[0]);
                // System.out.println("fileOS 2 " + fileOSArray[1]);
                // }
                // // 写入输出文件流
                // fileOutputStream.write(fileOSArray);
            }
        }
        fileOutputStream.flush();
        fileOutputStream.close();
        recorder.close();
    }

}
