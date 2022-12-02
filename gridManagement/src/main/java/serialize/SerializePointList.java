package serialize;

import geoobject.STGeometryTypeEnum;
import index.util.BitUtil;
import geoobject.STPoint;

import javax.annotation.Nonnull;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Serialized Pointlist for visualization.
 * @author Shendannan
 * create on 2019-04-20
 */
public class SerializePointList extends SerializeGeometry{

    private SerializePointList() {
        header = new byte[HEADER_LENGTH];
    }

    @Override
    protected void setSTPointList(List<STPoint> stPointList) {
        value = new byte[this.getNumber() * LNG_LAT_TIME_ID_BIT_LENGTH];
        int offset = 0;
        for (STPoint p : stPointList) {
            int lng = (int) (p.getLongitude() * PRECISION);
            int lat = (int) (p.getLatitude() * PRECISION);
            BitUtil.int32ToBinary(lng, value, offset);
            BitUtil.int32ToBinary(lat, value, offset + 4);
            BitUtil.int64ToBinary(p.getLongTime(), value, offset + 8);
            BitUtil.idToBinary(p.getID(),value,offset + 16);
            offset += LNG_LAT_TIME_ID_BIT_LENGTH;
        }
    }

    @Override
    @Nonnull
    public Iterator<STPoint> iterator() {
        class Iter implements Iterator<STPoint> {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < SerializePointList.this.getNumber();
            }

            @Override
            public STPoint next() {
                if (hasNext()) {
                    int offset = index * LNG_LAT_TIME_ID_BIT_LENGTH;
                    double lng = ((double) BitUtil.binaryToInt32(value, offset)) / PRECISION;
                    double lat = ((double) BitUtil.binaryToInt32(value, offset + 4)) / PRECISION;
                    long time = BitUtil.binaryToInt64(value, offset + 8);
                    String id = BitUtil.binaryToId(value,offset + 16);
                    index++;
                    return new STPoint(lng, lat, new Date(time),id);
                }
                throw new NoSuchElementException("Only" + getNumber() + "element");
            }
        }

        return new Iter();
    }

    public static class Builder {
        private SerializePointList pointList = new SerializePointList();

        public Builder(STGeometryTypeEnum type, int number) {
            pointList.setType(type);
            pointList.setNumber(number);
        }

        public Builder(byte[] data) {
            pointList.setData(data);
        }

        public Builder(byte[] data, int start) {
            pointList.setData(data, start);
        }

        public Builder stPointList(List<STPoint> stPointList) {
            pointList.setSTPointList(stPointList);
            return this;
        }

        public SerializePointList build() {
            return pointList;
        }
    }

    @Override
    protected void setData(final byte[] data, int start) {
        byte headerLength = getHeaderLength();
        System.arraycopy(data, start, header, 0, headerLength);
        value = new byte[getNumber() * LNG_LAT_TIME_ID_BIT_LENGTH];
        System.arraycopy(data, start + headerLength, value, 0, getNumber() * LNG_LAT_TIME_ID_BIT_LENGTH);
    }
}
