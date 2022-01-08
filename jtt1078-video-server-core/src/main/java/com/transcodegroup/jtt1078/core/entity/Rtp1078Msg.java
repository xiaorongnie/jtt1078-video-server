package com.transcodegroup.jtt1078.core.entity;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;

import com.transcodegroup.jtt1078.common.util.Utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Data;

/**
 * RTP数据包结构
 * 
 * @author lin
 * @date 2018年7月7日
 */
@Data
public class Rtp1078Msg {
    // 头标识
    private int head;
    // 标识
    private byte flag1; // 固定 0x81
    private byte flag2; // 标志位与负载类型
    // 包序号 从0开始 到 65535 超出后继续从0开始
    private short packId;
    // sim卡号bcd格式
    private String sim;
    // 通道号
    private byte chn;
    // 数据类型
    private byte dataType;
    // 私有数据
    private byte emptyAudio = 0;

    // 时间戳(视频 音频有 透传包没有)
    private long timestamp;
    // 与最后一个i帧的间隔 (视频帧独有)
    private short lastIInterval;
    // 与最后一帧的间隔(视频帧独有)
    private short lastInterval;
    // 音视频数据大小 协议中是word两字节 这里为了合并数据包需要改为int
    private int dataSize;
    // 音视频数据
    private byte[] data;

    // 解出 sps pps idr 丢掉 sei

    // idr帧
    private byte[] idr = null;
    // idr帧中的 sps
    private byte[] sps = null;
    // idr帧中的 pps
    private byte[] pps = null;
    // idr帧中的sei
    private byte[] sei = null;
    // 存放sps pps sei
    private byte[] extradata = null;
    // 海思头
    private boolean hig726 = false;

    public byte[] getData() {
        if (hig726) {
            byte[] hiHead = new byte[] {0x00, 0x01, (byte)(data.length / 2), 0x00};
            return ArrayUtils.addAll(hiHead, data);
        }
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;

        // i帧时才处理 datatype需要先有值
        if (data != null && this.getDT() == 0) {
            this.splitFrame();
        }
    }

    /**
     * 获取数据包类型
     * 
     * @return 0 1 2=表视频包 3=表音频包 4=表透传包
     */
    public byte getDT() {
        return Utils.getDataType(this.dataType);
    }

    /**
     * 判断数据包分包类型
     * 
     * @return 0=原子包 1=分包第一个包 2=分包最后一个包 3=分包中间包
     */
    public byte getPT() {
        return Utils.getPackType(this.dataType);
    }

    /**
     * 是否有sps头
     * 
     * @return
     */
    public boolean hasSps() {
        return this.sps != null;
    }

    /**
     * 是否有pps头
     * 
     * @return
     */
    public boolean hasPps() {
        return this.pps != null;
    }

    /**
     * 是否有sei头
     * 
     * @return
     */
    public boolean hasSei() {
        return this.sei != null;
    }

    public boolean hasExtradata() {
        return this.extradata != null;
    }

    /**
     * 是否有idr帧
     * 
     * @return
     */
    public boolean hasIdr() {
        return this.idr != null;
    }

    /**
     * 只获取idr部份的数据
     * 
     * @return
     */
    private void splitFrame() {
        byte[] buf = this.data;

        List<Integer> list = new ArrayList<Integer>();
        ByteBuf bf = Unpooled.buffer(buf.length);
        bf.writeBytes(buf);

        int id = 0;
        while (bf.readableBytes() > 4) {
            bf.markReaderIndex();
            id = bf.readerIndex();
            int nal = bf.readInt();
            if (nal == 1) {
                list.add(id);
            } else {
                bf.resetReaderIndex();
                bf.readByte();
            }
        }

        bf.clear();
        bf = null;

        int start = 0;
        int len = 0;
        for (int i = 0; i < list.size(); i++) {
            start = list.get(i);
            if (i == list.size() - 1) {
                // 说明最后一笔
                len = buf.length - start;
            } else {
                len = list.get(i + 1) - start;
            }

            byte[] curf = new byte[len];
            System.arraycopy(buf, start, curf, 0, len);

            byte nal_unit_type = (byte)(curf[4] & 0x0F);
            switch (nal_unit_type) {
                case 6: // sei
                    this.sei = curf;
                    break;
                case 7: // sps
                    this.sps = curf;
                    break;
                case 8: // pps
                    this.pps = curf;
                    break;
                case 5: // idr
                    this.idr = curf;
                    break;
                default:
                    this.idr = curf;
            }
        }

        len = 0;
        int sps_len = 0;
        int pps_len = 0;
        int sei_len = 0;
        if (this.sps != null)
            sps_len = this.sps.length;
        if (this.pps != null)
            pps_len = this.pps.length;
        if (this.sei != null)
            sei_len = this.sei.length;
        len = sps_len + pps_len + sei_len;
        byte[] ebuf = new byte[len];
        if (this.sps != null)
            System.arraycopy(this.sps, 0, ebuf, 0, sps_len);
        if (this.pps != null)
            System.arraycopy(this.pps, 0, ebuf, sps_len, pps_len);
        if (this.sei != null)
            System.arraycopy(this.sei, 0, ebuf, sps_len + pps_len, sei_len);
        this.extradata = ebuf;
    }

    /**
     * 释放内存 不一定要 但可能可以加速被gc检测
     */
    public void release() {
        this.data = null;
        this.sei = null;
        this.sps = null;
        this.pps = null;
        this.idr = null;
        this.dataSize = 0;
    }

    /**
     * 获取负载类型 1078表12
     * 
     * @return
     */
    public byte getCodeType() {
        byte ret = (byte)(this.flag2 & 127);
        return ret;
    }

    @Override
    public String toString() {

        byte dt = (byte)(this.dataType >> 4);

        String dtype;
        switch (dt) {
            case 0: // i帧
                dtype = "Video(I)";
                break;
            case 1: // p帧
                dtype = "Video(P)";
                break;
            case 2: // b帧
                dtype = "Video(B)";
                break;
            case 3:
                dtype = "Audio(A)";
                break;
            default:
                // 协议中是 4
                dtype = "Other(X)";
        }

        // 分包类型 0=原子包 1=分包第一个包 2=分包最后一个包 3=分包中间包
        byte packType = (byte)(this.dataType & 0x0F);

        return String.format("Frame=%s,packType=%d,packId=%d,sim=%s,chn=%d,dataSize=%d,time=%d", dtype, packType,
            this.packId, this.sim, this.chn, this.dataSize, this.getTimestamp());
    }

}
