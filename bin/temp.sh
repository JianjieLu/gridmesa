#!/usr/bin/env bash

spark-submit \
--master spark://huawei5:7077 \
--class cn.edu.whu.ingest.Ingestion \
--conf spark.serializer=org.apache.spark.serializer.KryoSerializer \
--conf spark.driver.memory=2g \
--conf spark.executor.memory=6g \
--conf spark.driver.extraClassPath="/opt/module/spark-2.4.4/HBaseJar/*:/opt/module/hadoop-2.7.7/etc/hadoop/:/opt/module/hbase-1.4.9/conf/" \
--conf spark.executor.extraClassPath="/opt/module/spark-2.4.4/HBaseJar/*:/opt/module/hadoop-2.7.7/etc/hadoop/:/opt/module/hbase-1.4.9/conf/" \
grid-analysis-1.0-SNAPSHOT.jar /data/openStreetMap/linestrings_72M.csv_1000000 linestrings_test
