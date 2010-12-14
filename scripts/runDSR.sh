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
# Matching on UF ID person
$Score -v config/jenaModels/VIVO.xml -i config/jenaModels/h2.xml -I modelName=dsrTempTransfer -I dbUrl="jdbc:h2:XMLVault/h2dsr/All/store;MODE=HSQLDB" -m http://vivo.ufl.edu/ontology/vivo-ufl/ufid=http://vivo.ufl.edu/ontology/vivo-ufl/ufid -n http://vivoweb.org/harvest/dsr/person/ -r -c

# Matching on Dept ID
$Score -v config/jenaModels/VIVO.xml -i config/jenaModels/h2.xml -I modelName=dsrTempTransfer -I dbUrl="jdbc:h2:XMLVault/h2dsr/All/store;MODE=HSQLDB" -m http://vivo.ufl.edu/ontology/vivo-ufl/deptID=http://vivo.ufl.edu/ontology/vivo-ufl/deptID -n http://vivoweb.org/harvest/dsr/org/ -r -c

# Matching sponsors by labels
$Score -v config/jenaModels/VIVO.xml -i config/jenaModels/h2.xml -I modelName=dsrTempTransfer -I dbUrl="jdbc:h2:XMLVault/h2dsr/All/store;MODE=HSQLDB" -m http://www.w3.org/2000/01/rdf-schema#label=http://www.w3.org/2000/01/rdf-schema#label -n http://vivoweb.org/harvest/dsr/sponsor/ -r -c

# Matching of PIs
$Score -v config/jenaModels/VIVO.xml -i config/jenaModels/h2.xml -I modelName=dsrTempTransfer -I dbUrl="jdbc:h2:XMLVault/h2dsr/All/store;MODE=HSQLDB" -n http://vivoweb.org/harvest/dsr/piRole/ -m http://www.w3.org/1999/02/22-rdf-syntax-ns#type=http://www.w3.org/1999/02/22-rdf-syntax-ns#type -m http://vivoweb.org/ontology/core#roleIn=http://vivoweb.org/ontology/core#roleIn -m http://vivoweb.org/ontology/core#principalInvestigatorRoleOf=http://vivoweb.org/ontology/core#principalInvestigatorRoleOf -r -c

# Matching of coPIs
$Score -v config/jenaModels/VIVO.xml -i config/jenaModels/h2.xml -I modelName=dsrTempTransfer -I dbUrl="jdbc:h2:XMLVault/h2dsr/All/store;MODE=HSQLDB" -n http://vivoweb.org/harvest/dsr/coPiRole/ -m http://www.w3.org/1999/02/22-rdf-syntax-ns#type=http://www.w3.org/1999/02/22-rdf-syntax-ns#type -m http://vivoweb.org/ontology/core#roleIn=http://vivoweb.org/ontology/core#roleIn -m http://vivoweb.org/ontology/core#co-PrincipalInvestigatorRoleOf=http://vivoweb.org/ontology/core#co-PrincipalInvestigatorRoleOf -r -c

# Execute ChangeNamespace to get grants into current namespace
$ChangeNamespace -i config/jenaModels/h2.xml -I modelName=dsrTempTransfer -I dbUrl="jdbc:h2:XMLVault/h2dsr/All/store;MODE=HSQLDB" -v config/jenaModels/VIVO.xml -n http://vivo.ufl.edu/individual/ -o http://vivoweb.org/harvest/dsr/grant/
# backup H2 change namesace Models
date=`date +%Y-%m-%d_%T`
tar -czpf backups/dsr.cngrant.$date.tar.gz XMLVault/h2dsr/All
rm -rf backups/dsr.cngrant.latest.tar.gz
ln -s dsr.cngrant.$date.tar.gz backups/dsr.cngrant.latest.tar.gz

# uncomment to restore previous changenamespace model
#tar -xzpf backups/dsr.cngrant.latest.tar.gz XMLVault/h2dsr/All

