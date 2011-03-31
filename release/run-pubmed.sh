#!/bin/bash

# Copyright (c) 2010 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the new BSD license
# which accompanies this distribution, and is available at
# http://www.opensource.org/licenses/bsd-license.html
# 
# Contributors:
#     Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams - initial API and implementation

set -e

# Set working directory
HARVESTERDIR=`dirname "$(cd "${0%/*}" 2>/dev/null; echo "$PWD"/"${0##*/}")"`
HARVESTERDIR=$(cd $HARVESTERDIR; cd ..; pwd)

HARVESTER_TASK=pubmed

if [ -f scripts/env ]; then
  . scripts/env
else
  exit 1
fi
echo "Full Logging in $HARVESTER_TASK_DATE.log"

BASEDIR=harvested-data/$HARVESTER_TASK
RAWRHDIR=$BASEDIR/rh-raw
RAWRHDBURL=jdbc:h2:$RAWRHDIR/store
RDFRHDIR=$BASEDIR/rh-rdf
RDFRHDBURL=jdbc:h2:$RDFRHDIR/store
MODELDIR=$BASEDIR/model
MODELDBURL=jdbc:h2:$MODELDIR/store
MODELNAME=pubmedTempTransfer
SCOREDATADIR=$BASEDIR/score-data
SCOREDATADBURL=jdbc:h2:$SCOREDATADIR/store
SCOREDATANAME=pubmedScoreData
TEMPCOPYDIR=$BASEDIR/temp-copy
MATCHEDDIR=$BASEDIR/matched
MATCHEDDBURL=jdbc:h2:$MATCHEDDIR/store
MATCHEDNAME=matchedData

#scoring algorithms
EQTEST="org.vivoweb.harvester.score.algorithm.EqualityTest"
LEVDIFF="org.vivoweb.harvester.score.algorithm.NormalizedLevenshteinDifference"

#matching properties
CWEMAIL="http://vivoweb.org/ontology/core#workEmail"
SWEMAIL="http://vivoweb.org/ontology/score#workEmail"
FFNAME="http://xmlns.com/foaf/0.1/firstName"
SFNAME="http://vivoweb.org/ontology/score#foreName"
FLNAME="http://xmlns.com/foaf/0.1/lastName"
CMNAME="http://vivoweb.org/ontology/core#middleName"
BPMID="http://purl.org/ontology/bibo/pmid"
CTITLE="http://vivoweb.org/ontology/core#title"
BISSN="http://purl.org/ontology/bibo/ISSN"
PVENUEFOR="http://vivoweb.org/ontology/core#publicationVenueFor"
LINKINFORES="http://vivoweb.org/ontology/core#linkedInformationResource"
AUTHINAUTH="http://vivoweb.org/ontology/core#authorInAuthorship"
RDFTYPE="http://www.w3.org/1999/02/22-rdf-syntax-ns#type"
RDFSLABEL="http://www.w3.org/2000/01/rdf-schema#label"
BASEURI="http://vivoweb.org/harvest/pubmed/"

#clear old fetches
rm -rf $RAWRHDIR

#Dump vivo
$Transfer -i $VIVOCONFIG -d vivo_start.rdf

# Execute Fetch for Pubmed
$PubmedFetch -X config/tasks/example.pubmedfetch.xml -o $H2RH -OdbUrl=$RAWRHDBURL

# clear old translates
rm -rf $RDFRHDIR

# Execute Translate using the PubmedToVIVO.xsl file
$XSLTranslator -i $H2RH -IdbUrl=$RAWRHDBURL -o $H2RH -OdbUrl=$RDFRHDBURL -x config/datamaps/pubmed-to-vivo.xsl

# Clear old H2 transfer model
rm -rf $MODELDIR

# Execute Transfer to import from record handler into local temp model
$Transfer -o $H2MODEL -OmodelName=$MODELNAME -OcheckEmpty=$CHECKEMPTY -OdbUrl=$MODELDBURL -h $H2RH -HdbUrl=$RDFRHDBURL
$Transfer -o $H2MODEL -OmodelName=$MODELNAME -OcheckEmpty=$CHECKEMPTY -OdbUrl=$MODELDBURL -d ../modeldumpfile.xml

