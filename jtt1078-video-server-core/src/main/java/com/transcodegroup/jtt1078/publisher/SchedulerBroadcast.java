package com.transcodegroup.jtt1078.publisher;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.time.Instant;
import java.util.Arrays;
import java.util.Iterator;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.transcodegroup.jtt1078.ws.WsSessionGroup;

import lombok.extern.slf4j.Slf4j;

@Component
@EnableScheduling
@Slf4j
public class SchedulerBroadcast {

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
    public void sendTestData2Device() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(1024 * 1024 * 4);
        byte[] block = new byte[512];
        FileInputStream fis = new FileInputStream("test.wav");
        while (fis.read(block) > -1) {
            baos.write(block);
        }
        byte[] data = baos.toByteArray();
        PublishManager.getInstance().publishAudio(Arrays.copyOfRange(data, 44, data.length), null);
        fis.close();
    }

    @Scheduled(initialDelay = 10 * 1000, fixedDelay = 10 * 1000)
    public void sendTestData2Platform() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(1024 * 1024 * 4);
        byte[] block = new byte[512];
        FileInputStream fis = new FileInputStream("test.wav");
        while ((fis.read(block)) > -1) {
            baos.write(block);
        }
        byte[] data = baos.toByteArray();
        WsSessionGroup.broadcastWavDataChunks(data);
        fis.close();
    }

}