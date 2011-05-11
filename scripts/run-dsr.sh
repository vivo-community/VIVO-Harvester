#!/bin/bash

# Copyright (c) 2010 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the new BSD license
# which accompanies this distribution, and is available at
# http://www.opensource.org/licenses/bsd-license.html
# 
# Contributors:
#     Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence- initial API and implementation

# Exit on first error
set -e

# Set working directory
HARVESTERDIR=`dirname "$(cd "${0%/*}" 2>/dev/null; echo "$PWD"/"${0##*/}")"`
HARVESTERDIR=$(cd $HARVESTERDIR; cd ..; pwd)

HARVESTER_TASK=dsr

if [ -f scripts/env ]; then
  . scripts/env
else
  exit 1
fi
echo "Full Logging in $HARVESTER_TASK_DATE.log"

# Setting variables for cleaner script lines.
#Base data directory
BASEDIR=harvested-data/$HARVESTER_TASK

#data directories
RAWRHDIR=$BASEDIR/rh-raw
RDFRHDIR=$BASEDIR/rh-rdf
MODELDIR=$BASEDIR/model
SCOREDATADIR=$BASEDIR/score-data

#model database urls
RAWRHDBURL=jdbc:h2:$RAWRHDIR/store
RDFRHDBURL=jdbc:h2:$RDFRHDIR/store
MODELDBURL=jdbc:h2:$MODELDIR/store
SCOREDATADBURL=jdbc:h2:$SCOREDATADIR/store
MATCHEDDBURL=jdbc:h2:$SCOREDATADIR/match

#model names
MODELNAME=dsrTempTransfer
SCOREDATANAME=dsrScoreData
MATCHEDNAME=dsrTempMatch

#temporary copy directory
TEMPCOPYDIR=$BASEDIR/temp-copy

#scoring data models
SCOREINPUT="-i $H2MODEL -ImodelName=$MODELNAME -IdbUrl=$MODELDBURL -IcheckEmpty=$CHECKEMPTY"
SCOREDATA="-s $H2MODEL -SmodelName=$SCOREDATANAME -SdbUrl=$SCOREDATADBURL -ScheckEmpty=$CHECKEMPTY"
SCOREMODELS="$SCOREINPUT -v $VIVOCONFIG -VcheckEmpty=$CHECKEMPTY $SCOREDATA -t $TEMPCOPYDIR -b $SCOREBATCHSIZE"
MATCHOUTPUT="-o $H2MODEL -OmodelName=$MATCHEDNAME -OdbUrl=$MATCHEDDBURL -OcheckEmpty=$CHECKEMPTY"

#Changenamespace settings
CNFLAGS="$SCOREINPUT -v $VIVOCONFIG -n $NAMESPACE"

#The equality test algorithm
EQTEST="org.vivoweb.harvester.score.algorithm.EqualityTest"

#matching properties
CONNUM="http://vivo.ufl.edu/ontology/vivo-ufl/psContractNumber"
RDFTYPE="http://www.w3.org/1999/02/22-rdf-syntax-ns#type"
RDFSLABEL="http://www.w3.org/2000/01/rdf-schema#label"
UFID="http://vivo.ufl.edu/ontology/vivo-ufl/ufid"
UFDEPTID="http://vivo.ufl.edu/ontology/vivo-ufl/deptID"
ROLEIN="http://vivoweb.org/ontology/core#roleIn"
PIROLEOF="http://vivoweb.org/ontology/core#principalInvestigatorRoleOf"
COPIROLEOF="http://vivoweb.org/ontology/core#co-PrincipalInvestigatorRoleOf"
DATETIME="http://www.w3.org/2001/XMLSchema#dateTime"

BASEURI="http://vivoweb.org/harvest/ufl/dsr/"

#connection info for reindex of lucene
ADMINNAME="USERNAME"
ADMINPASS="vitro123"

#clear old fetches
rm -rf $RAWRHDIR