#Dump Transfer
$Transfer -o $H2MODEL -OmodelName=$MODELNAME -OcheckEmpty=$CHECKEMPTY -OdbUrl=$MODELDBURL -d transfer.rdf   

SCOREINPUT="-i $H2MODEL -ImodelName=$MODELNAME -IdbUrl=$MODELDBURL -IcheckEmpty=$CHECKEMPTY"
SCOREDATA="-s $H2MODEL -SmodelName=$SCOREDATANAME -SdbUrl=$SCOREDATADBURL -ScheckEmpty=$CHECKEMPTY"
MATCHOUTPUT="-o $H2MODEL -OmodelName=$MATCHEDNAME -OdbUrl=$MATCHEDDBURL -OcheckEmpty=$CHECKEMPTY"
SCOREMODELS="$SCOREINPUT -v $VIVOCONFIG -VcheckEmpty=$CHECKEMPTY $SCOREDATA -t $TEMPCOPYDIR -b $SCOREBATCHSIZE"

# Clear old H2 score data
rm -rf $SCOREDATADIR

# Clear old H2 temp copy
rm -rf $TEMPCOPYDIR

# Execute Score to disambiguate data in "scoring" JENA model
WORKEMAIL="-AwEmail=$LEVDIFF -FwEmail=$CWEMAIL -WwEmail=0.7 -PwEmail=$SWEMAIL"
FNAME="-AfName=$LEVDIFF -FfName=$FFNAME -WfName=0.3 -PfName=$SFNAME"
LNAME="-AlName=$LEVDIFF -FlName=$FLNAME -WlName=0.5 -PlName=$FLNAME"
MNAME="-AmName=$LEVDIFF -FmName=$CMNAME -WmName=0.1 -PmName=$CMNAME"
$Score $SCOREMODELS $WORKEMAIL $LNAME $FNAME $MNAME -n ${BASEURI}author/

# Find matches using scores and rename nodes to matching uri and clear literals
$Match $SCOREINPUT $SCOREDATA $MATCHOUTPUT -t 0.7 -r -c
$Transfer -o $H2MODEL -OmodelName=$MODELNAME -OcheckEmpty=$CHECKEMPTY -OdbUrl=$MODELDBURL -d ../scoreddumpfile.xml
$Transfer $MATCHOUTPUT -d ../matcheddumpfile.xml

# clear H2 score data Model
rm -rf $SCOREDATADIR

# Clear old H2 temp copy
rm -rf $TEMPCOPYDIR

MATCHEDINPUT="-i $H2MODEL -ImodelName=$MATCHEDNAME -IdbUrl=$MATCHEDDBURL -IcheckEmpty=$CHECKEMPTY"
SCOREMODELS="$MATCHEDINPUT -v $VIVOCONFIG -VcheckEmpty=$CHECKEMPTY $SCOREDATA -t $TEMPCOPYDIR -b $SCOREBATCHSIZE"

# find the originally ingested publication
$Score $SCOREMODELS -Apmid=$EQTEST -Fpmid=$BPMID -Wpmid=1.0 -Ppmid=$BPMID -n ${BASEURI}pub/

# find the originally ingested journal
TITLE="-Atitle=$EQTEST -Ftitle=$CTITLE -Wtitle=1.0 -Ptitle=$CTITLE"
ISSN="-Aissn=$EQTEST -Fissn=$BISSN -Wissn=1.0 -Pissn=$BISSN"
JOURNALPUB="-Ajournalpub=$EQTEST -Fjournalpub=$PVENUEFOR -Wjournalpub=1.0 -Pjournalpub=$PVENUEFOR"
$Score $SCOREMODELS $TITLE $ISSN $JOURNALPUB -n ${BASEURI}journal/

# Find matches using scores and rename nodes to matching uri and clear literals
$Match $MATCHEDINPUT $SCOREDATA -t 1.0 -r
rm -rf $SCOREDATADIR

