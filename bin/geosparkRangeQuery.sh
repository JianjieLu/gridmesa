#!/bin/bash

function rangeQuery(){
  echo "running rangeQuery"
  spark-submit \
    --master spark://huawei5:7077 \
    --deploy-mode client \
    --executor-cores 5 \
    --executor-memory 4g \
    --driver-memory 2g \
    --conf spark.yarn.executor.memoryOverhead=409 \
    --conf spark.driver.extraClassPath="/opt/module/spark-2.4.4/HBaseJar/*" \
    --conf spark.executor.extraClassPath="/opt/module/spark-2.4.4/HBaseJar/*" \
    --files /opt/module/hbase-1.4.9/conf/hbase-site.xml \
    --class geospark.rangeQuery \
    ./baselines-1.0-SNAPSHOT.jar ./queryRecords.txt
}

eval rangeQuery


