#!/bin/bash
mvn clean dependency:copy-dependencies package -DskipTests=true
#check for failure
sudo dpkg -i bin/harvester*.deb
