# Execute Fetch for PubMed
java -cp bin/ingest-0.4.0.jar:bin/dependency/* org.vivoweb.ingest.fetch.PubmedSOAPFetch -X config/tasks/PubmedFetch.xml

# Execute Translate using the PubMedToVIVO.xsl file
java -cp bin/ingest-0.4.0.jar:bin/dependency/* org.vivoweb.ingest.translate.XSLTranslator -i config/recordHandlers/PubmedXMLRecordHandler.xml -x config/datamaps/PubMedToVivo.xsl -o config/recordHandlers/PubmedRDFRecordHandler.xml  

# Execute Score
java -cp bin/ingest-0.4.0.jar:bin/dependency/* org.vivoweb.ingest.score.Score -i config/recordHandlers/PubmedRDFRecordHandler.xml -V config/jenaModels/VIVO.xml

# Execute Qualify
# Note: This example leaves data the same; the replace value is the same as the match value
java -cp bin/ingest-0.4.0.jar:bin/dependency/* org.vivoweb.ingest.qualify.SPARQLQualify -j config/jenaModels/VIVO.xml -n staging -t "Prof" -v "Professor" -d http://vivoweb.org/ontology/core#Title
java -cp bin/ingest-0.4.0.jar:bin/dependency/* org.vivoweb.ingest.qualify.SPARQLQualify -j config/jenaModels/VIVO.xml -n staging -r .*JAMA.* -v "The Journal of American Medical Association" -d http://vivoweb.org/ontology/core#Title

# Execute Transfer
java -cp bin/ingest-0.4.0.jar:bin/dependency/* org.vivoweb.ingest.transfer.Transfer -i config/jenaModels/VIVO.xml -I staging -o config/jenaModels/VIVO.xml
