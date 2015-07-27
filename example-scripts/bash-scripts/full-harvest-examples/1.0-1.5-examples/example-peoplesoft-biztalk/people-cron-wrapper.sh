#!/bin/bash
#This wrapper ensures that any errors or failures caused by the ingest process result in a released lock when run through cron.

export PEOPLE_HARVEST_HOME=/usr/local/src/VIVO-Harvester/example-scripts/bash-scripts/full-harvest-examples/1.0-1.5-examples/example-peoplesoft-biztalk

cd $PEOPLE_HARVEST_HOME

set -e
sudo bash run-people.sh
