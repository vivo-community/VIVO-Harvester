# Copyright (c) 2010 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the new BSD license
# which accompanies this distribution, and is available at
# http://www.opensource.org/licenses/bsd-license.html
#

#author: nskaggs@ctrip.ufl.edu
#This will run a predetermined harvest against pubmed into a known set of data
#For now, it is meant as a QA script for the harvester

#WARNING: this code is fragile and not robust.
#No attempts to sanitize or rationalize input have been made

# Set working directory
DIR=$(cd "$(dirname "$0")"; pwd)
cd $DIR

echo $DIR

rm -rf usr
rm *.log
rm vivo_start.rdf
rm vivo_end.rdf
rm pubmed.rdf
rm match.rdf
rm qualify.rdf

echo -n "Grab local?: "
read LOCAL

if [ "$LOCAL" = "y" ]; then
	#build it and extract tarball
	cd ..
	mvn clean dependency:copy-dependencies package -DskipTests=true
	#check for failure
	if [ "$?" = "1" ]; then
		echo "Exiting - mvn install failure:" $?
		exit
	fi

	cd bin
	ar -x harvester*.deb

	#rename tarball
	tar -xzf data.tar.gz -C ../release

	cd ../release
else
	echo -n "Enter release version: "
	read RELEASENAME

	#delete old version
	rm harvester*.tar.gz
	rm harvester*.deb

	#pull release version from sf
	wget -O harvester-$RELEASENAME.tar.gz http://sourceforge.net/projects/vivo/files/VIVO%20Harvester/harvester-$RELEASENAME.tar.gz/download

	#rename tarball
	tar -xzf harvester-$RELEASENAME.tar.gz
fi

#test tarball
mv usr/share/vivo/harvester/config/models/vivo.xml usr/share/vivo/harvester/config/models/vivo.xml.bck
cp vivo.xml usr/share/vivo/harvester/config/models/vivo.xml
cp vivotest.h2.db usr/share/vivo/harvester
cp run-pubmed.sh usr/share/vivo/harvester/scripts
cp example.pubmedfetch.xml usr/share/vivo/harvester/config/tasks/example.pubmedfetch.xml

#run example scripts
cd usr/share/vivo/harvester
bash scripts/run-pubmed.sh

#check for failure
if [ "$?" = "1" ]; then
	echo "TEST FAILED DURING run-pubmed.sh" $?
fi

#copy out the log and remove
cp logs/* ../../../..
cp vivo_start.rdf ../../../..
cp vivo_end.rdf ../../../..
cp pubmed.rdf ../../../..
cp score.rdf ../../../..
cp match.rdf ../../../..
cp qualify.rdf ../../../..

#check for data written to vivo, otherwise failure
#PRESIZE=`du -b vivo_start.rdf | awk '{ print $1 }'`
#POSTSIZE=`du -b vivo_end.rdf | awk '{ print $1 }'`

#if [[ $POSTSIZE > $PRESIZE ]]
grep Authorship vivo_end.rdf
if [ $? -eq 0 ]
then
  echo "SUCCESS -- Test harvest completed"
else
  echo "FAILED -- publication didn't get into vivo"
fi
