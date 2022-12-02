package index.coding;

import constant.CommonConstants;
import index.coding.concatenate.STCoding;
import index.coding.concatenate.TSCoding;
import index.coding.concatenate.time.TimeCoding;
import index.coding.spatial.SpatialCoding;
import index.coding.spatial.Z2Coding;
import index.coding.spatial.geohash.GeohashCoding;

/**
 * Factory used to generate an instance of a known Coding type
 *
 * @author Shendannan
 * Create on 2019-06-06.
 */

//todo 需要包装成一个可以生产 s t st ts z2 geohash 编码等各种方面的工厂类
public class CodingFactory1 {

    /*private static final Coding coding = new TSCoding(Coding.SpatialType.Z2,
            62, "yyMMddHH", "H");*/


    private static int maxLevel = CommonConstants.DEFAULT_MAX_LEVEL;
    private static String timePattern = CommonConstants.TIME_CODING_PATTERN;

    public static Coding getCoding(String type, int level) {
        Coding coding = null;
        switch (type) {
            case "11":
                coding = tsIndexCoding.get();
        }
        return coding;
    }


    //每个线程的 Coding 实例
    private static final ThreadLocal<Coding> tsIndexCoding = ThreadLocal.withInitial(() -> new TSCoding(Coding.SpatialType.Z2, maxLevel * 2, "yyMMddHHmmss", "s"));

    //todo 这个level需要调整，需要采取用户指定的层级
    private static final ThreadLocal<Coding> spatialIndexCoding = new ThreadLocal<Coding>() {
        @Override
        protected Coding initialValue() {
            return new Z2Coding(maxLevel * 2);
        }
    };

    private static final ThreadLocal<Coding> stIndexCoding = new ThreadLocal<Coding>() {
        @Override
        protected Coding initialValue() {
            return new STCoding(Coding.SpatialType.Z2, maxLevel * 2, timePattern, "s");
        }
    };

    //每个线程的 Coding 实例
    private static final ThreadLocal<TimeCoding> timeIndexCoding = new ThreadLocal<TimeCoding>() {
        @Override
        protected TimeCoding initialValue() {
            return new TimeCoding(timePattern, "s");
        }
    };

    /**
     * 要考虑并发性！！！！不能返回coding单例,TimeCoding不具备并发安全性
     *
     * @return
     */
    public static Coding getTSPointIndexCoding() {
        return tsIndexCoding.get();
    }

    public static Coding getSpatialIndexCoding() {
        return spatialIndexCoding.get();
    }

    public static Coding getSTIndexCoding() {
        return stIndexCoding.get();
    }

    public static Coding getSTIndexCoding(int maxLevel) {
        CodingFactory1.maxLevel = maxLevel;
        return stIndexCoding.get();
    }

    public static TimeCoding getTimeIndexCoding() {
        return timeIndexCoding.get();
    }

    public static TimeCoding getTimeIndexCoding(String timePattern) {
        CodingFactory1.timePattern = timePattern;
        return timeIndexCoding.get();
    }


    /***
     * Generates a spatial coding based on the dimensions definition and the space filling curve type
     *
     * @param precision Meshing depth of the specific SFC
     * @param codingType specifies the type (Hilbert, ZOrder) of space filling curve to generate
     * @return a space filling curve instance generated based on the supplied parameters
     */
    public static SpatialCoding createSpatialCoding(
            final Coding.SpatialType codingType,
            final int precision) {
        switch (codingType) {
            case Z2:
                return new Z2Coding(precision);
            case Geohash:
                return new GeohashCoding(precision);
            default:
                return null;
        }
    }
}