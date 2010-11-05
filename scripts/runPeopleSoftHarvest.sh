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

# Set working directory
cd /usr/share/vivoingest

# Execute Fetch
rm -rd XMLVault/h2ps/XML
java -Xms1024m -Xmx3072M -cp bin/ingest-$VERSION.jar:bin/dependency/* org.vivoweb.ingest.fetch.JDBCFetch -X config/tasks/PeopleSoftFetch.xml
date=`date +%Y-%m-%d_%k%M.%S`
tar -czpf backups/h2psXML.$date.tar.gz XMLVault/h2ps/XML
rm -rf backups/h2psXML.latest.tar.gz
ln -s h2psXML.$date.tar.gz backups/h2psXML.latest.tar.gz
#tar -xzpf backups/h2psXML.latest.tar.gz XMLVault/h2ps/XML

# Execute Translate
rm -rd XMLVault/h2ps/RDF
java -Xms1024m -Xmx3072M -cp bin/ingest-$VERSION.jar:bin/dependency/* org.vivoweb.ingest.translate.XSLTranslator -i config/recordHandlers/PeopleSoft-XML.xml -o config/recordHandlers/PeopleSoft-RDF.xml -x config/datamaps/PeopleSoftToVivo.xsl
date=`date +%Y-%m-%d_%k%M.%S`
tar -czpf backups/ps.rdf.$date.tar.gz XMLVault/h2ps/RDF
rm -rf backups/ps.rdf.latest.tar.gz
ln -s ps.rdf.$date.tar.gz backups/ps.rdf.latest.tar.gz
#tar -xzpf backups/ps.rdf.latest.tar.gz XMLVault/h2ps/RDF

# Execute Transfer to import from record handler into local temp model
rm -rd XMLVault/h2ps/all
java -Xms1024m -Xmx3072M -cp bin/ingest-$VERSION.jar:bin/dependency/* org.vivoweb.ingest.transfer.Transfer -o config/jenaModels/h2.xml -O modelName=peopleSoftTempTransfer -O dbUrl="jdbc:h2:XMLVault/h2ps/all/store;MODE=HSQLDB" -h config/recordHandlers/PeopleSoft-RDF.xml -n http://vivotest.ctrip.ufl.edu/vivo/individual/
date=`date +%Y-%m-%d_%k%M.%S`
tar -czpf backups/ps.all.$date.tar.gz XMLVault/h2ps/all
rm -rf backups/ps.all.latest.tar.gz
ln -s ps.all.$date.tar.gz backups/ps.all.latest.tar.gz
#tar -xzpf backups/ps.all.latest.tar.gz XMLVault/h2ps/all

# Execute Score to match jobs with organizations
rm -rd XMLVault/h2ps/scored
java -Xms1024m -Xmx3072M -cp bin/ingest-$VERSION.jar:bin/dependency/* org.vivoweb.ingest.score.Score -v config/jenaModels/VIVO.xml -i config/jenaModels/h2.xml -I dbUrl="jdbc:h2:XMLVault/h2all/store;MODE=HSQLDB" -I modelName=peopleSoftTempTransfer -o config/jenaModels/h2.xml -O dbUrl="jdbc:h2:XMLVault/h2scored/store;MODE=HSQLDB" -O modelName=peopleSoftStaging -f "http://vivoweb.org/ontology/score#deptID=http://vivo.ufl.edu/ontology/vivo-ufl/deptID" -x "http://vivoweb.org/ontology/core#positionInOrganization" -y "http://vivoweb.org/ontology/core#organizationForPosition"
date=`date +%Y-%m-%d_%k%M.%S`
tar -czpf backups/ps.scored.$date.tar.gz XMLVault/h2ps/scored
rm -rf backups/ps.scored.latest.tar.gz
ln -s ps.scored.$date.tar.gz backups/ps.scored.latest.tar.gz
#tar -xzpf backups/ps.scored.latest.tar.gz XMLVault/h2ps/scored

# Execute ChangeNamespace to get into corrent namespace

# Backup pretransfer vivo database
date=`date +%Y-%m-%d_%k%M.%S`
mysqldump -u USERNAME -pPASSWORD vitrodb > backups/vitrodb.ps.pretransfer.$date.sql
rm -rf backups/vitrodb.ps.pretransfer.latest.sql
ln -s vitrodb.ps.pretransfer.$date.sql backups/vitrodb.ps.pretransfer.latest.sql

# Execute Transfer from local temp model into main vivo model
java -Xms1024m -Xmx3072M -cp bin/ingest-$VERSION.jar:bin/dependency/* org.vivoweb.ingest.transfer.Transfer -i config/jenaModels/h2.xml -I modelName=peopleSoftStaging -I dbUrl="jdbc:h2:XMLVault/h2ps/scored/store;MODE=HSQLDB" -o config/jenaModels/VIVO.xml

# Backup posttransfer vivo database
date=`date +%Y-%m-%d_%k%M.%S`
mysqldump -u USERNAME -pPASSWORD vitrodb > backups/vitrodb.ps.posttransfer.$date.sql
rm -rf backups/vitrodb.ps.posttransfer.latest.sql
ln -s vitrodb.ps.posttransfer.$date.sql backups/vitrodb.ps.posttransfer.latest.sql

# Execute Transfer to copy into peopleSoft model
java -Xms1024m -Xmx3072M -cp bin/ingest-$VERSION.jar:bin/dependency/* org.vivoweb.ingest.transfer.Transfer -i config/jenaModels/h2.xml -I modelName=peopleSoftStaging -I dbUrl="jdbc:h2:XMLVault/h2ps/scored/store;MODE=HSQLDB" -o config/jenaModels/VIVO.xml -O modelName=peopleSoft

#Restart Tomcat
#Tomcat must be restarted in order for the harvested data to appear in VIVO
/etc/init.d/tomcat restart
sleep 20s
/etc/init.d/apache2 restart