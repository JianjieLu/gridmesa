package query;

import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.ByteString;
import common.Constants;
import geoobject.BoundingBox;
import geoobject.geometry.Grid;
import geoobject.geometry.GridGeometry;
import geoobject.geometry.GridPolygon;
import index.coding.spatial.geohash.GeoHash;
import index.coding.spatial.geohash.GeohashCoding;
import index.data.ByteArrayRange;
import index.util.ByteArrayUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.client.coprocessor.Batch;
import org.apache.hadoop.hbase.ipc.BlockingRpcCallback;
import org.apache.hadoop.hbase.util.Bytes;
import org.locationtech.jts.geom.Polygon;
import query.coprocessor.protobuf.NonPointQueryCondition;
import util.JsonUtil;

import java.io.*;
import java.util.*;

/**
 * Spatial range query.
 * 用于查询线、面数据;
 * @author yangxiangyang
 * create on 2022-11-5.
 */
public class NonPointSRCoprocessorQuery {

    public static List<BoundingBox> generateQueryBoxByConf(JSONObject jsonConf) {

        double initMinLng = Double.parseDouble(jsonConf.getString("minLng"));
        double initMinLat = Double.parseDouble(jsonConf.getString("minLat"));
        double LngRange = Double.parseDouble(jsonConf.getString("LngRange"));
        double LatRange = Double.parseDouble(jsonConf.getString("LatRange"));
        double width = Double.parseDouble(jsonConf.getString("width"));
        int loopTime = Integer.parseInt(jsonConf.getString("loopTime"));
        List<BoundingBox> queryBox = new ArrayList<>();
        Random r = new Random();

        for(int i = 0; i < loopTime;i++) {
            double minLng = initMinLng + LngRange * r.nextDouble();
            double minLat = initMinLat + LatRange * r.nextDouble();
            double maxLng = minLng + width;
            double maxLat = minLat + width;
            BoundingBox bbox = new BoundingBox(minLng,minLat,maxLng,maxLat);
            queryBox.add(bbox);
        }
        return queryBox;
    }

    public static int getMinLevel(BoundingBox queryBox, Table secondaryIndexTable, int secondaryIndexLevel) throws IOException {
        Polygon polygon = queryBox.toJTSPolygon();
        GridGeometry gridPolygon = new GridPolygon(polygon,secondaryIndexLevel);
        int minLevel = 31;
        for (Grid grid:gridPolygon.getSplitGrids()){
            Get get = new Get(Bytes.toBytes(grid.getIndex()));
            get.addColumn(Bytes.toBytes(Constants.SECONDARY_INDEX_TABLE_FAMILY),
                    Bytes.toBytes(Constants.SECONDARY_INDEX_TABLE_QUALIFIER_MIN));
            Result result = secondaryIndexTable.get(get);
            Cell[] cells = result.rawCells();
            for (Cell cell:cells){
                byte[] data = CellUtil.cloneValue(cell);
                minLevel = Math.min(minLevel,(int) data[0]);
            }
        }
        return minLevel;
    }


    public static BoundingBox getExtendBox(BoundingBox queryBox, int minLevel){

        double[] minGridSize = Grid.computeGridSize(minLevel);
        double detaLng = minGridSize[0]; double detaLat = minGridSize[1];
        // 扩展queryBox 保证查询正确性;
        double newMinLng = Math.max(queryBox.getMinLng()-detaLng,-180);
        double newMinLat = Math.max(queryBox.getMinLat()-detaLat,-90);
        double newMaxLng = queryBox.getMaxLng();
        double newMaxLat = queryBox.getMaxLat();
        BoundingBox extendBox = new BoundingBox(newMinLng,newMinLat,newMaxLng,newMaxLat);
//        List<GeoHash> queryBoxInitialGeoHash = GeohashCoding.getSearchHashes(queryBox);
//
//        System.out.println("minLevel:"+minLevel*2+" queryBoxInitialGeoHash:"+queryBoxInitialGeoHash.get(0).getSignificantBits()+" detaLng:"+detaLng+" detaLat:"+detaLat);

        return extendBox;

    }


