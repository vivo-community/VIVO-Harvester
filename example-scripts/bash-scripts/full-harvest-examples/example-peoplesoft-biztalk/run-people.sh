#!/bin/bash

#Copyright (c) 2010-2011 VIVO Harvester Team. For full list of contributors, please see the AUTHORS file provided.
#All rights reserved.
#This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
# AUTHORS Vincent Sposato, Mayank Saini, Stephen Williams, Rene Ziede

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
export HARVEST_NAME=example-peoplesoft-biztalk
export DATE=`date +%Y-%m-%d'T'%T`

# Add harvester binaries to path for execution
# The tools within this script refer to binaries supplied within the harvester
#       Since they can be located in another directory their path should be
#       included within the classpath and the path environment variables.
export PATH=$PATH:$HARVESTER_INSTALL_DIR/bin
export CLASSPATH=$CLASSPATH:$HARVESTER_INSTALL_DIR/bin/harvester.jar:$HARVESTER_INSTALL_DIR/bin/dependency/*
export CLASSPATH=$CLASSPATH:$HARVESTER_INSTALL_DIR/build/harvester.jar:$HARVESTER_INSTALL_DIR/build/dependency/*

# Set the email address of the person receiving the email
export EMAIL_RECIPIENT="vsposato@ufl.edu"

# Supply the location of the detailed log file which is generated during the script.
#       If there is an issue with a harvest, this file proves invaluable in finding
#       a solution to the problem. It has become common practice in addressing a problem
#       to request this file. The passwords and usernames are filtered out of this file
#       to prevent these logs from containing sensitive information.
echo "Full Logging in peoplesoft-biztalk-harvest-$DATE.log"

# Check to see if the logs directory exists, and if it doesn't create it
if [ ! -d logs ]; then
  mkdir logs
fi

# Check to see if the data directory exists, and if it doesn't create it
if [ ! -d data ]; then
  mkdir data
fi

# Check to see if data/translated-records exists, and if so delete it
# This will prevent translation errors when running the harvest over the same set of data
# more than once
if [ -d data/translated-records ]; then
  rm -rf data/translated-records
fi

# Check to see if the data/ignored-records exists, and if not create it
# so that we don't get errors later on - this is where all records that
# are not in our target audience go to be deleted
if [ ! -d data/ignored-records ]; then
  mkdir data/ignored-records
fi

# Check to see if the data/renamed-records exists, and if not create it
# so that we don't get errors later on - this is where all records that are marked
# for renaming go to be emailed to CTSI
if [ ! -d data/renamed-records ]; then
  mkdir data/renamed-records
fi

# Check to see if there is a harvested-data model directory, and if so delete it
# This is important for subtractions and additions to work correctly, because if data changes
# between harvests for an individual then this will aggregate the data as opposed to treating it
# as an update
if [ -d data/harvested-data ]; then
  rm -rf data/harvested-data
fi

# Check to see if previous harvest's full analytics file is there, and delete it
if [ -f logfile.txt ]; then
  rm -rf logfile.txt
fi

# Create the logfile.txt now to capture all of the analytics
touch logfile.txt

# Change directory to the logs director and touch the file
cd logs
touch $HARVEST_NAME.$DATE.log
ln -sf $HARVEST_NAME.$DATE.log $HARVEST_NAME.latest.log
cd ..


# Execute Fetch
# This stage of the script is where the information is gathered together into one local
#       place to facilitate the further steps of the harvest. The data is stored locally
#       in a format based off of the source. The format is a form of RDF yet its ontology
#       too simple to be put into a model and be useful.
#  The fetch-filter.sh  in particular takes the data from the chosen source described, filter and places it into 
#  different destination directory.

# TODO - We need a fetch process here

# Execute XMLGrep to remove any person that is marked for renaming by ES - this will prevent issues later
harvester-xmlgrep -X xmlgrep-rename.config.xml

# Execute the translation to convert all input records into RDF
harvester-xsltranslator -X xsltranslator.config.xml

# Execute XMLGrep to remove non-targeted people from the group of raw-records
harvester-xmlgrep -X xmlgrep-ignore-students.config.xml

# Execute Email script for sending email with all renamed records to CTSI@vivo.ufl.edu
# This is so the complicated one off records can be handled by the Ontologists, and not
# handled programmatically
bash emailRenameFiles.sh

# Run pre-harvest analytics
echo "Running Pre Peoplesoft Ingest Analytics......."
bash analytics.sh

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

# Execute Score for Departments
# In the scoring phase the data in the harvest is compared to the data within Vivo and a new model
#       is created with the values / scores of the data comparisons. 
harvester-score -X score-departments.config.xml

# Find matches using scores and rename nodes to matching uri
# Using the data model created by the score phase, the match process changes the harvested uris for
#       comparison values above the chosen threshold within the xml configuration file.
# This config differs from the previous match config, in that it removes types and literals from the 
#       resources in the incoming model for those that are considered a match.
harvester-match -X match-people-departments.config.xml 

# Image Preservation
# Using the input model, vivo model, and a private model we will determine if a person has been set to
# protected that was previously not protected. If this person has a photo, we are going to save said photo
# information in the private model. If a person is set to not protected, but were previously protected
# then we will restore their photo from the private model
harvester-imagepresduringprivacy -X image-pres.config.xml

# Change Namespace - People
# Once all nodes have been scored and matched, any 'new' nodes will need to have a node number in the 
#       target namespace created
harvester-changenamespace -X changenamespace-people.config.xml

# Change Namespace - Departments
# Once all nodes have been scored and matched, any 'new' nodes will need to have a node number in the 
#       target namespace created
harvester-changenamespace -X changenamespace-departments.config.xml

# Find Subtractions
# When making the previous harvest model agree with the current harvest, the entries that exist in
#       the previous harvest but not in the current harvest need to be identified for removal.
harvester-diff -X diff-subtractions.config.xml

# Find Additions
# When making the previous harvest model agree with the current harvest, the entries that exist in
#       the current harvest but not in the previous harvest need to be identified for addition.
harvester-diff -X diff-additions.config.xml

# Apply Subtractions to Previous model
harvester-transfer -o previous-harvest.model.xml -r data/vivo-subtractions.rdf.xml -m
# Apply Additions to Previous model
harvester-transfer -o previous-harvest.model.xml -r data/vivo-additions.rdf.xml

# Now that the changes have been applied to the previous harvest and the harvested data in vivo
#       should agree with the previous harvest, the changes are now applied to the vivo model.
# Apply Subtractions to VIVO for pre-1.2 versions
harvester-transfer -o vivo.model.xml -r data/vivo-subtractions.rdf.xml -m
# Apply Additions to VIVO for pre-1.2 versions
harvester-transfer -o vivo.model.xml -r data/vivo-additions.rdf.xml

echo "Pre Course Ingest Analytics" &>> logfile.txt
echo "========================================================================="
cat analytics.txt &>> logfile.txt
echo -e "\n" &>>logfile.txt

echo "Running Post Course Ingest Analytics......."
bash analytics.sh
echo "Post Course Ingest Analytics" &>> logfile.txt
echo "========================================================================="
cat analytics.txt &>> logfile.txt
echo -e "\n" &>> logfile.txt

# Send analytics email out to group
mail -a "FROM:PeopleSoft_Ingest" -s "PeopleSoft Ingest harvest of $DATE" "$EMAIL_RECIPIENT" < logfile.txt

echo 'Harvest completed successfully'
