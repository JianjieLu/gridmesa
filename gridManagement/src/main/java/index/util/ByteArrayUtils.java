package index.util;


import index.data.ByteArray;
import index.data.ByteArrayRange;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.tuple.Pair;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.Base64.Encoder;

/**
 * Convenience methods for converting binary data to and from strings. The
 * encoding and decoding is done in base-64. These methods should be used for
 * converting data that is binary in nature to a String representation for
 * transport. Use StringUtils for serializing and deserializing text-based data.
 * <p>
 * Additionally, this class has methods for manipulating byte arrays, such as
 * combining or incrementing them.
 */
public class ByteArrayUtils {
    private static Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();

    private static byte[] internalCombineArrays(
            final byte[] beginning,
            final byte[] end) {
        final byte[] combined = new byte[beginning.length + end.length];
        System.arraycopy(beginning, 0, combined, 0, beginning.length);
        System.arraycopy(end, 0, combined, beginning.length, end.length);
        return combined;
    }

    /**
     * Convert binary data to a string for transport
     *
     * @param byteArray the binary data
     * @return the base64url encoded string
     */
    public static String byteArrayToString(final byte[] byteArray) {
        return new String(ENCODER.encode(byteArray), StringUtils.getGeoWaveCharset());
    }

    /**
     * Convert a string representation of binary data back to a String
     *
     * @param str the string representation of binary data
     * @return the base64url decoded binary data
     */
    public static byte[] byteArrayFromString(final String str) {
        return Base64.getUrlDecoder().decode(str);
    }

    /**
     * Combine 2 arrays into one large array. If both are not null it will
     * append id2 to id1 and the result will be of length id1.length +
     * id2.length
     *
     * @param id1 the first byte array to use (the start of the result)
     * @param id2 the second byte array to combine (appended to id1)
     * @return the concatenated byte array
     */
    public static byte[] combineArrays(final byte[] id1, final byte[] id2) {
        byte[] combinedId;
        if ((id1 == null) || (id1.length == 0)) {
            combinedId = id2;
        } else if ((id2 == null) || (id2.length == 0)) {
            combinedId = id1;
        } else {
            // concatenate bin ID 2 to the end of bin ID 1
            combinedId = ByteArrayUtils.internalCombineArrays(id1, id2);
        }
        return combinedId;
    }

    /**
     * add 1 to the least significant bit in this byte array (the last byte in
     * the array)
     *
     * @param value the array to increment
     * @return will return true as long as the value did not overflow
     */
    public static boolean increment(final byte[] value) {
        for (int i = value.length - 1; i >= 0; i--) {
            value[i]++;
            if (value[i] != 0) {
                return true;
            }
        }
        return value[0] != 0;
    }

    public static ByteArray increment(final ByteArray byteArray) {
        byte[] newBytes = new byte[byteArray.getBytes().length];
        System.arraycopy(byteArray.getBytes(), 0, newBytes, 0, newBytes.length);
        boolean flag = increment(newBytes);
        if (flag) {
            return new ByteArray(newBytes);
        } else {
            throw new UnsupportedOperationException("Value overflow");
        }
    }

    public static boolean decrement(final byte[] value) {
        for (int i = value.length - 1; i >= 0; i--) {
            value[i]--;
            if (value[i] != -1) {
                return true;
            }
        }
        return value[0] != -1;
    }

    public static ByteArray decrement(final ByteArray byteArray) {
        byte[] newBytes = new byte[byteArray.getBytes().length];
        System.arraycopy(byteArray.getBytes(), 0, newBytes, 0, newBytes.length);
        boolean flag = decrement(newBytes);
        if (flag) {
            return new ByteArray(newBytes);
        } else {
            throw new UnsupportedOperationException("Value overflow");
        }
    }

    /**
     * Converts a UUID to a byte array
     *
     * @param uuid the uuid
     * @return the byte array representing that UUID
     */
    public static byte[] uuidToByteArray(final UUID uuid) {
        final ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }

    /**
     * Converts a long to a byte array
     *
     * @param l the long
     * @return the byte array representing that long
     */
    public static byte[] longToByteArray(final long l) {
        final ByteBuffer bb = ByteBuffer.allocate(Long.BYTES);
        bb.putLong(l);
        return bb.array();
    }

    /**
     * Converts a byte array to a long
     *
     * @param bytes the byte array the long
     * @return the long represented by the byte array
     */
    public static long byteArrayToLong(final byte[] bytes) {
        final ByteBuffer bb = ByteBuffer.allocate(Long.BYTES);
        bb.put(bytes);
        bb.flip();
        return bb.getLong();
    }

