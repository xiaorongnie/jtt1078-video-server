package com.transcodegroup.jtt1078;

import java.io.FileInputStream;
import java.io.FileOutputStream;

import com.transcodegroup.jtt1078.common.util.Packet;
import com.transcodegroup.jtt1078.core.codec.G711Codec;
import com.transcodegroup.jtt1078.core.server.Jtt1078Decoder;

/**
 * Created by matrixy on 2019/12/21.
 */
public class AudioTest {
    public static void main(String[] args) throws Exception {
        int len = -1;
        byte[] block = new byte[1024];
        FileInputStream fis = new FileInputStream("e:\\test\\streaming.hex");
        Jtt1078Decoder decoder = new Jtt1078Decoder();
        G711Codec codec = new G711Codec();
        FileOutputStream fos = new FileOutputStream("e:\\test\\fuckfuckfuck1111.pcm");
        while ((len = fis.read(block)) > -1) {
            decoder.write(block, 0, len);
            while (true) {
                Packet p = decoder.decode();
                if (p == null)
                    break;

                int lengthOffset = 28;
                int dataType = (p.seek(15).nextByte() >> 4) & 0x0f;
                // 透传数据类型：0100，没有后面的时间以及Last I Frame Interval和Last Frame Interval字段
                if (dataType == 0x04)
                    lengthOffset = 28 - 8 - 2 - 2;
                else if (dataType == 0x03)
                    lengthOffset = 28 - 4;

                if (dataType == 0x03) {
                    byte[] pcmData = codec.toPCM(p.seek(lengthOffset + 2).nextBytes());
                    fos.write(pcmData);
                    fos.flush();
                }
            }
        }
        fos.close();
        fis.close();
    }
}
