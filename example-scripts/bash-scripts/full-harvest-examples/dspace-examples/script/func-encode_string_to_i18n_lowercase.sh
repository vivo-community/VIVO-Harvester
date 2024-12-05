#!/bin/bash 

###################################################################
# Script Name   :
# Description   : put all in lowercase (also special caracters (ex. É to é) 
# Args          : Arguments ars stdin and stdout
# Author       	: Michel Héon PhD
# Institution   : Université du Québec à Montréal
# Copyright     : Université du Québec à Montréal (c) 2022
# Email         : heon.michel@uqam.ca
###################################################################
awk '{print tolower($0);}' <&0  | awk '{$1=$1};1'
