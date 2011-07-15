#Copyright (c) 2010-2011 VIVO Harvester Team. For full list of contributors, please see the AUTHORS file provided.
#All rights reserved.
#This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html

#author: nskaggs@ctrip.ufl.edu

#WARNING: this code is fragile and not robust.
#This tool requires you to have your ssh key on the sourceforge server
#No attempts to sanitize or rationalize input have been made

echo -n "Enter release version: "
read RELEASENAME

echo -n "Run junit tests before build?: "
read RUNTEST

echo -n "Upload files to sourceforge?: "
read UPLOAD
if [ "$UPLOAD" = "y" ]; then
	echo -n "Enter sourceforge username: "
	read NAME
fi

#assuming inside release dir, up one dir
cd ..

#update pom.xml,deb control, and env file with new version
#hack alert -- this will be fixed later
sed 10q pom.xml | sed "s/<version>.*<\/version>/<version>$RELEASENAME<\/version>/" > pom1.xml
sed '1,10d' pom.xml > pom2.xml
cat pom1.xml pom2.xml > pom.xml
rm pom1.xml pom2.xml
sed -i "s/Version\: .*/Version\: $RELEASENAME/" src/deb/control/control

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

#rename deb to use harvester name
cd build
mv harvester_*.deb harvester-$RELEASENAME.deb

#unpack debian package
ar -x harvester-$RELEASENAME.deb

#create tarball
tar -xvzpf data.tar.gz #
cd usr/share/vivo/

#set permissions
find . -name '*.sh' | xargs chmod +x
chmod +x harvester/bin/harvester*
chmod +x harvester/vivo/scripts/*
tar -cvzpf harvester-$RELEASENAME.tar.gz harvester/
mv harvester-$RELEASENAME.tar.gz ../../../
cd ../../../

#Upload tarball and deb package to sourceforge
if [ "$UPLOAD" = "y" ]; then
	scp harvester-$RELEASENAME.deb $NAME,vivo@frs.sourceforge.net:"/home/frs/project/v/vi/vivo/VIVO\ Harvester"
	scp harvester-$RELEASENAME.tar.gz $NAME,vivo@frs.sourceforge.net:"/home/frs/project/v/vi/vivo/VIVO\ Harvester"
fi