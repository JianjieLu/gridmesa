package query.coprocessor;

import query.coprocessor.proto.PointKnnCountCondition;
import index.coding.CodingUtil;
import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import com.google.protobuf.Service;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.Coprocessor;
import org.apache.hadoop.hbase.CoprocessorEnvironment;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.coprocessor.CoprocessorException;
import org.apache.hadoop.hbase.coprocessor.CoprocessorService;
import org.apache.hadoop.hbase.coprocessor.RegionCoprocessorEnvironment;
import org.apache.hadoop.hbase.protobuf.ResponseConverter;
import org.apache.hadoop.hbase.regionserver.InternalScanner;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class is used for point KNN Coprocessor Query.
 * @author shendannan
 * Create on 2019-8-30.
 */
public class KnnCountEndPoint extends PointKnnCountCondition.CountService implements Coprocessor, CoprocessorService {
    private Logger LOGGER = LoggerFactory.getLogger(KnnCountEndPoint.class);
    private RegionCoprocessorEnvironment env;

    @Override
    public void query(RpcController controller, PointKnnCountCondition.CountRequest request,
                      RpcCallback<PointKnnCountCondition.CountResponse> done) {
        InternalScanner scanner = null;
        PointKnnCountCondition.CountResponse.Builder response = PointKnnCountCondition.CountResponse.newBuilder();

        List<PointKnnCountCondition.CountRange> ranges = request.getRangesList();
        byte[] startKey = env.getRegionInfo().getStartKey();
        byte[] endKey = env.getRegionInfo().getEndKey();
        byte[][] regionStartEndKey = new byte[][] {
                startKey,
                endKey
        };

        int statisticCount = 0;
        Set<String> ids = new HashSet<>();
        for (int i = 0; i < ranges.size(); i++) {
            byte[][] rangeKeys = new byte[][] {
                    ranges.get(i).getRangeStart().toByteArray(),
                    ranges.get(i).getRangeEnd().toByteArray()
            };

            byte[][] intersectRange = CodingUtil.bytesInterSect(regionStartEndKey, rangeKeys);
            if (null == intersectRange) {
                continue;
            }
            Scan scan = new Scan();
            scan.addFamily(Bytes.toBytes("F"));
            scan.withStartRow(intersectRange[0], true);
            scan.withStopRow(intersectRange[1], true);
            try {
                scanner = env.getRegion().getScanner(scan);
                boolean hasMore = false;
                do {
                    List<Cell> cells = new ArrayList<>();
                    hasMore = scanner.next(cells);
                    for (Cell cell : cells) {
                        // count point
                        statisticCount++;
                        ids.add(Bytes.toString(CellUtil.cloneQualifier(cell)));
                    }
                } while (hasMore);
            } catch (IOException e) {
                ResponseConverter.setControllerException(controller, e);
            } finally {
                if (scanner != null) {
                    try {
                        scanner.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        LOGGER.info("start:0000");
        LOGGER.info("count1:"+ids.size());
        PointKnnCountCondition.CountResult.Builder countResult = PointKnnCountCondition.CountResult.newBuilder().setCount(statisticCount).addAllIds(ids);
        LOGGER.info("count2:"+countResult.getCount());
        LOGGER.info("count3:"+countResult.getIdsCount());

        done.run(response.setResult(countResult.build()).build());
    }

    @Override
    public void start(CoprocessorEnvironment coprocessorEnvironment) throws IOException {
        if (coprocessorEnvironment instanceof RegionCoprocessorEnvironment) {
            this.env = (RegionCoprocessorEnvironment) coprocessorEnvironment;
        } else {
            throw new CoprocessorException("no load region");
        }
    }

    @Override
    public void stop(CoprocessorEnvironment coprocessorEnvironment) {

    }

    @Override
    public Service getService() {
        return this;
    }
}
