package query.concurrency;

import geoobject.BoundingBox;
import index.data.ByteArrayRange;
import serialize.SerializeGeometry;
import com.alibaba.fastjson.JSONObject;
import org.apache.hadoop.hbase.client.Table;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedTransferQueue;

/**
 * Abstract query job.
 */
public abstract class QueryJob implements Runnable {
    protected Table table;
    protected Table gridTable;
    protected Table geomTable;
    protected List<ByteArrayRange> ranges;
    protected ByteArrayRange range;
    protected int start;
    protected int end;
    protected BoundingBox bbox;
    protected JSONObject codingSchema;
    protected CountDownLatch countDownLatch;
    protected LinkedTransferQueue<SerializeGeometry> resultQueue;


    public QueryJob(Table table, List<ByteArrayRange> ranges, int start,
                    int end, BoundingBox bbox, JSONObject codingSchema,
                    LinkedTransferQueue<SerializeGeometry> resultQueue) {
        this.table = table;
        this.ranges = ranges;
        this.start = start;
        this.end = end;
        this.bbox = bbox;
        this.codingSchema = codingSchema;
        this.resultQueue = resultQueue;
    }

    public QueryJob(Table table, List<ByteArrayRange> ranges, int start,
                    int end, BoundingBox bbox, JSONObject codingSchema,CountDownLatch countDownLatch) {
        this.table = table;
        this.ranges = ranges;
        this.start = start;
        this.end = end;
        this.bbox = bbox;
        this.codingSchema = codingSchema;
        this.countDownLatch = countDownLatch;
    }

    public QueryJob(Table gridTable,Table geomTable, ByteArrayRange range, BoundingBox bbox) {
        this.gridTable = gridTable;
        this.geomTable = geomTable;
        this.range = range;
        this.bbox = bbox;
    }

    public QueryJob(Table table, List<ByteArrayRange> ranges,
                    int start, int end, BoundingBox bbox) {
        this.table = table;
        this.ranges = ranges;
        this.start = start;
        this.end = end;
        this.bbox = bbox;
    }

    public QueryJob(Table table, BoundingBox bbox) {
        this.table = table;
        this.bbox = bbox;
    }

    public QueryJob(Table table) {
        this.table = table;
    }


    @Override
    public void run() {
        //throw new NotImplementedException();
    }
}
