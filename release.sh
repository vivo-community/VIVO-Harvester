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
#This tool requires you to have your ssh key on the sourceforge server

echo -n "Enter sourceforge username: "
read NAME

#ask for version name
echo -n "Enter release version: "
read RELEASENAME

#update pom.xml and deb control file with new version
sed 's_<version>.*</version>_<version>'$RELEASENAME'</version>_' <pom.xml>pomtmp.xml
mv pomtmp.xml pom.xml

sed 's_Version: .*_Version: '$RELEASENAME'_' <src/deb/control/control>src/deb/control/controltmp
mv src/deb/control/controltmp src/deb/control/control

#update changelog

#check out from dev svn

#merge down to staging

#merge down to trunk

#tag inside trunk

#check out from trunk svn
svn co https://vivo.svn.sourceforge.net/svnroot/vivo/Harvester/trunk

#build
cd trunk
mvn clean dependency:copy-dependencies package

#check for fail
if [ "$?" -eq "1" ]; then
	echo "Exiting - Maven failure:" $?
	exit
fi

cd bin

#unpack debian package
ar -x $RELEASENAME.deb

#rename tarball
mv data.tar.gz $RELEASENAME.tar.gz

#Upload tarball and deb package to sourceforge
scp $RELEASENAME.deb $NAME,vivo@frs.sourceforge.net:"/home/frs/project/v/vi/vivo/VIVO\ Harvester"
scp $RELEASENAME.tar.gz $NAME,vivo@frs.sourceforge.net:"/home/frs/project/v/vi/vivo/VIVO\ Harvester"

#merge back to dev