# Copyright (c) 2010 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the new BSD license
# which accompanies this distribution, and is available at
# http://www.opensource.org/licenses/bsd-license.html
#
# Contributors:
#     Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams - initial API and implementation
#	  James Pence

#author: nskaggs@ctrip.ufl.edu

#WARNING: this code is fragile and not robust.
#No attempts to sanitize or rationalize input have been made

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
	#tar -xzf ../bin/harvester-$RELEASENAME.tar.gz
	#dpkg -i ../bin/harvester-$RELEASENAME.deb

	#check for failure
	#if [ "$?" = "1" ]; then
	#	echo "Exiting - dpkg install failure:" $?
	#	exit
	#fi

	cd ../release
else
	echo -n "Enter release version: "
	read RELEASENAME

	#delete old version
	rm harvester*.tar.gz
	rm harvester*.deb
	rm -rf usr

	#pull release version from sf
	wget -O harvester-$RELEASENAME.tar.gz http://sourceforge.net/projects/vivo/files/VIVO%20Harvester/harvester-$RELEASENAME.tar.gz/download
	wget -O harvester-$RELEASENAME.deb http://sourceforge.net/projects/vivo/files/VIVO%20Harvester/harvester-$RELEASENAME.deb/download

	#unpack tarball, install deb
	#unpack debian package
	#ar -x harvester*.deb

	#rename tarball
	tar -xzf harvester-$RELEASENAME.tar.gz
	#tar -xzf ../bin/harvester-$RELEASENAME.tar.gz
	#dpkg -i ../bin/harvester-$RELEASENAME.deb

	#check for failure
	#if [ "$?" = "1" ]; then
	#	echo "Exiting - dpkg install failure:" $?
	#	exit
	#fi
fi

#create h2 vivo db, and load it with stuff
#use h2 vivo db with stuff

#test tarball
mv usr/share/vivo/harvester/config/jenaModels/VIVO.xml usr/share/vivo/harvester/config/jenaModels/VIVO.xml.bck
cp VIVO.xml usr/share/vivo/harvester/config/jenaModels/VIVO.xml
cp vivotest.h2.db usr/share/vivo/harvester

#modify configs?
#config/tasks/PubmedFetch.xml

#run example scripts as shipped
cd usr/share/vivo/harvester
bash scripts/runPubmed.sh

#check for failure
if [ "$?" = "1" ]; then
	echo "Exiting - tarball pubmed example failure:" $?
	exit
fi

#test deb
#mv /usr/share/vivo/harvester/config/jenaModels/VIVO.xml /usr/share/vivo/harvester/config/jenaModels/VIVO.xml.bck
#cp VIVO.xml /usr/share/vivo/harvester/config/jenaModels/VIVO.xml
#cp vivotest.h2.db /usr/share/vivo/harvester

#modify configs?
#config/tasks/PubmedFetch.xml

#run example scripts as shipped
#cd /usr/share/vivo/harvester
#bash scripts/runPubmed.sh

#check for failure
#if [ "$?" = "1" ]; then
#	echo "Exiting - dpkg pubmed example failure:" $?
#	exit
#fi
