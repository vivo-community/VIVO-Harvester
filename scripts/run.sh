#!/bin/bash

# Set working directory to base folder
#cd ..

# Execute Fetch for PubMed
#java -cp bin/ingest-0.4.0.jar:bin/dependency/* org.vivoweb.ingest.fetch.PubmedSOAPFetch -X config/tasks/PubmedFetch.xml

# Execute Translate
#java -cp bin/ingest-0.4.0.jar:bin/dependency/* org.vivoweb.ingest.translate.XSLTranslator -i config/recordHandlers/PubmedXMLRecordHandler.xml -x config/datamaps/PubMedToVivo.xsl -o config/recordHandlers/PubmedRDFRecordHandler.xml  

# Execute Score
#java -cp bin/ingest-0.4.0.jar:bin/dependency/* org.vivoweb.ingest.score.Score -i config/recordHandlers/PubmedRDFRecordHandler.xml -V config/jenaModels/VIVO.xml

# Execute Qualify
# Note: This example leaves data the same; the replace value is the same as the match value
java -cp bin/ingest-0.4.0.jar:bin/dependency/* org.vivoweb.ingest.qualify.SPARQLQualify -j config/jenaModels/VIVO.xml -n staging -t PROF -v Professor -d http://vivoweb.org/ontology/core#hrJobTitle 

# Execute Transfer
java -cp bin/ingest-0.4.0.jar:bin/dependency/* org.vivoweb.ingest.transfer.Transfer -i config/jenaModels/VIVO.xml -I staging -d dump.rdf.xml
#-o config/jenaModels/VIVO.xml 
