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

HARVESTER_TASK=oai

if [ -f scripts/env ]; then
  . scripts/env
else
  exit 1
fi

#clear old fetches
rm -rf XMLVault/h2oai/XML

# Execute Fetch for OAI
$OAIFetch -X config/tasks/OAIFetch.xml

# backup fetch
date=`date +%Y-%m-%d_%T`
tar -czpf backups/oai.xml.$date.tar.gz XMLVault/h2oai/XML
rm -rf backups/oai.xml.latest.tar.gz
ln -s oai.xml.$date.tar.gz backups/oai.xml.latest.tar.gz

# uncomment to restore previous fetch
#tar -xzpf backups/oai.xml.latest.tar.gz XMLVault/h2oai/XML

# clear old translates
rm -rf XMLVault/h2oai/RDF

# Execute Translate
$XSLTranslator -i config/recordHandlers/OAIXMLRecordHandler.xml -x DataMaps/OAIDublinCoreToVIVO.xsl -o config/recordHandlers/OAIRDFRecordHandler.xml

# backup translate
date=`date +%Y-%m-%d_%T`
tar -czpf backups/oai.rdf.$date.tar.gz XMLVault/h2oai/RDF
rm -rf backups/oai.rdf.latest.tar.gz
ln -s oai.rdf.$date.tar.gz backups/oai.rdf.latest.tar.gz

# uncomment to restore previous translate
#tar -xzpf backups/oai.rdf.latest.tar.gz XMLVault/h2oai/RDF

# Clear old H2 models
rm -rf XMLVault/h2oai/all

# Execute Transfer to transfer rdf into "scoring" JENA model
$Transfer -h config/recordHandlers/OAIRDFRecordHandler.xml -o config/jenaModels/h2.xml -OmodelName=oaiTempTransfer -OdbUrl=jdbc:h2:XMLVault/h2oai/all/store

# backup H2 translate Models
date=`date +%Y-%m-%d_%T`
tar -czpf backups/oai.all.$date.tar.gz XMLVault/h2oai/all
rm -rf backups/oai.all.latest.tar.gz
ln -s oai.all.$date.tar.gz backups/oai.all.latest.tar.gz

# uncomment to restore previous H2 translate models
#tar -xzpf backups/oai.all.latest.tar.gz XMLVault/h2oai/all

# Execute Transfer to load "staging" JENA model into VIVO
$Transfer -i config/jenaModels/h2.xml -ImodelName=oaiTempTransfer -IdbUrl=jdbc:h2:XMLVault/h2oai/all/store -o $VIVOCONFIG -OmodelName=oaiDemo

#Restart Tomcat
#Tomcat must be restarted in order for the harvested data to appear in VIVO
/etc/init.d/tomcat restart
