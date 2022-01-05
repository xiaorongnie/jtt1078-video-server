package com.transcodegroup.jtt1078.ffmpeg;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
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

import lombok.extern.slf4j.Slf4j;

/**
 * PCM编码aac之后，将数据写入输出流，最后存成aac文件
 * 
 * @author eason
 * @date 2022/12/30
 */
@Slf4j
public class GrabberG726Stream2Wav {
    public static void main(String[] args) {
        try {
            PipedOutputStream pipedOutputStream = new PipedOutputStream();
            PipedInputStream pipedInputStream = new PipedInputStream(pipedOutputStream);

            Thread thread2 = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(pipedInputStream, 0);
                        grabber.setFormat("g726");
                        grabber.setAudioBitrate(16000);
                        grabber.setAudioChannels(1);
                        // grabber.setSampleMode(FrameGrabber.SampleMode.FLOAT);
                        // grabber.setSampleFormat(org.bytedeco.ffmpeg.global.avutil.AV_SAMPLE_FMT_FLTP);
                        grabber.setSampleRate(8000);
                        grabber.setSampleFormat(avutil.AV_SAMPLE_FMT_S16);
                        grabber.start();
                        log.info("grabber start ....");
                        Frame frame2;
                        while ((frame2 = grabber.grab()) != null) {
                            log.info("timestamp " + frame2.timestamp);
                            Buffer buffer = frame2.samples[0];
                            if (buffer instanceof FloatBuffer) {
                                FloatBuffer floatbuffer = (FloatBuffer)buffer;
                                ByteBuffer byteBuffer = ByteBuffer.allocate(floatbuffer.capacity() * 4);
                                byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                                byteBuffer.asFloatBuffer().put(floatbuffer);
                                outputStream.write(byteBuffer.array());
                                log.info("FloatBuffer," + outputStream.size());
                            }
                            if (buffer instanceof ShortBuffer) {
                                // ShortBuffer shortBuffer = (ShortBuffer)buffer;
                                // ByteBuffer byteBuffer = ByteBufUtils.shortToByteValue(shortBuffer);

                                ShortBuffer shortBuffer = (ShortBuffer)buffer;
                                ByteBuffer byteBuffer = ByteBuffer.allocate(shortBuffer.capacity() * 2);
                                byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                                byteBuffer.asShortBuffer().put(shortBuffer);

                                outputStream.write(byteBuffer.array());
                                log.info("ShortBuffer," + outputStream.size());
                            }
                        }

                        FileUtils.writeByteArrayToFile(new WavCodec(8000, 1, 16).fromPCM(outputStream.toByteArray()),
                            "pcm16Towav.wav");
                        grabber.close();
                        outputStream.close();
                        pipedInputStream.close();
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                }
            });
            thread2.start();

            Thread thread1 = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        FileInputStream inputStream = new FileInputStream("in_16.g726");
                        byte[] block = new byte[1024];
                        while ((inputStream.read(block)) > -1) {
                            pipedOutputStream.write(block);
                            pipedOutputStream.flush();
                            log.info("pipedOutputStream.write " + block.length);
                        }
                        inputStream.close();
                        pipedOutputStream.close();
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                }
            });
            thread1.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