    public static byte[] binaryStringToIndex(String indexStr) {
        int length = indexStr.length();
        if (length % 8 != 0) {
            indexStr = indexStr +
                    org.apache.commons.lang3.StringUtils.repeat("0", (length / 8 + 1) * 8 - length);
        }
        //debug
//		System.out.println("行键补0至8的整数倍："+indexStr);
        byte[] index = new byte[(int) Math.ceil(length / 8.)];
        for (int i = 0; i < index.length; i++) {
            String s1 = indexStr.substring(i * 8, i * 8 + 8);
            index[i] = (byte) Integer.parseInt(s1, 2);
        }
        return index;
    }

    public static byte[] XZ2BinaryStringToIndex(String indexStr) {
        int length = indexStr.length();
        if (length % 8 != 0) {
            indexStr = indexStr +
                    org.apache.commons.lang3.StringUtils.repeat("0", (length / 8 + 1) * 8 - length);
        }
        //debug
//		System.out.println("行键补0至8的整数倍："+indexStr);
        byte[] index = new byte[(int) Math.ceil(length / 8.)];
        for (int i = 0; i < index.length; i++) {
            String s1 = indexStr.substring(i * 8, i * 8 + 8);
            index[i] = (byte) Integer.parseInt(s1, 2);
        }
        return index;
    }

    public static ByteArrayRange binaryStringToIndexRange(String indexStr) {
        int length = indexStr.length();
        String start = "", end = "";
        if (length % 8 != 0) {
            start = indexStr +
                    org.apache.commons.lang3.StringUtils.repeat("0", (length / 8 + 1) * 8 - length);
            end = indexStr +
                    org.apache.commons.lang3.StringUtils.repeat("1", (length / 8 + 1) * 8 - length);
        } else{
            start = indexStr;
            end = indexStr;
        }
        byte[] rangeStart = new byte[(int) Math.ceil(length / 8.)];
        byte[] rangeEnd = new byte[(int) Math.ceil(length / 8.) + 3];

        for (int i = 0; i < rangeStart.length; i++) {
            String s1 = start.substring(i * 8, i * 8 + 8);
            rangeStart[i] = (byte) Integer.parseInt(s1, 2);
            String s2 = end.substring(i * 8, i * 8 + 8);
            rangeEnd[i] = (byte) Integer.parseInt(s2, 2);
        }
        //由于实际行键末尾有层级，多一个字节，所以这里的rangeEnd如果不多几个字节，就会漏查
        rangeEnd[rangeEnd.length - 1] = (byte) 255;
        rangeEnd[rangeEnd.length - 2] = (byte) 255;
        rangeEnd[rangeEnd.length - 3] = (byte) 255;
//		System.out.print(Arrays.toString(rangeStart) + " " + Arrays.toString(rangeEnd));
//		System.out.println();
        return new ByteArrayRange(new ByteArray(rangeStart), new ByteArray(rangeEnd));
    }

    public static String[] binaryStringToIndexRange1(String indexStr) {
        int length = indexStr.length();
        String start = "", end = "";
        if (length % 8 != 0) {
            start = indexStr +
                    org.apache.commons.lang3.StringUtils.repeat("0", (length / 8 + 1) * 8 - length);
            end = indexStr +
                    org.apache.commons.lang3.StringUtils.repeat("1", (length / 8 + 1) * 8 - length);
        }
        String[] strings = new String[2];
        strings[0] = start;
        strings[1] = end;
        return strings;
    }

    public static String indexToBinaryString(byte[] index, int length) {
        StringBuilder full = new StringBuilder();
        for (byte b : index) {
            String S1 = Integer.toBinaryString(b & 0xff);
            S1 = org.apache.commons.lang3.StringUtils.repeat("0", Byte.SIZE - S1.length()) + S1;
            full.append(S1);
        }
        return full.substring(0, length);
    }

    /**
     * 返回当前层编码的3个邻居
     *
     * @param key
     * @param precision 当前层级
     * @return
     */
//	public static List<ByteArray> getNeighbour(ByteArray key, int precision){
//		String full = indexToBinaryString(key);
//		String rawS = full.substring(0, precision-2);
//		List<ByteArray> neighbour = new ArrayList<>(3);
//		String[] n = new String[4];
//		n[0] = rawS + "00" + org.apache.commons.lang3.StringUtils.repeat("0", 24 - precision);
//		n[1] = rawS + "01" + org.apache.commons.lang3.StringUtils.repeat("0", 24 - precision);
//		n[2] = rawS + "10" + org.apache.commons.lang3.StringUtils.repeat("0", 24 - precision);
//		n[3] = rawS + "11" + org.apache.commons.lang3.StringUtils.repeat("0", 24 - precision);
//		for(String str:n){
//			if(!str.equals(full)){
//				neighbour.add(binaryStringToIndex(str));
//			}
//		}
//		return neighbour;
//	}

//	/**
//	 * 返回空间编码在指定划分次数的所有孩子
//	 * @param key
//	 * @param precision 当前划分次数
//	 * @return des_precision 一般为初始划分次数
//	 */

