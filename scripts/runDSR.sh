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
cd /usr/share/vivo/harvester

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
$XSLTranslator -i config/recordHandlers/DSR-XML.xml -o config/recordHandlers/DSR-RDF.xml -x config/datamaps/DSRtoVIVO.xsl
tar -czpf backups/h2dsr-rdf.tar.gz XMLVault/h2dsr/rdf
#tar -xzpf backups/h2dsr-rdf.tar.gz XMLVault/h2dsr/rdf

# Execute Transfer to import from record handler into local temp model
rm -rf XMLVault/h2dsr/All
$Transfer -o config/jenaModels/h2.xml -O modelName=dsrTempTransfer -O dbUrl="jdbc:h2:XMLVault/h2dsr/All/store;MODE=HSQLDB" -h config/recordHandlers/DSR-RDF.xml -n http://vivotest.ctrip.ufl.edu/vivo/individual/
tar -czpf backups/h2dsr-All.tar.gz XMLVault/h2dsr/All
#tar -xzpf backups/h2dsr-All.tar.gz XMLVault/h2dsr/All

# Execute Score to match jobs with organizations
rm -rf XMLVault/h2dsr/scored
$Score -v config/jenaModels/myVIVO.xml -i config/jenaModels/h2.xml -I dbUrl="jdbc:h2:XMLVault/h2dsr/All/store;MODE=HSQLDB" -I modelName=dsrTempTransfer -o config/jenaModels/h2.xml -O dbUrl="jdbc:h2:XMLVault/h2dsr/scored/store;MODE=HSQLDB" -O modelName=dsrStaging -f "http://vivoweb.org/ontology/score#ufid=http://vivo.ufl.edu/ontology/vivo-ufl/ufid" -x "http://vivoweb.org/ontology/core#principalInvestigatorRoleOf" -y "http://vivoweb.org/ontology/core#relatedRole"

# Backup pretransfer vivo database, symlink latest to latest.sql
date=`date +%Y-%m-%d_%T`
mysqldump -h $SERVER -u $USERNAME -p$PASSWORD $DBNAME > backups/$DBNAME.dsr.pretransfer.$date.sql
rm -rf backups/$DBNAME.dsr.pretransfer.latest.sql
ln -s $DBNAME.dsr.pretransfer.$date.sql backups/$DBNAME.dsr.pretransfer.latest.sql

#Update VIVO, using previous model as comparison. On first run, previous model won't exist resulting in all statements being passed to VIVO  
$Update -p config/jenaModels/VIVO.xml -P modelName="http://vivoweb.org/ingest/dsr" -i config/jenaModels/h2.xml -I dbUrl="jdbc:h2:XMLVault/h2dsr/scored/store;MODE=HSQLDB" -I modelName=DSRStaging -v config/jenaModels/VIVO.xml

# Backup posttransfer vivo database, symlink latest to latest.sql
date=`date +%Y-%m-%d_%T`
mysqldump -h $SERVER -u $USERNAME -p$PASSWORD $DBNAME > backups/$DBNAME.dsr.posttransfer.$date.sql
rm -rf backups/$DBNAME.dsr.posttransfer.latest.sql
ln -s $DBNAME.dsr.posttransfer.$date.sql backups/$DBNAME.dsr.posttransfer.latest.sql

#Restart Tomcat
#Tomcat must be restarted in order for the harvested data to appear in VIVO
/etc/init.d/tomcat restart
