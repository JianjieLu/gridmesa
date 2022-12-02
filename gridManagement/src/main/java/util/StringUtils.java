package util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Convenience methods for converting to and from strings. The encoding and
 * decoding of strings uses UTF-8, and these methods should be used for
 * serializing and deserializing text-based data, not for converting binary data
 * to a String representation. Use ByteArrayUtils for converting data that is
 * binary in nature to a String for transport.
 */
public class StringUtils {
    public static final Charset UTF8_CHARSET = Charset.forName("UTF-8");
    public static final String GEOWAVE_CHARSET_PROPERTY_NAME = "geowave.charset";
    private static final Logger LOGGER = LoggerFactory.getLogger(StringUtils.class);
    private static final String DEFAULT_GEOWAVE_CHARSET = "ISO-8859-1";
    private static Charset geowaveCharset = null;

    public static Charset getGeoWaveCharset() {
        if (geowaveCharset == null) {
            String charset = System.getProperty(
                    GEOWAVE_CHARSET_PROPERTY_NAME,
                    DEFAULT_GEOWAVE_CHARSET);
            geowaveCharset = Charset.forName(charset);
        }
        return geowaveCharset;
    }

    /**
     * Utility to convert a String to bytes
     *
     * @param string incoming String to convert
     * @return a byte array
     */
    public static byte[] stringToBinary(
            final String string) {
        return string.getBytes(getGeoWaveCharset());
    }

    /**
     * Utility to convert a String to bytes
     *
     * @param strings incoming String to convert
     * @return a byte array
     */
    public static byte[] stringsToBinary(
            final String strings[]) {
        int len = 4;
        final List<byte[]> strsBytes = new ArrayList<byte[]>();
        for (final String str : strings) {
            final byte[] strByte = str.getBytes(getGeoWaveCharset());
            strsBytes.add(strByte);
            len += (strByte.length + 4);

        }
        final ByteBuffer buf = ByteBuffer.allocate(len);
        buf.putInt(strings.length);
        for (final byte[] str : strsBytes) {
            buf.putInt(str.length);
            buf.put(str);
        }
        return buf.array();
    }

    /**
     * Utility to convert bytes to a String
     *
     * @param binary a byte array to convert to a String
     * @return a String representation of the byte array
     */
    public static String stringFromBinary(
            final byte[] binary) {
        return new String(
                binary,
                getGeoWaveCharset());
    }

    /**
     * Utility to convert bytes to a String
     *
     * @param binary a byte array to convert to a String
     * @return a String representation of the byte array
     */
    public static String[] stringsFromBinary(
            final byte[] binary) {
        final ByteBuffer buf = ByteBuffer.wrap(binary);
        final int count = buf.getInt();
        final String[] result = new String[count];
        for (int i = 0; i < count; i++) {
            final int size = buf.getInt();
            final byte[] strBytes = new byte[size];
            buf.get(strBytes);
            result[i] = new String(
                    strBytes,
                    getGeoWaveCharset());
        }
        return result;
    }

    /**
     * Convert a number to a string. In this case we ensure that it is safe for
     * Accumulo table names by replacing '-' with '_'
     *
     * @param number the number to convert
     * @return the safe string representing that number
     */
    public static String intToString(
            final int number) {
        return org.apache.commons.lang3.StringUtils.replace(
                Integer.toString(number),
                "-",
                "_");
    }

    public static Map<String, String> parseParams(
            final String params)
            throws NullPointerException {
        final Map<String, String> paramsMap = new HashMap<String, String>();
        final String[] paramsSplit = params.split(";");
        for (final String param : paramsSplit) {
            final String[] keyValue = param.split("=");
            if (keyValue.length != 2) {
                LOGGER.warn("Unable to parse param '" + param + "'");
                continue;
            }
            paramsMap.put(
                    keyValue[0].trim(),
                    keyValue[1].trim());
        }
        return paramsMap;
    }

    public static String getStart(String[] str) {
        String tmp = "";
        boolean flag = true;
        for (int i = 1; i < str[0].length() && flag; i++) {
            tmp = str[0].substring(0, i);
            for (int j = 1; j < str.length && flag; j++) {
                if (!str[j].startsWith(tmp)) {
                    flag = false;
                    break;
                }
            }
        }
        return tmp.substring(0, tmp.length() - 1);
    }

    public static String timeIndexToString(byte[] timeCoding) {
        StringBuilder codingStr = new StringBuilder();
        for (byte b : timeCoding) {
            codingStr.append(String.format("%02d", b));
        }
        return codingStr.toString();
    }

    public static byte[] timeStringToBytes(String timeStr) {
        byte[] bytes = new byte[timeStr.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = Byte.parseByte(timeStr.substring(i << 1, (i << 1) + 2));
        }
        return bytes;
    }

    public static String longestCommonPrefix(String[] strs) {
        if (strs.length == 0)
            return "";
        String prefix = strs[0];
        for (int i = 1; i < strs.length; i++)
            while (strs[i].indexOf(prefix) != 0) {
                prefix = prefix.substring(0, prefix.length() - 1);
                if (prefix.isEmpty()) return "";
            }
        return prefix;
    }
}
