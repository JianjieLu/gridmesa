package index.sfc.zorder;

import geoobject.BoundingBox;
import geoobject.Polygon;
import index.coding.spatial.geohash.GeoHashSizeTable;
import index.data.*;
import index.persist.PersistenceUtils;
import index.sfc.SFCDimensionDefinition;
import index.sfc.SpaceFillingCurve;
import index.util.ByteArrayUtils;

import java.io.Serializable;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.*;

/***
 * Implementation of a ZOrder Space Filling Curve. Also called Morton, GeoHash, etc.
 */
public class ZOrderSFC implements SpaceFillingCurve, Serializable {
    private static final int MAX_CACHED_QUERIES = 500;
    private final Map<QueryCacheKey, List<ByteArrayRange>> queryDecompositionCache =
            new LinkedHashMap<QueryCacheKey, List<ByteArrayRange>>(MAX_CACHED_QUERIES + 1, .75F, true) {
                @Override
                public boolean removeEldestEntry(
                        final Map.Entry<QueryCacheKey, List<ByteArrayRange>> eldest) {
                    return size() > MAX_CACHED_QUERIES;
                }
            };
    private SFCDimensionDefinition[] dimensionDefs;
    private int[] cardinalityPerDimension;
    private long[] binsPerDimension;//the number of bins in each dimension
    private int maxcardinality;//the max bits of precision in each dimension

    public ZOrderSFC() {
        super();
    }

    /***
     * Use the SFCFactory.createSpaceFillingCurve method - don't call this constructor directly
     */
    public ZOrderSFC(final SFCDimensionDefinition[] dimensionDefs) {
        init(dimensionDefs);
    }

    private void init(final SFCDimensionDefinition[] dimensionDefs) {
        this.dimensionDefs = dimensionDefs;
        cardinalityPerDimension = new int[dimensionDefs.length];
        binsPerDimension = new long[dimensionDefs.length];
        maxcardinality = 1;
        for (int i = 0; i < dimensionDefs.length; i++) {
            cardinalityPerDimension[i] = Math.max(dimensionDefs[i].getBitsOfPrecision(), 1);
            maxcardinality = Math.max(dimensionDefs[i].getBitsOfPrecision(), maxcardinality);
            binsPerDimension[i] = (long) Math.pow(2, cardinalityPerDimension[i]);
        }
    }

    /***
     * it maps a multi-dimensional data to a 1-dimentional index called id.
     */
    @Override
    public ByteArray getId(final double[] values) {
        final double[] normalizedValues = new double[values.length];
        for (int d = 0; d < values.length; d++) {
            normalizedValues[d] = dimensionDefs[d].normalize(values[d]);
        }
        return new ByteArray(ZOrderUtils.convertToZorder(normalizedValues, maxcardinality, values.length));
    }

    /***
     * it is a invert method of function getId , which invert an Id to a range,and the
     * range is multi-dimensional,every dimension has its own range.
     */
    @Override
    public MultiDimensionalNumericData getRanges(final ByteArray id) {
        return new BasicNumericDataset(ZOrderUtils.convertFromZorder(id.getBytes(), maxcardinality, dimensionDefs));
    }

    /***
     * it is also a invert method of function getId , which invert an Id to a multi-dimensional
     * index ,every dimension has its own index of its own bounds. etc
     * 15 =(1111) -> (3,3)
     */
    @Override
    public long[] getCoordinates(final ByteArray id) {
        return ZOrderUtils.indicesFromZorder(id.getBytes(), cardinalityPerDimension, dimensionDefs.length);
    }

    @Override
    public double[] getIdRangePerDimension() {
        final double[] retVal = new double[dimensionDefs.length];
        for (int i = 0; i < dimensionDefs.length; i++) {
            retVal[i] = dimensionDefs[i].getRange() / binsPerDimension[i];
        }
        return retVal;
    }

