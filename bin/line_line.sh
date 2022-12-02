#!/usr/bin/env bash
spark-submit --conf spark.driver.extraClassPath=/opt/module/spark-2.4.4/HBaseJar/* --conf spark.executor.extraClassPath=/opt/module/spark-2.4.4/HBaseJar/* --conf spark.serializer=org.apache.spark.serializer.KryoSerializer --conf spark.driver.memory=5g  --conf spark.executor.memory=40g --conf spark.memory.fraction=0.2  --master spark://huawei5:7077 --class cn.edu.whu.App grid-analysis-1.0-SNAPSHOT.jar cellLine0 cellLine1 line0 line1 20