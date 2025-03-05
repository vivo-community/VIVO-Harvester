#!/bin/bash

# Set environment variables
export HARVESTER_INSTALL_DIR=/usr/local/src/VIVO-Harvester
export HARVEST_NAME=example-pubmed
export DATE=`date +%Y-%m-%d'T'%T`

# Add harvester binaries to path
export PATH=$PATH:$HARVESTER_INSTALL_DIR/bin
export CLASSPATH=$CLASSPATH:$HARVESTER_INSTALL_DIR/bin/harvester.jar:$HARVESTER_INSTALL_DIR/bin/dependency/*
export CLASSPATH=$CLASSPATH:$HARVESTER_INSTALL_DIR/build/harvester.jar:$HARVESTER_INSTALL_DIR/build/dependency/*

set -e  # Exit on first error

# Logging setup
echo "Full Logging in $HARVEST_NAME.$DATE.log"
mkdir -p logs
cd logs
touch $HARVEST_NAME.$DATE.log
ln -sf $HARVEST_NAME.$DATE.log $HARVEST_NAME.latest.log
cd ..

# Clear old data
rm -rf data

# Execute Fetch
harvester-pubmedhttpfetch -X pubmedfetch.config.xml

# Execute Translate
harvester-xsltranslator -X xsltranslator.config.xml

# Execute Transfer to import from record handler into local temp model
harvester-transfer -w INFO -s translated-records.config.xml -o harvested-data.model.xml -d data/harvested-data/imported-records.rdf.xml

# Find Subtractions
harvester-diff -X diff-subtractions.config.xml

# Find Additions
harvester-diff -X diff-additions.config.xml

# Apply Subtractions to Previous model (TDB)
harvester-transfer -w INFO -o previous-harvest-tdb.model.xml -r data/vivo-subtractions.rdf.xml -m

# Apply Additions to Previous model (TDB)
harvester-transfer -w INFO -o previous-harvest-tdb.model.xml -r data/vivo-additions.rdf.xml

# Apply changes to VIVO model (TDB)
harvester-transfer -w INFO -o vivo-tdb.model.xml -r data/vivo-subtractions.rdf.xml -m
harvester-transfer -w INFO -o vivo-tdb.model.xml -r data/vivo-additions.rdf.xml

# Output counts
PUBS=`grep -c oai data/vivo-additions.rdf.xml`
AUTHORS=`grep -c 'http://xmlns.com/foaf/0.1/Person' data/vivo-additions.rdf.xml`
AUTHORSHIPS=`grep -c Authorship data/vivo-additions.rdf.xml`
echo "Imported $PUBS publications, $AUTHORS authors, and $AUTHORSHIPS authorships"

echo 'Harvest completed successfully'
