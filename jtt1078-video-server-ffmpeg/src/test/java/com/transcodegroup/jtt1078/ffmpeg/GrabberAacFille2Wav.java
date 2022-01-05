package com.transcodegroup.jtt1078.ffmpeg;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;

import com.transcodegroup.jtt1078.common.util.FileUtils;
import com.transcodegroup.jtt1078.ffmpeg.codec.WavCodec;

/**
 * PCM编码aac之后，将数据写入输出流，最后存成aac文件
 * 
 * @author eason
 * @date 2022/12/30
 */
public class GrabberAacFille2Wav {
    public static void main(String[] args) throws Exception {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(new FileInputStream("audio/test.aac"));
        // grabber.setSampleMode(FrameGrabber.SampleMode.FLOAT);
        // grabber.setSampleFormat(org.bytedeco.ffmpeg.global.avutil.AV_SAMPLE_FMT_FLTP);
        grabber.setSampleRate(44100);
        grabber.setAudioChannels(1);
        grabber.setSampleFormat(avutil.AV_SAMPLE_FMT_S16);
        grabber.start();

        Frame frame2;
        while ((frame2 = grabber.grab()) != null) {
            System.out.println("timestamp " + frame2.timestamp);
            Buffer buffer = frame2.samples[0];
            if (buffer instanceof FloatBuffer) {
                FloatBuffer floatbuffer = (FloatBuffer)buffer;
                ByteBuffer byteBuffer = ByteBuffer.allocate(floatbuffer.capacity() * 4);
                byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                byteBuffer.asFloatBuffer().put(floatbuffer);
                outputStream.write(byteBuffer.array());
                System.out.println("FloatBuffer," + outputStream.size());
            }
            if (buffer instanceof ShortBuffer) {
                // ShortBuffer shortBuffer = (ShortBuffer)buffer;
                // ByteBuffer byteBuffer = ByteBufUtils.shortToByteValue(shortBuffer);

                ShortBuffer shortBuffer = (ShortBuffer)buffer;
                ByteBuffer byteBuffer = ByteBuffer.allocate(shortBuffer.capacity() * 2);
                byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                byteBuffer.asShortBuffer().put(shortBuffer);

                outputStream.write(byteBuffer.array());
                System.out.println("ShortBuffer," + outputStream.size());
            }
        }

        FileUtils.writeByteArrayToFile(new WavCodec(44100, 1, 16).fromPCM(outputStream.toByteArray()), "aac2wav.wav");
        grabber.close();
        outputStream.close();
    }

}
