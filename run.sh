#!/bin/bash

# Execute Fetch for PubMed
java -cp target/ingest-0.3.jar:target/dependency/* org.vivoweb.ingest.fetch.PubmedSOAPFetch -c config/tasks/PubmedFetchTask.xml

# Execute Translate
java -cp target/ingest-0.3.jar:target/dependency/* org.vivoweb.ingest.translate.XSLTranslator -i config/recordHandlers/PubmedXMLRecordHandler.xml -x DataMaps/PubMedToVivo.xsl -o config/recordHandlers/PubmedRDFRecordHandler.xml  

# Execute Score
java -cp target/ingest-0.3.jar:target/dependency/* org.vivoweb.ingest.score.Score config/recordHandlers/PubmedRDFRecordHandler.xml config/jenaModels/ScoringJenaInput.xml config/jenaModels/ScoringVIVOInput.xml config/jenaModels/ScoringJenaOutput.xml 

# Execute Qualify


# Execute Transfer