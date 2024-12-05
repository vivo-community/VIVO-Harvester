#!/bin/bash

###################################################################
# Script Name   :
# Description   : Recompute vivo solr index
# Args          :
# Author       	: Michel Héon PhD
# Institution   : Université du Québec à Montréal
# Copyright     : Université du Québec à Montréal (c) 2022
# Email         : heon.michel@uqam.ca
###################################################################
export SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd -P)"
source $SCRIPT_DIR/00-env.sh


############################################################
# Send command
############################################################

curl -s -d "email=$ROOT_USER" -d "password=$ROOT_PASSWD" -d "$QUERY" "$VIVO_URL/SearchIndex" 2>/dev/null
