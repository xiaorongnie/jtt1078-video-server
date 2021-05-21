package cn.org.hentai.jtt1078.ffmpeg;
/*
 * Copyright (C) 2009-2021 Samuel Audet
 *
 * Licensed either under the Apache License, Version 2.0, or (at your option) under the terms of the GNU General Public
 * License as published by the Free Software Foundation (subject to the "Classpath" exception), either version 2, or any
 * later version (collectively, the "License"); you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0 http://www.gnu.org/licenses/
 * http://www.gnu.org/software/classpath/license.html
 *
 * or as provided in the LICENSE.txt file that accompanied this code. Unless required by applicable law or agreed to in
 * writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 * Based on the avcodec_sample.0.5.0.c file available at
 * http://web.me.com/dhoerl/Home/Tech_Blog/Entries/2009/1/22_Revised_avcodec_sample.c_files/avcodec_sample.0.5.0.c by
 * Martin Böhme, Stephen Dranger, and David Hoerl as well as on the decoding_encoding.c file included in FFmpeg 0.11.1,
 * and on the decode_video.c file included in FFmpeg 4.4, which is covered by the following copyright notice:
 *
 * Copyright (c) 2001 Fabrice Bellard
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

import static org.bytedeco.ffmpeg.global.avcodec.av_jni_set_java_vm;
import static org.bytedeco.ffmpeg.global.avcodec.av_packet_unref;
import static org.bytedeco.ffmpeg.global.avcodec.avcodec_alloc_context3;
import static org.bytedeco.ffmpeg.global.avcodec.avcodec_find_decoder;
import static org.bytedeco.ffmpeg.global.avcodec.avcodec_find_decoder_by_name;
import static org.bytedeco.ffmpeg.global.avcodec.avcodec_free_context;
import static org.bytedeco.ffmpeg.global.avcodec.avcodec_open2;
import static org.bytedeco.ffmpeg.global.avcodec.avcodec_receive_frame;
import static org.bytedeco.ffmpeg.global.avcodec.avcodec_register_all;
import static org.bytedeco.ffmpeg.global.avcodec.avcodec_send_packet;
import static org.bytedeco.ffmpeg.global.avdevice.avdevice_register_all;
import static org.bytedeco.ffmpeg.global.avformat.av_register_all;
import static org.bytedeco.ffmpeg.global.avformat.avformat_network_init;
import static org.bytedeco.ffmpeg.global.avutil.AVERROR_EOF;
import static org.bytedeco.ffmpeg.global.avutil.AV_PIX_FMT_NONE;
import static org.bytedeco.ffmpeg.global.avutil.AV_SAMPLE_FMT_DBL;
import static org.bytedeco.ffmpeg.global.avutil.AV_SAMPLE_FMT_DBLP;
import static org.bytedeco.ffmpeg.global.avutil.AV_SAMPLE_FMT_FLT;
import static org.bytedeco.ffmpeg.global.avutil.AV_SAMPLE_FMT_FLTP;
import static org.bytedeco.ffmpeg.global.avutil.AV_SAMPLE_FMT_NONE;
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
import static org.bytedeco.ffmpeg.global.swresample.swr_get_out_samples;
import static org.bytedeco.ffmpeg.global.swresample.swr_init;
import static org.bytedeco.ffmpeg.presets.avutil.AVERROR_EAGAIN;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.Map.Entry;

import org.bytedeco.ffmpeg.avcodec.AVCodec;
import org.bytedeco.ffmpeg.avcodec.AVCodecContext;
import org.bytedeco.ffmpeg.avcodec.AVPacket;
import org.bytedeco.ffmpeg.avutil.AVDictionary;
import org.bytedeco.ffmpeg.avutil.AVFrame;
import org.bytedeco.ffmpeg.swresample.SwrContext;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.PointerPointer;
import org.bytedeco.javacpp.PointerScope;
import org.bytedeco.javacv.FFmpegLockCallback;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;

import cn.org.hentai.jtt1078.util.ByteBufUtils;

/**
 * 音频抓帧器
 * 
 * @author eason
 * @date 2021/05/21
 */
public class AudioStreamGrabber extends FrameGrabber {

    public static class Exception extends FrameGrabber.Exception {

        private static final long serialVersionUID = -7729718318093135207L;

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

