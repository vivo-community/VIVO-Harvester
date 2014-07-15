#!/bin/bash

# see example-scripts/README.txt for information about HARVESTER_JAVA_OPTS
rm -rf data logs
export HARVESTER_INSTALL_DIR=/usr/local/src/VIVO-Harvester
export CLASSPATH=$HARVESTER_INSTALL_DIR/build/harvester.jar:$HARVESTER_INSTALL_DIR/build/dependency/*
export HARVESTER_JAVA_OPTS=
java $HARVESTER_JAVA_OPTS org.vivoweb.harvester.fetch.WOSFetch -X wosfetch.conf.xml

