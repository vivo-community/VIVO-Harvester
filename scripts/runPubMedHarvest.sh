#!/bin/bash

# Set working directory
#cd /usr/share/vivoingest

# Execute Fetch for PubMed
java -cp bin/ingest-0.5.0.jar:bin/dependency/* org.vivoweb.ingest.fetch.PubmedSOAPFetch -X config/tasks/PubmedFetch.xml

# Execute Translate using the PubMedToVIVO.xsl file
java -cp bin/ingest-0.5.0.jar:bin/dependency/* org.vivoweb.ingest.translate.XSLTranslator -i config/recordHandlers/PubmedXMLRecordHandler.xml -x config/datamaps/PubMedToVivo.xsl -o config/recordHandlers/PubmedRDFRecordHandler.xml  

# Execute Score
#java -cp bin/ingest-0.5.0.jar:bin/dependency/* org.vivoweb.ingest.score.Score -i config/recordHandlers/PubmedRDFRecordHandler.xml -V config/jenaModels/VIVO.xml -e workEmail
#java -cp bin/ingest-0.5.0.jar:bin/dependency/* org.vivoweb.ingest.score.Score -i config/recordHandlers/PubmedRDFRecordHandler.xml -V config/jenaModels/VIVO.xml -a 3

# Execute Qualify - depending on your data source you may not need to qualify follow the below examples for qualifying
#Off by default, examples show below
#java -cp bin/ingest-0.5.0.jar:bin/dependency/* org.vivoweb.ingest.qualify.SPARQLQualify -j config/jenaModels/VIVO.xml -t "Prof" -v "Professor" -d http://vivoweb.org/ontology/core#Title
#java -cp bin/ingest-0.5.0.jar:bin/dependency/* org.vivoweb.ingest.qualify.SPARQLQualify -j config/jenaModels/VIVO.xml -r .*JAMA.* -v "The Journal of American Medical Association" -d http://vivoweb.org/ontology/core#Title

# Execute Transfer
#java -cp bin/ingest-0.5.jar:bin/dependency/* org.vivoweb.ingest.transfer.Transfer -i config/jenaModels/VIVO.xml -o config/jenaModels/VIVO.xml
#java -cp bin/ingest-0.5.jar:bin/dependency/* org.vivoweb.ingest.transfer.Transfer -i config/jenaModels/VIVO.xml -d dump.rdf

#Restart Tomcat
#Tomcat must be restarted in order for the harvested data to appear in VIVO
# /etc/init.d/tomcat restart
