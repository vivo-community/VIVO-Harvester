#!/bash

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
HARVESTER_INSTALL_DIR=/data/vivo/harvester/harvester_1.3
export HARVEST_NAME=example-courses
export DATE=`date +%Y-%m-%d'T'%T`

# Add harvester binaries to path for execution
# The tools within this script refer to binaries supplied within the harvester
#	Since they can be located in another directory their path should be
#	included within the classpath and the path environmental variables.
export PATH=$PATH:$HARVESTER_INSTALL_DIR/bin
export CLASSPATH=$CLASSPATH:$HARVESTER_INSTALL_DIR/bin/harvester.jar:$HARVESTER_INSTALL_DIR/bin/dependency/*
export CLASSPATH=$CLASSPATH:$HARVESTER_INSTALL_DIR/build/harvester.jar:$HARVESTER_INSTALL_DIR/build/dependency/*

# Exit on first error
# The -e flag prevents the script from continuing even though a tool fails.
#	Continuing after a tool failure is undesirable since the harvested
#	data could be rendered corrupted and incompatible.
set -e

# Supply the location of the detailed log file which is generated during the script.
#	If there is an issue with a harvest, this file proves invaluable in finding
#	a solution to the problem. It has become common practice in addressing a problem
#	to request this file. The passwords and usernames are filtered out of this file
#	To prevent these logs from containing sensitive information.
echo "Full Logging in $HARVEST_NAME.$DATE.log"
EMAIL_RECIPIENT=vsposato@ufl.edu

rm -f tmp.txt
if [ ! -d logs ]; then
  mkdir logs
fi
cd logs
touch $HARVEST_NAME.$DATE.log
ln -sf $HARVEST_NAME.$DATE.log $HARVEST_NAME.latest.log
cd ..

#clear old data
# For a fresh harvest, the removal of the previous information maintains data integrity.
#	If you are continuing a partial run or wish to use the old and already retrieved
#	data, you will want to comment out this line since it could prevent you from having
# 	the required harvest data.  
rm -rf data
rm -rf analytics.txt
touch analytics.txt
rm -rf courselogfile.txt
touch courselogfile.txt
#if [ -d data ]; then
#	mv -f data data.$DATE
#fi

#Follwing bash one liner is to padd the Ufid and section number with leading zeros, ie to make it a 8 digit & 4 digit number respt.


# Set the input delimeter /output delimeter to be ','. For every line in coursevivo_course.csv set if the column number 6 doest not start with a '"' pad the id value with leading zero to make it a 8 digit number
#BEGIN {FS=OFS=","} ->> setting delimeter
#if( $6 !~ /^"/&& NR >1) ->> doest not begin with '"' and Skip the header

#$4=sprintf("%04d", $4) Section number ->pad with missing zero to make it 8 digit
#sub($4,"\""$4"\"")- >> Section Number ->add '"' around it

#$6=sprintf("%08d", $6) Ufid- > pad with missing zero to make it 8 digit
#sub($6,"\""$6"\"")- >> Ufid -> add '"' around it

#print  to the wellformated.csv file
#else print to wellformated without any changes 


cat  course-input/coursevivo_course.csv  | awk 'BEGIN {FS=OFS=","}{ if( $4 !~ /^"/ && NR >1) {$2=sprintf("%04d", $2); sub($2,"\""$2"\""); $4=sprintf("%08d", $4);sub($4,"\""$4"\""); print }else print;}' >  course-input/wellformated.csv 

#move course-input/wellformated.csv to course-input/coursevivo_course.csv
mv course-input/wellformated.csv  course-input/coursevivo_course.csv


# Import CSV
# Takes the data from a comma-separated-values file and places it in a relational database.  Then
#   JDBCFetch, in the next step, can use this to generate XML.
harvester-csvtojdbc -X csvtojdbc.config.xml

# Execute Fetch
# This stage of the script is where the information is gathered together into one local
#	place to facilitate the further steps of the harvest. The data is stored locally
#	in a format based off of the source. The format is a form of RDF but not in the VIVO ontology
# The JDBCFetch tool in particular takes the data from the chosen source described in its
#	configuration XML file and places it into record set in the flat RDF directly 
#	related to the rows, columns and tables described in the target database.
harvester-jdbcfetch -X jdbcfetch.config.xml

# Execute Translate
# This is the part of the script where the outside data, in its flat RDF form is used to
#	create the more linked and descriptive form related to the ontological constructs.
#	The traditional XSL language is used to achieve this part of the work-flow.
harvester-xsltranslator -X xsltranslator.config.xml

echo "Running Pre Course Ingest Analytics......."
bash analytics.sh

# Execute Transfer to import from record handler into local temp model
# From this stage on the script places the data into a Jena model. A model is a
#       data storage structure similar to a database, but is in RDF.
# The harvester tool Transfer is used to move/add/remove/dump data in models.
# For this call on the transfer tool:
# -s refers to the source translated records file, which was just produced by the translator step
# -o refers to the destination model for harvested data
# -d means that this call will also produce a text dump file in the specified location 
harvester-transfer -s translated-records.config.xml -o harvested-data.model.xml -d data/harvested-data/imported-records.rdf.xml

# Execute Score for Courses
# In the scoring phase the data in the harvest is compared to the data within Vivo and a new model
#       is created with the values / scores of the data comparisons. 
harvester-score -X score-courses.config.xml

# Execute Score for Course Section
# In the scoring phase the data in the harvest is compared to the data within Vivo and a new model
#       is created with the values / scores of the data comparisons. 
harvester-score -X score-coursesection.config.xml

# Execute Score for People
# In the scoring phase the data in the harvest is compared to the data within Vivo and a new model
# 	is created with the values / scores of the data comparisons. 
harvester-score -X score-people.config.xml

# Execute Score for Academic Term
# In the scoring phase the data in the harvest is compared to the data within Vivo and a new model
# 	is created with the values / scores of the data comparisons. 
harvester-score -X score-academicTerm.config.xml

# Execute Score for Teacher Role
# In the scoring phase the data in the harvest is compared to the data within Vivo and a new model
# 	is created with the values / scores of the data comparisons. 
harvester-score -X score-teacherRole.config.xml

# Execute Score for Term Start
# In the scoring phase the data in the harvest is compared to the data within Vivo and a new model
# 	is created with the values / scores of the data comparisons. 
harvester-score -X score-termStart.config.xml


# Find matches using scores and rename nodes to matching uri
# Using the data model created by the score phase, the match process changes the harvested uris for
#       comparison values above the chosen threshold within the xml configuration file.
# This config differs from the previous match config, in that it removes types and literals from the 
#       resources in the incoming model for those that are considered a match.
harvester-match -X match-roles.config.xml

# Smush to remove duplicates
# Using a particular predicate as an identifying data element the smush tool will rename those
#       resources which have matching values of that predicate to be one resource.

# Smush to remove duplicates
# Using a particular predicate as an identifying data element the smush tool will rename those
#       resources which have matching values of that predicate to be one resource.
harvester-smush -X smush-person.config.xml

harvester-smush -X smush-course.config.xml

harvester-smush -X smush-coursesection.config.xml

harvester-smush -X smush-academicTerm.config.xml

harvester-smush -X smush-teacherRole.config.xml

harvester-smush -X smush-termStart.config.xml

# Execute ChangeNamespace to get unmatched People into current name-space
# This is where the new people from the harvest are given uris within the name-space of Vivo
# 	If there is an issue with uris being in another name-space, this is the phase
#	which should give some light to the problem.
harvester-changenamespace -X changenamespace-people.config.xml

# Execute ChangeNamespace to get unmatched course into current name-space
# This is where the new course from the harvest are given uris within the name-space of Vivo
# 	If there is an issue with uris being in another name-space, this is the phase
#	which should give some light to the problem.
harvester-changenamespace -X changenamespace-course.config.xml

# Execute ChangeNamespace to get unmatched course section into current name-space
# This is where the new course section from the harvest are given uris within the name-space of Vivo
# 	If there is an issue with uris being in another name-space, this is the phase
#	which should give some light to the problem.
harvester-changenamespace -X changenamespace-coursesection.config.xml

# Execute ChangeNamespace to get unmatched Academic Term into current name-space
# This is where the new course section from the harvest are given uris within the name-space of Vivo
# 	If there is an issue with uris being in another name-space, this is the phase
#	which should give some light to the problem.
harvester-changenamespace -X changenamespace-academicterm.config.xml

# Execute ChangeNamespace to get unmatched Teacher Role into current name-space
# This is where the new course section from the harvest are given uris within the name-space of Vivo
# 	If there is an issue with uris being in another name-space, this is the phase
#	which should give some light to the problem.
harvester-changenamespace -X changenamespace-teacherRole.config.xml

# Execute ChangeNamespace to get unmatched Term Start into current name-space
# This is where the new course section from the harvest are given uris within the name-space of Vivo
# 	If there is an issue with uris being in another name-space, this is the phase
#	which should give some light to the problem.
harvester-changenamespace -X changenamespace-termStart.config.xml

# Find Subtractions
# When making the previous harvest model agree with the current harvest, the entries that exist in
#       the previous harvest but not in the current harvest need to be identified for removal.
#harvester-diff -X diff-subtractions.config.xml

# Find Additions
# When making the previous harvest model agree with the current harvest, the entries that exist in
#       the current harvest but not in the previous harvest need to be identified for addition.
harvester-diff -X diff-additions.config.xml

# Apply Subtractions to Previous model
#harvester-transfer -o previous-harvest.model.xml -r data/vivo-subtractions.rdf.xml -m
# Apply Additions to Previous model
harvester-transfer -o previous-harvest.model.xml -r data/vivo-additions.rdf.xml

# Apply Subtractions to Previous model
#harvester-transfer -o vivo.model.xml -r data/vivo-subtractions.rdf.xml -m
# Apply Additions to VIVO for pre-1.2 versions
harvester-transfer -o vivo.model.xml -r data/vivo-additions.rdf.xml

echo "Pre Course Ingest Analytics" &>> courselogfile.txt
echo "================================================================================="  &>> courselogfile.txt
cat analytics.txt &>> courselogfile.txt
echo -e "\n" &>>courselogfile.txt

echo "Running Post Course Ingest Analytics......."
bash analytics.sh
echo "Post Course Ingest Analytics" &>> courselogfile.txt
echo "================================================================================="  &>> courselogfile.txt
cat analytics.txt &>> courselogfile.txt
echo -e "\n" &>> courselogfile.txt


echo `get_datetime` "End Course Run"

#Assemble the log files to be emailed
cat courselogfile.txt > tmp.txt
##echo "Full Log">>tmp.txt

echo -e "\n"  >> tmp.txt
echo "Ending full log" >> tmp.txt
#Mail the assembled log file to the desired person.

mail -a "FROM:Course_Ingest" -s "Course Ingest harvest of $DATE" "$EMAIL_RECIPIENT" < tmp.txt

echo 'Harvest courses completed successfully'

