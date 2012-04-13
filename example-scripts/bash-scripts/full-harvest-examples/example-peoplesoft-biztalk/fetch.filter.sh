#!/bin/bash

# Copyright (c) 2010-2011 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence, Michael Barbieri.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the new BSD license
# which accompanies this distribution, and is available at
# http://www.opensource.org/licenses/bsd-license.html
# 
# Contributors:
#     Mayank Saini

# set to the directory where the harvester was installed or unpacked
# HARVESTER_INSTALL_DIR is set to the location of the installed harvester
#       If the deb file was used to install the harvester then the
#       directory should be set to /usr/share/vivo/harvester which is the
#       current location associated with the deb installation.
#       Since it is also possible the harvester was installed by
#       uncompressing the tar.gz the setting is available to be changed
#       and should agree with the installation location
export HARVESTER_INSTALL_DIR=/data/vivo/harvester/harvester_1.3/example-scripts/
export HARVEST_NAME=example-peoplesoft-biztalk
export DATE=`date +%Y-%m-%d'T'%T`


SOURCE_DIR=/home/vitro/newdata
DESTINATION_RAW_REC=/data/vivo/harvester/harvester_1.3/example-scripts/example-peoplesoft-biztalk/data/raw-records
DESTINATION_RENAME_REC=/data/vivo/harvester/harvester_1.3/example-scripts/example-peoplesoft-biztalk/rename-records/
if [ ! -d $DESTINATION_RENAME_REC ]; then
  mkdir $DESTINATION_RENAME_REC
fi

echo $DESTINATION_RENAME_REC
echo "source dir"$SOURCE_DIR
for fn in $SOURCE_DIR/*; do

	echo $fn
        action=`grep -o '^<ACTION>[A-Z]*</ACTION>$' $fn `
	echo $action

	if [ "$action" == "<ACTION>RENAME</ACTION>" ] 
		then
		  cp $fn $DESTINATION_RENAME_REC/
		else
		  cp $fn $DESTINATION_RAW_REC/
	fi

done
#Done for for loop

