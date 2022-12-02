package query.concurrency;

import geoobject.BoundingBox;
import geoobject.STPoint;
import index.coding.Coding;
import index.coding.CodingFactory;
import index.data.ByteArray;
import index.data.ByteArrayRange;
import serialize.SerializeGeometry;
import serialize.SerializeNullGeometry;
import serialize.SerializeTrajectory;
import com.alibaba.fastjson.JSONObject;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;

import java.util.List;
import java.util.concurrent.LinkedTransferQueue;

public class TrajectoryClientQueryJob extends QueryJob{
    public TrajectoryClientQueryJob(Table table, List<ByteArrayRange> ranges,
                                    int start, int end, BoundingBox bbox, JSONObject coding,
                                    LinkedTransferQueue<SerializeGeometry> resultQueue) {
        super(table, ranges, start, end, bbox, coding, resultQueue);
    }

    @Override
    public void run() {
        int rangeNum = end - start;
        int containRangeNum = 0;
        int rowKeyNum = 0;
        int containRowKey = 0;
        int outRowKey = 0;

        Coding coding = CodingFactory.createCoding(codingSchema);
        for (int i = start; i < end; i++) {
            Scan scan = new Scan();
            scan.withStartRow(ranges.get(i).getStart().getBytes(), true);
            scan.withStopRow(ranges.get(i).getEnd().getBytes(), true);
            try {
                ResultScanner scanner = table.getScanner(scan);

                if (ranges.get(i).isContained()) {
                    containRangeNum++;
                    //add all points
                    for (Result result : scanner) {
                        rowKeyNum++;
                        containRowKey++;
                        Cell[] cells = result.rawCells();
                        for (Cell cell : cells) {
                            resultQueue.add(new SerializeTrajectory.Builder(cell.getValueArray(), cell.getValueOffset()).build());
                        }
                    }
                } else {
                    for (Result result : scanner) {
                        rowKeyNum++;
                        Cell[] cells = result.rawCells();
                        //  System.out.println(cells.length);
                        //rowKey filter
                        byte[] rowKey = result.getRow();
                        BoundingBox rowbbox = coding.getBoundingBox(new ByteArray(rowKey));
                        if (!bbox.intersects(rowbbox)) {
                            outRowKey++;
                            continue;
                        }
                        if (bbox.contains(rowbbox)) {
                            containRowKey++;
                            for (Cell cell : cells) {
                                resultQueue.add(new SerializeTrajectory.Builder(cell.getValueArray(), cell.getValueOffset()).build());
                            }
                            continue;
                        }
                        //precise filter
                        for (Cell cell : cells) {
                            SerializeTrajectory trajectory =
                                    new SerializeTrajectory.Builder(cell.getValueArray(), cell.getValueOffset()).build();
                            for (STPoint point : trajectory) {
                                if (!bbox.contains(point)) continue;
                                resultQueue.add(trajectory);
                                break;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // add end mark for this job
        resultQueue.add(new SerializeNullGeometry());

        System.out.println("Total Range num: " + rangeNum + ", contain range " +
                "num: " + containRangeNum + ", total rowKey num: " + rowKeyNum +
                ", contain row key num: " + containRowKey + ", out row key: " + outRowKey);
    }
}
