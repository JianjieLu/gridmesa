package index.coding.concatenate;

import geoobject.BoundingBox;
import geoobject.Polygon;
import geoobject.STPoint;
import index.coding.CodingFactory;
import index.coding.concatenate.time.TimeCoding;
import index.coding.spatial.SpatialCoding;
import index.data.ByteArray;
import index.data.ByteArrayRange;
import index.util.StringUtils;
import com.google.common.primitives.Bytes;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Create on 2019-06-06.
 *
 * @author Shendannan
 * TSCoding class implements ConcatenateCoding, time-space coding
 */
public class STCoding implements ConcatenateCoding {
    private final TimeCoding timeCoding;
    private final SpatialCoding spatialCoding;
    private final ConcatenateType connectionType;

    public STCoding(SpatialType type,
                    int precision,
                    String format,
                    String timeCoarseBin) {
        this.connectionType = ConcatenateType.S_T;
        this.spatialCoding = CodingFactory.createSpatialCoding(type, precision);
        this.timeCoding = new TimeCoding(format, timeCoarseBin);
    }

    @Override
    public SpatialCoding getSpatialCoding() {
        return spatialCoding;
    }

    @Override
    public ConcatenateCoding getConcatenateCoding() throws Exception {
        return this;
    }

    @Override
    public IndexType getIndexType() {
        return indexType;
    }

    @Override
    public ConcatenateType getConcatenateType() {
        return connectionType;
    }

    @Override
    public TimeCoding getTimeCoding() {
        return timeCoding;
    }

    @Override
    public List<ByteArrayRange> decomposeRange(BoundingBox querybox, int maxRecursive) {
        List<ByteArrayRange> spatialRanges = spatialCoding.decomposeRange(querybox, maxRecursive);
        List<ByteArray> timeRanges =
                timeCoding.decomposeRangeContinuous(querybox.getDateStart(), querybox.getDateEnd());
        return decomposeRange(spatialRanges, timeRanges);
    }

    @Override
    public List<ByteArrayRange> decomposeRange(Polygon queryPolygon, int maxRecursive) {
        List<ByteArrayRange> spatialRanges = spatialCoding.decomposeRange(queryPolygon, maxRecursive);
        List<ByteArray> timeRanges =
                timeCoding.decomposeRangeContinuous(queryPolygon.getDateStart(), queryPolygon.getDateEnd());
        return decomposeRange(spatialRanges, timeRanges);
    }

    private List<ByteArrayRange> decomposeRange(List<ByteArrayRange> spatialRanges,
                                                List<ByteArray> timeRanges) {
        System.out.println("spatial range:" + spatialRanges.size());
        System.out.println("time range:" + timeRanges.size());
        List<ByteArrayRange> rangeResult = new ArrayList<>();
        ByteArray rangeStart, rangeEnd;
        for (ByteArrayRange range : spatialRanges) {
            rangeStart = new ByteArray(Bytes.concat(range.getStart().getBytes(), timeRanges.get(0).getBytes()));
            rangeEnd = new ByteArray(Bytes.concat(range.getEnd().getBytes(), timeRanges.get(timeRanges.size() - 1).getBytes()));
            rangeResult.add(new ByteArrayRange(rangeStart, rangeEnd, false, range.isContained()));
        }
        return rangeResult;
    }

    @Override
    public List<ByteArrayRange> decomposeRangeFully(BoundingBox querybox) {
        List<ByteArrayRange> spatialRanges = spatialCoding.decomposeRangeFully(querybox);
        List<ByteArray> timeRanges =
                timeCoding.decomposeRangeContinuous(querybox.getDateStart(), querybox.getDateEnd());
        return decomposeRange(spatialRanges, timeRanges);
    }

