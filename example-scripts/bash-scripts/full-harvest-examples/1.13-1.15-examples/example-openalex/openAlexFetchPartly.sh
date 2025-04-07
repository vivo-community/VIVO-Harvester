#!/bin/bash

export HARVESTER_INSTALL_DIR=/home/kampeb/vivo-fid-bau-1-12/openalex-harvester/
export HARVEST_NAME=OpenAlex-Harvest
export DATE=`date +%Y-%m-%d'T'%T`

# Add harvester binaries to path for execution
# The tools within this script refer to binaries supplied within the harvester
# Since they can be located in another directory their path should be
# included within the classpath and the path environment variables.
export PATH=$PATH:$HARVESTER_INSTALL_DIR/bin
export CLASSPATH=$HARVESTER_INSTALL_DIR/build/harvester.jar:$HARVESTER_INSTALL_DIR/build/dependency/*

# Exit on first error
# The -e flag prevents the script from continuing even though a tool fails.
# Continuing after a tool failure is undesirable since the harvested
# data could be rendered corrupted and incompatible.
set -e

# Supply the location of the detailed log file which is generated during the script.
# If there is an issue with a harvest, this file proves invaluable in finding
# a solution to the problem. It has become common practice in addressing a problem
# to request this file. The passwords and usernames are filtered out of this file
# to prevent these logs from containing sensitive information.
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
# If you are continuing a partial run or wish to use the old and already retrieved
# data, you will want to comment out this line since it could prevent you from having
# the required harvest data.
#rm -rf data

# Execute Fetch
# This stage of the script is where the information is gathered together into one local
# place to facilitate the further steps of the harvest. The data is stored locally
# in a format based off of the source. The format is a form of RDF but not in the VIVO ontology
# The JDBCFetch tool in particular takes the data from the chosen source described in its
# configuration XML file and places it into record set in the flat RDF directly
# related to the rows, columns and tables described in the target database.
#echo Execute jsonfetch from OpenAlex.org
#harvester-jsonfetch -w DEBUG -X openAlexfetch.config.xml

# Execute Translate
# This is the part of the script where the input data is transformed into valid RDF
# Translate will apply an xslt file to the fetched data which will result in the data 
# becoming valid RDF in the VIVO ontology
#echo Execute translate
#harvester-xsltranslator -X xsltranslator.config.xml

# Execute Transfer to import from record handler into local temp model
# From this stage on the script places the data into a Jena model. A model is a
#	data storage structure similar to a database, but in RDF.
# The harvester tool Transfer is used to move/add/remove/dump data in models.
# For this call on the transfer tool:
# -s refers to the source translated records file, which was just produced by the translator step
# -o refers to the destination model for harvested data
# -d means that this call will also produce a text dump file in the specified location
#echo Execute initial transfer to triple store
#harvester-transfer -s translated-records.config.xml -o harvested-data.model.xml -d data/harvested-data/imported-records.rdf.xml

# Perform an update
# The harvester maintains copies of previous harvests in order to perform the same harvest twice
#   but only add the new statements, while removing the old statements that are no longer
#   contained in the input data. This is done in several steps of finding the old statements,
#   then the new statements, and then applying them to the Vivo main model.

# Find Subtractions
# When making the previous harvest model agree with the current harvest, the statements that exist in
#	the previous harvest but not in the current harvest need to be identified for removal.
#echo Find Subtractions
#harvester-diff -X diff-subtractions.config.xml

# Find Additions
# When making the previous harvest model agree with the current harvest, the statements that exist in
#	the current harvest but not in the previous harvest need to be identified for addition.
#echo Find Additions
#harvester-diff -X diff-additions.config.xml

# Apply Subtractions to Previous model
#echo Apply Subtractions to Previous model
#harvester-transfer -o previous-harvest.model.xml -r data/vivo-subtractions.rdf.xml -m
# Apply Additions to Previous model
#echo  Apply Additions to Previous model
#harvester-transfer -o previous-harvest.model.xml -r data/vivo-additions.rdf.xml

# Now that the changes have been applied to the previous harvest and the harvested data in vivo
#	agree with the previous harvest, the changes are now applied to the vivo model.
# Apply Subtractions to VIVO
echo Apply Subtractions to VIVO
harvester-transfer -o vivo.model.xml -r data/vivo-subtractions.rdf.xml -m
# Apply Additions to VIVO for pre-1.2 versions
echo Apply Additions to VIVO
harvester-transfer -o vivo.model.xml -r data/vivo-additions.rdf.xml

# Output some counts
ORGS=`cat data/vivo-additions.rdf.xml | grep 'http://xmlns.com/foaf/0.1/Organization' | wc -l`
PEOPLE=`cat data/vivo-additions.rdf.xml | grep 'http://xmlns.com/foaf/0.1/Person' | wc -l`
POSITIONS=`cat data/vivo-additions.rdf.xml | grep 'positionForPerson' | wc -l`
echo "Imported $ORGS organizations, $PEOPLE people, and $POSITIONS positions"

echo 'Harvest completed successfully'
