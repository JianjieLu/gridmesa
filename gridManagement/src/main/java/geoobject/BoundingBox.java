package geoobject;


import constant.CommonConstants;
import geoobject.util.SimplePolygon;
import serialize.SerializePoint;
import util.StringUtil;
import org.apache.hadoop.hbase.util.Bytes;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * This class is the define of spatial-temporal boundingbox object.
 *
 * @author Shendannan
 * Create on 2019-06-06.
 */

public class BoundingBox implements Serializable {

    private static final long serialVersionUID = -7145192134410261076L;
    private double minLng;
    private double minLat;
    private double maxLng;
    private double maxLat;
    private Date dateStart = null;
    private Date dateEnd = null;
    private boolean hasTime = false;

    /**
     * create a bounding box defined by two coordinates
     */
    public BoundingBox(STPoint p1, STPoint p2) {
        this(p1.getLongitude(), p1.getLatitude(), p2.getLongitude(), p2.getLatitude());
        if (p1.hasTime() && p2.hasTime()) {
            hasTime = true;
            dateStart = p1.getTime();
            dateEnd = p2.getTime();
        }
    }

    public BoundingBox(STPoint p1, STPoint p2, Date[] timeRange) {
        this(p1.getLongitude(), p1.getLatitude(), p2.getLongitude(), p2.getLatitude());
        try {
            if (timeRange.length == 2) {
                dateStart = timeRange[0];
                dateEnd = timeRange[1];
                hasTime = true;
            } else {
                throw new IllegalArgumentException();
            }
        } catch (IllegalArgumentException e) {
            System.out.println("The length of timeRange should be two! ");
        }
    }

    public BoundingBox(double minLng, double minLat, double maxLng, double maxLat) {
        this.minLng = Math.min(minLng, maxLng);
        this.maxLng = Math.max(minLng, maxLng);
        this.minLat = Math.min(minLat, maxLat);
        this.maxLat = Math.max(minLat, maxLat);
    }

    public BoundingBox(double minLng, double minLat, double maxLng, double maxLat, Date[] timeRange) {
        this.minLng = Math.min(minLng, maxLng);
        this.maxLng = Math.max(minLng, maxLng);
        this.minLat = Math.min(minLat, maxLat);
        this.maxLat = Math.max(minLat, maxLat);
        try {
            if (timeRange.length == 2) {
                dateStart = timeRange[0];
                dateEnd = timeRange[1];
                hasTime = true;
            } else {
                throw new IllegalArgumentException();
            }
        } catch (IllegalArgumentException e) {
            System.out.println("The length of timeRange should be two! ");
        }
    }

    public BoundingBox getExtendBox() {
        double detaLng = this.maxLng-this.minLng;
        double detaLat = this.maxLat-this.minLat;
        return new BoundingBox(this.minLng,this.minLat,this.maxLng+detaLng,this.maxLat+detaLat);
    }

    public BoundingBox(Iterator<STPoint> stPoints) {

        if (stPoints.hasNext()) {
            STPoint stPoint = stPoints.next();
            this.minLng = stPoint.getLongitude();
            this.maxLng = stPoint.getLongitude();
            this.minLat = stPoint.getLatitude();
            this.maxLat = stPoint.getLatitude();
        }

        while (stPoints.hasNext()) {
            STPoint point = stPoints.next();
            this.minLng = Math.min(minLng, point.getLongitude());
            this.maxLng = Math.max(point.getLongitude(), maxLng);
            this.minLat = Math.min(minLat, point.getLatitude());
            this.maxLat = Math.max(point.getLatitude(), maxLat);
        }
    }

    public BoundingBox(byte[] bbox) {
        byte[] minLngBytes = new byte[8];
        byte[] minLatBytes = new byte[8];
        byte[] maxLngBytes = new byte[8];
        byte[] maxLatBytes = new byte[8];
        System.arraycopy(bbox, 0, minLngBytes, 0, 8);
        System.arraycopy(bbox, 8, minLatBytes, 0, 8);
        System.arraycopy(bbox, 16, maxLngBytes, 0, 8);
        System.arraycopy(bbox, 24, maxLatBytes, 0, 8);
        this.minLng = bytes2Double(minLngBytes);
        this.minLat = bytes2Double(minLatBytes);
        this.maxLng = bytes2Double(maxLngBytes);
        this.maxLat = bytes2Double(maxLatBytes);
        if (bbox.length == (4 * Double.BYTES + 2 * Long.BYTES)) {
            byte[] dateStartBytes = new byte[8];
            byte[] dateEndBytes = new byte[8];
            System.arraycopy(bbox, 32, dateStartBytes, 0, 8);
            System.arraycopy(bbox, 40, dateEndBytes, 0, 8);
            this.dateStart = new Date(Bytes.toLong(dateStartBytes));
            this.dateEnd = new Date(Bytes.toLong(dateEndBytes));
            hasTime = true;
        }
    }

