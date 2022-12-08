package query;

import com.alibaba.fastjson.JSONObject;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.protobuf.ByteString;
import common.Constants;
import geoobject.BoundingBox;
import geoobject.STPoint;
import geoobject.geometry.Grid;
import geoobject.geometry.GridGeometry;
import geoobject.geometry.GridNonPoint;
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
import query.concurrency.NonPointClientJob;
import query.concurrency.NonPointFilterJob;
import query.concurrency.QueryJob;
import query.coprocessor.protobuf.NonPointQueryCondition;
import util.JsonUtil;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Spatial range query.
 * 用于查询线、面数据;
 * @author yangxiangyang
 * create on 2022-11-5.
 */
public class NonPointSRQuery {

    public static String indexTrans(String index) {
        // Z2生成的索引与本项目中的geohash编码顺序不一致，需要转换;index转换10->01,01->10,
        // todo:怎么让这两个在生成时保持一致？
        int indexLength = index.length();
        StringBuilder indexNew = new StringBuilder();
        for (int i=0;i<indexLength;i+=2){
            String subStr = index.substring(i, i + 2);
            if (subStr.equals("10")){indexNew.append("01");}
            else if (subStr.equals("01")){indexNew.append("10");}
            else {indexNew.append(subStr);}
        }
        return indexNew.toString();
    }

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

