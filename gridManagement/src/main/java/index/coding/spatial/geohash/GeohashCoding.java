package index.coding.spatial.geohash;


import geoobject.BoundingBox;
import geoobject.Polygon;
import geoobject.STPoint;
import index.coding.concatenate.ConcatenateCoding;
import index.coding.spatial.SpatialCoding;
import index.data.ByteArray;
import index.data.ByteArrayRange;
import index.util.VincentyGeodesy;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * GeohashCoding implements SpatialCoding, use Geohash to convertToZorder
 *
 * @author Shendannan
 * Create on 2019-06-06.
 */

public class GeohashCoding implements SpatialCoding {
    private static final SpatialType mode = SpatialType.Geohash;
    private int precision;

    public GeohashCoding(int precision) {
        super();
        this.precision = precision;
    }

    /**
     * get the whole 1 box
     *
     * @param boundingBox
     * @return
     */
    public static GeoHash getSearchHash(BoundingBox boundingBox) {
        // find 1 geohash to contain bounding box
        List<GeoHash> searchHashes = getSearchHashes(boundingBox);
        if (searchHashes.size() == 1) {
            return searchHashes.get(0);
        } else {
            String[] str = new String[searchHashes.size()];
            for (int i = 0; i < searchHashes.size(); i++) {
                str[i] = searchHashes.get(i).toBinaryString();
//                System.out.println(str[i]);
            }
            String s = index.util.StringUtils.longestCommonPrefix(str);
            return GeoHash.fromBinaryString(s);
        }
    }

    /**
     * get 1/2/4 boxes
     *
     * @param boundingBox
     * @return
     */
    public static List<GeoHash> getSearchHashes(BoundingBox boundingBox) {
        // find 1,2 or at most 4 geohash to contain bounding box
        List<GeoHash> searchHashes = new ArrayList<>();

        int fittingBits = GeoHashSizeTable.numberOfBitsForOverlappingGeoHash(boundingBox);
        STPoint center = boundingBox.getCenterPoint();
        GeoHash centerHash = GeoHash.withBitPrecision(center.getLongitude(), center.getLatitude(), fittingBits);

        if (centerHash.contains(boundingBox.getUpperLeft()) && centerHash.contains(boundingBox.getLowerRight())) {
            searchHashes.add(centerHash);
        } else {
            searchHashes.add(centerHash);
            for (GeoHash adjacent : centerHash.getAdjacent()) {
                BoundingBox adjacentBox = adjacent.getBoundingBox();
                if (adjacentBox.intersects(boundingBox) && !searchHashes.contains(adjacent)) {
                    searchHashes.add(adjacent);
                }
            }
        }
        return searchHashes;
    }

    public static List<GeoHash> toFittingGeohashes(List<GeoHash> searchHashes) {
        // maybe the fittingBits is not multiple of 5  , so we need to go further
        ArrayList<GeoHash> fittingGeohashes = new ArrayList<>();
        for (GeoHash hash : searchHashes) {
            fittingGeohashes.addAll(toFittingGeohashes(hash));
        }
        return fittingGeohashes;
    }

    public static List<GeoHash> toFittingGeohashes(GeoHash hash) {
        // maybe the fittingBits is not multiple of 5  , so we need to go further
        ArrayList<GeoHash> fittingGeohashes = new ArrayList<GeoHash>();

        int bitNum = hash.getSignificantBits();
        int paddingNum = (GeoHash.BASE32_BITS - bitNum % 5);
        String startHashBinaryStr = new StringBuilder().append(hash.toBinaryString())
                .append(StringUtils.repeat("0", paddingNum))
                .toString();
        GeoHash startHash = GeoHash.fromBinaryString(startHashBinaryStr);
        String endHashBinaryStr = new StringBuilder().append(hash.toBinaryString())
                .append(StringUtils.repeat("1", paddingNum))
                .toString();
        GeoHash endHash = GeoHash.fromBinaryString(endHashBinaryStr);

        GeoHash curHash = startHash;
        while (curHash.compareTo(endHash) <= 0) {
            fittingGeohashes.add(curHash);
            curHash = curHash.next();
        }

        return fittingGeohashes;
    }