    public static List<ByteArrayRange> getRanges(BoundingBox extendBox, int minLevel) throws IOException {

        List<ByteArrayRange> ranges = new ArrayList<>();
        List<GeoHash> initialGeoHash = GeohashCoding.getSearchHashes(extendBox);
        Set<GeoHash> geoHashes = new HashSet<>();
        int codeLength = minLevel * 2;
        // 如果查询范围的初始格网比minLevel还要小，则需要扩大格网;
        System.out.println("minLevelCodeLength:"+codeLength+"\textendBoxInitLevel:"+initialGeoHash.get(0).getSignificantBits());
        if (codeLength < initialGeoHash.get(0).getSignificantBits()) {
            for (GeoHash hash : initialGeoHash) {
                geoHashes.add(GeoHash.fromBinaryString(hash.toBinaryString().substring(0, codeLength)));
            }
        } else {
            geoHashes.addAll(initialGeoHash);
        }
        for (GeoHash geoHash: geoHashes) {
            ByteArrayRange range = ByteArrayUtils.binaryStringToIndexRange(geoHash.toBinaryString());
            ranges.add(range);
        }
        return ranges;
    }

    private static Set<String> coprocessorQuery(Table table, List<ByteArrayRange> ranges,
                                                BoundingBox queryBox, BoundingBox extendBox,
                                                int signatureSize, boolean isPolyline) throws Throwable {
        Set<String> results = new HashSet<>();
        if (ranges.size() == 0) return results;

        List<NonPointQueryCondition.Range> rangeList = new ArrayList<>();
        for (ByteArrayRange range : ranges) {
            NonPointQueryCondition.Range r = NonPointQueryCondition.Range.newBuilder()
                    .setRangeStart(ByteString.copyFrom(range.getStart().getBytes()))
                    .setRangeEnd(ByteString.copyFrom(range.getEnd().getBytes()))
                    .build();
            rangeList.add(r);
        }
        NonPointQueryCondition.BoundingBox queryBoxBuild = NonPointQueryCondition.BoundingBox.newBuilder()
                .setMinLng(queryBox.getMinLng())
                .setMinLat(queryBox.getMinLat())
                .setMaxLng(queryBox.getMaxLng())
                .setMaxLat(queryBox.getMaxLat())
                .build();

        NonPointQueryCondition.BoundingBox extendBoxBuild = NonPointQueryCondition.BoundingBox.newBuilder()
                .setMinLng(extendBox.getMinLng())
                .setMinLat(extendBox.getMinLat())
                .setMaxLng(extendBox.getMaxLng())
                .setMaxLat(extendBox.getMaxLat())
                .build();

        NonPointQueryCondition.QueryRequest queryRequest = NonPointQueryCondition.QueryRequest.newBuilder()
                .setQueryBox(queryBoxBuild)
                .setExtendBox(extendBoxBuild)
                .addAllRanges(rangeList)
                .setIsPolyline(isPolyline)
                .setSignatureSize(signatureSize)
                .build();

        Map<byte[], List<ByteString>> response = table.coprocessorService(NonPointQueryCondition.QueryService.class,
                ranges.get(0).getStart().getBytes(), ranges.get(ranges.size() - 1).getEnd().getBytes(),
                new Batch.Call<NonPointQueryCondition.QueryService, List<ByteString>>() {
                    @Override
                    public List<ByteString> call(NonPointQueryCondition.QueryService queryService) throws IOException {
                        BlockingRpcCallback<NonPointQueryCondition.QueryResponse> rpcCallback = new BlockingRpcCallback<>();
                        queryService.query(null, queryRequest, rpcCallback);
                        NonPointQueryCondition.QueryResponse response = rpcCallback.get();
                        return response.getResultsList();
                    }
                });
        for (List<ByteString> bs : response.values()) {
            for (ByteString b : bs) {
                String geomId = Bytes.toString(b.toByteArray());
                results.add(geomId);
            }
        }
        return results;
    }


