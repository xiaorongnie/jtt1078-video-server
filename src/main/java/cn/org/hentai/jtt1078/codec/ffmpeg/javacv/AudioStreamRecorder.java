package cn.org.hentai.jtt1078.codec.ffmpeg.javacv;

import static org.bytedeco.ffmpeg.global.avcodec.AV_PKT_FLAG_KEY;
import static org.bytedeco.ffmpeg.global.avcodec.av_jni_set_java_vm;
import static org.bytedeco.ffmpeg.global.avcodec.av_new_packet;
import static org.bytedeco.ffmpeg.global.avcodec.avcodec_alloc_context3;
import static org.bytedeco.ffmpeg.global.avcodec.avcodec_fill_audio_frame;
import static org.bytedeco.ffmpeg.global.avcodec.avcodec_find_encoder;
import static org.bytedeco.ffmpeg.global.avcodec.avcodec_find_encoder_by_name;
import static org.bytedeco.ffmpeg.global.avcodec.avcodec_free_context;
import static org.bytedeco.ffmpeg.global.avcodec.avcodec_open2;
import static org.bytedeco.ffmpeg.global.avcodec.avcodec_receive_packet;
import static org.bytedeco.ffmpeg.global.avcodec.avcodec_register_all;
import static org.bytedeco.ffmpeg.global.avcodec.avcodec_send_frame;
import static org.bytedeco.ffmpeg.global.avdevice.avdevice_register_all;
import static org.bytedeco.ffmpeg.global.avformat.av_register_all;
import static org.bytedeco.ffmpeg.global.avformat.avformat_network_init;
import static org.bytedeco.ffmpeg.global.avutil.AVERROR_EOF;
import static org.bytedeco.ffmpeg.global.avutil.AVMEDIA_TYPE_AUDIO;
import static org.bytedeco.ffmpeg.global.avutil.AV_NOPTS_VALUE;
import static org.bytedeco.ffmpeg.global.avutil.AV_SAMPLE_FMT_DBL;
import static org.bytedeco.ffmpeg.global.avutil.AV_SAMPLE_FMT_DBLP;
import static org.bytedeco.ffmpeg.global.avutil.AV_SAMPLE_FMT_FLT;
import static org.bytedeco.ffmpeg.global.avutil.AV_SAMPLE_FMT_FLTP;
import static org.bytedeco.ffmpeg.global.avutil.AV_SAMPLE_FMT_S16;
import static org.bytedeco.ffmpeg.global.avutil.AV_SAMPLE_FMT_S16P;
import static org.bytedeco.ffmpeg.global.avutil.AV_SAMPLE_FMT_S32;
import static org.bytedeco.ffmpeg.global.avutil.AV_SAMPLE_FMT_S32P;
import static org.bytedeco.ffmpeg.global.avutil.AV_SAMPLE_FMT_U8;
import static org.bytedeco.ffmpeg.global.avutil.AV_SAMPLE_FMT_U8P;
import static org.bytedeco.ffmpeg.global.avutil.av_dict_free;
import static org.bytedeco.ffmpeg.global.avutil.av_dict_set;
import static org.bytedeco.ffmpeg.global.avutil.av_frame_alloc;
import static org.bytedeco.ffmpeg.global.avutil.av_frame_free;
import static org.bytedeco.ffmpeg.global.avutil.av_free;
import static org.bytedeco.ffmpeg.global.avutil.av_get_bytes_per_sample;
import static org.bytedeco.ffmpeg.global.avutil.av_get_default_channel_layout;
import static org.bytedeco.ffmpeg.global.avutil.av_malloc;
import static org.bytedeco.ffmpeg.global.avutil.av_sample_fmt_is_planar;
import static org.bytedeco.ffmpeg.global.avutil.av_samples_get_buffer_size;
import static org.bytedeco.ffmpeg.global.swresample.swr_alloc_set_opts;
import static org.bytedeco.ffmpeg.global.swresample.swr_convert;
import static org.bytedeco.ffmpeg.global.swresample.swr_free;
import static org.bytedeco.ffmpeg.global.swresample.swr_init;
import static org.bytedeco.ffmpeg.presets.avutil.AVERROR_EAGAIN;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.Map.Entry;