    private static void findRectGeohash(GeoHash node, BoundingBox boundingBox, int maxSearchLevel, List<GeoHash> rectHash) {
        if (node.getSignificantBits() >= maxSearchLevel) {
            node.setSelected(true);
            node.setContained(false);
            return;
        }
        node.setChild();

        if (node.getSignificantBits() <= maxSearchLevel && boundingBox.contains(node.getleftChild().getBoundingBox())) {
            node.getleftChild().setSelected(true);
            node.getleftChild().setContained(true);
        } else if (boundingBox.intersects(node.getleftChild().getBoundingBox())) {
            findRectGeohash(node.getleftChild(), boundingBox, maxSearchLevel, rectHash);
        }

        if (node.getSignificantBits() <= maxSearchLevel && boundingBox.contains(node.getrightChild().getBoundingBox())) {
            node.getrightChild().setSelected(true);
            node.getrightChild().setContained(true);
        } else if (boundingBox.intersects(node.getrightChild().getBoundingBox())) {
            findRectGeohash(node.getrightChild(), boundingBox, maxSearchLevel, rectHash);
        }

        if (node.getleftChild().getSelected() && node.getrightChild().getSelected()) {
            node.setSelected(true);
            if (node.getleftChild().getContained() && node.getrightChild().getContained()) {
                node.setContained(true);
            }
        } else {
            if (node.getleftChild().getSelected()) {
                rectHash.add(node.getleftChild());
            }
            if (node.getrightChild().getSelected()) {
                rectHash.add(node.getrightChild());
            }
        }
    }

    private static void findRectGeohash1(GeoHash node, BoundingBox boundingBox, int maxSearchLevel, List<GeoHash> rectHash) {
        if (node.getSignificantBits() >= maxSearchLevel) {
            rectHash.add(node);
            return;
        }
        node.setChild();
        if (boundingBox.intersects(node.getleftChild().getBoundingBox()) && boundingBox.intersects(node.getrightChild().getBoundingBox())) {
            rectHash.add(node);
        } else if (boundingBox.intersects(node.getleftChild().getBoundingBox())) {
            findRectGeohash1(node.getleftChild(), boundingBox, maxSearchLevel, rectHash);
        } else if (boundingBox.intersects(node.getrightChild().getBoundingBox())) {
            findRectGeohash1(node.getrightChild(), boundingBox, maxSearchLevel, rectHash);
        }
    }

    //这个方法是自顶向下分割多边形，会将多边形内部连续的geohash合并，用于金安的geohash实验
    public static void findRectGeohash
    (GeoHash node, Polygon polygon, int maxSearchLevel, List<GeoHash> rectHash) {
        if (node.getSignificantBits() >= maxSearchLevel) {
            node.setSelected(true);
            node.setContained(false);
            return;
        }
        node.setChild();

        if (node.getSignificantBits() <= maxSearchLevel && polygon.contains(node.getleftChild().getBoundingBox())) {
            node.getleftChild().setSelected(true);
            node.getleftChild().setContained(true);
        } else if (polygon.intersects(node.getleftChild().getBoundingBox())) {
            findRectGeohash(node.getleftChild(), polygon, maxSearchLevel, rectHash);
        }

        if (node.getSignificantBits() <= maxSearchLevel && polygon.contains(node.getrightChild().getBoundingBox())) {
            node.getrightChild().setSelected(true);
            node.getrightChild().setContained(true);
        } else if (polygon.intersects(node.getrightChild().getBoundingBox())) {
            findRectGeohash(node.getrightChild(), polygon, maxSearchLevel, rectHash);
        }

        if (node.getleftChild().getSelected() && node.getrightChild().getSelected()) {
            node.setSelected(true);
            if (node.getleftChild().getContained() && node.getrightChild().getContained()) {
                node.setContained(true);
            }
        } else {
            if (node.getleftChild().getSelected()) {
                rectHash.add(node.getleftChild());
            }
            if (node.getrightChild().getSelected()) {
                rectHash.add(node.getrightChild());
            }
        }
    }

