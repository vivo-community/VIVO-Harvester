#!/bin/bash 

###################################################################
# Script Name   :
# Description   : ***  NOT WORK - Developpement in progress
# Args          : 
# Author       	: Michel Héon PhD
# Institution   : Université du Québec à Montréal
# Copyright     : Université du Québec à Montréal (c) 2022
# Email         : heon.michel@uqam.ca
###################################################################
export SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd -P)"
source $SCRIPT_DIR/../00-env.sh
echo ***  NOT WORK - Developpement in progress
exit 0
cd $INSTALLER_HOME
mvn install -s 04-DSPACEVIVO_METAMODEL/settings.xml

