#!/bin/bash 
###################################################################
# Script Name   :
# Description   :Delete the VIVO triplet directories. This action is 
#      used to reset the VIVO database. 
#      Warning: Tomcat must be stopped (tomcat-stop.sh) before executing this action
# Args          : 
# Author       	: Michel Héon PhD
# Institution   : Université du Québec à Montréal
# Copyright     : Université du Québec à Montréal (c) 2022
# Email         : heon.michel@uqam.ca
###################################################################
export SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd -P)"
source $SCRIPT_DIR/00-env.sh
cd $VIVO_HOME
if [ ! -f tdbModels ]
then
    systemctl stop tomcat
    rm -fr tdbContentModels  tdbModels  upgrade  uploads
    systemctl start tomcat
    echo "VIVO triplesore is clean"
fi



