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
#This tool requires you to have your ssh key on the sourceforge server

echo -n "Enter sourceforge username: "
read NAME

echo -n "Enter alpha number: "
read NUM

#edit release info to add alpha tag

#build
mvn clean dependency:copy-dependencies package

#check for fail
if [ "$?" -eq "1" ]; then
	echo "Exiting - Maven failure:" $?
	exit
fi

#get release name (hack alert!) :-)
cd bin
RELEASENAME=`ls | grep ingest`
RELEASENAME=${RELEASENAME:0:12}

#unpack debian package
ar -x $RELEASENAME.deb

#rename tarball
mv data.tar.gz $RELEASENAME-alpha$NUM.tar.gz

#rename deb
mv $RELEASENAME.deb $RELEASENAME-alpha$NUM.deb

#Upload tarball and deb package to sourceforge
scp $RELEASENAME-alpha$NUM.deb $NAME,vivo@frs.sourceforge.net:"/home/frs/project/v/vi/vivo/VIVO\ Harvester"
scp $RELEASENAME-alpha$NUM.tar.gz $NAME,vivo@frs.sourceforge.net:"/home/frs/project/v/vi/vivo/VIVO\ Harvester"
