#!/bin/bash 

###################################################################
# Script Name   : 01-gen-code.sh
# Description   : This script is used to generate the Java skeletons 
#     of the DSExDS YAML metamodel
# Args          : N/A
# Author       	: Michel Héon PhD
# Institution   : Université du Québec à Montréal
# Copyright     : Université du Québec à Montréal (c) 2022
# Email         : heon.michel@uqam.ca
###################################################################
export SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
source $SCRIPT_DIR/../00-env.sh
cd $METAMODEL_HOME
mvn clean generate-sources