    @Override
    public List<ByteArray> getNeighbours(ByteArray index) {
        List<ByteArray> neighbours = new ArrayList<>();
        ByteArray neighbourByteArray;
        long[] coordinates = getCoordinates(index);
        long centerX = coordinates[0];
        long centerY = coordinates[1];

        for (long x = centerX - 1; x <= centerX + 1; x++) {
            for (long y = centerY - 1; y <= centerY + 1; y++) {
                if ((x == centerX) && (y == centerY)) {
                    continue;
                }
                coordinates = new long[]{x, y};
                byte[] neighbourByte = ZOrderUtils.convertToZorder(coordinates, dimensionDefs[0].getBitsOfPrecision(), 2);
                neighbourByteArray = new ByteArray(neighbourByte);
                neighbours.add(neighbourByteArray);
            }
        }

        return neighbours;
    }

    @Override
    public BigInteger getEstimatedIdCount(final MultiDimensionalNumericData data) {
        final double[] mins = data.getMinValuesPerDimension();
        final double[] maxes = data.getMaxValuesPerDimension();
        BigInteger estimatedIdCount = BigInteger.valueOf(1);
        for (int d = 0; d < data.getDimensionCount(); d++) {
            final long binMin = normalizeDimension(dimensionDefs[d], mins[d], binsPerDimension[d], true, false);
            long binMax = normalizeDimension(dimensionDefs[d], maxes[d], binsPerDimension[d], false, false);
            binMax = binMin > binMax ? binMax : binMin;
            estimatedIdCount = estimatedIdCount.multiply(BigInteger.valueOf((Math.abs(binMax - binMin) + 1)));
        }
        return estimatedIdCount;
    }

    /***
     * Used to normalize the value based on the dimension definition, which
     * includes the dimensional bounds and the bits of precision. This ensures
     * the maximum amount of fidelity for represented values.
     *
     * @param boundedDimensionDefinition
     *            describes the min, max, and cardinality of a dimension
     * @param value
     *            value to be normalized
     * @param bins
     *            precomputed number of bins in this dimension the number of
     *            bins expected based on the cardinality of the definition
     * @param isMin
     *            flag indicating if this value is a minimum of a range in which
     *            case it needs to be inclusive on a boundary, otherwise it is
     *            exclusive
     * @return value after normalization
     * @throws IllegalArgumentException
     *             thrown when the value passed doesn't fit with in the
     *             dimension definition provided
     */
    public long normalizeDimension(
            final SFCDimensionDefinition boundedDimensionDefinition,
            final double value,
            final long bins,
            final boolean isMin,
            final boolean overInclusiveOnEdge)
            throws IllegalArgumentException {
        final double normalizedValue = boundedDimensionDefinition.normalize(value);
        if ((normalizedValue < 0) || (normalizedValue > 1)) {
            throw new IllegalArgumentException("Value (" + value + ") is not within dimension bounds. The normalized value ("
                    + normalizedValue + ") must be within (0,1)");
        }
        // scale it to a value within the bits of precision,
        // because max is handled as exclusive and min is inclusive, we need to
        // handle the edge differently
        if ((isMin && !overInclusiveOnEdge) || (!isMin && overInclusiveOnEdge)) {
            // this will round up on the edge
            return (long) Math.min(Math.floor(normalizedValue * bins), bins - 1);
        } else {
            // this will round down on the edge
            return (long) Math.max(Math.ceil(normalizedValue * bins) - 1L, 0);
        }

    }

    /***
     * {@inheritDoc}
     */
    @Override
    public List<ByteArrayRange> decomposeRange(
            final MultiDimensionalNumericData query,
            final int maxRecursive) {
        final QueryCacheKey key =
                new QueryCacheKey(
                        query.getMinValuesPerDimension(),
                        query.getMaxValuesPerDimension(),
                        maxRecursive);
        List<ByteArrayRange> rangeDecomp = queryDecompositionCache.get(key);
        if (rangeDecomp == null) {
            rangeDecomp = decomposeRangeUtil2(query, maxRecursive);
            queryDecompositionCache.put(key, rangeDecomp);
        }
        return rangeDecomp;
    }

