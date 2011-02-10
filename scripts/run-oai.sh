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
set -e

DIR=$(cd "$(dirname "$0")"; pwd)
cd $DIR
cd ..

HARVESTER_TASK=oai

if [ -f scripts/env ]; then
  . scripts/env
else
  exit 1
fi
echo "Full Logging in $HARVESTER_TASK_DATE.log"

BASEDIR=harvested-data/examples/$HARVESTER_TASK
RAWRHDIR=$BASEDIR/rh-raw
RDFRHDIR=$BASEDIR/rh-rdf
MODELDIR=$BASEDIR/model
MODELDBURL=jdbc:h2:$MODELDIR/store

#clear old fetches
rm -rf $RAWRHDIR

# Execute Fetch for OAI
$OAIFetch -X config/tasks/example.oaifetch.xml -o $TFRH -OfileDir=$RAWRHDIR

# backup fetch
BACKRAW="raw"
backup-path $RAWRHDIR $BACKRAW
# uncomment to restore previous fetch
#restore-path $RAWRHDIR $BACKRAW

# clear old translates
rm -rf $RDFRHDIR

# Execute Translate
$XSLTranslator -i $TFRH -IfileDir=$RAWRHDIR -x config/datamaps/oai-dc-to-vivo.xsl -o $TFRH -OfileDir=$RDFRHDIR

# backup translate
BACKRDF="rdf"
backup-path $RDFRHDIR $BACKRDF
# uncomment to restore previous translate
#restore-path $RDFRHDIR $BACKRDF

# Clear old H2 models
rm -rf $MODELDIR

# Execute Transfer to transfer rdf into "scoring" JENA model
$Transfer -h $TFRH -HfileDir=$RDFRHDIR -o $H2MODEL -OmodelName=oaiTempTransfer -OdbUrl=$MODELDBURL

# backup H2 transfer Model
BACKMODEL="model"
backup-path $MODELDIR $BACKMODEL
# uncomment to restore previous H2 transfer Model
#restore-path $MODELDIR $BACKMODEL

# Execute Transfer to load "staging" JENA model into VIVO
$Transfer -i $H2MODEL -ImodelName=oaiTempTransfer -IdbUrl=$MODELDBURL -o $VIVOCONFIG -OmodelName=oaiDemo

#Restart Tomcat
#Tomcat must be restarted in order for the harvested data to appear in VIVO
/etc/init.d/tomcat restart