    //TODO:需要重写
    public static List<ByteArray> getChildren(ByteArray key, int precision, int des_precision) {
        return null;
    }

    /**
     * Combines two variable length byte arrays into one large byte array and
     * appends the length of each individual byte array in sequential order at
     * the end of the combined byte array.
     * <p>
     * Given byte_array_1 of length 8 + byte_array_2 of length 16, the result
     * will be byte_array1 + byte_array_2 + 8 + 16.
     * <p>
     * Lengths are put after the individual arrays so they don't impact sorting
     * when used within the key of a sorted key-value data store.
     *
     * @param array1 the first byte array
     * @param array2 the second byte array
     * @return the combined byte array including the individual byte array
     * lengths
     */
    public static byte[] combineVariableLengthArrays(final byte[] array1, final byte[] array2) {
        Preconditions.checkNotNull(array1, "First byte array cannot be null");
        Preconditions.checkNotNull(array2, "Second byte array cannot be null");
        Preconditions.checkArgument(array1.length > 1, "First byte array cannot have length 0");
        Preconditions.checkArgument(array2.length > 1, "Second byte array cannot have length 0");
        final byte[] combinedWithoutLengths = ByteArrayUtils.internalCombineArrays(array1, array2);
        final ByteBuffer combinedWithLengthsAppended = ByteBuffer.allocate(combinedWithoutLengths.length + 8); // 8 for two integer lengths
        combinedWithLengthsAppended.put(combinedWithoutLengths);
        combinedWithLengthsAppended.putInt(array1.length);
        combinedWithLengthsAppended.putInt(array2.length);
        return combinedWithLengthsAppended.array();
    }

    public static Pair<byte[], byte[]> splitVariableLengthArrays(final byte[] combinedArray) {
        final ByteBuffer combined = ByteBuffer.wrap(combinedArray);
        final byte[] combinedArrays = new byte[combinedArray.length - 8];
        combined.get(combinedArrays);
        final ByteBuffer bb = ByteBuffer.wrap(combinedArrays);
        final int len1 = combined.getInt();
        final int len2 = combined.getInt();
        final byte[] part1 = new byte[len1];
        final byte[] part2 = new byte[len2];
        bb.get(part1);
        bb.get(part2);
        return Pair.of(part1, part2);
    }

    public static String shortToString(final short input) {
        return byteArrayToString(shortToByteArray(input));
    }

    public static short shortFromString(final String input) {
        return byteArrayToShort(byteArrayFromString(input));
    }

    public static byte[] shortToByteArray(final short input) {
        return new byte[]{(byte) (input & 0xFF), (byte) ((input >> 8) & 0xFF)};
    }

    public static short byteArrayToShort(final byte[] bytes) {
        int r = bytes[1] & 0xFF;
        r = (r << 8) | (bytes[0] & 0xFF);
        return (short) r;
    }

    public static byte[] variableLengthEncode(long n) {
        final int numRelevantBits = 64 - Long.numberOfLeadingZeros(n);
        int numBytes = (numRelevantBits + 6) / 7;
        if (numBytes == 0) {
            numBytes = 1;
        }
        final byte[] output = new byte[numBytes];
        for (int i = numBytes - 1; i >= 0; i--) {
            int curByte = (int) (n & 0x7F);
            if (i != (numBytes - 1)) {
                curByte |= 0x80;
            }
            output[i] = (byte) curByte;
            n >>>= 7;
        }
        return output;
    }

    public static long variableLengthDecode(final byte[] b) {
        long n = 0;
        for (int i = 0; i < b.length; i++) {
            final int curByte = b[i] & 0xFF;
            n = (n << 7) | (curByte & 0x7F);
            if ((curByte & 0x80) == 0) {
                break;
            }
        }
        return n;
    }

