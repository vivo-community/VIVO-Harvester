#!/bin/bash

# setup
rm -rf data logs
tar zxf data.tar.gz

export HARVESTER_INSTALL_DIR=/usr/local/src/VIVO-Harvester
export CLASSPATH=$HARVESTER_INSTALL_DIR/build/harvester.jar:$HARVESTER_INSTALL_DIR/build/dependency/*
export HARVESTER_JAVA_OPTS=
java $HARVESTER_JAVA_OPTS org.vivoweb.harvester.fetch.D2RMapFetch -X d2rmapfetch.conf.xml
