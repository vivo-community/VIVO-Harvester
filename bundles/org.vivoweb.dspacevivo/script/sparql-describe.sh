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
export LOC_SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd -P)"
source $LOC_SCRIPT_DIR/00-env.sh

############################################################
# Help                                                     #
############################################################
Help()
{
   # Display Help
   echo "Usage: $0 [-u URI|q QName|t|n|h] URI"
   echo "options:"
   echo "u     Follow by URI"
   echo "q     Follow by QName"
   echo "t     Output as Turtle (default)"
   echo "n     Output as n-triples"
   echo "h     Print this Help."
   echo
}

############################################################
# Process the input options. Add options as needed.        #
############################################################
# Get the options
FORM="text/turtle"
URI="<$1>"
while getopts "hu:q:nt" option; do
   case $option in
      h) # display Help
         Help
         exit;;
      u) # Enter an URI
         URI="<${OPTARG}>";;
      q) # Enter an URI as QName ex. dspace:Item
         URI=$OPTARG;;
	  t) # output format turtle
	  	 FORM="text/turtle";;
	  n)
	  	FORM="text/plain";;
     \?) # Invalid option
         echo "Error: Invalid option"
         Help
         exit;;
   esac
done
echo $URI
if [ -z ${URI} ] ; then
    echo "Error: No URI"
    Help
    exit 1
fi

############################################################
# Build query
############################################################
QUERY=$(cat <<EOF
$(cat $LOC_SCRIPT_DIR/lib/prefix.sparql)
DESCRIBE $URI
EOF
)
############################################################
# Send query
############################################################

curl -s -d "email=$ROOT_USER" -d "password=$ROOT_PASSWD" -d "query=$QUERY" -H "Accept: $FORM" "$VIVO_URL/api/sparqlQuery" 2>/dev/null

