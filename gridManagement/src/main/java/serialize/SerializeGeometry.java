package serialize;

import geoobject.BoundingBox;
import geoobject.STGeometryTypeEnum;
import geoobject.STPoint;
import index.util.BitUtil;

import java.util.Arrays;
import java.util.List;

/**
 * Abstract class of serialized geometry object.
 */
public abstract class SerializeGeometry implements Iterable<STPoint> {

    protected static final int PRECISION = 10000000;
    protected static final byte LNG_LAT_TIME_ID_BIT_LENGTH = 48;//4+4+8+32
    protected static final byte LNG_LAT_TIME_BIT_LENGTH = 16;
    protected static final byte LNG_LAT_BIT_LENGTH = 8;
    protected static final byte HEADER_LENGTH = 5;

    /**
     * Header of this object. The order is:
     * type of geometry,        1 byte
     * number of geometry,      4 bytes (if not point)
     **/
    protected byte[] header;

    protected byte[] value;

    // TODO: to support multi-object, not use currently.
    protected byte[] elementInfo;
    protected byte[] coordinate;

    protected byte[] id;

    protected BoundingBox boundingBox;

    /**
     * Get the binary value of this object. Use to store in the database.
     *
     * @return byte[] binary value of this object.
     */
    public byte[] getData() {
        byte headerLength = getHeaderLength();
        int len;
        if (!(id == null)) {
            len = headerLength + value.length + id.length;
        } else {
            len = headerLength + value.length;
        }
        byte[] data = new byte[len];
        System.arraycopy(header, 0, data, 0, headerLength);
        System.arraycopy(value, 0, data, headerLength, value.length);
        if (!(id == null)) {
            System.arraycopy(id, 0, data, headerLength + value.length, id.length);
        }
        return data;
    }

    /**
     * Set the binary value of this object. Use to analysis the value from database.
     *
     * @param data binary value from database.
     */
    protected void setData(final byte[] data) {
        setData(data, 0);
    }

    protected void setData(final byte[] data, int start) {
        byte headerLength = getHeaderLength();
        System.arraycopy(data, start, header, 0, headerLength);
        value = new byte[getNumber() * LNG_LAT_TIME_BIT_LENGTH];
        System.arraycopy(data, start + headerLength, value, 0, getNumber() * LNG_LAT_TIME_BIT_LENGTH);
        int idLen = data.length - start - headerLength - value.length;
        id = new byte[idLen];
        System.arraycopy(data, start + headerLength + value.length, id, 0, idLen);
    }

    public STGeometryTypeEnum getType() {
        return STGeometryTypeEnum.fromByte(header[0]);
    }

    protected void setType(STGeometryTypeEnum geometryType) {
        header[0] = geometryType.getType();
    }

    public int getNumber() {
        return BitUtil.binaryToInt32(header, 1);
    }

    protected void setNumber(int number) {
        BitUtil.int32ToBinary(number, header, 1);
    }

    /**
     * ID setter and getter.
     */
    public String getId() {
        return new String(id);
    }

    protected void setId(String id) {
        if (id != null) {
            this.id = id.getBytes();
        } else {
            this.id = new byte[0];
        }
    }

    protected void setSTPointList(List<STPoint> stPointList) {
        int offset = 0;
        for (STPoint p : stPointList) {
            int lng = (int) (p.getLongitude() * PRECISION);
            int lat = (int) (p.getLatitude() * PRECISION);
            BitUtil.int32ToBinary(lng, value, offset);
            BitUtil.int32ToBinary(lat, value, offset + 4);
            BitUtil.int64ToBinary(p.getLongTime(), value, offset + 8);
            offset += LNG_LAT_TIME_BIT_LENGTH;
        }
        this.boundingBox = new BoundingBox(stPointList.iterator());
    }

    protected byte getHeaderLength() {
        return HEADER_LENGTH;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SerializeGeometry) {
            SerializeGeometry geometry = (SerializeGeometry) obj;
            return (Arrays.equals(value, geometry.value)) &&
                    (Arrays.equals(id, geometry.id)) &&
                    (Arrays.equals(header, geometry.header));
        }
        return super.equals(obj);
    }

    public BoundingBox getBoundingBox() {
        if (this.boundingBox == null) {
            this.boundingBox = new BoundingBox(iterator());
        }
        return this.boundingBox;
    }

}