    @Override
    public List<ByteArrayRange> decomposeRange(Polygon polygon, int maxRecursive) {
        /************* Part 1 Normalize and getbounds *************/
        BoundingBox querybox = polygon.getBoundingBox();
        NumericRange[] range = new NumericRange[2];
        range[0] = new NumericRange(querybox.getMinLng(), querybox.getMaxLng());
        range[1] = new NumericRange(querybox.getMinLat(), querybox.getMaxLat());
        BasicNumericDataset query = new BasicNumericDataset(range);
        final double[] queryMins = query.getMinValuesPerDimension();//minLon minLat or minRow minCol
        final double[] queryMaxes = query.getMaxValuesPerDimension();//maxLon maxLat or maxRow maxCol
        final double[] normalizedMins = new double[query.getDimensionCount()];
        final double[] normalizedMaxes = new double[query.getDimensionCount()];
        for (int d = 0; d < query.getDimensionCount(); d++) {
            normalizedMins[d] = dimensionDefs[d].normalize(queryMins[d]);
            normalizedMaxes[d] = dimensionDefs[d].normalize(queryMaxes[d]);
        }
        final byte[] minZorder = ZOrderUtils.convertToZorder(normalizedMins, maxcardinality, query.getDimensionCount());
        final byte[] maxZorder = ZOrderUtils.convertToZorder(normalizedMaxes, maxcardinality, query.getDimensionCount());
        /************* Part 2 get commonprefix and offset *************/
        List<ByteArrayRange> ranges = new ArrayList<>(100);
        ArrayDeque<ByteArrayRange> remaining = new ArrayDeque<>(100);
        int commonBitsNum = ByteArrayUtils.commonBitsNum(minZorder, maxZorder);
        ByteArray commonPrefix = ByteArrayUtils.commonPrefix(minZorder, maxZorder);
        int offset = commonPrefix.getBytes().length * ByteArray.BYTE_BITS_LENGTH - commonBitsNum;

        /************* Part 3 get ranges from top to bottom *************/
        ByteArrayRange quadrantRange = getSubQuadrantRange(commonPrefix, offset, 0L);
        if (polygon.contains(rangeToBoundingbox(quadrantRange))) {
            ranges.add(new ByteArrayRange(quadrantRange, true));
        } else if (polygon.intersects(rangeToBoundingbox(quadrantRange))) {
            remaining.add(new ByteArrayRange(quadrantRange, false));
        }
        remaining.add(ByteArrayRange.LevelTerminator);
        offset -= dimensionDefs.length;

        int level = 0;
        while (level < maxRecursive && !remaining.isEmpty() && offset >= 0) {
            ByteArrayRange next = remaining.poll();
            if (next.equals(ByteArrayRange.LevelTerminator)) {
                if (!remaining.isEmpty()) {
                    level += 1;
                    offset -= dimensionDefs.length;
                    remaining.add(ByteArrayRange.LevelTerminator);
                }
            } else {
                ByteArray prefix = next.getStart();
                long quadrant = 0L;
                while (quadrant < (1 << dimensionDefs.length)) {
                    ByteArrayRange subQuadrantRange = getSubQuadrantRange(prefix, offset, quadrant);
                    if (polygon.contains(rangeToBoundingbox(subQuadrantRange))) {
                        ranges.add(new ByteArrayRange(subQuadrantRange, true));
                    } else if (polygon.intersects(rangeToBoundingbox(subQuadrantRange))) {
                        remaining.add(new ByteArrayRange(subQuadrantRange, false));
                    }
                    quadrant += 1;
                }
            }
        }
        while (!remaining.isEmpty()) {
            ByteArrayRange minMax = remaining.poll();
            if (!minMax.equals(ByteArrayRange.LevelTerminator)) {
                ranges.add(new ByteArrayRange(minMax.getStart(), minMax.getEnd(), false, false));
            }
        }
//		return ranges;
        return (List<ByteArrayRange>) ByteArrayRange.mergeIntersections(ranges, ByteArrayRange.MergeOperation.UNION);//sort
    }

