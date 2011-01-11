#!/bin/bash

# Copyright (c) 2010 Eliza Chan
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the new BSD license
# which accompanies this distribution, and is available at
# http://www.opensource.org/licenses/bsd-license.html
# 
# Contributors:
#     Eliza Chan

if [ -n "$1" ]

then

# Set working directory
DIR=$(cd "$(dirname "$0")"; pwd)
cd $DIR
cd ..

HARVESTER_TASK=csvmap

if [ -f scripts/env ]; then
  . scripts/env
else
  exit 1
fi

# Execute Fetch/Translate using D2RMap
$CSVMapFetch -o config/recordHandlers/CSVXMLRecordHandler.xml -u config/tasks/CSVMapFetchTask.d2r.xml -a $1 -s person.rdf

# Execute Transfer to transfer rdf into "d2rStaging" JENA model
$Transfer -h config/recordHandlers/CSVXMLRecordHandler.xml -o $VIVOCONFIG -OmodelName=d2rStaging

# Execute Transfer to load "d2rStaging" JENA model into VIVO
$Transfer -i $VIVOCONFIG -ImodelName=d2rStaging -o $VIVOCONFIG

# Execute Transfer to dump "d2rStaging" JENA model rdf into file
# Shown as example
#$Transfer -i $VIVOCONFIG -ImodelName=d2rStaging -d dump.rdf

#Restart Tomcat
#Tomcat must be restarted in order for the harvested data to appear in VIVO
#/etc/init.d/tomcat restart

else

echo
echo To continue, specify working directory
echo e.g. ./[your filename].sh /usr/share/harvester
echo

fi
