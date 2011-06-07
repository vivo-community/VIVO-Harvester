#!/bin/bash

./databaseclone.sh

./jdbcfetch.sh

./xsltranslator.sh

./transfer-records-to-model.sh

./score-people.sh

./score-departments.sh

./match-people-departments.sh

./jenaconnect-clear-score-data.sh

./score-positions.sh

./match-positions.sh

./changenamespace-people.sh

./changenamespace-departments.sh

./changenamespace-positions.sh

./diff-subtractions.sh

./diff-additions.sh

./transfer-subtractions-to-prevharv.sh

./transfer-additions-to-prevharv.sh

./transfer-subtractions-to-vivo.sh

./transfer-additions-to-vivo.sh