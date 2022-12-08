package ingest;

import com.alibaba.fastjson.JSONObject;
import common.Constants;
import common.readGeometry;
import geoobject.geometry.GridGeometry;
import geoobject.geometry.GridGeometryFactory;
import javafx.util.Pair;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.sql.SparkSession;
import org.locationtech.jts.geom.Geometry;
import scala.Tuple2;
import util.HBaseUtil;
import util.JsonUtil;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

public class statistic implements Serializable {

    public static void main(String[] args) {

        SparkSession sparkSession = SparkSession.builder().getOrCreate();
        SparkContext sc = sparkSession.sparkContext();
        JavaSparkContext javaSparkContext = new JavaSparkContext(sc);
        Configuration configuration = HBaseConfiguration.create();

        JSONObject jsonObject = JsonUtil.readLocalJSONFile(args[0]);
        Broadcast<JSONObject> argsJson = javaSparkContext.broadcast(jsonObject);

        String inputPath = argsJson.getValue().getString("inputPath");//输入文件

        JavaRDD<Pair<String,Geometry>> geometryRDD = javaSparkContext.textFile(inputPath).repartition(120).mapPartitions(new readGeometry()).filter(Objects::nonNull);
        System.out.println("*************1:load geometryRDD size:"+geometryRDD.count());
        JavaRDD<GridGeometry> gridGeometryRDD = geometryRDD.map(pair-> GridGeometryFactory.createGridGeometry(pair.getKey(),pair.getValue(),argsJson.getValue()))
                .filter(gridGeom->gridGeom.getSplitGrids().size()>0);

        Map<Integer, Integer> levelNums = gridGeometryRDD
                .mapToPair(gridGeometry -> {
                    int level = gridGeometry.getXZ2Index().length();
                    return new Tuple2<>(level, 1); })
                .reduceByKey(Integer::sum)
                .sortByKey().collectAsMap();
        for (Map.Entry kv:levelNums.entrySet()) {
            System.out.println("level:"+kv.getKey()+"\tnums:"+kv.getValue());
        }
    }

}
