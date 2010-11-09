#!/bin/bash

# Copyright (c) 2010 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the new BSD license
# which accompanies this distribution, and is available at
# http://www.opensource.org/licenses/bsd-license.html
# 
# Contributors:
#     Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams - initial API and implementation

INGEST_TASK=pubmed

# Set working directory
cd /usr/share/vivoingest

# Execute Fetch for Pubmed
java -cp bin/ingest-0.7.0.jar:bin/dependency/* org.vivoweb.ingest.fetch.PubmedFetch -X config/tasks/PubmedFetch.xml

# Execute Translate using the PubmedToVIVO.xsl file
java -cp bin/ingest-0.7.0.jar:bin/dependency/* org.vivoweb.ingest.translate.XSLTranslator -i config/recordHandlers/Pubmed-XML-h2RH.xml -x config/datamaps/PubmedToVivo.xsl -o config/recordHandlers/Pubmed-RDF-h2RH.xml

# Execute Transfer to import from record handler into local temp model
java -cp bin/ingest-0.7.0.jar:bin/dependency/* org.vivoweb.ingest.transfer.Transfer -o config/jenaModels/h2.xml -O modelName=PubmedTempTransfer -O dbUrl="jdbc:h2:XMLVault/h2Pubmed/all/store;MODE=HSQLDB" -h config/recordHandlers/Pubmed-RDF-h2RH.xml

# Execute Score to disambiguate data in "scoring" JENA model and place scored rdf into "staging" JENA model
java -cp bin/ingest-0.7.0.jar:bin/dependency/* org.vivoweb.ingest.score.Score -v config/jenaModels/VIVO.xml -i config/jenaModels/h2.xml -I dbUrl="jdbc:h2:XMLVault/h2Pubmed/all/store;MODE=HSQLDB" -I modelName=PubmedTempTransfer -o config/jenaModels/h2.xml -O dbUrl="jdbc:h2:XMLVault/h2Pubmed/scored/store;MODE=HSQLDB" -O modelName=PubmedStaging -e workEmail
#java -cp bin/ingest-0.7.0.jar:bin/dependency/* org.vivoweb.ingest.score.Score -v config/jenaModels/VIVO.xml -i config/jenaModels/h2.xml -I dbUrl="jdbc:h2:XMLVault/h2Pubmed/all/store;MODE=HSQLDB" -I modelName=PubmedTempTransfer -o config/jenaModels/h2.xml -O dbUrl="jdbc:h2:XMLVault/h2Pubmed/scored/store;MODE=HSQLDB" -O modelName=PubmedStaging -a 3

# Execute Qualify - depending on your data source you may not need to qualify follow the below examples for qualifying
# Off by default, examples show below
#java -cp bin/ingest-0.7.0.jar:bin/dependency/* org.vivoweb.ingest.qualify.Qualify -j config/jenaModels/VIVO.xml -t "Prof" -v "Professor" -d http://vivoweb.org/ontology/core#Title
#java -cp bin/ingest-0.7.0.jar:bin/dependency/* org.vivoweb.ingest.qualify.Qualify -j config/jenaModels/VIVO.xml -r .*JAMA.* -v "The Journal of American Medical Association" -d http://vivoweb.org/ontology/core#Title

# Execute Transfer from local temp model into main vivo model (Commented out in favor of Update)
#java -cp bin/ingest-0.7.0.jar:bin/dependency/* org.vivoweb.ingest.transfer.Transfer -i config/jenaModels/h2.xml -I modelName=PubmedStaging -I dbUrl="jdbc:h2:XMLVault/h2Pubmed/scored/store;MODE=HSQLDB" -o config/jenaModels/VIVO.xml

# Execute Transfer to dump model rdf into file
# Shown as example
#java -cp bin/ingest-0.7.0.jar:bin/dependency/* org.vivoweb.ingest.transfer.Transfer -i config/jenaModels/h2.xml -I modelName=PubmedStaging -I dbUrl="jdbc:h2:XMLVault/h2Pubmed/scored/store;MODE=HSQLDB" -d dump.rdf


#Update the example on the board
#java -cp bin/ingest-0.7.0.jar:bin/dependency/* org.vivoweb.ingest.update.Update -p config/jenaModels/VIVO.xml -P modelName="PreviousModelName" -i config/jenaModels/VIVO.xml -I modelName="NewModelName" -v config/jenaModels/VIVO.xml



#Restart Tomcat
#Tomcat must be restarted in order for the harvested data to appear in VIVO
/etc/init.d/tomcat restart
