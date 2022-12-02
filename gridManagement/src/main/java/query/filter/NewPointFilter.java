package query.filter;

import constant.TableConstants;
import geoobject.BoundingBox;
import index.coding.Coding;
import index.coding.CodingFactory;
import index.data.ByteArray;
import serialize.SerializePoint;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.filter.FilterBase;
import org.apache.hadoop.hbase.util.Bytes;

import java.text.ParseException;

import static constant.CommonConstants.TIME_CODING_PATTERN;

public class NewPointFilter extends FilterBase {


    protected final Coding coding;
    private final BoundingBox box;
    private boolean filterRow = false;
    private boolean isRowContained = false;

    public NewPointFilter(final BoundingBox box) {
        this.box = box;
        this.coding = CodingFactory.createConcatenateCoding(Coding.ConcatenateType.T_S, Coding.SpatialType.Z2,
                24 * 2, TIME_CODING_PATTERN, "s");
    }

    public static NewPointFilter parseFrom(final byte[] bytes) {
        return new NewPointFilter(new BoundingBox(bytes));
    }

    @Override
    public void reset() {
        filterRow = false;
        isRowContained = false;
    }

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

        return false;
    }

   /* @Override
    public boolean filterRowKey(byte[] buffer, int offset, int length) {
        try {
            BoundingBox rowBox = parseRowKey(buffer, offset, length);
            if (!box.intersects(rowBox)) {
                return true;
            }
            if (box.contains(rowBox)) {
                isRowContained = true;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    public BoundingBox parseRowKey(byte[] rowKey, int offset, int length) throws ParseException{
       *//* byte[] stIndex = new byte[12];//ST/TS编码结果的长度为12
        //从第4个字节开始复制，共复制12个字节，因为前4个字节是分区号，中间12个字节是编码，末尾是点的ID
        System.arraycopy(rowKey,offset + 4, stIndex,0,12);
        return coding.getBoundingBox(new ByteArray(stIndex));*//*

        byte[] rowkey = Bytes.copy(rowKey, offset, length);
        Coding coding = CodingFactory.getIndexCoding();
        byte[] st_index = new byte[12];//ST/TS编码结果的长度为12
        //从第4个字节开始复制，共复制12个字节，因为前4个字节是分区号，中间12个字节是编码，末尾是点的ID
        System.arraycopy(rowkey,4,st_index,0,12);
        return coding.getBoundingBox(new ByteArray(st_index));
    }*/

   /* @Override
    public ReturnCode filterKeyValue(Cell cell) {
        if (isRowContained) {
            filterRow = false;
            return ReturnCode.NEXT_ROW;
        }
        if (CellUtil.matchingFamily(cell, Bytes.toBytes(COLUMN_FAMILY)) &&
            CellUtil.matchingQualifier(cell, Bytes.toBytes(INDEX_QUALIFIER))){
            SerializePoint pointNew = new SerializePoint.Builder(cell.getValueArray()).build();
            if (box.contains(pointNew.getLng(), pointNew.getLat(), new Date(pointNew.getTime()))) {
                filterRow = false;
            }
            return ReturnCode.NEXT_ROW;
        }

        return ReturnCode.INCLUDE;
    }*/

    @Override
    public ReturnCode filterKeyValue(Cell cell) {

        if (isRowContained) {
            return ReturnCode.INCLUDE;
        }
        //todo 需要重新修改列族
        if (CellUtil.matchingFamily(cell, Bytes.toBytes(TableConstants.ST_UNIQUE_FAMILY)) &&
                CellUtil.matchingQualifier(cell, Bytes.toBytes(TableConstants.META_TOTAL_COUNT_QUALIFIER))) {
            byte[] data = CellUtil.cloneValue(cell);
            SerializePoint pointNew = new SerializePoint.Builder(data).build();
            if (!box.contains(pointNew.iterator().next())) {
                filterRow = true;
                return ReturnCode.NEXT_ROW;
            }
        }

        return ReturnCode.INCLUDE;
    }

    @Override
    public boolean filterRow() {
        return filterRow;
    }

    @Override
    public byte[] toByteArray() {
        return box.toBytes();
    }

}

