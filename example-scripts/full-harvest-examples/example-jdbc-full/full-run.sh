#!/bin/bash

harvester-databaseclone -X databaseclone.conf.xml

harvester-jdbcfetch -X jdbcfetch.conf.xml

harvester-xsltranslator -X xsltranslator.conf.xml

harvester-transfer -X transfer-records-to-model.conf.xml

harvester-score -X score-people.conf.xml

harvester-score -X score-departments.conf.xml

harvester-match -X match-people-departments.conf.xml

harvester-jenaconnect -X jenaconnect-clear-score-data.conf.xml

harvester-score -X score-positions.conf.xml

harvester-match -X match-positions.conf.xml

harvester-changenamespace -X changenamespace-people.conf.xml

harvester-changenamespace -X changenamespace-departments.conf.xml

harvester-changenamespace -X changenamespace-positions.conf.xml

harvester-diff -X diff-subtractions.conf.xml

harvester-diff -X diff-additions.conf.xml

harvester-transfer -X transfer-subtractions-to-prevharv.conf.xml

harvester-transfer -X transfer-additions-to-prevharv.conf.xml

harvester-transfer -X transfer-subtractions-to-vivo.conf.xml

harvester-transfer -X transfer-additions-to-vivo.conf.xml