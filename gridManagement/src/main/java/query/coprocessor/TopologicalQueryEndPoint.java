package query.coprocessor;

import common.Constants;
import query.coprocessor.protobuf.TopologicalQueryCondition;
import com.google.protobuf.ByteString;
import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import com.google.protobuf.Service;
import com.sun.org.apache.commons.logging.Log;
import com.sun.org.apache.commons.logging.LogFactory;
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
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TopologicalQueryEndPoint extends TopologicalQueryCondition.QueryService implements
        Coprocessor, CoprocessorService {

    private static final Log log = LogFactory.getLog(TopologicalQueryEndPoint.class);
    private RegionCoprocessorEnvironment env;


    @Override
    public void query(RpcController controller, TopologicalQueryCondition.QueryRequest request, RpcCallback<TopologicalQueryCondition.QueryResponse> done) throws ParseException {
        Set<ByteString> results = new HashSet<>();
        List<TopologicalQueryCondition.Range> ranges = request.getRangesList();
        byte[] GeomWKB = request.getQueryObjWKB().toByteArray();
        boolean isRefine = request.getIsRefine();
        InternalScanner scanner = null;
        WKBReader wkbReader = new WKBReader();
        Geometry queryGeometry = wkbReader.read(GeomWKB);
        for (TopologicalQueryCondition.Range range : ranges) {
            try {
                Scan scan = new Scan();
                scan.addFamily(Bytes.toBytes(Constants.GRID_TABLE_FAMILY));
                scan.withStartRow(range.getRangeStart().toByteArray(), true);
                scan.withStopRow(range.getRangeEnd().toByteArray(), true);
                scanner = env.getRegion().getScanner(scan);
//                log.info("rangeStart:"+range.getRangeStart().toString());
//                log.info("rangeEnd:"+range.getRangeEnd().toString());
//                log.info("queryGeometry:"+ queryGeometry.toString());
//                log.info("isRefine:"+ isRefine);

                boolean hasMore = false;
                List<Cell> cells = new ArrayList<>();
                do {
                    hasMore = scanner.next(cells);
                    for (Cell cell : cells) {
                        // column is objectID
                        byte[] col = CellUtil.cloneQualifier(cell);
                        ByteString objId = ByteString.copyFrom(col);
                        if (results.contains(objId)) {
                            continue;
                        }
                        byte[] data = CellUtil.cloneValue(cell);
                        Geometry subGeometry = wkbReader.read(data);
                        if (!isRefine || queryGeometry.intersects(subGeometry))
                            results.add(objId);
                    }
                    cells.clear();

                } while (hasMore);
            } catch (IOException e) {
                ResponseConverter.setControllerException(controller, (IOException) e);
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

        TopologicalQueryCondition.QueryResponse queryResponse = TopologicalQueryCondition.QueryResponse.newBuilder().addAllResults(results).build();
        done.run(queryResponse);
    }

//    private boolean filter(Cell cell, byte[] GeomWKB) throws ParseException {
//        byte[] data = CellUtil.cloneValue(cell);
//        Geometry candidateGeometry = wkbReader.read(data);
//        Geometry queryGeometry = wkbReader.read(GeomWKB);
//        return queryGeometry.contains(candidateGeometry);
//    }

    @Override
    public Service getService() {
        return this;
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
    public void stop(CoprocessorEnvironment coprocessorEnvironment) throws IOException {

    }
}
