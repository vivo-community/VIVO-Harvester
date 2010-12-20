#!/bin/bash

# Copyright (c) 2010 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the new BSD license
# which accompanies this distribution, and is available at
# http://www.opensource.org/licenses/bsd-license.html
# 
# Contributors:
#     Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams - initial API and implementation

# Set working directory
cd `dirname $(readlink -f $0)`
cd ..

HARVESTER_TASK=pubmed

#variables for model arguments
INPUT="-i config/jenaModels/h2.xml -I dbUrl=jdbc:h2:XMLVault/h2Pubmed/all/store;MODE=HSQLDB -I modelName=Pubmed"
OUTPUT="-o config/jenaModels/h2.xml -O modelName=Pubmed -O dbUrl=jdbc:h2:XMLVault/h2Pubmed/all/store;MODE=HSQLDB"
VIVO="-v config/jenaModels/VIVO.xml"
SCORE="-s config/jenaModels/h2.xml -S dbUrl=jdbc:h2:XMLVault/h2Pubmed/score/store;MODE=HSQLDB -S modelName=PubmedScore"
MATCHEDINPUT="-i config/jenaModels/h2.xml -i modelName=Pubmed -i dbUrl=jdbc:h2:XMLVault/h2Pubmed/all/store;MODE=HSQLDB"

#variables for scoring
WORKEMAIL="-A wEmail=org.vivoweb.harvester.score.algorithm.EqualityTest -F wEmail=http://vivoweb.org/ontology/score#workEmail -W wEmail=1 -P wEmail=http://vivoweb.org/ontology/core#workEmail"

if [ -f scripts/env ]; then
  . scripts/env
else
  exit 1
fi

#clear old fetches
rm -rf XMLVault/h2Pubmed/XML

# Execute Fetch for Pubmed
$PubmedFetch -X config/tasks/PubmedFetch.xml

# backup fetch
date=`date +%Y-%m-%d_%T`
tar -czpf backups/.$date.tar.gz XMLVault/h2Pubmed/XML
rm -rf backups/pubmed.xml.latest.tar.gz
ln -s pubmed.xml.$date.tar.gz backups/pubmed.xml.latest.tar.gz

# uncomment to restore previous fetch
#tar -xzpf backups/pubmed.xml.latest.tar.gz XMLVault/h2Pubmed/XML

# clear old translates
rm -rf XMLVault/h2Pubmed/RDF

# Execute Translate using the PubmedToVIVO.xsl file
$XSLTranslator -i config/recordHandlers/Pubmed-XML-h2RH.xml -x config/datamaps/PubmedToVivo.xsl -o config/recordHandlers/Pubmed-RDF-h2RH.xml

# backup translate
date=`date +%Y-%m-%d_%T`
tar -czpf backups/pubmed.rdf.$date.tar.gz XMLVault/h2Pubmed/RDF
rm -rf backups/pubmed.rdf.latest.tar.gz
ln -s pubmed.rdf.$date.tar.gz backups/pubmed.rdf.latest.tar.gz

# uncomment to restore previous translate
#tar -xzpf backups/pubmed.rdf.latest.tar.gz XMLVault/h2Pubmed/RDF

# Clear old H2 models
rm -rf XMLVault/h2Pubmed/all

# Execute Transfer to import from record handler into local temp model
$Transfer $OUTPUT -h config/recordHandlers/Pubmed-RDF-h2RH.xml

# backup H2 translate Models
date=`date +%Y-%m-%d_%T`
tar -czpf backups/pubmed.all.$date.tar.gz XMLVault/h2Pubmed/all
rm -rf backups/pubmed.all.latest.tar.gz
ln -s ps.all.$date.tar.gz backups/pubmed.all.latest.tar.gz

# uncomment to restore previous H2 translate models
#tar -xzpf backups/pubmed.all.latest.tar.gz XMLVault/h2Pubmed/all

# clear old score models
rm -rf XMLVault/h2Pubmed/score

# Execute Score to disambiguate data in "scoring" JENA model
$Score $VIVO $INPUT $SCORE $WORKEMAIL   

# Execute match to match and link data into "vivo" JENA model
$Match $INPUT $SCORE -t .8
 
# back H2 score models
date=`date +%Y-%m-%d_%T`
tar -czpf backups/pubmed.scored.$date.tar.gz XMLVault/h2Pubmed/scored
rm -rf backups/pubmed.scored.latest.tar.gz
ln -s ps.scored.$date.tar.gz backups/pubmed.scored.latest.tar.gz

# uncomment to restore previous H2 score models
#tar -xzpf backups/pubmed.scored.latest.tar.gz XMLVault/h2Pubmed/scored

# Execute Qualify - depending on your data source you may not need to qualify follow the below examples for qualifying
# Off by default, examples show below
#$Qualify -j config/jenaModels/VIVO.xml -t "Prof" -v "Professor" -d http://vivoweb.org/ontology/core#Title
#$Qualify -j config/jenaModels/VIVO.xml -r .*JAMA.* -v "The Journal of American Medical Association" -d http://vivoweb.org/ontology/core#Title

# Execute ChangeNamespace to get into current namespace
$ChangeNamespace $VIVO $MATCHEDINPUT -n http://vivo.ufl.edu/ -o http://vivoweb.org/harvest/

# Backup pretransfer vivo database, symlink latest to latest.sql
date=`date +%Y-%m-%d_%T`
mysqldump -h $SERVER -u $USERNAME -p$PASSWORD $DBNAME > backups/$DBNAME.pubmed.pretransfer.$date.sql
rm -rf backups/$DBNAME.pubmed.pretransfer.latest.sql
ln -s $DBNAME.pubmed.pretransfer.$date.sql backups/$DBNAME.pubmed.pretransfer.latest.sql

#Update VIVO, using previous model as comparison. On first run, previous model won't exist resulting in all statements being passed to VIVO  
$Update -p config/jenaModels/VIVO.xml -P modelName="http://vivoweb.org/ingest/pubmed" -i config/jenaModels/h2.xml -I dbUrl="jdbc:h2:XMLVault/h2Pubmed/scored/store;MODE=HSQLDB" -I modelName=PubmedStaging -v config/jenaModels/VIVO.xml

# Backup posttransfer vivo database, symlink latest to latest.sql
date=`date +%Y-%m-%d_%T`
mysqldump -h $SERVER -u $USERNAME -p$PASSWORD $DBNAME > backups/$DBNAME.pubmed.posttransfer.$date.sql
rm -rf backups/$DBNAME.pubmed.posttransfer.latest.sql
ln -s $DBNAME.pubmed.posttransfer.$date.sql backups/$DBNAME.pubmed.posttransfer.latest.sql

#Restart Tomcat
#Tomcat must be restarted in order for the harvested data to appear in VIVO
/etc/init.d/tomcat restart
