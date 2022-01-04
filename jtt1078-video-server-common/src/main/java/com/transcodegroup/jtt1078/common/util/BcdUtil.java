package com.transcodegroup.jtt1078.common.util;

/**
 * 
 * TG 10进制字符串字符串转BCD编码
 */
public class BcdUtil {

    /**
     * 获取需要下发的设备的终端手机号
     */
    public static byte[] getPhoneNumber(String sim) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < 12 - sim.length(); i++) {
            buffer.append("0");
        }
        buffer.append(sim);
        return str2Bcd(buffer.toString());
    }

    /**
     * 10进制字符串转byte[]
     */
    public static byte[] str2Bcd(String asc) {
        int len = asc.length();
        int mod = len % 2;

        if (mod != 0) {
            asc = "0" + asc;
            len = asc.length();
        }

        byte abt[] = new byte[len];
        if (len >= 2) {
            len = len / 2;
        }

        byte bbt[] = new byte[len];
        abt = asc.getBytes();
        int j, k;

        for (int p = 0; p < asc.length() / 2; p++) {
            if ((abt[2 * p] >= '0') && (abt[2 * p] <= '9')) {
                j = abt[2 * p] - '0';
            } else if ((abt[2 * p] >= 'a') && (abt[2 * p] <= 'z')) {
                j = abt[2 * p] - 'a' + 0x0a;
            } else {
                j = abt[2 * p] - 'A' + 0x0a;
            }

            if ((abt[2 * p + 1] >= '0') && (abt[2 * p + 1] <= '9')) {
                k = abt[2 * p + 1] - '0';
            } else if ((abt[2 * p + 1] >= 'a') && (abt[2 * p + 1] <= 'z')) {
                k = abt[2 * p + 1] - 'a' + 0x0a;
            } else {
                k = abt[2 * p + 1] - 'A' + 0x0a;
            }

            int a = (j << 4) + k;
            byte b = (byte)a;
            bbt[p] = b;
        }
        return bbt;
    }

    /**
     * byte[]转10进制字符串,并删除前面的0
     */
    public static String bcd2Str(byte[] bytes) {
        StringBuffer temp = new StringBuffer(bytes.length * 2);

        for (int i = 0; i < bytes.length; i++) {
            temp.append(String.format("%02x", bytes[i]));
        }
        String str = temp.toString().replaceFirst("^0*", "");
        return str;
    }
}