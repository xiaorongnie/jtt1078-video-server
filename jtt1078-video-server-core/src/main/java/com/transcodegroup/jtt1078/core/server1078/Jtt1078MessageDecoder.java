package com.transcodegroup.jtt1078.core.server1078;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.transcodegroup.jtt1078.common.util.Packet;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

/**
 * Created by matrixy on 2019/4/9.
 */
public class Jtt1078MessageDecoder extends ByteToMessageDecoder
{
    static Logger logger = LoggerFactory.getLogger(Jtt1078MessageDecoder.class);
    byte[] block = new byte[4096];
    Jtt1078Decoder decoder = new Jtt1078Decoder();

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception
    {
        int length = in.readableBytes();
        for (int i = 0, k = (int)Math.ceil(length / 512f); i < k; i++)
        {
            int l = i < k - 1 ? 512 : length - (i * 512);
            in.readBytes(block, 0, l);

            decoder.write(block, 0, l);

            while (true)
            {
                Packet p = decoder.decode();
                if (p == null) break;

                out.add(p);
            }
        }
    }
}