    public static BoundingBox getGlobalBox() {
        return new BoundingBox(-180, -90, 180, 90);
    }

    private static int hashCode(double x) {
        long f = Double.doubleToLongBits(x);
        return (int) (f ^ (f >>> 32));
    }

    public static byte[] double2Bytes(double d) {
        long value = Double.doubleToRawLongBits(d);
        byte[] byteRet = new byte[8];
        for (int i = 0; i < 8; i++) {
            byteRet[i] = (byte) ((value >> 8 * i) & 0xff);
        }
        return byteRet;
    }

    public static double bytes2Double(byte[] arr) {
        long value = 0;
        for (int i = 0; i < 8; i++) {
            value |= ((long) (arr[i] & 0xff)) << (8 * i);
        }
        return Double.longBitsToDouble(value);
    }

    private static byte[] byteMergerAll(int length_byte, byte[]... values) {
        byte[] all_byte = new byte[length_byte];
        int countLength = 0;
        for (byte[] b : values) {
            System.arraycopy(b, 0, all_byte, countLength, b.length);
            countLength += b.length;
        }
        return all_byte;
    }

    public STPoint getUpperLeft() {
        return new STPoint(minLng, maxLat);
    }

    public STPoint getLowerRight() {
        return new STPoint(maxLng, minLat);
    }

    public STPoint getLowerLeft() {
        return new STPoint(minLng, minLat);
    }

    public STPoint getUpperRight() {
        return new STPoint(maxLng, maxLat);
    }

    public double getLongitudeSize() {
        return maxLng - minLng;
    }

    public double getLatitudeSize() {
        return maxLat - minLat;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof BoundingBox) {
            BoundingBox that = (BoundingBox) obj;
            return minLng == that.minLng && minLat == that.minLat && maxLng == that.maxLng && maxLat == that.maxLat;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37 * result + hashCode(minLat);
        result = 37 * result + hashCode(maxLat);
        result = 37 * result + hashCode(minLng);
        result = 37 * result + hashCode(maxLng);
        return result;
    }

    public boolean contains(STPoint point) {
        boolean spatial = (point.getLongitude() >= minLng) && (point.getLatitude() >= minLat)
                && (point.getLongitude() <= maxLng) && (point.getLatitude() <= maxLat);
        boolean time = true;
        if (hasTime && point.hasTime()) {
            time = !point.getTime().before(dateStart) && !point.getTime().after(dateEnd);
        }
        return spatial && time;
    }

    public boolean contains(Point point) {
        boolean spatial = (point.getX() >= minLng) && (point.getY() >= minLat)
                && (point.getX() <= maxLng) && (point.getY() <= maxLat);

        return spatial;
    }

    public boolean contains(SerializePoint point) {
        boolean spatial = (point.getLng() >= minLng) && (point.getLat() >= minLat)
                && (point.getLng() <= maxLng) && (point.getLat() <= maxLat);

        return spatial;
    }

    public boolean contains(BoundingBox other) {
        boolean spatial = (other.getMinLat() >= minLat) && (other.getMinLng() >= minLng) && (other.getMaxLat() <= maxLat) && (other.getMaxLng() <= maxLng);
        if (hasTime && other.isHasTime()) {
            boolean time = !other.getDateStart().before(dateStart) && !other.getDateEnd().after(dateEnd);
            return spatial && time;
        } else {
            return spatial;
        }
    }

    public SimplePolygon toSimplePolygon(){
        STPoint[] list = new STPoint[5];
        list[0] = this.getLowerLeft();
        list[1] = this.getLowerRight();
        list[2] = this.getUpperRight();
        list[3] = this.getUpperLeft();
        list[4] = this.getLowerLeft();
        return new SimplePolygon(list);
    }

    public org.locationtech.jts.geom.Polygon toJTSPolygon(){
        GeometryFactory geometryFactory = new GeometryFactory();
        Coordinate[] list  = new Coordinate[5];
        list[0] = new Coordinate(minLng,minLat);
        list[1] = new Coordinate(maxLng, minLat);
        list[2] = new Coordinate(maxLng, maxLat);
        list[3] = new Coordinate(minLng, maxLat);
        list[4] = new Coordinate(minLng, minLat);
        return geometryFactory.createPolygon(list);
    }

