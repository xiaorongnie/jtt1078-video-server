package cn.org.hentai.jtt1078.publisher;

import java.time.Instant;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.lang.StringUtils;

import cn.org.hentai.jtt1078.codec.AudioCodec;
import cn.org.hentai.jtt1078.entity.Media;
import cn.org.hentai.jtt1078.entity.MediaEncoding;
import cn.org.hentai.jtt1078.entity.MediaEncoding.Encoding;
import cn.org.hentai.jtt1078.flv.FlvEncoder;
import cn.org.hentai.jtt1078.subscriber.RTMPPublisher;
import cn.org.hentai.jtt1078.subscriber.Subscriber;
import cn.org.hentai.jtt1078.subscriber.VideoSubscriber;
import cn.org.hentai.jtt1078.util.ByteHolder;
import cn.org.hentai.jtt1078.util.Configs;
import cn.org.hentai.jtt1078.ws.WsSessionGroup;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

/**
 * 流媒体频道
 * 
 * @author eason
 * @date 2021/04/21
 */
@Slf4j
public class Channel {
    ConcurrentLinkedQueue<Subscriber> subscribers;
    RTMPPublisher rtmpPublisher;

    String tag;
    String imei;
    int chn;
    ChannelHandlerContext ctx;
    boolean publishing;
    ByteHolder buffer;
    AudioCodec audioCodec;
    int payloadType;
    FlvEncoder flvEncoder;
    private long firstTimestamp = -1;
    Long lastEpochSecond;

    static String RTMP_URL = "rtmp.url";

    public Channel(String tag) {
        this.tag = tag;
        this.imei = tag.substring(0, tag.indexOf("-"));
        this.chn = Integer.valueOf(tag.substring(tag.indexOf("-") + 1));
        this.subscribers = new ConcurrentLinkedQueue<Subscriber>();
        this.flvEncoder = new FlvEncoder(true, true);
        this.buffer = new ByteHolder(2048 * 100);
        this.lastEpochSecond = Instant.now().getEpochSecond();

        if (StringUtils.isEmpty(Configs.get(RTMP_URL)) == false) {
            rtmpPublisher = new RTMPPublisher(tag);
            rtmpPublisher.start();
        }
    }

    public Channel(String tag, ChannelHandlerContext ctx) {
        this(tag);
        this.ctx = ctx;
    }

    public boolean isPublishing() {
        return publishing;
    }

    public Subscriber subscribe(ChannelHandlerContext ctx) {
        Subscriber subscriber = new VideoSubscriber(this.tag, ctx);
        this.subscribers.add(subscriber);
        this.lastEpochSecond = Instant.now().getEpochSecond();
        log.info("{} -> subscriber: {}, {}", toString(), ctx.channel().remoteAddress().toString(), subscriber.getId());
        return subscriber;
    }

    public void writeAudio(long timestamp, int payloadType, byte[] data) {
        if (audioCodec == null) {
            audioCodec = AudioCodec.getCodec(payloadType);
            this.payloadType = payloadType;
            Encoding encoding = MediaEncoding.getEncoding(Media.Type.Audio, payloadType);
            log.info("{} -> audio codec={}", toString(), encoding);
            log.info("{} -> audio data={}, pcm={}", toString(), data.length, audioCodec.toPCM(data).length);
        }
        broadcastAudio(timestamp, audioCodec.toPCM(data));
    }

    public void writeVideo(long sequence, long timeoffset, int payloadType, byte[] h264) {
        if (firstTimestamp == -1) {
            firstTimestamp = timeoffset;
        }
        this.publishing = true;
        this.buffer.write(h264);
        while (true) {
            byte[] nalu = readNalu();
            if (nalu == null) {
                break;
            }

            if (nalu.length < 4) {
                continue;
            }

            byte[] flvTag = this.flvEncoder.write(nalu, (int)(timeoffset - firstTimestamp));

            if (flvTag == null) {
                continue;
            }
            // 广播给所有的观众
            broadcastVideo(timeoffset, flvTag);
        }
    }

    public void broadcastVideo(long timeoffset, byte[] flvTag) {
        for (Subscriber subscriber : subscribers) {
            subscriber.onVideoData(timeoffset, flvTag, flvEncoder);
        }
    }

    public void broadcastAudio(long timeoffset, byte[] flvTag) {
        for (Subscriber subscriber : subscribers) {
            subscriber.onAudioData(timeoffset, flvTag, flvEncoder);
        }
        // 对讲通道请求=0
        if (this.chn == 0) {
            WsSessionGroup.onAudioData(imei, flvTag);
        }
    }

    public void unsubscribe(long watcherId) {
        this.lastEpochSecond = Instant.now().getEpochSecond();
        for (Iterator<Subscriber> itr = subscribers.iterator(); itr.hasNext();) {
            Subscriber subscriber = itr.next();
            if (subscriber.getId() == watcherId) {
                itr.remove();
                subscriber.close();
                return;
            }
        }
        log.info("{} -> unsubscribe: {}, {}", toString(), watcherId, watcherId);
    }

    public void close() {
        for (Iterator<Subscriber> itr = subscribers.iterator(); itr.hasNext();) {
            Subscriber subscriber = itr.next();
            subscriber.close();
            itr.remove();
        }
        if (rtmpPublisher != null) {
            rtmpPublisher.close();
        }
    }

    private byte[] readNalu() {
        for (int i = 0; i < buffer.size(); i++) {
            int a = buffer.get(i + 0) & 0xff;
            int b = buffer.get(i + 1) & 0xff;
            int c = buffer.get(i + 2) & 0xff;
            int d = buffer.get(i + 3) & 0xff;
            if (a == 0x00 && b == 0x00 && c == 0x00 && d == 0x01) {
                if (i == 0) {
                    continue;
                }
                byte[] nalu = new byte[i];
                buffer.sliceInto(nalu, i);
                return nalu;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return tag + " [" + toHexString() + "]";
    }

    /**
     * 频道ID
     */
    public String toHexString() {
        return Long.toHexString(this.hashCode() & 0xffffffffL);
    }

    /**
     * 获取频道订阅人数
     * 
     * @return
     */
    public synchronized int size() {
        return subscribers.size();
    }

}
