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
export HARVESTER_INSTALL_DIR=/usr/local/src/VIVO-Harvester
export HARVEST_NAME=example-dsr
export DATE=`date +%Y-%m-%d'T'%T`

# Add harvester binaries to path for execution
# The tools within this script refer to binaries supplied within the harvester
#       Since they can be located in another directory their path should be
#       included within the classpath and the path environment variables.
export PATH=$PATH:$HARVESTER_INSTALL_DIR/bin
export CLASSPATH=$CLASSPATH:$HARVESTER_INSTALL_DIR/bin/harvester.jar:$HARVESTER_INSTALL_DIR/bin/dependency/*
export CLASSPATH=$CLASSPATH:$HARVESTER_INSTALL_DIR/build/harvester.jar:$HARVESTER_INSTALL_DIR/build/dependency/*
rm -rf post-reversal-analytics.txt


echo "Total number of Grants in VIVO:  " `harvester-jenaconnect -j vivo.model.xml -q "SELECT  count(?grant)  WHERE  {?grant <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>  <http://vivoweb.org/ontology/core#Grant>. }" | sed -n '4,4p'`  | tee --append post-reversal-analytics.txt
echo "Total number of Funding Organizations in VIVO:  " `harvester-jenaconnect -j vivo.model.xml -q "SELECT COUNT(?x) WHERE { SELECT DISTINCT ?x { ?x <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Organization> . ?x <http://vivoweb.org/ontology/core#awardsGrant> ?y .} }" | sed -n '4,4p'`  | tee --append post-reversal-analytics.txt
echo "Total number of Organizations in Vivo: " `harvester-jenaconnect -j vivo.model.xml -q "SELECT COUNT(?x) WHERE { ?x <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Organization> . }" | sed -n '4,4p'`  | tee --append post-reversal-analytics.txt
echo "Total number of Principal Investigators in VIVO:  " `harvester-jenaconnect -j vivo.model.xml -q "SELECT COUNT(?x) WHERE { SELECT DISTINCT ?x ?lab WHERE { ?x <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Person> . ?y <http://vivoweb.org/ontology/core#principalInvestigatorRoleOf> ?x .?x <http://www.w3.org/2000/01/rdf-schema#label> ?lab } order by ?lab }" | sed -n '4,4p'`  | tee --append post-reversal-analytics.txt
echo "Total number of Co-Principal Investigators in VIVO:  " `harvester-jenaconnect -j vivo.model.xml -q "SELECT COUNT(?x) WHERE { SELECT DISTINCT ?x ?lab WHERE { ?x <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Person> . ?y <http://vivoweb.org/ontology/core#co-PrincipalInvestigatorRoleOf> ?x . ?x <http://www.w3.org/2000/01/rdf-schema#label> ?lab } order by ?lab }" | sed -n '4,4p'`  | tee --append post-reversal-analytics.txt
echo "Total number of people in VIVO:  " `harvester-jenaconnect -j vivo.model.xml -q "SELECT count(?person) where { ?person <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Person>. }" | sed -n '4,4p'`  | tee --append post-reversal-analytics.txt
echo "Total number of people with UFIDs:  " `harvester-jenaconnect -j vivo.model.xml -q "SELECT  count(?URI) WHERE { ?URI  <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Person>. ?URI <http://vivo.ufl.edu/ontology/vivo-ufl/ufid> ?UFID . }" | sed -n '4,4p'` | tee --append post-reversal-analytics.txt
echo "Total number of people without UFIDs:  " `harvester-jenaconnect -j vivo.model.xml -q "SELECT  count(?u) WHERE { ?u <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Person> . OPTIONAL {?u <http://vivo.ufl.edu/ontology/vivo-ufl/ufid> ?y . } FILTER (!bound(?y)) }" | sed -n '4,4p'` | tee --append post-reversal-analytics.txt