                // Register all formats and codecs
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
                    throw loadingException = new Exception("Failed to load " + AudioStreamGrabber.class, t);
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

    public AudioStreamGrabber() {
        this.pixelFormat = AV_PIX_FMT_NONE;
        this.sampleFormat = AV_SAMPLE_FMT_NONE;
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

        if (pkt != null) {
            if (pkt.stream_index() != -1) {
                av_packet_unref(pkt);
            }
            pkt.releaseReference();
            pkt = null;
        }

        // Free the audio samples frame
        if (samples_frame != null) {
            av_frame_free(samples_frame);
            samples_frame = null;
        }

        // Close the audio codec
        if (audio_c != null) {
            avcodec_free_context(audio_c);
            audio_c = null;
        }

        if (samples_ptr_out != null) {
            for (int i = 0; i < samples_ptr_out.length; i++) {
                av_free(samples_ptr_out[i].position(0));
            }
            samples_ptr_out = null;
            samples_buf_out = null;
        }

        if (samples_convert_ctx != null) {
            swr_free(samples_convert_ctx);
            samples_convert_ctx = null;
        }

        frame = null;
        timestamp = 0;
        frameNumber = 0;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        release();
    }

    /**
     * 解码器
     */
    private AVCodecContext audio_c;
    /**
     * 解码后数据结构体
     */
    private AVFrame samples_frame;
    private BytePointer[] samples_ptr;
    private Buffer[] samples_buf;
    private BytePointer[] samples_ptr_out;
    private Buffer[] samples_buf_out;
    @SuppressWarnings("rawtypes")
    private PointerPointer plane_ptr, plane_ptr2;
    private AVPacket pkt;
    private SwrContext samples_convert_ctx;
    private int samples_channels, samples_format, samples_rate;
    private Frame frame;
    private int bitsPerCodedSample;

    private volatile boolean started = false;

    public int getBitsPerCodedSample() {
        return bitsPerCodedSample;
    }

    public void setBitsPerCodedSample(int bitsPerCodedSample) {
        this.bitsPerCodedSample = bitsPerCodedSample;
    }

    /** Calls {@code start(true)}. */
    @Override
    public void start() throws Exception {
        start(true);
    }

    /** Set findStreamInfo to false to minimize startup time, at the expense of robustness. */
    public void start(boolean findStreamInfo) throws Exception {
        synchronized (org.bytedeco.ffmpeg.global.avcodec.class) {
            startUnsafe(findStreamInfo);
        }
    }

    public void startUnsafe() throws Exception {
        startUnsafe(true);
    }

    @Override
    public void trigger() throws org.bytedeco.javacv.FrameGrabber.Exception {
        //
    }

    @SuppressWarnings({"rawtypes", "resource", "unchecked"})
    public synchronized void startUnsafe(boolean findStreamInfo) throws Exception {
        try (PointerScope scope = new PointerScope()) {
            int ret;
            audio_c = null;
            plane_ptr = new PointerPointer(AVFrame.AV_NUM_DATA_POINTERS).retainReference();
            plane_ptr2 = new PointerPointer(AVFrame.AV_NUM_DATA_POINTERS).retainReference();
            pkt = new AVPacket().retainReference();

            frame = new Frame();
            timestamp = 0;
            frameNumber = 0;

            pkt.stream_index(-1);

            /** 查找解码器 */
            AVCodec codec = avcodec_find_decoder_by_name(getAudioCodecName());
            if (codec == null) {
                codec = avcodec_find_decoder(getAudioCodec());
            }
            if (codec == null) {
                throw new Exception("avcodec_find_decoder() error: Unsupported audio format or codec not found: "
                    + getAudioCodec() + ".");
            }

            /** 配置解码器 */
            if ((audio_c = avcodec_alloc_context3(codec)) == null) {
                throw new Exception("avcodec_alloc_context3() error: Could not allocate audio decoding context.");
            }
            initCodec();
            /** 打开解码器 */
            AVDictionary options = new AVDictionary(null);
            for (Entry<String, String> e : audioOptions.entrySet()) {
                av_dict_set(options, e.getKey(), e.getValue(), 0);
            }
            if ((ret = avcodec_open2(audio_c, codec, options)) < 0) {
                throw new Exception("avcodec_open2() error " + ret + ": Could not open audio codec.");
            }
            av_dict_free(options);

            /** Allocate audio samples frame */
            if ((samples_frame = av_frame_alloc()) == null) {
                throw new Exception("av_frame_alloc() error: Could not allocate audio frame.");
            }

            samples_ptr = new BytePointer[] {null};
            samples_buf = new Buffer[] {null};

            started = true;
        }
    }

