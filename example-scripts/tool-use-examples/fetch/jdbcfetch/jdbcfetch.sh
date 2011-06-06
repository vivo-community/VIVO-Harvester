#!/bin/bash

# see example-scripts/README.txt for information about HARVESTER_JAVA_OPTS

DESTINATIONARGS="--outputOverride --outputOverride --outputOverride"

QUERY1="--tableName --id --query"

LINEARGUMENTS="--driver --connection --username --password --tableName --validTableType"

java $HARVESTER_JAVA_OPTS org.vivoweb.harvester.fetch.JDBCFetch $LINEARGUMENTS

#$JDBCFetch -X config/tasks/example.jdbcfetch.xml --connection $CLONEDBURL -o $TFRH -OfileDir=$RAWRHDIR
