#!/usr/bin/env bash

HADDOOP_CLASSPATH=`hadoop classpath` && HBASE_CLASSPATH=`hbase classpath` && \
java -cp ./grid-analysis-infrastructure-1.0-SNAPSHOT.jar:${HADDOOP_CLASSPATH}:${HBASE_CLASSPATH} \
 cn.edu.whu.infrastructure.query.SRQPolygon ./config/query.json