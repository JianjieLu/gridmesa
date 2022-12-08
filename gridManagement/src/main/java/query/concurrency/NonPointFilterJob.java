package query.concurrency;

import geoobject.BoundingBox;
import index.coding.spatial.geohash.GeoHash;
import index.data.ByteArrayRange;
import index.util.ByteArrayUtils;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import query.filter.NonPointFilter;
import query.filter.commonFilter;
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
public class NonPointFilterJob extends QueryJob {

    private CountDownLatch countDownLatch;
    private Set<String> resultSet;
    private int signatureSize;
    private boolean isPolyline;
    private BoundingBox extendBox;

    public NonPointFilterJob(Table table, List<ByteArrayRange> ranges,
                             int start, int end,
                             BoundingBox queryBox, BoundingBox extendBox,
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
                scan.setFilter(new NonPointFilter(bbox,extendBox,signatureSize,isPolyline));
                for (Result result : table.getScanner(scan)) {
                    // rowkey 过滤
                    byte[] rowkey = result.getRow();
                    SerializeRowKey serializeRowKey = SerializeRowKey.fromByteArray(rowkey);
                    byte[] xz2Index = serializeRowKey.getXZ2Index();
                    int level = (int) serializeRowKey.getLevel()[0];
                    String objectID = Bytes.toString(serializeRowKey.getId());
                    GeoHash hash = GeoHash.fromBinaryString(ByteArrayUtils.indexToBinaryString(xz2Index,level*2));
                    BoundingBox rowBox = hash.getBoundingBox();
                    int rowKeyFilterState = commonFilter.rowKeyFilter(extendBox,bbox, rowBox);
                    if (rowKeyFilterState==2) {
                        resultSet.add(objectID);
                    } else if (rowKeyFilterState==1) {
                        Cell sketchCell = result.getColumnCells(Bytes.toBytes("F"), Bytes.toBytes("S")).get(0);
                        byte[] sketchData = CellUtil.cloneValue(sketchCell);
                        SerializeSketch serializeSketch = SerializeSketch.fromByteArray(sketchData);
                        BoundingBox mbrBox = serializeSketch.getMBR();
                        BitSet signature = serializeSketch.getSignature();
                        if (commonFilter.mbrFilter(bbox,mbrBox)) {
                            int signatureFilterStatue = commonFilter.bitSetFilter(bbox, signature, signatureSize);
                            if (signatureFilterStatue==2) {
                                resultSet.add(objectID);
                            } else if (signatureFilterStatue==1) {
                                Cell geomCell = result.getColumnCells(Bytes.toBytes("F"), Bytes.toBytes("G")).get(0);
                                if (commonFilter.geomFilter(CellUtil.cloneValue(geomCell), bbox, isPolyline)) {
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
}
