#!/bin/bash

# Description:
# Download and run Dspace with Docker-compose

export SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
source $SCRIPT_DIR/../00-env.sh
cd $DEPLOY

git clone https://github.com/DSpace/dspace-angular.git
cd dspace-angular
sed -i 's/DSPACE_REST_PORT: 8080/DSPACE_REST_PORT: 8081/' docker/docker-compose.yml
sed -i 's/8080\/server/8081\/server/' docker/docker-compose-rest.yml
sed -i 's/8080\/server/8081\/server/' docker/docker-compose-ci.yml
sed -i 's/8983\/solr/8984\/solr/' docker/docker-compose-rest.yml
sed -i 's/published: 8080/published: 8081/' docker/docker-compose-rest.yml
sed -i 's/published: 8983/published: 8984/' docker/docker-compose-rest.yml
sed -i 's/published: 8080/published: 8081/' docker/docker-compose-ci.yml


docker-compose -f docker/docker-compose.yml pull
docker-compose -p d7 -f docker/docker-compose.yml -f docker/docker-compose-rest.yml up -d
