package ingest;

import com.google.common.collect.Iterators;
import com.google.common.primitives.Longs;
import common.Constants;
import geoobject.STGeometryTypeEnum;
import geoobject.STPoint;
import geoobject.geometry.Grid;
import geoobject.geometry.GridGeometry;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKBWriter;
import scala.Tuple2;
import serialize.SerializePolygon;
import serialize.SerializeRowKey;
import serialize.SerializeSketch;
import util.BulkloadUtil;
import java.io.Serializable;
import java.util.*;

public class BulkLoadDataset implements Serializable {

    public void bulkLoadToMainIndexTable(Configuration conf, String tableNameStr, JavaRDD<GridGeometry> gridGeometryRDD) throws Exception{

        JavaPairRDD<ImmutableBytesWritable, KeyValue> hFileRDD = gridGeometryRDD
                .mapPartitions(iterator -> {
                    // WKBWriter wkbWriter = new WKBWriter();
                    return Iterators.transform(iterator, girdGeom -> {
                        String XZ2Index = girdGeom.getXZ2Index();
                        String newIndex = indexTrans(XZ2Index);
                        String geomId = girdGeom.getId();
                        BitSet signature = girdGeom.getSignature();
                        Geometry geometry = girdGeom.getGeometry();
                        Envelope geomMBR = geometry.getEnvelopeInternal();
                        // 序列化rowKey;
                        SerializeRowKey serializerowKey = new SerializeRowKey.Builder().setXZ2Index(newIndex).setId(geomId).build();
                        byte[] rowKey = serializerowKey.getData();
                        ImmutableBytesWritable ibw = new ImmutableBytesWritable(rowKey);
                        // 序列化geometry;
                        // byte[] byteGeom = wkbWriter.write(geometry);
                        Coordinate[] coordinates = geometry.getCoordinates();
                        List<STPoint> lngLatList = new ArrayList<>();
                        for (Coordinate coordinate : coordinates) {
                            double lng = coordinate.getX();
                            double lat = coordinate.getY();
                            lngLatList.add(new STPoint(lng, lat));
                        }
                        SerializePolygon polygon = new SerializePolygon
                                .Builder(STGeometryTypeEnum.POLYGON, lngLatList.size())
                                .lngLatList(lngLatList)
                                .id(geomId)
                                .build();

                        byte[] bytePolygon = polygon.getData();
                        // 序列化Sketch;
                        SerializeSketch serializesketch = new SerializeSketch.Builder()
                                .setMBR(geomMBR)
                                .setSignature(signature)
                                .build();

                        byte[] byteSketch = serializesketch.getData();
                        KeyValue geomKV = new KeyValue((rowKey),
                                Bytes.toBytes(Constants.MAIN_INDEX_TABLE_FAMILY),
                                Bytes.toBytes(Constants.MAIN_INDEX_TABLE_QUALIFIER_GEOM),
                                bytePolygon);
                        KeyValue sketchKV = new KeyValue((rowKey),
                                Bytes.toBytes(Constants.MAIN_INDEX_TABLE_FAMILY),
                                Bytes.toBytes(Constants.MAIN_INDEX_TABLE_QUALIFIER_SKETCH),
                                byteSketch);
                        List<KeyValue> listKV = new ArrayList<>();
                        listKV.add(geomKV);
                        listKV.add(sketchKV);
                        return new Tuple2<>(ibw, listKV);
                    });
                })
                .flatMapToPair(tuple2 -> {
                    List<Tuple2<ImmutableBytesWritable, KeyValue>> kvList = new ArrayList<>();
                    ImmutableBytesWritable key = tuple2._1;
                    for (KeyValue value : tuple2._2) {
                        kvList.add(new Tuple2<>(key, value));
                    }
                    return kvList.iterator();
                })
                .sortByKey();
        System.out.println("*************3:bulkloading MainIndex size:"+hFileRDD.count());
        BulkloadUtil.bulkLoad(hFileRDD, conf, tableNameStr);

    }

    public void bulkLoadToSecondaryIndexTable(Configuration conf, String tableNameStr, JavaRDD<GridGeometry> gridGeometryRDD) throws Exception{

        JavaPairRDD<ImmutableBytesWritable, KeyValue> hFileRDD = gridGeometryRDD
                .flatMapToPair(girdGeom -> {
                    HashMap<Long, Integer> indexMap = new HashMap<>();
                    Map<String, Grid>  XZ2Grids = girdGeom.getXZ2Grids();
                    Geometry geometry = girdGeom.getGeometry();
                    for (Grid grid:XZ2Grids.values()) {
                        if (!geometry.disjoint(grid.getPolygon())){
                            String index = grid.getBinaryStrIndex();
                            int level = index.length()/2;
                            long rowKey = Long.parseLong(index.substring(0, 20),2);
                            indexMap.merge(rowKey, level, (a, b) -> Math.min(b, a));
                        }
                    }
                    List<Tuple2<Long, Integer>> pairList = new ArrayList<>();
                    for (Map.Entry<Long,Integer> kv:indexMap.entrySet()){
                        pairList.add(new Tuple2<>(kv.getKey(), kv.getValue()));
                    }
                    return pairList.iterator();
                })
                .reduceByKey(Math::min)
                .mapToPair(tuple->{
                    byte[] rowKey = Bytes.toBytes(tuple._1());
                    int level = tuple._2();
                    byte[] value = new byte[1];
                    value[0] = (byte) level;
//                    byte[] rowKey = Longs.toByteArray(tuple._1());
//                    byte[] value = String.valueOf(tuple._2()).getBytes();
                    ImmutableBytesWritable ibw = new ImmutableBytesWritable(rowKey);
                    KeyValue kv = new KeyValue(rowKey,
                            Bytes.toBytes(Constants.SECONDARY_INDEX_TABLE_FAMILY),
                            Bytes.toBytes(Constants.SECONDARY_INDEX_TABLE_QUALIFIER_MIN),
                            value);
                    return new Tuple2<>(ibw, kv);
                })
                .sortByKey();
        System.out.println("*************3:bulkloading SecondaryIndex size:"+hFileRDD.count());
        BulkloadUtil.bulkLoad(hFileRDD, conf, tableNameStr);

    }

    public String indexTrans(String index) {
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

}
