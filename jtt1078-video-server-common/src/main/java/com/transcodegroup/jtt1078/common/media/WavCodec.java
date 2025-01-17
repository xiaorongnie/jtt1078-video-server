package com.transcodegroup.jtt1078.common.media;

import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;

/**
 * WAV音频编解码
 * 
 * @author eason
 * @date 2021/05/13
 */
public class WavCodec {

    /**
     * pcm采样率
     */
    private int pcmSample = 8000;

    /**
     * 音频通道数
     */
    private int channel = 1;

    /**
     * 2 每样本的数据位数，表示每个声道中各个样本的数据位数。
     */
    private int bitsPerSample;

    public WavCodec() {}

    public WavCodec(int pcmSample, int channel, int bitsPerSample) {
        this.pcmSample = pcmSample;
        this.channel = channel;
        this.bitsPerSample = bitsPerSample;
    }

    public byte[] toPCM(byte[] data) {
        // WAV和PCM的区别就是WAV在PCM的前面多了44字节
        return Arrays.copyOfRange(data, 44, data.length);
    }

    public byte[] fromPCM(byte[] data) {
        int pcmSize = data.length;
        // 填入参数，比特率等等。这里用的是16位单声道 8000hz
        WaveHeader header = new WaveHeader();
        // 从下个地址开始到文件尾的总字节数
        header.fileLength = data.length + (44 - 8);
        // 单声道为1，双声道为2
        header.channels = (short)channel;
        // 采样频率8khz
        header.samplesPerSec = pcmSample;
        // 采样位数
        header.bitsPerSample = (short)bitsPerSample;
        // 波形数据传输速率（每秒平均字节数）
        header.blockAlign = (short)(header.channels * header.bitsPerSample / 8);
        // 采样一次占用字节数 通道数×每样本的数据位数/8
        header.avgBytesPerSec = header.blockAlign * header.samplesPerSec;
        // DATA总数据长度字节
        header.dataHdrLeth = pcmSize;
        return ArrayUtils.addAll(header.getHeader(), data);
    }

}