    /**
     * get the common bits number of two ByteArray
     *
     * @param b1
     * @param b2
     * @return
     */
    public static int commonBitsNum(byte[] b1, byte[] b2) {

        BitSet b1Bits = BitSet.valueOf(swapEndianFormat(b1));
        BitSet b2Bits = BitSet.valueOf(swapEndianFormat(b2));

        int commonBitsNum = 0;
        for (int i = 0; i < b1.length * ByteArray.BYTE_BITS_LENGTH; i++) {
            if (b1Bits.get(i) == b2Bits.get(i)) {
                commonBitsNum++;
            } else {
                break;
            }
        }
        return commonBitsNum;
//        int commonBits = 0;
//	    int c = 0;
//	    byte[] tmp;
//
//	    if(b1.length > b2.length){
//	        tmp = b1;
//	        b1 = b2;
//	        b2 = tmp;
//        }
//
//	    for(int i=0;i < b1.length;i++){
//
//	    	System.out.println(b1[i] ^ b2[i]);
//
//            int byteCommonBits = bytePrefix[ b1[i] ^ b2[i]];
//            commonBits += byteCommonBits;
//
//            if(byteCommonBits != 8) {
//                break;
//            }
//        }
//        return commonBits;
    }

    public static ByteArray commonPrefix(byte[] b1, byte[] b2) {

        byte[] tmp;
        if (b1.length > b2.length) {
            tmp = b1;
            b1 = b2;
            b2 = tmp;
        }
        int commonPrefix = commonBitsNum(b1, b2);

        final byte[] littleEndianBytes = swapEndianFormat(b1);
        final BitSet bitSetStart = BitSet.valueOf(littleEndianBytes);

        // start length may shorter than before
        byte[] bytes = new byte[b1.length];
        for (int i = 0; i < commonPrefix; i++) {
            if (bitSetStart.get(i)) {
                bytes[i / 8] |= 1 << (7 - i % 8);
            }
        }

        return new ByteArray(bytes);
    }

    public static byte[] swapEndianFormat(final byte[] b) {
        final byte[] endianSwappedBytes = new byte[b.length];
        for (int i = 0; i < b.length; i++) {
            endianSwappedBytes[i] = swapEndianFormat(b[i]);
        }
        return endianSwappedBytes;
    }

    private static byte swapEndianFormat(final byte b) {
        int converted = 0x00;
        converted ^= (b & 0b1000_0000) >> 7;
        converted ^= (b & 0b0100_0000) >> 5;
        converted ^= (b & 0b0010_0000) >> 3;
        converted ^= (b & 0b0001_0000) >> 1;
        converted ^= (b & 0b0000_1000) << 1;
        converted ^= (b & 0b0000_0100) << 3;
        converted ^= (b & 0b0000_0010) << 5;
        converted ^= (b & 0b0000_0001) << 7;
        return (byte) (converted & 0xFF);
    }

    public static BitSet getBits(
            final double value,
            double floor,
            double ceiling,
            final int bitsPerDimension) {
        final BitSet buffer = new BitSet(bitsPerDimension);
        for (int i = 0; i < bitsPerDimension; i++) {
            final double mid = (floor + ceiling) / 2;
            if (value >= mid) {
                buffer.set(i);
                floor = mid;
            } else {
                ceiling = mid;
            }
        }
        return buffer;
    }

