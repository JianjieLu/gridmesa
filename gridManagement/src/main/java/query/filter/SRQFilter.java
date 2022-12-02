//package query.filter;
//
//import geoobject.BoundingBox;
//import geoobject.Polygon;
//import geoobject.STGeometryTypeEnum;
//import geoobject.STPoint;
//import geoobject.geometry.Grid;
//import index.coding.spatial.geohash.GeoHash;
//import index.util.ByteArrayUtils;
//import org.apache.hadoop.hbase.Cell;
//import org.apache.hadoop.hbase.CellUtil;
//import org.apache.hadoop.hbase.filter.Filter;
//import org.apache.hadoop.hbase.filter.FilterBase;
//import org.apache.hadoop.hbase.util.Bytes;
//import org.locationtech.jts.geom.Geometry;
//import org.locationtech.jts.io.WKBReader;
//import serialize.SerializePolygon;
//
//import java.nio.ByteBuffer;
//import java.util.List;
//
//public class SRQFilter extends FilterBase {
//    private final Geometry queryBox;
//    private boolean filterRow = true;
//
//    public SRQFilter(final BoundingBox queryBox) {
//        this.queryBox = queryBox.toJTSPolygon();
//    }
//
//    //重写Filter基类的方法，不可删除
//    public static SRQFilter parseFrom(final byte[] bytes) {
//        ByteBuffer buf = ByteBuffer.wrap(bytes);
//        byte[] box = new byte[buf.getInt()];
//        buf.get(box, 0, box.length);
//
//        return new SRQFilter(new BoundingBox(box));
//    }
//
//    @Override
//    public void reset() {
//        filterRow = true;
//    }
//
//    @Override
//    public boolean filterRowKey(byte[] buffer, int offset, int length) {
//        if (buffer == null)
//            return true;
//        byte[] rowkey = Bytes.copy(buffer, offset, length);
//        int i = rowkey.length - 1;
//        for (; i >= 0; i--) {
//            if (rowkey[i] == (byte) 95) {
//                break;
//            }
//        }
//        byte[] codeAndLevel = new byte[i];
//        System.arraycopy(rowkey, 0, codeAndLevel, 0, i);
//        int level = codeAndLevel[codeAndLevel.length - 1] & 0xff;
//
//        String binaryStrIndex = ByteArrayUtils.indexToBinaryString(codeAndLevel, level);
//        Grid grid = new Grid(binaryStrIndex);
//        if (!queryBox.intersects(grid.getPolygon())) {
//            return true;
//        }
//        filterRow = false;
//        return filterRow;
//    }
//
//    @Override
//    public ReturnCode filterKeyValue(Cell cell) {
//        if (filterRow) {
//            return ReturnCode.NEXT_ROW;
//        }
//        byte[] data = CellUtil.cloneValue(cell);
//        SerializePolygon polygon = new SerializePolygon.Builder(data).build();
//        List<STPoint> lngLats = polygon.getSTPointList();
//        Polygon polygon1 = new Polygon(lngLats, type[0] == STGeometryTypeEnum.POLYLINE.getType());
//        if (!box.intersects(polygon1)) {
//            return ReturnCode.NEXT_COL;
//        }
//        return ReturnCode.INCLUDE;
//    }
//
//    @Override
//    public boolean filterRow() {
//        return filterRow;
//    }
//
//    @Override
//    public byte[] toByteArray() {
//        byte[] boxByte = box.toBytes();
//        int bufferLength = type.length + boxByte.length + 16;
//        ByteBuffer buf = ByteBuffer.allocate(bufferLength);
//        buf.putInt(type.length);
//        buf.put(type);
//        buf.putInt(boxByte.length);
//        buf.put(boxByte);
//        return buf.array();
//    }
//
//    //重写Filter基类的方法，不可删除
//    boolean areSerializedFieldsEqual(Filter o) {
//        if (o == this) return true;
//        if (!(o instanceof SRQFilter)) return false;
//
//        SRQFilter other = (SRQFilter) o;
//
//        return Bytes.equals(this.box.toBytes(), other.box.toBytes());
//    }
//}
