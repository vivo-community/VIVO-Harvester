#!/bin/bash

# Execute Fetch for PubMed
java -cp target/ingest-0.3.2.jar:target/dependency/* org.vivoweb.ingest.fetch.Fetch PubMed

# Execute Translate
java -cp target/ingest-0.3.2.jar:target/dependency/* org.vivoweb.ingest.translate.XSLTranslator -rh config/recordHandlers/TranslateInPubmedXMLRecordHandler.xml DataMaps/PubMedToVivo.xsl config/recordHandlers/TranslateOutPubmedXMLRecordHandler.xml  

# Execute Score
java -cp target/ingest-0.3.2.jar:target/dependency/* org.vivoweb.ingest.score.Score config/recordHandlers/ScoringInXMLRecordHandler.xml config/jenaModels/ScoringJenaInput.xml config/jenaModels/ScoringVIVOInput.xml config/jenaModels/ScoringJenaOutput.xml 

# Execute Qualify


# Execute Transfer