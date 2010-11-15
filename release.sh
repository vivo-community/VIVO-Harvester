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
#No attempts to sanitize or rationalize input have been made

echo -n "Enter sourceforge username: "
read NAME

echo -n "Build from? (dev, trunk, staging | blank for local): "
read CODELOC

echo -n "Enter release version: "
read RELEASENAME

echo -n "Run junit tests before build?: "
read RUNTEST

#update changelog, exit if not been updated
echo -n "Has changelog been updated?: "
read CHANGELOG

if [ "$CHANGELOG" != "y" ]; then
	echo -n "Please update CHANGELOG and re-run"
	exit
fi 

#get code
if [ "$CODELOC" = "dev" ]; then
	svn co https://vivo.svn.sourceforge.net/svnroot/vivo/Harvester/trunk/branches/Development
	cd Development
	#if we're pulling from dev, ask if releasing stable 
	echo -n "Build a stable release? (merge down, tag and release from trunk): "
	read BUILDSTABLE
elif [ "$CODELOC" = "Staging" ]; then
	svn co https://vivo.svn.sourceforge.net/svnroot/vivo/Harvester/branches/Staging
	cd Staging
	#if we're pulling from Staging, ask if releasing point 
	echo -n "Build a point release? (merge down, tag and release from trunk): "
	read BUILDPOINT
elif [ "$CODELOC" = "trunk" ]; then
	svn co https://vivo.svn.sourceforge.net/svnroot/vivo/Harvester/trunk
	cd trunk
fi

#update pom.xml and deb control file with new version
sed 's_<version>.*</version>_<version>'$RELEASENAME'</version>_' <pom.xml>pomtmp.xml
mv pomtmp.xml pom.xml

sed 's_Version: .*_Version: '$RELEASENAME'_' <src/deb/control/control>src/deb/control/controltmp
mv src/deb/control/controltmp src/deb/control/control

#build
if [ "$RUNTEST" = "y" ]; then
	mvn clean dependency:copy-dependencies package
else
	mvn clean dependency:copy-dependencies package -DskipTests=true
fi

#check for failure
if [ "$?" -eq "1" ]; then
	echo "Exiting - Maven failure:" $?
	exit
fi

#unpack debian package
cd bin
ar -x $RELEASENAME.deb

#rename tarball
mv data.tar.gz $RELEASENAME.tar.gz

#Upload tarball and deb package to sourceforge
scp $RELEASENAME.deb $NAME,vivo@frs.sourceforge.net:"/home/frs/project/v/vi/vivo/VIVO\ Harvester"
scp $RELEASENAME.tar.gz $NAME,vivo@frs.sourceforge.net:"/home/frs/project/v/vi/vivo/VIVO\ Harvester"


if [ "$BUILDSTABLE" = "y" ]; then
	#merge down to Staging
	
	#merge down to trunk

	#tag inside trunk
elif [ "$BUILDPOINT" = "y" ]; then
	#merge down to trunk

	#tag inside trunk
fi