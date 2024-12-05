#!/bin/bash

###################################################################
# Script Name   :
# Description   :
# Args          : 
# Author       	: Michel Héon PhD
# Institution   : Université du Québec à Montréal
# Copyright     : Université du Québec à Montréal (c) 2022
# Email         : heon.michel@uqam.ca
###################################################################
export SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd -P)"
source $SCRIPT_DIR/00-env.sh
cd $ETL_DIR_TRANSFORM

############################################################
# Build query
############################################################
GET_TYPE_QUERY=$TMPDIR/$(basename $0).sparql
cat  <<EOF >> $GET_TYPE_QUERY
$(cat $QUERY_DIR/header.sparql)

select ?o
where { 
       ?s dc:subject ?o
}  
EOF


cat *.ntriples  > $TMPDIR/all.nt
sparql --data=$TMPDIR/all.nt --query=$GET_TYPE_QUERY --results=TSV 2>/dev/null  | sed 1d | func-encode_string_to_expertise.sh |  func-sort-list.sh > $MAPPING_DATA_DIR/list-of-all-expertises.data
cat $MAPPING_DATA_DIR/list-of-all-expertises.data
wc -l $MAPPING_DATA_DIR/list-of-all-expertises.data