    //这个方法是自顶向下分割多边形，直接分解到指定底层，不合并内部的geohash
    public static void findPolygonGeohash
    (GeoHash node, Polygon polygon, int maxSearchLevel, List<GeoHash> rectHash) {
        if (polygon.contains(node.getBoundingBox())
                && node.getSignificantBits() <= maxSearchLevel) {
            node.setContained(true);
            node.setSelected(true);
            return;
        } else if (node.getSignificantBits() >= maxSearchLevel) {
            node.setContained(false);
            node.setSelected(true);
            return;
        }
            node.setChild();

        if (polygon.intersects(node.getleftChild().getBoundingBox())) {
            findPolygonGeohash(node.getleftChild(), polygon, maxSearchLevel, rectHash);
        }

        if (polygon.intersects(node.getrightChild().getBoundingBox())) {
            findPolygonGeohash(node.getrightChild(), polygon, maxSearchLevel, rectHash);
        }

        if (node.getleftChild().getSelected()) {
            rectHash.add(node.getleftChild());
        }
        if (node.getrightChild().getSelected()) {
            rectHash.add(node.getrightChild());
        }
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
    public SpatialType getSpatialType() {
        return mode;
    }

    @Override
    public ByteArray getFather(ByteArray index) {
        String children = indexToString(index);
        String father = new StringBuilder().append(children).delete(children.length() - 1, children.length()).toString();
        return new ByteArray(father.getBytes());
    }

    @Override
    public ByteArrayRange getChildrenRange(ByteArray index, int n) {
        if (n < 0) {
            throw new IllegalArgumentException("Children level must lager than 0");
        }
        String father = indexToString(index);
        String rangeStart = new StringBuilder().append(father)
                .append(StringUtils.repeat("0", n))
                .toString();
        String rangeEnd = new StringBuilder().append(father)
                .append(StringUtils.repeat("z", n))
                .toString();
        return new ByteArrayRange(new ByteArray(rangeStart.getBytes()), new ByteArray(rangeEnd.getBytes()));
    }

    @Override
    public int getPrecision() {
        return precision;
    }

    @Override
    public ByteArray getIndex(STPoint point) {
        GeoHash hash = GeoHash.withBitPrecision(point.getLongitude(), point.getLatitude(), this.precision);
        return new ByteArray(hash.toBase32().getBytes());
    }

    @Override
    public STPoint getPoint(ByteArray index) {
        String indexStr = null;
        indexStr = new String(index.getBytes(), StandardCharsets.UTF_8);
        GeoHash hash = GeoHash.fromGeohashString(indexStr);
        return hash.getPoint();
    }

    @Override
    public BoundingBox getBoundingBox(ByteArray index) {
        GeoHash geoHashIndex = GeoHash.fromGeohashString(new String(index.getBytes()));
        STPoint minCoor = new STPoint(geoHashIndex.getBoundingBox().getMinLng(),
                geoHashIndex.getBoundingBox().getMinLat());
        STPoint maxCoor = new STPoint(geoHashIndex.getBoundingBox().getMaxLng(),
                geoHashIndex.getBoundingBox().getMaxLat());
        return new BoundingBox(minCoor, maxCoor);
    }

    @Override
    public String indexToString(ByteArray index) {
        return new String(index.getBytes(), StandardCharsets.UTF_8);
    }

    @Override
    public ByteArray stringToIndex(String indexStr) {
        return new ByteArray(indexStr.getBytes());
    }

    @Override
    public List<ByteArrayRange> decomposeRange(BoundingBox boundingBox, int maxRecursive) {
        List<GeoHash> rectHash = new ArrayList<>();
        int maxSearchLevel = getSearchHashes(boundingBox).get(0).getSignificantBits() + maxRecursive;
        if (maxSearchLevel > precision) {
            maxSearchLevel = precision;
        }
        int size = rectHash.size();
        for (GeoHash hash : getSearchHashes(boundingBox)) {
            findRectGeohash(hash, boundingBox, maxSearchLevel, rectHash);
            if (size == rectHash.size() && boundingBox.intersects(hash.getBoundingBox())) {
                rectHash.add(hash);
            }
            size = rectHash.size();
        }

        List<ByteArrayRange> rangeResult = new ArrayList<>();
        for (GeoHash geoHash : rectHash) {
            int significantBits = geoHash.getSignificantBits();
            int paddingCharNum = precision - significantBits;
            String rangeStart = GeoHash.fromBinaryString(geoHash.toBinaryString() +
                    StringUtils.repeat("0", paddingCharNum))
                    .toBase32();
            String rangeEnd = GeoHash.fromBinaryString(geoHash.toBinaryString() +
                    StringUtils.repeat("1", paddingCharNum))
                    .toBase32();
            rangeResult.add(new ByteArrayRange(new ByteArray(rangeStart.getBytes()), new ByteArray(rangeEnd.getBytes()),
                    false, geoHash.getContained()));
        }
        return rangeResult;
    }

    public List<GeoHash> decomposeRange1(BoundingBox boundingBox, int maxRecursive) {
        List<GeoHash> rectHash = new ArrayList<>();
        int maxSearchLevel = getSearchHashes(boundingBox).get(0).getSignificantBits() + maxRecursive;
        if (maxSearchLevel > precision) {
            maxSearchLevel = precision;
        }
        int size = rectHash.size();
        for (GeoHash hash : getSearchHashes(boundingBox)) {
            findRectGeohash(hash, boundingBox, maxSearchLevel, rectHash);
            if (size == rectHash.size() && boundingBox.intersects(hash.getBoundingBox())) {
                rectHash.add(hash);
            }
            size = rectHash.size();
        }
        return rectHash;
    }

    @Override
    public List<ByteArrayRange> decomposeRange(Polygon queryPolygon, int maxRecursive) {
        List<GeoHash> rectHash = new ArrayList<>();
        BoundingBox box = queryPolygon.getBoundingBox();
        int maxSearchLevel = getSearchHashes(box).get(0).getSignificantBits() + maxRecursive;
        if (maxSearchLevel > precision) {
            maxSearchLevel = precision;
        }
        int size = rectHash.size();
        for (GeoHash hash : getSearchHashes(box)) {
            findRectGeohash(hash, queryPolygon, maxSearchLevel, rectHash);
            if (size == rectHash.size() && box.intersects(hash.getBoundingBox())) {
                rectHash.add(hash);
            }
            size = rectHash.size();
        }

        List<ByteArrayRange> rangeResult = new ArrayList<>();
        for (GeoHash geoHash : rectHash) {
            int significantBits = geoHash.getSignificantBits();
            int paddingCharNum = precision - significantBits;
            String rangeStart = GeoHash.fromBinaryString(new StringBuilder().append(geoHash.toBinaryString())
                    .append(StringUtils.repeat("0", paddingCharNum))
                    .toString())
                    .toBase32();
            String rangeEnd = GeoHash.fromBinaryString(new StringBuilder().append(geoHash.toBinaryString())
                    .append(StringUtils.repeat("1", paddingCharNum))
                    .toString())
                    .toBase32();
            rangeResult.add(new ByteArrayRange(new ByteArray(rangeStart.getBytes()), new ByteArray(rangeEnd.getBytes()),
                    false, geoHash.getContained()));
        }
        return rangeResult;
    }

    public List<GeoHash> decomposeRange1(Polygon queryPolygon, int maxRecursive) {
        List<GeoHash> rectHash = new ArrayList<>();
        BoundingBox box = queryPolygon.getBoundingBox();
        int maxSearchLevel = getSearchHashes(box).get(0).getSignificantBits() + maxRecursive;
        if (maxSearchLevel > precision) {
            maxSearchLevel = precision;
        }
        int size = rectHash.size();
        for (GeoHash hash : getSearchHashes(box)) {
            findRectGeohash(hash, queryPolygon, maxSearchLevel, rectHash);
            if (size == rectHash.size() && box.intersects(hash.getBoundingBox())) {
                rectHash.add(hash);
            }
            size = rectHash.size();
        }
        return rectHash;
    }

    public List<GeoHash> decomposeBBox(BoundingBox box, int maxRecursive) {
        List<GeoHash> resultHash = new ArrayList<>();
        List<GeoHash> initializeHashs = getSearchHashes(box);
        int maxSearchLevel = initializeHashs.get(0).getSignificantBits() + maxRecursive;

        // 获取初始格网
        Queue<GeoHash> queue = new LinkedList<>(initializeHashs);
        while (!queue.isEmpty()) {
            GeoHash grid = queue.poll();
            byte gridBits = grid.significantBits;
            BoundingBox gridBbox = grid.getBoundingBox();
            if (box.contains(gridBbox)) {
                grid.setContained(true);
                resultHash.add(grid);
            } else if (box.intersects(gridBbox)) {
                if (gridBits>=maxSearchLevel) {
                    grid.setContained(false);
                    resultHash.add(grid);
                } else {
                    grid.setChild();
                    queue.addAll(grid.getChildren());
                }
            }
        }
        return resultHash;
    }

    public List<GeoHash> decomposePolygon(Polygon queryPolygon) {
        // 使用Polygon会有bug, 当两个Polygon都是四边形时,insert判断有问题：
        List<GeoHash> resultHash = new ArrayList<>();
        BoundingBox box = queryPolygon.getBoundingBox();
        // 获取初始格网
        Queue<GeoHash> queue = new LinkedList<>(getSearchHashes(box));
        while (!queue.isEmpty()) {
            GeoHash grid = queue.poll();
            byte gridBits = grid.significantBits;
            BoundingBox gridBbox = grid.getBoundingBox();
            if (queryPolygon.contains(gridBbox)) {
                grid.setContained(true);
                resultHash.add(grid);
            } else if (queryPolygon.intersects(gridBbox)) {
                if (gridBits>=precision) {
                    grid.setContained(false);
                    resultHash.add(grid);
                } else {
                    grid.setChild();
                    queue.addAll(grid.getChildren());
                }
            }
        }
        return resultHash;
    }

    public List<GeoHash> decomposeLineString(Polygon lineString) {
        List<GeoHash> resultHash = new ArrayList<>();
        BoundingBox box = lineString.getBoundingBox();
        // 获取初始格网
        Queue<GeoHash> queue = new LinkedList<>(getSearchHashes(box));
        while (!queue.isEmpty()) {
            GeoHash grid = queue.poll();
            byte gridBits = grid.significantBits;
            BoundingBox gridBbox = grid.getBoundingBox();
            if (lineString.intersects(gridBbox)) {
                if (gridBits>=precision) {
                    grid.setContained(false);
                    resultHash.add(grid);
                } else {
                    grid.setChild();
                    queue.addAll(grid.getChildren());
                }
            }
        }
        return resultHash;
    }

    public List<GeoHash> decomposeRange(BoundingBox boundingBox) {
        List<GeoHash> rectHash = new ArrayList<>();
        int size = rectHash.size();
        List<GeoHash> hashes = getSearchHashes(boundingBox);
        for (GeoHash hash : hashes) {
            findRectGeohash1(hash, boundingBox, precision, rectHash);
            if (size == rectHash.size() && boundingBox.intersects(hash.getBoundingBox())) {
                rectHash.add(hash);
            }
            size = rectHash.size();
        }
        return rectHash;
    }

    @Override
    public List<ByteArrayRange> decomposeRange(List<ByteArrayRange> spatialRanges, Date startDate, Date endDate) {
        return null;
    }

    @Override
    public List<ByteArrayRange> decomposeRangeFully(BoundingBox querybox) {
        return decomposeRange(querybox, precision);
    }

    @Override
    public List<ByteArrayRange> decomposeRangeByCircle(STPoint center, double radius) {
        STPoint northEast = VincentyGeodesy.moveInDirection(VincentyGeodesy.moveInDirection(center, 0, radius), 90, radius);
        STPoint southWest = VincentyGeodesy.moveInDirection(VincentyGeodesy.moveInDirection(center, 180, radius), 270, radius);
        BoundingBox bbox = new BoundingBox(northEast, southWest);
        List<GeoHash> fittingGeohash = toFittingGeohashes(getSearchHashes(bbox));
        List<ByteArrayRange> rangeResult = new ArrayList<>();
        ;
        for (GeoHash geoHash : fittingGeohash) {
            int significantBits = geoHash.getSignificantBits();
            int paddingCharNum = precision - significantBits;
            String rangeStart = GeoHash.fromBinaryString(new StringBuilder().append(geoHash.toBinaryString())
                    .append(StringUtils.repeat("0", paddingCharNum))
                    .toString())
                    .toBase32();
            String rangeEnd = GeoHash.fromBinaryString(new StringBuilder().append(geoHash.toBinaryString())
                    .append(StringUtils.repeat("1", paddingCharNum))
                    .toString())
                    .toBase32();
            rangeResult.add(new ByteArrayRange(new ByteArray(rangeStart.getBytes()), new ByteArray(rangeEnd.getBytes())));
        }
        return rangeResult;
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
    public List<ByteArray> getNeighbours(ByteArray index) {
        List<ByteArray> neighboursCode = new ArrayList<>();
        GeoHash geoHashIndex = GeoHash.fromGeohashString(new String(index.getBytes()));
        GeoHash[] neighbours = geoHashIndex.getAdjacent();
        for (GeoHash geoHash : neighbours) {
            neighboursCode.add(new ByteArray(geoHash.toBase32().getBytes()));
        }
        return neighboursCode;
    }

    @Override
    public List<ByteArray> getChildren(ByteArray index) {
        return null;
    }

    @Override
    public List<ByteArray> getRowKey(ByteArrayRange range) {
        return null;
    }
}