    public ByteArrayRange getSubQuadrantRange(ByteArray prefix, int bitOffset, Long quadrant) {
        byte[] quadrantBytes = ByteArrayUtils.longToByteArray(quadrant);
        BitSet quadrantBitset = BitSet.valueOf(new byte[]{quadrantBytes[7]});

        byte[] min = prefix.getBytes().clone();
        final byte[] littleEndianBytes = ByteArrayUtils.swapEndianFormat(min);
        final BitSet minbitSet = BitSet.valueOf(littleEndianBytes);
        int bitLength = littleEndianBytes.length * ByteArray.BYTE_BITS_LENGTH;
        for (int i = 0; i < dimensionDefs.length; i++) {
            if (quadrantBitset.get(i)) {
                minbitSet.set(bitLength - bitOffset - (i + 1));
            }
        }

        minbitSet.set(bitLength - bitOffset, bitLength, false);
        final BitSet maxbitSet = (BitSet) minbitSet.clone();
        maxbitSet.set(bitLength - bitOffset, bitLength, true);

        // start length may shorter than before
        byte[] minBytes = new byte[min.length];
        for (int i = 0; i < bitLength; i++) {
            if (minbitSet.get(i)) {
                minBytes[i / 8] |= 1 << (7 - i % 8);
            }
        }

        // start length may shorter than before
        byte[] maxBytes = new byte[min.length];
        for (int i = 0; i < bitLength; i++) {
            if (maxbitSet.get(i)) {
                maxBytes[i / 8] |= 1 << (7 - i % 8);
            }
        }
        return new ByteArrayRange(new ByteArray(minBytes), new ByteArray(maxBytes));
    }

    /***
     * {@inheritDoc}
     */
    @Override
    public List<ByteArrayRange> decomposeRangeFully(final MultiDimensionalNumericData query) {
        return decomposeRange(query, dimensionDefs[0].getBitsOfPrecision());
    }

    public List<ByteArrayRange> decomposeRangeUtil(
            final MultiDimensionalNumericData query,
            final int maxRecursive) {
        /************* Part 1 Normalize and getbounds *************/
        final double[] queryMins = query.getMinValuesPerDimension();//minLon minLat or minRow minCol
        final double[] queryMaxes = query.getMaxValuesPerDimension();//maxLon maxLat or maxRow maxCol
        final double[] normalizedMins = new double[query.getDimensionCount()];
        final double[] normalizedMaxes = new double[query.getDimensionCount()];
        for (int d = 0; d < query.getDimensionCount(); d++) {
            normalizedMins[d] = dimensionDefs[d].normalize(queryMins[d]);
            normalizedMaxes[d] = dimensionDefs[d].normalize(queryMaxes[d]);
        }
        final byte[] minZorder = ZOrderUtils.convertToZorder(normalizedMins, maxcardinality, query.getDimensionCount());
        final byte[] maxZorder = ZOrderUtils.convertToZorder(normalizedMaxes, maxcardinality, query.getDimensionCount());
        BoundingBox box = new BoundingBox(queryMins[0], queryMins[1], queryMaxes[0], queryMaxes[1]);
        /************* Part 2 get commonprefix and offset *************/
        List<ByteArrayRange> ranges = new ArrayList<>(100);
        ArrayDeque<ByteArrayRange> remaining = new ArrayDeque<>(100);
        int commonBitsNum = ByteArrayUtils.commonBitsNum(minZorder, maxZorder);
        ByteArray commonPrefix = ByteArrayUtils.commonPrefix(minZorder, maxZorder);

        int offset = minZorder.length * ByteArray.BYTE_BITS_LENGTH - commonBitsNum;//24-commonBitsNum

        /************* Part 3 get ranges from top to bottom *************/
        ByteArrayRange quadrantRange = getSubQuadrantRange(commonPrefix, offset, 0L);
        if (box.contains(rangeToBoundingbox(quadrantRange))) {
            ranges.add(new ByteArrayRange(quadrantRange, true));
        } else if (box.intersects(rangeToBoundingbox(quadrantRange))) {
            remaining.add(new ByteArrayRange(quadrantRange, false));
        }
        remaining.add(ByteArrayRange.LevelTerminator);
        offset -= dimensionDefs.length;
        int level = 0;
        while (level < maxRecursive && !remaining.isEmpty() && offset >= 0) {
            ByteArrayRange next = remaining.poll();
            if (next.equals(ByteArrayRange.LevelTerminator)) {
                if (!remaining.isEmpty()) {
                    level++;
                    offset -= dimensionDefs.length;
                    remaining.add(ByteArrayRange.LevelTerminator);
                }
            } else {
                ByteArray prefix = next.getStart();
                long quadrant = 0L;
                while (quadrant < (1 << dimensionDefs.length)) {
                    ByteArrayRange subQuadrantRange = getSubQuadrantRange(prefix, offset, quadrant);
                    if (box.contains(rangeToBoundingbox(subQuadrantRange))) {
                        ranges.add(new ByteArrayRange(subQuadrantRange, true));
                    } else if (box.intersects(rangeToBoundingbox(subQuadrantRange))) {
                        remaining.add(new ByteArrayRange(subQuadrantRange, false));
                    }
                    quadrant += 1;
                }
            }
        }
        while (!remaining.isEmpty()) {
            ByteArrayRange minMax = remaining.poll();
            if (!minMax.equals(ByteArrayRange.LevelTerminator)) {
//				System.out.println(ByteArrayUtils.indexToBinaryString(getNewIndex(minMax.getStart(),24))+ " "+ByteArrayUtils.indexToBinaryString(getNewIndex(minMax.getEnd(),24)));
                ranges.add(new ByteArrayRange(minMax.getStart(), minMax.getEnd(), false, false));
            }
        }
//		return ranges;
        return (List<ByteArrayRange>) ByteArrayRange.mergeIntersections(ranges, ByteArrayRange.MergeOperation.UNION);//sort
    }

