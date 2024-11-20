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
sed -e "s/\b\(.\)/\u\1/g"  <&0 | tr -d '"' | func-clean-begin-ending-whitespace.sh  
