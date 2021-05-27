package cn.org.hentai.jtt1078.codec.algorithm;

import java.util.Arrays;

import org.apache.commons.lang.ArrayUtils;

import cn.org.hentai.jtt1078.entity.WaveHeader;

/**
 * WAV音频编解码
 * 
 * @author eason
 * @date 2021/05/13
 */
public class WavCodec extends AudioCodec {

    /**
     * pcm采样率
     */
    private static final int PCM_SAMPLE = 8000;

    /**
     * 音频通道数
     */
    private static final int CHANNEL = 1;

    @Override
    public byte[] toPCM(byte[] data) {
        // WAV和PCM的区别就是WAV在PCM的前面多了44字节
        return Arrays.copyOfRange(data, 44, data.length);
    }

    @Override
    public byte[] fromPCM(byte[] data) {
        int pcmSize = data.length;
        // 填入参数，比特率等等。这里用的是16位单声道 8000hz
        WaveHeader header = new WaveHeader();
        // 从下个地址开始到文件尾的总字节数
        header.fileLength = data.length + (44 - 8);
        // 单声道为1，双声道为2
        header.channels = CHANNEL;
        // 采样频率8khz
        header.samplesPerSec = PCM_SAMPLE;
        // 波形数据传输速率（每秒平均字节数）
        header.blockAlign = (short)(header.channels * header.bitsPerSample / 8);
        // 采样一次占用字节数 通道数×每样本的数据位数/8
        header.avgBytesPerSec = header.blockAlign * header.samplesPerSec;
        // DATA总数据长度字节
        header.dataHdrLeth = pcmSize;
        return ArrayUtils.addAll(header.getHeader(), data);
    }

}