# Execute Fetch
$JDBCFetch -X config/tasks/dsr.jdbcfetch.xml -o $H2RH -OdbUrl=$RAWRHDBURL

# backup fetch
BACKRAW="raw"
backup-path $RAWRHDIR $BACKRAW
# uncomment to restore previous fetch
#restore-path $RAWRHDIR $BACKRAW

# clear old translates
rm -rf $RDFRHDIR

# Execute Translate
$XSLTranslator -i $H2RH -IdbUrl=$RAWRHDBURL -o $H2RH -OdbUrl=$RDFRHDBURL -x config/datamaps/dsr-to-vivo.xsl

# backup translate
BACKRDF="rdf"
backup-path $RDFRHDIR $BACKRDF
# uncomment to restore previous translate
#restore-path $RDFRHDIR $BACKRDF

# Clear old H2 transfer model
rm -rf $MODELDIR

# Execute Transfer to import from record handler into local temp model
$Transfer -o $H2MODEL -OmodelName=$MODELNAME -OdbUrl=$MODELDBURL -h $H2RH -HdbUrl=$RDFRHDBURL -n $NAMESPACE

$Transfer -i $H2MODEL -ImodelName=$MODELNAME -IdbUrl=$MODELDBURL -d $BASEDIR/transfer.dsr.rdf.xml

# backup H2 transfer Model
BACKMODEL="model"
backup-path $MODELDIR $BACKMODEL
# uncomment to restore previous H2 transfer Model
#restore-path $MODELDIR $BACKMODEL

#clear score model for next batch.
rm -rf $SCOREDATADIR

#smushes in-place(-r) on the contract number THEN on the UFID
$Smush $SCOREINPUT -P $CONNUM -P $UFID -n ${BASEURI} -r

$Transfer -i $H2MODEL -ImodelName=$MODELNAME -IdbUrl=$MODELDBURL -d $BASEDIR/smushed.dsr.rdf.xml

# Execute score to match with existing VIVO
# The -n flag value is determined by the XLST file
# The -A -W -F & -P flags need to be internally consistent per call

# Scoring of Grants on ContractNumber
$Score $SCOREMODELS -AContractNumber=$EQTEST -WContractNumber=1.0 -FContractNumber=$CONNUM -PContractNumber=$CONNUM -n ${BASEURI}grant/


$Smush $SCOREINPUT -P $RDFSLABEL -n ${BASEURI}sponsor/ -r
# Scoring sponsors by labels
$Score $SCOREMODELS -Alabel=$EQTEST -Wlabel=1.0 -Flabel=$RDFSLABEL -Plabel=$RDFSLABEL -n ${BASEURI}sponsor/

# Find matches using scores and rename nodes to matching uri
#$Match $SCOREINPUT $SCOREDATA -b $SCOREBATCHSIZE -t 1.0 -r

$Transfer -i $H2MODEL -ImodelName=$SCOREDATANAME -IdbUrl=$SCOREDATADBURL -d $BASEDIR/scored-1.dsr.rdf.xml

$Transfer -i $H2MODEL -ImodelName=$MODELNAME -IdbUrl=$MODELDBURL -d $BASEDIR/matched-1.dsr.rdf.xml

#clear score model for next batch.
#rm -rf $SCOREDATADIR

# Clear old H2 temp copy of input
#$JenaConnect -Jtype=tdb -JdbDir=$TEMPCOPYDIR -JmodelName=http://vivoweb.org/harvester/model/scoring#inputClone -t

# Scoring of people on UFID
$Score $SCOREMODELS -Aufid=$EQTEST -Wufid=1.0 -Fufid=$UFID -Pufid=$UFID -n ${BASEURI}person/


$Smush $SCOREINPUT -P $UFDEPTID -n ${BASEURI}org/ -r
# Scoring of orgs on DeptID
$Score $SCOREMODELS -AdeptID=$EQTEST -WdeptID=1.0 -FdeptID=$UFDEPTID -PdeptID=$UFDEPTID -n ${BASEURI}org/

