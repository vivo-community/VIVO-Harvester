#!/bin/bash

# Execute Fetch for PubMed
java -cp target/ingest-0.4.0.jar:target/dependency/* org.vivoweb.ingest.fetch.PubmedSOAPFetch -X config/tasks/PubmedFetchTask.xml

# Execute Translate
java -cp target/ingest-0.4.0.jar:target/dependency/* org.vivoweb.ingest.translate.XSLTranslator -i config/recordHandlers/PubmedXMLRecordHandler.xml -x DataMaps/PubMedToVivo.xsl -o config/recordHandlers/PubmedRDFRecordHandler.xml  

# Execute Score
java -cp target/ingest-0.4.0.jar:target/dependency/* org.vivoweb.ingest.score.Score -i config/recordHandlers/PubmedRDFRecordHandler.xml -V config/jenaModels/VIVO.xml

# Execute Qualify


# Execute Transfer
