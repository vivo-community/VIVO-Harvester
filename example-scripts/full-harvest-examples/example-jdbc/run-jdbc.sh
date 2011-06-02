#!/bash

# Copyright (c) 2010-2011 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the new BSD license
# which accompanies this distribution, and is available at
# http://www.opensource.org/licenses/bsd-license.html
# 
# Contributors:
#     Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams - initial API and implementation

#set to the directory where the harvester was installed or unpacked
HARVESTER_INSTALL_DIR=/usr/share/vivo/harvester
HARVEST_NAME=example-jdbc
DATE=`date +%Y-%m-%d'T'%T`

#Add harvester binaries to path for execution
PATH=$PATH:$HARVESTER_INSTALL_DIR/bin
CLASSPATH=$CLASSPATH:$HARVESTER_INSTALL_DIR/bin/harvester-1.1.1.jar:$HARVESTER_INSTALL_DIR/bin/dependency/*

# Exit on first error
set -e

echo "Full Logging in jdbc-harvest-$DATE.log"

#clear old data
rm -rf data

# clone db
harvester-databaseclone -X databaseclone.config.xml

# Execute Fetch
harvester-jdbcfetch -X jdbcfetch.config.xml

# Execute Translate
harvester-xsltranslator -X xsltranslator.config.xml

# Execute Transfer to import from record handler into local temp model
harvester-transfer -h translated-records.config.xml -o harvested-data.model.xml -d data/harvested-data/imported-records.rdf.xml

# Execute Score for People
harvester-score -X score-people.config.xml

# Find matches using scores and rename nodes to matching uri
harvester-match -X match-people.config.xml

# Execute ChangeNamespace to get unmatched People into current namespace
harvester-changenamespace -X changenamespace-people.config.xml

# Find Subtractions
harvester-diff -X diff-subtractions.config.xml

# Find Additions
harvester-diff -X diff-additions.config.xml

# Apply Subtractions to Previous model
harvester-transfer -o previous-harvest.model.xml -r data/vivo-subtractions.rdf.xml -m
# Apply Additions to Previous model
harvester-transfer -o previous-harvest.model.xml -r data/vivo-additions.rdf.xml
# Apply Subtractions to VIVO for pre-1.2 versions
harvester-transfer -o vivo.model.xml -r data/vivo-subtractions.rdf.xml -m
# Apply Additions to VIVO for pre-1.2 versions
harvester-transfer -o vivo.model.xml -r data/vivo-additions.rdf.xml

echo 'Harvest completed successfully'