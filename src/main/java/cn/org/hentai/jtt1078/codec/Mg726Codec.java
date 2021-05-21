package cn.org.hentai.jtt1078.codec;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

import org.apache.commons.lang.ArrayUtils;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;

import cn.org.hentai.jtt1078.util.FileUtils;

/**
 * 
 * @author eason
 * @date 2021/05/20
 */
public class Mg726Codec extends AudioCodec {

    FFmpegFrameGrabber ffmpegFrameGrabber;

    byte[] g726;

    public Mg726Codec() {
        try {
            ffmpegFrameGrabber = FFmpegFrameGrabber.createDefault("d:\\g726_16kps.g726");
        } catch (Exception e) {
            e.printStackTrace();
        }
        ffmpegFrameGrabber.setSampleRate(8000);
        ffmpegFrameGrabber.setAudioChannels(1);
        ffmpegFrameGrabber.setSampleFormat(avutil.AV_SAMPLE_FMT_S16);
        ffmpegFrameGrabber.setAudioCodec(avcodec.AV_CODEC_ID_ADPCM_G726);
        // ffmpegFrameGrabber.setAudioBitrate(16000);
        ffmpegFrameGrabber.setFormat("G726");
        // ffmpegFrameGrabber.setOption("bits_per_coded_sample", "2");
        // ffmpegFrameGrabber.setAudioOption("bits_per_coded_sample", "2");
        // ffmpegFrameGrabber.setMetadata("bits_per_coded_sample", "2");

        // ffmpegFrameGrabber.setAudioCodec(avcodec.AV_CODEC_ID_ADPCM_IMA_WAV);
        try {
            ffmpegFrameGrabber.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public byte[] toPCM(byte[] data) {
        // 如果前四字节是00 01 52 00，则是海思头，需要去掉
        // if (data[0] == 0x00 && data[1] == 0x01 && (data[2] & 0xff) == (data.length - 4) / 2 && data[3] == 0x00) {
        // byte[] newBuf = new byte[data.length - 4];
        // System.arraycopy(data, 4, newBuf, 0, data.length - 4);
        // data = newBuf;
        // }
        // record(data);
        byte[] pcmAll = null;
        try {
            Frame frame;
            while ((frame = ffmpegFrameGrabber.grabSamples()) != null) {
                if (frame.samples == null) {
                    continue;
                }
                // 单声道
                Buffer buffer = frame.samples[0];
                ByteBuffer TLData = shortToByteValue((ShortBuffer)buffer);
                pcmAll = ArrayUtils.addAll(pcmAll, TLData.array());
            }
            FileUtils.writeByteArrayToFile(new WavCodec().fromPCM(pcmAll), "d:\\g726_16kps-2.WAV");
        } catch (Exception e) {
        }
        return data;
    }

    private void record(byte[] data) {
        g726 = ArrayUtils.addAll(g726, data);
        if (g726.length == 52000) {
            FileUtils.writeByteArrayToFile(g726, "d:\\g726_16kps.g726");
        } else {
            System.out.println(data.length + "->" + g726.length);
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

    @Override
    public byte[] fromPCM(byte[] data) {
        return null;
    }

    @Override
    public void close() {
        if (ffmpegFrameGrabber != null) {
            try {
                ffmpegFrameGrabber.close();
            } catch (org.bytedeco.javacv.FrameGrabber.Exception e) {
                e.printStackTrace();
            }
        }
    }

}
