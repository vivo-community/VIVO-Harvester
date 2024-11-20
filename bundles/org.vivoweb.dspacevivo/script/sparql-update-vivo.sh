#!/bin/bash 

###################################################################
# Script Name   :
# Description   : see https://wiki.lyrasis.org/display/VIVODOC110x/SPARQL+Update+API
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
QUERY=$(cat <<EOF
update=INSERT DATA {
	GRAPH <http://vitro.mannlib.cornell.edu/default/vitro-kb-2> {
		$(cat <&0)
	}
}
EOF
)

############################################################
# Send query
############################################################

curl -s -d "email=$ROOT_USER" -d "password=$ROOT_PASSWD" -d "$QUERY" "$VIVO_URL/api/sparqlUpdate" 2>/dev/null

