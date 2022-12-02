package index.coding.spatial;


import geoobject.BoundingBox;
import geoobject.Polygon;
import geoobject.STPoint;
import index.coding.concatenate.ConcatenateCoding;
import index.data.BasicNumericDataset;
import index.data.ByteArray;
import index.data.ByteArrayRange;
import index.data.NumericRange;
import index.dimension.BasicDimensionDefinition;
import index.sfc.SFCDimensionDefinition;
import index.sfc.zorder.ZOrderSFC;
import index.util.ByteArrayUtils;
import index.util.VincentyGeodesy;
import java.io.Serializable;
import java.util.*;


/**
 * Z2Coding implements SpatialCoding, use ZOrderSFC to convertToZorder
 *
 * @author Shendannan
 * Create on 2020-03-26.
 */
public class Z2Coding implements SpatialCoding, Serializable {
    private static final SpatialType mode = SpatialType.Z2;
    private final ZOrderSFC zOrderSFC;
    private final int precision;//当前划分次数

    public Z2Coding(int precision) {
        this.precision = precision;
        SFCDimensionDefinition[] dimensions = {
                new SFCDimensionDefinition(new BasicDimensionDefinition(-180.0, 180.0),
                        precision / 2),
                new SFCDimensionDefinition(new BasicDimensionDefinition(-90.0, 90.0),
                        precision / 2)
        };
        zOrderSFC = new ZOrderSFC(dimensions);
    }

    public Z2Coding(int precision, int min_precision, int max_precision) {
        this.precision = precision;
        SFCDimensionDefinition[] dimensions = {
                new SFCDimensionDefinition(new BasicDimensionDefinition(-180.0, 180.0),
                        precision / 2),
                new SFCDimensionDefinition(new BasicDimensionDefinition(-90.0, 90.0),
                        precision / 2)
        };
        zOrderSFC = new ZOrderSFC(dimensions);
    }

    @Override
    public SpatialType getSpatialType() {
        return mode;
    }

    @Override
    public int getPrecision() {
        return precision;
    }

    @Override
    public SpatialCoding getSpatialCoding() {
        return this;
    }

    @Override
    public ConcatenateCoding getConcatenateCoding() throws Exception {
        throw new Exception("This coding is not ConcatenateCoding.Try use getSpatialCoding instead.");
    }

    @Override
    public IndexType getIndexType() {
        return indexType;
    }

    @Override
    public ByteArray getIndex(STPoint point) {
        double[] lngLat = new double[]{point.getLongitude(), point.getLatitude()};
        return zOrderSFC.getId(lngLat);
    }

    @Override
    public STPoint getPoint(ByteArray index) {
        BasicNumericDataset ranges = (BasicNumericDataset) zOrderSFC.getRanges(index);
        NumericRange[] data = (NumericRange[]) ranges.getDataPerDimension();
        return new STPoint(data[0].getCentroid(), data[1].getCentroid());
    }

    @Override
    public BoundingBox getBoundingBox(ByteArray index) {
        // todo: 必须根据level初始化z2coding，怎么直接根据index获取bbox，查询时需要；
        BasicNumericDataset ranges = (BasicNumericDataset) zOrderSFC.getRanges(index);
        NumericRange[] data = (NumericRange[]) ranges.getDataPerDimension();
        return new BoundingBox(data[0].getMin(), data[1].getMin(),
                data[0].getMax(), data[1].getMax());
    }

    @Override
    public List<ByteArrayRange> decomposeRange(BoundingBox querybox, int maxRecursive) {
        NumericRange[] range = new NumericRange[2];
        range[0] = new NumericRange(querybox.getMinLng(), querybox.getMaxLng());
        range[1] = new NumericRange(querybox.getMinLat(), querybox.getMaxLat());
        BasicNumericDataset query = new BasicNumericDataset(range);
        return zOrderSFC.decomposeRange(query, maxRecursive);
    }

    @Override
    public List<ByteArrayRange> decomposeRange(Polygon queryPolygon, int maxRecursive) {
        return zOrderSFC.decomposeRange(queryPolygon, maxRecursive);
    }

    @Override
    public List<ByteArrayRange> decomposeRange(List<ByteArrayRange> spatialRanges,
                                               Date startDate, Date endDate) {
        throw new UnsupportedOperationException();
    }

    @Override
    //TODO：待实现
    public List<ByteArray> getRowKey(ByteArrayRange range) {
        List<ByteArray> spatialRange = new ArrayList<>();
//        ByteArray next = range.getStart();
//        while(Integer.parseInt(indexToBinaryString(next), 2)<=
//                Integer.parseInt(indexToBinaryString(range.getEnd()),2)){
//            spatialRange.add(next);
//            next = ByteArrayUtils.getNext(next,max_precision,max_bits);
//        }
////        System.out.println("拆分之后数量："+spatialRange.size());
////        for (int i = 0;i<spatialRange.size();i++){
////            System.out.println(i + " " + indexToBinaryString(spatialRange.get(i)));
////        }
        return spatialRange;
    }

