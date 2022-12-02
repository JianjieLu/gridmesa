package query.filter;

import geoobject.BoundingBox;
import geoobject.STPoint;
import index.coding.Coding;
import index.coding.CodingFactory;
import index.data.ByteArray;
import serialize.SerializePoint;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.filter.FilterBase;
import org.apache.hadoop.hbase.util.Bytes;

import java.nio.ByteBuffer;
import java.text.ParseException;

import static constant.CommonConstants.TIME_CODING_PATTERN;


public class PointFilter extends FilterBase {
    private final BoundingBox box;
    private boolean filterRow = true;
    private boolean isRowContained = false;

    public PointFilter(final BoundingBox box) {
        this.box = box;
    }

    public static PointFilter parseFrom(final byte[] bytes) {
        ByteBuffer buf = ByteBuffer.wrap(bytes);
        byte[] box = new byte[buf.getInt()];
        buf.get(box, 0, box.length);
        BoundingBox boundingBox = new BoundingBox(box);
        return new PointFilter(boundingBox);
    }

    @Override
    public void reset() {
        filterRow = true;
        isRowContained = false;
    }

    /**
     * 根据行键进行粗过滤，如果行键所对应的时空范围与查询范围不相交，则进行过滤
     *
     * @param buffer
     * @param offset
     * @param length
     * @return
     */
    @Override
    public boolean filterRowKey(byte[] buffer, int offset, int length) {
        if (buffer == null)
            return true;
        byte[] rowkey = Bytes.copy(buffer, offset, length);
        Coding coding = CodingFactory.createConcatenateCoding(Coding.ConcatenateType.T_S, Coding.SpatialType.Z2,
                24 * 2, TIME_CODING_PATTERN, "s");


        byte[] st_index = new byte[12];//ST/TS编码结果的长度为12
        //从第4个字节开始复制，共复制12个字节，因为前4个字节是分区号，中间12个字节是编码，末尾是点的ID
        System.arraycopy(rowkey, 4, st_index, 0, 12);
        try {
            BoundingBox rowBox = coding.getBoundingBox(new ByteArray(st_index));
            if (!box.intersects(rowBox)) {
                return true;
            }
            if (box.contains(rowBox)) {
                isRowContained = true;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        filterRow = false;
        return filterRow;
    }

    /**
     * 将点数据进行反序列化，判断是否位于查询范围内
     *
     * @param cell
     * @return
     */
    @Override
    public ReturnCode filterKeyValue(Cell cell) {
        if (filterRow) {
            return ReturnCode.NEXT_ROW;
        }
        if (isRowContained) {
            return ReturnCode.INCLUDE;
        }
        byte[] data = CellUtil.cloneValue(cell);
        SerializePoint pointNew = new SerializePoint.Builder(data).build();
        STPoint point = new STPoint(pointNew.getLng(), pointNew.getLat(), pointNew.getTime(), pointNew.getId());
        if (!box.contains(point)) {
            return ReturnCode.NEXT_COL;
        }
        return ReturnCode.INCLUDE;
    }

    @Override
    public boolean filterRow() {
        return filterRow;
    }

    @Override
    public byte[] toByteArray() {
        byte[] boxByte = box.toBytes();
        int bufferLength = boxByte.length + 16;
        ByteBuffer buf = ByteBuffer.allocate(bufferLength);
        buf.putInt(boxByte.length);
        buf.put(boxByte);
        return buf.array();
    }

   /* boolean areSerializedFieldsEqual(Filter o) {
        if (o == this) return true;
        if (!(o instanceof NewPointFilter)) return false;

        NewPointFilter other = (NewPointFilter) o;

        return Bytes.equals(this.box.toBytes(), other.box.toBytes());
    }*/
}