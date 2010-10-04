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
cd /usr/share/vivoingest

# Delete local temp model and record handlers
rm -rd XMLVault/h2all
rm -rd XMLVault/PeopleSoft-*

# Execute Fetch
java -cp bin/ingest-0.5.0.jar:bin/dependency/* org.vivoweb.ingest.fetch.JDBCFetch -X config/tasks/PeopleSoft-JDBCFetch.xml

# Execute Translate
java -cp bin/ingest-0.5.0.jar:bin/dependency/* org.vivoweb.ingest.translate.XSLTranslator -i config/recordHandlers/PeopleSoft-XML-h2RH.xml -o config/recordHandlers/PeopleSoft-RDF-h2RH.xml -x config/datamaps/PeopleSoftToVivo.xsl

# Execute Transfer to import from record handler into local temp model
java -Xms1024m -Xmx1024M -cp bin/ingest-0.5.0.jar:bin/dependency/* org.vivoweb.ingest.transfer.Transfer -o config/jenaModels/h2.xml -O modelName=peopleSoftTempTransfer -O dbUrl="jdbc:h2:XMLVault/h2all/store;MODE=HSQLDB" -k -h config/recordHandlers/PeopleSoft-RDF-h2RH.xml -n http://vivotest.ctrip.ufl.edu/vivo/individual/

date=`date +%Y-%m-%d_%k%M.%S`
mysqldump -u USERNAME -pPASSWORD vitrodb > backups/vitrodb.$date.sql
rm -rf backups/vitrodb.current.sql
ln -s vitrodb.$date.sql backups/vitrodb.current.sql
mysql -u USERNAME -pPASSWORD -e 'drop database vitrodb;'
mysql -u USERNAME -pPASSWORD -e 'create database vitrodb;'
mysql -u USERNAME -pPASSWORD vitrodb < backups/blankVivo.sql

# Execute Transfer from local temp model into main vivo model
java -Xms1024m -Xmx1024M -cp bin/ingest-0.5.0.jar:bin/dependency/* org.vivoweb.ingest.transfer.Transfer -i config/jenaModels/h2.xml -I modelName=peopleSoftTempTransfer -I dbUrl="jdbc:h2:XMLVault/h2all/store;MODE=HSQLDB" -o config/jenaModels/VIVO.xml -k

# Execute Transfer to copy into peopleSoft model
#java -Xms1024m -Xmx1024M -cp bin/ingest-0.5.0.jar:bin/dependency/* org.vivoweb.ingest.transfer.Transfer -i config/jenaModels/h2.xml -I modelName=peopleSoftTempTransfer -I dbUrl="jdbc:h2:XMLVault/h2all/store;MODE=HSQLDB" -o config/jenaModels/VIVO.xml -k -O modelName=peopleSoft

#Restart Tomcat
#Tomcat must be restarted in order for the harvested data to appear in VIVO
/etc/init.d/tomcat restart
Sleep 120
/etc/init.d/apache2 restart