    /**
     * 初始化解码器
     */
    private void initCodec() {
        // Enable multithreading when available
        audio_c.thread_count(0);
        // 采样率
        audio_c.sample_rate(getSampleRate());
        // 表示编码压缩bit值与采样率的bit值之比 g726需要此参数
        if (bitsPerCodedSample > 0) {
            audio_c.bits_per_coded_sample(bitsPerCodedSample);
        }
        // 通道数
        audio_c.channels(getAudioChannels());
        // 音频采样格式
        audio_c.sample_fmt(getSampleFormat());
        // 码率
        audio_c.bit_rate(getAudioBitrate());
    }

    @Override
    public void stop() throws Exception {
        release();
    }

    @SuppressWarnings({"unchecked", "resource"})
    private void processSamples() throws Exception {
        int ret;

        int sample_format = samples_frame.format();
        int planes = av_sample_fmt_is_planar(sample_format) != 0 ? (int)samples_frame.channels() : 1;
        int data_size = av_samples_get_buffer_size((IntPointer)null, audio_c.channels(), samples_frame.nb_samples(),
            audio_c.sample_fmt(), 1) / planes;
        if (samples_buf == null || samples_buf.length != planes) {
            samples_ptr = new BytePointer[planes];
            samples_buf = new Buffer[planes];
        }
        frame.sampleRate = audio_c.sample_rate();
        frame.audioChannels = audio_c.channels();
        frame.samples = samples_buf;
        frame.opaque = samples_frame;
        int sample_size = data_size / av_get_bytes_per_sample(sample_format);
        for (int i = 0; i < planes; i++) {
            BytePointer p = samples_frame.data(i);
            if (!p.equals(samples_ptr[i]) || samples_ptr[i].capacity() < data_size) {
                samples_ptr[i] = p.capacity(data_size);
                ByteBuffer b = p.asBuffer();
                switch (sample_format) {
                    case AV_SAMPLE_FMT_U8:
                    case AV_SAMPLE_FMT_U8P:
                        samples_buf[i] = b;
                        break;
                    case AV_SAMPLE_FMT_S16:
                    case AV_SAMPLE_FMT_S16P:
                        samples_buf[i] = b.asShortBuffer();
                        break;
                    case AV_SAMPLE_FMT_S32:
                    case AV_SAMPLE_FMT_S32P:
                        samples_buf[i] = b.asIntBuffer();
                        break;
                    case AV_SAMPLE_FMT_FLT:
                    case AV_SAMPLE_FMT_FLTP:
                        samples_buf[i] = b.asFloatBuffer();
                        break;
                    case AV_SAMPLE_FMT_DBL:
                    case AV_SAMPLE_FMT_DBLP:
                        samples_buf[i] = b.asDoubleBuffer();
                        break;
                    default:
                        assert false;
                }
            }
            samples_buf[i].position(0).limit(sample_size);
        }

        if (audio_c.channels() != getAudioChannels() || audio_c.sample_fmt() != getSampleFormat()
            || audio_c.sample_rate() != getSampleRate()) {
            if (samples_convert_ctx == null || samples_channels != getAudioChannels()
                || samples_format != getSampleFormat() || samples_rate != getSampleRate()) {
                samples_convert_ctx =
                    swr_alloc_set_opts(samples_convert_ctx, av_get_default_channel_layout(getAudioChannels()),
                        getSampleFormat(), getSampleRate(), av_get_default_channel_layout(audio_c.channels()),
                        audio_c.sample_fmt(), audio_c.sample_rate(), 0, null);
                if (samples_convert_ctx == null) {
                    throw new Exception("swr_alloc_set_opts() error: Cannot allocate the conversion context.");
                } else if ((ret = swr_init(samples_convert_ctx)) < 0) {
                    throw new Exception("swr_init() error " + ret + ": Cannot initialize the conversion context.");
                }
                samples_channels = getAudioChannels();
                samples_format = getSampleFormat();
                samples_rate = getSampleRate();
            }

            int sample_size_in = samples_frame.nb_samples();
            int planes_out = av_sample_fmt_is_planar(samples_format) != 0 ? (int)samples_frame.channels() : 1;
            int sample_size_out = swr_get_out_samples(samples_convert_ctx, sample_size_in);
            int sample_bytes_out = av_get_bytes_per_sample(samples_format);
            int buffer_size_out = sample_size_out * sample_bytes_out * (planes_out > 1 ? 1 : samples_channels);
            if (samples_buf_out == null || samples_buf.length != planes_out
                || samples_ptr_out[0].capacity() < buffer_size_out) {
                for (int i = 0; samples_ptr_out != null && i < samples_ptr_out.length; i++) {
                    av_free(samples_ptr_out[i].position(0));
                }
                samples_ptr_out = new BytePointer[planes_out];
                samples_buf_out = new Buffer[planes_out];

                for (int i = 0; i < planes_out; i++) {
                    samples_ptr_out[i] = new BytePointer(av_malloc(buffer_size_out)).capacity(buffer_size_out);
                    ByteBuffer b = samples_ptr_out[i].asBuffer();
                    switch (samples_format) {
                        case AV_SAMPLE_FMT_U8:
                        case AV_SAMPLE_FMT_U8P:
                            samples_buf_out[i] = b;
                            break;
                        case AV_SAMPLE_FMT_S16:
                        case AV_SAMPLE_FMT_S16P:
                            samples_buf_out[i] = b.asShortBuffer();
                            break;
                        case AV_SAMPLE_FMT_S32:
                        case AV_SAMPLE_FMT_S32P:
                            samples_buf_out[i] = b.asIntBuffer();
                            break;
                        case AV_SAMPLE_FMT_FLT:
                        case AV_SAMPLE_FMT_FLTP:
                            samples_buf_out[i] = b.asFloatBuffer();
                            break;
                        case AV_SAMPLE_FMT_DBL:
                        case AV_SAMPLE_FMT_DBLP:
                            samples_buf_out[i] = b.asDoubleBuffer();
                            break;
                        default:
                            assert false;
                    }
                }
            }
            frame.sampleRate = samples_rate;
            frame.audioChannels = samples_channels;
            frame.samples = samples_buf_out;

            if ((ret = swr_convert(samples_convert_ctx, plane_ptr.put(samples_ptr_out), sample_size_out,
                plane_ptr2.put(samples_ptr), sample_size_in)) < 0) {
                throw new Exception("swr_convert() error " + ret + ": Cannot convert audio samples.");
            }
            for (int i = 0; i < planes_out; i++) {
                samples_ptr_out[i].position(0).limit(ret * (planes_out > 1 ? 1 : samples_channels));
                samples_buf_out[i].position(0).limit(ret * (planes_out > 1 ? 1 : samples_channels));
            }
        }
    }

