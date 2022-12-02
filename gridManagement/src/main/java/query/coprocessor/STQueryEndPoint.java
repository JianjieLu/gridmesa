package query.coprocessor;

import geodatabase.dataset.STFMetaData;
import query.coprocessor.proto.PointQueryCondition;
import serialize.SerializePoint;
import index.coding.Coding;
import index.coding.CodingFactory;
import index.data.ByteArray;
import geoobject.BoundingBox;
import geoobject.STPoint;
import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.ByteString;
import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import com.google.protobuf.Service;
import com.sun.org.apache.commons.logging.Log;
import com.sun.org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.coprocessor.CoprocessorException;
import org.apache.hadoop.hbase.coprocessor.CoprocessorService;
import org.apache.hadoop.hbase.coprocessor.RegionCoprocessorEnvironment;
import org.apache.hadoop.hbase.protobuf.ResponseConverter;
import org.apache.hadoop.hbase.regionserver.InternalScanner;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This class is used for point range Coprocessor Query.
 * @author shendannan
 * Create on 2019-8-30.
 */
public class STQueryEndPoint extends PointQueryCondition.QueryService implements
        Coprocessor, CoprocessorService {
    protected static final Log log = LogFactory.getLog(STQueryEndPoint.class);
    private RegionCoprocessorEnvironment env;
    private Coding coding;

    private void initCoding() throws IOException {
        String tableName = env.getRegionInfo().getTable().getNameAsString();
        String[] t = tableName.split("_");
        int codingID = Integer.parseInt(t[t.length-1]);

        String[] tl = tableName.split(":");
        String dataType = tl[0];
        String dataSetName = tl[1].substring(0, tl[1].length()-8);

        Table metaTable = env.getTable(TableName.valueOf(dataType + ":" + "MetaData"));
        Get get = new Get(Bytes.toBytes(dataSetName));
        Result result = metaTable.get(get);
        STFMetaData metaData = new STFMetaData();
        metaData.initFromCells(result.rawCells());
        JSONObject codingSchema = metaData.getCodingSchemaById(codingID);
        coding = CodingFactory.createCoding(codingSchema);
    }

    @Override
    public void query(RpcController controller, PointQueryCondition.QueryRequest request,
                      RpcCallback<PointQueryCondition.QueryResponse> done){

        List<ByteString> finalPoints = new ArrayList<>();
        PointQueryCondition.QueryResponse queryResponse = null;
        InternalScanner scanner = null;

        List<PointQueryCondition.Range> ranges = request.getRangesList();
        PointQueryCondition.BoundingBox bbox = request.getBbox();

        Date[] dates;
        if (bbox.hasStartTime()&&bbox.hasEndTime()){
            dates = new Date[] {
                    new Date(bbox.getStartDate()),
                    new Date(bbox.getEndDate()),
                    new Date(bbox.getStartTime()),
                    new Date(bbox.getEndTime())
            };
        }else {
            dates = new Date[] {
                    new Date(bbox.getStartDate()),
                    new Date(bbox.getEndDate()),
            };
        }
        BoundingBox box=new BoundingBox(bbox.getMinLng(),bbox.getMinLat(),bbox.getMaxLng(),bbox.getMaxLat(),dates);

        log.info("rangesSize:" + ranges.size());
        log.info("boudingbox:" + box.toString());
        int containRangeNum = 0;
        int coarseNumber = 0;
        int outRowKey = 0;
        int containRowKey = 0;
        for( PointQueryCondition.Range range : ranges ) {
            Scan scan = new Scan();
            scan.addFamily(Bytes.toBytes("F"));
            scan.withStartRow( range.getRangeStart().toByteArray(),true );
            scan.withStopRow( range.getRangeEnd().toByteArray(),true );
            try {
                scanner = env.getRegion().getScanner(scan);
                boolean hasMore;
                List<Cell> cells = new ArrayList<>();
                if(range.getIsContained()){
                    //add all points
                    containRangeNum++;
                    do {
                        hasMore = scanner.next(cells);
                        coarseNumber = coarseNumber + cells.size();
                        for (Cell cell : cells) {
                            finalPoints.add(ByteString.copyFrom(CellUtil.cloneValue(cell)));
                        }
                        cells.clear();
                    } while (hasMore);
                    continue;
                }
                do {
                    hasMore = scanner.next(cells);
                    coarseNumber = coarseNumber + cells.size();
                    for (Cell cell : cells) {
                        byte[] rowKey = CellUtil.cloneRow(cell);
                        byte[] data = CellUtil.cloneValue(cell);
                        BoundingBox rowbbox= coding.getBoundingBox(new ByteArray(rowKey));
                        if( !box.intersects(rowbbox)){
                            outRowKey++;
                            continue;
                        }
                        if( box.contains( rowbbox )){
                            containRowKey++;
                            finalPoints.add( ByteString.copyFrom(data) );
                            continue;
                        }
                        SerializePoint pointNew = new SerializePoint.Builder(data).build();
                        STPoint point = new STPoint(pointNew.getLng(),pointNew.getLat(),pointNew.getTime());
                        if(box.contains(point)){
                            finalPoints.add( ByteString.copyFrom(data) );
                        }
                    }
                    cells.clear();
                } while (hasMore);
            } catch (IOException e) {
                ResponseConverter.setControllerException(controller, e);
            } catch (ParseException e) {
                e.printStackTrace();
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

        log.info( "coarse number:" + coarseNumber);
        log.info( "containRange Num:" + containRangeNum);
        log.info( "outRowKey:" + outRowKey );
        log.info( "containRowKey:" + containRowKey );

        queryResponse = PointQueryCondition.QueryResponse.newBuilder().addAllResult( finalPoints ).build();
        done.run(queryResponse);
    }

    @Override
    public void start(CoprocessorEnvironment coprocessorEnvironment) throws IOException {
        if (coprocessorEnvironment instanceof RegionCoprocessorEnvironment) {
            this.env = (RegionCoprocessorEnvironment) coprocessorEnvironment;
            initCoding();
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
