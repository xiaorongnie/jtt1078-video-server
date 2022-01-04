package com.transcodegroup.jtt1078.util;

public class Utils {
    /**
     * 获取数据包类型
     * 
     * @param dataType
     * @return 0 1 2=表视频包 3=表音频包 4=表透传包
     */
    public static byte getDataType(byte dataType) {
        return (byte)(dataType >> 4);
    }

    /**
     * 判断数据包分包类型
     * 
     * @param dataType
     * @return 0=原子包 1=分包第一个包 2=分包最后一个包 3=分包中间包
     */
    public static byte getPackType(byte dataType) {
        return (byte)(dataType & 0x0F);
    }

    /**
     * 格式化12位手机号
     * 
     * @param phoneNumber
     * @return
     */
    public static String formatPhoneNumber(String phoneNumber) {
        return addZeroForNum(phoneNumber, 12);
    }

    /**
     * 左补0
     * 
     * @param str
     * @param strLength
     * @return
     */
    public static String addZeroForNum(String str, int strLength) {
        int strLen = str.length();
        if (strLen < strLength) {
            while (strLen < strLength) {
                StringBuffer sb = new StringBuffer();
                sb.append("0").append(str);// 左补0
                // sb.append(str).append("0");//右补0
                str = sb.toString();
                strLen = str.length();
            }
        }

        return str;
    }

}
