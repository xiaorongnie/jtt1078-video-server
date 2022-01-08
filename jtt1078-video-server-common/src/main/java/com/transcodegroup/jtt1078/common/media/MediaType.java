package com.transcodegroup.jtt1078.common.media;

/**
 * 
 * 数据流，可能是视频或是音频，
 * 
 * 视频为FLV封装，音频为PCM编码的片断
 * 
 * @author eason
 * @date 2022/01/08
 */
public class MediaType {
    public enum Type {
        Video, Audio
    };

}
