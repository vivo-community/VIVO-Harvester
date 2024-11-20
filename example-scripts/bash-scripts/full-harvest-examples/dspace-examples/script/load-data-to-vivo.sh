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

cd $WORKDIR/data/transform
for f in *.ntriples
do
    fileName=$(realpath $f)
    echo "Processing $f"
    cat $fileName >> $TMPDIR/all.ntriples
#    grep terms/type $fileName
done
echo "Loading all files to VIVO"
sparql-load-a-graph-to-vivo.sh -f $TMPDIR/all.ntriples
head -10  $TMPDIR/all.ntriples

