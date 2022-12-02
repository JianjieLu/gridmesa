//package query.concurrency;
//
//import geoobject.BoundingBox;
//import index.coding.spatial.geohash.GeoHash;
//import index.data.ByteArrayRange;
//import index.util.ByteArrayUtils;
//import org.apache.hadoop.hbase.Cell;
//import org.apache.hadoop.hbase.CellUtil;
//import org.apache.hadoop.hbase.client.Get;
//import org.apache.hadoop.hbase.client.Result;
//import org.apache.hadoop.hbase.client.Scan;
//import org.apache.hadoop.hbase.client.Table;
//import org.apache.hadoop.hbase.util.Bytes;
//
//import java.io.IOException;
//import java.util.List;
//import java.util.Set;
//import java.util.concurrent.CountDownLatch;
//
///**
// * spatial range query job
// *
// * @author yangxiangyang
// * Create on 2022-11-04.
// */
//public class SRQJob extends QueryJob {
//
//    private CountDownLatch countDownLatch;
//    private Set<String> trueHitSet;
//    private Set<String> candidateSet;
//    private List<Get> getList;
//    private Set<String> resultSet;
//
//    public SRQJob(Table gridTable,Table geomTable, ByteArrayRange range,
//                  BoundingBox bbox, Set<String> trueHitSet,
//                  Set<String> candidateSet,List<Get> getList,
//                  Set<String> resultSet, CountDownLatch countDownLatch) {
//        super(gridTable, geomTable, range, bbox);
//        this.countDownLatch = countDownLatch;
//        this.candidateSet = candidateSet;
//        this.trueHitSet = trueHitSet;
//        this.getList = getList;
//        this.resultSet = resultSet;
//    }
//
//    @Override
//    public void run() {
//
//        try {
//            // filter 过程
//            Scan scan = new Scan();
//            scan.withStartRow(range.getStart().getBytes(), true);
//            scan.withStopRow(range.getEnd().getBytes(), true);
//            scan.setFilter(new SRQFilter(bbox));
//            for (Result result : table.getScanner(scan)) {
//                boolean isTrueHit = false;
//                byte[] rowkey = result.getRow();
//                int level = rowkey[rowkey.length-1]&0xff;
//                GeoHash hash = GeoHash.fromBinaryString(ByteArrayUtils.indexToBinaryString(rowkey,level));
//                BoundingBox rowbox = hash.getBoundingBox();
//                if (bbox.contains(rowbox)) {
//                    isTrueHit=true;
//                }
//                Cell[] cells = result.rawCells();
//                for (Cell cell : cells) {
//                    String objectID = Bytes.toString(CellUtil.cloneQualifier(cell));
//                    if (isTrueHit) trueHitSet.add(objectID);
//                    else candidateSet.add(objectID);
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        countDownLatch.countDown();
//    }
//}