    public List<ByteArrayRange> decomposeRangeUtil2(
            final MultiDimensionalNumericData query,
            final int maxRecursive) {
        /************* Part 1 Normalize and getbounds *************/
        final double[] queryMins = query.getMinValuesPerDimension();//minLon minLat or minRow minCol
        final double[] queryMaxes = query.getMaxValuesPerDimension();//maxLon maxLat or maxRow maxCol
        final double[] normalizedMins = new double[query.getDimensionCount()];
        final double[] normalizedMaxes = new double[query.getDimensionCount()];
        for (int d = 0; d < query.getDimensionCount(); d++) {
            normalizedMins[d] = 0;
            normalizedMaxes[d] = 1;
        }
        final byte[] minZorder = ZOrderUtils.convertToZorder(normalizedMins, maxcardinality, query.getDimensionCount());
        final byte[] maxZorder = ZOrderUtils.convertToZorder(normalizedMaxes, maxcardinality, query.getDimensionCount());

        BoundingBox box = new BoundingBox(queryMins[0], queryMins[1], queryMaxes[0], queryMaxes[1]);
        /************* Part 2 get commonprefix and offset *************/
        List<ByteArrayRange> ranges = new ArrayList<>(100);
        ArrayDeque<ByteArrayRange> remaining = new ArrayDeque<>(100);

        ByteArrayRange rootRange = new ByteArrayRange(new ByteArray(minZorder), new ByteArray(maxZorder));
        int commonBitsNum = ByteArrayUtils.commonBitsNum(minZorder, maxZorder);
        int offset = minZorder.length * ByteArray.BYTE_BITS_LENGTH - commonBitsNum;

        /************* Part 3 get ranges from top to bottom *************/

        int size = 0;
        if (box.contains(rangeToBoundingbox(rootRange))) {
            ranges.add(new ByteArrayRange(rootRange, true));
        } else if (box.intersects(rangeToBoundingbox(rootRange))) {
            remaining.add(new ByteArrayRange(rootRange, false));
            size++;
        }
        offset -= dimensionDefs.length;
        int maxLevel = GeoHashSizeTable.numberOfBitsForOverlappingGeoHash(box) / 2 + maxRecursive;
        for (int i = 1; i <= maxLevel && offset >= 0; i++) {
            int levelSize = 0;
            for (int j = 0; j < size; j++) {
                ByteArrayRange next = remaining.poll();
                ByteArray prefix = next.getStart();
                long quadrant = 0L;
                while (quadrant < (1 << dimensionDefs.length)) {
                    ByteArrayRange subQuadrantRange = getSubQuadrantRange(prefix, offset, quadrant);
                    if (box.contains(rangeToBoundingbox(subQuadrantRange))) {
                        ranges.add(new ByteArrayRange(subQuadrantRange, true));
                    } else if (box.intersects(rangeToBoundingbox(subQuadrantRange))) {
                        remaining.add(new ByteArrayRange(subQuadrantRange, false));
                        levelSize++;
                    }
                    quadrant += 1;
                }
            }
            offset -= dimensionDefs.length;
            size = levelSize;
        }
        while (!remaining.isEmpty()) {
            ranges.add(remaining.poll());
        }

//		return ranges;
        return (List<ByteArrayRange>) ByteArrayRange.mergeIntersections(ranges, ByteArrayRange.MergeOperation.UNION);//sort
    }

