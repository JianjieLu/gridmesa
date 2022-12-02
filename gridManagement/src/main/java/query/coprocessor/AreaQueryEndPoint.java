package query.coprocessor;

import query.coprocessor.proto.AreaQueryCondition;
import serialize.SerializePolygon;
import index.coding.spatial.geohash.GeoHash;
import geoobject.BoundingBox;
import geoobject.STPoint;
import geoobject.util.SimplePolygon;
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
import org.apache.hadoop.hbase.filter.PrefixFilter;
import org.apache.hadoop.hbase.regionserver.InternalScanner;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is used for Area Coprocessor Query.
 * @author shendannan
 * Create on 2019-7-18.
 */
public class AreaQueryEndPoint extends AreaQueryCondition.QueryService implements
        Coprocessor, CoprocessorService {
    protected static final Log log = LogFactory.getLog(AreaQueryEndPoint.class);
    private RegionCoprocessorEnvironment env;

    @Override
    public void query(RpcController controller, AreaQueryCondition.QueryRequest request,
                      RpcCallback<AreaQueryCondition.QueryResponse> done) {

        List<ByteString> finalPoints = new ArrayList<>();
        AreaQueryCondition.QueryResponse queryResponse = null;

        InternalScanner scanner = null;
        List<AreaQueryCondition.Range> ranges = request.getRangesList();
        AreaQueryCondition.BoundingBox bbox = request.getBbox();
        BoundingBox box=new BoundingBox(bbox.getMinLng(),bbox.getMinLat(),bbox.getMaxLng(),bbox.getMaxLat());

        log.info("boudingbox:" + box.toString());
        int searchNumber = 0;
        for (AreaQueryCondition.Range range:ranges) {
            Scan scan = new Scan();
            scan.addFamily(Bytes.toBytes("F"));
            GeoHash geoHash = GeoHash.fromBinaryString(range.getGeohash());
            String geo = geoHash.toBase32Ignore();
            for (int i = geoHash.getSignificantBits() / 2; i < 32; i++) {
                String rowprefix = GeoHash.base32[i] + geo;
                scan.setFilter(new PrefixFilter(Bytes.toBytes(rowprefix)));
                try {
                    scanner = env.getRegion().getScanner(scan);
                    boolean hasMore = false;
                    List<Cell> cells = new ArrayList<>();
                    do {
                        hasMore = scanner.next(cells);
                        searchNumber = searchNumber + cells.size();
                        if(cells.size()==0){
                            continue;
                        }
                        for (Cell cell : cells) {
                            byte[] data = CellUtil.cloneValue(cell);
                            SerializePolygon polygon = new SerializePolygon.Builder(data).build();
                            List<STPoint> lngLats = polygon.getSTPointList();
                            STPoint[] stPoints = new STPoint[lngLats.size()];
                            lngLats.toArray(stPoints);
                            SimplePolygon polygon1 = new SimplePolygon(stPoints);
                            if(box.intersects(polygon1)){
                                finalPoints.add( ByteString.copyFrom(data) );
                            }
                        }
                        cells.clear();
                    } while (hasMore);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        log.info( "coarse number:" + searchNumber);

        queryResponse = AreaQueryCondition.QueryResponse.newBuilder().addAllResult( finalPoints ).build();
        done.run(queryResponse);
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
