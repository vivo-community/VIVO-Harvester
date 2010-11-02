#!/bin/bash

# Copyright (c) 2010 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the new BSD license
# which accompanies this distribution, and is available at
# http://www.opensource.org/licenses/bsd-license.html
# 
# Contributors:
#     Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams - initial API and implementation

# Set working directory
cd /usr/share/vivoingest

# Execute Fetch for OAI
java -cp bin/ingest-0.6.2.jar:bin/dependency/* org.vivoweb.ingest.fetch.OAIFetch -X config/tasks/OAIFetchTask.xml

# Execute Translate
java -cp bin/ingest-0.6.2.jar:bin/dependency/* org.vivoweb.ingest.translate.XSLTranslator -i config/recordHandlers/OAIXMLRecordHandler.xml -x DataMaps/OAIDublinCoreToVIVO.xsl -o config/recordHandlers/OAIRDFRecordHandler.xml  

# Execute Transfer to transfer rdf into "scoring" JENA model
java -cp bin/ingest-0.6.2.jar:bin/dependency/* org.vivoweb.ingest.transfer.Transfer -h config/recordHandlers/PubmedRDFRecordHandler.xml -o config/jenaModels/VIVO.xml -O modelName=scoring  

# Execute Score to disambiguate data in "scoring" JENA model and place scored rdf into "staging" JENA model
java -cp bin/ingest-0.6.2.jar:bin/dependency/* org.vivoweb.ingest.score.Score -v config/jenaModels/VIVO.xml -a 3

# Execute Qualify - depending on your data source you may not need to qualify follow the below examples for qualifying
# Off by default, examples show below
#java -cp bin/ingest-0.6.2.jar:bin/dependency/* org.vivoweb.ingest.qualify.Qualify -j config/jenaModels/VIVO.xml -t "Prof" -v "Professor" -d http://vivoweb.org/ontology/core#Title
#java -cp bin/ingest-0.6.2.jar:bin/dependency/* org.vivoweb.ingest.qualify.Qualify -j config/jenaModels/VIVO.xml -r .*JAMA.* -v "The Journal of American Medical Association" -d http://vivoweb.org/ontology/core#Title

# Execute Transfer to load "staging" JENA model into VIVO
java -cp bin/ingest-0.6.2.jar:bin/dependency/* org.vivoweb.ingest.transfer.Transfer -i config/jenaModels/VIVO.xml -I modelName=staging -o config/jenaModels/VIVO.xml

# Execute Transfer to dump "staging" JENA model rdf into file
# Shown as example
#java -cp bin/ingest-0.6.2.jar:bin/dependency/* org.vivoweb.ingest.transfer.Transfer -i config/jenaModels/VIVO.xml -d dump.rdf

#Restart Tomcat
#Tomcat must be restarted in order for the harvested data to appear in VIVO
/etc/init.d/tomcat restart
