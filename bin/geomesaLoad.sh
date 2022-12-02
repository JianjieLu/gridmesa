#!/usr/bin/env bash

HADDOOP_CLASSPATH=`hadoop classpath` && HBASE_CLASSPATH=`hbase classpath` && \
java -cp ./geomesa-test-1.0-SNAPSHOT.jar:${HADDOOP_CLASSPATH}:${HBASE_CLASSPATH} \
 GeoMesaIngest "$1"