#!/bin/bash 

###################################################################
# Script Name   :
# Description   : Standardizes the way to encode an expertise
# Args          : Arguments ars stdin and stdout
# Author       	: Michel Héon PhD
# Institution   : Université du Québec à Montréal
# Copyright     : Université du Québec à Montréal (c) 2022
# Email         : heon.michel@uqam.ca
###################################################################
#
# Traduit la chaine de caractères et fait des sauts de lignes au ',;' 
tr ',;' '\n'  <&0 |  tr -d '"' | func-clean-begin-ending-whitespace.sh | func-capitalize-each-fisrt-caracter.sh
