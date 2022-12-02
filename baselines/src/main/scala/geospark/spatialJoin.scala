package geospark

import org.apache.log4j.{Level, Logger}
import org.apache.spark.serializer.KryoSerializer
import org.apache.spark.storage.StorageLevel
import org.apache.spark.{SparkConf, SparkContext}
import org.datasyslab.geospark.enums.{FileDataSplitter, GridType, IndexType}
import org.datasyslab.geospark.serde.GeoSparkKryoRegistrator
import org.datasyslab.geospark.spatialOperator.JoinQuery
import org.datasyslab.geospark.spatialRDD.{LineStringRDD, PointRDD, PolygonRDD, RectangleRDD}

/*
 * Spatial Joins between different geometric objects using KDB and Quadtree partitioning
 */

object spatialJoin {

  def main(args: Array[String]) {
    val conf = new SparkConf().setAppName("GeoSpark Spatial Joins")
    conf.set("spark.serializer", classOf[KryoSerializer].getName)
    conf.set("spark.kryo.registrator", classOf[GeoSparkKryoRegistrator].getName)
//    conf.setMaster("local")
    val sc = new SparkContext(conf)
    Logger.getLogger("org").setLevel(Level.INFO)
    Logger.getLogger("akka").setLevel(Level.INFO)
//    val points = "C:\\user\\yxy\\data\\OpenStreetMap\\points_200M_sample.csv"
//    val polygons = "C:\\user\\yxy\\data\\OpenStreetMap\\buildings_114M_sample.csv"
    val points = "/data/openStreetMap/points_200M.csv"
    val polygons = "/data/openStreetMap/buildings_114M.csv"
    val rectangles = "/data/openStreetMap/rectangles_114M.csv"
    val linestrings = "/data/openStreetMap/linestrings_72M.csv"

    val quad = GridType.QUADTREE
    val kdb = GridType.KDBTREE
    val idx = IndexType.RTREE
    val numPartitions = 1024

    /*println("******************************* KDB Partitioning *******************************")
    runSpatialJoin(kdb,"point","point")
    runSpatialJoin(kdb,"point","linestring")
    runSpatialJoin(kdb,"point","polygon")
    runSpatialJoin(kdb,"point","rectangle")
    runSpatialJoin(kdb,"linestring","linestring")
    runSpatialJoin(kdb,"linestring","polygon")
    runSpatialJoin(kdb,"linestring","rectangle")
    runSpatialJoin(kdb,"rectangle","rectangle")
    runSpatialJoin(kdb,"rectangle","polygon")
    runSpatialJoin(kdb,"polygon","polygon")
    println("*************************** Finished KDB Partitioning ***************************")*/
    println("******************************* quad Partitioning *******************************")
    //    runSpatialJoin(quad, "point", "point")
    //    runSpatialJoin(quad, "point", "linestring")
    runSpatialJoin(quad, "point", "polygon")
    //    runSpatialJoin(quad, "point", "rectangle")
    runSpatialJoin(quad, "linestring", "linestring")
    runSpatialJoin(quad, "linestring", "polygon")
    //    runSpatialJoin(quad, "linestring", "rectangle")
    //    runSpatialJoin(quad, "rectangle", "rectangle")
    //    runSpatialJoin(quad, "rectangle", "polygon")
    //    runSpatialJoin(quad, "polygon", "polygon")
    println("*************************** Finished quad Partitioning ***************************")

    def runSpatialJoin(partitioningScheme: org.datasyslab.geospark.enums.GridType, leftrdd: String, rightrdd: String) {

      var count = 0L
      val beginTime = System.currentTimeMillis()
      var t0 = 0L
      var t1 = 0L

      println("******************************** " + leftrdd + " and " + rightrdd + " spatial join ********************************")

      t0 = System.nanoTime()
      val leftRDD = leftrdd match {

        case "point" => new PointRDD(sc, points, FileDataSplitter.CSV, false, numPartitions, StorageLevel.MEMORY_ONLY)
        case "linestring" => new LineStringRDD(sc, linestrings, FileDataSplitter.WKT, false, numPartitions, StorageLevel.MEMORY_ONLY)
        case "rectangle" => new RectangleRDD(sc, rectangles, FileDataSplitter.WKT, false, numPartitions, StorageLevel.MEMORY_ONLY)
        case "polygon" => new PolygonRDD(sc, polygons, FileDataSplitter.WKT, false, numPartitions, StorageLevel.MEMORY_ONLY)

      }
      leftRDD.spatialPartitioning(partitioningScheme)

      leftRDD.buildIndex(idx,true)

      leftRDD.indexedRDD.persist(StorageLevel.MEMORY_ONLY)

      leftRDD.spatialPartitionedRDD.persist(StorageLevel.MEMORY_ONLY)

      val c1 = leftRDD.spatialPartitionedRDD.count()

      val c2 = leftRDD.indexedRDD.count()

      leftRDD.rawSpatialRDD.unpersist()

      leftRDD.spatialPartitionedRDD.unpersist()

      val rightRDD = rightrdd match {

        case "point" => new PointRDD(sc, points, FileDataSplitter.CSV, false, numPartitions, StorageLevel.MEMORY_ONLY)
        case "linestring" => new LineStringRDD(sc, linestrings, FileDataSplitter.WKT, false, numPartitions, StorageLevel.MEMORY_ONLY)
        case "rectangle" => new RectangleRDD(sc, rectangles, FileDataSplitter.WKT, false, numPartitions, StorageLevel.MEMORY_ONLY)
        case "polygon" => new PolygonRDD(sc, polygons, FileDataSplitter.WKT, false, numPartitions, StorageLevel.MEMORY_ONLY)

      }

      rightRDD.spatialPartitioning(leftRDD.getPartitioner)

      rightRDD.spatialPartitionedRDD.persist(StorageLevel.MEMORY_ONLY)

      val c3 = rightRDD.spatialPartitionedRDD.count()

      t1 = System.nanoTime()

      val rpTime = (t1 - t0)/1E9

      println("Total Reading and Partitioning Time: " + rpTime + " sec")

      rightRDD.rawSpatialRDD.unpersist()

      t0 = System.nanoTime()

      count = JoinQuery.SpatialJoinQuery(leftRDD, rightRDD, true, false).count()

      t1 = System.nanoTime()
      val join_time = (t1 - t0) / 1E9
      println("Join Time: " + join_time + " sec")

      val total_time = rpTime + join_time

      println("Total Join Time: " + total_time + " sec")
      println("count: " + count)

      println("********************************************************************************************")

      leftRDD.spatialPartitionedRDD.unpersist()
      leftRDD.indexedRDD.unpersist()
      rightRDD.spatialPartitionedRDD.unpersist()
    }
  }
}

