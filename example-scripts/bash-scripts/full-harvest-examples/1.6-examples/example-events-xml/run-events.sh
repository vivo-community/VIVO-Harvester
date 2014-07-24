#!/bin/bash

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
export HARVESTER_INSTALL_DIR=/usr/local/src/VIVO-Harvester
export HARVEST_NAME=example-events
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
harvester-simplexmlfetch -X xmlfetch.config.xml

# Execute Translate
# This is the part of the script where the input data is transformed into valid RDF
#   Translate will apply an xslt file to the fetched data which will result in the data 
#   becoming valid RDF in the VIVO ontology
harvester-xsltranslator -X xsltranslator.config.xml

# Execute Transfer to import from record handler into local temp model
# From this stage on the script places the data into a Jena model. A model is a
#	data storage structure similar to a database, but in RDF.
# The harvester tool Transfer is used to move/add/remove/dump data in models.

#harvester-transfer -s translated-records.config.xml -o harvested-data.model.xml -d data/harvested-data/imported-records.rdf.xml
harvester-transfer -X transfer.config.xml

########################
# Event disambiguation #
########################

# Execute Event Scoring and Matching
# In the scoring phase the data in the harvest is compared to the data within Vivo and a new model
# 	is created with the values / scores of the data comparisons.
# We execute scores in 2 different steps, known as "tiered scoring". The initial score limits our input set to speed up performance 
harvester-score -X score-events.config.xml

# Find matches using scores and rename nodes to matching uri
# Using the data model created by the score phase, the match process changes the harvested uris for
# 	comparison values above the chosen threshold within the xml configuration file.
harvester-match -X match-events.config.xml

# Clear event score data, since we are done with it
harvester-jenaconnect -j score-data.model.xml -t -w INFO


###########################
# Location disambiguation #
###########################

# Execute Locatoin Scoring and Matching
# In the scoring phase the data in the harvest is compared to the data within Vivo and a new model
# 	is created with the values / scores of the data comparisons.
#harvester-score -X score-locations.config.xml

# Find matches using scores and rename nodes to matching uri
# Using the data model created by the score phase, the match process changes the harvested uris for
# 	comparison values above the chosen threshold within the xml configuration file.
#harvester-match -X match-exact.config.xml

# Clear email location data, since we are done with it
#harvester-jenaconnect -j score-data.model.xml -t


# Execute ChangeNamespace to get unmatched events into current namespace
# This is where the new events the the harvest are given uris within the namespace of Vivo
# 	If there is an issue with uris being in another namespace after import, make sure this step
#   was completed for those uris.
harvester-changenamespace -X changenamespace-events.config.xml

# Execute ChangeNamespace to get unmatched time intervals into current namespace
# This is where the new time intervals from the harvest are given uris within the namespace of Vivo
harvester-changenamespace -X changenamespace-timeinterval.config.xml

# Execute ChangeNamespace to get unmatched organizations into current namespace
# This is where the new time intervals from the harvest are given uris within the namespace of Vivo
harvester-changenamespace -X changenamespace-orgs.config.xml

# Execute ChangeNamespace to get unmatched locations into current namespace
# This is where the new locations from the harvest are given uris within the namespace of Vivo
harvester-changenamespace -X changenamespace-location.config.xml


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
harvester-transfer -w INFO -o previous-harvest.model.xml -r data/vivo-subtractions.rdf.xml -m
# Apply Additions to Previous model
harvester-transfer -w INFO -o previous-harvest.model.xml -r data/vivo-additions.rdf.xml

# Now that the changes have been applied to the previous harvest and the harvested data in vivo
#	agree with the previous harvest, the changes are now applied to the vivo model.
# Apply Subtractions to VIVO model
harvester-transfer -w INFO -o vivo.model.xml -r data/vivo-subtractions.rdf.xml -m
# Apply Additions to VIVO model
harvester-transfer -w INFO -o vivo.model.xml -r data/vivo-additions.rdf.xml

#Output some counts
#PUBS=`cat data/vivo-additions.rdf.xml | grep pmid | wc -l`
EVENTS=`cat data/vivo-additions.rdf.xml | grep 'http://purl.org/NET/c4dm/event.owl#Event' | wc -l`
#AUTHORSHIPS=`cat data/vivo-additions.rdf.xml | grep Authorship | wc -l`
echo "Imported $EVENTS events"

echo 'Harvest completed successfully'
