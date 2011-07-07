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
HARVESTERDIR=$(cd "$(dirname "$0")"; cd ..; pwd)
cd $HARVESTERDIR

HARVESTER_TASK=d2rdbmap

if [ -f scripts/env ]; then
  . scripts/env
else
  exit 1
fi
echo "Full Logging in $HARVESTER_TASK_DATE.log"

BASEDIR=harvested-data/examples/$HARVESTER_TASK
RDFRHDIR=$BASEDIR/rh-rdf
RDFRHDBURL=jdbc:h2:$RDFRHDIR/store
PREVHARVESTMODEL="http://vivoweb.org/ingest/people"
ADDFILE="$BASEDIR/additions.rdf.xml"
SUBFILE="$BASEDIR/subtractions.rdf.xml"
MODELDIR=$BASEDIR/model
MODELDBURL=jdbc:h2:$MODELDIR/store
MODELNAME=people

#clear old fetches/translates
rm -rf $RDFRHDIR

# Execute Fetch/Translate using D2RMap
$D2RMapFetch -X config/tasks/example.d2rmapfetch.xml -o $H2RH -OdbUrl=$RDFRHDBURL

# backup fetch
BACKRDF="rdf"
backup-path $RDFRHDIR $BACKRDF
# uncomment to restore previous fetch
#restore-path $RDFRHDIR $BACKRDF

# Clear old H2 transfer model
rm -rf $MODELDIR

# Execute Transfer to import from record handler into local temp model
$Transfer -o $H2MODEL -OmodelName=$MODELNAME -OcheckEmpty=$CHECKEMPTY -OdbUrl=$MODELDBURL -s $H2RH -SdbUrl=$RDFRHDBURL -n $NAMESPACE

# backup H2 transfer Model
BACKMODEL="model"
backup-path $MODELDIR $BACKMODEL
# uncomment to restore previous H2 transfer Model
#restore-path $MODELDIR $BACKMODEL

# Backup pretransfer vivo database, symlink latest to latest.sql
BACKPREDB="pretransfer"
backup-mysqldb $BACKPREDB
# uncomment to restore pretransfer vivo database
#restore-mysqldb $BACKPREDB

# Find Subtractions
$Diff -m $H2MODEL -MdbUrl=${PREVHARVDBURLBASE}${HARVESTER_TASK}/store -McheckEmpty=$CHECKEMPTY -MmodelName=$PREVHARVESTMODEL -s $H2MODEL -ScheckEmpty=$CHECKEMPTY -SdbUrl=$MODELDBURL -SmodelName=$MODELNAME -d $SUBFILE
# Find Additions
$Diff -m $H2MODEL -McheckEmpty=$CHECKEMPTY -MdbUrl=$MODELDBURL -MmodelName=$MODELNAME -s $H2MODEL -SdbUrl=${PREVHARVDBURLBASE}${HARVESTER_TASK}/store -ScheckEmpty=$CHECKEMPTY -SmodelName=$PREVHARVESTMODEL -d $ADDFILE

# Backup adds and subs
backup-file $ADDFILE adds.rdf.xml
backup-file $SUBFILE subs.rdf.xml

# Apply Subtractions to Previous model
$Transfer -o $H2MODEL -OdbUrl=${PREVHARVDBURLBASE}${HARVESTER_TASK}/store -OcheckEmpty=$CHECKEMPTY -OmodelName=$PREVHARVESTMODEL -r $SUBFILE -m
# Apply Additions to Previous model
$Transfer -o $H2MODEL -OdbUrl=${PREVHARVDBURLBASE}${HARVESTER_TASK}/store -OcheckEmpty=$CHECKEMPTY -OmodelName=$PREVHARVESTMODEL -r $ADDFILE
# Apply Subtractions to VIVO
$Transfer -o $VIVOCONFIG -OcheckEmpty=$CHECKEMPTY -r $SUBFILE -m
# Apply Additions to VIVO
$Transfer -o $VIVOCONFIG -OcheckEmpty=$CHECKEMPTY -r $ADDFILE
