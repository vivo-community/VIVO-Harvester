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
export VIVO_LOCATION_IN_TOMCAT_DIR=/var/lib/tomcat6/webapps/vivo
export HARVESTER_INSTALL_DIR=/usr/local/src/VIVO-Harvest
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
export EMAIL_RECIPIENT="ufvivotech-l@lists.ufl.edu"

export HARVESTER_LOG_DIR=/data/vivo/harvester/vivo-auto-harvest/peoplesoft-biztalk/log
export HARVESTER_BACKUP_DIR=/data/vivo/harvester/vivo-auto-harvest/peoplesoft-biztalk/backup
export PEOPLE_HARVEST_HOME=/data/vivo/harvester/vivo-auto-harvest/peoplesoft-biztalk/peoplesoft-ingest
export WEBSERVICE_DIR=/data/vivo/web_service
export WEBSERVICE_ARCHIVE_DIR=$WEBSERVICE_DIR/archive/$DATE

# Supply the location of the detailed log file which is generated during the script.
#       If there is an issue with a harvest, this file proves invaluable in finding
#       a solution to the problem. It has become common practice in addressing a problem
#       to request this file. The passwords and usernames are filtered out of this file
#       to prevent these logs from containing sensitive information.
echo "Full Logging in peoplesoft-biztalk-harvest-$DATE.log"

cd $PEOPLE_HARVEST_HOME

# Check to see if the logs directory exists, and if it doesn't create it
if [ ! -d logs ]; then
  mkdir logs
fi

# Check to see if the data directory exists, and if it doesn't create it
if [ ! -d data ]; then
  mkdir data
fi

# Check to see if the harvester log directory exists or not
if [ ! -d $HARVESTER_LOG_DIR ]; then
	mkdir $HARVESTER_LOG_DIR
fi

# Check to see if the harvester backup directory exists or not
if [ ! -d $HARVESTER_BACKUP_DIR ]; then
	mkdir $HARVESTER_BACKUP_DIR
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

# Check to see if raw-records/.metadata exists, and if so, delete it.
if [ -d data/raw-records/.metadata ]; then
  rm -rf data/raw-records/.metadata
fi

# Check to see if previous-data-state exists, and if so, delete it.
if [ -d previous-data-state ]; then
  rm -rf previous-data-state
fi

# Check to see if proxy-vivo exists, and if so delete it.
if [ -d data/proxy-vivo ]; then
  rm -rf data/proxy-vivo
fi

# Check to see if raw-records exists, and if so, delete it.
if [ -d data/raw-records ]; then
  echo "ALERT! raw-records is being deleted PRIOR to any harvest activities. If you wish to manually place records here for harvesting, comment out this block."
  rm -rf data/raw-records
fi

echo "Cleanup complete - moving to logs"

# Create the logfile.txt now to capture all of the analytics
touch logfile.txt

# Change directory to the logs director and touch the file
cd logs
touch $HARVEST_NAME.$DATE.log
ln -sf $HARVEST_NAME.$DATE.log $HARVEST_NAME.latest.log
cd ..

echo "Log creation done - moving to archive files"

# Copy all current incoming files to arvhice
if [ ! -d $WEBSERVICE_ARCHIVE_DIR ]; then
	mkdir $WEBSERVICE_ARCHIVE_DIR
