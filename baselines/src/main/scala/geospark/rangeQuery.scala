package geospark

import com.vividsolutions.jts.geom.Envelope
import org.apache.log4j.{Level, Logger}
import org.apache.spark.serializer.KryoSerializer
import org.apache.spark.storage.StorageLevel
import org.apache.spark.{SparkConf, SparkContext}
import org.datasyslab.geospark.enums.{FileDataSplitter, IndexType}
import org.datasyslab.geospark.serde.GeoSparkKryoRegistrator
import org.datasyslab.geospark.spatialOperator.RangeQuery
import org.datasyslab.geospark.spatialRDD.{LineStringRDD, PolygonRDD}

import scala.collection.mutable.ListBuffer
import scala.io.{BufferedSource, Source}
import com.alibaba.fastjson.JSON


/**
 * Range Queries For Different Geometric Objects.
 */

object rangeQuery {
    def main(args: Array[String]): Unit = {

        val conf = new SparkConf().setAppName("GeoSpark Range Queries")
        conf.set("spark.serializer", classOf[KryoSerializer].getName)
        conf.set("spark.kryo.registrator", classOf[GeoSparkKryoRegistrator].getName)
        val sc = new SparkContext(conf)
        Logger.getLogger("org").setLevel(Level.WARN)
        Logger.getLogger("akka").setLevel(Level.WARN)
        val queryWindows = parseQuery(args(0))
        val queryType = args(1)
        println("queryWindows:"+queryWindows.size)
        if (queryType=="line") {spatialRangeLineString(sc,queryWindows)}
        else if (queryType=="polygon") {spatialRangePolygon(sc,queryWindows)}
        else {println("query type Error: polygon or line")}
        sc.stop()

    }

    def parseQuery(queryFile:String)={
        val queryWindow :ListBuffer[Envelope] = ListBuffer()
        val source: BufferedSource = Source.fromFile(queryFile)
        for (line <- source.getLines()){

            val jsonObj = JSON.parseObject(line)
            if (jsonObj.containsKey("minLng")) {
                val minLng = jsonObj.getDouble("minLng")
                val minLat = jsonObj.getDouble("minLat")
                val maxLng = jsonObj.getDouble("maxLng")
                val maxLat = jsonObj.getDouble("maxLat")
                val tempWindow = new Envelope(minLng,maxLng,minLat,maxLat)
                queryWindow+=tempWindow
            }
        }
        source.close()
        queryWindow
    }

    def spatialRangeLineString(sc: SparkContext, queryWindows:ListBuffer[Envelope]) {
        println("************************ LineString Range Queries **************************************")
        val startLoad =System.currentTimeMillis()
        val objectRDD = new LineStringRDD(sc,
            "/data/openStreetMap/linestrings_72M.csv",
            FileDataSplitter.WKT, false,
            64, StorageLevel.MEMORY_ONLY)
        println("rawSpatialRDD First: "+objectRDD.rawSpatialRDD.first())
        val endLoad =System.currentTimeMillis()
        println("loadTime:"+(endLoad-startLoad)+"ms")

        objectRDD.buildIndex(IndexType.RTREE, false)
        objectRDD.indexedRawRDD.persist(StorageLevel.MEMORY_ONLY)
        // 打印会溢出内存
//        System.out.println("indexedRawRDD First: "+objectRDD.indexedRawRDD.first())
        val endIndex =System.currentTimeMillis()
        println("indexTime: "+(endIndex-endLoad)+"ms")

        // 首次查询用于缓存索引
        println("Start First Query.............")
        val startFirstQuery =System.currentTimeMillis()
        val firstQueryWindow = new Envelope(-77.61528718251162, -76.61528718251162, 38.60831328918238, 39.60831328918238)
        val firstCount = RangeQuery.SpatialRangeQuery(objectRDD, firstQueryWindow, true, true).count()
        val endFirstQuery =System.currentTimeMillis()
        println("firstQuery: "+firstCount+"\tcost time: "+(endFirstQuery-startFirstQuery))
        println("End First Query----------------------------------")

        var oneTime = 0L
        var totalTime = 0L
        var oneCount = 0L
        var totalCount = 0L
        for (queryWindow <- queryWindows) {
            val startTime =System.currentTimeMillis()
            oneCount = RangeQuery.SpatialRangeQuery(objectRDD, queryWindow, true, true).count()
            val endTime =System.currentTimeMillis()
            oneTime = endTime-startTime
            totalTime+=oneTime
            println("oneCount: " + oneCount+"\toneTime: " + oneTime)
            println("--------------------------------------------")
            totalCount+=oneCount
        }
        println("average Time: " + (totalTime/queryWindows.size))

        objectRDD.indexedRawRDD.unpersist()
        objectRDD.rawSpatialRDD.unpersist()

        println("***********************************************************************************")
    }

    def spatialRangePolygon(sc: SparkContext, queryWindows:ListBuffer[Envelope]) {

        println("************************ POLYGON Range Queries **************************************")
        val startLoad = System.currentTimeMillis()
        val objectRDD = new PolygonRDD(sc,
            "/data/openStreetMap/buildings_114M.csv",
            0, 8,
            FileDataSplitter.WKT,
            false,
            64,
            StorageLevel.MEMORY_ONLY)

        println("rawSpatialRDD First: "+objectRDD.rawSpatialRDD.first())
        val endLoad = System.currentTimeMillis()
        println("loadTime:"+(endLoad-startLoad)+"ms")

        objectRDD.buildIndex(IndexType.RTREE, false)
        objectRDD.indexedRawRDD.persist(StorageLevel.MEMORY_ONLY)
//        println("indexedRawRDD First: "+objectRDD.indexedRawRDD.first())
        val endIndex = System.currentTimeMillis()
        println("indexTime:"+(endIndex-endLoad)+"ms")

        // 首次查询用于缓存索引
        println("Start First Query.............")
        val startFirstQuery =System.currentTimeMillis()
        val firstQueryWindow = new Envelope(-77.61528718251162, -76.61528718251162, 38.60831328918238, 39.60831328918238)
        val firstCount = RangeQuery.SpatialRangeQuery(objectRDD, firstQueryWindow, true, true).count()
        val endFirstQuery =System.currentTimeMillis()
        println("firstQuery: "+firstCount+"\tcost time: "+(endFirstQuery-startFirstQuery))
        println("End First Query----------------------------------")

        var oneTime = 0L
        var totalTime = 0L
        var oneCount = 0L
        var totalCount = 0L

        for (queryWindow <- queryWindows) {
            val startTime =System.currentTimeMillis()
            oneCount = RangeQuery.SpatialRangeQuery(objectRDD, queryWindow, true, true).count()
            val endTime =System.currentTimeMillis()
            oneTime = endTime-startTime
            totalTime+=oneTime
            println("oneCount: " + oneCount+"\toneTime: " + oneTime)
            println("--------------------------------------------")
            totalCount+=oneCount
        }
        println("average Time: " + (totalTime/queryWindows.size))

        objectRDD.indexedRawRDD.unpersist()
        objectRDD.rawSpatialRDD.unpersist()

        println("***********************************************************************************")

    }

}