    public Frame grab() throws Exception {
        return null;
    }

    @SuppressWarnings("unchecked")
    public synchronized Frame grabAudio(byte[] data) throws Exception {
        try (PointerScope scope = new PointerScope()) {
            if (!started) {
                throw new Exception("start() was not called successfully!");
            }
            pkt.size(data.length);
            pkt.data(new BytePointer(data));
            int ret = avcodec_send_packet(audio_c, pkt);
            if (ret < 0) {
                throw new Exception(
                    "avcodec_send_packet() error " + ret + ": Error sending an audio packet for decoding.");
            }
            ret = avcodec_receive_frame(audio_c, samples_frame);
            if (ret == AVERROR_EAGAIN() || ret == AVERROR_EOF()) {
                return null;
            } else if (ret < 0) {
                throw new Exception("avcodec_receive_frame() error " + ret + ": Error during audio decoding.");
            }
            frame.samples = samples_buf;
            processSamples();
            frame.timestamp = timestamp;
            frame.keyFrame = samples_frame.key_frame() != 0;
            return frame;
        }
    }

    /**
     * 抓取PCM数据
     * 
     * @param data
     * @return
     * @throws Exception
     */
    public synchronized byte[] grabPcm(byte[] data) {
        try {
            Frame frame = grabAudio(data);
            if (frame == null) {
                return null;
            }
            Buffer buffer = frame.samples[0];
            ByteBuffer byteBuffer = ByteBufUtils.shortToByteValue((ShortBuffer)buffer);
            return byteBuffer.array();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

}
