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
cd $DEPLOY
TOMCAT_VER=8.5.82
SOLR_VER=8.11.2

######################
# Downloading packages
#
wget    https://dlcdn.apache.org/tomcat/tomcat-8/v$TOMCAT_VER/bin/apache-tomcat-$TOMCAT_VER.tar.gz \
        https://dlcdn.apache.org/lucene/solr/$SOLR_VER/solr-$SOLR_VER.tgz

######################
echo Installing tomcat
#
mkdir app-tomcat 
tar -xf apache-tomcat-$TOMCAT_VER.tar.gz -C ./app-tomcat --strip-components=1 

######################
echo Installing solr
#
mkdir app-solr
tar -xf solr-$SOLR_VER.tgz -C ./app-solr --strip-components=1
cp -r $SCRIPT_DIR/vivo-solr/* app-solr

echo Done!