    public Polygon toPolygon() {
        STPoint[] list = new STPoint[5];
        list[0] = this.getLowerLeft();
        list[1] = this.getLowerRight();
        list[2] = this.getUpperRight();
        list[3] = this.getUpperLeft();
        list[4] = this.getLowerLeft();
        return new Polygon(list);
    }

    public boolean contains(Polygon polygon) {
        STPoint[] ring = polygon.getRing();
        for (STPoint point : ring) {
            if (!this.contains(point)) {
                return false;
            }
        }
        return true;
    }

    public boolean intersects(Polygon polygon) {
        //首先判断两个外包框是否相交，如果不相交，直接返回
        if (!intersects(polygon.getBoundingBox())) {
            return false;
        }
        STPoint[] ring = polygon.getRing();
        //判断多边形顶点是否在矩形内：todo：有bug，未必准确；
        for (STPoint point : ring) {
            if (this.contains(point)) {
                return true;
            }
        }
        //判断矩形顶点是否在多边形内
        return polygon.contains(getUpperRight()) || polygon.contains(getLowerLeft())
                || polygon.contains(getLowerRight()) || polygon.contains(getUpperLeft());
    }

    public boolean intersects(SimplePolygon polygon){
        //首先判断两个外包框是否相交，如果不相交，直接返回
        if (!intersects(polygon.getBoundingBox())) {
            return false;
        }
        STPoint[] ring = polygon.getRing();
        //判断多边形顶点是否在矩形内
        for(STPoint point:ring){
            if(this.contains(point)) {return true;}
        }
        //判断矩形顶点是否在多边形内
        return polygon.contains(getUpperRight())||polygon.contains(getLowerLeft())
                ||polygon.contains(getLowerRight())||polygon.contains(getUpperLeft());
    }

    public boolean intersects(BoundingBox other) {
        boolean spatial = !(other.minLng > maxLng || other.maxLng < minLng || other.minLat > maxLat || other.maxLat < minLat);
        if (hasTime && other.isHasTime()) {
            boolean time = !(other.getDateEnd().before(dateStart) || other.getDateStart().after(dateEnd));
            return spatial && time;
        } else {
            return spatial;
        }
    }

    public boolean spatialIntersects(BoundingBox other) {
        return !(other.minLng > maxLng || other.maxLng < minLng || other.minLat > maxLat || other.maxLat < minLat);
    }

    public boolean spatialContain(BoundingBox other) {
        return (other.getMinLat() >= minLat) && (other.getMinLng() >= minLng) && (other.getMaxLat() <= maxLat) && (other.getMaxLng() <= maxLng);
    }

    public boolean timeIntersects(BoundingBox other) {
        return !(other.getDateEnd().before(dateStart) || other.getDateStart().after(dateEnd));
    }

    public boolean timeContains(BoundingBox other) {
        return !other.getDateStart().before(dateStart) && !other.getDateEnd().after(dateEnd);
    }

