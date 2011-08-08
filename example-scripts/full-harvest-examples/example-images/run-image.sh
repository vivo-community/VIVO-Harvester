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
VIVO_LOCATION_IN_TOMCAT_DIR=/var/lib/tomcat6/webapps/vivo
export HARVESTER_INSTALL_DIR=/usr/share/vivo/harvester
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

#clear old data
# For a fresh harvest, the removal of the previous information maintains data integrity.
#	If you are continuing a partial run or wish to use the old and already retrieved
#	data, you will want to comment out this line since it could prevent you from having
# 	the required harvest data.  
#rm -rf data

#remove previous model
rm -f model.xml
#remove previous ufids.txt
rm -f ufids.txt
#remove upload directory
rm -rf upload
#remove backed uup images
#	In this directory all the images that are not mapped to people we saved adn these images are used  
#rm -rf backup


#Look for fulllImages directory if not present exit the script
if [ ! -d "fullImages" ]; then
        echo "Missing fullImages directory, please create fullImages directory and put all full images in this directory"
	exit
fi


#Look for  if they are not present exit the script
if [ ! -d "thumbnails" ]; then
        echo "Missing thumbnails directory, please create thumbnails directory and put all thumbnails in this directory"
        exit
fi


#Get a model of the people who dont have images 
touch model.xml
harvester-jenaconnect -j vivo.model.xml -q "CONSTRUCT { ?URI  <http://vivo.ufl.edu/ontology/vivo-ufl/ufid> ?UFID  } WHERE { ?URI <http://vivo.ufl.edu/ontology/vivo-ufl/ufid> ?UFID . NOT EXISTS { ?URI <http://vitro.mannlib.cornell.edu/ns/vitro/public#mainImage> ?y . } }" -Q RDF/XML -f model.xml

#Get the ufids of the people who dont have images using the model generated above
grep -o "[0-9]\{8\}</...:ufid>$" model.xml  > ufids.txt

if [ ! -d "upload" -a ! -d "backup" ]; then
	mkdir upload
	mkdir upload/fullImages
	mkdir upload/thumbnails		

	mkdir backup
	mkdir backup/fullImages
	mkdir backup/thumbnails    
fi


#move any images from the previous harvest stored in backup directory in to images directory
numberOfFiles=`ls -A ./backup/fullImages/ | wc -l`


if [ "$numberOfFiles" == "0" ]; then
	echo "There are no backed up images in backup directory"
else
	echo "Backed up images are used in the new harvest"
	mv backup/fullImages/* fullImages/
        mv backup/thumbnails/* thumbnails/
fi

  
#Generate upload and backup folders 
#	For each image in the uplod folder there is corresponding person in VIVO
#	Back up folder contains images for which there is no corresponding people in VIVO or there is a coreesponding person and already have an image
####java CreateFolders $HARVESTER_INSTALL_DIR/example-scripts/example-images
harvester-createimagefolders -p $HARVESTER_INSTALL_DIR/example-scripts/example-images

#If the upload folder is empty then exit the script



#Create raw records for all the images
#	Each xml file contains just the ufid of a person	 
if [ ! -d "data" ]; then
	mkdir data
        mkdir data/raw-records
fi
cd data
cd raw-records
FOLDER="$HARVESTER_INSTALL_DIR/example-scripts/example-images/upload/fullImages/*"
for file in $FOLDER
do
        imageName=`echo $file | grep -o "[0-9]\{8\}.*"`
        echo "$file" | sed 's/[^0-9]*//g' | xargs -n1 -I {} sh -c "echo '<?xml version=\"1.0\"?><imageName>$imageName</imageName>'> '{}'"        
done
cd ..
cd ..

# Execute Translate
# This is the part of the script where the outside data, in its flat RDF form is used to
#	create the more linked and descriptive form related to the ontological constructs.
#	The traditional XSL language is used to achieve this part of the work-flow.
harvester-xsltranslator -X xsltranslator.config.xml

# Execute Transfer to import from record handler into local temp model
# From this stage on the script places the data into a Jena model. A model is a
#	data torage structure similar to a database, but is in RDF.
# The harvester tool Transfer is used to move/add/remove/dump data in models.
# For this call on the transfer tool:
# -s refers to the source translated records file, which was just produced by the translator step
# -o refers to the destination model for harvested data
# -d means that this call will also produce a text dump file in the specified location 
harvester-transfer -s translated-records.config.xml -o harvested-data.model.xml -d data/harvested-data/imported-records.rdf.xml

# Execute Score for People
# In the scoring phase the data in the harvest is compared to the data within Vivo and a new model
# 	is created with the values / scores of the data comparisons. 
harvester-score -X score-people.config.xml

# Find matches using scores and rename nodes to matching uri
# Using the data model created by the score phase, the match process changes the harvested uris for
# 	comparison values above the chosen threshold within the xml configuration file.
# This config differs from the previous match config, in that it removes types and literals from the 
#       resources in the incoming model for those that are considered a match.
harvester-match -X match-roles.config.xml

# Find Subtractions
# When making the previous harvest model agree with the current harvest, the entries that exist in
#	the previous harvest but not in the current harvest need to be identified for removal.
harvester-diff -X diff-subtractions.config.xml

# Find Additions
# When making the previous harvest model agree with the current harvest, the entries that exist in
#	the current harvest but not in the previous harvest need to be identified for addition.
harvester-diff -X diff-additions.config.xml

# Apply Subtractions to Previous model
harvester-transfer -o previous-harvest.model.xml -r data/vivo-subtractions.rdf.xml -m
# Apply Additions to Previous model
harvester-transfer -o previous-harvest.model.xml -r data/vivo-additions.rdf.xml

# Now that the changes have been applied to the previous harvest and the harvested data in vivo
#	should agree with the previous harvest, the changes are now applied to the vivo model.
# Apply Subtractions to VIVO for pre-1.2 versions
harvester-transfer -o vivo.model.xml -r data/vivo-subtractions.rdf.xml -m
# Apply Additions to VIVO for pre-1.2 versions
harvester-transfer -o vivo.model.xml -r data/vivo-additions.rdf.xml

#if there is no harvestedImages dirctory make one in tomcat VIVO
if [ ! -d "$VIVO_LOCATION_IN_TOMCAT_DIR/harvestedImages" ]; then
	mkdir $VIVO_LOCATION_IN_TOMCAT_DIR/harvestedImages
  	mkdir $VIVO_LOCATION_IN_TOMCAT_DIR/harvestedImages/fullImages
	mkdir $VIVO_LOCATION_IN_TOMCAT_DIR/harvestedImages/thumbnails
fi

numberOfFiles=`ls -A ./upload/fullImages/ | wc -l`
#move all the files in upload directory to  harvestedImages only if there are images in upload directory
if [ "$numberOfFiles" == "0" ]; then
	echo "There are no images to upload"	        
else
       echo "Uploading images to VIVO"
       mv ./upload/fullImages/* $VIVO_LOCATION_IN_TOMCAT_DIR/harvestedImages/fullImages/
       mv ./upload/thumbnails/* $VIVO_LOCATION_IN_TOMCAT_DIR/harvestedImages/thumbnails/
fi

echo 'Harvest completed successfully'
