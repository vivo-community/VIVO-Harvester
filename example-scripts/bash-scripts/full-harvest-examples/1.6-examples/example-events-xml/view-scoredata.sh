#!/bin/bash

# see example-scripts/README.txt for information about HARVESTER_JAVA_OPTS


export HARVESTER_INSTALL_DIR=/usr/local/src/VIVO-Harvester
export CLASSPATH=$HARVESTER_INSTALL_DIR/build/harvester.jar:$HARVESTER_INSTALL_DIR/build/dependency/*
export HARVESTER_JAVA_OPTS=
java $HARVESTER_JAVA_OPTS org.vivoweb.harvester.util.repo.JenaConnect -X view-scoredata.conf.xml 
