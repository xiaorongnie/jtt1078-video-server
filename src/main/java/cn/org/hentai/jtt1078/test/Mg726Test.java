package cn.org.hentai.jtt1078.test;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;

import cn.org.hentai.jtt1078.codec.WavCodec;
import cn.org.hentai.jtt1078.util.FileUtils;

public class Mg726Test {
    public static void main(String[] args) {
        try {
            FFmpegFrameGrabber ffmpegFrameGrabber = FFmpegFrameGrabber.createDefault("pcm");
            ffmpegFrameGrabber.setSampleRate(8000);
            ffmpegFrameGrabber.setAudioChannels(1);
            ffmpegFrameGrabber.setSampleFormat(avutil.AV_SAMPLE_FMT_S16);
            ffmpegFrameGrabber.setAudioCodec(avcodec.AV_CODEC_ID_ADPCM_IMA_WAV);
            ffmpegFrameGrabber.setFormat("WAV");
            ffmpegFrameGrabber.start();
            byte[] fileBytes = FileUtils.readFileToByteArray("D:\\铃声.wav");
            Frame frame = ffmpegFrameGrabber.grabAudio(fileBytes);
            Buffer buffer = frame.samples[0];
            ByteBuffer TLData = shortToByteValue((ShortBuffer)buffer);
            FileUtils.writeByteArrayToFile(new WavCodec().fromPCM(TLData.array()), "D:\\铃声2.wav");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static ByteBuffer shortToByteValue(ShortBuffer arr) {
        return shortToByteValue(arr, 1);
    }

    public static ByteBuffer shortToByteValue(ShortBuffer arr, float vol) {
        int len = arr.capacity();
        ByteBuffer bb = ByteBuffer.allocate(len * 2);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < len; i++) {
            bb.putShort(i * 2, (short)((float)arr.get(i) * vol));
        }
        return bb;
    }
}
