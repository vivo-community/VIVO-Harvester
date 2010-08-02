#!/bin/bash

# Execute Fetch for PubMed
java -cp target/ingest-0.4.jar:target/dependency/* org.vivoweb.ingest.fetch.OAIFetch -c config/tasks/OAIFetchTask.xml

# Execute Translate
java -cp target/ingest-0.4.jar:target/dependency/* org.vivoweb.ingest.translate.XSLTranslator -i config/recordHandlers/OAIXMLRecordHandler.xml -x DataMaps/OAIDublinCoreToVIVO.xsl -o config/recordHandlers/OAIRDFRecordHandler.xml  

# Execute Score


# Execute Qualify


# Execute Transfer
