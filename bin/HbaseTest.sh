#!/usr/bin/env bash

HADDOOP_CLASSPATH=`hadoop classpath` && HBASE_CLASSPATH=`hbase classpath` && \
java -cp /home/yxy/gridMesa/jar/gridManagement-1.0-SNAPSHOT.jar:${HADDOOP_CLASSPATH}:${HBASE_CLASSPATH} \
 util.HBaseUtil