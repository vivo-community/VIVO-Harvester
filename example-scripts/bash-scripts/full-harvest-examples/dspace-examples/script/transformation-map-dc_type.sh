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

############################################################
# Build query
############################################################
GET_TYPE_QUERY=$TMPDIR/$(basename $0).sparql
cat  <<EOF >> $GET_TYPE_QUERY
$(cat $QUERY_DIR/header.sparql)

select *
where { 
    ?s dc:type ?o  .
}  
EOF

fileName=$(realpath $1)
riot --output=RDFXML $fileName > /$TMPDIR/$1.rdf 2>/dev/null
sparql --data=$TMPDIR/$1.rdf --query=$GET_TYPE_QUERY --results=TSV
