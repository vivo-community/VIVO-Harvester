#!/bin/bash
#This wrapper ensures that any errors or failures caused by the ingest process result in a released lock when run through cron.

export PEOPLE_HARVEST_HOME=/data/vivo/harvester/vivo-auto-harvest/peoplesoft-biztalk/peoplesoft-ingest

cd $PEOPLE_HARVEST_HOME

set -e
sudo bash run-people.sh
