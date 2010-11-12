#!/bin/bash

# Copyright (c) 2010 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the new BSD license
# which accompanies this distribution, and is available at
# http://www.opensource.org/licenses/bsd-license.html
# 
# Contributors:
#     Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams - initial API and implementation

VERSION=0.7.0
INGEST_TASK=pubmed

# Set working directory
cd /usr/share/vivo/harvester

#clear old fetches
rm -rd XMLVault/h2Pubmed/XML

# Execute Fetch for Pubmed
java -Xms1024m -Xmx3072M -cp bin/harvester-$VERSION.jar:bin/dependency/* org.vivoweb.harvester.fetch.PubmedFetch -X config/tasks/PubmedFetch.xml

# backup fetch
date=`date +%Y-%m-%d_%k%M.%S`
tar -czpf backups/pubmed.xml.$date.tar.gz XMLVault/h2Pubmed/XML
rm -rf backups/pubmed.xml.latest.tar.gz
ln -s pubmed.xml.$date.tar.gz backups/pubmed.xml.latest.tar.gz

# uncomment to restore previous fetch
#tar -xzpf backups/pubmed.xml.latest.tar.gz XMLVault/h2Pubmed/XML

# clear old translates
rm -rd XMLVault/h2Pubmed/RDF

# Execute Translate using the PubmedToVIVO.xsl file
java -Xms1024m -Xmx3072M -cp bin/harvester-$VERSION.jar:bin/dependency/* org.vivoweb.harvester.translate.XSLTranslator -i config/recordHandlers/Pubmed-XML-h2RH.xml -x config/datamaps/PubmedToVivo.xsl -o config/recordHandlers/Pubmed-RDF-h2RH.xml

# backup translate
date=`date +%Y-%m-%d_%k%M.%S`
tar -czpf backups/pubmed.rdf.$date.tar.gz XMLVault/h2Pubmed/RDF
rm -rf backups/pubmed.rdf.latest.tar.gz
ln -s pubmed.rdf.$date.tar.gz backups/pubmed.rdf.latest.tar.gz

# uncomment to restore previous translate
#tar -xzpf backups/pubmed.rdf.latest.tar.gz XMLVault/h2Pubmed/RDF

# Clear old H2 models
rm -rd XMLVault/h2Pubmed/all

# Execute Transfer to import from record handler into local temp model
java -Xms1024m -Xmx3072M -cp bin/harvester-$VERSION.jar:bin/dependency/* org.vivoweb.harvester.transfer.Transfer -o config/jenaModels/h2.xml -O modelName=PubmedTempTransfer -O dbUrl="jdbc:h2:XMLVault/h2Pubmed/all/store;MODE=HSQLDB" -h config/recordHandlers/Pubmed-RDF-h2RH.xml

# backup H2 translate Models
date=`date +%Y-%m-%d_%k%M.%S`
tar -czpf backups/pubmed.all.$date.tar.gz XMLVault/h2Pubmed/all
rm -rf backups/pubmed.all.latest.tar.gz
ln -s ps.all.$date.tar.gz backups/pubmed.all.latest.tar.gz

# uncomment to restore previous H2 translate models
#tar -xzpf backups/pubmed.all.latest.tar.gz XMLVault/h2Pubmed/all

# clear old Score models
rm -rd XMLVault/h2Pubmed/scored

# Execute Score to disambiguate data in "scoring" JENA model and place scored rdf into "staging" JENA model
java -Xms1024m -Xmx3072M -cp bin/harvester-$VERSION.jar:bin/dependency/* org.vivoweb.harvester.score.Score -v config/jenaModels/VIVO.xml -i config/jenaModels/h2.xml -I dbUrl="jdbc:h2:XMLVault/h2Pubmed/all/store;MODE=HSQLDB" -I modelName=PubmedTempTransfer -o config/jenaModels/h2.xml -O dbUrl="jdbc:h2:XMLVault/h2Pubmed/scored/store;MODE=HSQLDB" -O modelName=PubmedStaging -e workEmail
#java -Xms1024m -Xmx3072M -cp bin/harvester-$VERSION.jar:bin/dependency/* org.vivoweb.harvester.score.Score -v config/jenaModels/VIVO.xml -i config/jenaModels/h2.xml -I dbUrl="jdbc:h2:XMLVault/h2Pubmed/all/store;MODE=HSQLDB" -I modelName=PubmedTempTransfer -o config/jenaModels/h2.xml -O dbUrl="jdbc:h2:XMLVault/h2Pubmed/scored/store;MODE=HSQLDB" -O modelName=PubmedStaging -a 3

# back H2 score models
date=`date +%Y-%m-%d_%k%M.%S`
tar -czpf backups/pubmed.scored.$date.tar.gz XMLVault/h2Pubmed/scored
rm -rf backups/pubmed.scored.latest.tar.gz
ln -s ps.scored.$date.tar.gz backups/pubmed.scored.latest.tar.gz

# uncomment to restore previous H2 score models
#tar -xzpf backups/pubmed.scored.latest.tar.gz XMLVault/h2Pubmed/scored

# Execute Qualify - depending on your data source you may not need to qualify follow the below examples for qualifying
# Off by default, examples show below
#java -Xms1024m -Xmx3072M -cp bin/harvester-$VERSION.jar:bin/dependency/* org.vivoweb.harvester.qualify.Qualify -j config/jenaModels/VIVO.xml -t "Prof" -v "Professor" -d http://vivoweb.org/ontology/core#Title
#java -Xms1024m -Xmx3072M -cp bin/harvester-$VERSION.jar:bin/dependency/* org.vivoweb.harvester.qualify.Qualify -j config/jenaModels/VIVO.xml -r .*JAMA.* -v "The Journal of American Medical Association" -d http://vivoweb.org/ontology/core#Title

# Execute ChangeNamespace to get into current namespace
java -Xms1024m -Xmx3072M -cp bin/harvester-$VERSION.jar:bin/dependency/* org.vivoweb.harvester.update.ChangeNamespace -i config/jenaModels/h2.xml -I modelName=PubmedStaging -I dbUrl="jdbc:h2:XMLVault/h2Pubmed/scored/store;MODE=HSQLDB" -v config/jenaModels/VIVO.xml -n http://vivo.ufl.edu/individual/ -o http://vivoweb.org/harvest/pubmedPub/ -p http://purl.org/ontology/bibo/pmid
java -Xms1024m -Xmx3072M -cp bin/harvester-$VERSION.jar:bin/dependency/* org.vivoweb.harvester.update.ChangeNamespace -i config/jenaModels/h2.xml -I modelName=PubmedStaging -I dbUrl="jdbc:h2:XMLVault/h2Pubmed/scored/store;MODE=HSQLDB" -v config/jenaModels/VIVO.xml -n http://vivo.ufl.edu/individual/ -o http://vivoweb.org/harvest/pubmedAuthorship/ -p http://vivoweb.org/ontology/core#linkedInformationResource -p http://vivoweb.org/ontology/core#authorRank
java -Xms1024m -Xmx3072M -cp bin/harvester-$VERSION.jar:bin/dependency/* org.vivoweb.harvester.update.ChangeNamespace -i config/jenaModels/h2.xml -I modelName=PubmedStaging -I dbUrl="jdbc:h2:XMLVault/h2Pubmed/scored/store;MODE=HSQLDB" -v config/jenaModels/VIVO.xml -n http://vivo.ufl.edu/individual/ -o http://vivoweb.org/harvest/pubmedAuthor/ -p http://vivoweb.org/ontology/core#authorInAuthorship
java -Xms1024m -Xmx3072M -cp bin/harvester-$VERSION.jar:bin/dependency/* org.vivoweb.harvester.update.ChangeNamespace -i config/jenaModels/h2.xml -I modelName=PubmedStaging -I dbUrl="jdbc:h2:XMLVault/h2Pubmed/scored/store;MODE=HSQLDB" -v config/jenaModels/VIVO.xml -n http://vivo.ufl.edu/individual/ -o http://vivoweb.org/harvest/pubmedJournal/ -p http://purl.org/ontology/bibo/ISSN

# Backup pretransfer vivo database, symlink latest to latest.sql
date=`date +%Y-%m-%d_%k%M.%S`
mysqldump -h SERVER -u USERNAME -pPASSWORD vivodb > backups/vivodb.pubmed.pretransfer.$date.sql
rm -rf backups/vivodb.pubmed.pretransfer.latest.sql
ln -s vivodb.pubmed.pretransfer.$date.sql backups/vivodb.pubmed.pretransfer.latest.sql

#Update VIVO, using previous model as comparison. On first run, previous model won't exist resulting in all statements being passed to VIVO  
java -Xms1024m -Xmx3072M -cp bin/harvester-$VERSION.jar:bin/dependency/* org.vivoweb.harvester.update.Update -p config/jenaModels/VIVO.xml -P modelName="http://vivoweb.org/ingest/pubmed" -i config/jenaModels/h2.xml -I dbUrl="jdbc:h2:XMLVault/h2Pubmed/scored/store;MODE=HSQLDB" -I modelName=PubmedStaging -v config/jenaModels/VIVO.xml

# Backup posttransfer vivo database, symlink latest to latest.sql
date=`date +%Y-%m-%d_%k%M.%S`
mysqldump -h SERVER -u USERNAME -pPASSWORD vivodb > backups/vivodb.pubmed.posttransfer.$date.sql
rm -rf backups/vivodb.pubmed.posttransfer.latest.sql
ln -s vivodb.pubmed.posttransfer.$date.sql backups/vivodb.pubmed.posttransfer.latest.sql

#Restart Tomcat
#Tomcat must be restarted in order for the harvested data to appear in VIVO
/etc/init.d/tomcat restart