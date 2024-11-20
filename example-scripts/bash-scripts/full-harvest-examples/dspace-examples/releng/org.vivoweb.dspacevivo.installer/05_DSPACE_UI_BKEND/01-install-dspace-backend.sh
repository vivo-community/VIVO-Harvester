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

JAVA_VERSION=$(java -version 2>&1 | head -1 | cut -d'"' -f2 | sed '/^1\./s///' | cut -d'.' -f1)
if (( 11 > $JAVA_VERSION )); then
    echo please using java 11 or higher
    exit 1
fi

###################################################################
# Setup config file dspace.dir to $DSPACE_HOME
sed -e "s|_DSPACE_DIR_|$DSPACE_HOME|g"  $SCRIPT_DIR/config/local.cfg > $DSPACE_BRANCH/dspace/config/local.cfg


###################################################################
# Compile DSPACE
cd $DSPACE_BRANCH
mvn clean package

###################################################################
# Deploy DSPACE

cd $DSPACE_BRANCH/dspace/target/dspace-installer/
ant fresh_install
cd $DSPACE_HOME


###################################################################
# Install in topcat webapps

rm -rf $CATALINA_HOME/webapps/server
cp -r $DSPACE_HOME/webapps/* $CATALINA_HOME/webapps

echo "You can start server with tomcat-start.sh "
echo "You can show-it with browse-dspace.sh "