# Execute ChangeNamespace to get orgs into current namespace
$ChangeNamespace -i config/jenaModels/h2.xml -I modelName=dsrTempTransfer -I dbUrl="jdbc:h2:XMLVault/h2dsr/All/store;MODE=HSQLDB" -v config/jenaModels/VIVO.xml -n http://vivo.ufl.edu/individual/ -o http://vivoweb.org/harvest/dsr/org/
# backup H2 change namesace Models
date=`date +%Y-%m-%d_%T`
tar -czpf backups/dsr.cnorg.$date.tar.gz XMLVault/h2dsr/All
rm -rf backups/dsr.cnorg.latest.tar.gz
ln -s dsr.cnorg.$date.tar.gz backups/dsr.cnorg.latest.tar.gz

# uncomment to restore previous changenamespace model
#tar -xzpf backups/dsr.cnorg.latest.tar.gz XMLVault/h2dsr/All

# Execute ChangeNamespace to get sponsors into current namespace
$ChangeNamespace -i config/jenaModels/h2.xml -I modelName=dsrTempTransfer -I dbUrl="jdbc:h2:XMLVault/h2dsr/All/store;MODE=HSQLDB" -v config/jenaModels/VIVO.xml -n http://vivo.ufl.edu/individual/ -o http://vivoweb.org/harvest/dsr/sponsor/
# backup H2 change namesace Models
date=`date +%Y-%m-%d_%T`
tar -czpf backups/dsr.cnsponsor.$date.tar.gz XMLVault/h2dsr/All
rm -rf backups/dsr.cnsponsor.latest.tar.gz
ln -s dsr.cnsponsor.$date.tar.gz backups/dsr.cnsponsor.latest.tar.gz

# uncomment to restore previous changenamespace model
#tar -xzpf backups/dsr.cnsponsor.latest.tar.gz XMLVault/h2dsr/All

# Execute ChangeNamespace to get people into current namespace
$ChangeNamespace -i config/jenaModels/h2.xml -I modelName=dsrTempTransfer -I dbUrl="jdbc:h2:XMLVault/h2dsr/All/store;MODE=HSQLDB" -v config/jenaModels/VIVO.xml -n http://vivo.ufl.edu/individual/ -o http://vivoweb.org/harvest/dsr/person/
# backup H2 change namesace Models
date=`date +%Y-%m-%d_%T`
tar -czpf backups/dsr.cnpeople.$date.tar.gz XMLVault/h2dsr/All
rm -rf backups/dsr.cnpeople.latest.tar.gz
ln -s dsr.cnpeople.$date.tar.gz backups/dsr.cnpeople.latest.tar.gz

# uncomment to restore previous changenamespace model
#tar -xzpf backups/dsr.cnpeople.latest.tar.gz XMLVault/h2dsr/All

# Execute ChangeNamespace to get PI roles into current namespace
$ChangeNamespace -i config/jenaModels/h2.xml -I modelName=dsrTempTransfer -I dbUrl="jdbc:h2:XMLVault/h2dsr/All/store;MODE=HSQLDB" -v config/jenaModels/VIVO.xml -n http://vivo.ufl.edu/individual/ -o http://vivoweb.org/harvest/dsr/piRole/
# backup H2 change namesace Models
date=`date +%Y-%m-%d_%T`
tar -czpf backups/dsr.cnpirole.$date.tar.gz XMLVault/h2dsr/All
rm -rf backups/dsr.cnpirole.latest.tar.gz
ln -s dsr.cnpirole.$date.tar.gz backups/dsr.cnpirole.latest.tar.gz

# uncomment to restore previous changenamespace model
#tar -xzpf backups/dsr.cnpirole.latest.tar.gz XMLVault/h2dsr/All

# Execute ChangeNamespace to get co-PI roles into current namespace
$ChangeNamespace -i config/jenaModels/h2.xml -I modelName=dsrTempTransfer -I dbUrl="jdbc:h2:XMLVault/h2dsr/All/store;MODE=HSQLDB" -v config/jenaModels/VIVO.xml -n http://vivo.ufl.edu/individual/ -o http://vivoweb.org/harvest/dsr/coPiRole/
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
