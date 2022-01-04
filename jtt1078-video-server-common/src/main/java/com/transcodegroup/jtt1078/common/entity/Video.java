package com.transcodegroup.jtt1078.common.entity;

/**
 * Created by houcheng on 2019-12-11.
 */
public class Video extends Media
{
    public Video(long sequence, MediaEncoding.Encoding encoding, byte[] data)
    {
        super(sequence, encoding, data);
    }
}
