#!/bin/bash -x

###################################################################
# Script Name   :
# Description   : Gets the list of bibo objects in VIVO
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
QUERY_FN=$TMPDIR/$(basename $0).sparql
cat  <<EOF >> $QUERY_FN
prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> 
prefix obo:   <http://purl.obolibrary.org/obo/> 
prefix vivo:  <http://vivoweb.org/ontology/core#> 
prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> 
select distinct ?s ?label
where { 
    ?s ?p ?o  .
    ?s rdfs:label ?label
    filter regex(str(?s), "bibo", "i")
}  
EOF
sparql --data=$WORKDIR/resources/ontology/vivo.ttl --query=$QUERY_FN --results=TSV 2> /dev/null | sed -e "s/\@en$//" >$WORKDIR/data/vivo_doc_type_list.data