    public static ByteArray[] splitDimension(final ByteArray index,
                                             final int precision,
                                             final int dimensions) {

//		BitSet indexBitSet = BitSet.valueOf(index.getBytes());
//		printBitSet(indexBitSet);
//		// TODO: check index length and dimensions num
//
//		BitSet[] bitSets = new BitSet[dimensions];
//		int bitsPerDim = precision / dimensions;
//		int dimUsedBytes = (int) Math.ceil(bitsPerDim / 8.0);
//		int dimBitSetLen = dimUsedBytes * 8;
//		for (int i = 0; i < dimensions; i++) {
//			bitSets[i] = new BitSet(dimBitSetLen);
//		}
//
//		int bitOffset = dimBitSetLen - bitsPerDim;
//		for (int i = precision - 1; i >= 0; i -= dimensions) {
//			for (int j = 0; j < dimensions; j++) {
//				bitSets[j].set(bitOffset, indexBitSet.get(i - j));
//			}
//			bitOffset++;
//		}
//
//		ByteArray[] splitResults = new ByteArray[dimensions];
//		for (int i = 0; i < dimensions; i++) {
//			printBitSet(bitSets[i]);
//			byte[] retVal = swapEndianFormat(bitSets[i].toByteArray());
//			if (retVal.length < dimUsedBytes) {
//				retVal = Arrays.copyOf(retVal, dimUsedBytes);
//			}
//			splitResults[i] = new ByteArray(retVal);
//		}
//		return splitResults;
        byte[] littleEndianBytes = swapEndianFormat(index.getBytes());
        BitSet indexBitSet = BitSet.valueOf(littleEndianBytes);
//		printBitSet(indexBitSet);

        BitSet[] bitSets = new BitSet[dimensions];
        int bitsPerDim = precision / dimensions;
        int dimUsedBytes = (int) Math.ceil(bitsPerDim / 8.0);
        int dimBitSetLen = dimUsedBytes * 8;
        for (int i = 0; i < dimensions; i++) {
            bitSets[i] = new BitSet(dimBitSetLen);
        }

        int bitOffset = dimBitSetLen - bitsPerDim;
        int usedBytes = (int) Math.ceil(precision / 8.0);
        int startBit = usedBytes * 8 - precision;
        for (int i = startBit; i < usedBytes * 8; i += dimensions) {
            for (int j = 0; j < dimensions; j++) {
                bitSets[j].set(bitOffset, indexBitSet.get(i + j));
            }
            bitOffset++;
        }

        ByteArray[] splitResults = new ByteArray[dimensions];
        for (int i = 0; i < dimensions; i++) {
//			printBitSet(bitSets[i]);
            byte[] retVal = swapEndianFormat(bitSets[i].toByteArray());
            if (retVal.length < dimUsedBytes) {
                retVal = Arrays.copyOf(retVal, dimUsedBytes);
            }
            splitResults[i] = new ByteArray(retVal);
        }
        return splitResults;
    }

    public static ByteArray combineDimension(final ByteArray lhs,
                                             final ByteArray rhs,
                                             final int precision) {
//		BitSet lhsBitSet = BitSet.valueOf(lhs.getBytes());
//		BitSet rhsBitSet = BitSet.valueOf(rhs.getBytes());
//		int dimBitLen = precision / 2;
//
//		int usedBytes = (int) Math.ceil(precision / 8.0);
//		int bitsLen = usedBytes * 8;
//
//		BitSet retBitSet = new BitSet(bitsLen);
//		int bitOffset = bitsLen - precision;
//		for (int i = dimBitLen - 1; i >= 0; i--) {
//			retBitSet.set(bitOffset++, lhsBitSet.get(i));
//			retBitSet.set(bitOffset++, rhsBitSet.get(i));
//		}
//
//		byte[] retVal = swapEndianFormat(retBitSet.toByteArray());
//		if (retVal.length < usedBytes) {
//			retVal = Arrays.copyOf(retVal, usedBytes);
//		}
//		return new ByteArray(retVal);
        BitSet lhsBitSet = BitSet.valueOf(swapEndianFormat(lhs.getBytes()));
        BitSet rhsBitSet = BitSet.valueOf(swapEndianFormat(rhs.getBytes()));

        int usedBytes = (int) Math.ceil(precision / 8.0);
        int bitsLen = usedBytes * 8;
        BitSet retBitSet = new BitSet(bitsLen);
        int bitOffset = bitsLen - precision;

        int dimUseBytes = (int) Math.ceil(usedBytes / 2.0);
        int dimBitOffset = dimUseBytes * 8 - precision / 2;
        for (int i = dimBitOffset; i < dimUseBytes * 8; i++) {
            retBitSet.set(bitOffset++, lhsBitSet.get(i));
            retBitSet.set(bitOffset++, rhsBitSet.get(i));
        }

//		System.out.print("lhs: "); printBitSet(lhsBitSet);
//		System.out.print("rhs: "); printBitSet(rhsBitSet);
//		System.out.print("ret: "); printBitSet(retBitSet);
        byte[] retVal = swapEndianFormat(retBitSet.toByteArray());
        if (retVal.length < usedBytes) {
            retVal = Arrays.copyOf(retVal, usedBytes);
        }
        return new ByteArray(retVal);
    }

    public static void printBitSet(BitSet bitSet) {
        for (int i = 0; i < bitSet.length(); i++) {
            System.out.print(bitSet.get(i) ? 1 : 0);
        }
        System.out.println();
    }

    public static byte[] concat(byte[]... arrays) {
        int length = 0;
        for (byte[] array : arrays) {
            length += array.length;
        }
        byte[] result = new byte[length];
        int pos = 0;
        for (byte[] array : arrays) {
            System.arraycopy(array, 0, result, pos, array.length);
            pos += array.length;
        }
        return result;
    }

    public static void main(String[] args) {
        binaryStringToIndexRange("01010101");
    }
}
