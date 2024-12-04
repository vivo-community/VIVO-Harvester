#!/bin/bash

###################################################################
# Script Name   :
# Description   : This script encapsulates the functions call allowing the migration of DSpace Demo(6&7) data into VIVO
# Args          : 
# Author       	: Michel Héon PhD
# Institution   : Université du Québec à Montréal
# Copyright     : Université du Québec à Montréal (c) 2022
# Email         : heon.michel@uqam.ca
###################################################################
export SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd -P)"
source $SCRIPT_DIR/00-env.sh

$SCRIPT_DIR/create-all-transformation-directory.sh
unset ROOT_USER
unset ROOT_PASSWD
unset JENA_PATH
unset GRAPH_NAME
source $SCRIPT_DIR/00-env.sh
cd $SCRIPT_DIR

##################################################################
# Clean and setup up data directories and properties
cp $RESSOURCESDIR/*.conf $RESSOURCES_TARGET_DIR
flush_data_dspace.sh 2>/dev/null
flush_data_dspace6.sh 2>/dev/null
flush_data_dspace7.sh 2>/dev/null

###################################################################
# Extract dspace(6-7) demo data
if test -f "$RUNTIME_PROP"; then
        [ -v DSPACE_VERSION ] || export DSPACE_VERSION=$(grep 'dspaceVersion' < $RUNTIME_PROP | tr -d ' ' | cut -f 2 -d '=')
        alias dspace_version="echo $DSPACE_VERSION"
fi

if [ "$DSPACE_VERSION" == "6" ]; then
  echo "Extracting using DSpace6 configuration"
  ./extract-dspace6.sh
else
  echo "Extracting using DSpace7 configuration"
  ./extract-dspace7.sh
fi

cp -rf $DATA_DEMO6_DIR/* $DATA_DEMO7_DIR/* $DATA_DIR
exit 1
###################################################################
# Produce all list
echo run produce-list-of-expertise.sh
produce-list-of-expertise.sh

###########################
echo run produce-list-of-itemtype.sh
produce-list-of-itemtype.sh

###########################
echo run produce-list-of-persons.sh
produce-list-of-persons.sh

###################################################################
# Process transformation and load to VIVO
load-data-to-vivo.sh

transform-map-vivo-doc-type.sh
load-data-doc_type-to-vivo.sh ; vivo-recomputeIndex.sh 

transform-map-vivo-person.sh
load-data-person-to-vivo.sh ; vivo-recomputeIndex.sh

transform-map-vivo-expertises.sh
load-data-expertises-to-vivo.sh ; vivo-recomputeIndex.sh

transform-map-expertise-and-item-to-a-person-to-vivo.sh
load-data-person-expertise-to-vivo.sh ; vivo-recomputeIndex.sh

###################################################################
# Done ETL Process
echo "Done!"
