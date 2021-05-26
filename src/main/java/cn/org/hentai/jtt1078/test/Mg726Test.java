package cn.org.hentai.jtt1078.test;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.Frame;

import cn.org.hentai.jtt1078.codec.WavCodec;
import cn.org.hentai.jtt1078.ffmpeg.AudioFileGrabber;
import cn.org.hentai.jtt1078.ffmpeg.AudioStreamGrabber;
import cn.org.hentai.jtt1078.ffmpeg.AudioStreamRecorder;
import cn.org.hentai.jtt1078.util.FileUtils;

public class Mg726Test {
    public static void main(String[] args) throws Exception {
        // grabberWavFile();
        grabberWavBytes();
        grabberG726_32Bytes();
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
        try (AudioStreamGrabber ffmpegFrameGrabber = new AudioStreamGrabber()) {
            ffmpegFrameGrabber.setSampleRate(8000);
            ffmpegFrameGrabber.setAudioChannels(1);
            ffmpegFrameGrabber.setSampleFormat(avutil.AV_SAMPLE_FMT_S16);
            ffmpegFrameGrabber.setAudioCodec(avcodec.AV_CODEC_ID_FIRST_AUDIO);
            ffmpegFrameGrabber.start();
            byte[] fileBytes = FileUtils.readFileToByteArray("d:\\铃声.wav");
            Frame frame = ffmpegFrameGrabber.grabAudio(fileBytes);
            try (AudioStreamRecorder audioStreamRecorder = new AudioStreamRecorder()) {
                audioStreamRecorder.setSampleRate(8000);
                audioStreamRecorder.setAudioChannels(1);
                audioStreamRecorder.setSampleFormat(avutil.AV_SAMPLE_FMT_S16);
                audioStreamRecorder.setAudioCodec(avcodec.AV_CODEC_ID_ADPCM_G726);
                audioStreamRecorder.setAudioBitrate(32000);
                audioStreamRecorder.start();
                byte[] data = audioStreamRecorder.recordSamples(frame.samples);
                FileUtils.writeByteArrayToFile(data, "d:\\铃声-g726-32bps.g726");
            }
        }

        // Buffer buffer = frame.samples[0];
        // ByteBuffer byteBuffer = ByteBufUtils.shortToByteValue((ShortBuffer)buffer);
        // FileUtils.writeByteArrayToFile(new WavCodec().fromPCM(byteBuffer.array()), "d:\\铃声-2.wav");
        // ffmpegFrameGrabber.close();
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
        ffmpegFrameGrabber.setBitsPerCodedSample(4);
        ffmpegFrameGrabber.start();

        byte[] fileBytes = FileUtils.readFileToByteArray("d:\\铃声-g726-32bps.g726");
        FileUtils.writeByteArrayToFile(new WavCodec().fromPCM(ffmpegFrameGrabber.grabPcm(fileBytes)),
            "d:\\铃声-g726-32bps-2.wav");
        ffmpegFrameGrabber.close();
    }

}