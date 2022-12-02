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
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.sql.SparkSession;
import org.locationtech.jts.geom.Geometry;
import util.HBaseUtil;
import util.JsonUtil;

import java.util.Objects;

public class Ingestion {

    public static void main(String[] args) throws Exception{

        SparkSession sparkSession = SparkSession.builder().getOrCreate();
        SparkContext sc = sparkSession.sparkContext();
        JavaSparkContext javaSparkContext = new JavaSparkContext(sc);
        Configuration configuration = HBaseConfiguration.create();

        JSONObject jsonObject = JsonUtil.readLocalJSONFile(args[0]);
        Broadcast<JSONObject> argsJson = javaSparkContext.broadcast(jsonObject);

        String inputPath = argsJson.getValue().getString("inputPath");//输入文件
        String mainIndexTable = argsJson.getValue().getString("mainIndexTable");//主索引表
        String secondaryIndexTableName = argsJson.getValue().getString("secondaryIndexTable");//二级索引表

        JavaRDD<Pair<String,Geometry>> geometryRDD = javaSparkContext.textFile(inputPath).repartition(120).mapPartitions(new readGeometry()).filter(Objects::nonNull);
        System.out.println("*************1:load geometryRDD size:"+geometryRDD.count());
        JavaRDD<GridGeometry> gridGeometryRDD = geometryRDD.map(pair-> GridGeometryFactory.createGridGeometry(pair.getKey(),pair.getValue(),argsJson.getValue()))
                .filter(gridGeom->gridGeom.getSplitGrids().size()>0);
        System.out.println("*************2:create gridGeometryRDD size:"+gridGeometryRDD.count());
        BulkLoadDataset bulkLoad = new BulkLoadDataset();
        System.out.println("*************bulkloading...***********");
        try (Connection connection = ConnectionFactory.createConnection(configuration);){
            HBaseUtil.createTable(connection, mainIndexTable, new String[]{Constants.MAIN_INDEX_TABLE_FAMILY});
            HBaseUtil.createTable(connection, secondaryIndexTableName, new String[]{Constants.SECONDARY_INDEX_TABLE_FAMILY});
        }

        bulkLoad.bulkLoadToMainIndexTable(configuration, mainIndexTable,gridGeometryRDD);
        System.out.println("*************3:bulkloading MainIndex success");

        bulkLoad.bulkLoadToSecondaryIndexTable(configuration, secondaryIndexTableName,gridGeometryRDD);
        System.out.println("*************4:bulkloading SecondaryIndex");

    }

}
