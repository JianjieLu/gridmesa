package query;

import com.alibaba.fastjson.JSONObject;
import common.Constants;
import geoobject.BoundingBox;
import geoobject.STPoint;
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
import org.apache.hadoop.hbase.util.Bytes;
import org.locationtech.jts.geom.Polygon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import query.concurrency.NonPointQueryJob;
import query.concurrency.QueryJob;
import query.condition.QueryCondition;
import query.condition.SRangeCondition;
import util.JsonUtil;
import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * Spatial range query.
 * 用于查询线、面数据;
 * @author yangxiangyang
 * create on 2022-11-5.
 */
public class NonPointSRQ extends QueryOptions {
    private static final Logger LOGGER = LoggerFactory.getLogger(NonPointSRQ.class);

    Set<String> resultSet;
    long totalTime;
    long getMinLevelTime;
    long getExtendBoxTime;
    long getRangesTime;
    long queryTime;

    public NonPointSRQ(Table mainTable, Table secondaryTable) {
        super(mainTable,secondaryTable);
    }

    public void initVariates() {
        resultSet = ConcurrentHashMap.<String>newKeySet();
        totalTime = 0;
        getMinLevelTime = 0;
        getExtendBoxTime = 0;
        getRangesTime = 0;
        queryTime=0;
    }

    @Override
    public Set<String> executeQuery(final QueryCondition queryCondition) throws Throwable {
        initVariates();
        SRangeCondition condition = (SRangeCondition) queryCondition;
        return spatialQueryMultiThread(condition);
    }

    private void rangeQuery(BoundingBox queryBox, BoundingBox extendBox,List<ByteArrayRange> ranges, Table mainTable,int signatureSize,boolean isPolyline) throws InterruptedException {

        threadNum = ranges.size();
        int perSplitNum = ranges.size() / threadNum;//每个线程分到的range
        CountDownLatch countDownLatch = new CountDownLatch(threadNum);
        for (int i = 0; i < threadNum; i++) {
            int start = i * perSplitNum;
            int end = (i + 1) * perSplitNum;
            QueryJob queryJob = new NonPointQueryJob(mainTable,
                    ranges, start, end, queryBox, extendBox,
                    resultSet,signatureSize,
                    isPolyline, countDownLatch);
            queryWorkersPool.execute(queryJob);
        }
        countDownLatch.await();
    }

