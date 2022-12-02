package query.concurrency;

import geoobject.BoundingBox;
import geoobject.Polygon;
import geoobject.STPoint;
import index.coding.spatial.geohash.GeoHash;
import index.data.ByteArrayRange;
import index.util.ByteArrayUtils;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import serialize.SerializePolygon;
import serialize.SerializeRowKey;
import serialize.SerializeSketch;

import java.io.IOException;
import java.util.BitSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

/**
 * lineString filter query job
 *
 * @author yangxiangyang
 * Create on 2022-11-04.
 */
public class NonPointQueryJob extends QueryJob {

    private CountDownLatch countDownLatch;
    private Set<String> resultSet;
    private int signatureSize;
    private boolean isPolyline;
    private BoundingBox extendBox;

    public NonPointQueryJob(Table table, List<ByteArrayRange> ranges,
                            int start, int end,
                            BoundingBox queryBox,BoundingBox extendBox,
                            Set<String> resultSet,
                            int signatureSize, boolean isPolyline,
                            CountDownLatch countDownLatch) {
        super(table, ranges, start, end, queryBox);
        this.countDownLatch = countDownLatch;
        this.resultSet = resultSet;
        this.signatureSize = signatureSize;
        this.isPolyline = isPolyline;
        this.extendBox = extendBox;
    }

    @Override
    public void run() {

        for (int i = start; i < end; i++) {

            try {
                ByteArrayRange range = ranges.get(i);
                Scan scan = new Scan();
                scan.withStartRow(range.getStart().getBytes(), true);
                scan.withStopRow(range.getEnd().getBytes(), true);
                for (Result result : table.getScanner(scan)) {
                    // rowkey 过滤
                    byte[] rowkey = result.getRow();
                    SerializeRowKey serializeRowKey = new SerializeRowKey.Builder(rowkey).build();
                    byte[] xz2Index = serializeRowKey.getXZ2Index();
                    int level = (int) serializeRowKey.getLevel()[0];
                    String objectID = Bytes.toString(serializeRowKey.getId());
                    GeoHash hash = GeoHash.fromBinaryString(ByteArrayUtils.indexToBinaryString(xz2Index,level*2));
                    BoundingBox rowbox = hash.getBoundingBox();
                    if (extendBox.intersects(rowbox)){
                        if (bbox.contains(rowbox)) {
                            resultSet.add(objectID);
                        } else {
                            Cell sketchCell = result.getColumnCells(Bytes.toBytes("F"), Bytes.toBytes("S")).get(0);
                            SerializeSketch serializeSketch = new SerializeSketch.Builder(CellUtil.cloneValue(sketchCell)).build();
                            // MBR 过滤
                            boolean mbrFilerFlag = mbrFilter(serializeSketch.getMBR());
                            if (mbrFilerFlag) {
                                BitSet signature = serializeSketch.getSignature();
                                // signature 过滤
                                int filterType = bitSetFilter(rowbox,signature);
                                if (filterType == 2){
                                    resultSet.add(objectID);
                                } else if (filterType == 1) {
                                    // geometry 过滤
                                    Cell geomCell = result.getColumnCells(Bytes.toBytes("F"), Bytes.toBytes("G")).get(0);
                                    byte[] geomData = CellUtil.cloneValue(geomCell);
                                    SerializePolygon serializePolygon = new SerializePolygon.Builder(geomData).build();
                                    if (geomFilter(serializePolygon))
                                        resultSet.add(objectID);
                                }
                            }
                        }
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        countDownLatch.countDown();
    }

    public boolean mbrFilter(BoundingBox mbrBox){
        return bbox.intersects(mbrBox);
    }

    public int bitSetFilter(BoundingBox rowbox,BitSet bs){
        int type = 0; // 0:不相交;1:相交;2:至少包含一个网格;
        double MinLng = rowbox.getMinLng();
        double MinLat = rowbox.getMinLat();
        double MaxLng = rowbox.getMaxLng();
        double MaxLat = rowbox.getMaxLat();
        double detaLng = (MaxLng - MinLng)*2 / signatureSize;
        double detaLat = (MaxLat - MinLat)*2 / signatureSize;
        for (int k = bs.nextSetBit(0); k >= 0; k = bs.nextSetBit(k+1)) {
            int i = k / signatureSize;
            int j = k % signatureSize;
            double gridMinX = MinLng + detaLng * i;
            double gridMaxX = MinLng + detaLng * (i+1);
            double gridMinY = MinLat + detaLat*j;
            double gridMaxY = MinLat + detaLat*(j+1);
            BoundingBox signatureBox = new BoundingBox(gridMinX, gridMaxX, gridMinY, gridMaxY);
            if (bbox.contains(signatureBox)) {
                type = 2;
                return type;
            } else if (bbox.intersects(signatureBox)) {
                type = 1;
            }
        }
        return type;
    }

    private boolean geomFilter(SerializePolygon serializePolygon) {
        List<STPoint> lngLats = serializePolygon.getSTPointList();
        Polygon polygon = new Polygon(lngLats, isPolyline);
        return bbox.intersects(polygon);
    }
}
