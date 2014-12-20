@echo off
IF exist data (
  rmdir /s /q data
)

IF exist logs (
  rmdir /s /q logs
)

IF exist previous-harvest (
  rmdir /s /q previous-harvest
)

tar zxf data.tar.gz

set HARVESTER_INSTALL_DIR=/usr/local/src/VIVO-Harvester
set CLASSPATH=%HARVESTER_INSTALL_DIR%/build/harvester.jar;%HARVESTER_INSTALL_DIR%/build/dependency/*
set HARVESTER_JAVA_OPTS=-Xms1024M -Xmx1024M

@java %HARVESTER_JAVA_OPTS% -cp %CLASSPATH% org.vivoweb.harvester.diff.Diff -X diff.conf.xml
