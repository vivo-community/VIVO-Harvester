#!/bin/bash

#Copyright (c) 2010-2011 VIVO Harvester Team. For full list of contributors, please see the AUTHORS file provided.
#All rights reserved.
#This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html

# set to the directory where the harvester was installed or unpacked
# HARVESTER_INSTALL_DIR is set to the location of the installed harvester
#       If the deb file was used to install the harvester then the
#       directory should be set to /usr/share/vivo/harvester which is the
#       current location associated with the deb installation.
#       Since it is also possible the harvester was installed by
#       uncompressing the tar.gz the setting is available to be changed
#       and should agree with the installation location
VIVO_LOCATION_IN_TOMCAT_DIR=/var/lib/tomcat6/webapps/vivo
export HARVESTER_INSTALL_DIR=/data/vivo/harvester/harvester_1.3
export HARVEST_NAME=example-images
export DATE=`date +%Y-%m-%d'T'%T`

# Add harvester binaries to path for execution
# The tools within this script refer to binaries supplied within the harvester
#       Since they can be located in another directory their path should be
#       included within the classpath and the path environment variables.
export PATH=$PATH:$HARVESTER_INSTALL_DIR/bin
export CLASSPATH=$CLASSPATH:$HARVESTER_INSTALL_DIR/bin/harvester.jar:$HARVESTER_INSTALL_DIR/bin/dependency/*
export CLASSPATH=$CLASSPATH:$HARVESTER_INSTALL_DIR/build/harvester.jar:$HARVESTER_INSTALL_DIR/build/dependency/*

# Supply the location of the detailed log file which is generated during the script.
#       If there is an issue with a harvest, this file proves invaluable in finding
#       a solution to the problem. It has become common practice in addressing a problem
#       to request this file. The passwords and usernames are filtered out of this file
#       to prevent these logs from containing sensitive information.
echo "Full Logging in image-harvest-$DATE.log"
if [ ! -d logs ]; then
  mkdir logs
fi
cd logs
touch $HARVEST_NAME.$DATE.log
ln -sf $HARVEST_NAME.$DATE.log $HARVEST_NAME.latest.log
cd ..


harvester-xsltranslator -X xsltranslator.config.xml
# Execute Transfer to import from record handler into local temp model
# From this stage on the script places the data into a Jena model. A model is a
#       data torage structure similar to a database, but is in RDF.
# The harvester tool Transfer is used to move/add/remove/dump data in models.
# For this call on the transfer tool:
# -s refers to the source translated records file, which was just produced by the translator step
# -o refers to the destination model for harvested data
# -d means that this call will also produce a text dump file in the specified location 
harvester-transfer -s translated-records.config.xml -o harvested-data.model.xml -d data/harvested-data/imported-records.rdf.xml

# Execute Score for People
# In the scoring phase the data in the harvest is compared to the data within Vivo and a new model
#       is created with the values / scores of the data comparisons. 
harvester-score -X score-people.config.xml

# Execute Score for People
# In the scoring phase the data in the harvest is compared to the data within Vivo and a new model
#       is created with the values / scores of the data comparisons. 
harvester-score -X score-departments.config.xml

# Find matches using scores and rename nodes to matching uri
# Using the data model created by the score phase, the match process changes the harvested uris for
#       comparison values above the chosen threshold within the xml configuration file.
# This config differs from the previous match config, in that it removes types and literals from the 
#       resources in the incoming model for those that are considered a match.
harvester-match -X match-people-departments.config.xml 

# Find Subtractions
# When making the previous harvest model agree with the current harvest, the entries that exist in
#       the previous harvest but not in the current harvest need to be identified for removal.
harvester-changenamespace -X changenamespace-people.config.xml

# Find Subtractions
# When making the previous harvest model agree with the current harvest, the entries that exist in
#       the previous harvest but not in the current harvest need to be identified for removal.
harvester-changenamespace -X changenamespace-departments.config.xml

harvester-diff -X diff-subtractions.config.xml

# Find Additions
# When making the previous harvest model agree with the current harvest, the entries that exist in
#       the current harvest but not in the previous harvest need to be identified for addition.
harvester-diff -X diff-additions.config.xml

# Apply Subtractions to Previous model
#harvester-transfer -o previous-harvest.model.xml -r data/vivo-subtractions.rdf.xml -m
# Apply Additions to Previous model
#harvester-transfer -o previous-harvest.model.xml -r data/vivo-additions.rdf.xml

# Now that the changes have been applied to the previous harvest and the harvested data in vivo
#       should agree with the previous harvest, the changes are now applied to the vivo model.
# Apply Subtractions to VIVO for pre-1.2 versions
#harvester-transfer -o vivo.model.xml -r data/vivo-subtractions.rdf.xml -m
# Apply Additions to VIVO for pre-1.2 versions
harvester-transfer -o vivo.model.xml -r data/vivo-additions.rdf.xml

echo 'Harvest completed successfully'