    @Override
    public List<ByteArrayRange> decomposeRangeFully(BoundingBox querybox) {
        System.out.println("precision:"+precision);
        // 这个地方不太对吧，precision应该不是maxRecursive？
        return decomposeRange(querybox, precision);
    }

    @Override
    public List<ByteArrayRange> decomposeRangeByCircle(STPoint center, double radius) {
        STPoint upperRight = VincentyGeodesy.moveInDirection(center, 45,
                Math.sqrt(2) * radius);
        STPoint bottomLeft = VincentyGeodesy.moveInDirection(center, 225,
                Math.sqrt(2) * radius);
        BoundingBox bbox = new BoundingBox(bottomLeft, upperRight);

        return decomposeRangeFully(bbox);
    }

    @Override
    public List<STPoint> getBBoxByCircle(STPoint center, double radius) {

        List<STPoint> bbox = new ArrayList<>();
        STPoint upperRight = VincentyGeodesy.moveInDirection(center, 45,
                Math.sqrt(2) * radius);
        STPoint bottomLeft = VincentyGeodesy.moveInDirection(center, 225,
                Math.sqrt(2) * radius);

        bbox.add(bottomLeft);
        bbox.add(upperRight);
//        BoundingBox bbox = new BoundingBox(bottomLeft, upperRight);
        return bbox;
    }

    @Override
    public ByteArray getFather(ByteArray index) {
        BitSet indexBitSet = BitSet.valueOf(ByteArrayUtils.swapEndianFormat(index.getBytes()));

        int usedBytes = index.getBytes().length;
        int bitOffset = usedBytes * ByteArray.BYTE_BITS_LENGTH - precision;
        int originBitLen = usedBytes * ByteArray.BYTE_BITS_LENGTH;

        int needBytes = (int) Math.ceil((precision - 2) / 8.0);
        int newBitOffset = needBytes * ByteArray.BYTE_BITS_LENGTH - (precision - 2);
        int newBitLen = needBytes * ByteArray.BYTE_BITS_LENGTH;

        BitSet fatherBitSet = new BitSet(newBitLen);
        for (int i = bitOffset; i < originBitLen - 2; ++i) {
            fatherBitSet.set(newBitOffset++, indexBitSet.get(i));
        }

        byte[] littleEndianBytes = fatherBitSet.toByteArray();

        return new ByteArray(ByteArrayUtils.swapEndianFormat(littleEndianBytes));
    }

    @Override
    public ByteArrayRange getChildrenRange(ByteArray index, int n) {
        if (n < 0) {
            throw new IllegalArgumentException("Children level must lager than 0");
        }
        int usedBytes = index.getBytes().length;
        int bitLen = usedBytes * ByteArray.BYTE_BITS_LENGTH;
        int bitOffset = bitLen - precision;

        int newPrecision = precision + 2 * n;
        int needBytes = (int) Math.ceil(newPrecision / 8.0);
        int newBitLen = needBytes * ByteArray.BYTE_BITS_LENGTH;
        int newBitOffset = newBitLen - newPrecision;

        BitSet indexBitSet = BitSet.valueOf(ByteArrayUtils.swapEndianFormat(index.getBytes()));

        BitSet startBitSet = new BitSet(newBitLen);
        BitSet endBitSet = new BitSet(newBitLen);
        for (int i = bitOffset; i < bitLen; i++) {
            startBitSet.set(newBitOffset, indexBitSet.get(i));
            endBitSet.set(newBitOffset, indexBitSet.get(i));
            newBitOffset++;
        }

        for (int i = 0; i < 2 * n; i++) {
            startBitSet.set(newBitOffset, false);
            endBitSet.set(newBitOffset, true);
            newBitOffset++;
        }

        byte[] startVal = ByteArrayUtils.swapEndianFormat(startBitSet.toByteArray());
        byte[] endVal = ByteArrayUtils.swapEndianFormat(endBitSet.toByteArray());
        if (startVal.length < needBytes) {
            startVal = Arrays.copyOf(startVal, needBytes);
        }
        if (endVal.length < needBytes) {
            endVal = Arrays.copyOf(endVal, needBytes);
        }

        ByteArray rangeStart = new ByteArray(startVal);
        ByteArray rangeEnd = new ByteArray(endVal);
        return new ByteArrayRange(rangeStart, rangeEnd);
    }

