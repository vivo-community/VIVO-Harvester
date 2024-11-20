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

cd $ETL_DIR_EXTRACT; rm -f *
cd $ETL_DIR_TRANSFORM; rm -f *
cd $ETL_DIR_TRANSFORM_DOC_TYPE; rm -f *
cd $ETL_DIR_TRANSFORM_PERSON; rm -f *
cd $ETL_DIR_TRANSFORM_EXPERTISES; rm -f *
cd $ETL_DIR_TRANSFORM_PERSON_EXPERTISES; rm -f *
cd $VIVO_HOME; rm -fr tdbContentModels  tdbModels  upgrade  uploads


