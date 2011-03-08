#!/bin/bash

# Copyright (c) 2010 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, Michael Barbieri.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the new BSD license
# which accompanies this distribution, and is available at
# http://www.opensource.org/licenses/bsd-license.html
# 
# Contributors:
#     Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, Michael Barbieri - initial API and implementation

#KNOWN ISSUE: Seems to tie in matches that were originally in VIVO into the input model, so that if the input model is cleaned out of VIVO,
#             then those matches will be removed.  Actually they remain, hidden, but much of their data including their rdf:type is gone.  An
#             RDF export will show this.

# Exit on first error
set -e

# Set working directory
HARVESTERDIR=`dirname "$(cd "${0%/*}" 2>/dev/null; echo "$PWD"/"${0##*/}")"`
HARVESTERDIR=$(cd $HARVESTERDIR; cd ..; pwd)

HARVESTER_TASK=mods

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
MODELNAME=modsTempTransfer
SCOREDATADIR=$BASEDIR/score-data
SCOREDATADBURL=jdbc:h2:$SCOREDATADIR/store
SCOREDATANAME=modsScoreData
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
LINKAUTH="http://vivoweb.org/ontology/core#linkedAuthor"
LINKINFORES="http://vivoweb.org/ontology/core#linkedInformationResource"
AUTHINAUTH="http://vivoweb.org/ontology/core#authorInAuthorship"
RDFTYPE="http://www.w3.org/1999/02/22-rdf-syntax-ns#type"
RDFSLABEL="http://www.w3.org/2000/01/rdf-schema#label"
BASEURI="http://vivoweb.org/harvest/mods/"




# clear old translates
rm -rf $RDFRHDIR

# Execute Translate using the mods-to-vivo.xsl file
$XSLTranslator -i $TFRH -IfileDir=$RAWRHDIR -o $H2RH -OdbUrl=$RDFRHDBURL -x config/datamaps/mods-to-vivo.xsl

# backup translate
BACKRDF="rdf"
backup-path $RDFRHDIR $BACKRDF
# uncomment to restore previous translate
#restore-path $RDFRHDIR $BACKRDF

# Clear old H2 transfer model
rm -rf $MODELDIR

# Execute Transfer to import from record handler into local temp model
$Transfer -o $H2MODEL -OmodelName=$MODELNAME -OcheckEmpty=$CHECKEMPTY -OdbUrl=$MODELDBURL -h $H2RH -HdbUrl=$RDFRHDBURL

# backup H2 transfer Model
BACKMODEL="model"
backup-path $MODELDIR $BACKMODEL
# uncomment to restore previous H2 transfer Model
#restore-path $MODELDIR $BACKMODEL

# Clear old H2 score data
rm -rf $SCOREDATADIR

# Clear old H2 match data
rm -rf $MATCHEDDIR

# Clear old H2 temp copy
rm -rf $TEMPCOPYDIR

# Score variables for cleaner lines
SCOREINPUT="-i $H2MODEL -ImodelName=$MODELNAME -IdbUrl=$MODELDBURL -IcheckEmpty=$CHECKEMPTY"
SCOREDATA="-s $H2MODEL -SmodelName=$SCOREDATANAME -SdbUrl=$SCOREDATADBURL -ScheckEmpty=$CHECKEMPTY"
MATCHOUTPUT="-o $H2MODEL -OmodelName=$MATCHEDNAME -OdbUrl=$MATCHEDDBURL -OcheckEmpty=$CHECKEMPTY"
MATCHEDINPUT="-i $H2MODEL -ImodelName=$MATCHEDNAME -IdbUrl=$MATCHEDDBURL -IcheckEmpty=$CHECKEMPTY"
SCOREMODELS="$SCOREINPUT -v $VIVOCONFIG -VcheckEmpty=$CHECKEMPTY $SCOREDATA -t $TEMPCOPYDIR -b $SCOREBATCHSIZE"

# Execute Score to disambiguate data in "scoring" JENA model
TITLE="-Atitle=$EQTEST -Ftitle=$RDFSLABEL -Wtitle=1.0 -Ptitle=$RDFSLABEL"

$Score $SCOREMODELS $TITLE -n  ${BASEURI}pub/
$Match $SCOREINPUT $SCOREDATA -t 0.7 -r

# clear H2 score data Model
rm -rf $SCOREDATADIR


#Author, Organization, Geographic Location, Journal match
LNAME="-AlName=$LEVDIFF -FlName=$FLNAME -WlName=0.5 -PlName=$FLNAME"
FNAME="-AfName=$LEVDIFF -FfName=$FFNAME -WfName=0.3 -PfName=$FFNAME"
RDFSLABELSCORE="-ArdfsLabel=$LEVDIFF -FrdfsLabel=$RDFSLABEL -WrdfsLabel=1.0 -PrdfsLabel=$RDFSLABEL"

