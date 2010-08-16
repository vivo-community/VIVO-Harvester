#!/bin/bash

# Set working directory
cd /usr/share/vivoingest

# Execute Fetch for OAI
java -cp bin/ingest-0.4.5.jar:bin/dependency/* org.vivoweb.ingest.fetch.OAIFetch -X config/tasks/OAIFetchTask.xml

# Execute Translate
java -cp bin/ingest-0.4.5.jar:bin/dependency/* org.vivoweb.ingest.translate.XSLTranslator -i config/recordHandlers/OAIXMLRecordHandler.xml -x DataMaps/OAIDublinCoreToVIVO.xsl -o config/recordHandlers/OAIRDFRecordHandler.xml  

# Execute Score
java -cp bin/ingest-0.4.5.jar:bin/dependency/* org.vivoweb.ingest.score.Score -i config/recordHandlers/PubmedRDFRecordHandler.xml -V config/jenaModels/VIVO.xml -a 3

# Execute Qualify - depending on your data source you may not need to qualify follow the below examples for qualifying
#Off by default, examples show below
#java -cp bin/ingest-0.4.5.jar:bin/dependency/* org.vivoweb.ingest.qualify.SPARQLQualify -j config/jenaModels/VIVO.xml -t "Prof" -v "Professor" -d http://vivoweb.org/ontology/core#Title
#java -cp bin/ingest-0.4.5.jar:bin/dependency/* org.vivoweb.ingest.qualify.SPARQLQualify -j config/jenaModels/VIVO.xml -r .*JAMA.* -v "The Journal of American Medical Association" -d http://vivoweb.org/ontology/core#Title

# Execute Transfer
java -cp bin/ingest-0.4.5.jar:bin/dependency/* org.vivoweb.ingest.transfer.Transfer -i config/jenaModels/VIVO.xml -o config/jenaModels/VIVO.xml

#Restart Tomcat
#Tomcat must be restarted in order for the harvested data to appear in VIVO
# /etc/init.d/tomcat restart