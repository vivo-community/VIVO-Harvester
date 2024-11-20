#!/bin/bash 

###################################################################
# Script Name   :
# Description   : Script to build the DSpace Docker image
# Args          : 
# Author       	: Michel Héon PhD
# Institution   : Université du Québec à Montréal
# Copyright     : Université du Québec à Montréal (c) 2022
# Email         : heon.michel@uqam.ca
###################################################################
export SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd -P)"
source $SCRIPT_DIR/../00-env.sh
cd $DSPACE_BRANCH
docker-compose -f docker-compose.yml -f docker-compose-cli.yml pull
docker-compose -f docker-compose.yml -f docker-compose-cli.yml build
