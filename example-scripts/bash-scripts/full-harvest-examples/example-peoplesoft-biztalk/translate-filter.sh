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

SOURCE_TRANSLATED_DIR=/data/vivo/harvester/harvester_1.3/example-scripts/example-peoplesoft-biztalk/data/translated-records
DESTINATION_IGNORED_REC=/data/vivo/harvester/harvester_1.3/example-scripts/example-peoplesoft-biztalk/ignored-records/

if [ ! -d $DESTINATION_IGNORED_REC ]; then
  mkdir $DESTINATION_IGNORED_REC
fi

echo $DESTINATION__IGNORED_REC
echo "source dir"$SOURCE_TRANSLATED_DIR
for fn in $SOURCE_TRANSLATED_DIR/*; do

	echo "-------------" $fn
        action=`grep -o '^<Ignore />$' $fn `
	echo $action

	if [ "$action" == "" ] 
		then
		  mv $fn $DESTINATION_IGNORED_REC
	fi

done
#Done for for loop

