@echo off
rmdir /s /q data 
rmdir /s /q logs 
tar zxf data.tar.gz

set HARVESTER_INSTALL_DIR=/usr/local/src/VIVO-Harvester
set CLASSPATH=%HARVESTER_INSTALL_DIR%/build/harvester.jar;%HARVESTER_INSTALL_DIR%/build/dependency/*
set HARVESTER_JAVA_OPTS=-Xms1024M -Xmx1024M

@java %HARVESTER_JAVA_OPTS% -cp %CLASSPATH% org.vivoweb.harvester.fetch.JDBCFetch -X jdbcfetch.conf.xml

