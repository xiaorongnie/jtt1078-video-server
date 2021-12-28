package cn.org.hentai.jtt1078.codec.ffmpeg.aac;

/**
 * AAC ADTS头信息 ADTS头包含了AAC文件的采样率、通道数、帧数据长度等信息。ADTS头分为固定头信息和可变头信息两个部分，固定头信息在每个帧中的是一样的，可变头信息在各个帧中并不是固定值。
 * ADTS头一般是7个字节((28+28)/8)长度，如果需要对数据进行CRC校验，则会有2个Byte的校验码，所以ADTS头的实际长度是7个字节或9个字节。
 * 
 * 参考文档https://zhuanlan.zhihu.com/p/351347165
 * 
 * @author zhen.lin
 * @date 2019年7月8日
 */
public class ADTS {

    // 是否adts头
    private boolean head = false;
    // 固定头信息
    private ADTSFixedHeader fixed = null;
    // 可变头信息
    private ADTSVariableHeader variable = null;

    public ADTS() {
        this.head = false;
    }

    /**
     * 构造
     * 
     * @param buf
     */
    public ADTS(byte[] buf) {
        this.analyse(buf);
    }

    public void analyse(byte[] buf) {
        this.head = false;
        // 不管校验的两字节
        if (buf == null || buf.length < 7) {
            this.head = true;
        } else {
            int ff = (0xff & buf[1]) >> 4;
            if ((0xff & buf[0]) == 0xff && ff == 0xf) {
                this.head = true;
                // 是adts头
                this.fixed = new ADTSFixedHeader(buf);
            }
        }
        if (!this.head) {
            this.fixed = null;
            this.variable = null;
        }
    }

    public boolean isHead() {
        return head;
    }

    public void setHead(boolean head) {
        this.head = head;
    }

    public ADTSFixedHeader getFixed() {
        return fixed;
    }

    public void setFixed(ADTSFixedHeader fixed) {
        this.fixed = fixed;
    }

    public ADTSVariableHeader getVariable() {
        return variable;
    }

    public void setVariable(ADTSVariableHeader variable) {
        this.variable = variable;
    }

    public int getSampleRate() {
        if (!this.head || this.fixed == null) {
            return 0;
        }
        int ret = 0;
        switch (this.fixed.getSampling_frequency_index()) {
            case 0x0:
                ret = 96000;
                break;
            case 0x1:
                ret = 88200;
                break;
            case 0x2:
                ret = 64000;
                break;
            case 0x3:
                ret = 48000;
                break;
            case 0x4:
                ret = 44100;
                break;
            case 0x5:
                ret = 32000;
                break;
            case 0x6:
                ret = 24000;
                break;
            case 0x7:
                ret = 22050;
                break;
            case 0x8:
                ret = 16000;
                break;
            case 0x9:
                ret = 12000;
                break;
            case 0xa:
                ret = 11025;
                break;
            case 0xb:
                ret = 8000;
                break;
            case 0xc:
                ret = 7350;
                break;
        }
        return ret;
    }
}
