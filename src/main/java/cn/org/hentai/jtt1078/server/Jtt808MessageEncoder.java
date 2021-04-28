package cn.org.hentai.jtt1078.server;

import cn.org.hentai.jtt1078.entity.Rtp1078Msg;
import cn.org.hentai.jtt1078.util.BcdUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

/**
 * 1078编码器
 * 
 */
@Slf4j
public class Jtt808MessageEncoder extends MessageToByteEncoder<Rtp1078Msg> {

    private static final AttributeKey<Session> SESSION_KEY = AttributeKey.valueOf("session-key");;

    @Override
    protected void encode(ChannelHandlerContext ctx, Rtp1078Msg msg, ByteBuf out) throws Exception {
        log.info("Jtt808MessageEncoder...");
        Attribute<Session> attr = ctx.channel().attr(SESSION_KEY);
        if (null == attr) {
            return;
        }
        Session session = attr.get();

        // 帧头+4
        out.writeInt(0x30316364);
        // VPXCC+1
        out.writeByte(0x81);
        // MPT+1
        out.writeByte(msg.getCodeType());
        // ID 流水号+2
        int serial = session.getSerial();
        out.writeShort(serial);
        // SIM卡号+6
        byte[] bs2 = BcdUtil.getPhoneNumber(msg.getSim());
        out.writeBytes(bs2);
        // 通道+1
        out.writeByte(msg.getChn());
        // 原子音频包+1
        out.writeByte(0x30);
        // 相对时间错+8
        long time = System.currentTimeMillis() - session.getFirstTime();
        out.writeLong(time);
        // 长度+2
        out.writeShort(msg.getData().length);
        out.writeBytes(msg.getData());
        log.info("===>>> RTP1078Msg [sim=" + msg.getSim() + ",size=" + msg.getData().length + ",time=" + time
            + ",serial=" + serial + "]");
    }

}
