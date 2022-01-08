package com.transcodegroup.jtt1078.ffmpeg.aac;

/**
 * AAC ADTS 固定头信息
 * 
 * @author zhen.lin
 * @date 2019年7月8日
 */
public class ADTSFixedHeader {

    // 帧同步标识一个帧的开始，固定为0xFFF
    private int syncword;
    // MPEG 标示符。0表示MPEG-4，1表示MPEG-2
    private int id;
    // 固定为'00'
    private int layer;
    // 标识是否进行误码校验。0表示有CRC校验，1表示没有CRC校验
    private int protection_absent;
    // 标识使用哪个级别的AAC。1=AAC Main 2=AAC LC (Low Complexity) 3=AAC SSR (Scalable
    // Sample Rate) 4=AAC LTP (Long Term Prediction)
    private int profile;
    // 标识使用的采样率的下标
    // 0x0 96000
    // 0x1 88200
    // 0x2 64000
    // 0x3 48000
    // 0x4 44100
    // 0x5 32000
    // 0x6 24000
    // 0x7 22050
    // 0x8 16000
    // 0x9 12000
    // 0xa 11025
    // 0xb 8000
    // 0xc 73500
    // 0xd 0xe 保留
    // 0xf escape value
    private int sampling_frequency_index;
    // 私有位，编码时设置为0，解码时忽略
    private int private_bit;
    // 标识声道数
    private int channel_configuration;
    // 编码时设置为0，解码时忽略
    private int original_copy;
    // 编码时设置为0，解码时忽略
    private int home;

    public ADTSFixedHeader(byte[] buf) {
        // 取16位
        this.syncword = ((0xff & buf[0]) << 4) + ((0xff & buf[1]) >> 4);
        int f1 = (0xff & buf[1]) & 15;
        this.id = (f1 & 8) >> 3;
        this.layer = (f1 & 6) >> 1;
        this.protection_absent = f1 & 1;
        // 取12位
        int f2 = ((0xff & buf[2]) << 4) + ((0xff & buf[3]) >> 4);
        this.profile = (f2 & 3072) >> 10;
        this.sampling_frequency_index = (f2 & 960) >> 6;
        this.private_bit = (f2 & 32) >> 5;
        this.channel_configuration = (f2 & 28) >> 2;
        this.original_copy = (f2 & 2) >> 1;
        this.home = f2 & 1;
    }

    public int getSyncword() {
        return syncword;
    }

    public void setSyncword(int syncword) {
        this.syncword = syncword;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getLayer() {
        return layer;
    }

    public void setLayer(int layer) {
        this.layer = layer;
    }

    public int getProtection_absent() {
        return protection_absent;
    }

    public void setProtection_absent(int protection_absent) {
        this.protection_absent = protection_absent;
    }

    public int getProfile() {
        return profile;
    }

    public void setProfile(int profile) {
        this.profile = profile;
    }

    public int getSampling_frequency_index() {
        return sampling_frequency_index;
    }

    public void setSampling_frequency_index(int sampling_frequency_index) {
        this.sampling_frequency_index = sampling_frequency_index;
    }

    public int getPrivate_bit() {
        return private_bit;
    }

    public void setPrivate_bit(int private_bit) {
        this.private_bit = private_bit;
    }

    public int getChannel_configuration() {
        return channel_configuration;
    }

    public void setChannel_configuration(int channel_configuration) {
        this.channel_configuration = channel_configuration;
    }

    public int getOriginal_copy() {
        return original_copy;
    }

    public void setOriginal_copy(int original_copy) {
        this.original_copy = original_copy;
    }

    public int getHome() {
        return home;
    }

    public void setHome(int home) {
        this.home = home;
    }

    @Override
    public String toString() {
        return String.format(
            "syncword=%x, id=%d, layer=%d, protection_absent=%d, profile=%d,"
                + " sampling_frequency_index=%d, private_bit=%d, channel_configuration=%d, original_copy=%d, home=%d",
            this.syncword, this.id, this.layer, this.protection_absent, this.profile, this.sampling_frequency_index,
            this.private_bit, this.channel_configuration, this.original_copy, this.home);
    }

    /**
     * 封装AAC头
     * 
     * @param len
     * @return 封装一帧AAC=1024PCM
     */
    public static byte[] getADTS(int len) {
        len = len + 7;
        int profile = 0x02; // AAC LC
        int freqIdx = 0x0b; // 22050
        int chanCfg = 1; // 2 Channel
        byte[] adts = new byte[7];
        adts[0] = (byte)0xFF;
        adts[1] = (byte)0xF1;
        adts[2] = (byte)(((profile - 1) << 6) + (freqIdx << 2) + (chanCfg >> 2));
        adts[3] = (byte)(((chanCfg & 3) << 6) + (len >> 11));
        adts[4] = (byte)((len & 0x7FF) >> 3);
        adts[5] = (byte)(((len & 7) << 5) + 0x1F);
        adts[6] = (byte)0xFC;
        return adts;
    }

    /**
     * 封装AAC头
     * 
     * @param len
     * @return 封装一帧AAC=1024PCM
     */
    public static byte[] getADTS2(int len) {
        return getADTSHeader(1, 0x0b, 1, len);
    }

    /* 获取ACC的ADTS头
    * @param profile:  0:Main profile   1:AAC LC  2:SSR 3 reserved
    * @param freqIdx: 0:96000 HZ 1:88200 HZ 2:64000HZ 3:48000HZ 4:44100HZ 5:32000HZ 6:24000HZ 7:22050HZ 8:16000HZ 9:12000HZ
    * @param chanCfg: 0:Defined in AOT Specifc Config  1: 1 channel: front-center  2: 2 channels: front-left, front-right
    * @param aacDataLen aac裸流的长度
    * 更多参数可取值参考 https://blog.csdn.net/tantion/article/details/82743942
    */
    public static byte[] getADTSHeader(int profile, int freqIdx, int chanCfg, int aacDataLen) {
        int dataLen = aacDataLen + 7; // AAC裸帧的数据长度加上ADTS的头长度
        byte[] buffer = new byte[7];
        buffer[0] = (byte)0xFF;
        buffer[1] = (byte)0xF1;
        buffer[2] = (byte)((profile << 6) + (freqIdx << 2) + (chanCfg >> 2));
        buffer[3] = (byte)(((chanCfg & 3) << 6) + (dataLen >> 11));
        buffer[4] = (byte)((dataLen & 0x7FF) >> 3);
        buffer[5] = (byte)(((dataLen & 7) << 5) + 0x1F);
        buffer[6] = (byte)0xFC;
        return buffer;
    }

}