    public Set<String> spatialQueryMultiThread(SRangeCondition condition) throws Exception {

        BoundingBox queryBox = condition.getBoundingBox();
        boolean isPolyline = condition.getIsPolyline();
        int signatureSize = condition.getSignatureSize();
        long start = System.currentTimeMillis();
        // 根据二级索引表确定查询范围;
        int minLevel = getMinLevel(queryBox,secondaryTable);
        long end1 = System.currentTimeMillis();
        BoundingBox extendBox = getExtendBox(queryBox,minLevel);
        long end2 = System.currentTimeMillis();
        List<ByteArrayRange> ranges = getRanges(extendBox, minLevel);
        long end3 = System.currentTimeMillis();
        // range query
        rangeQuery(queryBox,extendBox, ranges, mainTable,signatureSize,isPolyline);
        long end4 = System.currentTimeMillis();
        getMinLevelTime = end1-start;
        getExtendBoxTime = end2-end1;
        getRangesTime = end3-end2;
        queryTime = end4-end3;
        totalTime = end4-start;
        return resultSet;
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

    public int getMinLevel(BoundingBox queryBox,Table secondaryIndexTable) throws IOException {
        Polygon polygon = queryBox.toJTSPolygon();
        GridGeometry gridPolygon = new GridPolygon(polygon,10);
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


    public BoundingBox getExtendBox(BoundingBox queryBox,int minLevel){

        double[] minGridSize = Grid.computeGridSize(minLevel);
        double detaLng = minGridSize[0]; double detaLat = minGridSize[1];
        // 扩展queryBox 保证查询正确性;
        double newMinLng = Math.max(queryBox.getMinLng()-detaLng,-180);
        double newMinLat = Math.max(queryBox.getMinLat()-detaLat,-90);

        double newMaxLng = queryBox.getMaxLng();
        double newMaxLat = queryBox.getMaxLat();
        BoundingBox extendBox = new BoundingBox(newMinLng,newMinLat,newMaxLng,newMaxLat);
        return extendBox;

    }


    public List<ByteArrayRange> getRanges(BoundingBox extendBox,int minLevel) throws IOException {

        List<ByteArrayRange> ranges = new ArrayList<>();
        List<GeoHash> initialGeoHash = GeohashCoding.getSearchHashes(extendBox);
        Set<GeoHash> geoHashes = new HashSet<>();
        // 如果查询范围的初始格网比minLevel还要小，则需要扩大格网;
        if (minLevel*2 < initialGeoHash.get(0).getSignificantBits()) {
            for (GeoHash hash : initialGeoHash) {
                geoHashes.add(GeoHash.fromBinaryString(hash.toBinaryString().substring(0, minLevel)));
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

    public static void main(String[] args) throws Throwable {

        Configuration conf = HBaseConfiguration.create();
        Connection connection = ConnectionFactory.createConnection(conf);
        //读取conf文件配置参数
        JSONObject jsonConf = JsonUtil.readLocalJSONFile(args[0]);
        String queryBoxType = args[1];
        List<BoundingBox> queryBoxes = new ArrayList<>();
        queryBoxes = generateQueryBoxByConf(jsonConf);
//        if (queryBoxType.equals("config")){
//            queryBoxes = generateQueryBoxByConf(jsonConf);
//        }
//        else {
//            String recordFile = args[2];
//            queryBoxes = generateQueryBoxByRecords(recordFile);
//        }
//        queryBoxes.add(new BoundingBox(9.723938942021048,52.35287415580825,9.724904537266411,52.35335122161004));

        String mainTableName = jsonConf.getString("mainTable");
        String secondaryTableName = jsonConf.getString("secondaryTable");
        Boolean isPolyline = jsonConf.getBoolean("isPolyline");
        String saveFile = jsonConf.getString("saveFile");
        int signatureSize = jsonConf.getIntValue("signatureSize");

        Table mainTable = connection.getTable(TableName.valueOf(mainTableName));
        Table secondaryTable = connection.getTable(TableName.valueOf(secondaryTableName));
        BufferedWriter out = new BufferedWriter(new FileWriter(saveFile));

        long AVGTotalTime = 0;
        long AVGGetMinLevelTime = 0;
        long AVGGetExtendBoxTime = 0;
        long AVGGetRangesTime = 0;
        long AVGQueryTime = 0;

        for (BoundingBox box: queryBoxes){
            NonPointSRQ query = new NonPointSRQ(mainTable, secondaryTable);
            SRangeCondition condition = SRangeCondition.fromBox(box,isPolyline);
            condition.setSignatureSize(signatureSize);
            Set<String> fineResults = query.executeQuery(condition);
            long queryTime = query.queryTime;
            long getRangesTime = query.getRangesTime;
            long totalTime = query.totalTime;
            long getExtendBoxTime = query.getExtendBoxTime;
            long getMinLevelTime = query.getMinLevelTime;
            JSONObject tempJson = new JSONObject();
            tempJson.put("minLng",box.getMinLng());
            tempJson.put("minLat",box.getMinLat());
            tempJson.put("maxLng",box.getMaxLng());
            tempJson.put("maxLat",box.getMaxLat());
            tempJson.put("queryTime",queryTime);
            tempJson.put("getExtendBoxTime",getExtendBoxTime);
            tempJson.put("totalTime",totalTime);
            tempJson.put("getRangesTime",getRangesTime);
            tempJson.put("getMinLevelTime",getMinLevelTime);
            tempJson.put("queryResult",fineResults.size());
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