$Score $SCOREMODELS $FNAME $LNAME -n ${BASEURI}author/
$Score $SCOREMODELS $RDFSLABELSCORE -n ${BASEURI}org/
$Score $SCOREMODELS $RDFSLABELSCORE -n ${BASEURI}geo/
$Score $SCOREMODELS $RDFSLABELSCORE -n ${BASEURI}journal/
$Match $SCOREINPUT $SCOREDATA -t 0.7 -r


# clear H2 score data Model
rm -rf $SCOREDATADIR

# Clear old H2 temp copy of input (URI here is hardcoded in Score)
$JenaConnect -Jtype=tdb -JdbDir=$TEMPCOPYDIR -JmodelName=http://vivoweb.org/harvester/model/scoring#inputClone -t


#Authorship match
AUTHPUB="-Aauthpub=$EQTEST -Fauthpub=$LINKINFORES -Wauthpub=0.5 -Pauthpub=$LINKINFORES"
AUTHAUTH="-Aauthauth=$EQTEST -Fauthauth=$LINKAUTH -Wauthauth=0.5 -Pauthauth=$LINKAUTH"

$Score $SCOREMODELS $AUTHPUB $AUTHAUTH -n ${BASEURI}authorship/
$Match $SCOREINPUT $SCOREDATA -t 0.7 -r






# backup H2 score data Model
BACKSCOREDATA="scoredata-auths"
backup-path $SCOREDATADIR $BACKSCOREDATA
# uncomment to restore previous H2 matched Model
#restore-path $SCOREDATADIR $BACKSCOREDATA

# clear H2 score data Model
rm -rf $SCOREDATADIR

# Clear old H2 temp copy
rm -rf $TEMPCOPYDIR






# Execute ChangeNamespace lines: the -o flag value is determined by the XSLT used to translate the data
CNFLAGS="$SCOREINPUT -v $VIVOCONFIG -VcheckEmpty=$CHECKEMPTY -n $NAMESPACE"
# Execute ChangeNamespace to get unmatched Publications into current namespace
$ChangeNamespace $CNFLAGS -u ${BASEURI}pub/
# Execute ChangeNamespace to get unmatched Authorships into current namespace
$ChangeNamespace $CNFLAGS -u ${BASEURI}authorship/
# Execute ChangeNamespace to get unmatched Authors into current namespace
$ChangeNamespace $CNFLAGS -u ${BASEURI}author/
# Execute ChangeNamespace to get unmatched Organizations into current namespace
$ChangeNamespace $CNFLAGS -u ${BASEURI}org/
# Execute ChangeNamespace to get unmatched Geographic Locations into current namespace
$ChangeNamespace $CNFLAGS -u ${BASEURI}geo/
# Execute ChangeNamespace to get unmatched Journals into current namespace
$ChangeNamespace $CNFLAGS -u ${BASEURI}journal/


# Backup pretransfer vivo database, symlink latest to latest.sql
BACKPREDB="pretransfer"
backup-mysqldb $BACKPREDB
# uncomment to restore pretransfer vivo database
#restore-mysqldb $BACKPREDB

PREVHARVESTMODEL="http://vivoweb.org/ingest/mods"
ADDFILE="$BASEDIR/additions.rdf.xml"
SUBFILE="$BASEDIR/subtractions.rdf.xml"

# Find Subtractions
$Diff -m $VIVOCONFIG -MmodelName=$PREVHARVESTMODEL -McheckEmpty=$CHECKEMPTY -s $H2MODEL -ScheckEmpty=$CHECKEMPTY -SdbUrl=$MODELDBURL -SmodelName=$MODELNAME -d $SUBFILE
# Find Additions
$Diff -m $H2MODEL -McheckEmpty=$CHECKEMPTY -MdbUrl=$MODELDBURL -MmodelName=$MODELNAME -s $VIVOCONFIG -ScheckEmpty=$CHECKEMPTY -SmodelName=$PREVHARVESTMODEL -d $ADDFILE

PREVHARVESTMODELINPUT="-i $VIVOCONFIG -ImodelName=$PREVHARVESTMODEL -IcheckEmpty=$CHECKEMPTY"


# Backup adds and subs
backup-file $ADDFILE adds.rdf.xml
backup-file $SUBFILE subs.rdf.xml

# Apply Subtractions to Previous model
$Transfer -o $VIVOCONFIG -OcheckEmpty=$CHECKEMPTY -OmodelName=$PREVHARVESTMODEL -r $SUBFILE -m
# Apply Additions to Previous model
$Transfer -o $VIVOCONFIG -OcheckEmpty=$CHECKEMPTY -OmodelName=$PREVHARVESTMODEL -r $ADDFILE
# Apply Subtractions to VIVO
$Transfer -o $VIVOCONFIG -OcheckEmpty=$CHECKEMPTY -r $SUBFILE -m
# Apply Additions to VIVO
$Transfer -o $VIVOCONFIG -OcheckEmpty=$CHECKEMPTY -r $ADDFILE


#Restart Tomcat
#Tomcat must be restarted in order for the harvested data to appear in VIVO
echo $HARVESTER_TASK ' completed successfully'
/etc/init.d/tomcat stop
/etc/init.d/apache2 reload
/etc/init.d/tomcat start
