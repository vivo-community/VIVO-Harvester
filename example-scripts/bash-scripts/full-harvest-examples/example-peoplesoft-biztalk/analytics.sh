#!/bin/bash

# Copyright (c) 2010-2011 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence, Michael Barbieri.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the new BSD license
# which accompanies this distribution, and is available at
# http://www.opensource.org/licenses/bsd-license.html
# 
# Contributors:
#     Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence, Michael Barbieri - initial API and implementation

# set to the directory where the harvester was installed or unpacked
# HARVESTER_INSTALL_DIR is set to the location of the installed harvester
#       If the deb file was used to install the harvester then the
#       directory should be set to /usr/share/vivo/harvester which is the
#       current location associated with the deb installation.
#       Since it is also possible the harvester was installed by
#       uncompressing the tar.gz the setting is available to be changed
#       and should agree with the installation location
export HARVESTER_INSTALL_DIR=/data/vivo/harvester/harvester_1.3
export HARVEST_NAME=example-scripts
export DATE=`date +%Y-%m-%d'T'%T`

# Add harvester binaries to path for execution
# The tools within this script refer to binaries supplied within the harvester
#       Since they can be located in another directory their path should be
#       included within the classpath and the path environment variables.
export PATH=$PATH:$HARVESTER_INSTALL_DIR/bin
export CLASSPATH=$CLASSPATH:$HARVESTER_INSTALL_DIR/bin/harvester.jar:$HARVESTER_INSTALL_DIR/bin/dependency/*
export CLASSPATH=$CLASSPATH:$HARVESTER_INSTALL_DIR/build/harvester.jar:$HARVESTER_INSTALL_DIR/build/dependency/*

# Check to see if analytics.txt already exists
if [ -f analytics.txt ]; then
   rm -rf analytics.txt
fi

# Create the analytics.txt file
touch analytics.txt

#return total number of people in Consultant class
echo "Total number of persons with Gatorlink : " `harvester-jenaconnect -j vivo.model.xml -q "SELECT COUNT(?x) WHERE { ?x <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Person> .?x <http://vivo.ufl.edu/ontology/vivo-ufl/ufid> ?y .}" | sed -n '4,4p'`  >>analytics.txt
echo "Total number of persons with UFID : " `harvester-jenaconnect -j vivo.model.xml -q "SELECT COUNT(?x) WHERE { ?x <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Person> .?x <http://vivo.ufl.edu/ontology/vivo-ufl/ufid> ?y .}" | sed -n '4,4p'`  >>analytics.txt

echo "Total number of people marked Faculty : " `harvester-jenaconnect -j vivo.model.xml -q "SELECT COUNT(?x) WHERE { ?x <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://vivoweb.org/ontology/core#FacultyMember> .}" | sed -n '4,4p'`  >>analytics.txt
echo "Total number of people marked nonAcademic : " `harvester-jenaconnect -j vivo.model.xml -q "SELECT COUNT(?x) WHERE { ?x <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://vivoweb.org/ontology/core#NonAcademic> .}" | sed -n '4,4p'`  >>analytics.txt
echo "Total number of people marked Courtesy Faculty : " `harvester-jenaconnect -j vivo.model.xml -q "SELECT COUNT(?x) WHERE { ?x <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://vivoweb.org/ontology/core#CourtesyFaculty> .}" | sed -n '4,4p'`  >>analytics.txt
echo "Total number of people marked Emeritus : " `harvester-jenaconnect -j vivo.model.xml -q "SELECT COUNT(?x) WHERE { ?x <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://vivoweb.org/ontology/core#EmeritusProfessor> .}" | sed -n '4,4p'`  >>analytics.txt
echo "Total number of people marked Consultant : " `harvester-jenaconnect -j vivo.model.xml -q "SELECT COUNT(?x) WHERE { ?x <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://vivo.ufl.edu/ontology/vivo-ufl/Consultant> .}" | sed -n '4,4p'`  >>analytics.txt
echo "Total number of people marked Recent Employee : " `harvester-jenaconnect -j vivo.model.xml -q "SELECT COUNT(?x) WHERE { ?x <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://vivo.ufl.edu/ontology/vivo-ufl/RecentEmployee> .}" | sed -n '4,4p'`  >>analytics.txt
echo "Total number of people marked Deceased : " `harvester-jenaconnect -j vivo.model.xml -q "SELECT COUNT(?x) WHERE { ?x <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://vivo.ufl.edu/ontology/vivo-ufl/Deceased> .}" | sed -n '4,4p'`  >>analytics.txt
