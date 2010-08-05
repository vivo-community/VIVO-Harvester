#!/bin/bash

# Set working directory
cd /usr/share/vivoingest

# Execute Fetch for RDBMS
java -cp bin/ingest-0.4.1.jar:bin/dependency/* org.vivoweb.ingest.fetch.JDBCFetch -X config/tasks/JDBCFetchTask.xml

# Execute Translate - to translate from an unknown data source you must convert the xml/rdf to the vivo ontology by defining an xsl file
java -cp bin/ingest-0.4.1.jar:bin/dependency/* org.vivoweb.ingest.translate.XSLTranslator -i config/recordHandlers/OAIXMLRecordHandler.xml -x DataMaps/OAIDublinCoreToVIVO.xsl -o config/recordHandlers/OAIRDFRecordHandler.xml  

# Execute Score - for scoring to work you must match on core version ontology using -e
java -cp bin/ingest-0.4.1.jar:bin/dependency/* org.vivoweb.ingest.score.Score -i config/recordHandlers/PubmedRDFRecordHandler.xml -V config/jenaModels/VIVO.xml -e ""

# Execute Qualify - depending on your data source you may not need to qualify follow the below examples for qualifing
java -cp bin/ingest-0.4.1.jar:bin/dependency/* org.vivoweb.ingest.qualify.SPARQLQualify -j config/jenaModels/VIVO.xml -n staging -t "Prof" -v "Professor" -d http://vivoweb.org/ontology/core#Title
java -cp bin/ingest-0.4.1.jar:bin/dependency/* org.vivoweb.ingest.qualify.SPARQLQualify -j config/jenaModels/VIVO.xml -n staging -r .*JAMA.* -v "The Journal of American Medical Association" -d http://vivoweb.org/ontology/core#Title

# Execute Transfer
java -cp bin/ingest-0.4.1.jar:bin/dependency/* org.vivoweb.ingest.transfer.Transfer -i config/jenaModels/VIVO.xml -I staging -o config/jenaModels/VIVO.xml
