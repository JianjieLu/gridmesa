package index.util;

/**
 * Always high bits at the start.
 */
public class BitUtil {

    /**
     * Int32 to byte[4]
     */
    public static byte[] int32ToBinary(int value) {
        byte[] binary = new byte[4];
        binary[0] = (byte) (value >> 24);
        binary[1] = (byte) ((value >> 16) & 0xff);
        binary[2] = (byte) ((value >> 8) & 0xff);
        binary[3] = (byte) (value & 0xff);
        return binary;
    }

    public static void int32ToBinary(int value, byte[] binary, int start, int end) {
        if (start < 0 || end - start != 4 || end > binary.length) {
            throw new IllegalArgumentException("Destination bytes array must have 4 bytes.");
        }
        binary[start] = (byte) (value >> 24);
        binary[start + 1] = (byte) ((value >> 16) & 0xff);
        binary[start + 2] = (byte) ((value >> 8) & 0xff);
        binary[start + 3] = (byte) (value & 0xff);
    }

    public static void int32ToBinary(int value, byte[] binary, int start) {
        int32ToBinary(value, binary, start, start + 4);
    }

    /**
     * byte[4] to int32.
     */
    public static int binaryToInt32(byte[] binary) {
        return binaryToInt32(binary, 0, binary.length);
    }

    public static int binaryToInt32(byte[] binary, int start) {
        return binaryToInt32(binary, start, start + 4);
    }

    public static int binaryToInt32(byte[] binary, int start, int end) {
        if (start < 0 || end - start != 4 || end > binary.length) {
            throw new IllegalArgumentException("Must use 4 bytes.");
        }
        int value = 0;
        value = value
                | ((binary[start] & 0xff) << 24)
                | ((binary[start + 1] & 0xff) << 16)
                | ((binary[start + 2] & 0xff) << 8)
                | (binary[start + 3] & 0xff);
        return value;
    }

    public static void idToBinary(String id, byte[] binary, int start) {
        idToBinary(id, binary, start, start + 32);
    }

    public static void idToBinary(String id, byte[] binary, int start, int end) {
        if (start < 0 || end - start != 32 || end > binary.length) {
            throw new IllegalArgumentException("Destination bytes array must have 32 bytes.");
        }
        byte[] temp = id.getBytes();
        System.arraycopy(temp, 0, binary, start, temp.length);
    }

    public static String binaryToId(byte[] binary, int start) {
        return binaryToId(binary, start, start + 32);
    }

    private static String binaryToId(byte[] binary, int start, int end) {
        if (start < 0 || end - start != 32 || end > binary.length) {
            throw new IllegalArgumentException("ID Must use 32 bytes.");
        }
        byte[] newid = new byte[32];
        System.arraycopy(binary, start, newid, 0, 32);
        return new String(newid);
    }

    /**
     * int64 to byte[8]
     */
    public static byte[] int64ToBinary(long value) {
        byte[] binary = new byte[8];
        binary[0] = (byte) (value >> 56);
        binary[1] = (byte) ((value >> 48) & 0xff);
        binary[2] = (byte) ((value >> 40) & 0xff);
        binary[3] = (byte) ((value >> 32) & 0xff);
        binary[4] = (byte) ((value >> 24) & 0xff);
        binary[5] = (byte) ((value >> 16) & 0xff);
        binary[6] = (byte) ((value >> 8) & 0xff);
        binary[7] = (byte) (value & 0xff);
        return binary;
    }

    public static void int64ToBinary(long value, byte[] binary, int start, int end) {
        if (start < 0 || end - start != 8 || end > binary.length) {
            throw new IllegalArgumentException("Destination bytes array must have 4 bytes.");
        }
        binary[start] = (byte) (value >> 56);
        binary[start + 1] = (byte) ((value >> 48) & 0xff);
        binary[start + 2] = (byte) ((value >> 40) & 0xff);
        binary[start + 3] = (byte) ((value >> 32) & 0xff);
        binary[start + 4] = (byte) ((value >> 24) & 0xff);
        binary[start + 5] = (byte) ((value >> 16) & 0xff);
        binary[start + 6] = (byte) ((value >> 8) & 0xff);
        binary[start + 7] = (byte) (value & 0xff);
    }

    public static void int64ToBinary(long value, byte[] binary, int start) {
        int64ToBinary(value, binary, start, start + 8);
    }

    /**
     * byte[8] to int64
     */
    public static long binaryToInt64(byte[] binary) {
        return binaryToInt64(binary, 0, binary.length);
    }

    public static long binaryToInt64(byte[] binary, int start) {
        return binaryToInt64(binary, start, start + 8);
    }

    public static long binaryToInt64(byte[] binary, int start, int end) {
        if (start < 0 || end - start != 8 || end > binary.length) {
            throw new IllegalArgumentException("Must use 8 bytes.");
        }
        long value = 0;
        value = value
                | ((long) (binary[start] & 0xff) << 56)
                | ((long) (binary[start + 1] & 0xff) << 48)
                | ((long) (binary[start + 2] & 0xff) << 40)
                | ((long) (binary[start + 3] & 0xff) << 32)
                | ((long) (binary[start + 4] & 0xff) << 24)
                | ((long) (binary[start + 5] & 0xff) << 16)
                | ((long) (binary[start + 6] & 0xff) << 8)
                | (long) (binary[start + 7] & 0xff);
        return value;
    }


}
