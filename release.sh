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
read name
echo -n "Enter sourceforge password: "
read password

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
#sed -n 's/<version>,</version>/p' pom.xml
cd bin
RELEASENAME=`ls | grep ingest`
RELEASENAME=${RELEASENAME:0:12}

#unpack debian package
ar -x $RELEASENAME.deb

#rename tarball
mv data.tar.gz $RELEASENAME.tar.gz

#make tarball
#tar -cjvf ingest.tar.bz usr/

#Upload tarball and deb package to sourceforge
sftp $NAME,$PASSWORD@frs.sourceforge.net:"/home/frs/project/v/vi/vivo/VIVO Harvester"
ls
#put $RELEASENAME.deb
#put $RELEASENAME.tar.gz
