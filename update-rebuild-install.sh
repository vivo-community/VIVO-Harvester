#!/bin/bash
set -e
#svn up
hg pull
hg update
mvn clean dependency:copy-dependencies package -DskipTests=true
#check for failure
sudo dpkg -i bin/harvester*.deb