fi
cp $WEBSERVICE_DIR/incoming/* $WEBSERVICE_ARCHIVE_DIR

echo "Archive done - moving to sed"

# Stopgap measure to ensure valid XML reaches the translator. TEMPORARY.
perl -pi -e 's/(&amp;|&)/&amp;/g' $WEBSERVICE_DIR/incoming/*

# Exit on first error
# The -e flag prevents the script from continuing even though a tool fails.
#	Continuing after a tool failure is undesirable since the harvested
#	data could be rendered corrupted and incompatible.
set -e

echo "Sed complete - moving to xmlgrep"

# Execute XMLGrep to remove any person that is marked for renaming by ES - this will prevent issues later
# Also, this will handle fetching the XML files from the WEBSERVICE directory, so that we do not need to create
# a separate fetch process
echo "XMLGREP Rename"
harvester-xmlgrep -X xmlgrep-rename.config.xml

# Stopgap measure to ensure valid XML reaches the translator. TEMPORARY.
#sed -i 's/&/&amp;/g' ./data/raw-records/*

# Execute the translation to convert all input records into RDF
echo "Translate"
harvester-xsltranslator -X xsltranslator.config.xml

# Execute XMLGrep to remove non-targeted people from the group of raw-records
echo "XMLGrep Ignore"
harvester-xmlgrep -X xmlgrep-ignore-students.config.xml

# Execute Email script for sending email with all renamed records to CTSI@vivo.ufl.edu
# This is so the complicated one off records can be handled by the Ontologists, and not
# handled programmatically
echo "Rename Files Email"
cd $PEOPLE_HARVEST_HOME
bash emailRenameFiles.sh

# Run pre-harvest analytics
echo "Running Pre Peoplesoft Ingest Analytics......."
cd $PEOPLE_HARVEST_HOME
bash analytics.sh

# Preseed of Previous-Data-State
# Most ingests are built assuming there are currently none of the data items in VIVO, in UF's case we do have
# people in the VIVO system. We will need to account for that by pre-seeding a model with data from 
# VIVO. We do this by running a series of SPARQL queries, and then transferring them in.
if [ ! -d previous-data-state ]; then
	echo "ALERT: Running Pre-Seed script for previous-data-state. Pulling down relevant UFIDs/deptIDs!"
	cd $PEOPLE_HARVEST_HOME
	bash peopleexport.sh
fi 

# Execute Transfer to import from record handler into local temp model
# From this stage on the script places the data into a Jena model. A model is a
#       data torage structure similar to a database, but is in RDF.
# The harvester tool Transfer is used to move/add/remove/dump data in models.
# For this call on the transfer tool:
# -s refers to the source translated records file, which was just produced by the translator step
# -o refers to the destination model for harvested data
# -d means that this call will also produce a text dump file in the specified location 
echo "Transfer"
harvester-transfer -s translated-records.config.xml -o harvested-data.model.xml -d data/harvested-data/imported-records.rdf.xml

# Execute Score for People
# In the scoring phase the data in the harvest is compared to the data within Vivo and a new model
#       is created with the values / scores of the data comparisons. 
echo "Score People"
harvester-score -X score-people.config.xml

# Find matches using scores and rename nodes to matching uri
# Using the data model created by the score phase, the match process changes the harvested uris for
#       comparison values above the chosen threshold within the xml configuration file.
# This config differs from the previous match config, in that it removes types and literals from the 
#       resources in the incoming model for those that are considered a match.
echo "Match People"
harvester-match -X match-people-departments.config.xml 

# Check to for previous score model and remove it.
if [ -f data/score-data ]; then
  echo "Remove Score Data"
  rm -rf data/score-data
fi

# Execute Score for Departments
# In the scoring phase the data in the harvest is compared to the data within Vivo and a new model
#       is created with the values / scores of the data comparisons. 
echo "Score Departments"
harvester-score -X score-departments.config.xml

# Find matches using scores and rename nodes to matching uri
# Using the data model created by the score phase, the match process changes the harvested uris for
#       comparison values above the chosen threshold within the xml configuration file.
# This config differs from the previous match config, in that it removes types and literals from the 
#       resources in the incoming model for those that are considered a match.
echo "Match Departments"
harvester-match -X match-people-departments.config.xml 

# Check to for previous score model and remove it.
if [ -f data/score-data ]; then
  echo "Remove Score Data"
  rm -rf data/score-data
fi

# Image Preservation
# Using the input model, vivo model, and a private model we will determine if a person has been set to
# protected that was previously not protected. If this person has a photo, we are going to save said photo
# information in the private model. If a person is set to not protected, but were previously protected
# then we will restore their photo from the private model
echo "Image Preservation"
harvester-imagepresduringprivacy -X image-pres.config.xml

# Change Namespace - People
# Once all nodes have been scored and matched, any 'new' nodes will need to have a node number in the 
#       target namespace created
echo "Change People Namespace"
harvester-changenamespace -X changenamespace-people.config.xml

# Change Namespace - Departments
# Once all nodes have been scored and matched, any 'new' nodes will need to have a node number in the 
#       target namespace created
echo "Change Department Namespace"
harvester-changenamespace -X changenamespace-departments.config.xml

# Find Subtractions
# When making the previous harvest model agree with the current harvest, the entries that exist in
#       the previous harvest but not in the current harvest need to be identified for removal.
echo "Diff Subtractions"
harvester-diff -X diff-subtractions.config.xml

# Find Additions
# When making the previous harvest model agree with the current harvest, the entries that exist in
#       the current harvest but not in the previous harvest need to be identified for addition.
echo "Diff Additions"
harvester-diff -X diff-additions.config.xml

# Apply People Diffs to Actual VIVO
echo "Apply Subtractions to Actual VIVO"
harvester-transfer -o vivo.model.xml -r data/vivo-subtractions.rdf.xml -m
echo "Apply Additions to Actual VIVO"
harvester-transfer -o vivo.model.xml -r data/vivo-additions.rdf.xml

# Apply Privacy-Generated Diffs to Actual VIVO
echo "Apply Privacy Subtractions to Actual VIVO"
harvester-transfer -o vivo.model.xml -r data/privacy-vivo-subtractions.rdf.xml -m
echo "Apply Privacy Additions to Actual VIVO"
harvester-transfer -o vivo.model.xml -r data/privacy-vivo-additions.rdf.xml

echo "Pre People Ingest Analytics" &>> logfile.txt
echo "========================================================================="
cat analytics.txt &>> logfile.txt
echo -e "\n" &>>logfile.txt

echo "Running Post Course Ingest Analytics......."
cd $PEOPLE_HARVEST_HOME
bash analytics.sh
echo "Post Course Ingest Analytics" &>> logfile.txt
echo "========================================================================="
cat analytics.txt &>> logfile.txt
echo -e "\n" &>> logfile.txt

# Send analytics email out to group
mail -a "FROM:PeopleSoft_Ingest" -s "PeopleSoft Ingest harvest of $DATE" "$EMAIL_RECIPIENT" < logfile.txt

# Backup logs and deltas into their respective directories.
mv ./logs/* $HARVESTER_LOG_DIR
mv ./data/vivo-additions.rdf.xml $HARVESTER_BACKUP_DIR/vivo-additions.$DATE.rdf.xml
mv ./data/vivo-subtractions.rdf.xml $HARVESTER_BACKUP_DIR/vivo-subtractions.$DATE.rdf.xml
mv ./data/privacy-vivo-subtractions.rdf.xml $HARVESTER_BACKUP_DIR/privacy-vivo-subtractions.$DATE.rdf.xml
mv ./data/privacy-vivo-additions.rdf.xml $HARVESTER_BACKUP_DIR/privacy-vivo-additions.$DATE.rdf.xml

echo 'Harvest completed successfully'

# Reformat ntriple additions / subtractions to make a logfile
sudo bash /data/vivo/manual-edits/reformat.sh people $PEOPLE_HARVEST_HOME
