#!/bin/bash

rm -rf data
tar zxf data.tar.gz
export HARVESTER_INSTALL_DIR=/usr/local/src/VIVO-Harvester
export CLASSPATH=$HARVESTER_INSTALL_DIR/build/harvester.jar:$HARVESTER_INSTALL_DIR/build/dependency/*
#$HARVESTER_INSTALL_DIR/bin/harvester-h2console -help
$HARVESTER_INSTALL_DIR/bin/harvester-h2console -web -browser
