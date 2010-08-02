#!/bin/bash

# Set working directory to base folder
cd /usr/share/vivoingest

# Execute Fetch for PubMed
java -cp bin/ingest-0.4.0.jar:bin/dependency/* org.vivoweb.ingest.fetch.PubmedSOAPFetch -X config/tasks/PubmedFetchTask.xml

# Execute Translate
java -cp bin/ingest-0.4.0.jar:bin/dependency/* org.vivoweb.ingest.translate.XSLTranslator -i config/recordHandlers/PubmedXMLRecordHandler.xml -x DataMaps/PubMedToVivo.xsl -o config/recordHandlers/PubmedRDFRecordHandler.xml  

# Execute Score
java -cp bin/ingest-0.4.0.jar:bin/dependency/* org.vivoweb.ingest.score.Score -i config/recordHandlers/PubmedRDFRecordHandler.xml -V config/jenaModels/VIVO.xml

# Execute Qualify


# Execute Transfer
