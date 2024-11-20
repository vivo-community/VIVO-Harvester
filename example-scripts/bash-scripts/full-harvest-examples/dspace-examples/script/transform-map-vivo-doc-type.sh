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
cd $WORKDIR/data/transform
NBR_FILE=$(ls -al *.ntriples | wc -l)

for f in *.ntriples
do
    ((LOOP_CTR=LOOP_CTR+1))
    fileName=$(realpath $f)
    echo "Processing ($LOOP_CTR/$NBR_FILE) $f"
    (map-vivo-doc-type.sh $f > $ETL_DIR_TRANSFORM_DOC_TYPE/$f )&
    ((j=j+1))
    if [ $j = "10" ]
    then
        wait; ((j=0)) ;  echo =========; sleep 0.1
         
    else
        sleep 0.2
    fi
done
wait
echo "$(basename $0) Done" >&2