#!/bin/bash

# Set working directory to base folder
cd ..

# Execute Fetch for PubMed
java -cp bin/ingest-0.4.0.jar:bin/dependency/* org.vivoweb.ingest.fetch.PubmedSOAPFetch -X config/tasks/PubmedFetchTask.xml

# Execute Translate
java -cp bin/ingest-0.4.0.jar:bin/dependency/* org.vivoweb.ingest.translate.XSLTranslator -i config/recordHandlers/PubmedXMLRecordHandler.xml -x config/datamaps/PubMedToVivo.xsl -o config/recordHandlers/PubmedRDFRecordHandler.xml  

# Execute Score
java -cp bin/ingest-0.4.0.jar:bin/dependency/* org.vivoweb.ingest.score.Score -i config/recordHandlers/PubmedRDFRecordHandler.xml -V config/jenaModels/VIVO.xml

# Execute Qualify
java -cp bin/ingest-0.4.0.jar:bin/dependency/* org.vivoweb.ingest.qualify.SPARQLQualify -j config/jenaModels/VIVO.xml -n staging

# Execute Transfer
java -cp bin/ingest-0.4.0.jar:bin/dependency/* org.vivoweb.ingest.qualify.SPARQLQualify -i config/jenaModels/VIVO.xml -I staging -o config/jenaModels/VIVO.xml