# Scoring of PI Roles
PIURI="-Aperson=$EQTEST -Wperson=0.5 -Fperson=$PIROLEOF -Pperson=$PIROLEOF"
GRANTURI="-Agrant=$EQTEST -Wgrant=0.5 -Fgrant=$ROLEIN -Pgrant=$ROLEIN"
$Score $SCOREMODELS $PIURI $GRANTURI -n ${BASEURI}piRole/

# Scoring of coPI Roles
COPIURI="-Aperson=$EQTEST -Wperson=0.5 -Fperson=$COPIROLEOF -Pperson=$COPIROLEOF"
$Score $SCOREMODELS $COPIURI $GRANTURI -n ${BASEURI}coPiRole/

# Find matches using scores and rename nodes to matching uri
$Match $SCOREINPUT $SCOREDATA -b $SCOREBATCHSIZE -t 1.0 -r


# Scoring of DateTime Starts and ends
$Score $SCOREMODELS -Adate=$EQTEST -Wdate=1.0 -Fdate=$DATETIME -Pdate=$DATETIME -n ${BASEURI}timeInterval/

# Find matches using scores and rename nodes to matching uri
$Match $SCOREINPUT $SCOREDATA -b $SCOREBATCHSIZE -t 1.0 -r

rm -rf $SCOREDATADIR
rm -rf $TEMPCOPYDIR

STARTTIME="http://vivoweb.org/ontology/core#start"
ENDTIME="http://vivoweb.org/ontology/core#end"

# Scoring of DateTimeInterval
$Score $SCOREMODELS -Asdate=$EQTEST -Wsdate=0.5 -Fsdate=$STARTTIME -Psdate=$STARTTIME -Aedate=$EQTEST -Wedate=0.5 -Fedate=$ENDTIME -Pedate=$ENDTIME -n ${BASEURI}timeInterval/

# Find matches using scores and rename nodes to matching uri
$Match $SCOREINPUT $SCOREDATA -b $SCOREBATCHSIZE -t 1.0 -r


$Transfer -i $H2MODEL -ImodelName=$SCOREDATANAME -IdbUrl=$SCOREDATADBURL -d $BASEDIR/scored-2.dsr.rdf.xml

$Transfer -i $H2MODEL -ImodelName=$MODELNAME -IdbUrl=$MODELDBURL -d $BASEDIR/matched-2.dsr.rdf.xml

# Execute ChangeNamespace to get grants into current namespace
# the -o flag value is determined by the XSLT used to translate the data
$ChangeNamespace $CNFLAGS -u ${BASEURI}grant/

# Execute ChangeNamespace to get orgs into current namespace
# the -o flag value is determined by the XSLT used to translate the data
$ChangeNamespace $CNFLAGS -u ${BASEURI}org/

# Execute ChangeNamespace to get sponsors into current namespace
# the -o flag value is determined by the XSLT used to translate the data
$ChangeNamespace $CNFLAGS -u ${BASEURI}sponsor/

# Execute ChangeNamespace to get people into current namespace
# the -o flag value is determined by the XSLT used to translate the data
$ChangeNamespace $CNFLAGS -u ${BASEURI}person/

# Execute ChangeNamespace to get PI roles into current namespace
# the -o flag value is determined by the XSLT used to translate the data
$ChangeNamespace $CNFLAGS -u ${BASEURI}piRole/

# Execute ChangeNamespace to get co-PI roles into current namespace
# the -o flag value is determined by the XSLT used to translate the data
$ChangeNamespace $CNFLAGS -u ${BASEURI}coPiRole/

# Execute ChangeNamespace to get co-PI roles into current namespace
# the -o flag value is determined by the XSLT used to translate the data
$ChangeNamespace $CNFLAGS -u ${BASEURI}timeInterval/


