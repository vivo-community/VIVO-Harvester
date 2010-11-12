#!/bin/bash

# Copyright (c) 2010 Eliza Chan
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the new BSD license
# which accompanies this distribution, and is available at
# http://www.opensource.org/licenses/bsd-license.html
# 
# Contributors:
#     Eliza Chan

# Set working directory
cd /usr/share/vivo/harvester

if [ -f scripts/env ]; then
  . scripts/env
else
  exit 1
fi

HARVESTER_TASK=d2rmap

# Execute Fetch/Translate using D2RMap
java -cp lib/d2rmap-V03.jar:bin/harvester-$VERSION.jar:bin/dependency/* org.vivoweb.harvester.fetch.D2RMapFetch -o config/recordHandlers/JDBCXMLRecordHandler.xml -u config/tasks/D2RMapFetchTask.d2r.xml -s person.rdf

# Execute Transfer to transfer rdf into "d2rStaging" JENA model
Transfer -h config/recordHandlers/JDBCXMLRecordHandler.xml -o config/jenaModels/VIVO.xml -O modelName=d2rStaging

# Execute Transfer to load "d2rStaging" JENA model into VIVO
Transfer -i config/jenaModels/VIVO.xml -I modelName=d2rStaging -o config/jenaModels/VIVO.xml

# Execute Transfer to dump "d2rStaging" JENA model rdf into file
# Shown as example
#Transfer -i config/jenaModels/VIVO.xml -I modelName=d2rStaging -d dump.rdf

#Update the example on the board
#Update -p config/jenaModels/VIVO.xml -P modelName="PreviousModelName" -i config/jenaModels/VIVO.xml -I modelName="d2rStaging" -v config/jenaModels/VIVO.xml

#Restart Tomcat
#Tomcat must be restarted in order for the harvested data to appear in VIVO
#/etc/init.d/tomcat restart
