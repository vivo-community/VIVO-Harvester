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
cd $ETL_DIR_TRANSFORM
grep terms/type *.ntriples | cut -d ' ' -f 3- | tr -d '.' |  func-encode_string_to_i18n_lowercase.sh | func-sort-list.sh | func-skip-first-line.sh> $MAPPING_DATA_DIR/list-of-itemsType.data
cat  $MAPPING_DATA_DIR/list-of-itemsType.data
