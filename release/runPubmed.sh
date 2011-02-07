#!/bin/bash

# Copyright (c) 2010 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the new BSD license
# which accompanies this distribution, and is available at
# http://www.opensource.org/licenses/bsd-license.html
# 
# Contributors:
#     Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams - initial API and implementation

# Set working directory
set -e

DIR=$(cd "$(dirname "$0")"; pwd)
cd $DIR
cd ..

HARVESTER_TASK=pubmed

if [ -f scripts/env ]; then
  . scripts/env
else
  exit 1
fi

#variables for model arguments
HCONFIG="config/jenaModels/h2.xml"
INPUT="-i $HCONFIG -IdbUrl=jdbc:h2:harvested-data/h2Pubmed/all/store -ImodelName=Pubmed"
OUTPUT="-o $HCONFIG -OdbUrl=jdbc:h2:harvested-data/h2Pubmed/all/store -OmodelName=Pubmed"
VIVO="-v $VIVOCONFIG"
SCORE="-s $HCONFIG -SdbUrl=jdbc:h2:harvested-data/h2Pubmed/score/store -SmodelName=Pubmed"
MATCHOUTPUT="-o $HCONFIG -OdbUrl=jdbc:h2:harvested-data/h2Pubmed/match/store -OmodelName=Pubmed"
MATCHINPUT="-i $HCONFIG -IdbUrl=jdbc:h2:harvested-data/h2Pubmed/match/store -ImodelName=Pubmed"

#Dump vivo
$Transfer -i $VIVOCONFIG -d vivo_start.rdf

#clear old fetches
rm -rf harvested-data/h2Pubmed/XML

# Execute Fetch for Pubmed
$PubmedFetch -X config/tasks/PubmedFetch.xml

# clear old translates
rm -rf harvested-data/h2Pubmed/RDF

# Execute Translate using the PubmedToVIVO.xsl file
$XSLTranslator -i config/recordHandlers/Pubmed-XML-h2RH.xml -x config/datamaps/PubmedToVivo.xsl -o config/recordHandlers/Pubmed-RDF-h2RH.xml

# Clear old H2 models
rm -rf harvested-data/h2Pubmed/all
rm -rf harvested-data/h2Pubmed/temp

# Execute Transfer to import from record handler into local temp model
$Transfer $OUTPUT -h config/recordHandlers/Pubmed-RDF-h2RH.xml -d pubmed.rdf

# clear old score models
rm -rf harvested-data/h2Pubmed/score

# Execute Score to disambiguate data in "scoring" JENA model
# Execute match to match and link data into "vivo" JENA model
LEVDIFF="org.vivoweb.harvester.score.algorithm.NormalizedLevenshteinDifference"
EXACT="org.vivoweb.harvester.score.algorithm.EqualityTest"
WORKEMAIL="-AwEmail=$EXACT -FwEmail=http://vivoweb.org/ontology/core#workEmail -WwEmail=0.1 -PwEmail=http://vivoweb.org/ontology/score#workEmail"
FNAME="-AfName=$LEVDIFF -FfName=http://xmlns.com/foaf/0.1/firstName -WfName=0.3 -PfName=http://vivoweb.org/ontology/score#foreName"
LNAME="-AlName=$LEVDIFF -FlName=http://xmlns.com/foaf/0.1/lastName -WlName=0.5 -PlName=http://xmlns.com/foaf/0.1/lastName"
MNAME="-AmName=$LEVDIFF -FmName=http://vivoweb.org/ontology/core#middleName -WmName=0.1 -PmName=http://vivoweb.org/ontology/core#middleName"
mkdir harvested-data/h2Pubmed/temp/
TEMP="-t harvested-data/h2Pubmed/temp/"

$Score $VIVO $INPUT $TEMP $SCORE $WORKEMAIL $LNAME $MNAME $FNAME
$Match $INPUT $SCORE $MATCHOUTPUT -t 0.1 -r -c

#Dump score
$Transfer -i $HCONFIG -IdbUrl=jdbc:h2:harvested-data/h2Pubmed/score/store -ImodelName=Pubmed -d score.rdf

#Dump Match
$Transfer $MATCHINPUT -d match.rdf

#remove score statements
$Qualify $MATCHINPUT -n http://vivoweb.org/ontology/score -p

#Dump Qualify
$Transfer $MATCHINPUT -d qualify.rdf

# Execute ChangeNamespace to get into current namespace
$ChangeNamespace $VIVO $MATCHINPUT -n $NAMESPACE -o http://vivoweb.org/harvest/pubmedPub/
$ChangeNamespace $VIVO $MATCHINPUT -n $NAMESPACE -o http://vivoweb.org/harvest/pubmedAuthorship/
$ChangeNamespace $VIVO $MATCHINPUT -n $NAMESPACE -o http://vivoweb.org/harvest/pubmedAuthor/
$ChangeNamespace $VIVO $MATCHINPUT -n $NAMESPACE -o http://vivoweb.org/harvest/pubmedJournal/

#Update VIVO, using previous model as comparison. On first run, previous model won't exist resulting in all statements being passed to VIVO
VIVOMODELNAME="modelName=http://vivoweb.org/ingest/pubmed"
INMODELNAME="modelName=Pubmed"
INURL="dbUrl=jdbc:h2:harvested-data/h2Pubmed/match/store"
ADDFILE="harvested-data/update_Additions.rdf.xml"
SUBFILE="harvested-data/update_Subtractions.rdf.xml"

# Find Subtractions
$Diff -m $VIVOCONFIG -M$VIVOMODELNAME -s $HCONFIG -S$INURL -S$INMODELNAME -d $SUBFILE
# Find Additions
$Diff -m $HCONFIG -M$INURL -M$INMODELNAME -s $VIVOCONFIG -S$VIVOMODELNAME -d $ADDFILE
# Apply Subtractions to Previous model
$Transfer -o $VIVOCONFIG -O$VIVOMODELNAME -r $SUBFILE -m
# Apply Additions to Previous model
$Transfer -o $VIVOCONFIG -O$VIVOMODELNAME -r $ADDFILE
# Apply Subtractions to VIVO
$Transfer -o $VIVOCONFIG -r $SUBFILE -m
# Apply Additions to VIVO
$Transfer -o $VIVOCONFIG -r $ADDFILE

#Dump vivo
$Transfer -i $VIVOCONFIG -d vivo_end.rdf
