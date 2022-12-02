package index.sfc.zorder;


import index.data.NumericRange;
import index.sfc.SFCDimensionDefinition;
import index.util.ByteArrayUtils;

import java.util.Arrays;
import java.util.BitSet;

/**
 * Convenience methods used to decode/convertToZorder Z-Order space filling curve values
 * (using a simple bit-interleaving approach).
 */
public class ZOrderUtils {
    public static NumericRange[] convertFromZorder(
            final byte[] bytes,
            final int bitsPerDimension,
            final SFCDimensionDefinition[] dimensionDefinitions) {

        final byte[] littleEndianBytes = ByteArrayUtils.swapEndianFormat(bytes);
        final BitSet bitSet = BitSet.valueOf(littleEndianBytes);

        int usedBits = bitsPerDimension * dimensionDefinitions.length;
        int usedBytes = (int) Math.ceil(usedBits / 8.0);
        int bitsetLength = (usedBytes * 8);
        int bitOffset = bitsetLength - usedBits;

        final NumericRange[] normalizedValues = new NumericRange[dimensionDefinitions.length];
        for (int d = 0; d < dimensionDefinitions.length; d++) {

            final BitSet dimensionSet = new BitSet(bitsPerDimension);
            int j = 0;
            for (int i = d; i < (bitsPerDimension * dimensionDefinitions.length); i += dimensionDefinitions.length) {
                dimensionSet.set(j, bitSet.get(bitOffset + i));
                j++;
            }

            normalizedValues[d] = decode(dimensionSet, 0, 1, dimensionDefinitions[d]);
        }

        return normalizedValues;
    }

    public static long[] indicesFromZorder(
            final byte[] bytes,
            final int[] bitsPerDimension,
            final int numDimensions) {

        final byte[] littleEndianBytes = ByteArrayUtils.swapEndianFormat(bytes);
        final BitSet bitSet = BitSet.valueOf(littleEndianBytes);
        final long[] coordinates = new long[numDimensions];

        int usedBits = 0;
        for (int d = 0; d < numDimensions; d++) {
            usedBits += bitsPerDimension[d];
        }

        int usedBytes = (int) Math.ceil(usedBits / 8.0);
        int bitsetLength = (usedBytes * 8);
        int bitOffset = bitsetLength - usedBits;

        for (int d = 0; d < numDimensions; d++) {
            final long rangePerDimension = (long) Math.pow(2, bitsPerDimension[d]);
            final BitSet dimensionSet = new BitSet();
            int j = 0;
            for (int i = d; i < (bitsPerDimension[d] * numDimensions); i += numDimensions) {
                dimensionSet.set(j, bitSet.get(bitOffset + i));
                j++;
            }
            coordinates[d] = decodeIndex(dimensionSet, rangePerDimension);
        }

        return coordinates;
    }

    private static long decodeIndex(
            final BitSet bs,
            final long rangePerDimension) {
        long floor = 0;
        long ceiling = rangePerDimension;
        long mid = 0;
        for (int i = 0; i < bs.length(); i++) {
            mid = (floor + ceiling) / 2;
            if (bs.get(i)) {
                floor = mid;
            } else {
                ceiling = mid;
            }
        }
        return mid;
    }

    private static NumericRange decode(
            final BitSet bs,
            double floor,
            double ceiling,
            final SFCDimensionDefinition dimensionDefinition) {
        double mid = 0;
        for (int i = 0; i < dimensionDefinition.getBitsOfPrecision(); i++) {
            mid = (floor + ceiling) / 2;
            if (bs.get(i)) {
                floor = mid;
            } else {
                ceiling = mid;
            }
        }
        return new NumericRange(
                dimensionDefinition.denormalize(floor),
                dimensionDefinition.denormalize(ceiling));
    }

    public static byte[] convertToZorder(
            final long[] coordinates,
            final int bitsPerDimension,
            final int numDimensions) {
        final BitSet[] bitSets = new BitSet[numDimensions];
        for (int d = 0; d < numDimensions; d++) {
            bitSets[d] = ByteArrayUtils.getBits(coordinates[d], 0, (long) (Math.pow(2, bitsPerDimension) - 1), bitsPerDimension);
        }
        int usedBits = bitsPerDimension * numDimensions;
        int usedBytes = (int) Math.ceil(usedBits / 8.0);
        int bitsetLength = (usedBytes * 8);
        int bitOffset = bitsetLength - usedBits;

        // round up to a bitset divisible by 8
        final BitSet combinedBitSet = new BitSet(bitsetLength);
        for (int i = 0; i < bitsPerDimension; i++) {
            for (int d = 0; d < numDimensions; d++) {
                combinedBitSet.set(bitOffset++, bitSets[d].get(i));
            }
        }
        final byte[] littleEndianBytes = combinedBitSet.toByteArray();
        byte[] retVal = ByteArrayUtils.swapEndianFormat(littleEndianBytes);
        if (retVal.length < usedBytes) {
            return Arrays.copyOf(retVal, usedBytes);
        }
        return retVal;
    }

    public static byte[] convertToZorder(
            final double[] normalizedValues,
            final int bitsPerDimension,
            final int numDimensions) {
        final BitSet[] bitSets = new BitSet[numDimensions];
        for (int d = 0; d < numDimensions; d++) {
            bitSets[d] = ByteArrayUtils.getBits(normalizedValues[d], 0, 1, bitsPerDimension);
        }
        int usedBits = bitsPerDimension * numDimensions;
        int usedBytes = (int) Math.ceil(usedBits / 8.0);
        int bitsetLength = (usedBytes * 8);
        int bitOffset = bitsetLength - usedBits;

        // round up to a bitset divisible by 8
        final BitSet combinedBitSet = new BitSet(bitsetLength);
        for (int i = 0; i < bitsPerDimension; i++) {
            for (int d = 0; d < numDimensions; d++) {
                combinedBitSet.set(bitOffset++, bitSets[d].get(i));
            }
        }
        final byte[] littleEndianBytes = combinedBitSet.toByteArray();
        byte[] retVal = ByteArrayUtils.swapEndianFormat(littleEndianBytes);
        if (retVal.length < usedBytes) {
            return Arrays.copyOf(retVal, usedBytes);
        }
        return retVal;
    }


}
