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
  ?s dc:creator ?o .
}  
EOF

#       { ?s dc:creator ?o  }
#       UNION { ?s dc:contributor  ?o  }

combined_data=$(cat *.ntriples)
echo "$combined_data" > "$MAPPING_DATA_DIR/all_combined.nt"

# Use the variable directly as input to the `sparql` command
sparql --data="$MAPPING_DATA_DIR/all_combined.nt" --query="$GET_TYPE_QUERY" --results=TSV 2>/dev/null | sed 1d | func-sort-list.sh > "$MAPPING_DATA_DIR/list-of-all-persons.data"
cat $MAPPING_DATA_DIR/list-of-all-persons.data
wc -l $MAPPING_DATA_DIR/list-of-all-persons.data

#grep terms/type *.ntriples | cut -d ' ' -f 3- | tr -d '.' |  tr '[:upper:]' '[:lower:]'| sort | uniq 
