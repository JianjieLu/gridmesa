package query.coprocessor;

import com.google.protobuf.ByteString;
import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import com.google.protobuf.Service;
import com.sun.org.apache.commons.logging.Log;
import com.sun.org.apache.commons.logging.LogFactory;
import geoobject.BoundingBox;
import geoobject.Polygon;
import geoobject.STPoint;
import index.coding.spatial.geohash.GeohashCoding;
import index.util.ByteArrayUtils;
import query.filter.commonFilter;
import geoobject.util.SimplePolygon;
import index.coding.spatial.geohash.GeoHash;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.Coprocessor;
import org.apache.hadoop.hbase.CoprocessorEnvironment;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.coprocessor.CoprocessorException;
import org.apache.hadoop.hbase.coprocessor.CoprocessorService;
import org.apache.hadoop.hbase.coprocessor.RegionCoprocessorEnvironment;
import org.apache.hadoop.hbase.filter.PrefixFilter;
import org.apache.hadoop.hbase.protobuf.ResponseConverter;
import org.apache.hadoop.hbase.regionserver.InternalScanner;
import org.apache.hadoop.hbase.util.Bytes;
import query.coprocessor.protobuf.NonPointQueryCondition;
import serialize.SerializePolygon;
import serialize.SerializeRowKey;
import serialize.SerializeSketch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 * This class is used for lineString/Polygon Coprocessor Query.
 * @author yangxiangyang
 * Create on 2022-12-03.
 */
public class NonPointQueryEndPoint extends NonPointQueryCondition.QueryService implements
        Coprocessor, CoprocessorService {
    protected static final Log log = LogFactory.getLog(NonPointQueryEndPoint.class);
    private RegionCoprocessorEnvironment env;

    @Override
    public void query(RpcController controller, NonPointQueryCondition.QueryRequest request,
                      RpcCallback<NonPointQueryCondition.QueryResponse> done) {

        List<ByteString> results = new ArrayList<>();
        List<NonPointQueryCondition.Range> ranges = request.getRangesList();
        NonPointQueryCondition.BoundingBox requestQueryBox = request.getQueryBox();
        NonPointQueryCondition.BoundingBox requestExtendBox = request.getExtendBox();
        int signatureSize = request.getSignatureSize();
        boolean isPolyline = request.getIsPolyline();
        BoundingBox queryBox = new BoundingBox(requestQueryBox.getMinLng(),requestQueryBox.getMinLat(),requestQueryBox.getMaxLng(),requestQueryBox.getMaxLat());
        BoundingBox extendBox = new BoundingBox(requestExtendBox.getMinLng(),requestExtendBox.getMinLat(),requestExtendBox.getMaxLng(),requestExtendBox.getMaxLat());
        InternalScanner scanner = null;
        for (NonPointQueryCondition.Range range : ranges) {
            try {
                Scan scan = new Scan();
                scan.withStartRow(range.getRangeStart().toByteArray(), true);
                scan.withStopRow(range.getRangeEnd().toByteArray(), true);
                scanner = env.getRegion().getScanner(scan);
                boolean hasMore = false;
                List<Cell> cells = new ArrayList<>();
                do {
                    hasMore = scanner.next(cells);
                    if (cells.size()==0) { break;}
                    Cell geomCell=null;
                    Cell sketchCell=null;
                    byte[] rowKey = CellUtil.cloneRow(cells.get(0));;
                    SerializeRowKey serializeRowKey = SerializeRowKey.fromByteArray(rowKey);
                    byte[] geomId = serializeRowKey.getId();
                    byte[] xz2Index = serializeRowKey.getXZ2Index();
                    int indexLevel = (int) serializeRowKey.getLevel()[0];

                    BoundingBox rowBox = GeoHash.fromBinaryString(ByteArrayUtils.indexToBinaryString(xz2Index, indexLevel*2)).getBoundingBox();
                    int rowKeyFilterState = commonFilter.rowKeyFilter(extendBox,queryBox, rowBox);
                    if (rowKeyFilterState==2) {
                        results.add(ByteString.copyFrom(geomId));
                    } else if (rowKeyFilterState==1) {
                        for (Cell cell : cells) {
                            if (Bytes.toString(CellUtil.cloneQualifier(cell)).equals("G")){ geomCell=cell; }
                            else {sketchCell=cell;}
                        }
                        byte[] sketchData = CellUtil.cloneValue(sketchCell);
                        SerializeSketch serializeSketch = SerializeSketch.fromByteArray(sketchData);
                        BoundingBox mbrBox = serializeSketch.getMBR();
                        BitSet signature = serializeSketch.getSignature();

                        if (commonFilter.mbrFilter(queryBox,mbrBox)) {
                            int signatureFilterStatue = commonFilter.bitSetFilter(queryBox, signature, signatureSize);
                            if (signatureFilterStatue==2) {
                                results.add(ByteString.copyFrom(geomId));
                            } else if (signatureFilterStatue==1) {
                                if (commonFilter.geomFilter(CellUtil.cloneValue(geomCell), queryBox, isPolyline)) {
                                    results.add(ByteString.copyFrom(geomId));
                                }
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
        NonPointQueryCondition.QueryResponse queryResponse = NonPointQueryCondition
                .QueryResponse
                .newBuilder()
                .addAllResults(results)
                .build();
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
