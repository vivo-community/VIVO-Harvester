#!/bin/bash

# Description:
# Create an admin account for Dspace

export SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
source $SCRIPT_DIR/../00-env.sh
cd $DEPLOY
cd dspace-angular
docker-compose -p d7 -f docker/cli.yml run --rm dspace-cli create-administrator -e test@test.edu -f admin -l user -p admin -c en
