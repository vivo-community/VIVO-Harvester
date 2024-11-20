#!/bin/bash 

###################################################################
# Script Name   :
# Description   : Allows associating a DSpace docunent type to a VIVO Document
# Args          : 
# Author        : Michel Héon PhD
# Institution   : Université du Québec à Montréal
# Copyright     : Université du Québec à Montréal (c) 2022
# Email         : heon.michel@uqam.ca
###################################################################
export SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd -P)"
source $SCRIPT_DIR/00-env.sh
inputGraph=$ETL_DIR_TRANSFORM/$1

###
# Remove   trim leading and trailing whitespace plus " caracter to docValue
docValues="$(grep terms/type $inputGraph | cut -d ' ' -f 3- | tr -d '.' |  func-encode_string_to_i18n_lowercase.sh | tr -d '\"' | func-clean-begin-ending-whitespace.sh | cut -f 1 -d '^')"
while read docValue; do
    VIVO_URI=$(grep -i "\"$docValue\"" < $MAPPING_DATA_DIR/dspace_vivo_url_mapping_list.data | cut -d '"' -f 3 | xargs)
    echo "      Result   docValue=($docValue) ($VIVO_URI) ($1)" >&2
    ############################################################
    # Build query
    ############################################################
    GET_TYPE_QUERY=$TMPDIR/$(basename $0).sparql
    cat  <<EOF > $GET_TYPE_QUERY
    $(cat $QUERY_DIR/header.sparql)
    
    construct {
        ?s a $VIVO_URI .
        ?s vitro:mostSpecificType $VIVO_URI
        
    }
    where { 
        ?s dc:type ?o  .
    }  
EOF
    #riot --output=RDFXML $inputGraph > /$TMPDIR/$1.rdf 2>/dev/null
    cp $inputGraph  /$TMPDIR/$1.nt
    sparql --data=/$TMPDIR/$1.nt --query=$GET_TYPE_QUERY --results=ntriples 2>/dev/null
done <<< " $docValues"