RDFSLAB="-Ardfslabel=$EQTEST -Frdfslabel=$RDFSLABEL -Wrdfslabel=0.5 -Prdfslabel=$RDFSLABEL"

# find the originally ingested Authorship
$Score $SCOREMODELS $RDFSLAB -Aauthpub=$EQTEST -Fauthpub=$LINKINFORES -Wauthpub=0.5 -Pauthpub=$LINKINFORES -n ${BASEURI}authorship/

# Find matches using scores and rename nodes to matching uri and clear literals
$Match $MATCHEDINPUT $SCOREDATA -t 1.0 -r
rm -rf $SCOREDATADIR

# find the originally ingested  Author
$Score $SCOREMODELS $RDFSLAB -Aauthtoship=$EQTEST -Fauthtoship=$AUTHINAUTH -Wauthtoship=0.5 -Pauthtoship=$AUTHINAUTH -n ${BASEURI}author/

# Find matches using scores and rename nodes to matching uri and clear literals
$Match $MATCHEDINPUT $SCOREDATA -t 1.0 -r

#Dump score
$Transfer $SCOREINPUT -d score.rdf

#Dump Match
$Transfer $MATCHEDINPUT -d match.rdf

# clear H2 score data Model
rm -rf $SCOREDATADIR

#remove score statements
$Qualify $MATCHEDINPUT -n http://vivoweb.org/ontology/score -p

# Execute ChangeNamespace lines: the -o flag value is determined by the XSLT used to translate the data
CNFLAGS="$MATCHEDINPUT -v $VIVOCONFIG -VcheckEmpty=$CHECKEMPTY -n $NAMESPACE"
# Execute ChangeNamespace to get unmatched Publications into current namespace
$ChangeNamespace $CNFLAGS -o ${BASEURI}pub/
# Execute ChangeNamespace to get unmatched Authorships into current namespace
$ChangeNamespace $CNFLAGS -o ${BASEURI}authorship/
# Execute ChangeNamespace to get unmatched Authors into current namespace
#$ChangeNamespace $CNFLAGS -o ${BASEURI}author/
$Qualify $MATCHEDINPUT -n ${BASEURI}author/ -c
# Execute ChangeNamespace to get unmatched Journals into current namespace
$ChangeNamespace $CNFLAGS -o ${BASEURI}journal/

PREVHARVESTMODEL="http://vivoweb.org/ingest/pubmed"
ADDFILE="$BASEDIR/additions.rdf.xml"
SUBFILE="$BASEDIR/subtractions.rdf.xml"

# Find Subtractions
$Diff -m $VIVOCONFIG -MmodelName=$PREVHARVESTMODEL -McheckEmpty=$CHECKEMPTY -s $H2MODEL -ScheckEmpty=$CHECKEMPTY -SdbUrl=$MATCHEDDBURL -SmodelName=$MATCHEDNAME -d $SUBFILE
# Find Additions
$Diff -m $H2MODEL -McheckEmpty=$CHECKEMPTY -MdbUrl=$MATCHEDDBURL -MmodelName=$MATCHEDNAME -s $VIVOCONFIG -ScheckEmpty=$CHECKEMPTY -SmodelName=$PREVHARVESTMODEL -d $ADDFILE

# Apply Subtractions to Previous model
$Transfer -o $VIVOCONFIG -OcheckEmpty=$CHECKEMPTY -OmodelName=$PREVHARVESTMODEL -r $SUBFILE -m
# Apply Additions to Previous model
$Transfer -o $VIVOCONFIG -OcheckEmpty=$CHECKEMPTY -OmodelName=$PREVHARVESTMODEL -r $ADDFILE
# Apply Subtractions to VIVO
$Transfer -o $VIVOCONFIG -OcheckEmpty=$CHECKEMPTY -r $SUBFILE -m
# Apply Additions to VIVO
$Transfer -o $VIVOCONFIG -OcheckEmpty=$CHECKEMPTY -r $ADDFILE

#Dump vivo
$Transfer -i $VIVOCONFIG -d vivo_end.rdf