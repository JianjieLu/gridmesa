package serialize;

import geoobject.STGeometryTypeEnum;
import geoobject.STPoint;
import index.util.BitUtil;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class SerializePolygon extends SerializeGeometry {
    private SerializePolygon() {
        header = new byte[HEADER_LENGTH];
    }

    @Nonnull
    public Iterator<STPoint> iterator() {
        class Iter implements Iterator<STPoint> {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < SerializePolygon.this.getNumber();
            }

            @Override
            public STPoint next() {
                if (hasNext()) {
                    int offset = index * LNG_LAT_BIT_LENGTH;
                    double lng = ((double) BitUtil.binaryToInt32(value, offset)) / PRECISION;
                    double lat = ((double) BitUtil.binaryToInt32(value, offset + 4)) / PRECISION;
                    index++;
                    return new STPoint(lng, lat);
                }
                throw new NoSuchElementException("Only" + getNumber() + "element");
            }
        }
        return new Iter();
    }

    public List<STPoint> getSTPointList() {
        List<STPoint> lngLatList = new ArrayList<>();
        for (int i = 0; i < this.getNumber(); i++) {
            int offset = i * LNG_LAT_BIT_LENGTH;
            double lng = ((double) BitUtil.binaryToInt32(value, offset)) / PRECISION;
            double lat = ((double) BitUtil.binaryToInt32(value, offset + 4)) / PRECISION;
            lngLatList.add(new STPoint(lng, lat));
        }
        return lngLatList;
    }

    @Override
    protected void setSTPointList(List<STPoint> lngLatList) {
        value = new byte[this.getNumber() * LNG_LAT_BIT_LENGTH];
//        super.setSTPointList(lngLatList);
        int offset = 0;
        for (STPoint p : lngLatList) {
            int lng = (int) (p.getLongitude() * PRECISION);
            int lat = (int) (p.getLatitude() * PRECISION);
            BitUtil.int32ToBinary(lng, value, offset);
            BitUtil.int32ToBinary(lat, value, offset + 4);
            offset += LNG_LAT_BIT_LENGTH;
        }
    }

    @Override
    protected void setData(final byte[] data, int start) {
        byte headerLength = getHeaderLength();
        System.arraycopy(data, start, header, 0, headerLength);
        value = new byte[getNumber() * LNG_LAT_BIT_LENGTH];
        System.arraycopy(data, start + headerLength, value, 0, getNumber() * LNG_LAT_BIT_LENGTH);
        int idLen = data.length - start - headerLength - value.length;
        id = new byte[idLen];
        System.arraycopy(data, start + headerLength + value.length, id, 0, idLen);
    }

    public static class Builder {
        private SerializePolygon polygon = new SerializePolygon();

        public Builder(STGeometryTypeEnum type, int number) {
            polygon.setType(type);
            polygon.setNumber(number);
        }

        public Builder(byte[] data) {
            polygon.setData(data);
        }

        public Builder(byte[] data, int start) {
            polygon.setData(data, start);
        }

        public Builder lngLatList(List<STPoint> lngLatList) {
            polygon.setSTPointList(lngLatList);
            return this;
        }

        public Builder id(String id) {
            polygon.setId(id);
            return this;
        }

        public SerializePolygon build() {
            return polygon;
        }
    }
}