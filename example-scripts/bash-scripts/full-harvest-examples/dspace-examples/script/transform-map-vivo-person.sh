#!/bin/bash 

###################################################################
# Script Name   :
# Description   : Allows associating a DSpace docunent type to a VIVO Document
# Args          : 
# Author       	: Michel Héon PhD
# Institution   : Université du Québec à Montréal
# Copyright     : Université du Québec à Montréal (c) 2022
# Email         : heon.michel@uqam.ca
###################################################################
export SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd -P)"
source $SCRIPT_DIR/00-env.sh

cd $ETL_DIR_TRANSFORM_PERSON
NBR_FILE=$(cat $MAPPING_DATA_DIR/list-of-all-persons.data | wc -l)
while read name; do
    ((LOOP_CTR=LOOP_CTR+1))
    echo "($LOOP_CTR/$NBR_FILE) Processing $name"
    map-name-to-vivo-person.sh "$name" & 
    ((j=j+1))
    if [ $j = "10" ]
    then
        wait; ((j=0)) ; echo "($LOOP_CTR/$NBR_FILE)====================" ; sleep 0.1 
    else
        sleep 0.20 
    fi
done < $MAPPING_DATA_DIR/list-of-all-persons.data
wait
echo "$(basename $0) Done" >&2
