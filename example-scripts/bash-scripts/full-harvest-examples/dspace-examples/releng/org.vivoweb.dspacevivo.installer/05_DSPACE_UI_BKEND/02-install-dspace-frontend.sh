#!/bin/bash 

###################################################################
# Script Name   :
# Description   : see https://wiki.lyrasis.org/display/DSDOC7x/Installing+DSpace
# Args          : 
# Author       	: Michel Héon PhD
# Institution   : Université du Québec à Montréal
# Copyright     : Université du Québec à Montréal (c) 2022
# Email         : heon.michel@uqam.ca
###################################################################
export SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd -P)"
source $SCRIPT_DIR/../00-env.sh

###################################################################
# Compiling dspace-ui
cd $DSPACE_FRONT_BRANCH
yarn install
yarn build:prod
###################################################################
# Deploy in deployement environment 
mkdir -p $DSPACE_UI_HOME
cp -r $DSPACE_FRONT_BRANCH/dist $DSPACE_UI_HOME
cp -r $DSPACE_FRONT_BRANCH/config $DSPACE_UI_HOME
echo "node-start.sh to start dspace-ui" 


