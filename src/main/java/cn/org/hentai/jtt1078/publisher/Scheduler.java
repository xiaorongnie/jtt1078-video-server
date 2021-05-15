package cn.org.hentai.jtt1078.publisher;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.time.Instant;
import java.util.Arrays;
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

    @Scheduled(initialDelay = 15 * 1000, fixedDelay = 15 * 1000)
    public void test() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(1024 * 1024 * 4);
        int len = -1;
        byte[] block = new byte[512];
        FileInputStream fis =
            new FileInputStream("E:\\eclipse-jee-neon-2-win32-x86_64\\workspace2020\\webvoice\\docs\\temp\\test.wav");
        while ((len = fis.read(block)) > -1) {
            baos.write(block, 0, len);
        }
        byte[] data = baos.toByteArray();
        PublishManager.getInstance().publishAudio(Arrays.copyOfRange(data, 44, data.length), null);
        fis.close();
    }

}
