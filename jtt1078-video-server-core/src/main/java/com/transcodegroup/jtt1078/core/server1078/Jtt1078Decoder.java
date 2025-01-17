package com.transcodegroup.jtt1078.core.server1078;

import com.transcodegroup.jtt1078.common.util.ByteHolder;
import com.transcodegroup.jtt1078.common.util.ByteUtils;
import com.transcodegroup.jtt1078.common.util.Packet;

/**
 * 1078解码器
 * 
 * @author eason
 * @date 2021/04/30
 */
public class Jtt1078Decoder {
    ByteHolder buffer = new ByteHolder(4096);

    public void write(byte[] block) {
        buffer.write(block);
    }

    public void write(byte[] block, int startIndex, int length) {
        byte[] buff = new byte[length];
        System.arraycopy(block, startIndex, buff, 0, length);
        write(buff);
    }

    public Packet decode() {
        if (this.buffer.size() < 30) {
            return null;
        }
        if ((buffer.getInt(0) & 0x7fffffff) != 0x30316364) {
            String header = ByteUtils.toString(buffer.array(30));
            throw new RuntimeException("invalid protocol header: " + header);
        }

        int lengthOffset = 28;
        int dataType = (this.buffer.get(15) >> 4) & 0x0f;
        // 透传数据类型：0100，没有后面的时间以及Last I Frame Interval和Last Frame Interval字段
        if (dataType == 0x04) {
            lengthOffset = 28 - 8 - 2 - 2;
        } else if (dataType == 0x03) {
            lengthOffset = 28 - 4;
        }
        int bodyLength = this.buffer.getShort(lengthOffset);

        int packetLength = bodyLength + lengthOffset + 2;

        if (this.buffer.size() < packetLength) {
            return null;
        }
        byte[] block = new byte[packetLength];
        this.buffer.sliceInto(block, packetLength);
        return Packet.create(block);
    }
}
