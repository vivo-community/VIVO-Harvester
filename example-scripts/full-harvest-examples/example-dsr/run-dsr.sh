#!/bash

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
#	If the deb file was used to install the harvester then the
#	directory should be set to /usr/share/vivo/harvester which is the
#	current location associated with the deb installation.
#	Since it is also possible the harvester was installed by
#	uncompressing the tar.gz the setting is available to be changed
#	and should agree with the installation location
HARVESTER_INSTALL_DIR=/home/jrpence/workspace/HarvesterTrunk
HARVEST_NAME=example-dsr
DATE=`date +%Y-%m-%d'T'%T`

# Add harvester binaries to path for execution
# The tools within this script refer to binaries supplied within the harvester
#	Since they can be located in another directory their path should be
#	included within the classpath and the path enviromental variables.
PATH=$PATH:$HARVESTER_INSTALL_DIR/bin
CLASSPATH=$CLASSPATH:$HARVESTER_INSTALL_DIR/bin/harvester-1.1.1.jar:$HARVESTER_INSTALL_DIR/bin/dependency/*

# Exit on first error
# The -e flag prevents the script from continuing even though a tool fails.
#	Continuing after a tool failure is undesirable since the harvested
#	data could be rendered corrupted and incompatible.
set -e

# Supply the location of the detailed log file which is generated during the script.
#	If there is an issue with a harvest, this file proves invaluable in finding
#	a solution to the problem. I has become common practice in addressing a problem
#	to request this file. The passwords and user-names are filter out of this file
#	To prevent these logs from containing sensitive information.
echo "Full Logging in dsr-harvest-$DATE.log"

#clear old data
# For a fresh harvest, the removal of the previous information maintains data integrity.
#	If you are continuing a partial run or wish to use the old and already retrieved
#	data, you will want to comment out this line since it could prevent you from having
# 	the required harvest data.  
rm -rf data

# clone db
# Databaseclone is a tool used to make a local copy of the database. One reason for this
#	is that constantly querying a database could put undue load on a repository. This
#	allows the use of intensive queries to happen to a local copy and only tie up the
#	resources in the local machine.
harvester-databaseclone -X databaseclone.config.xml

# Execute Fetch
# This stage of the script is where the information is gathered together into one local
#	place to facilitate the further steps of the harvest. The data is stored locally
#	in a format based off of the source. The format is a form of RDF yet its ontology
#	too simple to be put into a model and be useful.
# The JDBCFetch tool in particular takes the data from the chosen source described in its
#	configuration XML file and places it into record set in the flat RDF directly 
#	related to the rows, columns and tables described in the target database.
harvester-jdbcfetch -X jdbcfetch.config.xml

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
# -h refers to the source translated records file, which was just produced by the translator step
# -o refers to the destination model for harvested data
# -d means that this call will also produce a text dump file in the specified location 
harvester-transfer -h translated-records.config.xml -o harvested-data.model.xml -d data/harvested-data/imported-records.rdf.xml

# Execute Score for People
# In the scoring phase the data in the harvest is compared to the data within Vivo and a new model
# 	is created with the values / scores of the data comparisons. 
harvester-score -X score-grants.config.xml

# Execute Score for People
# In the scoring phase the data in the harvest is compared to the data within Vivo and a new model
# 	is created with the values / scores of the data comparisons. 
harvester-score -X score-sponsor.config.xml

# Execute Score for People
# In the scoring phase the data in the harvest is compared to the data within Vivo and a new model
# 	is created with the values / scores of the data comparisons. 
harvester-score -X score-people.config.xml

# Execute Score for People
# In the scoring phase the data in the harvest is compared to the data within Vivo and a new model
# 	is created with the values / scores of the data comparisons. 
harvester-score -X score-dept.config.xml

# Find matches using scores and rename nodes to matching uri
# Using the data model created by the score phase, the match process changes the harvested uris for
# 	comparsion values above the chosen threshold within the xml configuration file.
harvester-match -X match-grants.config.xml

# Execute Score for People
# In the scoring phase the data in the harvest is compared to the data within Vivo and a new model
# 	is created with the values / scores of the data comparisons. 
harvester-score -X score-pirole.config.xml

# Execute Score for People
# In the scoring phase the data in the harvest is compared to the data within Vivo and a new model
# 	is created with the values / scores of the data comparisons. 
harvester-score -X score-copirole.config.xml

# Find matches using scores and rename nodes to matching uri
# Using the data model created by the score phase, the match process changes the harvested uris for
# 	comparsion values above the chosen threshold within the xml configuration file.
harvester-match -X match-roles.config.xml

# Smush to remove duplicates
# Using a particular predicate as an identifying data element the smush tool will rename those
#	resources which match to be one resource.
harvester-smush -X smush-grant.config.xml

harvester-smush -X smush-org.config.xml

harvester-smush -X smush-person.config.xml

harvester-smush -X smush-sponsor.config.xml

# Execute ChangeNamespace to get unmatched People into current name-space
# This is where the new people from the harvest are given uris within the name-space of Vivo
# 	If there is an issue with uris being in another name-space, this is the phase
#	which should give some light to the problem.
harvester-changenamespace -X changenamespace-grant.config.xml

# Execute ChangeNamespace to get unmatched People into current name-space
# This is where the new people from the harvest are given uris within the name-space of Vivo
# 	If there is an issue with uris being in another name-space, this is the phase
#	which should give some light to the problem.
harvester-changenamespace -X changenamespace-org.config.xml

# Execute ChangeNamespace to get unmatched People into current name-space
# This is where the new people from the harvest are given uris within the name-space of Vivo
# 	If there is an issue with uris being in another name-space, this is the phase
#	which should give some light to the problem.
harvester-changenamespace -X changenamespace-sponsor.config.xml

# Execute ChangeNamespace to get unmatched People into current name-space
# This is where the new people from the harvest are given uris within the name-space of Vivo
# 	If there is an issue with uris being in another name-space, this is the phase
#	which should give some light to the problem.
harvester-changenamespace -X changenamespace-people.config.xml

# Execute ChangeNamespace to get unmatched People into current name-space
# This is where the new people from the harvest are given uris within the name-space of Vivo
# 	If there is an issue with uris being in another name-space, this is the phase
#	which should give some light to the problem.
harvester-changenamespace -X changenamespace-pirole.config.xml

# Execute ChangeNamespace to get unmatched People into current name-space
# This is where the new people from the harvest are given uris within the name-space of Vivo
# 	If there is an issue with uris being in another name-space, this is the phase
#	which should give some light to the problem.
harvester-changenamespace -X changenamespace-copirole.config.xml

# Execute ChangeNamespace to get unmatched People into current name-space
# This is where the new people from the harvest are given uris within the name-space of Vivo
# 	If there is an issue with uris being in another name-space, this is the phase
#	which should give some light to the problem.
harvester-changenamespace -X changenamespace-timeinterval.config.xml

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

echo 'Harvest completed successfully'
