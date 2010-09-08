# Copyright (c) 2010 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the new BSD license
# which accompanies this distribution, and is available at
# http://www.opensource.org/licenses/bsd-license.html
#
# Contributors:
#     Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams - initial API and implementation

#author: nskaggs@ctrip.ufl.edu

#WARNING: this code is fragile and not robust.

echo -n "Enter sourceforge username: "
read NAME
echo -n "Enter sourceforge password: "
read PASSWORD

#check out from svn
svn co https://vivo.svn.sourceforge.net/svnroot/vivo/Harvester/trunk

#build
cd trunk
mvn clean dependency:copy-dependencies package

#check for fail
if [ "$?" -eq "1" ]; then
	echo "Exiting - Maven failure:" $?
	exit
fi

#get release name (hack alert!) :-)
cd bin
RELEASENAME=`ls | grep ingest`
RELEASENAME=${RELEASENAME:0:1}

#unpack debian package
ar -x $RELEASENAME.deb

#rename tarball
mv data.tar.gz $RELEASENAME.tar.gz

#Upload tarball and deb package to sourceforge
echo $RELEASENAME.deb
echo $NAME

scp $RELEASENAME.deb $NAME,vivo@frs.sourceforge.net:"/home/frs/project/v/vi/vivo/VIVO Harvester"
expect "password:"
send "$PASSWORD\r"
scp $RELEASENAME.tar.gz $NAME,vivo@frs.sourceforge.net:"/home/frs/project/v/vi/vivo/VIVO Harvester"
expect "password:"
send "$PASSWORD\r"