    @Override
    public List<ByteArrayRange> decomposeRange(List<ByteArrayRange> spatialRanges,
                                               Date startDate,
                                               Date endDate) {
        List<ByteArray> timeRanges = timeCoding.decomposeRangeContinuous(startDate, endDate);
        ByteArray rangeStart, rangeEnd;
        List<ByteArrayRange> rangesResult = new ArrayList<>();
        for (ByteArrayRange range : spatialRanges) {
            rangeStart = new ByteArray(Bytes.concat(range.getStart().getBytes(), timeRanges.get(0).getBytes()));
            rangeEnd = new ByteArray(Bytes.concat(range.getEnd().getBytes(), timeRanges.get(timeRanges.size() - 1).getBytes()));
            rangesResult.add(new ByteArrayRange(rangeStart, rangeEnd, false, range.isContained()));
        }
        return rangesResult;
    }

    @Override
    public ByteArray getIndex(STPoint point) {
        Date time = point.getTime();
        ByteArray timeCode = timeCoding.getIndex(time);
        ByteArray spatialCode = spatialCoding.getIndex(point);

        byte[] index = Bytes.concat(spatialCode.getBytes(), timeCode.getBytes());
        return new ByteArray(index);
    }

    @Override
    public STPoint getPoint(ByteArray index) throws ParseException {
        byte[] indexByte = index.getBytes();
        String pattern = timeCoding.getDateFormat().toPattern();
        byte[] t1 = Arrays.copyOfRange(indexByte, indexByte.length - pattern.length() / 2, indexByte.length);
        STPoint point = spatialCoding.getPoint(
                new ByteArray(Arrays.copyOfRange(indexByte, 0, indexByte.length - t1.length)));
        point.setTime(timeCoding.invert(new ByteArray(t1)));
        return point;
    }

    @Override
    public BoundingBox getBoundingBox(ByteArray index) throws ParseException {
        byte[] indexByte = index.getBytes();
        String pattern = timeCoding.getDateFormat().toPattern();
        byte[] t1 = Arrays.copyOfRange(indexByte, indexByte.length - pattern.length() / 2, indexByte.length);
        BoundingBox boundingBox = spatialCoding.getBoundingBox(
                new ByteArray(Arrays.copyOfRange(indexByte, 0, indexByte.length - t1.length)));
        Date[] timeRange = timeCoding.preciseInvert(new ByteArray(t1));
        boundingBox.setTime(timeRange);
        return boundingBox;
    }

    @Override
    public String indexToString(ByteArray index) {
        byte[] indexByte = index.getBytes();
        SpatialType type = spatialCoding.getSpatialType();
        String pattern = timeCoding.getDateFormat().toPattern();

        byte[] t1 = Arrays.copyOfRange(indexByte, indexByte.length - pattern.length() / 2, indexByte.length);
        byte[] spatialCode = Arrays.copyOfRange(indexByte, 0, indexByte.length - t1.length);
        if (type.equals(SpatialType.Geohash)) {
            return new String(spatialCode, StandardCharsets.UTF_8) + new String(t1, StandardCharsets.UTF_8);
        } else if (type.equals(SpatialType.Z2)) {
            return spatialCoding.indexToString(new ByteArray(spatialCode)) + StringUtils.timeIndexToString(t1);
        }
        return "";
    }

    @Override
    public ByteArray stringToIndex(String indexStr) {
        SpatialType type = spatialCoding.getSpatialType();

        String pattern = timeCoding.getDateFormat().toPattern();
        String timeStr = indexStr.substring(indexStr.length() - pattern.length());
        String spaceStr = indexStr.substring(0, indexStr.length() - pattern.length());
        byte[] spatialCode;
        byte[] t1 = StringUtils.timeStringToBytes(timeStr);
        if (type.equals(SpatialType.Geohash)) {
            spatialCode = spaceStr.getBytes();
        } else {
            spatialCode = spatialCoding.stringToIndex(spaceStr).getBytes();
        }
        return new ByteArray(Bytes.concat(spatialCode, t1));
    }

    @Override
    public int getPrecision() {
        return spatialCoding.getPrecision();
    }

    @Override
    public List<ByteArrayRange> decomposeRangeByCircle(STPoint center, double radius) {
        return spatialCoding.decomposeRangeByCircle(center,radius);
    }

    @Override
    public List<STPoint> getBBoxByCircle(STPoint center, double radius) {
        return spatialCoding.getBBoxByCircle(center,radius);
    }
}
