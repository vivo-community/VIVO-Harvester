#!/bin/bash

# Copyright (c) 2012 Symplectic Ltd. All rights reserved.
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.

#Nmae of this harvest
export HARVEST_NAME=example-elements

#Location of the main Vivo Harvester code
export HARVESTER_INSTALL_DIR=

#Date harvest was run
export DATE=`date +%Y-%m-%d'T'%T`

# Add harvester binaries to path for execution
# The tools within this script refer to binaries supplied within the harvester
#	Since they can be located in another directory their path should be
#	included within the classpath and the path environment variables.
export PATH=$PATH:$HARVESTER_INSTALL_DIR/bin
export CLASSPATH=$CLASSPATH:$HARVESTER_INSTALL_DIR/bin/elements-vivo-harvester.jar:$HARVESTER_INSTALL_DIR/bin/harvester.jar:$HARVESTER_INSTALL_DIR/bin/dependency/*
export CLASSPATH=$CLASSPATH:$HARVESTER_INSTALL_DIR/build/elements-vivo-harvester.jar:$HARVESTER_INSTALL_DIR/build/harvester.jar:$HARVESTER_INSTALL_DIR/build/dependency/*

# Exit on first error
# The -e flag prevents the script from continuing even though a tool fails.
#	Continuing after a tool failure is undesirable since the harvested
#	data could be rendered corrupted and incompatible.
set -e

rm -rf data

# Retrieve the data from Elements
harvester-elementsfetch -X elements.config.xml

# We don't need to score or match the data, as we have pulled a full set of information
# But we do need to load it into a Jena model, so that we can perform the update
harvester-transfer -s translated-records.config.xml -o matched-data.model.xml -d data/matched-data/imported-records.rdf.xml

# We don't need to smush if we are generating consistent URIs.
# There are some tricks used in the current default datamp to ensure that is the case, but for a customized datamap
# this may be required
# harvester-smush -r -i matched-data.model.xml -P http://www.symplectic.co.uk/vivo/smush -n http://vivo.symplectic.co.uk/individual/

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
#harvester-transfer -o previous-harvest.model.xml -r data/vivo-subtractions.rdf.xml -m
harvester-transfer -o previous-harvest.model.xml -i subtracted-data.model.xml -m
# Apply Additions to Previous model
#harvester-transfer -o previous-harvest.model.xml -r data/vivo-additions.rdf.xml
harvester-transfer -o previous-harvest.model.xml -i added-data.model.xml

# Now that the changes have been applied to the previous harvest and the harvested data in vivo
#	agree with the previous harvest, the changes are now applied to the vivo model.
# Apply Subtractions to VIVO model
#harvester-transfer -o vivo.model.xml -r data/vivo-subtractions.rdf.xml -m
harvester-transfer -o vivo.model.xml -i subtracted-data.model.xml -m
# Apply Additions to VIVO model
#harvester-transfer -o vivo.model.xml -r data/vivo-additions.rdf.xml
harvester-transfer -o vivo.model.xml -i added-data.model.xml