    public String indexToString(ByteArray index) {
        byte[] bytes = index.getBytes();
        byte[] longBytes = new byte[8];
        int offset = ByteArray.BYTE_BITS_LENGTH - bytes.length;
        System.arraycopy(bytes, 0, longBytes, offset, bytes.length);

        return "" + ByteArrayUtils.byteArrayToLong(longBytes);
    }

    @Override
    public BoundingBox rangeToBoundingbox(ByteArrayRange range) {
        ByteArray rangeStart = range.getStart();
        ByteArray rangeEnd = range.getEnd();
        NumericRange[] data1 = (NumericRange[]) getRanges(rangeStart).getDataPerDimension();
        NumericRange[] data2 = (NumericRange[]) getRanges(rangeEnd).getDataPerDimension();
        return new BoundingBox(data1[0].getMin(), data1[1].getMin(), data2[0].getMax(), data2[1].getMax());
    }

    @Override
    public byte[] toBinary() {
        final List<byte[]> dimensionDefBinaries = new ArrayList<byte[]>(dimensionDefs.length);
        int bufferLength = 4;
        for (final SFCDimensionDefinition sfcDimension : dimensionDefs) {
            final byte[] sfcDimensionBinary = PersistenceUtils.toBinary(sfcDimension);
            bufferLength += (sfcDimensionBinary.length + 4);
            dimensionDefBinaries.add(sfcDimensionBinary);
        }
        final ByteBuffer buf = ByteBuffer.allocate(bufferLength);
        buf.putInt(dimensionDefs.length);
        for (final byte[] dimensionDefBinary : dimensionDefBinaries) {
            buf.putInt(dimensionDefBinary.length);
            buf.put(dimensionDefBinary);
        }
        return buf.array();
    }

    @Override
    public void fromBinary(final byte[] bytes) {
        final ByteBuffer buf = ByteBuffer.wrap(bytes);
        final int numDimensions = buf.getInt();
        dimensionDefs = new SFCDimensionDefinition[numDimensions];
        for (int i = 0; i < numDimensions; i++) {
            final byte[] dim = new byte[buf.getInt()];
            buf.get(dim);
            dimensionDefs[i] = (SFCDimensionDefinition) PersistenceUtils.fromBinary(dim);
        }
        init(dimensionDefs);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        final String className = getClass().getName();
        result = (prime * result) + ((className == null) ? 0 : className.hashCode());
        result = (prime * result) + Arrays.hashCode(dimensionDefs);
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ZOrderSFC other = (ZOrderSFC) obj;

        if (!Arrays.equals(dimensionDefs, other.dimensionDefs)) {
            return false;
        }
        return true;
    }

    @Override
    public long[] normalizeRange(
            final double minValue,
            final double maxValue,
            final int d) {
        long normalizeMin = (long) (dimensionDefs[d].normalize(minValue) * binsPerDimension[d]);
        long normalizeMax = (long) (dimensionDefs[d].normalize(maxValue) * binsPerDimension[d]);
        if (normalizeMax == binsPerDimension[d]) {
            normalizeMax--;
        }
        return new long[]{normalizeMin, normalizeMax};
    }

    public SFCDimensionDefinition[] getSFCDimensionDefinition() {
        return this.dimensionDefs;
    }

    private static class QueryCacheKey {
        private final double[] minsPerDimension;
        private final double[] maxesPerDimension;
        private final int maxRecursive;

        public QueryCacheKey(
                final double[] minsPerDimension,
                final double[] maxesPerDimension,
                final int maxRecursive) {
            this.minsPerDimension = minsPerDimension;
            this.maxesPerDimension = maxesPerDimension;
            this.maxRecursive = maxRecursive;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = (prime * result) + maxRecursive;
            result = (prime * result) + Arrays.hashCode(maxesPerDimension);
            result = (prime * result) + Arrays.hashCode(minsPerDimension);
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final QueryCacheKey other = (QueryCacheKey) obj;
            if (maxRecursive != other.maxRecursive) {
                return false;
            }
            if (!Arrays.equals(maxesPerDimension, other.maxesPerDimension)) {
                return false;
            }
            if (!Arrays.equals(minsPerDimension, other.minsPerDimension)) {
                return false;
            }
            return true;
        }
    }
}
