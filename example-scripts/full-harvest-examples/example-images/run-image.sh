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
rm -rf data
rm -f model.xml
rm -f ufids.txt
rm -f upload
rm -f backup

#Get a model of the people who dont have images 
touch model.xml
harvester-jenaconnect -j vivo.model.xml -q "CONSTRUCT { ?URI  <http://vivo.ufl.edu/ontology/vivo-ufl/ufid> ?UFID  } WHERE { ?URI <http://vivo.ufl.edu/ontology/vivo-ufl/ufid> ?UFID . NOT EXISTS { ?URI <http://vitro.mannlib.cornell.edu/ns/vitro/public#mainImage> ?y . } }" -Q RDF/XML -f model.xml

#Get the ufids of the people who dont have images using the model generated above
grep -o "[0-9]\{8\}</j.2:ufid>$" model.xml  > ufids.txt

#Generate upload and backup folders 
#	For each image in the uplod folder there is corresponding person in VIVO
#	Back up folder contains images for which there is no corresponding people in VIVO or there is a coreesponding person and already have an image
java createFolders $HARVESTER_INSTALL_DIR/example-scripts/example-images

#Create XML files for all the images
#	Each xml file contains just the ufid of a person	 
mkdir data
cd data
mkdir raw-records
cd raw-records
ls -1 $HARVESTER_INSTALL_DIR/example-scripts/example-images/upload/ | sed 's/[^0-9]*//g' | xargs -n1 -I {} sh -c "echo '<?xml version=\"1.0\"?><ufid>'{}'</ufid>' > '{}'" 
cd ..
cd ..

# Execute Translate
# This is the part of the script where the outside data, in its flat RDF form is used to
#	create the more linked and descriptive form related to the ontological constructs.
#	The traditional XSL language is used to achieve this part of the work-flow.
harvester-xsltranslator -X xsltranslator.config.xml

# Execute Transfer to import from record handler into local temp model
# From this stage on the script places the data into a Jena model. A model is a
#	data storage structure similar to a database, but is in RDF.
# The harvester tool Transfer is used to move/add/remove/dump data in models.
# For this call on the transfer tool:
# -s refers to the source translated records file, which was just produced by the translator step
# -o refers to the destination model for harvested data
# -d means that this call will also produce a text dump file in the specified location 
harvester-transfer -s translated-records.config.xml -o harvested-data.model.xml -d data/harvested-data/imported-records.rdf.xml

echo '******* END ********'
