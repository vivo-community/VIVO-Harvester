#!/bin/bash

#Copyright (c) 2010-2011 VIVO Harvester Team. For full list of contributors, please see the AUTHORS file provided.
#All rights reserved.
#This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
# AUTHORS Vincent Sposato

# Target Directory To Review
export TARGET_DIR=/data/vivo/harvester/vivo-auto-harvest/peoplesoft/peoplesoft-ingest/data/renamed-records
export INGEST_DIR=/data/vivo/harvester/vivo-auto-harvest/peoplesoft/peoplesoft-ingest
export CURRENT_DIR=`pwd`

echo $CURRENT_DIR

# Email Address to Send List
export EMAIL_RECIPIENT="*****@***.edu"

# Current Date & Time
export DATE=`date +%Y-%m-%d'T'%T`

# Change directory to harvest directory
cd $INGEST_DIR

for file in `find $TARGET_DIR -type f`
do
	{	

		# Create a subject line
		export SUBJECT="PEOPLE INGEST - Rename Person Received - $DATE"

		# Send the email
		mail -a "FROM:PeopleSoft_Ingest" -s "$SUBJECT" "$EMAIL_RECIPIENT" < $file

		# Remove the file
		rm -rf $file
	}
done

cd $CURRENT_DIR
