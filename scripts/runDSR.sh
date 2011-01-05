#!/bin/bash

# Copyright (c) 2010 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the new BSD license
# which accompanies this distribution, and is available at
# http://www.opensource.org/licenses/bsd-license.html
# 
# Contributors:
#     Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams - initial API and implementation
#	  James Pence

# Set working directory
cd `dirname $(readlink -f $0)`
cd ..

HARVESTER_TASK=dsr

if [ -f scripts/env ]; then
  . scripts/env
else
  exit 1
fi

# Setting variables for cleaner script lines.
$TEMPINPUT="-i config/jenaModels/h2.xml -I modelName=dsrTempTransfer -I dbUrl="jdbc:h2:XMLVault/h2dsr/All/store"
$SCOREDATA="-s config/jenaModels/h2.xml -S modelName=dsrScoreData -S dbUrl="jdbc:h2:XMLVault/h2dsr/All/store"
$CNFLAGS="$TEMPINPUT -v config/jenaModels/VIVO.xml -n http://vivo.ufl.edu/individual/"
$EQTEST="org.vivoweb.harvester.score.algorithm.EqualityTest"


# Execute Fetch
rm -rf XMLVault/h2dsr/xml
$JDBCFetch -X config/tasks/DSR-JDBCFetch.xml
tar -czpf backups/h2dsr-xml.tar.gz XMLVault/h2dsr/xml
#tar -xzpf backups/h2dsr-xml.tar.gz XMLVault/h2dsr/xml

# Execute Translate
rm -rf XMLVault/h2dsr/rdf
$XSLTranslator -i config/recordHandlers/DSR-XML-h2RH.xml -o config/recordHandlers/DSR-RDF-h2RH.xml -x config/datamaps/DSRtoVIVO.xsl
tar -czpf backups/h2dsr-rdf.tar.gz XMLVault/h2dsr/rdf
#tar -xzpf backups/h2dsr-rdf.tar.gz XMLVault/h2dsr/rdf

# Execute Transfer to import from record handler into local temp model
rm -rf XMLVault/h2dsr/All
$Transfer -o config/jenaModels/h2.xml -O modelName=dsrTempTransfer -O dbUrl="jdbc:h2:XMLVault/h2dsr/All/store;MODE=HSQLDB" -h config/recordHandlers/DSR-RDF-h2RH.xml -n http://vivo.ufl.edu/individual/
tar -czpf backups/h2dsr-All.tar.gz XMLVault/h2dsr/All
#tar -xzpf backups/h2dsr-All.tar.gz XMLVault/h2dsr/All

# Execute score to match with existing VIVO
# The -n flag value is determined by the XLST file
# The -A -W -F & -P flags need to be internally consistent per call
# Scoring on UF ID person
$Score -v config/jenaModels/VIVO.xml $TEMPINPUT $SCOREDATA -A ufid=$EQTEST -W ufid="1.0" -F ufid=http://vivo.ufl.edu/ontology/vivo-ufl/ufid -P ufid=http://vivo.ufl.edu/ontology/vivo-ufl/ufid -n http://vivoweb.org/harvest/dsr/person/

# Scoring on Dept ID
$Score -v config/jenaModels/VIVO.xml $TEMPINPUT $SCOREDATA -A deptID=$EQTEST -W deptID="1.0" -F deptID=http://vivo.ufl.edu/ontology/vivo-ufl/deptID -P deptID=http://vivo.ufl.edu/ontology/vivo-ufl/deptID -n http://vivoweb.org/harvest/dsr/org/

# Scoring sponsors by labels
$Score -v config/jenaModels/VIVO.xml $TEMPINPUT $SCOREDATA -A label=$EQTEST -W label="1.0" -F label=http://www.w3.org/2000/01/rdf-schema#label -P label=http://www.w3.org/2000/01/rdf-schema#label -n http://vivoweb.org/harvest/dsr/sponsor/

# Scoring of PIs
$Score -v config/jenaModels/VIVO.xml $TEMPINPUT $SCOREDATA -A type=$EQTEST -W type="1.0" -F type=http://www.w3.org/1999/02/22-rdf-syntax-ns#type -P type=http://www.w3.org/1999/02/22-rdf-syntax-ns#type -n http://vivoweb.org/harvest/dsr/piRole/

