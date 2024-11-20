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
echo "http://dspacevivo.vivoweb.org/individual/$(func-encode_string_to_uid.sh <&0)"
