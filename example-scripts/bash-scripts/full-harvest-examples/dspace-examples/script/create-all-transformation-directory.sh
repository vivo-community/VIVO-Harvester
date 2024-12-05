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
DATA_ROOT="$( cd $LOC_SCRIPT_DIR/../ && pwd -P)"

rm -fr $DATA_ROOT/data $DATA_ROOT/data_src_dspace6 $DATA_ROOT/data_src_dspace7

mkdir -p $DATA_ROOT/data
cd $DATA_ROOT/data
mkdir -p extract transform transform_doc_type transform_person transform_expertises transform_person_expertises

mkdir -p $DATA_ROOT/data_src_dspace6
cd $DATA_ROOT/data_src_dspace6
mkdir -p extract transform

mkdir -p $DATA_ROOT/data_src_dspace7
cd $DATA_ROOT/data_src_dspace7
mkdir -p extract transform
