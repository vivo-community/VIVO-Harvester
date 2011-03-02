#!/bin/bash

# Copyright (c) 2010 Eliza Chan
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the new BSD license
# which accompanies this distribution, and is available at
# http://www.opensource.org/licenses/bsd-license.html
# 
# Contributors:
#     Eliza Chan

# Exit on first error
set -e

# Set working directory
HARVESTERDIR=`dirname "$(cd "${0%/*}" 2>/dev/null; echo "$PWD"/"${0##*/}")"`
HARVESTERDIR=$(cd $HARVESTERDIR; cd ..; pwd)

HARVESTER_TASK=csvmap

if [ -f scripts/env ]; then
  . scripts/env
else
  exit 1
fi
echo "Full Logging in $HARVESTER_TASK_DATE.log"

BASEDIR=harvested-data/examples/$HARVESTER_TASK
RDFRHDIR=$BASEDIR/rh-rdf

# Execute Fetch/Translate using D2RMap
$D2RMapFetch -o $TFRH -OfileDir=$RDFRHDIR -u config/datamaps/example.csv-map.xml -a $DIR -s person.rdf

# Execute Transfer to transfer rdf into "d2rStaging" JENA model
$Transfer -h $TFRH -HfileDir=$RDFRHDIR -o $VIVOCONFIG -OmodelName=d2rStaging

# Execute Transfer to load "d2rStaging" JENA model into VIVO
$Transfer -i $VIVOCONFIG -ImodelName=d2rStaging -o $VIVOCONFIG

# Execute Transfer to dump "d2rStaging" JENA model rdf into file
# Shown as example
#$Transfer -i $VIVOCONFIG -ImodelName=d2rStaging -d dump.rdf.xml

#Restart Tomcat
#Tomcat must be restarted in order for the harvested data to appear in VIVO
#/etc/init.d/tomcat restart