    public static void main(String[] args) throws Throwable {

        Configuration conf = HBaseConfiguration.create();
        Connection connection = ConnectionFactory.createConnection(conf);
        //读取conf文件配置参数
        JSONObject jsonConf = JsonUtil.readLocalJSONFile(args[0]);
        List<BoundingBox> queryBoxes = new ArrayList<>();
        queryBoxes = generateQueryBoxByConf(jsonConf);
        String mainTableName = jsonConf.getString("mainTable");
        String secondaryTableName = jsonConf.getString("secondaryTable");
        Boolean isPolyline = jsonConf.getBoolean("isPolyline");
        String saveFile = jsonConf.getString("saveFile");
        int signatureSize = jsonConf.getIntValue("signatureSize");
        int secondaryIndexLevel = jsonConf.getIntValue("secondaryIndexLevel");

        Table mainTable = connection.getTable(TableName.valueOf(mainTableName));
        Table secondaryTable = connection.getTable(TableName.valueOf(secondaryTableName));
        BufferedWriter out = new BufferedWriter(new FileWriter(saveFile));

        long AVGTotalTime = 0;
        long AVGGetMinLevelTime = 0;
        long AVGGetExtendBoxTime = 0;
        long AVGGetRangesTime = 0;
        long AVGQueryTime = 0;

        for (BoundingBox queryBox: queryBoxes){
            // 根据二级索引表确定查询范围;
            long start = System.currentTimeMillis();
            int minLevel = getMinLevel(queryBox,secondaryTable,secondaryIndexLevel);
            long end1 = System.currentTimeMillis();
            BoundingBox extendBox = getExtendBox(queryBox,minLevel);
            long end2 = System.currentTimeMillis();
            List<ByteArrayRange> ranges = getRanges(extendBox, minLevel);
            long end3 = System.currentTimeMillis();
            // range query
            Set<String> results = coprocessorQuery(mainTable, ranges, queryBox, extendBox,signatureSize, isPolyline);
            long end4 = System.currentTimeMillis();
            long getMinLevelTime = end1 - start;
            long getExtendBoxTime = end2 - end1;
            long getRangesTime = end3 - end2;
            long queryTime = end4 - end3;
            long totalTime = end4 - start;
            JSONObject tempJson = new JSONObject();
            tempJson.put("minLng",queryBox.getMinLng());
            tempJson.put("minLat",queryBox.getMinLat());
            tempJson.put("maxLng",queryBox.getMaxLng());
            tempJson.put("maxLat",queryBox.getMaxLat());
            tempJson.put("queryTime",queryTime);
            tempJson.put("getExtendBoxTime",getExtendBoxTime);
            tempJson.put("totalTime",totalTime);
            tempJson.put("getRangesTime",getRangesTime);
            tempJson.put("getMinLevelTime",getMinLevelTime);
            tempJson.put("queryResult",results.size());
            out.write(tempJson.toJSONString());
            out.write("\n");
            System.out.println("queryResult:"+tempJson.toJSONString());
            AVGTotalTime+=totalTime;
            AVGGetMinLevelTime+=getMinLevelTime;
            AVGGetExtendBoxTime+=getExtendBoxTime;
            AVGGetRangesTime+=getRangesTime;
            AVGQueryTime+=queryTime;
        }
        int loopTime = queryBoxes.size();
        JSONObject tempJson = new JSONObject();
        tempJson.put("AVGQueryTime",(AVGQueryTime/loopTime));
        tempJson.put("AVGGetRangesTime",(AVGGetRangesTime/loopTime));
        tempJson.put("AVGTotalTime",(AVGTotalTime/loopTime));
        tempJson.put("AVGGetExtendBoxTime",(AVGGetExtendBoxTime/loopTime));
        tempJson.put("AVGGetMinLevelTime",(AVGGetMinLevelTime/loopTime));
        out.write(tempJson.toJSONString());
        out.close();
        System.out.println("AVGQueryTime:"+(AVGQueryTime/loopTime));
        System.out.println("AVGGetRangesTime"+(AVGGetRangesTime/loopTime));
        System.out.println("AVGTotalTime"+(AVGTotalTime/loopTime));
        System.out.println("AVGGetExtendBoxTime"+(AVGGetExtendBoxTime/loopTime));
        System.out.println("AVGGetMinLevelTime"+(AVGGetMinLevelTime/loopTime));
    }

}

