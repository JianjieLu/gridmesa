package util;

import java.util.Iterator;

/**
 * @Author zhangjianhao
 * @Date 2021/10/28
 */

public class StringUtil {

    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static boolean isLess(String str1, String str2) {
        int i = Integer.parseInt(str1);
        int j = Integer.parseInt(str2);
        return i - j < 0;
    }

    public static String inverse(String str) {
        if (isEmpty(str)) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        int len = str.length();
        for (int i = len - 1; i >= 0; i--) {
            sb.append(str.charAt(i));
        }
        return sb.toString();
    }

    public static byte[] convertLongToByteArray(long l) {

        String str = Long.toBinaryString(l);
        //不足64位先在前面补0
        int diff = 64 - str.length();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < diff; i++) {
            sb.append("0");
        }
        sb.append(str);
        String finalStr = sb.toString();
        //按照8位拆分
        byte[] bytes = new byte[8];
        int idx = 0;
        for (int i = 0; i < 64; i += 8) {
            String tmp = finalStr.substring(i, i + 8);
            int t = Integer.parseInt(tmp, 2);
            t = t > 127 ? t - 256 : t;
            bytes[idx++] = (byte) t;
        }
        return bytes;
    }


    public static int parseInt(String str) {
        if (isEmpty(str)) {
            return 0;
        }
        return Integer.parseInt(str);
    }

    public static String commonPrefix(String str1, String str2) {
        if (isEmpty(str1) || isEmpty(str2)) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        int len1 = str1.length();
        int len2 = str2.length();
        int min = Math.min(len1, len2);
        for (int i = 0; i < min; i++) {
            if (str1.charAt(i) != str2.charAt(i)) {
                break;
            }
            sb.append(str1.charAt(i));
        }
        //长度必须是偶数
        return sb.length() % 2 == 0 ? sb.toString() : sb.toString().substring(0, sb.length() - 1);
    }

    public static int parseBinaryStrToInt(String str) {
        if (isEmpty(str)) {
            return 0;
        }
        if (str.length() > 32) {
            throw new IllegalArgumentException("str length is wrong,plz check again.");
        }
        int res = 0;
        int len = str.length();
        for (int i = 0; i < len; i++) {
            int tmp = str.charAt(i) - '0';
            if (tmp != 0) {
                res += Math.pow(2, len - i - 1);
            }
        }
        return res;
    }

    public static long parseBinaryStrToLong(String str) {
        if (isEmpty(str)) {
            return 0;
        }
        if (str.length() > 64) {
            throw new IllegalArgumentException("str length is wrong,plz check again.");
        }
        long res = 0L;
        int len = str.length();
        for (int i = 0; i < len; i++) {
            int tmp = str.charAt(i) - '0';
            if (tmp != 0) {
                res += Math.pow(2, len - i - 1);
            }
        }
        return res;
    }

    /**
     * 将若干id根据连接符拼接成字符串，作为整体存储到一列中
     *
     * @param iterator  id集合
     * @param connector 连接符
     * @return 拼接后的字符串
     */
    public static String buildIdsStr(Iterator<String> iterator, String connector) {
        StringBuilder sb = new StringBuilder();
        while (iterator.hasNext()) {
            sb.append(iterator.next()).append(connector);
        }
        return sb.toString();
    }
}
