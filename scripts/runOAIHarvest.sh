#!/bin/bash

# Copyright (c) 2010 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the new BSD license
# which accompanies this distribution, and is available at
# http://www.opensource.org/licenses/bsd-license.html
# 
# Contributors:
#     Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams - initial API and implementation

if [ -f env ]; then
  . env
else
  exit 1
fi

INGEST_TASK=oai

# Execute Fetch for OAI
OAIFetch -X config/tasks/OAIFetchTask.xml

# Execute Translate
XSLTranslator -i config/recordHandlers/OAIXMLRecordHandler.xml -x DataMaps/OAIDublinCoreToVIVO.xsl -o config/recordHandlers/OAIRDFRecordHandler.xml  

# Execute Transfer to transfer rdf into "scoring" JENA model
Transfer -h config/recordHandlers/PubmedRDFRecordHandler.xml -o config/jenaModels/VIVO.xml -O modelName=scoring  

# Execute Score to disambiguate data in "scoring" JENA model and place scored rdf into "staging" JENA model
Score -v config/jenaModels/VIVO.xml -a 3

# Execute Qualify - depending on your data source you may not need to qualify follow the below examples for qualifying
# Off by default, examples show below
#Qualify -j config/jenaModels/VIVO.xml -t "Prof" -v "Professor" -d http://vivoweb.org/ontology/core#Title
#Qualify -j config/jenaModels/VIVO.xml -r .*JAMA.* -v "The Journal of American Medical Association" -d http://vivoweb.org/ontology/core#Title

# Execute Transfer to load "staging" JENA model into VIVO
Transfer -i config/jenaModels/VIVO.xml -I modelName=staging -o config/jenaModels/VIVO.xml

#Update the example on the board
#Update -p config/jenaModels/VIVO.xml -P modelName="PreviousModelName" -i config/jenaModels/VIVO.xml -I modelName="staging" -v config/jenaModels/VIVO.xml

# Execute Transfer to dump "staging" JENA model rdf into file
# Shown as example
#Transfer -i config/jenaModels/VIVO.xml -d dump.rdf

#Restart Tomcat
#Tomcat must be restarted in order for the harvested data to appear in VIVO
/etc/init.d/tomcat restart
