@echo off
REM Copyright (c) 2010-2011 VIVO Harvester Team. For full list of contributors, please see the AUTHORS file provided.
REM All rights reserved.
REM This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html

REM  set to the directory where the harvester was installed or unpacked
REM  HARVESTER_INSTALL_DIR is set to the location of the installed harvester
REM 	If the deb file was used to install the harvester then the
REM 	directory should be set to /usr/share/vivo/harvester which is the
REM 	current location associated with the deb installation.
REM 	Since it is also possible the harvester was installed by
REM 	uncompressing the tar.gz the setting is available to be changed
REM 	and should agree with the installation location
set HARVESTER_INSTALL_DIR=/usr/local/src/VIVO-Harvester

REM  set the CLASSPATH and HARVESTER_JAVA_OPTS to be used by all commands

set CLASSPATH=%HARVESTER_INSTALL_DIR%/build/harvester.jar;%HARVESTER_INSTALL_DIR%/build/dependency/*

set HARVESTER_JAVA_OPTS=-Xms1024M -Xmx1024M
REM set HARVESTER_JAVA_OPTS=

REM  Supply the location of the detailed log file which is generated during the script.
REM 	If there is an issue with a harvest, this file proves invaluable in finding
REM 	a solution to the problem. It has become common practice in addressing a problem
REM 	to request this file. The passwords and usernames are filtered out of this file
REM 	to prevent these logs from containing sensitive information.

IF not exist logs (
  mkdir logs
)

IF exist data ( rm -rf data )
@java %HARVESTER_JAVA_OPTS% -Dharvester-task=Fetch -cp %CLASSPATH% -Dharvester-task=Fetch org.vivoweb.harvester.fetch.linkeddata.LinkedDataFetch  -X linkeddata-fetch.config.xml
if %errorlevel% neq 0 exit /b %errorlevel% 



