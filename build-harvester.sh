#!/bin/bash

mvn clean dependency:copy-dependencies package -DskipTests=true
