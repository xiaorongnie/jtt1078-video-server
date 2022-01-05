package com.transcodegroup.jtt1078.test;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.util.Arrays;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.transcodegroup.jtt1078.publisher.PublishManager;
import com.transcodegroup.jtt1078.ws.WsSessionGroup;

/**
 * 定时业务
 *
 */
@Component
@EnableScheduling
@ConditionalOnProperty(prefix = "scheduled", value = "active", havingValue = "true")
public class SchedulerBroadcast {

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
