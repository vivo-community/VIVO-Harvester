#!/bin/bash 

###################################################################
# Script Name   : get-and-run-swagger-codegen.sh
# Description   : 
# Res           : https://github.com/swagger-api/swagger-codegen
# Args          : 
# Author       	: Michel Héon PhD
# Institution   : Université du Québec à Montréal
# Copyright     : Université du Québec à Montréal (c) 2022
# Email         : heon.michel@uqam.ca
###################################################################
export SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd -P)"
source $SCRIPT_DIR/00-env.sh
# Download current stable 3.x.x branch (OpenAPI version 3)
wget https://repo1.maven.org/maven2/io/swagger/codegen/v3/swagger-codegen-cli/3.0.33/swagger-codegen-cli-3.0.33.jar -O swagger-codegen-cli.jar

java -jar swagger-codegen-cli.jar --help
