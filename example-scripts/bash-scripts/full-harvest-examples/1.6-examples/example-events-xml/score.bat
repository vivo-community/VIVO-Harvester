set HARVESTER_INSTALL_DIR=/usr/local/src/VIVO-Harvester
set HARVEST_NAME=example-events

set CLASSPATH=%HARVESTER_INSTALL_DIR%/build/harvester.jar;%HARVESTER_INSTALL_DIR%/build/dependency/*
set HARVESTER_JAVA_OPTS=-Xms1024M -Xmx1024M
@java %HARVESTER_JAVA_OPTS% -cp %CLASSPATH% org.vivoweb.harvester.score.Score -X score-events.config.xml
