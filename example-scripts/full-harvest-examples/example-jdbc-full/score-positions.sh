#!/bin/bash

# see example-scripts/README.txt for information about HARVESTER_JAVA_OPTS

# clear H2 score data Model
#rm -rf $SCOREDATADIR

# Clear old H2 temp copy of input
#$JenaConnect -Jtype=tdb -JdbDir=$TEMPCOPYDIR -JmodelName=http://vivoweb.org/harvester/model/scoring#inputClone -t

java $HARVESTER_JAVA_OPTS org.vivoweb.harvester.score.Score -X score-positions.conf.xml
