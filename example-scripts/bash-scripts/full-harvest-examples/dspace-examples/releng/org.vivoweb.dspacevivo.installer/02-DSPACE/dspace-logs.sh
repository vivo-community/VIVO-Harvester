#!/bin/bash

# Description:
# Show logs of Dspace with Docker-compose

export SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
source $SCRIPT_DIR/../00-env.sh
cd $DEPLOY
cd dspace-angular
docker-compose -p d7 -f docker/docker-compose.yml -f docker/docker-compose-rest.yml logs -f
