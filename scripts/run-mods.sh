#!/bin/bash

# Copyright (c) 2010 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, Michael Barbieri.
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
LINKINFORES="http://vivoweb.org/ontology/core#linkedInformationResource"
AUTHINAUTH="http://vivoweb.org/ontology/core#authorInAuthorship"
RDFTYPE="http://www.w3.org/1999/02/22-rdf-syntax-ns#type"
RDFSLABEL="http://www.w3.org/2000/01/rdf-schema#label"
BASEURI="http://vivoweb.org/harvest/mods/"

#clear old fetches
#rm -rf $RAWRHDIR

#Dump vivo
#$Transfer -i $VIVOCONFIG -d vivo_start.rdf

## Execute Fetch for Pubmed
#$PubmedFetch -X config/tasks/example.pubmedfetch.xml -o $H2RH -OdbUrl=$RAWRHDBURL
#$PubmedFetch -X config/tasks/ufl.pubmedfetch.xml -o $H2RH -OdbUrl=$RAWRHDBURL

## backup fetch
#BACKRAW="raw"
#backup-path $RAWRHDIR $BACKRAW
## uncomment to restore previous fetch
##restore-path $RAWRHDIR $BACKRAW

# clear old translates
rm -rf $RDFRHDIR

# Execute Translate using the mods-to-vivo.xsl file
$XSLTranslator -i config/recordhandlers/mods-xml.xml -o $H2RH -OdbUrl=$RDFRHDBURL -x config/datamaps/mods-to-vivo.xsl
#$XSLTranslator -i config/recordhandlers/mods-xml.xml -x config/datamaps/mods-to-vivo.xsl -o config/recordhandlers/mods-rdf.xml
#$XSLTranslator -i $H2RH -IdbUrl=$RAWRHDBURL -o $H2RH -OdbUrl=$RDFRHDBURL -x config/datamaps/mods-to-vivo.xsl

$Transfer -h $H2RH -HdbUrl=$RDFRHDBURL -d ../dumpfile1.xml

# backup translate
BACKRDF="rdf"
backup-path $RDFRHDIR $BACKRDF
# uncomment to restore previous translate
#restore-path $RDFRHDIR $BACKRDF

# Clear old H2 transfer model
rm -rf $MODELDIR

# Execute Transfer to import from record handler into local temp model
$Transfer -o $H2MODEL -OmodelName=$MODELNAME -OcheckEmpty=$CHECKEMPTY -OdbUrl=$MODELDBURL -h $H2RH -HdbUrl=$RDFRHDBURL
#$Transfer -o $H2MODEL -OmodelName=$MODELNAME -OcheckEmpty=$CHECKEMPTY -OdbUrl=$MODELDBURL -d ../modeldumpfile.xml

$Transfer -i $H2MODEL -ImodelName=$MODELNAME -IcheckEmpty=$CHECKEMPTY -IdbUrl=$MODELDBURL -d ../dumpfile2.xml

# backup H2 transfer Model
BACKMODEL="model"
backup-path $MODELDIR $BACKMODEL
# uncomment to restore previous H2 transfer Model
#restore-path $MODELDIR $BACKMODEL

SCOREINPUT="-i $H2MODEL -ImodelName=$MODELNAME -IdbUrl=$MODELDBURL -IcheckEmpty=$CHECKEMPTY"
SCOREDATA="-s $H2MODEL -SmodelName=$SCOREDATANAME -SdbUrl=$SCOREDATADBURL -ScheckEmpty=$CHECKEMPTY"
MATCHOUTPUT="-o $H2MODEL -OmodelName=$MATCHEDNAME -OdbUrl=$MATCHEDDBURL -OcheckEmpty=$CHECKEMPTY"
SCOREMODELS="$SCOREINPUT -v $VIVOCONFIG -VcheckEmpty=$CHECKEMPTY $SCOREDATA -t $TEMPCOPYDIR -b $SCOREBATCHSIZE"

# Clear old H2 score data
rm -rf $SCOREDATADIR