    @Override
    public List<ByteArray> getChildren(ByteArray index) {
        List<ByteArray> children = new ArrayList<>();

        byte[] indexByte = index.getBytes();
        BitSet bitSet = BitSet.valueOf(ByteArrayUtils.swapEndianFormat(indexByte));

        int usedBytes = index.getBytes().length;
        int bitOffset = usedBytes * ByteArray.BYTE_BITS_LENGTH - precision;

        BitSet childBitset;
        int newBitsetLength = 0, newBitOffset = 0;
        if (bitOffset < 2) {
            newBitsetLength = (usedBytes + 1) * ByteArray.BYTE_BITS_LENGTH;
            newBitOffset = ByteArray.BYTE_BITS_LENGTH - 2 + bitOffset;
            childBitset = new BitSet(newBitsetLength);

        } else {
            newBitsetLength = usedBytes * ByteArray.BYTE_BITS_LENGTH;
            newBitOffset = bitOffset - 2;
            childBitset = new BitSet(newBitsetLength);
        }

        for (int i = 0; i < precision; i++) {
            childBitset.set(newBitOffset + i, bitSet.get(bitOffset + i));
        }

        boolean[] flag = {false, true};
        for (boolean i : flag) {
            for (boolean j : flag) {
                childBitset.set(newBitOffset + precision, i);
                childBitset.set(newBitOffset + precision + 1, j);
                byte[] littleEndianBytes = childBitset.toByteArray();
                byte[] childBytes = ByteArrayUtils.swapEndianFormat(littleEndianBytes);
                //BitSet在所有bit都为0时会返回空数组
                if (childBytes.length < (newBitsetLength + 7) / 8) {
                    childBytes = Arrays.copyOf(childBytes, (newBitsetLength + 7) / 8);
                }
                children.add(new ByteArray(childBytes));
            }
        }
        return children;
    }

    @Override
    public List<ByteArray> getNeighbours(ByteArray index) {
        ByteArray[] dimBins = ByteArrayUtils.splitDimension(index, precision, 2);

        // get neighbours from north with clock-wise
        List<ByteArray> neighbours = new ArrayList<>(8);

        ByteArray latBin = dimBins[1];
        ByteArray lngBin = dimBins[0];

        ByteArray northLatBin = ByteArrayUtils.increment(latBin);
        ByteArray sourthLatBin = ByteArrayUtils.decrement(latBin);
        ByteArray eastLngBin = ByteArrayUtils.increment(lngBin);
        ByteArray westLngBin = ByteArrayUtils.decrement(lngBin);

        neighbours.add(ByteArrayUtils.combineDimension(lngBin, northLatBin, precision));
        neighbours.add(ByteArrayUtils.combineDimension(eastLngBin, northLatBin, precision));
        neighbours.add(ByteArrayUtils.combineDimension(eastLngBin, latBin, precision));
        neighbours.add(ByteArrayUtils.combineDimension(eastLngBin, sourthLatBin, precision));
        neighbours.add(ByteArrayUtils.combineDimension(lngBin, sourthLatBin, precision));
        neighbours.add(ByteArrayUtils.combineDimension(westLngBin, sourthLatBin, precision));
        neighbours.add(ByteArrayUtils.combineDimension(westLngBin, latBin, precision));
        neighbours.add(ByteArrayUtils.combineDimension(westLngBin, northLatBin, precision));

        return neighbours;
        //        return zOrderSFC.getNeighbours(index);
    }

    @Override
    public String indexToString(ByteArray index) {
        // todo: 只能转为long类型的索引，怎么转成二进制的？
        byte[] bytes = index.getBytes();
        byte[] longBytes = new byte[8];
        int offset = ByteArray.BYTE_BITS_LENGTH - bytes.length;
        System.arraycopy(bytes, 0, longBytes, offset, bytes.length);
        return "" + ByteArrayUtils.byteArrayToLong(longBytes);
    }

    public static ByteArray binaryStringToIndex(String indexStr) {
        int indexPrecision = indexStr.length();
        int len =
                (int) Math.ceil((double) indexPrecision / ByteArray.BYTE_BITS_LENGTH);
        byte[] bytes = new byte[len];
        byte[] longBytes = ByteArrayUtils.longToByteArray(Long.parseLong(indexStr));
        System.arraycopy(longBytes, ByteArray.BYTE_BITS_LENGTH - len, bytes,
                0, len);
        return new ByteArray(bytes);
    }

    @Override
    public ByteArray stringToIndex(String indexStr) {
        // todo: 只能转为long类型的索引，怎么转成二进制的？
        int len =
                (int) Math.ceil((double) precision / ByteArray.BYTE_BITS_LENGTH);
        byte[] bytes = new byte[len];
        byte[] longBytes = ByteArrayUtils.longToByteArray(Long.parseLong(indexStr));
        System.arraycopy(longBytes, ByteArray.BYTE_BITS_LENGTH - len, bytes,
                0, len);
        return new ByteArray(bytes);
    }

    public ByteArray stringToIndex(String indexStr, int precision) {
        int len =
                (int) Math.ceil((double) precision / ByteArray.BYTE_BITS_LENGTH);
        byte[] bytes = new byte[len];
        byte[] longBytes = ByteArrayUtils.longToByteArray(Long.parseLong(indexStr));
        System.arraycopy(longBytes, ByteArray.BYTE_BITS_LENGTH - len, bytes,
                0, len);
        return new ByteArray(bytes);
    }
}