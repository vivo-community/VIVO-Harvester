#!/bin/bash

# see example-scripts/README.txt for information about HARVESTER_JAVA_OPTS

java $HARVESTER_JAVA_OPTS org.vivoweb.harvester.score.Score -X score-departments.conf.xml
