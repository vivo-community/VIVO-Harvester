#!/bin/bash

# Absolute path to this script. /home/user/bin/foo.sh
SCRIPT=$(readlink -f $0)

cd `dirname $SCRIPT`

sudo svn up --force --accept theirs-full
sudo mvn clean dependency:copy-dependencies package
sudo dpkg -i bin/ingest*.deb