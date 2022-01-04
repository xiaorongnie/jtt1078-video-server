package com.transcodegroup.jtt1078.publisher;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Iterator;

import org.apache.commons.lang.ArrayUtils;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.transcodegroup.jtt1078.codec.ffmpeg.aac.AACCodec;
import com.transcodegroup.jtt1078.codec.ffmpeg.aac.ADTSFixedHeader;
import com.transcodegroup.jtt1078.ws.WsSessionGroup;

import lombok.extern.slf4j.Slf4j;

/**
 * 定时业务
 *
 */
@Component
@EnableScheduling
@Slf4j
public class Scheduler {
    private static File file;
    private static FileOutputStream stream;
    private static BufferedOutputStream bos;
    static {
        file = new File("F:\\pcm2aac.aac");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            stream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        bos = new BufferedOutputStream(stream);
    }

    int counts = 0;

    AACCodec aacCodec = new AACCodec();
    AACCodec aacCodec2 = new AACCodec();

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

    // @Scheduled(initialDelay = 15 * 1000, fixedDelay = 15 * 1000)
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

    // @Scheduled(initialDelay = 10 * 1000, fixedDelay = 10 * 1000)
    public void test2() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(1024 * 1024 * 4);
        int len = -1;
        byte[] block = new byte[512];
        FileInputStream fis =
            new FileInputStream("E:\\eclipse-jee-neon-2-win32-x86_64\\workspace2020\\webvoice\\docs\\temp\\test.wav");
        // FileInputStream fis =
        // new FileInputStream("D:\\test.wav");
        while ((len = fis.read(block)) > -1) {
            baos.write(block, 0, len);
        }
        byte[] data = baos.toByteArray();
        if (counts % 2 == 0) {
            WsSessionGroup.onAudioData2(data);
        } else {
            WsSessionGroup.onAudioData2(data);
        }
        counts++;
        fis.close();
    }

    /**
     * 将wav数据包装再传给前端
     * 
     * @throws Exception
     */
    @Scheduled(initialDelay = 10 * 1000, fixedDelay = 10 * 1000)
    public void test3() throws Exception {
        if (++counts % 2 == 0) {
            sendWavToWeb();
            log.info("sendWavToWeb...");
        } else {
            sendWavToACCToPcmToWeb();
            log.info("sendWavToACCToPcmToWeb...");
        }
    }

    /**
     * 将wav数据包装再传给前端
     * 
     * @throws Exception
     */
    public void sendWavToWeb() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(1024 * 1024 * 4);
        int len = -1;
        byte[] block = new byte[512];
        FileInputStream fis =
            new FileInputStream("E:\\eclipse-jee-neon-2-win32-x86_64\\workspace2020\\webvoice\\docs\\temp\\test.wav");
        while ((len = fis.read(block)) > -1) {
            baos.write(block, 0, len);
        }
        byte[] wavData = baos.toByteArray();
        byte[] pcmData = Arrays.copyOfRange(wavData, 44, wavData.length);
        // 一次发送320个采样点16bit
        int pcmBlock = 320 * 2;
        int times = pcmData.length / pcmBlock;
        for (int i = 0; i < times; i++) {
            byte[] pcmBlockData = ArrayUtils.subarray(pcmData, pcmBlock * i, pcmBlock * (i + 1));
            WsSessionGroup.sendWavData(pcmBlockData, i);
        }
        fis.close();
    }

    /**
     * 将wav数据包装再传给前端
     * 
     * @throws Exception
     */
    public void sendWavToACCToPcmToWeb() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(1024 * 1024 * 4);
        int len = -1;
        byte[] block = new byte[512];
        FileInputStream fis =
            new FileInputStream("E:\\eclipse-jee-neon-2-win32-x86_64\\workspace2020\\webvoice\\docs\\temp\\test.wav");
        while ((len = fis.read(block)) > -1) {
            baos.write(block, 0, len);
        }
        byte[] wavData = baos.toByteArray();
        byte[] pcmData = Arrays.copyOfRange(wavData, 44, wavData.length);
        // 一次发送320个采样点16bit
        int pcmBlock = 1024 * 2;
        int times = pcmData.length / pcmBlock;
        for (int i = 0; i < times; i++) {
            byte[] pcmBlockData = ArrayUtils.subarray(pcmData, pcmBlock * i, pcmBlock * (i + 1));
            byte[] accBlockData = aacCodec2.fromPCM(pcmBlockData);
            try {
                bos.write(ADTSFixedHeader.getADTS(accBlockData.length));
                bos.write(accBlockData);
                bos.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // byte[] pcmBlockData2 = aacCodec2.toPCM(accBlockData);
            // WsSessionGroup.sendWavData(pcmBlockData2, i);
        }
        fis.close();
    }

}
