package index.coding;

import geoobject.BoundingBox;
import geoobject.Polygon;
import geoobject.STPoint;
import index.coding.concatenate.ConcatenateCoding;
import index.coding.spatial.SpatialCoding;
import index.data.ByteArray;
import index.data.ByteArrayRange;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

/**
 * Coding interface is the basic interface of all coding strategies
 *
 * @author Shendannan
 * Create on 2019-06-06.
 * @see SpatialCoding
 */
public interface Coding {
    /**
     * Encode lat lon time to a spatial-temporal index,
     * i.e. RowKey of EncodeTable
     *
     * @param point lonlattime
     * @return Spatial-temporal index(RowKey)
     */
    ByteArray getIndex(STPoint point);

    /**
     * Invert spatial-temporal code (i.e. RowKey) to a spatial-temporal point
     *
     * @param index index of STPoint
     * @return a spatial-temporal point
     */
    STPoint getPoint(ByteArray index) throws ParseException;

    /**
     * Invert spatial-temporal code (i.e. RowKey) to a spatial-temporal boundingbox.
     *
     * @param index index of STPoint
     * @return a spatial-temporal boundingbox
     */
    BoundingBox getBoundingBox(ByteArray index) throws ParseException;

    /**
     * Get RowKey pairs for scan operation, based on MultiDimensionalNumericData(bounding box) and
     * temporal range.A pair of RowKey is the startKey and endKey of a single scan.
     *
     * @param querybox     query condition
     * @param maxRecursive the max recursive times
     * @return RowKey pairs
     */
    List<ByteArrayRange> decomposeRange(BoundingBox querybox, int maxRecursive);

    /**
     * Get RowKey pairs for scan operation, based on MultiDimensionalNumericData(bounding box) and
     * temporal range.A pair of RowKey is the startKey and endKey of a single scan.
     *
     * @param queryPolygon query condition
     * @param maxRecursive the max recursive times
     * @return RowKey pairs
     */
    List<ByteArrayRange> decomposeRange(Polygon queryPolygon, int maxRecursive);

    /**
     * used for KNN query.
     *
     * @param spatialRanges the spatial ranges decomposed by SpatialCoding interface.
     * @param startDate     query condition
     * @param endDate       query condition
     * @return RowKey pairs
     */
    List<ByteArrayRange> decomposeRange(List<ByteArrayRange> spatialRanges,
                                        Date startDate,
                                        Date endDate);

    /**
     * Get RowKey pairs for scan operation, based on MultiDimensionalNumericData(bounding box) and
     * temporal range.A pair of RowKey is the startKey and endKey of a single scan.This function
     * decompose ranges till the max layer that storage in HBase.
     *
     * @param querybox query condition
     * @return RowKey pairs
     */
    List<ByteArrayRange> decomposeRangeFully(BoundingBox querybox);

    /**
     * transfer index to UTF-8 string.
     *
     * @param index
     * @return coding string
     */
    String indexToString(ByteArray index);

    /**
     * transfer UTF-8 string to index.
     *
     * @param indexStr coding string
     * @return coding index
     */
    ByteArray stringToIndex(String indexStr);

    /**
     * get the precision of spatial coding.
     */
    int getPrecision();

    /**
     * used for KNN query
     * @param center the center point for KNN
     * @param radius the max search radius
     * @return List
     */
    List<ByteArrayRange> decomposeRangeByCircle(STPoint center, double radius) ;

    List<STPoint> getBBoxByCircle(STPoint center, double radius);

    SpatialCoding getSpatialCoding();

    ConcatenateCoding getConcatenateCoding() throws Exception;

    enum SpatialType {
        Z2("Z2"),
        Geohash("GeoHash");
        private String name;
        SpatialType(String name) {
            this.name = name;
        }
    }

    enum ConcatenateType {
        S_T("S_T"),
        T_S("T_S");
        private String name;
        ConcatenateType(String name) {
            this.name = name;
        }
    }

    enum IndexType {
        OnlySpatial,
        STConcatenate
    }

    IndexType getIndexType();
}


