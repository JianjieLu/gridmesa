package query.coprocessor;

import common.Constants;
import geoobject.BoundingBox;
import index.coding.spatial.geohash.GeoHash;
import index.util.ByteArrayUtils;
import query.coprocessor.protobuf.SRQueryCondition;
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
import org.locationtech.jts.io.ParseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SRQueryEndPoint extends SRQueryCondition.QueryService implements
        Coprocessor, CoprocessorService {

    private static final Log log = LogFactory.getLog(TopologicalQueryEndPoint.class);
    private RegionCoprocessorEnvironment env;


    @Override
    public void query(RpcController controller, SRQueryCondition.QueryRequest request, RpcCallback<SRQueryCondition.QueryResponse> done) throws ParseException {
        // result
        Set<ByteString> candidateId = new HashSet<>();
        Set<ByteString> trueHitId = new HashSet<>();
        // query range
        List<SRQueryCondition.Range> ranges = request.getRangesList();
        // query box
        double minLon = request.getQueryBox().getMinLon();
        double minLat = request.getQueryBox().getMinLat();
        double maxLon = request.getQueryBox().getMaxLon();
        double maxLat = request.getQueryBox().getMaxLat();
        BoundingBox queryBox = new BoundingBox(minLon,minLat,maxLon,maxLat);
        InternalScanner scanner = null;
        for (SRQueryCondition.Range range : ranges) {
            try {
                Scan scan = new Scan();
                scan.addFamily(Bytes.toBytes(Constants.GRID_TABLE_FAMILY));
                scan.withStartRow(range.getRangeStart().toByteArray(), true);
                scan.withStopRow(range.getRangeEnd().toByteArray(), true);
                scanner = env.getRegion().getScanner(scan);
                boolean hasMore = false;
                List<Cell> cells = new ArrayList<>();
                do {
                    hasMore = scanner.next(cells);
                    boolean isContain=false;
                    if (cells.size()>0) {
                        byte[] rowkey = CellUtil.cloneRow(cells.get(0));
                        int level = rowkey[rowkey.length-1]&0xff;
                        GeoHash hash = GeoHash.fromBinaryString(ByteArrayUtils.indexToBinaryString(rowkey,level));
                        BoundingBox rowbox = hash.getBoundingBox();

                        if(queryBox.intersects(rowbox)){
                            if (queryBox.contains(rowbox)) {
                                isContain=true;
                            }
                            for (Cell cell : cells) {
                                // column is objectID
                                byte[] col = CellUtil.cloneQualifier(cell);
                                ByteString objId = ByteString.copyFrom(col);
                                if (isContain) trueHitId.add(objId);
                                else candidateId.add(objId);
                            }
                        }
                    }
                    cells.clear();

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

        SRQueryCondition.QueryResponse queryResponse = SRQueryCondition.QueryResponse
                .newBuilder()
                .addAllTrueHitId(trueHitId)
                .addAllCandidateId(candidateId)
                .build();
        done.run(queryResponse);
    }

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
