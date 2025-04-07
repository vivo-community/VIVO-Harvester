@echo off
REM ============================
REM HARVESTER SCRIPT
REM ============================

REM Clean up previous data and logs
IF exist data (
  echo Removing 'data' directory...
  rmdir /s /q data
)

IF exist logs (
  echo Removing 'logs' directory...
  rmdir /s /q logs
)

REM Load method selection (default: sparql)
set LOAD_METHOD=sparql

if not "%~1"=="" (
    if /I "%~1"=="tdb" (
        set LOAD_METHOD=tdb
    ) else if /I "%~1"=="sparql" (
        set LOAD_METHOD=sparql
    ) else (
        echo Invalid argument: %~1
        echo Usage: %~nx0 [tdb|sparql]
        exit /b 1
    )
)

REM Set installation directory
REM Replace this with the actual installation path
set "HARVESTER_INSTALL_DIR=%cd%\..\..\..\..\..\..\VIVO-Harvester"

REM Validate HARVESTER_INSTALL_DIR
IF NOT EXIST "%HARVESTER_INSTALL_DIR%" (
    echo ERROR: HARVESTER_INSTALL_DIR does not exist: %HARVESTER_INSTALL_DIR%
    exit /b 1
)

REM Set today's date
FOR /F "tokens=2-4 delims=/ " %%A IN ('date /t') DO set Today=%%A%%B%%C

REM Classpath setup
set CLASSPATH=%HARVESTER_INSTALL_DIR%\build\harvester.jar;%HARVESTER_INSTALL_DIR%\build\dependency\*

REM Java options
set HARVESTER_JAVA_OPTS=-Xms1024M -Xmx2048M

REM ============================
REM Fetch Stage
REM ============================
echo Fetching from DSpace...
IF NOT EXIST dspace-oaifetch.conf.xml (
    echo ERROR: Missing configuration file: dspace-oaifetch.conf.xml
    exit /b 1
)
java %HARVESTER_JAVA_OPTS% -cp "%CLASSPATH%" org.vivoweb.harvester.fetch.OAIFetch -X dspace-oaifetch.conf.xml
if %errorlevel% neq 0 exit /b %errorlevel%

REM ============================
REM Translate Stage
REM ============================
echo Translating data to valid RDF...
IF NOT EXIST xsltranslator.config.xml (
    echo ERROR: Missing configuration file: xsltranslator.config.xml
    exit /b 1
)
java %HARVESTER_JAVA_OPTS% -cp "%CLASSPATH%" org.vivoweb.harvester.translate.XSLTranslator -X xsltranslator.config.xml
if %errorlevel% neq 0 exit /b %errorlevel%

REM ============================
REM Transfer Stage
REM ============================
echo Transferring RDF into temporary triple store...
java %HARVESTER_JAVA_OPTS% -cp "%CLASSPATH%" org.vivoweb.harvester.transfer.Transfer -w INFO -s translated-records.config.xml -o harvested-data.model.xml -d data/harvested-data/imported-records.rdf.xml
if %errorlevel% neq 0 exit /b %errorlevel%

REM ============================
REM Diff Stage
REM ============================
echo Finding Subtractions...
java %HARVESTER_JAVA_OPTS% -cp "%CLASSPATH%" org.vivoweb.harvester.diff.Diff -X diff-subtractions.config.xml
if %errorlevel% neq 0 exit /b %errorlevel%

echo Finding Additions...
java %HARVESTER_JAVA_OPTS% -cp "%CLASSPATH%" org.vivoweb.harvester.diff.Diff -X diff-additions.config.xml
if %errorlevel% neq 0 exit /b %errorlevel%

REM ============================
REM Apply Changes
REM ============================
echo Applying Subtractions to Previous model...
java %HARVESTER_JAVA_OPTS% -cp "%CLASSPATH%" org.vivoweb.harvester.transfer.Transfer -w INFO -o previous-harvest.model.xml -r data/vivo-subtractions.rdf.xml -m
if %errorlevel% neq 0 exit /b %errorlevel%

echo Applying Additions to Previous model...
java %HARVESTER_JAVA_OPTS% -cp "%CLASSPATH%" org.vivoweb.harvester.transfer.Transfer -w INFO -o previous-harvest.model.xml -r data/vivo-additions.rdf.xml
if %errorlevel% neq 0 exit /b %errorlevel%

REM Apply to VIVO model
if "%LOAD_METHOD%"=="tdb" (
    echo Applying Subtractions to VIVO model...
    java %HARVESTER_JAVA_OPTS% -cp "%CLASSPATH%" org.vivoweb.harvester.transfer.Transfer -w INFO -o vivo.model.xml -r data/vivo-subtractions.rdf.xml -m
    if %errorlevel% neq 0 exit /b %errorlevel%

    echo Applying Additions to VIVO model...
    java %HARVESTER_JAVA_OPTS% -cp "%CLASSPATH%" org.vivoweb.harvester.transfer.Transfer -w INFO -o vivo.model.xml -r data/vivo-additions.rdf.xml
    if %errorlevel% neq 0 exit /b %errorlevel%
) else (
    echo Applying changes using SPARQL update...
    java %HARVESTER_JAVA_OPTS% -cp "%CLASSPATH%" org.vivoweb.harvester.services.SparqlUpdate -X sparqlupdate.conf.xml
)

echo Harvest completed successfully.
exit /b 0
