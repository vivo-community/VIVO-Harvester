#!/bin/bash
cd `dirname $(readlink -f $0)`
sudo svn up --force --accept theirs-full
sudo mvn clean dependency:copy-dependencies package
sudo dpkg -i bin/ingest*.deb
