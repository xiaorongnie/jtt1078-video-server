package com.transcodegroup.jtt1078.publisher;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.ArrayUtils;
import org.springframework.util.StringUtils;

import com.transcodegroup.jtt1078.entity.Media;
import com.transcodegroup.jtt1078.entity.Rtp1078Msg;
import com.transcodegroup.jtt1078.subscriber.Subscriber;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

/**
 * 推流服务器
 * 
 * @author eason
 * @date 2021/04/21
 */
@Slf4j
public final class PublishManager {

    ConcurrentHashMap<String, Channel> channels;

    private PublishManager() {
        channels = new ConcurrentHashMap<>();
    }

    /**
     * HTTP客户端订阅
     * 
     * @param tag
     *            频道 如010000012345-1
     * @param type
     *            视频或是音频
     * @param ctx
     *            客户端上下文
     * 
     * @return
     */
    public Subscriber subscribe(String tag, Media.Type type, ChannelHandlerContext ctx) {
        Channel channel = channels.get(tag);
        if (channel == null) {
            channel = new Channel(tag);
            channels.put(tag, channel);
            log.info("{} => start publishing", channel);
        }
        Subscriber subscriber = null;
        if (type.equals(Media.Type.Video)) {
            subscriber = channel.subscribe(ctx);
        } else {
            throw new RuntimeException("unknown media type: " + type);
        }
        subscriber.setName("subscriber-" + tag + "-" + subscriber.getId());
        subscriber.start();
        return subscriber;
    }

    /**
     * HTTP取消订阅
     * 
     * @param tag
     * @param watcherId
     */
    public void unsubscribe(String tag, long watcherId) {
        Channel channel = channels.get(tag);
        if (channel != null) {
            channel.unsubscribe(watcherId);
        }
        log.info("{} -> unsubscribe {}", channel, watcherId);
    }

    /**
     * 推送音频流&HTTP订阅者
     * 
     * @param tag
     * @param sequence
     * @param timestamp
     * @param payloadType
     * @param data
     */
    public void publishAudio(String tag, int sequence, long timestamp, int payloadType, byte[] data) {
        Channel channel = channels.get(tag);
        if (channel != null) {
            channel.writeAudio(timestamp, payloadType, data);
        }
    }

    /**
     * 推送视频流给HTTP订阅者
     * 
     * @param tag
     * @param sequence
     * @param timestamp
     * @param payloadType
     * @param data
     */
    public void publishVideo(String tag, int sequence, long timestamp, int payloadType, byte[] data) {
        Channel channel = channels.get(tag);
        if (channel != null) {
            channel.writeVideo(sequence, timestamp, payloadType, data);
        }
    }

    /**
     * RTP开启频道
     * 
     * @param tag
     * @return
     */
    public Channel open(String tag, ChannelHandlerContext ctx) {
        Channel channel = channels.get(tag);
        if (channel == null) {
            channel = new Channel(tag, ctx);
            channels.put(tag, channel);
            log.info("{} -> start publishing", channel);
        }
        if (channel.ctx == null) {
            channel.ctx = ctx;
        }
        if (channel.isPublishing()) {
            throw new RuntimeException("channel already publishing");
        }
        return channel;
    }

    /**
     * RTP关闭频道
     * 
     * @param tag
     */
    public void close(String tag) {
        Channel channel = channels.remove(tag);
        if (channel != null) {
            channel.close();
        }
    }

    static final PublishManager INSTANCE = new PublishManager();

    public static void init() {}

    public static PublishManager getInstance() {
        return INSTANCE;
    }

    /**
     * 发送音频数据到终端
     * 
     * @param pcmData
     *            PCM数据
     * @throws InterruptedException
     */
    public void publishAudio(byte[] pcmData, String imei) {
        // 一包包含320个16bit采样点
        int pcmBlock = 320 * 2;
        int times = pcmData.length / pcmBlock;
        for (int i = 0; i < times; i++) {
            byte[] data = ArrayUtils.subarray(pcmData, 640 * i, 640 * (i + 1));
            for (Channel channel : channels.values()) {
                if (StringUtils.hasText(imei) && channel.audioCodec != null && imei.equals(channel.imei)) {
                    Rtp1078Msg rtp1078Msg = new Rtp1078Msg();
                    rtp1078Msg.setSim(channel.imei);
                    rtp1078Msg.setData(channel.audioCodec.fromPCM(data));
                    rtp1078Msg.setFlag2((byte)channel.payloadType);
                    rtp1078Msg.setHig726(channel.audioCodec.hisi);
                    channel.ctx.channel().writeAndFlush(rtp1078Msg);
                }
            }
        }
    }

}
