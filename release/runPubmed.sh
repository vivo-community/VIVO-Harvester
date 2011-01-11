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
INPUT="-i config/jenaModels/h2.xml -IdbUrl=jdbc:h2:XMLVault/h2Pubmed/all/store -ImodelName=Pubmed"
OUTPUT="-o config/jenaModels/h2.xml -OmodelName=Pubmed -OdbUrl=jdbc:h2:XMLVault/h2Pubmed/all/store"
VIVO="-v $VIVOCONFIG"
SCORE="-s config/jenaModels/h2.xml -SdbUrl=jdbc:h2:XMLVault/h2Pubmed/score/store -SmodelName=PubmedScore"

#variables for scoring
LEVDIFF="org.vivoweb.harvester.score.algorithm.NormalizedLevenshteinDifference"
WORKEMAIL="-AwEmail=$LEVDIFF -FwEmail=http://vivoweb.org/ontology/core#workEmail -WwEmail=0.4 -PwEmail=http://vivoweb.org/ontology/score#workEmail"
FNAME="-AfName=$LEVDIFF -FfName=http://xmlns.com/foaf/0.1/firstName -WfName=0.2 -PfName=http://vivoweb.org/ontology/score#foreName"
LNAME="-AlName=$LEVDIFF -FlName=http://xmlns.com/foaf/0.1/lastName -WlName=0.3 -PlName=http://xmlns.com/foaf/0.1/lastName"
MNAME="-AmName=$LEVDIFF -FmName=http://vivoweb.org/ontology/core#middleName -WmName=0.1 -PmName=http://vivoweb.org/ontology/score#middleName"

#clear old fetches
rm -rf XMLVault/h2Pubmed/XML

# Execute Fetch for Pubmed
$PubmedFetch -X config/tasks/PubmedFetch.xml

# clear old translates
rm -rf XMLVault/h2Pubmed/RDF

# Execute Translate using the PubmedToVIVO.xsl file
$XSLTranslator -i config/recordHandlers/Pubmed-XML-h2RH.xml -x config/datamaps/PubmedToVivo.xsl -o config/recordHandlers/Pubmed-RDF-h2RH.xml

# Clear old H2 models
rm -rf XMLVault/h2Pubmed/all

# Execute Transfer to import from record handler into local temp model
$Transfer $OUTPUT -h config/recordHandlers/Pubmed-RDF-h2RH.xml

# clear old score models
rm -rf XMLVault/h2Pubmed/score

# Execute Score to disambiguate data in "scoring" JENA model
$Score $VIVO $INPUT $SCORE $WORKEMAIL $FNAME $LNAME $MNAME

# Execute match to match and link data into "vivo" JENA model
$Match $INPUT $SCORE -t 0.5

# Execute ChangeNamespace to get into current namespace
$ChangeNamespace $VIVO $INPUT -n $NAMESPACE -o http://vivoweb.org/harvest/pubmedPub/
$ChangeNamespace $VIVO $INPUT -n $NAMESPACE -o http://vivoweb.org/harvest/pubmedAuthorship/
$ChangeNamespace $VIVO $INPUT -n $NAMESPACE -o http://vivoweb.org/harvest/pubmedAuthor/
$ChangeNamespace $VIVO $INPUT -n $NAMESPACE -o http://vivoweb.org/harvest/pubmedJournal/

#Update VIVO, using previous model as comparison. On first run, previous model won't exist resulting in all statements being passed to VIVO  
# Find Subtractions
$Diff -m $VIVOCONFIG -MmodelName=http://vivoweb.org/ingest/pubmed -s config/jenaModels/h2.xml -SdbUrl=jdbc:h2:XMLVault/h2Pubmed/score/store -SmodelName=pubmedScore -d XMLVault/update_Subtractions.rdf.xml
# Find Additions
$Diff -m config/jenaModels/h2.xml -MdbUrl=jdbc:h2:XMLVault/h2Pubmed/score/store -MmodelName=pubmedScore -s $VIVOCONFIG -SmodelName=http://vivoweb.org/ingest/pubmed -d XMLVault/update_Additions.rdf.xml
# Apply Subtractions to Previous model
$Transfer -o $VIVOCONFIG -OmodelName=http://vivoweb.org/ingest/pubmed -r XMLVault/update_Subtractions.rdf.xml -m
# Apply Additions to Previous model
$Transfer -o $VIVOCONFIG -OmodelName=http://vivoweb.org/ingest/pubmed -r XMLVault/update_Additions.rdf.xml
# Apply Subtractions to VIVO
$Transfer -o $VIVOCONFIG -r XMLVault/update_Subtractions.rdf.xml -m
# Apply Additions to VIVO
$Transfer -o $VIVOCONFIG -r XMLVault/update_Additions.rdf.xml
