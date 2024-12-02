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
## Root variables
source cleanup.sh
export DEPLOY=$(cd $LOC_SCRIPT_DIR/../../../../../build; pwd -P)
export LIB=$DEPLOY/dependency

export VIVO_APP_NAME=vivo

export VIVO_URL=http://localhost:8080/$VIVO_APP_NAME
export PATH=$CATALINA_HOME/bin:$SOLR_DIR/bin:$DSPACE_HOME/bin:$PATH

###################################################################
## Extract username and password
export RUNTIME_PROP=vivoauth.properties
if test -f "$RUNTIME_PROP"; then
        [ -v ROOT_PASSWD ] || export ROOT_PASSWD=$(grep 'rootUser.password' < $RUNTIME_PROP | tr -d ' ' | cut -f 2 -d '=')
        [ -v ROOT_USER ] || export ROOT_USER=$(grep 'rootUser.emailAddress' < $RUNTIME_PROP | tr -d ' ' | cut -f 2 -d '=')
        [ -v JENA_PATH ] || export JENA_PATH=$(grep 'jenaPath' < $RUNTIME_PROP | tr -d ' ' | cut -f 2 -d '=')
        alias vivo_passwd="echo $ROOT_PASSWD"
        alias vivo_user="echo $ROOT_USER"
        alias jena_path="echo $JENA_PATH"
fi

###################################################################
## Variables for dspace backend/frontend runtime

# "rest" section
export DSPACE_REST_SSL=false
export DSPACE_REST_HOST=localhost
export DSPACE_REST_PORT=8080
export DSPACE_REST_NAMESPACE=/server

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

###################################################################
# Setup Jena environment

cd $JENA_PATH
export JENA_HOME="$(pwd)"
export PATH="$PATH:$(pwd)/bin"
