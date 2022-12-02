package serialize;

import geoobject.STGeometryTypeEnum;
import geoobject.STPoint;
import index.util.BitUtil;

import javax.annotation.Nonnull;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Serialized Trajectory.
 */
public class SerializeTrajectory extends SerializeGeometry {

    private SerializeTrajectory() {
        header = new byte[HEADER_LENGTH];
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof SerializeTrajectory)) {
            return false;
        }
        SerializeTrajectory objTrj = (SerializeTrajectory) obj;
        return objTrj.value.equals(this.value) && objTrj.id.equals(this.id) && objTrj.header.equals(this.header);
    }

    @Override
    public int hashCode() {
        return Objects.hash(header, id, value);
    }

    @Override
    @Nonnull
    public Iterator<STPoint> iterator() {
        class Iter implements Iterator<STPoint> {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < SerializeTrajectory.this.getNumber();
            }

            @Override
            public STPoint next() {
                if (hasNext()) {
                    int offset = index * LNG_LAT_TIME_BIT_LENGTH;
                    double lng = ((double) BitUtil.binaryToInt32(value, offset)) / PRECISION;
                    double lat = ((double) BitUtil.binaryToInt32(value, offset + 4)) / PRECISION;
                    long time = BitUtil.binaryToInt64(value, offset + 8);
                    index++;
                    return new STPoint(lng, lat, new Date(time));
                }
                throw new NoSuchElementException("Only" + getNumber() + "element");
            }
        }

        return new Iter();
    }

    public List<STPoint> getSTPointList() {
        Iterable<STPoint> iterable = () -> iterator();
        return StreamSupport.stream(iterable.spliterator(), false).collect(Collectors.toList());
    }

    @Override
    protected void setSTPointList(List<STPoint> stPointList) {
        value = new byte[this.getNumber() * LNG_LAT_TIME_BIT_LENGTH];
        super.setSTPointList(stPointList);
    }

    @Override
    public String toString() {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("id: " + this.getId());
        for (STPoint stPoint : this) {
            stringBuffer.append(", lng: " + stPoint.getLongitude() + ", lat: " + stPoint.getLatitude() + ", time:" +
                    " " + df.format(stPoint.getTime()));
        }
        return stringBuffer.toString();
    }

    public static class Builder {
        private SerializeTrajectory trajectory = new SerializeTrajectory();

        public Builder(STGeometryTypeEnum type, int number) {
            trajectory.setType(type);
            trajectory.setNumber(number);
        }

        public Builder(byte[] data) {
            trajectory.setData(data);
        }

        public Builder(byte[] data, int start) {
            trajectory.setData(data, start);
        }


        public Builder stPointList(List<STPoint> stPointList) {
            trajectory.setSTPointList(stPointList);
            return this;
        }

        public Builder id(String id) {
            trajectory.setId(id);
            return this;
        }

        public SerializeTrajectory build() {
            return trajectory;
        }
    }

}