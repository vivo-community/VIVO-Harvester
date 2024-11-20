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

NAME=$(echo $2 | tr -d '"'| func-encode_string_to_i18n_lowercase.sh | func-capitalize-each-fisrt-caracter.sh )
#FIRST_NAME=$(echo $NAME | cut -f2 -d ',' | func-clean-begin-ending-whitespace.sh)
#SECOND_NAME=$(echo $NAME | cut -f1 -d ',' | func-clean-begin-ending-whitespace.sh )
#FULL_NAME="$FIRST_NAME $SECOND_NAME"

echo "      EXPERTISE_LABEL=($EXPERTISE_LABEL)  NAME=($NAME)"

if [ -z "${NAME}" ]; then
echo "NAME is empty"
exit 0
fi
if [ -z "${EXPERTISE_LABEL}" ]; then
echo "EXPERTISE_LABEL is empty"
exit 0
fi

EXPERTISE_URI="$(echo $EXPERTISE_LABEL | func-encode_string_to_vivo-URI.sh)" 
PERS_URI="$(echo $NAME | func-encode_string_to_vivo-URI.sh)" 
ITEM_URI="$3"

AUTHORSHIP_URI="$(echo ${NAME}_${ITEM_URI} | func-encode_string_to_vivo-URI.sh)" 

############################################################
# Build query
############################################################
GET_TYPE_QUERY=$TMPDIR/$(basename $0).sparql
cat  <<EOF > $GET_TYPE_QUERY
$(cat $QUERY_DIR/header.sparql)

construct {

<$AUTHORSHIP_URI>
        a                       owl:Thing , obo:BFO_0000001 , obo:BFO_0000020 , vivo:Authorship , vivo:Relationship , obo:BFO_0000002 ;
        vitro:mostSpecificType  vivo:Authorship ;
        vivo:rank               1 ;
        vivo:relates            <$PERS_URI> , <$ITEM_URI> .
<$ITEM_URI> vivo:relatedBy <$AUTHORSHIP_URI> ,  <$PERS_URI> .
<$PERS_URI> 
        vivo:relatedBy          <$AUTHORSHIP_URI> ;
        vivo:relates            <$ITEM_URI> .
<$PERS_URI> <http://vivoweb.org/ontology/core#hasResearchArea>    <$EXPERTISE_URI> .
<$EXPERTISE_URI> <http://vivoweb.org/ontology/core#researchAreaOf>    <$PERS_URI> .
} where {}
EOF
NAME_PRAGMA=$(echo $NAME | func-encode_string_to_uid.sh )
EXPERTISE_PRAGMA=$(echo $EXPERTISE_LABEL | func-encode_string_to_uid.sh )
#cat $GET_TYPE_QUERY > $ETL_DIR_TRANSFORM_PERSON_EXPERTISES/${EXPERTISE_PRAGMA}-${NAME_PRAGMA}.ntriples
sparql  --query=$GET_TYPE_QUERY --results=nt 2>/dev/null > $ETL_DIR_TRANSFORM_PERSON_EXPERTISES/${EXPERTISE_PRAGMA}-${NAME_PRAGMA}.ntriples