# Scoring of coPIs
$Score -v config/jenaModels/VIVO.xml $TEMPINPUT $SCOREDATA -A type=$EQTEST -W type="1.0" -F type=http://www.w3.org/1999/02/22-rdf-syntax-ns#type -P type=http://www.w3.org/1999/02/22-rdf-syntax-ns#type -n http://vivoweb.org/harvest/dsr/coPiRole/

# Find matches using scores and rename nodes to matching uri
$Match -v config/jenaModels/VIVO.xml $TEMPINPUT $SCOREDATA -t"1.0" -r

# Execute ChangeNamespace to get grants into current namespace
# the -o flag value is determined by the XSLT used to translate the data
$ChangeNamespace $CNFLAGS -o http://vivoweb.org/harvest/dsr/grant/
# backup H2 change namesace Models
date=`date +%Y-%m-%d_%T`
tar -czpf backups/dsr.cngrant.$date.tar.gz XMLVault/h2dsr/All
rm -rf backups/dsr.cngrant.latest.tar.gz
ln -s dsr.cngrant.$date.tar.gz backups/dsr.cngrant.latest.tar.gz

# uncomment to restore previous changenamespace model
#tar -xzpf backups/dsr.cngrant.latest.tar.gz XMLVault/h2dsr/All

# Execute ChangeNamespace to get orgs into current namespace
# the -o flag value is determined by the XSLT used to translate the data
$ChangeNamespace $CNFLAGS -o http://vivoweb.org/harvest/dsr/org/
# backup H2 change namesace Models
date=`date +%Y-%m-%d_%T`
tar -czpf backups/dsr.cnorg.$date.tar.gz XMLVault/h2dsr/All
rm -rf backups/dsr.cnorg.latest.tar.gz
ln -s dsr.cnorg.$date.tar.gz backups/dsr.cnorg.latest.tar.gz

# uncomment to restore previous changenamespace model
#tar -xzpf backups/dsr.cnorg.latest.tar.gz XMLVault/h2dsr/All

# Execute ChangeNamespace to get sponsors into current namespace
# the -o flag value is determined by the XSLT used to translate the data
$ChangeNamespace $CNFLAGS -o http://vivoweb.org/harvest/dsr/sponsor/
# backup H2 change namesace Models
date=`date +%Y-%m-%d_%T`
tar -czpf backups/dsr.cnsponsor.$date.tar.gz XMLVault/h2dsr/All
rm -rf backups/dsr.cnsponsor.latest.tar.gz
ln -s dsr.cnsponsor.$date.tar.gz backups/dsr.cnsponsor.latest.tar.gz

# uncomment to restore previous changenamespace model
#tar -xzpf backups/dsr.cnsponsor.latest.tar.gz XMLVault/h2dsr/All

# Execute ChangeNamespace to get people into current namespace
# the -o flag value is determined by the XSLT used to translate the data
$ChangeNamespace $CNFLAGS -o http://vivoweb.org/harvest/dsr/person/
# backup H2 change namesace Models
date=`date +%Y-%m-%d_%T`
tar -czpf backups/dsr.cnpeople.$date.tar.gz XMLVault/h2dsr/All
rm -rf backups/dsr.cnpeople.latest.tar.gz
ln -s dsr.cnpeople.$date.tar.gz backups/dsr.cnpeople.latest.tar.gz

# uncomment to restore previous changenamespace model
#tar -xzpf backups/dsr.cnpeople.latest.tar.gz XMLVault/h2dsr/All

# Execute ChangeNamespace to get PI roles into current namespace
# the -o flag value is determined by the XSLT used to translate the data
$ChangeNamespace $CNFLAGS -o http://vivoweb.org/harvest/dsr/piRole/
# backup H2 change namesace Models
date=`date +%Y-%m-%d_%T`
tar -czpf backups/dsr.cnpirole.$date.tar.gz XMLVault/h2dsr/All
rm -rf backups/dsr.cnpirole.latest.tar.gz
ln -s dsr.cnpirole.$date.tar.gz backups/dsr.cnpirole.latest.tar.gz

