package cn.org.hentai.jtt1078.codec.ffmpeg.aac;

/**
 * AAC ADTS可变头信息
 * 
 * @author zhen.lin
 * @date 2019年7月8日
 */
public class ADTSVariableHeader {
    // 编码时设置为0，解码时忽略
    private int copyrighted_id_bit;
    // 编码时设置为0，解码时忽略
    private int copyrighted_id_start;
    // ADTS帧长度包括ADTS长度和AAC声音数据长度的和。即 aac_frame_length = (protection_absent == 0
    // ? 9 : 7) + audio_data_length
    private int aac_frame_length;
    // 固定为0x7FF。表示是码率可变的码流
    private int adts_buffer_fullness;
    // 表示当前帧有number_of_raw_data_blocks_in_frame + 1
    // 个原始帧(一个AAC原始帧包含一段时间内1024个采样及相关数据)。
    private int number_of_raw_data_blocks_in_frame;

    @SuppressWarnings("unused")
    public ADTSVariableHeader(Byte[] buf) {
        // 暂时不解
    }

    public int getCopyrighted_id_bit() {
        return copyrighted_id_bit;
    }

    public void setCopyrighted_id_bit(int copyrighted_id_bit) {
        this.copyrighted_id_bit = copyrighted_id_bit;
    }

    public int getCopyrighted_id_start() {
        return copyrighted_id_start;
    }

    public void setCopyrighted_id_start(int copyrighted_id_start) {
        this.copyrighted_id_start = copyrighted_id_start;
    }

    public int getAac_frame_length() {
        return aac_frame_length;
    }

    public void setAac_frame_length(int aac_frame_length) {
        this.aac_frame_length = aac_frame_length;
    }

    public int getAdts_buffer_fullness() {
        return adts_buffer_fullness;
    }

    public void setAdts_buffer_fullness(int adts_buffer_fullness) {
        this.adts_buffer_fullness = adts_buffer_fullness;
    }

    public int getNumber_of_raw_data_blocks_in_frame() {
        return number_of_raw_data_blocks_in_frame;
    }

    public void setNumber_of_raw_data_blocks_in_frame(int number_of_raw_data_blocks_in_frame) {
        this.number_of_raw_data_blocks_in_frame = number_of_raw_data_blocks_in_frame;
    }

}