import org.apache.commons.lang.ArrayUtils;
import org.bytedeco.ffmpeg.avcodec.AVCodec;
import org.bytedeco.ffmpeg.avcodec.AVCodecContext;
import org.bytedeco.ffmpeg.avcodec.AVPacket;
import org.bytedeco.ffmpeg.avutil.AVDictionary;
import org.bytedeco.ffmpeg.avutil.AVFrame;
import org.bytedeco.ffmpeg.swresample.SwrContext;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacpp.FloatPointer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.PointerPointer;
import org.bytedeco.javacpp.PointerScope;
import org.bytedeco.javacpp.ShortPointer;
import org.bytedeco.javacv.FFmpegLockCallback;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameRecorder;

/**
 * 音频流编码器
 * 
 * @author eason
 * @date 2021/05/26
 */
public class AudioStreamRecorder extends FrameRecorder {

    public static class Exception extends FrameRecorder.Exception {
        private static final long serialVersionUID = -6646799993794674906L;

        public Exception(String message) {
            super(message + " (For more details, make sure FFmpegLogCallback.set() has been called.)");
        }

        public Exception(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private static Exception loadingException = null;

    @SuppressWarnings("deprecation")
    public static void tryLoad() throws Exception {
        if (loadingException != null) {
            throw loadingException;
        } else {
            try {
                Loader.load(org.bytedeco.ffmpeg.global.avutil.class);
                Loader.load(org.bytedeco.ffmpeg.global.swresample.class);
                Loader.load(org.bytedeco.ffmpeg.global.avcodec.class);
                Loader.load(org.bytedeco.ffmpeg.global.avformat.class);
                Loader.load(org.bytedeco.ffmpeg.global.swscale.class);

                /* initialize libavcodec, and register all codecs and formats */
                av_jni_set_java_vm(Loader.getJavaVM(), null);
                avcodec_register_all();
                av_register_all();
                avformat_network_init();

                Loader.load(org.bytedeco.ffmpeg.global.avdevice.class);
                avdevice_register_all();
            } catch (Throwable t) {
                if (t instanceof Exception) {
                    throw loadingException = (Exception)t;
                } else {
                    throw loadingException = new Exception("Failed to load " + AudioStreamRecorder.class, t);
                }
            }
        }
    }

    static {
        try {
            tryLoad();
            FFmpegLockCallback.init();
        } catch (Exception ex) {
        }
    }

    public void release() throws Exception {
        synchronized (org.bytedeco.ffmpeg.global.avcodec.class) {
            releaseUnsafe();
        }
    }

    public synchronized void releaseUnsafe() throws Exception {
        started = false;

        if (plane_ptr != null && plane_ptr2 != null) {
            plane_ptr.releaseReference();
            plane_ptr2.releaseReference();
            plane_ptr = plane_ptr2 = null;
        }

        if (audio_pkt != null) {
            audio_pkt.releaseReference();
            audio_pkt = null;
        }

        if (audio_c != null) {
            avcodec_free_context(audio_c);
            audio_c = null;
        }

        if (frame != null) {
            av_frame_free(frame);
            frame = null;
        }
        if (samples_in != null) {
            for (int i = 0; i < samples_in.length; i++) {
                if (samples_in[i] != null) {
                    samples_in[i].releaseReference();
                }
            }
            samples_in = null;
        }
        if (samples_out != null) {
            for (int i = 0; i < samples_out.length; i++) {
                av_free(samples_out[i].position(0));
            }
            samples_out = null;
        }

        if (samples_convert_ctx != null) {
            swr_free(samples_convert_ctx);
            samples_convert_ctx = null;
        }

    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        release();
    }

    /**
     * PCM原始数据
     */
    private AVFrame frame;
    /**
     * 编码之后字节流
     */
    private byte[] packet_data;
    /**
     * 输入音频数据指针
     */
    private Pointer[] samples_in;
    /**
     * 输出音频PCM数据流
     */
    private BytePointer[] samples_out;
    /**
     * 采样数
     */
    private int audio_input_frame_size;
    /**
     * 音频编码器
     */
    private AVCodec audio_codec;
    /**
     * 编码器上下文
     */
    private AVCodecContext audio_c;
    /**
     * 重采样上下文
     */
    private SwrContext samples_convert_ctx;
    /**
     * 重采样前后指针
     */
    private PointerPointer<?> plane_ptr, plane_ptr2;

    /**
     * 输出buf大小
     */
    private int audio_outbuf_size;
    /**
     * 编码数据包
     */
    private AVPacket audio_pkt;

    private int bitsPerCodedSample;

    public int getBitsPerCodedSample() {
        return bitsPerCodedSample;
    }

    public void setBitsPerCodedSample(int bitsPerCodedSample) {
        this.bitsPerCodedSample = bitsPerCodedSample;
    }

    private volatile boolean started = false;

    /** Returns best guess for timestamp in microseconds... */
    @Override
    public long getTimestamp() {
        return Math.round(getFrameNumber() * 1000000L / getFrameRate());
    }

    @Override
    public void setTimestamp(long timestamp) {
        setFrameNumber((int)Math.round(timestamp * getFrameRate() / 1000000L));
    }

    @Override
    public void start() throws Exception {
        synchronized (org.bytedeco.ffmpeg.global.avcodec.class) {
            startUnsafe();
        }
    }

    @SuppressWarnings({"resource", "rawtypes", "unchecked"})
    public synchronized void startUnsafe() throws Exception {
        try (PointerScope scope = new PointerScope()) {
            int ret;
            frame = null;
            audio_c = null;
            plane_ptr = new PointerPointer(AVFrame.AV_NUM_DATA_POINTERS).retainReference();
            plane_ptr2 = new PointerPointer(AVFrame.AV_NUM_DATA_POINTERS).retainReference();
            audio_pkt = new AVPacket().retainReference();
            started = true;

            /** 查找解码器 */
            if ((audio_codec = avcodec_find_encoder_by_name(audioCodecName)) == null
                && (audio_codec = avcodec_find_encoder(getAudioCodec())) == null) {
                releaseUnsafe();
                throw new Exception("avcodec_find_encoder() error: Audio codec not found.");
            }
            /** 配置编码器 */
            if ((audio_c = avcodec_alloc_context3(audio_codec)) == null) {
                releaseUnsafe();
                throw new Exception("avcodec_alloc_context3() error: Could not allocate audio encoding context.");
            }
            initCodec();
            /** 打开编码器 */
            AVDictionary options = new AVDictionary(null);
            for (Entry<String, String> e : audioOptions.entrySet()) {
                av_dict_set(options, e.getKey(), e.getValue(), 0);
            }
            /* open the codec */
            if ((ret = avcodec_open2(audio_c, audio_codec, options)) < 0) {
                releaseUnsafe();
                av_dict_free(options);
                throw new Exception("avcodec_open2() error " + ret + ": Could not open audio codec.");
            }
            av_dict_free(options);
            int planes = av_sample_fmt_is_planar(audio_c.sample_fmt()) != 0 ? (int)audio_c.channels() : 1;
            int data_size = av_samples_get_buffer_size((IntPointer)null, audio_c.channels(), audio_input_frame_size,
                audio_c.sample_fmt(), 1) / planes;
            samples_out = new BytePointer[planes];
            for (int i = 0; i < samples_out.length; i++) {
                samples_out[i] = new BytePointer(av_malloc(data_size)).capacity(data_size);
            }
            samples_in = new Pointer[AVFrame.AV_NUM_DATA_POINTERS];

            /* allocate the audio frame */
            if ((frame = av_frame_alloc()) == null) {
                releaseUnsafe();
                throw new Exception("av_frame_alloc() error: Could not allocate audio frame.");
            }
            frame.pts(0); // magic required by libvorbis and webm
            audio_outbuf_size = 256 * 1024;
        }
    }

    /**
     * 初始化解码器
     */
    private void initCodec() {
        // Enable multithreading when available
        audio_c.thread_count(0);
        audio_c.codec_type(AVMEDIA_TYPE_AUDIO);
        // 采样率
        audio_c.sample_rate(getSampleRate());
        // 表示编码压缩bit值与采样率的bit值之比 g726需要此参数
        if (bitsPerCodedSample > 0) {
            audio_c.bits_per_coded_sample(bitsPerCodedSample);
        }
        // 通道数
        audio_c.channels(getAudioChannels());
        // 通道布局
        audio_c.channel_layout(av_get_default_channel_layout(audioChannels));
        // 音频采样格式
        audio_c.sample_fmt(getSampleFormat());
        // 默认一次打包320个采样点
        int frame_size = getFrameNumber();
        audio_c.frame_size(frame_size <= 0 ? 320 : frame_size);
        audio_input_frame_size = audio_c.frame_size();
        // 码率
        audio_c.bit_rate(getAudioBitrate());
        switch (audio_c.sample_fmt()) {
            case AV_SAMPLE_FMT_U8:
            case AV_SAMPLE_FMT_U8P:
                audio_c.bits_per_raw_sample(8);
                break;
            case AV_SAMPLE_FMT_S16:
            case AV_SAMPLE_FMT_S16P:
                audio_c.bits_per_raw_sample(16);
                break;
            case AV_SAMPLE_FMT_S32:
            case AV_SAMPLE_FMT_S32P:
                audio_c.bits_per_raw_sample(32);
                break;
            case AV_SAMPLE_FMT_FLT:
            case AV_SAMPLE_FMT_FLTP:
                audio_c.bits_per_raw_sample(32);
                break;
            case AV_SAMPLE_FMT_DBL:
            case AV_SAMPLE_FMT_DBLP:
                audio_c.bits_per_raw_sample(64);
                break;
            default:
                assert false;
        }
    }

    @Override
    public void record(Frame frame) throws Exception {

    }

    /**
     * 编码单声道16bit音频PCM数据
     * 
     * @param data
     *            16bit,单声道PCM
     * @return
     * @throws Exception
     */
    public synchronized byte[] recordShortSamples(byte[] data) {
        if (data == null) {
            return null;
        }
        try {
            short[] samples_short_buf = new short[data.length / 2];
            ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(samples_short_buf);
            ShortBuffer shortBuffer = ShortBuffer.wrap(samples_short_buf);
            Buffer[] samples = new Buffer[] {shortBuffer};
            return recordSamples(0, 0, samples);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public byte[] recordSamples(Buffer... samples) throws Exception {
        return recordSamples(0, 0, samples);
    }

    @SuppressWarnings("unchecked")
    public synchronized byte[] recordSamples(int sampleRate, int audioChannels, Buffer... samples) throws Exception {
        try (PointerScope scope = new PointerScope()) {
            if (!started) {
                throw new Exception("start() was not called successfully!");
            }
            packet_data = null;
            int ret;
            // 输入PCM采样率,默认8K
            if (sampleRate <= 0) {
                sampleRate = audio_c.sample_rate();
            }
            // 输入PCM音频通道，默认1
            if (audioChannels <= 0) {
                audioChannels = audio_c.channels();
            }
            // 输入PCM数据大小
            int inputSize = samples != null ? samples[0].limit() - samples[0].position() : 0;
            // 输PCM采样位数,默认16bit
            int inputFormat = getSampleFormat();
            // 输入PCM音频通道，默认1
            int inputChannels = samples != null && samples.length > 1 ? 1 : audioChannels;
            // 输入PCM采样深度,默认16bit/8=2
            int inputDepth = 0;
            // 输出音频采样位数，默认16bit
            int outputFormat = audio_c.sample_fmt();
            // 输出音频通道,默认1
            int outputChannels = samples_out.length > 1 ? 1 : audio_c.channels();
            // 输出音频采样深度,默认2
            int outputDepth = av_get_bytes_per_sample(outputFormat);

            if (samples != null && samples[0] instanceof ByteBuffer) {
                // 输入音频采样位数=8bit
                inputFormat = samples.length > 1 ? AV_SAMPLE_FMT_U8P : AV_SAMPLE_FMT_U8;
                inputDepth = 1;
                for (int i = 0; i < samples.length; i++) {
                    ByteBuffer b = (ByteBuffer)samples[i];
                    if (samples_in[i] instanceof BytePointer && samples_in[i].capacity() >= inputSize && b.hasArray()) {
                        ((BytePointer)samples_in[i]).position(0).put(b.array(), b.position(), inputSize);
                    } else {
                        if (samples_in[i] != null) {
                            samples_in[i].releaseReference();
                        }
                        samples_in[i] = new BytePointer(b).retainReference();
                    }
                }
            } else if (samples != null && samples[0] instanceof ShortBuffer) {
                // 输入音频采样位数=16bit
                inputFormat = samples.length > 1 ? AV_SAMPLE_FMT_S16P : AV_SAMPLE_FMT_S16;
                inputDepth = 2;
                // 映射C++指针
                for (int i = 0; i < samples.length; i++) {
                    ShortBuffer b = (ShortBuffer)samples[i];
                    if (samples_in[i] instanceof ShortPointer && samples_in[i].capacity() >= inputSize
                        && b.hasArray()) {
                        ((ShortPointer)samples_in[i]).position(0).put(b.array(), samples[i].position(), inputSize);
                    } else {
                        if (samples_in[i] != null) {
                            samples_in[i].releaseReference();
                        }
                        samples_in[i] = new ShortPointer(b).retainReference();
                    }
                }
            } else if (samples != null && samples[0] instanceof IntBuffer) {
                // 输入音频采样位数=32bit
                inputFormat = samples.length > 1 ? AV_SAMPLE_FMT_S32P : AV_SAMPLE_FMT_S32;
                inputDepth = 4;
                for (int i = 0; i < samples.length; i++) {
                    IntBuffer b = (IntBuffer)samples[i];
                    if (samples_in[i] instanceof IntPointer && samples_in[i].capacity() >= inputSize && b.hasArray()) {
                        ((IntPointer)samples_in[i]).position(0).put(b.array(), samples[i].position(), inputSize);
                    } else {
                        if (samples_in[i] != null) {
                            samples_in[i].releaseReference();
                        }
                        samples_in[i] = new IntPointer(b).retainReference();
                    }
                }
            } else if (samples != null && samples[0] instanceof FloatBuffer) {
                // 输入音频采样位数=64bit
                inputFormat = samples.length > 1 ? AV_SAMPLE_FMT_FLTP : AV_SAMPLE_FMT_FLT;
                inputDepth = 4;
                for (int i = 0; i < samples.length; i++) {
                    FloatBuffer b = (FloatBuffer)samples[i];
                    if (samples_in[i] instanceof FloatPointer && samples_in[i].capacity() >= inputSize
                        && b.hasArray()) {
                        ((FloatPointer)samples_in[i]).position(0).put(b.array(), b.position(), inputSize);
                    } else {
                        if (samples_in[i] != null) {
                            samples_in[i].releaseReference();
                        }
                        samples_in[i] = new FloatPointer(b).retainReference();
                    }
                }
            } else if (samples != null && samples[0] instanceof DoubleBuffer) {
                // 输入音频采样位数=64bit
                inputFormat = samples.length > 1 ? AV_SAMPLE_FMT_DBLP : AV_SAMPLE_FMT_DBL;
                inputDepth = 8;
                for (int i = 0; i < samples.length; i++) {
                    DoubleBuffer b = (DoubleBuffer)samples[i];
                    if (samples_in[i] instanceof DoublePointer && samples_in[i].capacity() >= inputSize
                        && b.hasArray()) {
                        ((DoublePointer)samples_in[i]).position(0).put(b.array(), b.position(), inputSize);
                    } else {
                        if (samples_in[i] != null) {
                            samples_in[i].releaseReference();
                        }
                        samples_in[i] = new DoublePointer(b).retainReference();
                    }
                }
            } else if (samples != null) {
                throw new Exception("Audio samples Buffer has unsupported type: " + samples);
            }

            // SwrContext重采样结构体
            if (samples_convert_ctx == null) {
                /**
                 * 参数1：重采样上下文
                 * 
                 * 参数2：输出的layout, 如：5.1声道…
                 * 
                 * 参数3：输出的样本格式。Float, S16, S24
                 * 
                 * 参数4：输出的样本率。可以不变。
                 * 
                 * 参数5：输入的layout。
                 * 
                 * 参数6：输入的样本格式。
                 * 
                 * 参数7：输入的样本率。
                 * 
                 * 参数8，参数9，日志，不用管，可直接传0
                 */
                samples_convert_ctx = swr_alloc_set_opts(samples_convert_ctx, audio_c.channel_layout(), outputFormat,
                    audio_c.sample_rate(), av_get_default_channel_layout(audioChannels), inputFormat, sampleRate, 0,
                    null);
                if (samples_convert_ctx == null) {
                    throw new Exception("swr_alloc_set_opts() error: Cannot allocate the conversion context.");
                } else if ((ret = swr_init(samples_convert_ctx)) < 0) {
                    throw new Exception("swr_init() error " + ret + ": Cannot initialize the conversion context.");
                }
            }

            // 设置输入音频指针数据位置
            for (int i = 0; samples != null && i < samples.length; i++) {
                samples_in[i].position(samples_in[i].position() * inputDepth)
                    .limit((samples_in[i].position() + inputSize) * inputDepth);
            }
            // 开始重采样
            while (true) {
                // 输入采样点
                int inputCount =
                    (int)Math.min(
                        samples != null
                            ? (samples_in[0].limit() - samples_in[0].position()) / (inputChannels * inputDepth) : 0,
                        Integer.MAX_VALUE);
                // 输出采样点
                int outputCount =
                    (int)Math.min((samples_out[0].limit() - samples_out[0].position()) / (outputChannels * outputDepth),
                        Integer.MAX_VALUE);
                inputCount = Math.min(inputCount,
                    (outputCount * sampleRate + audio_c.sample_rate() - 1) / audio_c.sample_rate());

                for (int i = 0; samples != null && i < samples.length; i++) {
                    plane_ptr.put(i, samples_in[i]);
                }

                for (int i = 0; i < samples_out.length; i++) {
                    plane_ptr2.put(i, samples_out[i]);
                }

                /**
                 * 重采样
                 * 
                 * 参数1：音频重采样的上下文
                 * 
                 * 参数2：输出的指针。传递的输出的数组
                 * 
                 * 参数3：输出的样本数量，不是字节数。单通道的样本数量。
                 * 
                 * 参数4：输入的数组，AVFrame解码出来的DATA
                 * 
                 * 参数5：输入的单通道的样本数量。
                 */
                // System.out.println(samples_convert_ctx);
                // System.out.println(plane_ptr2);
                // System.out.println(outputCount);
                // System.out.println(plane_ptr);
                // System.out.println(inputCount);
                if ((ret = swr_convert(samples_convert_ctx, plane_ptr2, outputCount, plane_ptr, inputCount)) < 0) {
                    throw new Exception("swr_convert() error " + ret + ": Cannot convert audio samples.");
                } else if (ret == 0) {
                    break;
                }

                // 修改输入索引
                for (int i = 0; samples != null && i < samples.length; i++) {
                    samples_in[i].position(samples_in[i].position() + inputCount * inputChannels * inputDepth);
                }

                // 修改输出索引
                for (int i = 0; i < samples_out.length; i++) {
                    samples_out[i].position(samples_out[i].position() + ret * outputChannels * outputDepth);
                }

                if (samples == null || samples_out[0].position() >= samples_out[0].limit()) {
                    writeSamples(audio_input_frame_size);
                }
            }
            return packet_data;
        }
    }

    /**
     * 输出采样点PCM
     * 
     * @param nb_samples
     *            采样数
     * @return
     * @throws Exception
     */
    private void writeSamples(int nb_samples) throws Exception {
        if (samples_out == null || samples_out.length == 0) {
            return;
        }
        frame.nb_samples(nb_samples);
        avcodec_fill_audio_frame(frame, audio_c.channels(), audio_c.sample_fmt(), samples_out[0],
            (int)samples_out[0].position(), 0);
        for (int i = 0; i < samples_out.length; i++) {
            int linesize = 0;
            if (samples_out[0].position() > 0 && samples_out[0].position() < samples_out[0].limit()) {
                // align the end of the buffer to a 32-byte boundary as sometimes required by FFmpeg
                linesize = ((int)samples_out[i].position() + 31) & ~31;
            } else {
                linesize = (int)Math.min(samples_out[i].limit(), Integer.MAX_VALUE);
            }
            frame.data(i, samples_out[i].position(0));
            frame.linesize(i, linesize);
        }
        frame.channels(audio_c.channels());
        frame.format(audio_c.sample_fmt());
        frame.quality(audio_c.global_quality());
        record(frame);
    }

    private void record(AVFrame frame) throws Exception {
        int ret;
        if ((ret = avcodec_send_frame(audio_c, frame)) < 0 && frame != null) {
            throw new Exception("avcodec_send_frame() error " + ret + ": Error sending an audio frame for encoding.");
        }
        if (frame != null) {
            frame.pts(frame.pts() + frame.nb_samples()); // magic required by libvorbis and webm
        }

        while (ret >= 0) {
            av_new_packet(audio_pkt, audio_outbuf_size);
            ret = avcodec_receive_packet(audio_c, audio_pkt);
            if (ret == AVERROR_EAGAIN() || ret == AVERROR_EOF()) {
                break;
            } else if (ret < 0) {
                throw new Exception("avcodec_receive_packet() error " + ret + ": Error during audio encoding.");
            }
            if (audio_pkt.pts() != AV_NOPTS_VALUE) {
                // audio_pkt.pts(av_rescale_q(audio_pkt.pts(), audio_c.time_base(), audio_st.time_base()));
            }
            if (audio_pkt.dts() != AV_NOPTS_VALUE) {
                // audio_pkt.dts(av_rescale_q(audio_pkt.dts(), audio_c.time_base(), audio_st.time_base()));
            }
            audio_pkt.flags(audio_pkt.flags() | AV_PKT_FLAG_KEY);
            /* write the compressed frame in the media file */
            writePacket(audio_pkt);
        }
    }

    public void writePacket(AVPacket avPacket) {
        BytePointer p = audio_pkt.data();
        byte[] data = new byte[audio_pkt.size()];
        for (int i = 0; i < audio_pkt.size(); i++) {
            data[i] = p.get(i);
        }
        packet_data = ArrayUtils.addAll(packet_data, data);
    }

    @Override
    public void flush() throws org.bytedeco.javacv.FrameRecorder.Exception {

    }

    @Override
    public void stop() throws org.bytedeco.javacv.FrameRecorder.Exception {

    }

}