# uncomment to restore previous changenamespace model
#tar -xzpf backups/dsr.cnpirole.latest.tar.gz XMLVault/h2dsr/All

# Execute ChangeNamespace to get co-PI roles into current namespace
# the -o flag value is determined by the XSLT used to translate the data
$ChangeNamespace $CNFLAGS -o http://vivoweb.org/harvest/dsr/coPiRole/
# backup H2 change namesace Models
date=`date +%Y-%m-%d_%T`
tar -czpf backups/dsr.cncopirole.$date.tar.gz XMLVault/h2dsr/All
rm -rf backups/dsr.cncopirole.latest.tar.gz
ln -s dsr.cncopirole.$date.tar.gz backups/dsr.cncopirole.latest.tar.gz

#uncomment to restore previous changenamespace model
#tar -xzpf backups/dsr.cncopirole.latest.tar.gz XMLVault/h2dsr/All

# Backup pretransfer vivo database, symlink latest to latest.sql
date=`date +%Y-%m-%d_%T`
mysqldump -h $SERVER -u $USERNAME -p$PASSWORD $DBNAME > backups/$DBNAME.dsr.pretransfer.$date.sql
ln -sf $DBNAME.dsr.pretransfer.$date.sql backups/$DBNAME.dsr.pretransfer.latest.sql

#mysql -h $SERVER -u $USERNAME -p$PASSWORD -e "drop database $DBNAME;"
#mysql -h $SERVER -u $USERNAME -p$PASSWORD -e "create database $DBNAME;"
#mysql -h $SERVER -u $USERNAME -p$PASSWORD $DBNAME < backups/$DBNAME.dsr.pretransfer.latest.sql

# Find Subtractions
$Diff -m config/jenaModels/VIVO.xml -M modelName="http://vivoweb.org/ingest/dsr" -s config/jenaModels/h2.xml -S dbUrl="jdbc:h2:XMLVault/h2dsr/All/store;MODE=HSQLDB" -S modelName=dsrTempTransfer -d XMLVault/update_DSR_Subtractions.rdf.xml
# Find Additions
$Diff -m config/jenaModels/h2.xml -M dbUrl="jdbc:h2:XMLVault/h2dsr/All/store;MODE=HSQLDB" -M modelName=dsrTempTransfer -s config/jenaModels/VIVO.xml -S modelName="http://vivoweb.org/ingest/dsr" -d XMLVault/update_DSR_Additions.rdf.xml
# Apply Subtractions to Previous model
$Transfer -o config/jenaModels/VIVO.xml -O modelName="http://vivoweb.org/ingest/dsr" -r XMLVault/update_DSR_Subtractions.rdf.xml -m
# Apply Additions to Previous model
$Transfer -o config/jenaModels/VIVO.xml -O modelName="http://vivoweb.org/ingest/dsr" -r XMLVault/update_DSR_Additions.rdf.xml
# Apply Subtractions to VIVO
$Transfer -o config/jenaModels/VIVO.xml -r XMLVault/update_DSR_Subtractions.rdf.xml -m
# Apply Additions to VIVO
$Transfer -o config/jenaModels/VIVO.xml -r XMLVault/update_DSR_Additions.rdf.xml

# Backup posttransfer vivo database, symlink latest to latest.sql
date=`date +%Y-%m-%d_%T`
mysqldump -h $SERVER -u $USERNAME -p$PASSWORD $DBNAME > backups/$DBNAME.dsr.posttransfer.$date.sql
ln -sf $DBNAME.dsr.posttransfer.$date.sql backups/$DBNAME.dsr.posttransfer.latest.sql

#mysql -h $SERVER -u $USERNAME -p$PASSWORD -e "drop database $DBNAME;"
#mysql -h $SERVER -u $USERNAME -p$PASSWORD -e "create database $DBNAME;"
#mysql -h $SERVER -u $USERNAME -p$PASSWORD $DBNAME < backups/$DBNAME.dsr.posttransfer.latest.sql

#Restart Tomcat
#Tomcat must be restarted in order for the harvested data to appear in VIVO
/etc/init.d/tomcat restart
