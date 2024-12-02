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

NAME=$(echo $1 | tr -d '"'| func-encode_string_to_i18n_lowercase.sh | func-capitalize-each-fisrt-caracter.sh )
####
# Invert first and second name separated by first occurance of ',' + Capitalize first car of each name + remove first and last blank caracter
FIRST_NAME=$(echo $NAME | cut -f2 -d ',' | func-clean-begin-ending-whitespace.sh)
SECOND_NAME=$(echo $NAME | cut -f1 -d ',' | func-clean-begin-ending-whitespace.sh )
FULL_NAME="$FIRST_NAME $SECOND_NAME"
PRAGMA=$(echo $NAME | func-encode_string_to_uid.sh )

 echo "     NAME=($NAME) FIRST_NAME=($FIRST_NAME) SECOND_NAME=($SECOND_NAME) FULL_NAME=($FULL_NAME) PRAGMA=($PRAGMA)" >&2
PERS_URI=$(echo "$NAME" | func-encode_string_to_vivo-URI.sh)
############################################################
# Build query
############################################################
GET_TYPE_QUERY=$TMPDIR/$(basename $0).sparql
cat  <<EOF >> $GET_TYPE_QUERY
$(cat $QUERY_DIR/header.sparql)

construct {

<${PERS_URI}>
        a                       owl:Thing , obo:BFO_0000002 , obo:BFO_0000004 , obo:BFO_0000001 , foaf:Agent , foaf:Person ;
        rdfs:label              "$FULL_NAME "@en-US ;
        obo:ARG_2000028         <${PERS_URI}-vcard> ;
        vitro:mostSpecificType  foaf:Person .

<${PERS_URI}-vcard>
        a                       obo:BFO_0000001 , obo:BFO_0000031 , vcard:Individual , obo:ARG_2000379 , obo:BFO_0000002 , obo:IAO_0000030 , owl:Thing , vcard:Kind ;
        obo:ARG_2000029         <${PERS_URI}> ;
        vitro:mostSpecificType  vcard:Individual ;
        vcard:hasName           <${PERS_URI}-vcard-name> .
<${PERS_URI}-vcard-name>
        a                       owl:Thing , vcard:Identification , vcard:Addressing , vcard:Explanatory , vcard:Communication , vcard:Name ;
        vitro:mostSpecificType  vcard:Name ;
        vcard:familyName        "$SECOND_NAME"@en-US ;
        vcard:givenName         "$FIRST_NAME"@en-US .

} where {}
EOF
sparql  --query=$GET_TYPE_QUERY --results=TTL --results=nt 2>/dev/null > $ETL_DIR_TRANSFORM_PERSON/$PRAGMA.ntriples