# Clear old H2 temp copy
rm -rf $TEMPCOPYDIR

# Execute Score to disambiguate data in "scoring" JENA model
WORKEMAIL="-AwEmail=$LEVDIFF -FwEmail=$CWEMAIL -WwEmail=0.5 -PwEmail=$SWEMAIL"
LNAME="-AlName=$LEVDIFF -FlName=$FLNAME -WlName=0.5 -PlName=$FLNAME"
#FNAME="-AfName=$LEVDIFF -FfName=$FFNAME -WfName=0.3 -PfName=$SFNAME"
#MNAME="-AmName=$LEVDIFF -FmName=$CMNAME -WmName=0.1 -PmName=$CMNAME"
$Score $SCOREMODELS $WORKEMAIL $LNAME -n ${BASEURI}author/
#$Score $SCOREMODELS $WORKEMAIL $LNAME $FNAME $MNAME -n ${BASEURI}author/

# Find matches using scores and rename nodes to matching uri and clear literals
$Match $SCOREINPUT $SCOREDATA $MATCHOUTPUT -t 0.7 -r -c
#$Transfer -o $H2MODEL -OmodelName=$MODELNAME -OcheckEmpty=$CHECKEMPTY -OdbUrl=$MODELDBURL -d ../scoreddumpfile.xml
#$Transfer $MATCHOUTPUT -d ../matcheddumpfile.xml

# backup H2 score data Model
BACKSCOREDATA="scoredata-auths"
backup-path $SCOREDATADIR $BACKSCOREDATA
# uncomment to restore previous H2 matched Model
#restore-path $SCOREDATADIR $BACKSCOREDATA

# clear H2 score data Model
rm -rf $SCOREDATADIR

# clear H2 match data Model
#rm -rf $MATCHEDDIR

# Clear old H2 temp copy
rm -rf $TEMPCOPYDIR

MATCHEDINPUT="-i $H2MODEL -ImodelName=$MATCHEDNAME -IdbUrl=$MATCHEDDBURL -IcheckEmpty=$CHECKEMPTY"
SCOREMODELS="$MATCHEDINPUT -v $VIVOCONFIG -VcheckEmpty=$CHECKEMPTY $SCOREDATA -t $TEMPCOPYDIR -b $SCOREBATCHSIZE"

# find the originally ingested publication
#$Score $SCOREMODELS -Apmid=$EQTEST -Fpmid=$BPMID -Wpmid=1.0 -Ppmid=$BPMID -n ${BASEURI}pub/

# find the originally ingested journal
TITLE="-Atitle=$EQTEST -Ftitle=$CTITLE -Wtitle=1.0 -Ptitle=$CTITLE"
ISSN="-Aissn=$EQTEST -Fissn=$BISSN -Wissn=1.0 -Pissn=$BISSN"
JOURNALPUB="-Ajournalpub=$EQTEST -Fjournalpub=$PVENUEFOR -Wjournalpub=1.0 -Pjournalpub=$PVENUEFOR"
#$Score $SCOREMODELS $TITLE $ISSN $JOURNALPUB -n ${BASEURI}journal/

# Find matches using scores and rename nodes to matching uri and clear literals
#$Match $MATCHEDINPUT $SCOREDATA -t 1.0 -r
rm -rf $SCOREDATADIR

RDFSLAB="-Ardfslabel=$EQTEST -Frdfslabel=$RDFSLABEL -Wrdfslabel=0.5 -Prdfslabel=$RDFSLABEL"

# find the originally ingested Authorship
#$Score $SCOREMODELS $RDFSLAB -Aauthpub=$EQTEST -Fauthpub=$LINKINFORES -Wauthpub=0.5 -Pauthpub=$LINKINFORES -n ${BASEURI}authorship/

# Find matches using scores and rename nodes to matching uri and clear literals
#$Match $MATCHEDINPUT $SCOREDATA -t 1.0 -r
rm -rf $SCOREDATADIR

