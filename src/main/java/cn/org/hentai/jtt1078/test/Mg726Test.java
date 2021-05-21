package cn.org.hentai.jtt1078.test;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.Frame;

import cn.org.hentai.jtt1078.codec.WavCodec;
import cn.org.hentai.jtt1078.codec.grabber.G726Grabber16kbps;
import cn.org.hentai.jtt1078.ffmpeg.AudioFileGrabber;
import cn.org.hentai.jtt1078.ffmpeg.AudioStreamGrabber;
import cn.org.hentai.jtt1078.util.ByteBufUtils;
import cn.org.hentai.jtt1078.util.FileUtils;

public class Mg726Test {
    public static void main(String[] args) throws Exception {
        // grabberWavFile();
        // grabberWavBytes();
        // grabberG726_32Bytes();
        G726Grabber16kbps g726Kbps16Grabber = new G726Grabber16kbps();
        g726Kbps16Grabber.open();
        byte[] fileBytes = FileUtils.readFileToByteArray("d:\\g726_16kps.g726");
        g726Kbps16Grabber.toPCM(fileBytes);
    }

    /**
     * 测试WAV文件抓帧器
     * 
     * @return
     * @throws Exception
     */
    public static AudioFileGrabber grabberWavFile() throws Exception {
        AudioFileGrabber ffmpegFrameGrabber = new AudioFileGrabber("d:\\铃声.wav");
        ffmpegFrameGrabber.setSampleRate(8000);
        ffmpegFrameGrabber.setAudioChannels(1);
        ffmpegFrameGrabber.setSampleFormat(avutil.AV_SAMPLE_FMT_S16);
        ffmpegFrameGrabber.setAudioCodec(avcodec.AV_CODEC_ID_FIRST_AUDIO);
        // ffmpegFrameGrabber.setFormat("G726");
        ffmpegFrameGrabber.start();
        return ffmpegFrameGrabber;
    }

    /**
     * 测试WAV流抓帧器
     * 
     * @return
     * @throws Exception
     */
    public static void grabberWavBytes() throws Exception {
        AudioStreamGrabber ffmpegFrameGrabber = new AudioStreamGrabber();
        ffmpegFrameGrabber.setSampleRate(8000);
        ffmpegFrameGrabber.setAudioChannels(1);
        ffmpegFrameGrabber.setSampleFormat(avutil.AV_SAMPLE_FMT_S16);
        ffmpegFrameGrabber.setAudioCodec(avcodec.AV_CODEC_ID_FIRST_AUDIO);
        ffmpegFrameGrabber.start();

        byte[] fileBytes = FileUtils.readFileToByteArray("d:\\铃声.wav");
        Frame frame = ffmpegFrameGrabber.grabAudio(fileBytes);
        Buffer buffer = frame.samples[0];
        ByteBuffer byteBuffer = ByteBufUtils.shortToByteValue((ShortBuffer)buffer);
        FileUtils.writeByteArrayToFile(new WavCodec().fromPCM(byteBuffer.array()), "d:\\铃声-2.wav");
        ffmpegFrameGrabber.close();
    }

    /**
     * 测试WAV流抓帧器
     * 
     * @return
     * @throws Exception
     */
    public static void grabberG726_32Bytes() throws Exception {
        AudioStreamGrabber ffmpegFrameGrabber = new AudioStreamGrabber();
        ffmpegFrameGrabber.setSampleRate(8000);
        ffmpegFrameGrabber.setAudioChannels(1);
        ffmpegFrameGrabber.setSampleFormat(avutil.AV_SAMPLE_FMT_S16);
        ffmpegFrameGrabber.setAudioCodec(avcodec.AV_CODEC_ID_ADPCM_G726);
        ffmpegFrameGrabber.setBitsPerCodedSample(2);
        ffmpegFrameGrabber.start();

        byte[] fileBytes = FileUtils.readFileToByteArray("d:\\g726_16kps.g726");
        FileUtils.writeByteArrayToFile(new WavCodec().fromPCM(ffmpegFrameGrabber.grabPcm(fileBytes)),
            "d:\\g726_16kps-2.wav");
        ffmpegFrameGrabber.close();
    }

}
