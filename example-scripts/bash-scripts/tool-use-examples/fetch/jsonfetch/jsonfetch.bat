@echo off

set HARVESTER_INSTALL_DIR=/usr/local/src/VIVO-Harvester
set CLASSPATH=%HARVESTER_INSTALL_DIR%/build/harvester.jar;%HARVESTER_INSTALL_DIR%/build/dependency/*
set HARVESTER_JAVA_OPTS=-Xms1024M -Xmx1024M

@java %HARVESTER_JAVA_OPTS% -cp %CLASSPATH% org.vivoweb.harvester.fetch.JSONFetch -X jsonfetch.config.xml

