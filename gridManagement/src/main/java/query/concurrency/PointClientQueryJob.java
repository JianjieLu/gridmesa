package query.concurrency;

import geoobject.BoundingBox;
import geoobject.STPoint;
import index.data.ByteArrayRange;
import serialize.SerializePoint;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

/**
 * STPoint client cn.edu.whu.storage.query job.
 *
 * @author shendannan
 * Create on 2019-07-16.
 */
public class PointClientQueryJob extends QueryJob {

    private CountDownLatch countDownLatch;

    private Set<String> results;

    public PointClientQueryJob(Table table, List<ByteArrayRange> ranges, int start, int end,
                               BoundingBox bbox, CountDownLatch countDownLatch, Set<String> results) {
        super(table, ranges, start, end, bbox);
        this.countDownLatch = countDownLatch;
        this.results = results;
    }

    @Override
    public void run() {

        for (int i = start; i < end; i++) {
            try {
                Scan scan = new Scan();
                ByteArrayRange range = ranges.get(i);
                scan.withStartRow(range.getStart().getBytes(), true);
                scan.withStopRow(range.getEnd().getBytes(), true);
                ResultScanner scanner = table.getScanner(scan);
                if (range.isContained()) {
                    //add all points
                    for (Result result : scanner) {
                        Cell[] cells = result.rawCells();
                        for (Cell cell : cells) {
                            SerializePoint point = new SerializePoint.Builder(CellUtil.cloneValue(cell)).build();
                            results.add(point.getId());
                        }
                    }
                } else {
                    for (Result result : scanner) {
                        Cell[] cells = result.rawCells();
                        //precise filter
                        for (Cell cell : cells) {
                            SerializePoint pointNew = new SerializePoint.Builder(CellUtil.cloneValue(cell)).build();
                            STPoint point = new STPoint(pointNew.getLng(), pointNew.getLat(), pointNew.getTime(), pointNew.getId());
                            if (bbox.contains(point)) {
                                results.add(pointNew.getId());
                            }
                        }
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException("scan hbase table failed: " + e.getMessage());
            }
        }
        countDownLatch.countDown();

    }
}
