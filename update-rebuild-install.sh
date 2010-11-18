#!/bin/bash
cd `dirname $(readlink -f $0)`
svn up
mvn clean dependency:copy-dependencies package -DskipTests=true
sudo dpkg -i bin/harvester*.deb
