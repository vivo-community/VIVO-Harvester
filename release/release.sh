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

cd ..

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
	echo "Please update CHANGELOG and re-run"
	exit
fi 

echo -n "Commit script version file changes?: "
read COMMIT

echo -n "Upload files to sourceforge?: "
read UPLOAD

#get code
if [ "$CODELOC" = "dev" ]; then
	svn co https://vivo.svn.sourceforge.net/svnroot/vivo/Harvester/branches/Development
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
else
	echo -n "Build a stable release? (merge down, tag and release from trunk): "
	read BUILDSTABLE
	echo -n "Build a point release? (merge down, tag and release from trunk): "
	read BUILDPOINT
fi

#update pom.xml,deb control, and env file with new version
#hack alert -- this will be fixed later
sed 10q pom.xml | sed "s/<version>.*<\/version>/<version>$RELEASENAME<\/version>/" > pom1.xml
sed '1,10d' pom.xml > pom2.xml
cat pom1.xml pom2.xml > pom.xml
rm pom1.xml pom2.xml

sed -i "s/Version: .*/Version: $RELEASENAME/" src/deb/control/control

sed -i "s/VERSION=.*/VERSION=$RELEASENAME/" scripts/env

#update conffiles from config


#commit pom file and deb control file
if [ "$COMMIT" = "y" ]; then
	svn commit -m "Update pom.xml and deb control file for release"
fi

#build
if [ "$RUNTEST" = "y" ]; then
	mvn clean dependency:copy-dependencies package
else
	mvn clean dependency:copy-dependencies package -DskipTests=true
fi

#check for failure
if [ "$?" = "1" ]; then
	echo "Exiting - Maven failure:" $?
	exit
fi

cd bin
#rename deb to use -
mv harvester_*.deb harvester-$RELEASENAME.deb

#unpack debian package
ar -x harvester-$RELEASENAME.deb

#rename tarball
mv data.tar.gz harvester-$RELEASENAME.tar.gz

#Upload tarball and deb package to sourceforge
if [ "$UPLOAD" = "y" ]; then
	scp harvester-$RELEASENAME.deb $NAME,vivo@frs.sourceforge.net:"/home/frs/project/v/vi/vivo/VIVO\ Harvester"
	scp harvester-$RELEASENAME.tar.gz $NAME,vivo@frs.sourceforge.net:"/home/frs/project/v/vi/vivo/VIVO\ Harvester"
fi


if [ "$BUILDSTABLE" = "y" ]; then
	#merge down to Staging
	svn co https://vivo.svn.sourceforge.net/svnroot/vivo/Harvester/branches/Staging
	cd Staging
	svn merge --depth=infinity https://vivo.svn.sourceforge.net/svnroot/vivo/Harvester/branches/Staging@HEAD https://vivo.svn.sourceforge.net/svnroot/vivo/Harvester/branches/Development@HEAD
	svn commit -m $RELEASENAME
	cd ..
	rm -rf Staging

	#merge down to trunk
	svn co https://vivo.svn.sourceforge.net/svnroot/vivo/Harvester/trunk
	cd trunk
	svn merge --depth=infinity https://vivo.svn.sourceforge.net/svnroot/vivo/Harvester/trunk@HEAD https://vivo.svn.sourceforge.net/svnroot/vivo/Harvester/branches/Staging@HEAD
	svn commit -m $RELEASENAME

	#tag inside trunk
	svn cp . https://vivo.svn.sourceforge.net/svnroot/vivo/Harvester/tags/$RELEASENAME
	svn commit -m "Tag Release"
	cd ..
	rm -rf trunk
elif [ "$BUILDPOINT" = "y" ]; then
	#merge down to trunk
	svn co https://vivo.svn.sourceforge.net/svnroot/vivo/Harvester/trunk
	cd trunk
	svn merge --depth=infinity https://vivo.svn.sourceforge.net/svnroot/vivo/Harvester/trunk@HEAD https://vivo.svn.sourceforge.net/svnroot/vivo/Harvester/branches/Staging@HEAD
	svn commit -m $RELEASENAME

	#tag inside trunk
	svn cp . https://vivo.svn.sourceforge.net/svnroot/vivo/Harvester/tags/$RELEASENAME
	svn commit -m "Tag Release"
	cd ..
	rm -rf trunk
fi
