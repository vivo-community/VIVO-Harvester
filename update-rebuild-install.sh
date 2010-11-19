#!/bin/bash
cd `dirname $(readlink -f $0)`
svn up && mvn clean dependency:copy-dependencies package -DskipTests=true
#check for failure
if [ "$?" -eq "1" ]; then
	echo "Exiting - svn or maven failure:" $?
	exit
fi
sudo dpkg -i bin/harvester*.deb
