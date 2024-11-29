#!/bin/bash

###################################################################
# Script Name   : 00-env.sh
# Description   : This file is used to define the environment variables 
#                 needed to run the extract/transform/load (ETL) 
#                 process of dspace2vivo
# Args          : 
# Author        : Michel Héon PhD
# Institution   : Université du Québec à Montréal
# Copyright     : Université du Québec à Montréal (c) 2022
# Email         : heon.michel@uqam.ca
###################################################################
# Scripts root directory
export LOC_SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd -P)"


###################################################################
# Root installation directory of the different dspace2vivo packages
export INSTALLER_DIR=$(cd $LOC_SCRIPT_DIR/../releng/org.vivoweb.dspacevivo.installer ; pwd -P)

###################################################################
# Project root variables
source $INSTALLER_DIR/00-env.sh

###################################################################
# Executable and script path needed to run dspace2VIVO
PATH=$LOC_SCRIPT_DIR:$PATH

###################################################################
# Working directory of scripts
export WORKDIR=$(cd $LOC_SCRIPT_DIR/../; pwd -P)

###################################################################
# Directory of resources needed to configure the expected operation of the scripts
#export RESSOURCESDIR=$(cd $WORKDIR/src/main/resources ; pwd -P)
export RESSOURCESDIR=$(cd $WORKDIR/resources ; pwd -P)

###################################################################
# Directory containing the correspondence files between DSpace values and VIVO values
export MAPPING_DATA_DIR=$(cd $RESSOURCESDIR/mapping_data ; pwd -P)

###################################################################
# Resource directories after compilation. This directory is modified at each compilation (Do not edit)
export RESSOURCES_TARGET_DIR=$(cd $WORKDIR/../../../../build/classes ; pwd -P)

###################################################################
# Directory containing the queries necessary for the execution of SPARQL
export QUERY_DIR=$(cd $RESSOURCESDIR/query ; pwd -P)

###################################################################
# Repositories containing transient data from the extract/transform/load process
export DATA_DIR=$(cd $WORKDIR/data ; pwd -P)
export DATA_DEMO6_DIR=$(cd $WORKDIR/data_src_dspace6 ; pwd -P)
export DATA_DEMO7_DIR=$(cd $WORKDIR/data_src_dspace7 ; pwd -P)

###################################################################
# Data transition sub-directories for each step of the ETL process
export ETL_DIR_EXTRACT=$DATA_DIR/extract
export ETL_DIR_TRANSFORM=$DATA_DIR/transform
export ETL_DIR_TRANSFORM_DOC_TYPE=$(cd ${ETL_DIR_TRANSFORM}_doc_type ; pwd -P)
export ETL_DIR_TRANSFORM_PERSON=$(cd ${ETL_DIR_TRANSFORM}_person ; pwd -P)
export ETL_DIR_TRANSFORM_EXPERTISES=$(cd ${ETL_DIR_TRANSFORM}_expertises ; pwd -P)
export ETL_DIR_TRANSFORM_PERSON_EXPERTISES=$(cd ${ETL_DIR_TRANSFORM}_person_expertises ; pwd -P)