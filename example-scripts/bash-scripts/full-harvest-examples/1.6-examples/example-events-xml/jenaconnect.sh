#!/bin/bash

# see example-scripts/README.txt for information about HARVESTER_JAVA_OPTS


export HARVESTER_INSTALL_DIR=/usr/local/src/VIVO-Harvester
export CLASSPATH=$HARVESTER_INSTALL_DIR/build/harvester.jar:$HARVESTER_INSTALL_DIR/build/dependency/*
export HARVESTER_JAVA_OPTS=
java $HARVESTER_JAVA_OPTS org.vivoweb.harvester.util.repo.JenaConnect -X jenaconnect.conf.xml 
#java $HARVESTER_JAVA_OPTS org.vivoweb.harvester.util.repo.JenaConnect -j harvested-data.model.xml -q "SELECT ?person ?name where { ?person <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Person> . ?person <http://www.w3.org/2000/01/rdf-schema#label> ?name }"
