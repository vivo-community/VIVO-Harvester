#!/bin/bash

#Copyright (c) 2010-2011 VIVO Harvester Team. For full list of contributors, please see the AUTHORS file provided.
#All rights reserved.
#This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html

# set to the directory where the harvester was installed or unpacked
# HARVESTER_INSTALL_DIR is set to the location of the installed harvester
#	If the deb file was used to install the harvester then the
#	directory should be set to /usr/share/vivo/harvester which is the
#	current location associated with the deb installation.
#	Since it is also possible the harvester was installed by
#	uncompressing the tar.gz the setting is available to be changed
#	and should agree with the installation location
HARVESTER_INSTALL_DIR=${WORKING_DIRECTORY} #replaced by servlet
export HARVEST_NAME=csvGrant
export DATE=`date +%Y-%m-%d'T'%T`

# Add harvester binaries to path for execution
# The tools within this script refer to binaries supplied within the harvester
#	Since they can be located in another directory their path should be
#	included within the classpath and the path environment variables.
export PATH=$PATH:$HARVESTER_INSTALL_DIR/bin
export CLASSPATH=$CLASSPATH:$HARVESTER_INSTALL_DIR/bin/harvester.jar:$HARVESTER_INSTALL_DIR/bin/dependency/*
export CLASSPATH=$CLASSPATH:$HARVESTER_INSTALL_DIR/build/harvester.jar:$HARVESTER_INSTALL_DIR/build/dependency/*
# Exit on first error
set -e


cd $HARVESTER_INSTALL_DIR


#if [ -f vivo/scripts/env ]; then
#  . vivo/scripts/env
#else
#  exit 1
#fi
echo "Full Logging in ${HARVEST_NAME}${DATE}.log"

VIVOCONFIG="vivo/config/vivo.xml"
NAMESPACE="http://localhost/individual/"
H2RH="vivo/config/h2-jdbc.xml"
TFRH="vivo/config/tfrh.xml"
BACKUPPATH="$HARVESTER_INSTALL_DIR/backups"
LATESTBACKUPPATH="$BACKUPPATH/latest"
H2MODEL="vivo/config/h2-sdb.xml"

# Set working directory
#HARVESTER_INSTALL_DIR=`dirname "$(cd "${0%/*}" 2>/dev/null; echo "$PWD"/"${0##*/}")"`
#HARVESTER_INSTALL_DIR=$(cd $HARVESTER_INSTALL_DIR; cd ..; pwd)
HARVESTER_INSTALL_DIR=${WORKING_DIRECTORY} #replaced by servlet
cd $HARVESTER_INSTALL_DIR

HARVEST_NAME=csvGrant

#if [ -f vivo/scripts/env ]; then
#  . vivo/scripts/env
#else
#  exit 1
#fi
#echo "Full Logging in $HARVEST_NAME_DATE.log"

# Setting variables for cleaner script lines.
#Base data directory
BASEDIR=${HARVESTED_DATA_PATH} #this gets replaced by servlet
PREVHARVDBURLBASE="jdbc:h2:${HARVESTED_DATA_PATH}prevHarvs/" #GLOBAL_HARVESTED_DATA_RELATIVE_PATH is replaced by server

#data directories
RAWCSVDIR=$BASEDIR/csv
RAWRHDIR=$BASEDIR/rh-raw
RDFRHDIR=$BASEDIR/rh-rdf
MODELDIR=$BASEDIR/model
SCOREDATADIR=$BASEDIR/score-data
MULTIJENADIR=$BASEDIR/multi-jena

#model database urls
RAWCSVDBURL=jdbc:h2:$RAWCSVDIR/store
RAWRHDBURL=jdbc:h2:$RAWRHDIR/store
RDFRHDBURL=jdbc:h2:$RDFRHDIR/store
MODELDBURL=jdbc:h2:$MODELDIR/store
SCOREDATADBURL=jdbc:h2:$SCOREDATADIR/store
MATCHEDDBURL=jdbc:h2:$SCOREDATADIR/match
MULTIJENADBURL=jdbc:h2:$MULTIJENADIR/store

#model names
CSVMODELNAME=csvRawData
MODELNAME=http://localhost/vivo/harvested-data
SCOREDATANAME=csvScoreData
MATCHEDNAME=csvTempharvester-match
MULTIJENANAME=http://localhost/vivo/multi-jena

#temporary copy directory
TEMPCOPYDIR=$BASEDIR/temp-copy

#scoring data models
SCOREINPUT="-i $H2MODEL -ImodelName=$MODELNAME -IdbUrl=$MODELDBURL -IcheckEmpty=$CHECKEMPTY"
SCOREDATA="-s $H2MODEL -SmodelName=$SCOREDATANAME -SdbUrl=$SCOREDATADBURL -ScheckEmpty=$CHECKEMPTY"
SCOREMODELS="$SCOREINPUT -v $VIVOCONFIG -VcheckEmpty=$CHECKEMPTY $SCOREDATA -t $TEMPCOPYDIR"
MATCHOUTPUT="-o $H2MODEL -OmodelName=$MATCHEDNAME -OdbUrl=$MATCHEDDBURL -OcheckEmpty=$CHECKEMPTY"

#Changenamespace settings
CNFLAGS="$SCOREINPUT -v $VIVOCONFIG -n $NAMESPACE"

#The equality test algorithm
EQTEST="org.vivoweb.harvester.score.algorithm.EqualityTest"

#matching properties
GRANTIDNUM="http://vivoweb.org/ontology/score#grantID"
RDFTYPE="http://www.w3.org/1999/02/22-rdf-syntax-ns#type"
RDFSLABEL="http://www.w3.org/2000/01/rdf-schema#label"
PERSONIDNUM="http://vivoweb.org/ontology/score#personID"
DEPTIDNUM="http://vivoweb.org/ontology/score#deptID"
ROLEIN="http://vivoweb.org/ontology/core#roleIn"
PIROLEOF="http://vivoweb.org/ontology/core#principalInvestigatorRoleOf"
COPIROLEOF="http://vivoweb.org/ontology/core#co-PrincipalInvestigatorRoleOf"
DATETIME="http://vivoweb.org/ontology/core#dateTime"

BASEURI="http://vivoweb.org/harvest/csvfile/"

#clear old fetches
rm -rf $RAWRHDIR
rm -rf $RAWCSVDIR

#CSVFILE="files/granttemplatetest.csv"
XSLFILE="vivo/datamaps/csv-grant-to-vivo.xsl"

# Execute Fetch
#harvester-csvtojdbc -i $CSVFILE -d "org.h2.Driver" -c $RAWCSVDBURL -u "sa" -p "" -t "CSV"

#harvester-jdbcfetch -d "org.h2.Driver" -c $RAWCSVDBURL -u "sa" -p "" -o $TFRH -O fileDir=$RAWRHDIR

for CURRENT_FILE in ${UPLOADS_FOLDER}* #UPLOADS_FOLDER variable gets replaced by servlet
do
	harvester-csvtojdbc -i "$CURRENT_FILE" -d "org.h2.Driver" -c $RAWCSVDBURL -u "sa" -p "" -t "CSV"
	
#	harvester-csvtojdbc -i "$CURRENT_FILE" -d "org.h2.Driver" -c $RAWCSVDBURL -u "sa" -p "" -t "CSV2"

#	harvester-jdbcfetch -d "org.h2.Driver" -c $RAWCSVDBURL -u "sa" -p "" -o $TFRH -O fileDir=$RAWRHDIR
done
harvester-jdbcfetch -d "org.h2.Driver" -c $RAWCSVDBURL -u "sa" -p "" -o $TFRH -O fileDir=$RAWRHDIR -n "null"


# backup fetch
#BACKRAW="raw"
#backup-path $RAWRHDIR $BACKRAW
# uncomment to restore previous fetch
#restore-path $RAWRHDIR $BACKRAW

# clear old translates
rm -rf $RDFRHDIR

# Execute Translate
harvester-xsltranslator -i $TFRH -IfileDir=$RAWRHDIR -o $TFRH -OfileDir=$RDFRHDIR -x $XSLFILE

# backup translate
#BACKRDF="rdf"
#backup-path $RDFRHDIR $BACKRDF
# uncomment to restore previous translate
#restore-path $RDFRHDIR $BACKRDF

# Clear old H2 transfer model
rm -rf $MODELDIR

# Execute Transfer to import from record handler into local temp model

harvester-transfer -o $H2MODEL -OmodelName=$MODELNAME -OdbUrl=$MODELDBURL -s $TFRH -SfileDir=$RDFRHDIR

#harvester-transfer -i $H2MODEL -ImodelName=$MODELNAME -IdbUrl=$MODELDBURL -d $BASEDIR/transfer.rdf.xml

# backup H2 transfer Model
#BACKMODEL="model"
#backup-path $MODELDIR $BACKMODEL
# uncomment to restore previous H2 transfer Model
#restore-path $MODELDIR $BACKMODEL

#clear score model for next batch.
rm -rf $SCOREDATADIR

#harvester-smushes in-place(-r) on the Grant id THEN on the person ID  then deptID
harvester-smush $SCOREINPUT -P $GRANTIDNUM -P $PERSONIDNUM -P $DEPTIDNUM -P $DATETIME -n ${BASEURI} -r

#harvester-transfer -i $H2MODEL -ImodelName=$MODELNAME -IdbUrl=$MODELDBURL -d $BASEDIR/harvester-smushed.rdf.xml

# Execute score to match with existing VIVO
# The -n flag value is determined by the XLST file
# The -A -W -F & -P flags need to be internally consistent per call

# Scoring of Grants on GrantNumber
harvester-score $SCOREMODELS -AGrantNumber=$EQTEST -WGrantNumber=1.0 -FGrantNumber=$GRANTIDNUM -PGrantNumber=$GRANTIDNUM -n ${BASEURI}grant/

# Scoring of people on PERSONIDNUM
harvester-score $SCOREMODELS -Aufid=$EQTEST -Wufid=1.0 -Fufid=$PERSONIDNUM -Pufid=$PERSONIDNUM -n ${BASEURI}person/


harvester-smush $SCOREINPUT -P $DEPTIDNUM -n ${BASEURI}org/ -r
# Scoring of orgs on DeptID
harvester-score $SCOREMODELS -AdeptID=$EQTEST -WdeptID=1.0 -FdeptID=$DEPTIDNUM -PdeptID=$DEPTIDNUM -n ${BASEURI}org/


harvester-smush $SCOREINPUT -P $RDFSLABEL -n ${BASEURI}sponsor/ -r
# Scoring sponsors by labels
harvester-score $SCOREMODELS -Alabel=$EQTEST -Wlabel=1.0 -Flabel=$RDFSLABEL -Plabel=$RDFSLABEL -n ${BASEURI}sponsor/

# Find matches using scores and rename nodes to matching uri
harvester-match $SCOREINPUT $SCOREDATA -t 1.0 -r

rm -rf $SCOREDATADIR
rm -rf $TEMPCOPYDIR

# Scoring of PI Roles
PIURI="-Aperson=$EQTEST -Wperson=0.5 -Fperson=$PIROLEOF -Pperson=$PIROLEOF"
GRANTURI="-Agrant=$EQTEST -Wgrant=0.5 -Fgrant=$ROLEIN -Pgrant=$ROLEIN"
harvester-score $SCOREMODELS $PIURI $GRANTURI -n ${BASEURI}piRole/

# Scoring of coPI Roles
COPIURI="-Aperson=$EQTEST -Wperson=0.5 -Fperson=$COPIROLEOF -Pperson=$COPIROLEOF"
harvester-score $SCOREMODELS $COPIURI $GRANTURI -n ${BASEURI}coPiRole/

# Scoring of DateTime Starts and ends
harvester-score $SCOREMODELS -Adate=$EQTEST -Wdate=1.0 -Fdate=$DATETIME -Pdate=$DATETIME -n ${BASEURI}timeInterval/

# Find matches using scores and rename nodes to matching uri
harvester-match $SCOREINPUT $SCOREDATA -t 1.0 -r

rm -rf $SCOREDATADIR
rm -rf $TEMPCOPYDIR

STARTTIME="http://vivoweb.org/ontology/core#start"
ENDTIME="http://vivoweb.org/ontology/core#end"

# Scoring of DateTimeInterval
harvester-score $SCOREMODELS -Asdate=$EQTEST -Wsdate=0.5 -Fsdate=$STARTTIME -Psdate=$STARTTIME -Aedate=$EQTEST -Wedate=0.5 -Fedate=$ENDTIME -Pedate=$ENDTIME -n ${BASEURI}timeInterval/

# Find matches using scores and rename nodes to matching uri
harvester-match $SCOREINPUT $SCOREDATA -t 1.0 -r

harvester-transfer -i $H2MODEL -ImodelName=$MODELNAME -IdbUrl=$MODELDBURL -d $BASEDIR/matched.rdf.xml
# Execute ChangeNamespace to get grants into current namespace
# the -o flag value is determined by the XSLT used to translate the data
harvester-changenamespace $CNFLAGS -u ${BASEURI}grant/

# Execute ChangeNamespace to get orgs into current namespace
# the -o flag value is determined by the XSLT used to translate the data
harvester-changenamespace $CNFLAGS -u ${BASEURI}org/

# Execute ChangeNamespace to get sponsors into current namespace
# the -o flag value is determined by the XSLT used to translate the data
harvester-changenamespace $CNFLAGS -u ${BASEURI}sponsor/

# Execute ChangeNamespace to get people into current namespace
# the -o flag value is determined by the XSLT used to translate the data
harvester-changenamespace $CNFLAGS -u ${BASEURI}person/

# Execute ChangeNamespace to get PI roles into current namespace
# the -o flag value is determined by the XSLT used to translate the data
harvester-changenamespace $CNFLAGS -u ${BASEURI}piRole/

# Execute ChangeNamespace to get co-PI roles into current namespace
# the -o flag value is determined by the XSLT used to translate the data
harvester-changenamespace $CNFLAGS -u ${BASEURI}coPiRole/

# Execute ChangeNamespace to get co-PI roles into current namespace
# the -o flag value is determined by the XSLT used to translate the data
harvester-changenamespace $CNFLAGS -u ${BASEURI}timeInterval/


harvester-transfer -i $H2MODEL -ImodelName=$MODELNAME -IdbUrl=$MODELDBURL -d $BASEDIR/changed.rdf.xml

# backup H2 matched Model
#BACKMATCHED="matched"
#backup-path $MODELDIR $BACKMATCHED
# uncomment to restore previous H2 matched Model
#restore-path $MODELDIR $BACKMATCHED

# Backup pretransfer vivo database, symlink latest to latest.sql
#BACKPREDB="pretransfer"
#backup-mysqldb $BACKPREDB
# uncomment to restore pretransfer vivo database
#restore-mysqldb $BACKPREDB

PREVHARVESTMODEL="http://vivoweb.org/ingest/dsr"
ADDFILE="$BASEDIR/additions.rdf.xml"
SUBFILE="$BASEDIR/subtractions.rdf.xml"

VIVOMODELNAME=`harvester-xpathtool -x $VIVOCONFIG -e "//Model/Param[@name='modelName']"`
VIVOINFMODELNAME="http://vitro.mannlib.cornell.edu/default/vitro-kb-inf"

# Everything harvested will be added to VIVO
harvester-transfer -i $H2MODEL -IdbUrl=$MODELDBURL -ImodelName=$MODELNAME -d $ADDFILE

# Anything with a subject and predicate that matches a subject and predicate being added to VIVO will
#   be removed.  This will be done in two steps.  First, all triples in VIVO that match subjects in the
#   harvested data will be placed in a separate model.  Then that model will be queried for the predicates
#   we are looking to replace with the data input from the harvest.
MULTIJENARESULTFILE="$BASEDIR/multijena.rdf.xml"
MULTIJENAQUERYFILE=vivo/config/grantmultijenaquery.xml
#MULTIJENAQUERY="PREFIX harv: <http://localhost/vivo/> PREFIX vivo: <http://vitro.mannlib.cornell.edu/default/> CONSTRUCT { ?subject ?predicate ?object } FROM NAMED <http://localhost/vivo/harvested-data> FROM NAMED <http://vitro.mannlib.cornell.edu/default/vitro-kb-2> WHERE { GRAPH vivo:vitro-kb-2 { ?subject ?predicate ?object . } GRAPH harv:harvested-data { ?subject ?dummy1 ?dummy2 . } }"
harvester-transfer -i $H2MODEL -ImodelName=$MODELNAME -IdbUrl=$MODELDBURL -o $H2MODEL -OmodelName=$MODELNAME -OdbUrl=$MULTIJENADBURL
harvester-transfer -i $VIVOCONFIG -o $H2MODEL -OmodelName=$VIVOMODELNAME -OdbUrl=$MULTIJENADBURL
harvester-transfer -i $VIVOCONFIG -ImodelName=$VIVOINFMODELNAME -o $H2MODEL -OmodelName=$VIVOMODELNAME -OdbUrl=$MULTIJENADBURL
harvester-jenaconnect -X $MULTIJENAQUERYFILE -j $H2MODEL -JmodelName=$MULTIJENANAME -JdbUrl=$MULTIJENADBURL -d -f $MULTIJENARESULTFILE 

# Step 2 of getting the subtractions
PREDICATESQUERYFILE=vivo/config/grantquery.xml
rm -rf $MULTIJENADIR
harvester-transfer -o $H2MODEL -OmodelName=$MULTIJENANAME -OdbUrl=$MULTIJENADBURL -r $MULTIJENARESULTFILE
#harvester-transfer -i $H2MODEL -ImodelName=$MULTIJENANAME -IdbUrl=$MULTIJENADBURL -d "$BASEDIR/dumpmultijenamodel.rdf.xml"
harvester-jenaconnect -X $PREDICATESQUERYFILE -j $H2MODEL -JmodelName=$MULTIJENANAME -JdbUrl=$MULTIJENADBURL -d -f $SUBFILE 
#harvester-jenaconnect -j $H2MODEL -JmodelName=$MULTIJENANAME -JdbUrl=$MULTIJENADBURL -q "PREFIX harv: <http://localhost/vivo/> CONSTRUCT { ?a ?b ?c } FROM NAMED <http://localhost/vivo/multi-jena> WHERE { GRAPH harv:multi-jena { ?a ?b ?c } }" -d -f $SUBFILE 



# Find Subtractions
#$Diff -m $H2MODEL -MdbUrl=${PREVHARVDBURLBASE}${HARVEST_NAME}/store -McheckEmpty=$CHECKEMPTY -MmodelName=$PREVHARVESTMODEL -s $H2MODEL -ScheckEmpty=$CHECKEMPTY -SdbUrl=$MODELDBURL -SmodelName=$MODELNAME -d $SUBFILE
# Find Additions
#$Diff -m $H2MODEL -McheckEmpty=$CHECKEMPTY -MdbUrl=$MODELDBURL -MmodelName=$MODELNAME -s $H2MODEL -ScheckEmpty=$CHECKEMPTY -SdbUrl=${PREVHARVDBURLBASE}${HARVEST_NAME}/store -SmodelName=$PREVHARVESTMODEL  -d $ADDFILE

# Backup adds and subs
#backup-file $ADDFILE adds.rdf.xml
#backup-file $SUBFILE subs.rdf.xml
#restore-file $ADDFILE adds.rdf.xml
#restore-file $SUBFILE subs.rdf.xml

# Apply Subtractions to Previous model
#harvester-transfer -o $H2MODEL -OdbUrl=${PREVHARVDBURLBASE}${HARVEST_NAME}/store -OcheckEmpty=$CHECKEMPTY -OmodelName=$PREVHARVESTMODEL -r $SUBFILE -m
# Apply Additions to Previous model
#harvester-transfer -o $H2MODEL -OdbUrl=${PREVHARVDBURLBASE}${HARVEST_NAME}/store -OcheckEmpty=$CHECKEMPTY -OmodelName=$PREVHARVESTMODEL -r $ADDFILE
# Apply Subtractions to VIVO
harvester-transfer -o $VIVOCONFIG -OcheckEmpty=$CHECKEMPTY -r $SUBFILE -m
# Apply Additions to VIVO
harvester-transfer -o $VIVOCONFIG -OcheckEmpty=$CHECKEMPTY -r $ADDFILE

rm -rf $TEMPCOPYDIR

# Backup posttransfer vivo database, symlink latest to latest.sql
#BACKPOSTDB="posttransfer"
#backup-mysqldb $BACKPOSTDB
# uncomment to restore posttransfer vivo database
#restore-mysqldb $BACKPOSTDB

#Restart Tomcat
#Tomcat must be restarted in order for the harvested data to appear in VIVO
#echo $HARVEST_NAME ' completed successfully'

#IMPORTANT: This line must exist AS-IS in every File Harvest script.  The server checks the output and uses this line to verify that the harvest completed.
echo 'File Harvest completed successfully' 

#rm cookie.txt
#rm "authenticate?loginName=defaultAdmin&loginPassword=vitro123&loginForm=1"
#wget --cookies=on --keep-session-cookies --save-cookies=cookie.txt "http://localhost:8080/vivo/authenticate?loginName=defaultAdmin&loginPassword=vitro123&loginForm=1"

#rm SearchIndex
#wget --referer=http://first_page --cookies=on --load-cookies=cookie.txt --keep-session-cookies --save-cookies=cookie.txt http://localhost:8080/vivo/SearchIndex

#/etc/init.d/tomcat6 stop
#/etc/init.d/apache2 restart
#/etc/init.d/tomcat6 start
