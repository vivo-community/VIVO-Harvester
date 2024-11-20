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
source $SCRIPT_DIR/../00-env.sh
cd $GIT_REPO
git clone -b dspace-7.2.x https://github.com/DSpace/DSpace.git
git clone https://github.com/DSpace/dspace-angular.git
cd dspace-angular
git checkout tags/dspace-7.2
