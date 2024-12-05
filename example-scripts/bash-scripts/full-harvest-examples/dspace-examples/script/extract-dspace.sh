#!/bin/bash
 
###################################################################
# Script Name   :
# Description   :
# Args          : 
# Author       	: Michel Héon PhD
# Institution   : Université du Québec à Montréal
# Copyright     : Université du Québec à Montréal (c) 2022
# Email         : heon.michel@uqam.ca
###################################################################
export SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd -P)"
source $SCRIPT_DIR/00-env.sh
cd $WORKDIR
CLASSPATH=$(find "$LIB" -name '*.jar' -printf '%p:' | sed 's/:$//')
export PRJ_CP=$WORKDIR/../../../../build/classes:$WORKDIR/../../../../build/dependency/*:
export JVM_ARGS=""


echo $PRJ_CP | tr ':' '\n'
echo "============================================"

cp $WORKDIR/resources/harvester-dspace.conf $WORKDIR/../../../../build/classes/harvester.conf
java $JVM_ARGS -cp "$PRJ_CP" org.vivoweb.harvester.extractdspace.ExtractDSpace

