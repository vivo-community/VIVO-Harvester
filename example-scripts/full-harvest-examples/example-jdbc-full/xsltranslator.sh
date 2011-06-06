#!/bin/bash

# see example-scripts/README.txt for information about HARVESTER_JAVA_OPTS

java $HARVESTER_JAVA_OPTS org.vivoweb.harvester.translate.XSLTranslator -X xsltranslator.conf.xml
