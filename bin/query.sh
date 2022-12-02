#!/usr/bin/env bash

HADDOOP_CLASSPATH=`hadoop classpath` && HBASE_CLASSPATH=`hbase classpath` && \
java -cp ./grid-analysis-infrastructure-1.0-SNAPSHOT.jar:${HADDOOP_CLASSPATH}:${HBASE_CLASSPATH} \
 cn.edu.whu.infrastructure.query.QueryTest linestringsSample \
"POLYGON((-114.50 42.00,-114.00 42.00, -114.00 42.50, -114.50 42.50,-114.50 42.00))" \
false