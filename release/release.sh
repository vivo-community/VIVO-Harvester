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

# Set working directory
HARVESTERDIR=`dirname "$(cd "${0%/*}" 2>/dev/null; echo "$PWD"/"${0##*/}")"`
HARVESTERDIR=$(cd $HARVESTERDIR; cd ..; pwd)

echo -n "Enter sourceforge username: "
read NAME

echo -n "Build from? (dev, trunk | blank for local): "
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
	svn co svn+ssh://${NAME}@svn.code.sf.net/p/vivo/code/Harvester/branches/Development
	cd Development
	#if we're pulling from dev, ask if releasing stable 
	echo -n "Merge to trunk?: "
	read MERGETRUNK
elif [ "$CODELOC" = "trunk" ]; then
	svn co svn+ssh://${NAME}@svn.code.sf.net/p/vivo/code/vivo/Harvester/trunk
	cd trunk
else
	echo -n "Merge to trunk?: "
	read MERGETRUNK
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


#build
if [ "$RUNTEST" = "y" ]; then
	mvn clean dependency:copy-dependencies package
else
	mvn clean dependency:copy-dependencies package -DskipTests=true
fi

#check for failure
if [ "$?" -ne "0" ]; then
	echo "Exiting - Maven failure:" $?
	exit
fi

#commit pom file and deb control file
if [ "$COMMIT" = "y" ]; then
	svn commit -m "Update pom.xml and deb control file for release $RELEASENAME"
fi


cd bin
#rename deb to use -
mv harvester_*.deb harvester-$RELEASENAME.deb

#unpack debian package
ar -x harvester-$RELEASENAME.deb

#create tarball
tar -xvzf data.tar.gz
cd usr/share/vivo/
tar -cvzf harvester-$RELEASENAME.tar.gz harvester/


#Upload tarball and deb package to sourceforge
if [ "$UPLOAD" = "y" ]; then
	scp harvester-$RELEASENAME.deb $NAME,vivo@frs.sourceforge.net:"/home/frs/project/v/vi/vivo/VIVO\ Harvester"
	scp harvester-$RELEASENAME.tar.gz $NAME,vivo@frs.sourceforge.net:"/home/frs/project/v/vi/vivo/VIVO\ Harvester"
fi

#tag
cd $HARVESTERDIR
svn cp . svn+ssh://${NAME}@svn.code.sf.net/p/vivo/code/Harvester/tags/$RELEASENAME
svn commit -m "Tag Release $RELEASENAME"


if [ "$MERGETRUNK" = "y" ]; then
	#merge down to trunk
	svn co svn+ssh://${NAME}@svn.code.sf.net/p/vivo/code/Harvester/trunk
	cd trunk
	svn merge --depth=infinity svn+ssh://${NAME}@svn.code.sf.net/p/vivo/code/Harvester/trunk@HEAD svn+ssh://${NAME}@svn.code.sf.net/p/vivo/code/Harvester/branches/Development@HEAD
	svn commit -m "Commit Release $RELEASENAME"
fi
