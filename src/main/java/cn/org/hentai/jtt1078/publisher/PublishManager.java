package cn.org.hentai.jtt1078.publisher;

import java.util.concurrent.ConcurrentHashMap;

import cn.org.hentai.jtt1078.entity.Media;
import cn.org.hentai.jtt1078.subscriber.Subscriber;
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

    /**
     * 频道列表
     */
    ConcurrentHashMap<String, Channel> channels;

    private PublishManager() {
        channels = new ConcurrentHashMap<String, Channel>();
    }

    /**
     * 订阅
     * 
     * @param tag
     * @param type
     * @param ctx
     * @return
     */
    public Subscriber subscribe(String tag, Media.Type type, ChannelHandlerContext ctx) {
        Channel chl = channels.get(tag);
        if (chl == null) {
            chl = new Channel(tag);
            channels.put(tag, chl);
        }
        Subscriber subscriber = null;
        if (type.equals(Media.Type.Video)) {
            subscriber = chl.subscribe(ctx);
        } else {
            throw new RuntimeException("unknown media type: " + type);
        }

        subscriber.setName("subscriber-" + tag + "-" + subscriber.getId());
        subscriber.start();

        return subscriber;
    }

    /**
     * 取消订阅
     * 
     * @param tag
     * @param watcherId
     */
    public void unsubscribe(String tag, long watcherId) {
        Channel chl = channels.get(tag);
        if (chl != null) {
            chl.unsubscribe(watcherId);
        }
        log.info("unsubscribe: {} - {}", tag, watcherId);
    }

    /**
     * 推送音频
     * 
     * @param tag
     * @param sequence
     * @param timestamp
     * @param payloadType
     * @param data
     */
    public void publishAudio(String tag, int sequence, long timestamp, int payloadType, byte[] data) {
        Channel chl = channels.get(tag);
        if (chl != null) {
            chl.writeAudio(timestamp, payloadType, data);
        }
    }

    /**
     * 推送视频
     * 
     * @param tag
     * @param sequence
     * @param timestamp
     * @param payloadType
     * @param data
     */
    public void publishVideo(String tag, int sequence, long timestamp, int payloadType, byte[] data) {
        Channel chl = channels.get(tag);
        if (chl != null) {
            chl.writeVideo(sequence, timestamp, payloadType, data);
        }
    }

    /**
     * 开启频道
     * 
     * @param tag
     * @return
     */
    public Channel open(String tag) {
        Channel chl = channels.get(tag);
        if (chl == null) {
            chl = new Channel(tag);
            channels.put(tag, chl);
        }
        if (chl.isPublishing()) {
            throw new RuntimeException("channel already publishing");
        }
        return chl;
    }

    /**
     * 关闭频道
     * 
     * @param tag
     */
    public void close(String tag) {
        Channel chl = channels.remove(tag);
        if (chl != null) {
            chl.close();
        }
    }

    static final PublishManager INSTANCE = new PublishManager();

    public static void init() {}

    public static PublishManager getInstance() {
        return INSTANCE;
    }
}
