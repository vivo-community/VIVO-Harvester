#!/bin/bash

#Copyright (c) 2010-2011 VIVO Harvester Team. For full list of contributors, please see the AUTHORS file provided.
#All rights reserved.
#This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html

# set to the directory where the harvester was installed or unpacked
# HARVESTER_INSTALL_DIR is set to the location of the installed harvester
#	If the deb file was used to install the harvester then the
#	directory should be set to /usr/share/vivo/harvester which is the
#	current location associated with the deb installation.
#	Since it is also possible the harvester was installed by
#	uncompressing the tar.gz the setting is available to be changed
#	and should agree with the installation location
HARVESTER_INSTALL_DIR=/usr/local/src/VIVO-Harvester
export HARVEST_NAME=example-soap
export DATE=`date +%Y-%m-%d'T'%T`

# Add harvester binaries to path for execution
# The tools within this script refer to binaries supplied within the harvester
#	Since they can be located in another directory their path should be
#	included within the classpath and the path environment variables.
export PATH=$PATH:$HARVESTER_INSTALL_DIR/bin
export CLASSPATH=$CLASSPATH:$HARVESTER_INSTALL_DIR/bin/harvester.jar:$HARVESTER_INSTALL_DIR/bin/dependency/*
export CLASSPATH=$CLASSPATH:$HARVESTER_INSTALL_DIR/build/harvester.jar:$HARVESTER_INSTALL_DIR/build/dependency/*

# Exit on first error
# The -e flag prevents the script from continuing even though a tool fails.
#	Continuing after a tool failure is undesirable since the harvested
#	data could be rendered corrupted and incompatible.
set -e -x

# Supply the location of the detailed log file which is generated during the script.
#	If there is an issue with a harvest, this file proves invaluable in finding
#	a solution to the problem. It has become common practice in addressing a problem
#	to request this file. The passwords and usernames are filtered out of this file
#	to prevent these logs from containing sensitive information.
echo "Full Logging in $HARVEST_NAME.$DATE.log"
if [ ! -d logs ]; then
  mkdir logs
fi
cd logs
touch $HARVEST_NAME.$DATE.log
ln -sf $HARVEST_NAME.$DATE.log $HARVEST_NAME.latest.log
cd ..

#clear old data
# For a fresh harvest, the removal of the previous information maintains data integrity.
#	If you are continuing a partial run or wish to use the old and already retrieved
#	data, you will want to comment out this line since it could prevent you from having
# 	the required harvest data.  
rm -rf data

# Execute Fetch
# This stage of the script is where the information is gathered together into one local
#	place to facilitate the further steps of the harvest. The data is stored locally
#	in a format based off of the source. The format is a form of RDF but not in the VIVO ontology
#harvester-soapmessenger -X soapmessenger-auth.config.xml
java $HARVESTER_JAVA_OPTS org.vivoweb.harvester.util.SOAPMessenger -X soapmessenger-auth.config.xml
#SESID=`harvester-xpathtool -X xpath-get-authcode.config.xml`
SESID=`java $HARVESTER_JAVA_OPTS org.vivoweb.harvester.util.XPathTool -X xpath-get-authcode.config.xml`
#echo " The session ID is :" $SESID

#harvester-soapmessenger -X soapmessenger-search.config.xml -a "$SESID"
java $HARVESTER_JAVA_OPTS org.vivoweb.harvester.util.SOAPMessenger -X soapmessenger-search.config.xml -a "$SESID"
#harvester-soapmessenger -X soapmessenger-retrieve.config.xml -a "$SESID"
java $HARVESTER_JAVA_OPTS org.vivoweb.harvester.util.SOAPMessenger -X soapmessenger-retrieve.config.xml -a "$SESID"
#harvester-soapmessenger -X soapmessenger-close.config.xml -a "$SESID"
java $HARVESTER_JAVA_OPTS org.vivoweb.harvester.util.SOAPMessenger -X soapmessenger-close.config.xml -a "$SESID"

echo 'soapmessenger completed successfully'
