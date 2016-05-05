@echo off
REM Copyright (c) 2010-2011 VIVO Harvester Team. For full list of contributors, please see the AUTHORS file provided.
REM All rights reserved.
REM This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html

REM  set to the directory where the harvester was installed or unpacked
REM  HARVESTER_INSTALL_DIR is set to the location of the installed harvester
REM 	If the deb file was used to install the harvester then the
REM 	directory should be set to /usr/share/vivo/harvester which is the
REM 	current location associated with the deb installation.
REM 	Since it is also possible the harvester was installed by
REM 	uncompressing the tar.gz the setting is available to be changed
REM 	and should agree with the installation location
set HARVESTER_INSTALL_DIR=/usr/local/src/VIVO-Harvester
set HARVEST_NAME=example-events
FOR %%A IN (%Date:/=%) DO SET Today=%%A

REM  set the CLASSPATH and HARVESTER_JAVA_OPTS to be used by all commands

set CLASSPATH=%HARVESTER_INSTALL_DIR%/build/harvester.jar;%HARVESTER_INSTALL_DIR%/build/dependency/*
set HARVESTER_JAVA_OPTS=-Xms1024M -Xmx1024M
REM set HARVESTER_JAVA_OPTS=

REM  Supply the location of the detailed log file which is generated during the script.
REM 	If there is an issue with a harvest, this file proves invaluable in finding
REM 	a solution to the problem. It has become common practice in addressing a problem
REM 	to request this file. The passwords and usernames are filtered out of this file
REM 	to prevent these logs from containing sensitive information.
echo Full Logging in %HARVEST_NAME%.%TODAY%.log 

IF not exist logs (
  mkdir logs
)
REM clear old data
REM  For a fresh harvest, the removal of the previous information maintains data integrity.
REM 	If you are continuing a partial run or wish to use the old and already retrieved
REM 	data, you will want to comment out this line since it could prevent you from having
REM  	the required harvest data.  


IF exist data (
  rmdir /s /q data
)


REM  Execute Fetch
REM  This stage of the script is where the information is gathered together into one local
REM 	place to facilitate the further steps of the harvest. The data is stored locally
REM 	in a format based off of the source. The format is a form of RDF but not in the VIVO ontology
@java %HARVESTER_JAVA_OPTS% -cp %CLASSPATH% org.vivoweb.harvester.fetch.SimpleXMLFetch -X xmlfetch.config.xml
if %errorlevel% neq 0 exit /b %errorlevel%


REM  Execute Translate
REM  This is the part of the script where the input data is transformed into valid RDF
REM    Translate will apply an xslt file to the fetched data which will result in the data 
REM    becoming valid RDF in the VIVO ontology

@java %HARVESTER_JAVA_OPTS% -cp %CLASSPATH% org.vivoweb.harvester.translate.XSLTranslator -X xsltranslator.config.xml
if %errorlevel% neq 0 exit /b %errorlevel%

REM  Execute Transfer to import from record handler into local temp model
REM  From this stage on the script places the data into a Jena model. A model is a
REM 	data storage structure similar to a database, but in RDF.
REM  The harvester tool Transfer is used to move/add/remove/dump data in models.

@java %HARVESTER_JAVA_OPTS% -cp %CLASSPATH% org.vivoweb.harvester.transfer.Transfer -X transfer.config.xml
if %errorlevel% neq 0 exit /b %errorlevel%

REM #######################
REM  Event disambiguation #
REM #######################

REM  Execute Event Scoring and Matching
REM  In the scoring phase the data in the harvest is compared to the data within Vivo and a new model
REM  	is created with the values / scores of the data comparisons.
REM  We execute scores in 2 different steps, known as "tiered scoring". The initial score limits our input set to speed up performance 

@java %HARVESTER_JAVA_OPTS% -cp %CLASSPATH% org.vivoweb.harvester.score.Score -X score-events.config.xml
if %errorlevel% neq 0 exit /b %errorlevel%

REM  Find matches using scores and rename nodes to matching uri
REM  Using the data model created by the score phase, the match process changes the harvested uris for
REM  	comparison values above the chosen threshold within the xml configuration file.

@java %HARVESTER_JAVA_OPTS% -cp %CLASSPATH% org.vivoweb.harvester.score.Match -X match-events.config.xml
if %errorlevel% neq 0 exit /b %errorlevel%

REM  Clear event score data, since we are done with it
@java %HARVESTER_JAVA_OPTS% -cp %CLASSPATH% org.vivoweb.harvester.util.repo.JenaConnect -j score-data.model.xml -t -w INFO
if %errorlevel% neq 0 exit /b %errorlevel%

REM ##########################
REM  Location disambiguation #
REM ##########################

REM  Execute Locatoin Scoring and Matching
REM  In the scoring phase the data in the harvest is compared to the data within Vivo and a new model
REM  	is created with the values / scores of the data comparisons.
REM harvester-score -X score-locations.config.xml

REM  Find matches using scores and rename nodes to matching uri
REM  Using the data model created by the score phase, the match process changes the harvested uris for
REM  	comparison values above the chosen threshold within the xml configuration file.
REM harvester-match -X match-exact.config.xml

REM  Clear email location data, since we are done with it
REM harvester-jenaconnect -j score-data.model.xml -t


REM  Execute ChangeNamespace to get unmatched events into current namespace
REM  This is where the new events the the harvest are given uris within the namespace of Vivo
REM  	If there is an issue with uris being in another namespace after import, make sure this step
REM    was completed for those uris.

@java %HARVESTER_JAVA_OPTS% -cp %CLASSPATH% org.vivoweb.harvester.qualify.ChangeNamespace -X changenamespace-events.config.xml
if %errorlevel% neq 0 exit /b %errorlevel%

REM  Execute ChangeNamespace to get unmatched time intervals into current namespace
REM  This is where the new time intervals from the harvest are given uris within the namespace of Vivo

@java %HARVESTER_JAVA_OPTS% -cp %CLASSPATH% org.vivoweb.harvester.qualify.ChangeNamespace -X changenamespace-timeinterval.config.xml
if %errorlevel% neq 0 exit /b %errorlevel%

REM  Execute ChangeNamespace to get unmatched organizations into current namespace
REM  This is where the new time intervals from the harvest are given uris within the namespace of Vivo

@java %HARVESTER_JAVA_OPTS% -cp %CLASSPATH% org.vivoweb.harvester.qualify.ChangeNamespace -X changenamespace-orgs.config.xml
if %errorlevel% neq 0 exit /b %errorlevel%

REM  Execute ChangeNamespace to get unmatched locations into current namespace
REM  This is where the new locations from the harvest are given uris within the namespace of Vivo

@java %HARVESTER_JAVA_OPTS% -cp %CLASSPATH% org.vivoweb.harvester.qualify.ChangeNamespace -X changenamespace-location.config.xml
if %errorlevel% neq 0 exit /b %errorlevel%

REM  Execute ChangeNamespace to get unmatched contactInfo into current namespace
REM  This is where the new locations from the harvest are given uris within the namespace of Vivo

@java %HARVESTER_JAVA_OPTS% -cp %CLASSPATH% org.vivoweb.harvester.qualify.ChangeNamespace -X changenamespace-contact.config.xml
if %errorlevel% neq 0 exit /b %errorlevel%

REM  Perform an update
REM  The harvester maintains copies of previous harvests in order to perform the same harvest twice
REM    but only add the new statements, while removing the old statements that are no longer
REM    contained in the input data. This is done in several steps of finding the old statements,
REM    then the new statements, and then applying them to the Vivo main model.

REM  Find Subtractions
REM  When making the previous harvest model agree with the current harvest, the statements that exist in
REM 	the previous harvest but not in the current harvest need to be identified for removal.

@java %HARVESTER_JAVA_OPTS% -cp %CLASSPATH% org.vivoweb.harvester.diff.Diff -X diff-subtractions.config.xml
if %errorlevel% neq 0 exit /b %errorlevel%

REM  Find Additions
REM  When making the previous harvest model agree with the current harvest, the statements that exist in
REM 	the current harvest but not in the previous harvest need to be identified for addition.

@java %HARVESTER_JAVA_OPTS% -cp %CLASSPATH% org.vivoweb.harvester.diff.Diff -X diff-additions.config.xml
if %errorlevel% neq 0 exit /b %errorlevel%

REM  Apply Subtractions to Previous model

@java %HARVESTER_JAVA_OPTS% -cp %CLASSPATH% org.vivoweb.harvester.transfer.Transfer  -w INFO -o previous-harvest.model.xml -r data/vivo-subtractions.rdf.xml -m
if %errorlevel% neq 0 exit /b %errorlevel%

REM  Apply Additions to Previous model

@java %HARVESTER_JAVA_OPTS% -cp %CLASSPATH% org.vivoweb.harvester.transfer.Transfer  -w INFO -o previous-harvest.model.xml -r data/vivo-additions.rdf.xml
if %errorlevel% neq 0 exit /b %errorlevel%

REM  Now that the changes have been applied to the previous harvest and the harvested data in vivo
REM 	agree with the previous harvest, the changes are now applied to the vivo model.
REM  Apply Subtractions to VIVO model

@java %HARVESTER_JAVA_OPTS% -cp %CLASSPATH% org.vivoweb.harvester.transfer.Transfer  -w INFO -o vivo.model.xml -r data/vivo-subtractions.rdf.xml -m
if %errorlevel% neq 0 exit /b %errorlevel%

REM  Apply Additions to VIVO model

@java %HARVESTER_JAVA_OPTS% -cp %CLASSPATH% org.vivoweb.harvester.transfer.Transfer  -w INFO -o vivo.model.xml -r data/vivo-additions.rdf.xml
if %errorlevel% neq 0 exit /b %errorlevel%

REM Output some counts
REM PUBS=`cat data/vivo-additions.rdf.xml | grep pmid | wc -l`
rem EVENTS=`cat data/vivo-additions.rdf.xml | grep 'http://purl.org/NET/c4dm/event.owl#Event' | wc -l`
REM AUTHORSHIPS=`cat data/vivo-additions.rdf.xml | grep Authorship | wc -l`
rem echo "Imported $EVENTS events"

echo Harvest completed successfully
