package cn.org.hentai.jtt1078.publisher;

import java.time.Instant;
import java.util.Iterator;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * 定时业务
 *
 */
@Component
@EnableScheduling
@Slf4j
public class Scheduler {

    /**
     * 15秒检查一次,无人订阅的频道,主动关闭
     * 
     * @throws Exception
     */
    @Scheduled(initialDelay = 15 * 1000, fixedDelay = 15 * 1000)
    public void checkIdleChannel() throws Exception {
        PublishManager publishManager = PublishManager.getInstance();
        for (Iterator<Channel> itr = publishManager.channels.values().iterator(); itr.hasNext();) {
            Channel channel = itr.next();
            long idleDurationSecond = Instant.now().getEpochSecond() - channel.lastEpochSecond;
            if (channel.size() == 0 && idleDurationSecond > 30) {
                log.info("{} -> subscribers {}, publishing {}", channel, channel.size(), channel.isPublishing());
                if (channel.ctx != null) {
                    channel.ctx.close();
                }
            }
        }
    }

}
