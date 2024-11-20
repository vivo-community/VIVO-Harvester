#!/bin/bash

###################################################################
# Script Name   :
# Description   :
# Args          : Arguments ars stdin and stdout
# Author       	: Michel Héon PhD
# Institution   : Université du Québec à Montréal
# Copyright     : Université du Québec à Montréal (c) 2022
# Email         : heon.michel@uqam.ca
###################################################################
recode -f utf8..flat  <&0 | tr '[:upper:]' '[:lower:]' | tr '[[:punct:]][[:blank:]]' '_' | tr -s '_' 