# find the originally ingested  Author
#$Score $SCOREMODELS $RDFSLAB -Aauthtoship=$EQTEST -Fauthtoship=$AUTHINAUTH -Wauthtoship=0.5 -Pauthtoship=$AUTHINAUTH -n ${BASEURI}author/

# Find matches using scores and rename nodes to matching uri and clear literals
#$Match $MATCHEDINPUT $SCOREDATA -t 1.0 -r

#Dump score
#$Transfer $SCOREINPUT -d score.rdf

#Dump Match
#$Transfer $MATCHEDINPUT -d match.rdf

# clear H2 score data Model
rm -rf $SCOREDATADIR

#remove score statements
$Qualify $MATCHEDINPUT -n http://vivoweb.org/ontology/score -p

$Transfer $MATCHEDINPUT -d ../dumpfile3.xml
$Transfer $SCOREINPUT -d ../dumpfile4.xml



# Execute ChangeNamespace lines: the -o flag value is determined by the XSLT used to translate the data
#CNFLAGS="$MATCHEDINPUT -v $VIVOCONFIG -VcheckEmpty=$CHECKEMPTY -n $NAMESPACE"
CNFLAGS="$SCOREINPUT -v $VIVOCONFIG -VcheckEmpty=$CHECKEMPTY -n $NAMESPACE"
# Execute ChangeNamespace to get unmatched Publications into current namespace
$ChangeNamespace $CNFLAGS -o ${BASEURI}pub/
# Execute ChangeNamespace to get unmatched Authorships into current namespace
$ChangeNamespace $CNFLAGS -o ${BASEURI}authorship/
# Execute ChangeNamespace to get unmatched Authors into current namespace
$ChangeNamespace $CNFLAGS -o ${BASEURI}author/
#$Qualify $MATCHEDINPUT -n ${BASEURI}author/ -c
## Execute ChangeNamespace to get unmatched Journals into current namespace
#$ChangeNamespace $CNFLAGS -o ${BASEURI}journal/

$Transfer $SCOREINPUT -d ../dumpfile5.xml


# backup H2 matched Model
BACKMATCHED="matched"
backup-path $MATCHEDDIR $BACKMATCHED
# uncomment to restore previous H2 matched Model
#restore-path $MATCHEDDIR $BACKMATCHED

# Backup pretransfer vivo database, symlink latest to latest.sql
BACKPREDB="pretransfer"
backup-mysqldb $BACKPREDB
# uncomment to restore pretransfer vivo database
#restore-mysqldb $BACKPREDB

PREVHARVESTMODEL="http://vivoweb.org/ingest/mods"
ADDFILE="$BASEDIR/additions.rdf.xml"
SUBFILE="$BASEDIR/subtractions.rdf.xml"

# Find Subtractions
#$Diff -m $VIVOCONFIG -MmodelName=$PREVHARVESTMODEL -McheckEmpty=$CHECKEMPTY -s $H2MODEL -ScheckEmpty=$CHECKEMPTY -SdbUrl=$MATCHEDDBURL -SmodelName=$MATCHEDNAME -d $SUBFILE
# Find Additions
#$Diff -m $H2MODEL -McheckEmpty=$CHECKEMPTY -MdbUrl=$MATCHEDDBURL -MmodelName=$MATCHEDNAME -s $VIVOCONFIG -ScheckEmpty=$CHECKEMPTY -SmodelName=$PREVHARVESTMODEL -d $ADDFILE
$Diff -m $H2MODEL -McheckEmpty=$CHECKEMPTY -MdbUrl=$MODELDBURL -MmodelName=$MODELNAME -s $VIVOCONFIG -ScheckEmpty=$CHECKEMPTY -SmodelName=$PREVHARVESTMODEL -d $ADDFILE

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

#Dump vivo
$Transfer -i $VIVOCONFIG -d ../vivo_end_dump.rdf

#Restart Tomcat
#Tomcat must be restarted in order for the harvested data to appear in VIVO
echo $HARVESTER_TASK ' completed successfully'
/etc/init.d/tomcat stop
/etc/init.d/apache2 reload
/etc/init.d/tomcat start





