    public static List<BoundingBox> generateQueryBoxByRecords(String recordFile) {

        List<BoundingBox> queryBoxes = new ArrayList<>();
        try {
            // create a reader instance
            BufferedReader br = new BufferedReader(new FileReader(recordFile));
            // read until end of file
            String line;
            while ((line = br.readLine()) != null) {
                JSONObject jsonObj = JSONObject.parseObject(line);
                if (jsonObj.containsKey("minLng")) {
                    double minLng = jsonObj.getDoubleValue("minLng");
                    double minLat = jsonObj.getDoubleValue("minLat");
                    double maxLng = jsonObj.getDoubleValue("maxLng");
                    double maxLat = jsonObj.getDoubleValue("maxLat");
                    STPoint bottomLeft = new STPoint(minLng, minLat);
                    STPoint upperRight = new STPoint(maxLng, maxLat);
                    BoundingBox queryBox = new BoundingBox(bottomLeft, upperRight);
                    queryBoxes.add(queryBox);
                }
            }

            // close the reader
            br.close();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return queryBoxes;
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
//        BoundingBox extendBox=queryBox;
        BoundingBox extendBox = new BoundingBox(newMinLng,newMinLat,newMaxLng,newMaxLat);
//        List<GeoHash> queryBoxInitialGeoHash = GeohashCoding.getSearchHashes(queryBox);
//
//        System.out.println("minLevel:"+minLevel*2+" queryBoxInitialGeoHash:"+queryBoxInitialGeoHash.get(0).getSignificantBits()+" detaLng:"+detaLng+" detaLat:"+detaLat);

        return extendBox;

    }

    public static List<ByteArrayRange> getRanges1(BoundingBox extendBox, int minLevel) throws IOException {

        List<ByteArrayRange> ranges = new ArrayList<>();
        int boxLevel = GridGeometry.computeInitialLevel(extendBox.toJTSPolygon());
        if ((boxLevel+2)>=minLevel){boxLevel=minLevel;}
        List<Grid> initialGrids = new GridNonPoint(extendBox.toJTSPolygon(),boxLevel).getSplitGrids();
        Set<String> indexSet = new TreeSet<>();
        for (Grid grid : initialGrids) {
            indexSet.add(grid.getBinaryStrIndex());
        }
        for (String index: indexSet) {
            ByteArrayRange range = ByteArrayUtils.binaryStringToIndexRange(indexTrans(index));
            ranges.add(range);
        }
        return ranges;
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

    private static Set<String> clientQuery(Table mainTable, List<ByteArrayRange> ranges, BoundingBox queryBox, BoundingBox extendBox, int signatureSize,boolean isPolyline) throws InterruptedException {
        Set<String> results = ConcurrentHashMap.<String>newKeySet();
        ThreadFactory threadFactory = new ThreadFactoryBuilder().setDaemon(true).setNameFormat("internal-pol-%d").build();
        int threadNum = ranges.size();
        ThreadPoolExecutor queryWorkersPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(threadNum, threadFactory);
        queryWorkersPool.prestartAllCoreThreads();
        int perSplitNum = ranges.size() / threadNum;//每个线程分到的range
        CountDownLatch countDownLatch = new CountDownLatch(threadNum);
        for (int i = 0; i < threadNum; i++) {
            int start = i * perSplitNum;
            int end = (i + 1) * perSplitNum;
            QueryJob queryJob = new NonPointClientJob(mainTable,
                    ranges, start, end, queryBox, extendBox,
                    results,signatureSize,
                    isPolyline, countDownLatch);
            queryWorkersPool.execute(queryJob);
        }
        countDownLatch.await();
        return results;
    }

    private static Set<String> filterQuery(Table mainTable, List<ByteArrayRange> ranges, BoundingBox queryBox, BoundingBox extendBox, int signatureSize,boolean isPolyline) throws InterruptedException {
        Set<String> results = ConcurrentHashMap.<String>newKeySet();
        ThreadFactory threadFactory = new ThreadFactoryBuilder().setDaemon(true).setNameFormat("internal-pol-%d").build();
        int threadNum = ranges.size();
        ThreadPoolExecutor queryWorkersPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(threadNum, threadFactory);
        queryWorkersPool.prestartAllCoreThreads();
        int perSplitNum = ranges.size() / threadNum;//每个线程分到的range
        CountDownLatch countDownLatch = new CountDownLatch(threadNum);
        for (int i = 0; i < threadNum; i++) {
            int start = i * perSplitNum;
            int end = (i + 1) * perSplitNum;
            QueryJob queryJob = new NonPointFilterJob(mainTable,
                    ranges, start, end, queryBox, extendBox,
                    results,signatureSize,
                    isPolyline, countDownLatch);
            queryWorkersPool.execute(queryJob);
        }
        countDownLatch.await();
        return results;
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
        String mainTableName = jsonConf.getString("mainTable");
        String secondaryTableName = jsonConf.getString("secondaryTable");
        Boolean isPolyline = jsonConf.getBoolean("isPolyline");
        String saveFile = jsonConf.getString("saveFile");
        int signatureSize = jsonConf.getIntValue("signatureSize");
        int secondaryIndexLevel = jsonConf.getIntValue("secondaryIndexLevel");
        String queryType = jsonConf.getString("queryType");
        String queryBoxFrom = jsonConf.getString("queryBoxFrom");
        List<BoundingBox> queryBoxes = new ArrayList<>();
        if (queryBoxFrom.equals("config")) {
            queryBoxes = generateQueryBoxByConf(jsonConf);
        } else {
            String recordFile = jsonConf.getString("recordFile");
            queryBoxes = generateQueryBoxByRecords(recordFile);
        }
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
            List<ByteArrayRange> ranges = getRanges1(extendBox, minLevel);
            long end3 = System.currentTimeMillis();
            // range query
            Set<String> results = new HashSet<>();
            if (queryType.equals("client")) {
                results = clientQuery(mainTable, ranges, queryBox, extendBox,signatureSize, isPolyline);
            } else if (queryType.equals("filter")) {
                results = filterQuery(mainTable, ranges, queryBox, extendBox,signatureSize, isPolyline);
            } else if (queryType.equals("coprocessor")) {
                results = coprocessorQuery(mainTable, ranges, queryBox, extendBox,signatureSize, isPolyline);
            } else {
                throw new RuntimeException("unsupported query mode :" + queryType);
            }
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

