#!/bin/bash

CURRENT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd -P)"
cd $CURRENT_DIR/example-scripts/bash-scripts/full-harvest-examples/dspace-examples/script
bash mvn_install_example.sh
cd $CURRENT_DIR
mvn clean dependency:copy-dependencies package -DskipTests=true