exit

#!/bin/bash

###under construction


# Set working directory
set -e

DIR=$(cd "$(dirname "$0")"; pwd)
cd $DIR
cd ..

HARVESTER_TASK=mods

if [ -f scripts/env ]; then
  . scripts/env
else
  exit 1
fi
echo "Full Logging in $HARVESTER_TASK_DATE.log"

#variables for model arguments
HCONFIG="config/models/h2-sdb.xml"
INPUT="-i $HCONFIG -IdbUrl=jdbc:h2:XMLVault/h2MODS/all/store -ImodelName=MODS"
OUTPUT="-o $HCONFIG -OdbUrl=jdbc:h2:XMLVault/h2MODS/all/store -OmodelName=MODS"
VIVO="-v $VIVOCONFIG"
SCORE="-s $HCONFIG -SdbUrl=jdbc:h2:XMLVault/h2MODS/score/store -SmodelName=MODS"
MATCHOUTPUT="-o $HCONFIG -OdbUrl=jdbc:h2:XMLVault/h2MODS/match/store -OmodelName=MODS"
MATCHINPUT="-i $HCONFIG -IdbUrl=jdbc:h2:XMLVault/h2MODS/match/store -ImodelName=MODS"

SCOREINPUT="-i $HCONFIG -IdbUrl=jdbc:h2:XMLVault/h2MODS/score/store -ImodelName=MODS"

SANITIZEROUTPUTPATH=`grep fileDir config/recordhandlers/mods-xml.xml| sed 's|^.*>\(.*\/[^<]*\)<.*$|\1|g'`



# Sanitize input
$SanitizeMODSXML -inputPath XMLVault/MODS-RH-XML-unsanitized -outputPath $SANITIZEROUTPUTPATH

# clear old translates
rm -rf XMLVault/MODS-RH-RDF

# Execute Translate using the mods-to-vivo.xsl file
$XSLTranslator -i config/recordhandlers/mods-xml.xml -x config/datamaps/mods-to-vivo.xsl -o config/recordhandlers/mods-rdf.xml

# backup translate
date=`date +%Y-%m-%d_%T`
tar -czpf backups/mods.rdf.$date.tar.gz XMLVault/MODS-RH-RDF
rm -rf backups/mods.rdf.latest.tar.gz
ln -s mods.rdf.$date.tar.gz backups/mods.rdf.latest.tar.gz
# uncomment to restore previous translate
#tar -xzpf backups/mods.rdf.latest.tar.gz XMLVault/MODS-RH-RDF

# Clear old H2 models
rm -rf XMLVault/h2MODS/all
rm -rf XMLVault/h2MODS/temp

# Execute Transfer to import from record handler into local temp model
$Transfer $OUTPUT -h config/recordhandlers/mods-rdf.xml

# backup H2 translate Models
date=`date +%Y-%m-%d_%T`
tar -czpf backups/mods.all.$date.tar.gz XMLVault/h2MODS/all
rm -rf backups/mods.all.latest.tar.gz
ln -s ps.all.$date.tar.gz backups/mods.all.latest.tar.gz
# uncomment to restore previous H2 translate models
#tar -xzpf backups/mods.all.latest.tar.gz XMLVault/h2MODS/all

# clear old score models
rm -rf XMLVault/h2MODS/score

# Execute Score to disambiguate data in "scoring" JENA model
# Execute match to match and link data into "vivo" JENA model
LEVDIFF="org.vivoweb.harvester.score.algorithm.NormalizedLevenshteinDifference"
WORKEMAIL="-AwEmail=$LEVDIFF -FwEmail=http://vivoweb.org/ontology/core#workEmail -WwEmail=0.5 -PwEmail=http://vivoweb.org/ontology/score#workEmail"
#FNAME="-AfName=$LEVDIFF -FfName=http://xmlns.com/foaf/0.1/firstName -WfName=0.3 -PfName=http://vivoweb.org/ontology/score#foreName"
LNAME="-AlName=$LEVDIFF -FlName=http://xmlns.com/foaf/0.1/lastName -WlName=0.5 -PlName=http://xmlns.com/foaf/0.1/lastName"
#MNAME="-AmName=$LEVDIFF -FmName=http://vivoweb.org/ontology/core#middleName -WmName=0.2 -PmName=http://vivoweb.org/ontology/score#middleName"
mkdir XMLVault/h2MODS/temp/
TEMP="-t XMLVault/h2MODS/temp/"

