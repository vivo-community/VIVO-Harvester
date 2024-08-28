@echo off

IF exist data (
  rmdir /s /q data
)

IF exist logs (
    rmdir /s /q logs
)

REM  set to the directory where the harvester was installed or unpacked
REM  HARVESTER_INSTALL_DIR is set to the location of the installed harvester
REM 	If the deb file was used to install the harvester then the
REM 	directory should be set to /usr/share/vivo/harvester which is the
REM 	current location associated with the deb installation.
REM 	Since it is also possible the harvester was installed by
REM 	uncompressing the tar.gz the setting is available to be changed
REM 	and should agree with the installation location
set HARVESTER_INSTALL_DIR=C:\Users\KampeB\Dev\Harvester
set HARVEST_NAME=OpenAlex-Fetch
FOR %%A IN (%Date:/=%) DO SET Today=%%A

REM  set the CLASSPATH and HARVESTER_JAVA_OPTS to be used by all commands
set CLASSPATH=%HARVESTER_INSTALL_DIR%/build/harvester.jar;%HARVESTER_INSTALL_DIR%/build/dependency/*
set HARVESTER_JAVA_OPTS=-Xms1024M -Xmx2048M

REM  Execute Fetch
REM  This stage of the script is where the information is gathered together into one local
REM 	place to facilitate the further steps of the harvest. The data is stored locally
REM 	in a format based off of the source. The format is a form of RDF but not in the VIVO ontology
echo Fetch from OpenAlex
@java %HARVESTER_JAVA_OPTS% -cp %CLASSPATH% org.vivoweb.harvester.fetch.JSONFetch -X openalexfetch.config.xml
 if %errorlevel% neq 0 exit /b %errorlevel%

REM  Execute Translate
REM  This is the part of the script where the input data is transformed into valid RDF
REM    Translate will apply an xslt file to the fetched data which will result in the data
REM    becoming valid RDF in the VIVO ontology
echo Translate data to valid RDF
@java %HARVESTER_JAVA_OPTS% -cp %CLASSPATH% org.vivoweb.harvester.translate.XSLTranslator -X xsltranslator.config.xml
 if %errorlevel% neq 0 exit /b %errorlevel%

REM  Execute Transfer to import from record handler into local temp model
REM  From this stage on the script places the data into a Jena model. A model is a
REM 	data storage structure similar to a database, but in RDF.
REM  The harvester tool Transfer is used to move/add/remove/dump data in models.
echo Transfer RDF into temporary triple store
@java %HARVESTER_JAVA_OPTS% -cp %CLASSPATH% org.vivoweb.harvester.transfer.Transfer -X transfer.config.xml
 if %errorlevel% neq 0 exit /b %errorlevel%

REM  Perform an update
REM  The harvester maintains copies of previous harvests in order to perform the same harvest twice
REM    but only add the new statements, while removing the old statements that are no longer
REM    contained in the input data. This is done in several steps of finding the old statements,
REM    then the new statements, and then applying them to the Vivo main model.

REM  Find Subtractions
REM  When making the previous harvest model agree with the current harvest, the statements that exist in
REM 	the previous harvest but not in the current harvest need to be identified for removal.
echo Find Subtractions
@java %HARVESTER_JAVA_OPTS% -cp %CLASSPATH% org.vivoweb.harvester.diff.Diff -X diff-subtractions.config.xml
 if %errorlevel% neq 0 exit /b %errorlevel%

REM  Find Additions
REM  When making the previous harvest model agree with the current harvest, the statements that exist in
REM 	the current harvest but not in the previous harvest need to be identified for addition.
echo Find Additions
@java %HARVESTER_JAVA_OPTS% -cp %CLASSPATH% org.vivoweb.harvester.diff.Diff -X diff-additions.config.xml
 if %errorlevel% neq 0 exit /b %errorlevel%

REM  Apply Subtractions to Previous model
echo Apply Subtractions to Previous model
@java %HARVESTER_JAVA_OPTS% -cp %CLASSPATH% org.vivoweb.harvester.transfer.Transfer  -w INFO -o previous-harvest.model.xml -r data/vivo-subtractions.rdf.xml -m
 if %errorlevel% neq 0 exit /b %errorlevel%

REM  Apply Additions to Previous model
echo Apply Additions to Previous model
@java %HARVESTER_JAVA_OPTS% -cp %CLASSPATH% org.vivoweb.harvester.transfer.Transfer  -w INFO -o previous-harvest.model.xml -r data/vivo-additions.rdf.xml
 if %errorlevel% neq 0 exit /b %errorlevel%

REM  Now that the changes have been applied to the previous harvest and the harvested data in vivo
REM 	agree with the previous harvest, the changes are now applied to the vivo model.
REM  Apply Subtractions to VIVO model
echo Apply Subtractions to VIVO model
@java %HARVESTER_JAVA_OPTS% -cp %CLASSPATH% org.vivoweb.harvester.transfer.Transfer  -w INFO -o vivo.model.xml -r data/vivo-subtractions.rdf.xml -m
 if %errorlevel% neq 0 exit /b %errorlevel%

REM  Apply Additions to VIVO model
echo Apply Additions to VIVO model
@java %HARVESTER_JAVA_OPTS% -cp %CLASSPATH% org.vivoweb.harvester.transfer.Transfer  -w INFO -o vivo.model.xml -r data/vivo-additions.rdf.xml
 if %errorlevel% neq 0 exit /b %errorlevel%

echo Harvest completed successfully
