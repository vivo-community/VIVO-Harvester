@echo off
rmdir /s /q data 
rmdir /s /q logs 
tar zxf data.tar.gz

set HARVESTER_INSTALL_DIR=/usr/local/src/VIVO-Harvester
set CLASSPATH=%HARVESTER_INSTALL_DIR%/build/harvester.jar;%HARVESTER_INSTALL_DIR%/build/dependency/*
set HARVESTER_JAVA_OPTS=-Xms1024M -Xmx1024M

@java %HARVESTER_JAVA_OPTS% -cp %CLASSPATH% org.vivoweb.harvester.util.repo.JenaConnect -j harvested-data.model.xml -q "SELECT ?person ?name where { ?person <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Person> . ?person <http://www.w3.org/2000/01/rdf-schema#label> ?name }"
