package query.concurrency;

import geoobject.BoundingBox;
import geoobject.STPoint;
import index.data.ByteArrayRange;
import query.filter.NewPointFilter;
import serialize.SerializeGeometry;
import serialize.SerializePoint;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

/**
 * point cn.edu.whu.storage.query using filtering
 *
 * @Author pangzhaoxing
 * @date 2021.5.25
 */
public class PointFilterQueryJob extends QueryJob {

    private CountDownLatch countDownLatch;

    private Set<SerializeGeometry> results;

    public PointFilterQueryJob(Table table, List<ByteArrayRange> ranges, int start, int end,
                               BoundingBox bbox, CountDownLatch countDownLatch, Set<SerializeGeometry> results) {
        super(table, ranges, start, end, bbox);
        this.countDownLatch = countDownLatch;
        this.results = results;
    }

    @Override
    public void run() {
        for (int i = start; i < end; i++) {
            try {
                ByteArrayRange range = ranges.get(i);
                Scan scan = new Scan();
                scan.withStartRow(range.getStart().getBytes(), true);
                scan.withStopRow(range.getEnd().getBytes(), true);
                //当时空不完全包含时，则需要精过滤
                if (!range.isContained())
                    scan.setFilter(new NewPointFilter(bbox));

                for (Result result : table.getScanner(scan)) {
                    Cell[] cells = result.rawCells();
                    for (Cell cell : cells) {
                        byte[] data = CellUtil.cloneValue(cell);
                        SerializePoint pointNew = new SerializePoint.Builder(data).build();
                        results.add(pointNew);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException("scan hbase table failed: " + e.getMessage());
            }
        }
        countDownLatch.countDown();
    }

    private void checkPoint(SerializePoint pointNew) {
        STPoint stPoint = new STPoint(pointNew.getLng(), pointNew.getLat(), pointNew.getTime());
        if (!bbox.contains(stPoint))
            throw new RuntimeException(stPoint.toString());
    }
}