    public BoundingBox getBigger(BoundingBox other) {
        double minLng = Math.min(this.minLng, other.minLng);
        double maxLng = Math.max(this.maxLng, other.maxLng);
        double minLat = Math.min(this.minLat, other.minLat);
        double maxLat = Math.max(this.maxLat, other.maxLat);
        return new BoundingBox(minLng, minLat, maxLng, maxLat);
    }

    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat(CommonConstants.TIME_FORMAT);
        if (hasTime) {
            return getLowerLeft() + " -> " + getUpperRight() + "\n" +
                    sdf.format(getDateStart()) + " , " + sdf.format(getDateEnd());
        } else {
            return getLowerLeft() + " -> " + getUpperRight();
        }
    }

    public String toJsonString() {
        double minLng = getLowerLeft().getLongitude();
        double minLat = getLowerLeft().getLatitude();
        double maxLng = getUpperRight().getLongitude();
        double maxLat = getUpperRight().getLatitude();
        StringBuilder sb = new StringBuilder();
        sb.append("{\"minLng\":").append(minLng)
                .append(",\"minLat\":").append(minLat)
                .append(",\"maxLng\":").append(maxLng)
                .append(",\"maxLat\":").append(maxLat).append("}");
        return sb.toString();
    }

    public static BoundingBox initFromJsonStr(String jsonStr) {
        jsonStr = jsonStr.replace("{", "").replace("}", "");
        String[] values = jsonStr.split(",");
        double minLng = Double.parseDouble(values[0].split(":")[1]);
        double minLat = Double.parseDouble(values[1].split(":")[1]);
        double maxLng = Double.parseDouble(values[2].split(":")[1]);
        double maxLat = Double.parseDouble(values[3].split(":")[1]);
        return new BoundingBox(minLng, minLat, maxLng, maxLat);
    }

    public static BoundingBox initFromSTList(List<STPoint> stPointList) {
        if (stPointList == null || stPointList.size() == 0) {
            return null;
        }
        STPoint p1 = stPointList.get(0);
        double minLng = p1.getLongitude();
        double maxLng = p1.getLongitude();
        double minLat = p1.getLatitude();
        double maxLat = p1.getLatitude();
        for (STPoint stPoint : stPointList) {
            double lng = stPoint.getLongitude();
            double lat = stPoint.getLatitude();
            minLng = Math.min(minLng, lng);
            maxLng = Math.max(maxLng, lng);
            minLat = Math.min(minLat, lat);
            maxLat = Math.max(maxLat, lat);
        }
        return new BoundingBox(minLng, minLat, maxLng, maxLat);
    }

    public STPoint getCenterPoint() {
        double centerLongitude = (minLng + maxLng) / 2;
        double centerLatitude = (minLat + maxLat) / 2;
        return new STPoint(centerLongitude, centerLatitude);
    }

    public void expandToInclude(BoundingBox other) {
        if (other.minLng < minLng) {
            minLng = other.minLng;
        }
        if (other.maxLng > maxLng) {
            maxLng = other.maxLng;
        }
        if (other.minLat < minLat) {
            minLat = other.minLat;
        }
        if (other.maxLat > maxLat) {
            maxLat = other.maxLat;
        }
    }

    public void expand(double bias) {
        this.minLng -= bias;
        this.minLat -= bias;
        this.maxLng += bias;
        this.maxLat += bias;
    }

    public double getMinLng() {
        return minLng;
    }

    public double getMinLat() {
        return minLat;
    }

    public double getMaxLat() {
        return maxLat;
    }

    public double getMaxLng() {
        return maxLng;
    }

    public boolean isHasTime() {
        return hasTime;
    }

    public Date getDateStart() {
        return dateStart;
    }

    public Date getDateEnd() {
        return dateEnd;
    }

    public void setTime(Date[] timeRange) {
        try {
            if (timeRange.length == 2) {
                dateStart = timeRange[0];
                dateEnd = timeRange[1];
                hasTime = true;
            } else {
                throw new IllegalArgumentException();
            }
        } catch (IllegalArgumentException e) {
            System.out.println("The length of timeRange should be two! ");
        }
    }

    public byte[] coordinatesToBytes(){
        return byteMergerAll(32,double2Bytes(minLng),double2Bytes(minLat), double2Bytes(maxLng),double2Bytes(maxLat));
    }

    public byte[] toBytes() {
        if (hasTime) {
            return byteMergerAll(4 * Double.BYTES + 2 * Long.BYTES,
                    double2Bytes(minLng), double2Bytes(minLat), double2Bytes(maxLng), double2Bytes(maxLat),
                    Bytes.toBytes(dateStart.getTime()), Bytes.toBytes(dateEnd.getTime()));
        } else {
            return byteMergerAll(4 * Double.BYTES,
                    double2Bytes(minLng), double2Bytes(minLat), double2Bytes(maxLng), double2Bytes(maxLat));
        }
    }

    public static BoundingBox initByZorder(String zOrder) {
        BoundingBox box = new BoundingBox(-180, -90, 180, 90);
        if (StringUtil.isEmpty(zOrder)) {
            return box;
        }
        for (int i = 0; i < zOrder.length(); i += 2) {
            String str = zOrder.substring(i, i + 2);
            int idx = StringUtil.parseBinaryStrToInt(str);
            box = box.getSubBoxes()[idx];
        }
        return box;
    }

    public BoundingBox[] getSubBoxes() {
        BoundingBox[] boxes = new BoundingBox[4];
        double midLng = (minLng + maxLng) / 2;
        double midLat = (minLat + maxLat) / 2;
        boxes[0] = new BoundingBox(minLng, minLat, midLng, midLat);
        boxes[1] = new BoundingBox(minLng, midLat, midLng, maxLat);
        boxes[2] = new BoundingBox(midLng, minLat, maxLng, midLat);
        boxes[3] = new BoundingBox(midLng, midLat, maxLng, maxLat);
        return boxes;
    }

    public Envelope boxToEnvelope() {
        return new Envelope(this.minLng, this.maxLng, this.minLat, this.maxLat);
    }

    public BoundingBox getIntersects(BoundingBox other) {
        if (this.intersects(other)) {
            double minLng = Math.max(this.minLng, other.minLng);
            double maxLng = Math.min(this.maxLng, other.maxLng);
            double minLat = Math.max(this.minLat, other.minLat);
            double maxLat = Math.min(this.maxLat, other.maxLat);
            return new BoundingBox(minLng, minLat, maxLng, maxLat);
        } else {
            return null;
        }
    }

}
