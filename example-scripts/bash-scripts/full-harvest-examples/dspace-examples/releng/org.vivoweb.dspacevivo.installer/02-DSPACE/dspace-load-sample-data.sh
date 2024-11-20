#!/bin/bash

# Description:
# Load sample data on Dspace

export SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
source $SCRIPT_DIR/../00-env.sh
cd $DEPLOY
cd dspace-angular
docker-compose -p d7 -f docker/cli.yml -f ./docker/cli.ingest.yml run --rm dspace-cli