#$Score $VIVO $INPUT $TEMP $SCORE $WORKEMAIL $LNAME

#$Match $INPUT $SCORE $MATCHOUTPUT -t 0.9 -r
$Score $VIVO $INPUT $TEMP $SCORE $FNAME $LNAME
$Match $INPUT $SCORE -t 0.8 -r

# back H2 score models
date=`date +%Y-%m-%d_%T`
tar -czpf backups/mods.scored.$date.tar.gz XMLVault/h2MODS/score
rm -rf backups/mods.scored.latest.tar.gz
ln -s ps.scored.$date.tar.gz backups/mods.scored.latest.tar.gz
# uncomment to restore previous H2 score models
#tar -xzpf backups/mods.scored.latest.tar.gz XMLVault/h2MODS/score

#remove score statements
$Qualify $MATCHINPUT -n http://vivoweb.org/ontology/score -p

# Execute ChangeNamespace to get into current namespace
$ChangeNamespace $VIVO $INPUT -n $NAMESPACE -o http://vivoweb.org/harvest/modsPub/
$ChangeNamespace $VIVO $INPUT -n $NAMESPACE -o http://vivoweb.org/harvest/modsAuthorship/
$ChangeNamespace $VIVO $INPUT -n $NAMESPACE -o http://vivoweb.org/harvest/modsAuthor/
$ChangeNamespace $VIVO $INPUT -n $NAMESPACE -o http://vivoweb.org/harvest/modsPublisher/
$ChangeNamespace $VIVO $INPUT -n $NAMESPACE -o http://vivoweb.org/harvest/modsPubVenue/
$ChangeNamespace $VIVO $INPUT -n $NAMESPACE -o http://vivoweb.org/harvest/modsPubDate/

# Backup pretransfer vivo database, symlink latest to latest.sql
date=`date +%Y-%m-%d_%T`
mysqldump -h $SERVER -u $USERNAME -p$PASSWORD $DBNAME > backups/$DBNAME.mods.pretransfer.$date.sql
rm -rf backups/$DBNAME.mods.pretransfer.latest.sql
ln -s $DBNAME.mods.pretransfer.$date.sql backups/$DBNAME.mods.pretransfer.latest.sql

#Update VIVO, using previous model as comparison. On first run, previous model won't exist resulting in all statements being passed to VIVO
VIVOMODELNAME="modelName=http://vivoweb.org/ingest/mods"
INMODELNAME="modelName=MODS"
INURL="dbUrl=jdbc:h2:XMLVault/h2MODS/match/store"
ADDFILE="XMLVault/update_Additions.rdf.xml"
SUBFILE="XMLVault/update_Subtractions.rdf.xml"

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

# Backup posttransfer vivo database, symlink latest to latest.sql
date=`date +%Y-%m-%d_%T`
mysqldump -h $SERVER -u $USERNAME -p$PASSWORD $DBNAME > backups/$DBNAME.mods.posttransfer.$date.sql
rm -rf backups/$DBNAME.mods.posttransfer.latest.sql
ln -s $DBNAME.mods.posttransfer.$date.sql backups/$DBNAME.mods.posttransfer.latest.sql

#Restart Tomcat
#Tomcat must be restarted in order for the harvested data to appear in VIVO
echo $HARVESTER_TASK ' completed successfully'
/etc/init.d/tomcat stop
/etc/init.d/apache2 reload
/etc/init.d/tomcat start

