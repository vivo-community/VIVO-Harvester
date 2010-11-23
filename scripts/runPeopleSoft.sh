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

HARVESTER_TASK=peoplesoft

if [ -f scripts/env ]; then
  . scripts/env
else
  exit 1
fi

#clear old fetches
rm -rf XMLVault/h2ps/XML

# Execute Fetch
$JDBCFetch -X config/tasks/PeopleSoftFetch.xml

# backup fetch
date=`date +%Y-%m-%d_%T`
tar -czpf backups/ps.xml.$date.tar.gz XMLVault/h2ps/XML
rm -rf backups/ps.xml.latest.tar.gz
ln -s ps.xml.$date.tar.gz backups/ps.xml.latest.tar.gz

# uncomment to restore previous fetch
#tar -xzpf backups/ps.xml.latest.tar.gz XMLVault/h2ps/XML

# clear old translates
rm -rf XMLVault/h2ps/RDF

# Execute Translate
$XSLTranslator -i config/recordHandlers/PeopleSoft-XML.xml -o config/recordHandlers/PeopleSoft-RDF.xml -x config/datamaps/PeopleSoftToVivo.xsl

# backup translate
date=`date +%Y-%m-%d_%T`
tar -czpf backups/ps.rdf.$date.tar.gz XMLVault/h2ps/RDF
rm -rf backups/ps.rdf.latest.tar.gz
ln -s ps.rdf.$date.tar.gz backups/ps.rdf.latest.tar.gz

# uncomment to restore previous translate
#tar -xzpf backups/ps.rdf.latest.tar.gz XMLVault/h2ps/RDF

# Clear old H2 models
rm -rf XMLVault/h2ps/all

# Execute Transfer to import from record handler into local temp model
$Transfer -o config/jenaModels/h2.xml -O modelName=peopleSoftTempTransfer -O dbUrl="jdbc:h2:XMLVault/h2ps/all/store;MODE=HSQLDB" -h config/recordHandlers/PeopleSoft-RDF.xml -n http://vivotest.ctrip.ufl.edu/vivo/individual/

# backup H2 translate Models
date=`date +%Y-%m-%d_%T`
tar -czpf backups/ps.all.$date.tar.gz XMLVault/h2ps/all
rm -rf backups/ps.all.latest.tar.gz
ln -s ps.all.$date.tar.gz backups/ps.all.latest.tar.gz

# uncomment to restore previous H2 translate models
#tar -xzpf backups/ps.all.latest.tar.gz XMLVault/h2ps/all

# Execute ChangeNamespace to get into current namespace
$ChangeNamespace -i config/jenaModels/h2.xml -I modelName=peopleSoftTempTransfer -I dbUrl="jdbc:h2:XMLVault/h2ps/all/store;MODE=HSQLDB" -v config/jenaModels/VIVO.xml -n http://vivo.ufl.edu/individual/ -o http://vivoweb.org/harvest/peoplesoft/person/ -p http://vivo.ufl.edu/ontology/vivo-ufl/ufid
# backup H2 change namesace Models
date=`date +%Y-%m-%d_%T`
tar -czpf backups/ps.cnpeople.$date.tar.gz XMLVault/h2ps/all
rm -rf backups/ps.cnpeople.latest.tar.gz
ln -s ps.cnpeople.$date.tar.gz backups/ps.cnpeople.latest.tar.gz

# uncomment to restore previous changenamespace model
#tar -xzpf backups/ps.cnpeople.latest.tar.gz XMLVault/h2ps/all

$ChangeNamespace -i config/jenaModels/h2.xml -I modelName=peopleSoftTempTransfer -I dbUrl="jdbc:h2:XMLVault/h2ps/all/store;MODE=HSQLDB" -v config/jenaModels/VIVO.xml -n http://vivo.ufl.edu/individual/ -o http://vivoweb.org/harvest/peoplesoft/org/ -p http://vivo.ufl.edu/ontology/vivo-ufl/deptID -e
# backup H2 change namesace Models
date=`date +%Y-%m-%d_%T`
tar -czpf backups/ps.cnorg.$date.tar.gz XMLVault/h2ps/all
rm -rf backups/ps.cnorg.latest.tar.gz
ln -s ps.cnorg.$date.tar.gz backups/ps.cnorg.latest.tar.gz

# uncomment to restore previous changenamespace model
#tar -xzpf backups/ps.cnorg.latest.tar.gz XMLVault/h2ps/all

