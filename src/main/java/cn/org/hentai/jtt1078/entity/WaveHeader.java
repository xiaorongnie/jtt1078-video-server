package cn.org.hentai.jtt1078.entity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * wav文件头信息辅助类
 * 
 * @author eason
 * @date 2017年1月11日 下午6:09:19
 */
public class WaveHeader {
    /***
     * 资源交换文件标志（RIFF）
     */
    public final char fileID[] = {'R', 'I', 'F', 'F'};
    /**
     * 4 long int 文件长度
     */
    public int fileLength;
    /**
     * 4 char "WAVE"标志
     */
    public char wavTag[] = {'W', 'A', 'V', 'E'};
    /**
     * 4 char "fmt"标志
     */
    public char fmtHdrId[] = {'f', 'm', 't', ' '};
    /**
     * 4 过渡字节（不定）
     */
    public int fmtHdrLeth = 16;
    /**
     * 2 int 格式类别（10H为PCM形式的声音数据)
     */
    public short formatTag = 16;
    /**
     * 2 int 通道数，单声道为1，双声道为2
     */
    public short channels;
    /**
     * 2 int 采样率（每秒样本数），表示每个通道的播放速度，
     */
    public int samplesPerSec;
    /**
     * 4 long int 波形音频数据传送速率，其值为通道数×每秒数据位数×每样 本的数据位数／8。播放软件利用此值可以估计缓冲区的大小。
     */
    public int avgBytesPerSec;
    /**
     * 2 int 数据块的调整数（按字节算的），其值为通道数×每样本的数据位 值／8。播放软件需要一次处理多个该值大小的字节数据，以便将其 值用于缓冲区的调整。
     */
    public short blockAlign;
    /**
     * 2 每样本的数据位数，表示每个声道中各个样本的数据位数。如果有多 个声道，对每个声道而言，样本大小都一样。
     */
    public short bitsPerSample;
    /**
     * 4 char 数据标记符＂data＂
     */
    public char dataHdrId[] = {'d', 'a', 't', 'a'};
    /**
     * 4 long int 语音数据的长度
     */
    public int dataHdrLeth;

    public byte[] getHeader() {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            writeChar(bos, fileID);
            writeInt(bos, fileLength);
            writeChar(bos, wavTag);
            writeChar(bos, fmtHdrId);
            writeInt(bos, fmtHdrLeth);
            writeShort(bos, formatTag);
            writeShort(bos, channels);
            writeInt(bos, samplesPerSec);
            writeInt(bos, avgBytesPerSec);
            writeShort(bos, blockAlign);
            writeShort(bos, bitsPerSample);
            writeChar(bos, dataHdrId);
            writeInt(bos, dataHdrLeth);
            bos.flush();
            byte[] r = bos.toByteArray();
            bos.close();
            return r;
        } catch (Exception e) {
        }
        return new byte[44];
    }

    private void writeShort(ByteArrayOutputStream bos, int s) throws IOException {
        byte[] mybyte = new byte[2];
        mybyte[1] = (byte)((s << 16) >> 24);
        mybyte[0] = (byte)((s << 24) >> 24);
        bos.write(mybyte);
    }

    private void writeInt(ByteArrayOutputStream bos, int n) throws IOException {
        byte[] buf = new byte[4];
        buf[3] = (byte)(n >> 24);
        buf[2] = (byte)((n << 8) >> 24);
        buf[1] = (byte)((n << 16) >> 24);
        buf[0] = (byte)((n << 24) >> 24);
        bos.write(buf);
    }

    private void writeChar(ByteArrayOutputStream bos, char[] id) {
        for (int i = 0; i < id.length; i++) {
            char c = id[i];
            bos.write(c);
        }
    }
}