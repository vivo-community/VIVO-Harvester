#!/bin/bash

###################################################################
# Script Name   :
# Description   : see: https://wiki.lyrasis.org/display/VIVODOC110x/SPARQL+Update+API
# Args          : 
# Author       	: Michel Héon PhD
# Institution   : Université du Québec à Montréal
# Copyright     : Université du Québec à Montréal (c) 2022
# Email         : heon.michel@uqam.ca
###################################################################
export SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd -P)"
source $SCRIPT_DIR/00-env.sh
############################################################
# Process the input options. Add options as needed.        #
############################################################
# Do help
Help()
{
   # Display Help
   echo "Usage: $0 [-s (loading sample-data) | -f graph-filename | -h help]]"
}
# Get the options
while getopts "hsf:" option; do
   case $option in
      h) # display Help
         Help
         exit;;
      s) # Sample ontoogy
         FILE_NAME=$RESSOURCES/sample-graph.ttl;;
      f) # Enter a Fisrt Name
         FILE_NAME=$(realpath $OPTARG);;
     \?) # Invalid option
         echo "Error: Invalid option"
         Help
         exit;;
   esac
done
# Evaluate mandatory options
if [ -z ${FILE_NAME} ] ; then
    echo "Missing arguments"
    Help
    exit 1
fi
############################################################
# Process values 
############################################################


############################################################
# Build query
############################################################
TMP_FILE=/tmp/$BASHPID_tmp_data.ntriples
cat $FILE_NAME > $TMP_FILE
QUERY=$(cat <<EOF
update=LOAD <file://$TMP_FILE> into graph <http://vitro.mannlib.cornell.edu/default/vitro-kb-2>
EOF
)

############################################################
# Send query
############################################################

curl -s -d "email=$ROOT_USER" -d "password=$ROOT_PASSWD" -d "$QUERY" "$VIVO_URL/api/sparqlUpdate" 2>/dev/null
rm $TMP_FILE
