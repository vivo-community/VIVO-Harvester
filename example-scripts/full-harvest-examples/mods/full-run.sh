#!/bin/bash

harvester-runbibutils -X runbibutils.conf.xml

harvester-sanitizemodsxml -X sanitizemodsxml.conf.xml

harvester-xsltranslator -X xsltranslator.conf.xml

harvester-transfer -X transfer-records-to-model.conf.xml

harvester-score -X score-pub.conf.xml
harvester-match -X match-pub.conf.xml

harvester-score -X score-author.conf.xml
harvester-score -X score-org.conf.xml
harvester-score -X score-geo.conf.xml
harvester-score -X score-journal.conf.xml
harvester-score -X score-hyperlink.conf.xml
harvester-score -X score-interval.conf.xml
harvester-score -X score-datetime.conf.xml
harvester-match -X match-main.conf.xml

harvester-score -X score-authorship.conf.xml
harvester-match -X match-authorship.conf.xml

harvester-qualify -X qualify.conf.xml

harvester-changenamespace -X changenamespace-pub.conf.xml
harvester-changenamespace -X changenamespace-authorship.conf.xml
harvester-changenamespace -X changenamespace-author.conf.xml
harvester-changenamespace -X changenamespace-org.conf.xml
harvester-changenamespace -X changenamespace-geo.conf.xml
harvester-changenamespace -X changenamespace-journal.conf.xml
harvester-changenamespace -X changenamespace-hyperlink.conf.xml
harvester-changenamespace -X changenamespace-interval.conf.xml
harvester-changenamespace -X changenamespace-datetime.conf.xml

harvester-diff -X diff-subtractions.conf.xml
harvester-diff -X diff-additions.conf.xml

harvester-transfer -X transfer-subtractions-to-prevharv.conf.xml
harvester-transfer -X transfer-additions-to-prevharv.conf.xml
harvester-transfer -X transfer-subtractions-to-vivo.conf.xml
harvester-transfer -X transfer-additions-to-vivo.conf.xml

