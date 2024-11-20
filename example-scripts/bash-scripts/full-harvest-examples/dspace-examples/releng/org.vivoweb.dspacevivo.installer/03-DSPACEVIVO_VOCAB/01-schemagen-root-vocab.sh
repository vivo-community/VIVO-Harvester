#!/bin/bash 

###################################################################
# Script Name   :
# Description   :
# Args          : 
# Author       	: Michel Héon PhD
# Institution   : Université du Québec à Montréal
# Copyright     : Université du Québec à Montréal (c) 2021
# Email         : heon.michel@uqam.ca
###################################################################
export SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
source $SCRIPT_DIR/../00-env.sh
cd $VOCAB_HOME
mvn org.codehaus.mojo:exec-maven-plugin:java@vitro-code -DskipGen=false &
mvn org.codehaus.mojo:exec-maven-plugin:java@vivo-code -DskipGen=false &
mvn org.codehaus.mojo:exec-maven-plugin:java@obo-code -DskipGen=false &
wait
mvn org.codehaus.mojo:exec-maven-plugin:java@skos-code -DskipGen=false &
mvn org.codehaus.mojo:exec-maven-plugin:java@skos2-code -DskipGen=false &
mvn org.codehaus.mojo:exec-maven-plugin:java@term-code -DskipGen=false &
mvn org.codehaus.mojo:exec-maven-plugin:java@bibo-code -DskipGen=false &
wait
mvn org.codehaus.mojo:exec-maven-plugin:java@ns-code -DskipGen=false  &
mvn org.codehaus.mojo:exec-maven-plugin:java@geopolitical-code -DskipGen=false  &
mvn org.codehaus.mojo:exec-maven-plugin:java@vcard-code -DskipGen=false &
mvn org.codehaus.mojo:exec-maven-plugin:java@dspace -DskipGen=false &
wait
mvn install -Dexec.skip=true
echo "DONE!"

