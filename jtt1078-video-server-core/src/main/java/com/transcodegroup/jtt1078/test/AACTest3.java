package com.transcodegroup.jtt1078.test;

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
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.FrameGrabber;

import com.transcodegroup.jtt1078.codec.ffmpeg.javacv.AudioStreamGrabber;

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
        AudioStreamGrabber grabber = createAudioStreamGrabber();

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
            System.out.println("PCM样本 " + nSamplesRead);
            short[] samples = new short[nSamplesRead];
            ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(samples);
            ShortBuffer sBuff = ShortBuffer.wrap(samples, 0, nSamplesRead);
            // 指定采样点的音频采样率和声道数量
            if (recorder.recordSamples(sampleRate, numChannels, sBuff)) {
                System.out.println("编码AAC长度 " + fileOS.size());
                byte[] fileOSArray = fileOS.toByteArray();
                if (fileOSArray.length > 1) {
                    System.out.println("fileOS 1" + fileOSArray[0]);
                    System.out.println("fileOS 2 " + fileOSArray[1]);

                    // 如果是海思的g726 要跳掉前面的4个字节
                    byte[] newBuf = new byte[fileOSArray.length - 7];
                    System.arraycopy(fileOSArray, 7, newBuf, 0, newBuf.length);
                    byte[] aac2PcmData = grabber.grabPcm(newBuf);
                    if (aac2PcmData != null) {
                        System.out.println("AAC解码PCM长度 " + aac2PcmData.length);
                    }
                }
                // 写入输出文件流
                fileOutputStream.write(fileOSArray);
                fileOS.reset();
            }
        }
        fileOutputStream.flush();
        fileOutputStream.close();
        fileOS.flush();
        recorder.close();
        grabber.close();
    }

    /**
     * 创建一个音频解码器
     * 
     * @return
     * @throws Exception
     */
    public static AudioStreamGrabber createAudioStreamGrabber() throws Exception {
        // AAC解码器
        AudioStreamGrabber audioStreamGrabber = new AudioStreamGrabber();
        audioStreamGrabber.setSampleRate(44100);
        audioStreamGrabber.setAudioChannels(1);
        audioStreamGrabber.setSampleFormat(avutil.AV_SAMPLE_FMT_FLTP);
        audioStreamGrabber.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
        audioStreamGrabber.setSampleMode(FrameGrabber.SampleMode.FLOAT);
        audioStreamGrabber.start();
        return audioStreamGrabber;
    }

}
