#!/bin/bash

# Copyright (c) 2010-2011 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence, Michael Barbieri.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the new BSD license
# which accompanies this distribution, and is available at
# http://www.opensource.org/licenses/bsd-license.html
# 
# Contributors:
#     Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence, Michael Barbieri - initial API and implementation

# set to the directory where the harvester was installed or unpacked
# HARVESTER_INSTALL_DIR is set to the location of the installed harvester
#	If the deb file was used to install the harvester then the
#	directory should be set to /usr/share/vivo/harvester which is the
#	current location associated with the deb installation.
#	Since it is also possible the harvester was installed by
#	uncompressing the tar.gz the setting is available to be changed
#	and should agree with the installation location
HARVESTER_INSTALL_DIR=/home/mbarbieri/workspace/HarvesterDev
#HARVESTER_INSTALL_DIR=/usr/share/vivo/harvester
HARVEST_NAME=example-mods
DATE=`date +%Y-%m-%d'T'%T`

# Add harvester binaries to path for execution
# The tools within this script refer to binaries supplied within the harvester
#	Since they can be located in another directory their path should be
#	included within the classpath and the path environment variables.
PATH=$PATH:$HARVESTER_INSTALL_DIR/bin
CLASSPATH=$CLASSPATH:$HARVESTER_INSTALL_DIR/bin/harvester-1.1.1.jar:$HARVESTER_INSTALL_DIR/bin/dependency/*

# Exit on first error
# The -e flag prevents the script from continuing even though a tool fails.
#	Continuing after a tool failure is undesirable since the harvested
#	data could be rendered corrupted and incompatible.
set -e

# Supply the location of the detailed log file which is generated during the script.
#	If there is an issue with a harvest, this file proves invaluable in finding
#	a solution to the problem. It has become common practice in addressing a problem
#	to request this file. The passwords and usernames are filtered out of this file
#	to prevent these logs from containing sensitive information.
echo "Full Logging in pubmed-harvest-$DATE.log"

#clear old data
# For a fresh harvest, the removal of the previous information maintains data integrity.
#	If you are continuing a partial run or wish to use the old and already retrieved
#	data, you will want to comment out this line since it could prevent you from having
# 	the required harvest data.  
rm -rf data



harvester-runbibutils -X runbibutils.conf.xml

harvester-sanitizemodsxml -X sanitizemodsxml.conf.xml

harvester-xsltranslator -X xsltranslator.conf.xml

harvester-transfer -X transfer-records-to-model.conf.xml

harvester-score -X score-pub.conf.xml
harvester-match -X match-pub.conf.xml

harvester-score -X score-author.conf.xml
harvester-score -X score-org.conf.xml
harvester-score -X score-geo.conf.xml
harvester-score -X score-journal.conf.xml
harvester-score -X score-hyperlink.conf.xml
harvester-score -X score-interval.conf.xml
harvester-score -X score-datetime.conf.xml
harvester-match -X match-main.conf.xml

harvester-score -X score-authorship.conf.xml
harvester-match -X match-authorship.conf.xml

harvester-qualify -X qualify.conf.xml

harvester-changenamespace -X changenamespace-pub.conf.xml
harvester-changenamespace -X changenamespace-authorship.conf.xml
harvester-changenamespace -X changenamespace-author.conf.xml
harvester-changenamespace -X changenamespace-org.conf.xml
harvester-changenamespace -X changenamespace-geo.conf.xml
harvester-changenamespace -X changenamespace-journal.conf.xml
harvester-changenamespace -X changenamespace-hyperlink.conf.xml
harvester-changenamespace -X changenamespace-interval.conf.xml
harvester-changenamespace -X changenamespace-datetime.conf.xml

harvester-diff -X diff-subtractions.conf.xml
harvester-diff -X diff-additions.conf.xml

harvester-transfer -X transfer-subtractions-to-prevharv.conf.xml
harvester-transfer -X transfer-additions-to-prevharv.conf.xml
harvester-transfer -X transfer-subtractions-to-vivo.conf.xml
harvester-transfer -X transfer-additions-to-vivo.conf.xml

echo 'Harvest completed successfully'

