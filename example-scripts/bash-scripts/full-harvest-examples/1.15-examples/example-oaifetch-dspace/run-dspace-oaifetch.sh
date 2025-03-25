#!/bin/bash

if [[ "$1" != "" && "$1" != "tdb" && "$1" != "sparql" ]]; then
  echo "Invalid argument: $1"
  echo "Usage: $0 [tdb|sparql]"
  exit 1
fi

#Copyright (c) 2010-2011 VIVO Harvester Team. For full list of contributors, please see the AUTHORS file provided.
#All rights reserved.
#This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html

# set to the directory where the harvester was installed or unpacked
# HARVESTER_INSTALL_DIR is set to the location of the installed harvester
#	If the deb file was used to install the harvester then the
#	directory should be set to /usr/share/vivo/harvester which is the
#	current location associated with the deb installation.
#	Since it is also possible the harvester was installed by
#	uncompressing the tar.gz the setting is available to be changed
#	and should agree with the installation location
#export HARVESTER_INSTALL_DIR=<HARVESTER_INSTALL_DIR>
HARVESTER_INSTALL_DIR=$(pwd)/../../../../../../VIVO-Harvester
export HARVEST_NAME=DSpace-OAI-fetch
export DATE=`date +%Y-%m-%d'T'%T`

# Add harvester binaries to path for execution
# The tools within this script refer to binaries supplied within the harvester
#	Since they can be located in another directory their path should be
#	included within the classpath and the path environment variables.
export PATH=$PATH:$HARVESTER_INSTALL_DIR/bin
export CLASSPATH=$CLASSPATH:$HARVESTER_INSTALL_DIR/bin/harvester.jar:$HARVESTER_INSTALL_DIR/bin/dependency/*
export CLASSPATH=$CLASSPATH:$HARVESTER_INSTALL_DIR/build/harvester.jar:$HARVESTER_INSTALL_DIR/build/dependency/*

# Exit on first error
# The -e flag prevents the script from continuing even though a tool fails.
#	Continuing after a tool failure is undesirable since the harvested
#	data could be rendered corrupted and incompatible.
set -e

# Supply the location of the detailed log file which is generated during the script.
#	If there is an issue with a harvest, this file proves invaluable in finding
#	a solution to the problem. It has become common practice in addressing a problem
#	to request this file. The passwords and usernames are filtered out of this file
#	to prevent these logs from containing sensitive information.
echo "Full Logging in $HARVEST_NAME.$DATE.log"
if [ ! -d logs ]; then
  mkdir logs
fi
cd logs
touch $HARVEST_NAME.$DATE.log
ln -sf $HARVEST_NAME.$DATE.log $HARVEST_NAME.latest.log
cd ..

#clear old data
# For a fresh harvest, the removal of the previous information maintains data integrity.
#	If you are continuing a partial run or wish to use the old and already retrieved
#	data, you will want to comment out this line since it could prevent you from having
# 	the required harvest data.  
rm -rf data

# Execute Fetch
# This stage of the script is where the information is gathered together into one local
#	place to facilitate the further steps of the harvest. The data is stored locally
#	in a format based off of the source. The format is a form of RDF but not in the VIVO ontology
java $HARVESTER_JAVA_OPTS org.vivoweb.harvester.fetch.OAIFetch -X dspace-oaifetch.conf.xml

# Execute Translate
# This is the part of the script where the input data is transformed into valid RDF
#   Translate will apply an xslt file to the fetched data which will result in the data
#   becoming valid RDF in the VIVO ontology
harvester-xsltranslator -X xsltranslator.config.xml

# Execute Transfer to import from record handler into local temp model
# From this stage on the script places the data into a Jena model. A model is a
#	data storage structure similar to a database, but in RDF.
# The harvester tool Transfer is used to move/add/remove/dump data in models.
# For this call on the transfer tool:
# -s refers to the source translated records file, which was just produced by the translator step
# -o refers to the destination model for harvested data
# -d means that this call will also produce a text dump file in the specified location
harvester-transfer -w INFO -s translated-records.config.xml -o harvested-data.model.xml -d data/harvested-data/imported-records.rdf.xml

#Score on publications
# Compare names of publications in VIVO with names of publications from MODS and assign a
#	score value indicating how likely they are the same publication.
#harvester-score -X score-pub.conf.xml

#Match publications
# Use the score values from the previous score to rename publications which we deem are the
#	same, so that they match the URI of the publication in VIVO.
#harvester-match -X match-pub.conf.xml

#Score on authors, organizations, geographic locations, journals, hyperlinks, and
#	date-time intervals
# Same as above, but for authors, organizations, geographic locations, journals, hyperlinks,
#	and date-time intervals.
#harvester-score -X score-author.conf.xml
#harvester-score -X score-publisher.conf.xml

#harvester-score -X score-interval.conf.xml
#harvester-score -X score-datetime.conf.xml

#Match
# Rename matches scored above.
#harvester-match -X match-main.conf.xml

#Score on authorships
# Same as above, but for authorships.
#harvester-score -X score-authorship.conf.xml

#Match
# Rename matching authorships.
#harvester-match -X match-authorship.conf.xml

#Truncate Score Data model
#harvester-jenaconnect -X truncate.config.xml


# Execute ChangeNamespace to get unmatched publications into current namespace
# This is where the new people from the harvest are given uris within the namespace of Vivo
# 	If there is an issue with uris being in another namespace after import, make sure this step
#   was completed for those uris.

#echo "changenamespace"
#harvester-changenamespace -X changenamespace-all.config.xml


# Dump harvested data model for testing
# harvester-jenaconnect -X harvested-dump.config.xml

# Perform an update
# The harvester maintains copies of previous harvests in order to perform the same harvest twice
#   but only add the new statements, while removing the old statements that are no longer
#   contained in the input data. This is done in several steps of finding the old statements,
#   then the new statements, and then applying them to the Vivo main model.

# Find Subtractions
# When making the previous harvest model agree with the current harvest, the statements that exist in
#	the previous harvest but not in the current harvest need to be identified for removal.
harvester-diff -X diff-subtractions.config.xml

# Find Additions
# When making the previous harvest model agree with the current harvest, the statements that exist in
#	the current harvest but not in the previous harvest need to be identified for addition.
harvester-diff -X diff-additions.config.xml

# Apply Subtractions to Previous model
harvester-transfer -w info -o previous-harvest.model.xml -r data/vivo-subtractions.rdf.xml -m
# Apply Additions to Previous model
harvester-transfer -w info -o previous-harvest.model.xml -r data/vivo-additions.rdf.xml

# Now that the changes have been applied to the previous harvest and the harvested data in vivo
#	agree with the previous harvest, the changes are now applied to the vivo model.
if [[ "$1" == "tdb" ]]; then
  # Apply Subtractions to VIVO model
  harvester-transfer -w info -o vivo.model.xml -r data/vivo-subtractions.rdf.xml -m
  # Apply Additions to VIVO model
  harvester-transfer -w info -o vivo.model.xml -r data/vivo-additions.rdf.xml
fi

if [[ "$1" == "" || "$1" == "sparql" ]]; then
  java $HARVESTER_JAVA_OPTS org.vivoweb.harvester.services.SparqlUpdate -X sparqlupdate.conf.xml
fi

#Output some counts
PUBS=`cat data/vivo-additions.rdf.xml | grep oai | wc -l`
AUTHORS=`cat data/vivo-additions.rdf.xml | grep 'http://xmlns.com/foaf/0.1/Person' | wc -l`
AUTHORSHIPS=`cat data/vivo-additions.rdf.xml | grep Authorship | wc -l`
echo "Imported $PUBS publications, $AUTHORS authors, and $AUTHORSHIPS authorships"

echo 'Harvest completed successfully'
