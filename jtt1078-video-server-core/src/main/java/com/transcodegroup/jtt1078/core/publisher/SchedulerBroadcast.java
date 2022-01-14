package com.transcodegroup.jtt1078.core.publisher;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.time.Instant;
import java.util.Arrays;
import java.util.Iterator;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.transcodegroup.jtt1078.core.websocket.WsSession;
import com.transcodegroup.jtt1078.core.websocket.WsSessionGroup;

import lombok.extern.slf4j.Slf4j;

@Component
@EnableScheduling
@Slf4j
public class SchedulerBroadcast {

    /**
     * 15秒检查一次,无人订阅的频道,主动关闭
     * 
     */
    // @Scheduled(initialDelay = 15 * 1000, fixedDelay = 15 * 1000)
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

    /**
     * 15秒输出一次会话数量
     * 
     */
    @Scheduled(initialDelay = 15 * 1000, fixedDelay = 15 * 1000)
    public void check() throws Exception {
        PublishManager publishManager = PublishManager.getInstance();
        log.info("------->");
        log.info("Channels = {}", publishManager.channels.size());
        log.info("WsSessions = {}", WsSessionGroup.size());
        log.info("<-------");
    }

    /**
     * 定时发送测试数据到终端
     * 
     */
    // @Scheduled(initialDelay = 5 * 1000, fixedDelay = 10 * 1000)
    public void sendTestData2Device() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(1024 * 1024 * 4);
        byte[] block = new byte[512];
        FileInputStream fis = new FileInputStream("test.wav");
        while (fis.read(block) > -1) {
            baos.write(block);
        }
        baos.flush();
        byte[] data = baos.toByteArray();

        WsSession wsSession = WsSessionGroup.getSession("010007012345");
        if (wsSession != null) {
            byte[] pcmData = Arrays.copyOfRange(data, 0, data.length);
            // 一包包含320个16bit采样点
            int pcmBlock = 320 * 2;
            int times = pcmData.length / pcmBlock;
            for (int i = 0; i < times; i++) {
                byte[] data2 = ArrayUtils.subarray(pcmData, 640 * i, 640 * (i + 1));
                wsSession.onAudioDataOfPlatform(data2);
                System.out.println("发送分块数据 640bit");
                Thread.sleep(40);
            }
        }
        fis.close();
    }

}
