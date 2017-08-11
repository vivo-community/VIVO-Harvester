#!/bin/bash

export HARVESTER_INSTALL_DIR=/usr/local/src/VIVO-Harvester
export CLASSPATH=$HARVESTER_INSTALL_DIR/build/harvester.jar:$HARVESTER_INSTALL_DIR/build/dependency/*
export HARVESTER_JAVA_OPTS=
java $HARVESTER_JAVA_OPTS org.vivoweb.harvester.util.MergeUriTool -p  http://localhost:8080/vivo/individual/n826084493 -d http://localhost:8080/vivo/individual/n528791970 -m vivo.model.xml
