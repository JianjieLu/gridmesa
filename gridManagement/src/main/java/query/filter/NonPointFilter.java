package query.filter;

import geoobject.BoundingBox;
import index.coding.spatial.geohash.GeoHash;
import index.util.ByteArrayUtils;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterBase;
import org.apache.hadoop.hbase.util.Bytes;
import serialize.SerializeSketch;
import java.nio.ByteBuffer;
import java.util.BitSet;
import java.util.List;

public class NonPointFilter extends FilterBase {
    private final BoundingBox queryBox;
    private final BoundingBox extendBox;
    private final int signatureSize;
    private final boolean isPolyline;
    private boolean filterRow = true;

    public NonPointFilter(final BoundingBox queryBox,final BoundingBox extendBox, int signatureSize, boolean isPolyline) {
        this.queryBox = queryBox;
        this.extendBox = extendBox;
        this.signatureSize = signatureSize;
        this.isPolyline = isPolyline;
    }

    // 0. 输入参数转为ByteArray
    @Override
    public byte[] toByteArray() {
        byte[] isPolylineByte = Bytes.toBytes(isPolyline);
        byte[] signatureSizeByte = new byte[1];
        signatureSizeByte[0] = (byte) signatureSize;
        byte[] queryBoxByte = queryBox.toBytes();
        byte[] extendBoxByte = extendBox.toBytes();
        int bufferLength = isPolylineByte.length + signatureSizeByte.length + queryBoxByte.length+extendBoxByte.length+16;
        ByteBuffer buf = ByteBuffer.allocate(bufferLength);
        buf.putInt(isPolylineByte.length);
        buf.put(isPolylineByte);
        buf.putInt(signatureSizeByte.length);
        buf.put(signatureSizeByte);
        buf.putInt(queryBoxByte.length);
        buf.put(queryBoxByte);
        buf.putInt(extendBoxByte.length);
        buf.put(extendBoxByte);
        return buf.array();
    }

    //0.解析传入参数, 初始化filter;
    public static NonPointFilter parseFrom(final byte[] bytes) {
        ByteBuffer buf = ByteBuffer.wrap(bytes);
        byte[] isPolyline = new byte[buf.getInt()];
        buf.get(isPolyline, 0, isPolyline.length);
        byte[] signatureSize = new byte[buf.getInt()];
        buf.get(signatureSize, 0, signatureSize.length);
        byte[] queryBox = new byte[buf.getInt()];
        buf.get(queryBox, 0, queryBox.length);
        byte[] extendBox = new byte[buf.getInt()];
        buf.get(extendBox, 0, extendBox.length);
        return new NonPointFilter(new BoundingBox(queryBox),new BoundingBox(extendBox),(int) signatureSize[0], Bytes.toBoolean(isPolyline));
    }

    // 1. 根据rowKey进行过滤; 返回true表示丢弃;
    @Override
    public boolean filterRowKey(byte[] buffer, int offset, int length) {
        if (buffer == null)
            return true;
        byte[] rowkey = Bytes.copy(buffer, offset, length);
        int i = rowkey.length - 1;
        for (; i >= 0; i--) {
            if (rowkey[i] == (byte) 95) {
                break;
            }
        }
        byte[] codeAndLevel = new byte[i];
        System.arraycopy(rowkey, 0, codeAndLevel, 0, i);
        int level = codeAndLevel[codeAndLevel.length - 1] & 0xff;
        String binaryStrIndex = ByteArrayUtils.indexToBinaryString(codeAndLevel, level);
        GeoHash hash = GeoHash.fromBinaryString(binaryStrIndex);
        filterRow = commonFilter.rowKeyFilter1(extendBox, hash.getBoundingBox());
        return filterRow;
    }

    // 2. 根据Cell进行过滤; 返回ReturnCode;
    @Override
    public ReturnCode filterKeyValue(Cell cell) {
        if(this.filterRow) {
            return ReturnCode.NEXT_ROW;
        }
        return ReturnCode.INCLUDE;
    }
    // 3. 根据Cells进行过滤; 无返回值;
    @Override
    public void filterRowCells(List<Cell> cells) {

        if (cells.size()==0) {
            filterRow=true;
            return;
        }

        Cell geomCell=null;
        Cell sketchCell=null;
        for (Cell cell : cells) {
            if (Bytes.toString(CellUtil.cloneQualifier(cell)).equals("G")){ geomCell=cell; }
            else {sketchCell=cell;}
        }

        byte[] sketchData = CellUtil.cloneValue(sketchCell);
        SerializeSketch serializeSketch = SerializeSketch.fromByteArray(sketchData);
        BoundingBox mbrBox = serializeSketch.getMBR();
        BitSet signature = serializeSketch.getSignature();

        if (commonFilter.mbrFilter(queryBox,mbrBox)) {
            int signatureFilterStatue = commonFilter.bitSetFilter(queryBox, signature, signatureSize);
            if (signatureFilterStatue==0) {
                filterRow=true;
            } else if (signatureFilterStatue==2) {
                filterRow=false;
            } else if (commonFilter.geomFilter(CellUtil.cloneValue(geomCell), queryBox, isPolyline)) {
                filterRow=false;
            }
        }
    }

    @Override
    public boolean filterRow() {
        return filterRow;
    }


    //重写Filter基类的方法，不可删除
    boolean areSerializedFieldsEqual(Filter o) {
        if (o == this) return true;
        if (!(o instanceof NonPointFilter)) return false;

        NonPointFilter other = (NonPointFilter) o;

        return Bytes.equals(this.queryBox.toBytes(), other.queryBox.toBytes());
    }

    @Override
    public void reset() {
        filterRow = true;
    }
}
