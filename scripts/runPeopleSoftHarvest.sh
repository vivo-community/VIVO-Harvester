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
rm -rd XMLVault/h2scored
mysql -u USERNAME -pPASSWORD -e 'drop database peopleSoftXML;'
mysql -u USERNAME -pPASSWORD -e 'create database peopleSoftXML;'
mysql -u USERNAME -pPASSWORD -e 'drop database peopleSoftRDF;'
mysql -u USERNAME -pPASSWORD -e 'create database peopleSoftRDF;'

# Execute Fetch
java -Xms1024m -Xmx1024M -cp bin/ingest-0.5.0.jar:bin/dependency/* org.vivoweb.ingest.fetch.JDBCFetch -X config/tasks/PeopleSoft-JDBCFetch.xml

# Execute Translate
java -Xms1024m -Xmx1024M -cp bin/ingest-0.5.0.jar:bin/dependency/* org.vivoweb.ingest.translate.XSLTranslator -i config/recordHandlers/PeopleSoft-XML-RH.xml -o config/recordHandlers/PeopleSoft-RDF-RH.xml -x config/datamaps/PeopleSoftToVivo.xsl

# Execute Transfer to import from record handler into local temp model
java -Xms1024m -Xmx1024M -cp bin/ingest-0.5.0.jar:bin/dependency/* org.vivoweb.ingest.transfer.Transfer -o config/jenaModels/h2.xml -O modelName=peopleSoftTempTransfer -O dbUrl="jdbc:h2:XMLVault/h2all/store;MODE=HSQLDB" -h config/recordHandlers/PeopleSoft-RDF-RH.xml -n http://vivotest.ctrip.ufl.edu/vivo/individual/

# Execute Score to match jobs with organizations
java -Xms1024m -Xmx1024M -cp bin/ingest-0.5.0.jar:bin/dependency/* org.vivoweb.ingest.score.Score -v config/jenaModels/VIVO.xml -i config/jenaModels/h2.xml -I dbUrl="jdbc:h2:XMLVault/h2all/store;MODE=HSQLDB" -I modelName=peopleSoftTempTransfer -o config/jenaModels/h2.xml -O dbUrl="jdbc:h2:XMLVault/h2scored/store;MODE=HSQLDB" -O modelName=peopleSoftStaging -f "http://vivoweb.org/ontology/score#deptID=http://vivo.ufl.edu/ontology/vivo-ufl/deptID" -x "http://vivoweb.org/ontology/core#positionInOrganization" -y "http://vivoweb.org/ontology/core#organizationForPosition"

date=`date +%Y-%m-%d_%k%M.%S`
mysqldump -u USERNAME -pPASSWORD vitrodb > backups/vitrodb.$date.sql
rm -rf backups/vitrodb.current.sql
ln -s vitrodb.$date.sql backups/vitrodb.current.sql
mysql -u USERNAME -pPASSWORD -e 'drop database vitrodb;'
mysql -u USERNAME -pPASSWORD -e 'create database vitrodb;'
mysql -u USERNAME -pPASSWORD vitrodb < backups/blankVivo.sql
java -Xms1024m -Xmx1024M -cp bin/ingest-0.5.0.jar:bin/dependency/* org.vivoweb.ingest.transfer.Transfer -r backups/uf.owl.rdf.xml -o config/jenaModels/VIVO.xml
java -Xms1024m -Xmx1024M -cp bin/ingest-0.5.0.jar:bin/dependency/* org.vivoweb.ingest.transfer.Transfer -r backups/ufOrgs.rdf.xml -o config/jenaModels/VIVO.xml

# Execute Transfer from local temp model into main vivo model
java -Xms1024m -Xmx1024M -cp bin/ingest-0.5.0.jar:bin/dependency/* org.vivoweb.ingest.transfer.Transfer -i config/jenaModels/h2.xml -I modelName=peopleSoftTempTransfer -I dbUrl="jdbc:h2:XMLVault/h2all/store;MODE=HSQLDB" -o config/jenaModels/VIVO.xml

# Execute Transfer to copy into peopleSoft model
java -Xms1024m -Xmx1024M -cp bin/ingest-0.5.0.jar:bin/dependency/* org.vivoweb.ingest.transfer.Transfer -i config/jenaModels/h2.xml -I modelName=peopleSoftTempTransfer -I dbUrl="jdbc:h2:XMLVault/h2all/store;MODE=HSQLDB" -o config/jenaModels/VIVO.xml -O modelName=peopleSoft

#Restart Tomcat
#Tomcat must be restarted in order for the harvested data to appear in VIVO
/etc/init.d/tomcat restart
sleep 20s
/etc/init.d/apache2 restart