package serialize;

import geoobject.STGeometryTypeEnum;
import geoobject.STPoint;
import index.util.BitUtil;

import javax.annotation.Nonnull;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Serialized st-point.
 */
public class SerializePoint extends SerializeGeometry {

    private static final byte POINT_HEADER_LENGTH = 1;

    private SerializePoint() {
        header = new byte[POINT_HEADER_LENGTH];
        value = new byte[LNG_LAT_TIME_BIT_LENGTH];
    }

    public static SerializePoint fromSTPoint(STPoint point) {
        return new Builder(STGeometryTypeEnum.POINT)
                .lng(point.getLongitude())
                .lat(point.getLatitude())
                .time(point.getLongTime())
                .id(point.getID())
                .build();
    }

    public static SerializePoint fromSPoint(STPoint point) {
        return new Builder(STGeometryTypeEnum.POINT)
                .lng(point.getLongitude())
                .lat(point.getLatitude())
                .id(point.getID())
                .build();
    }

    public double getLng() {
        return ((double) BitUtil.binaryToInt32(value, 0)) / PRECISION;
    }

    private void setLng(double lng) {
        int integerLng = (int) (lng * PRECISION);
        BitUtil.int32ToBinary(integerLng, value, 0);
    }

    public double getLat() {
        return ((double) BitUtil.binaryToInt32(value, 4)) / PRECISION;
    }

    private void setLat(double lat) {
        int integerLat = (int) (lat * PRECISION);
        BitUtil.int32ToBinary(integerLat, value, 4);
    }

    public long getTime() {
        return BitUtil.binaryToInt64(value, 8);
    }

    private void setTime(long time) {
        BitUtil.int64ToBinary(time, value, 8);
    }

    @Override
    public int getNumber() {
        return 1;
    }

    @Override
    protected byte getHeaderLength() {
        return POINT_HEADER_LENGTH;
    }

    /**
     * Iterator method.
     */
    @Override
    @Nonnull
    public Iterator<STPoint> iterator() {
        class Iter implements Iterator<STPoint> {
            private boolean isHasNext = true;

            @Override
            public boolean hasNext() {
                return isHasNext;
            }

            @Override
            public STPoint next() {
                if (isHasNext) {
                    isHasNext = false;
                    return new STPoint(SerializePoint.this.getLng(),
                            SerializePoint.this.getLat(),
                            new Date(SerializePoint.this.getTime()));
                }
                throw new NoSuchElementException("Only 1 element");
            }
        }

        return new Iter();
    }

    @Override
    public String toString() {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return "lng: " + this.getLng() + ", lat: " + this.getLat() + ", time:" +
                " " + df.format(new Date(this.getTime()));
    }

    @Override
    protected void setData(final byte[] data, int start) {
        byte headerLength = getHeaderLength();
        System.arraycopy(data, start, header, 0, headerLength);
        value = new byte[LNG_LAT_TIME_BIT_LENGTH];
        System.arraycopy(data, start + headerLength, value, 0, LNG_LAT_TIME_BIT_LENGTH);
        int idLen = data.length - start - headerLength - value.length;
        id = new byte[idLen];
        System.arraycopy(data, start + headerLength + value.length, id, 0, idLen);
    }

    /**
     * Builder
     */
    public static class Builder {
        private SerializePoint stPoint = new SerializePoint();

        public Builder(STGeometryTypeEnum type) {
            stPoint.setType(type);
        }

        public Builder() {
            stPoint.setType(STGeometryTypeEnum.POINT);
        }

        public Builder(byte[] data) {
            stPoint.setData(data);
        }

        public Builder(byte[] data, int start) {
            stPoint.setData(data, start);
        }

        public Builder lng(double lng) {
            stPoint.setLng(lng);
            return this;
        }

        public Builder lat(double lat) {
            stPoint.setLat(lat);
            return this;
        }

        public Builder time(long time) {
            stPoint.setTime(time);
            return this;
        }

        public Builder id(String id) {
            stPoint.setId(id);
            return this;
        }

        //        public Builder
        public SerializePoint build() {
            return this.stPoint;
        }
    }
}