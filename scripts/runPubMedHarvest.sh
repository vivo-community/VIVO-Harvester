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

# Execute Fetch for PubMed
java -cp bin/ingest-0.6.0.jar:bin/dependency/* org.vivoweb.ingest.fetch.PubmedSOAPFetch -X config/tasks/PubmedFetch.xml

# Execute Translate using the PubMedToVIVO.xsl file
java -cp bin/ingest-0.6.0.jar:bin/dependency/* org.vivoweb.ingest.translate.XSLTranslator -i config/recordHandlers/Pubmed-XML-TFRH.xml -x config/datamaps/PubMedToVivo.xsl -o config/recordHandlers/Pubmed-XML-TFRH.xml

# Execute Transfer to import from record handler into local temp model
java -cp bin/ingest-0.6.0.jar:bin/dependency/* org.vivoweb.ingest.transfer.Transfer -o config/jenaModels/h2.xml -O modelName=pubMedTempTransfer -O dbUrl="jdbc:h2:XMLVault/h2pubmed/all/store;MODE=HSQLDB" -h config/recordHandlers/PubMed-RDF-h2RH.xml

# Execute Score to disambiguate data in "scoring" JENA model and place scored rdf into "staging" JENA model
java -cp bin/ingest-0.6.0.jar:bin/dependency/* org.vivoweb.ingest.score.Score -v config/jenaModels/VIVO.xml -i config/jenaModels/h2.xml -I dbUrl="jdbc:h2:XMLVault/h2pubmed/all/store;MODE=HSQLDB" -I modelName=pubMedTempTransfer -o config/jenaModels/h2.xml -O dbUrl="jdbc:h2:XMLVault/h2pubmed/scored/store;MODE=HSQLDB" -O modelName=pubMedStaging -e workEmail
#java -cp bin/ingest-0.6.0.jar:bin/dependency/* org.vivoweb.ingest.score.Score -v config/jenaModels/VIVO.xml -a 3

# Execute Qualify - depending on your data source you may not need to qualify follow the below examples for qualifying
# Off by default, examples show below
#java -cp bin/ingest-0.6.0.jar:bin/dependency/* org.vivoweb.ingest.qualify.Qualify -j config/jenaModels/VIVO.xml -t "Prof" -v "Professor" -d http://vivoweb.org/ontology/core#Title
#java -cp bin/ingest-0.6.0.jar:bin/dependency/* org.vivoweb.ingest.qualify.Qualify -j config/jenaModels/VIVO.xml -r .*JAMA.* -v "The Journal of American Medical Association" -d http://vivoweb.org/ontology/core#Title

# Execute Transfer from local temp model into main vivo model
java -cp bin/ingest-0.6.0.jar:bin/dependency/* org.vivoweb.ingest.transfer.Transfer -i config/jenaModels/h2.xml -I modelName=pubMedStaging -I dbUrl="jdbc:h2:XMLVault/h2pubmed/scored/store;MODE=HSQLDB" -o config/jenaModels/VIVO.xml

# Execute Transfer to dump model rdf into file
# Shown as example
#java -cp bin/ingest-0.6.0.jar:bin/dependency/* org.vivoweb.ingest.transfer.Transfer -i config/jenaModels/h2.xml -I modelName=pubMedStaging -I dbUrl="jdbc:h2:XMLVault/h2pubmed/scored/store;MODE=HSQLDB" -d dump.rdf

#Restart Tomcat
#Tomcat must be restarted in order for the harvested data to appear in VIVO
/etc/init.d/tomcat restart