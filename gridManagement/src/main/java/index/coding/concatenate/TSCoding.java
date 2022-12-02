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

import java.io.Serializable;
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
public class TSCoding implements ConcatenateCoding, Serializable {
    private final TimeCoding timeCoding;
    private final SpatialCoding spatialCoding;
    private final ConcatenateType connectionType;

    public TSCoding(SpatialType type,
                    int precision,
                    String format,
                    String timeCoarseBin) {
        this.connectionType = ConcatenateType.T_S;
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
        for (ByteArray timeBin : timeRanges) {
            for (ByteArrayRange range : spatialRanges) {
                rangeStart = new ByteArray(Bytes.concat(timeBin.getBytes(), range.getStart().getBytes()));
                rangeEnd = new ByteArray(Bytes.concat(timeBin.getBytes(), range.getEnd().getBytes()));
                rangeResult.add(new ByteArrayRange(rangeStart, rangeEnd, false, range.isContained()));
            }
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
        for (ByteArray timeBin : timeRanges) {
            for (ByteArrayRange range : spatialRanges) {
                rangeStart = new ByteArray(Bytes.concat(timeBin.getBytes(), range.getStart().getBytes()));
                rangeEnd = new ByteArray(Bytes.concat(timeBin.getBytes(), range.getEnd().getBytes()));
                rangesResult.add(new ByteArrayRange(rangeStart, rangeEnd, false, range.isContained()));
            }
        }
        return rangesResult;
    }

    @Override
    public ByteArray getIndex(STPoint point) {
        Date time = point.getTime();
        ByteArray timeCode = timeCoding.getIndex(time);
        ByteArray spatialCode = spatialCoding.getIndex(point);

        byte[] index = Bytes.concat(timeCode.getBytes(), spatialCode.getBytes());
        return new ByteArray(index);
    }

    @Override
    public STPoint getPoint(ByteArray index) throws ParseException {
        byte[] indexByte = index.getBytes();
        String pattern = timeCoding.getDateFormat().toPattern();
        byte[] t1 = Arrays.copyOfRange(indexByte, 0, pattern.length() / 2);
        STPoint point = spatialCoding.getPoint(
                new ByteArray(Arrays.copyOfRange(indexByte, t1.length, indexByte.length)));
        point.setTime(timeCoding.invert(new ByteArray(t1)));
        return point;
    }

    @Override
    public BoundingBox getBoundingBox(ByteArray index) throws ParseException {
        byte[] indexByte = index.getBytes();
        String pattern = timeCoding.getDateFormat().toPattern();
        byte[] t1 = Arrays.copyOfRange(indexByte, 0, pattern.length() / 2);
        BoundingBox boundingBox = spatialCoding.getBoundingBox(
                new ByteArray(Arrays.copyOfRange(indexByte, t1.length, indexByte.length)));
        Date[] timeRange = timeCoding.preciseInvert(new ByteArray(t1));
        boundingBox.setTime(timeRange);
        return boundingBox;
    }

    @Override
    public String indexToString(ByteArray index) {
        byte[] indexByte = index.getBytes();
        SpatialType type = spatialCoding.getSpatialType();
        String pattern = timeCoding.getDateFormat().toPattern();

        byte[] t1 = Arrays.copyOfRange(indexByte, 0, pattern.length() / 2);
        byte[] spatialCode = Arrays.copyOfRange(indexByte, t1.length, indexByte.length);
        if (type.equals(SpatialType.Geohash)) {
            return StringUtils.timeIndexToString(t1) + new String(spatialCode, StandardCharsets.UTF_8);
        } else if (type.equals(SpatialType.Z2)) {
            return StringUtils.timeIndexToString(t1) + spatialCoding.indexToString(new ByteArray(spatialCode));
        }
        return "";
    }

    @Override
    public ByteArray stringToIndex(String indexStr) {

        SpatialType type = spatialCoding.getSpatialType();
        String pattern = timeCoding.getDateFormat().toPattern();
        byte[] spatialCode;
        byte[] t1 = StringUtils.timeStringToBytes(indexStr.substring(0, pattern.length()));
        if (type.equals(SpatialType.Geohash)) {
            spatialCode = indexStr.substring(pattern.length()).getBytes();
        } else {
            spatialCode = spatialCoding.stringToIndex(indexStr.substring(pattern.length())).getBytes();
        }
        return new ByteArray(Bytes.concat(t1, spatialCode));
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
