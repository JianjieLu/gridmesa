package index.coding.spatial;

import geoobject.STPoint;
import index.coding.Coding;
import index.data.ByteArray;
import index.data.ByteArrayRange;

import java.io.Serializable;
import java.util.List;

/**
 * SpatialCoding interface has index，invert and getRanges function
 *
 * @author Shendannan
 * Create on 2019-06-06.
 */
public interface SpatialCoding extends Coding, Serializable {
    IndexType indexType = IndexType.OnlySpatial;

    SpatialType getSpatialType();

    ByteArray getFather(ByteArray index);

    /**
     * Get eight neighbours of given cell index.
     * Warning: If return type is byte[], then Set.contain() can't judge whether an element is in a Set, String can.
     *
     * @param index
     * @return
     */
    List<ByteArray> getNeighbours(ByteArray index);

    /**
     * Get the children boxes of given cell index.
     *
     * @param index
     * @return
     */
    List<ByteArray> getChildren(ByteArray index);

    ByteArrayRange getChildrenRange(ByteArray index, int n);

    /**
     * used for KNN query
     *
     * @param center the center point for KNN
     * @param radius the max search radius
     * @return List
     */
    List<ByteArrayRange> decomposeRangeByCircle(STPoint center, double radius);

    /**
     * used for KNN query
     *
     * @param center the center point for KNN
     * @param radius the max search radius
     * @return BoundingBox
     */
    List<STPoint> getBBoxByCircle(STPoint center, double radius);

    /**
     * used for secondary index filter.
     *
     * @param range range
     * @return RowKey pairs
     */
    List<ByteArray> getRowKey(ByteArrayRange range);

    /**
     * get the precision of spatial coding.
     */
    int getPrecision();
}