$ChangeNamespace -i config/jenaModels/h2.xml -I modelName=peopleSoftTempTransfer -I dbUrl="jdbc:h2:XMLVault/h2ps/all/store;MODE=HSQLDB" -v config/jenaModels/VIVO.xml -n http://vivo.ufl.edu/individual/ -o http://vivoweb.org/harvest/peoplesoft/position/ -p http://vivoweb.org/ontology/core#positionInOrganization -p http://vivoweb.org/ontology/core#positionForPerson -p http://vivo.ufl.edu/ontology/vivo-ufl/deptIDofPosition
# backup H2 change namesace Models
date=`date +%Y-%m-%d_%T`
tar -czpf backups/ps.cnpos.$date.tar.gz XMLVault/h2ps/all
rm -rf backups/ps.cnpos.latest.tar.gz
ln -s ps.cnpos.$date.tar.gz backups/ps.cnpos.latest.tar.gz

# uncomment to restore previous changenamespace model
#tar -xzpf backups/ps.cnpos.latest.tar.gz XMLVault/h2ps/all

# Backup pretransfer vivo database, symlink latest to latest.sql
date=`date +%Y-%m-%d_%T`
mysqldump -h $SERVER -u $USERNAME -p$PASSWORD $DBNAME > backups/$DBNAME.ps.pretransfer.$date.sql
rm -rf backups/$DBNAME.ps.pretransfer.latest.sql
ln -s $DBNAME.ps.pretransfer.$date.sql backups/$DBNAME.ps.pretransfer.latest.sql

#mysql -h $SERVER -u $USERNAME -p$PASSWORD -e "drop database $DBNAME;"
#mysql -h $SERVER -u $USERNAME -p$PASSWORD -e "create database $DBNAME;"
#mysql -h $SERVER -u $USERNAME -p$PASSWORD $DBNAME < backups/$DBNAME.ps.pretransfer.latest.sql

# Find Subtractions
$Diff -m config/jenaModels/VIVO.xml -M modelName="http://vivoweb.org/ingest/ufl/peoplesoft" -s config/jenaModels/h2.xml -S dbUrl="jdbc:h2:XMLVault/h2ps/all/store;MODE=HSQLDB" -S modelName=peopleSoftTempTransfer -d XMLVault/update_Subtractions.rdf.xml
# Find Additions
$Diff -m config/jenaModels/h2.xml -M dbUrl="jdbc:h2:XMLVault/h2ps/all/store;MODE=HSQLDB" -M modelName=peopleSoftTempTransfer -s config/jenaModels/VIVO.xml -S modelName="http://vivoweb.org/ingest/ufl/peoplesoft" -d XMLVault/update_Additions.rdf.xml
# Apply Subtractions to Previous model
$Transfer -o config/jenaModels/VIVO.xml -O modelName="http://vivoweb.org/ingest/ufl/peoplesoft" -r XMLVault/update_Subtractions.rdf.xml -m
# Apply Additions to Previous model
$Transfer -o config/jenaModels/VIVO.xml -O modelName="http://vivoweb.org/ingest/ufl/peoplesoft" -r XMLVault/update_Additions.rdf.xml
# Apply Subtractions to VIVO
$Transfer -o config/jenaModels/VIVO.xml -r XMLVault/update_Subtractions.rdf.xml -m
# Apply Additions to VIVO
$Transfer -o config/jenaModels/VIVO.xml -r XMLVault/update_Additions.rdf.xml

# Backup posttransfer vivo database, symlink latest to latest.sql
date=`date +%Y-%m-%d_%T`
mysqldump -h $SERVER -u $USERNAME -p$PASSWORD $DBNAME > backups/$DBNAME.ps.posttransfer.$date.sql
rm -rf backups/$DBNAME.ps.posttransfer.latest.sql
ln -s $DBNAME.ps.posttransfer.$date.sql backups/$DBNAME.ps.posttransfer.latest.sql

#mysql -h $SERVER -u $USERNAME -p$PASSWORD -e "drop database $DBNAME;"
#mysql -h $SERVER -u $USERNAME -p$PASSWORD -e "create database $DBNAME;"
#mysql -h $SERVER -u $USERNAME -p$PASSWORD $DBNAME < backups/$DBNAME.ps.posttransfer.latest.sql

# Restart Tomcat
# Tomcat must be restarted in order for the harvested data to appear in VIVO
/etc/init.d/tomcat restart