#!/bin/bash

# Execute Fetch for PubMed
java -cp target/ingest-0.4.jar:target/dependency/* org.vivoweb.ingest.fetch.PubmedSOAPFetch -c config/tasks/PubmedFetchTask.xml

# Execute Translate
java -cp target/ingest-0.4.jar:target/dependency/* org.vivoweb.ingest.translate.XSLTranslator -i config/recordHandlers/PubmedXMLRecordHandler.xml -x DataMaps/PubMedToVivo.xsl -o config/recordHandlers/PubmedRDFRecordHandler.xml  

# Execute Score
java -cp target/ingest-0.4.jar:target/dependency/* org.vivoweb.ingest.score.Score -r config/recordHandlers/PubmedRDFRecordHandler.xml -v config/jenaModels/VIVO.xml

# Execute Qualify


# Execute Transfer
