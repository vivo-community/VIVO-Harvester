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
if [ -v $1 ] ; then
    # Data com from stdin
    cat - > $TMPDIR/data.tmp
    DATA=$TMPDIR/data.tmp
else
    # Filename is set
    DATA=$(realpath $1)
fi
riot --syntax=nt --output=ttl $DATA > /$TMPDIR/data_out.ttl 2>/dev/null
sparql --data=/$TMPDIR/data_out.ttl --query=$SCRIPT_DIR/lib/construct.sparql 2>/dev/null 


