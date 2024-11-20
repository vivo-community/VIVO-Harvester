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

EXPERTISE=$1
####
# Invert first and second name separated by first occurance of ',' + Capitalize first car of each name + remove first and last blank caracter
EXPERTISE_LABEL=$(echo $EXPERTISE | cut -f2 -d ',' | func-clean-begin-ending-whitespace.sh )
PRAGMA=$(echo $EXPERTISE_LABEL | func-encode_string_to_uid.sh )
echo "  EXPERTISE=($EXPERTISE) EXPERTISE_LABEL=($EXPERTISE_LABEL) PRAGMA=($PRAGMA)" >&2
EXPERTISE_URI="http://dspacevivo.vivoweb.org/individual/$PRAGMA"
############################################################
# Build query
############################################################
GET_TYPE_QUERY=$TMPDIR/$(basename $0).sparql
cat  <<EOF >> $GET_TYPE_QUERY
$(cat $QUERY_DIR/header.sparql)

construct {

<$EXPERTISE_URI>
        a                       owl:Thing , skos:Concept ;
        rdfs:label              "$EXPERTISE_LABEL"@en-US ;
        vitro:mostSpecificType  skos:Concept .

} where {}
EOF

sparql  --query=$GET_TYPE_QUERY --results=nt 2>/dev/null > $PRAGMA.ntriples