$Transfer -i $H2MODEL -ImodelName=$MODELNAME -IdbUrl=$MODELDBURL -d $BASEDIR/changed.dsr.rdf.xml
# backup H2 matched Model
BACKMATCHED="matched"
backup-path $MODELDIR $BACKMATCHED
# uncomment to restore previous H2 matched Model
#restore-path $MODELDIR $BACKMATCHED

# Backup pretransfer vivo database, symlink latest to latest.sql
BACKPREDB="pretransfer"
backup-mysqldb $BACKPREDB
# uncomment to restore pretransfer vivo database
#restore-mysqldb $BACKPREDB

PREVHARVESTMODEL="http://vivoweb.org/ingest/dsr"
ADDFILE="$BASEDIR/additions.rdf.xml"
SUBFILE="$BASEDIR/subtractions.rdf.xml"

# Find Subtractions
$Diff -m $H2MODEL -MdbUrl=${PREVHARVDBURLBASE}${HARVESTER_TASK}/store -McheckEmpty=$CHECKEMPTY -MmodelName=$PREVHARVESTMODEL -s $H2MODEL -ScheckEmpty=$CHECKEMPTY -SdbUrl=$MODELDBURL -SmodelName=$MODELNAME -d $SUBFILE
# Find Additions
$Diff -m $H2MODEL -McheckEmpty=$CHECKEMPTY -MdbUrl=$MODELDBURL -MmodelName=$MODELNAME -s $H2MODEL -ScheckEmpty=$CHECKEMPTY -SdbUrl=${PREVHARVDBURLBASE}${HARVESTER_TASK}/store -SmodelName=$PREVHARVESTMODEL  -d $ADDFILE

# Backup adds and subs
backup-file $ADDFILE adds.rdf.xml
backup-file $SUBFILE subs.rdf.xml
#restore-file $ADDFILE adds.rdf.xml
#restore-file $SUBFILE subs.rdf.xml

# Apply Subtractions to Previous model
$Transfer -o $H2MODEL -OdbUrl=${PREVHARVDBURLBASE}${HARVESTER_TASK}/store -OcheckEmpty=$CHECKEMPTY -OmodelName=$PREVHARVESTMODEL -r $SUBFILE -m
# Apply Additions to Previous model
$Transfer -o $H2MODEL -OdbUrl=${PREVHARVDBURLBASE}${HARVESTER_TASK}/store -OcheckEmpty=$CHECKEMPTY -OmodelName=$PREVHARVESTMODEL -r $ADDFILE
# Apply Subtractions to VIVO
$Transfer -o $VIVOCONFIG -OcheckEmpty=$CHECKEMPTY -r $SUBFILE -m
# Apply Additions to VIVO
$Transfer -o $VIVOCONFIG -OcheckEmpty=$CHECKEMPTY -r $ADDFILE

# Backup posttransfer vivo database, symlink latest to latest.sql
BACKPOSTDB="posttransfer"
backup-mysqldb $BACKPOSTDB
# uncomment to restore posttransfer vivo database
#restore-mysqldb $BACKPOSTDB

#Restart Tomcat
#Tomcat must be restarted in order for the harvested data to appear in VIVO
echo $HARVESTER_TASK ' completed successfully'

if [$ADMINNAME = "USERNAME"]; then
	/etc/init.d/tomcat6 stop
	/etc/init.d/apache2 restart
	/etc/init.d/tomcat6 start
else
	rm -f "authenticate?loginName=${ADMINNAME}&loginPassword=${ADMINPASS}&loginForm=1"
	rm -f cookie.txt
	wget --cookies=on --keep-session-cookies --save-cookies=cookie.txt "http://localhost:8080/vivo/authenticate?loginName=${ADMINNAME}&loginPassword=${ADMINPASS}&loginForm=1"

	rm -f "SearchIndex"
	wget --referer=http://first_page --cookies=on --load-cookies=cookie.txt --keep-session-cookies --save-cookies=cookie.txt http://localhost:8080/vivo/SearchIndex
fi

