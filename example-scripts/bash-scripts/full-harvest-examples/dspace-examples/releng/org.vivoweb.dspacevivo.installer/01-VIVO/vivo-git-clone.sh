#!/bin/bash

###################################################################
# Script Name   :
# Description   :
# Args          : 
# Author       	: Michel Héon PhD
# Institution   : Université du Québec à Montréal
# Copyright     : Université du Québec à Montréal (c) 2022
# Email         : heon.michel@uqam.ca
###################################################################
export SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd -P)"
source $SCRIPT_DIR/../00-env.sh
export REL=1.12.2
cd $GIT_REPO
git clone https://github.com/vivo-project/VIVO.git
(cd VIVO; git checkout tags/vivo-$REL ) 
git clone https://github.com/vivo-project/Vitro.git
(cd Vitro; git checkout tags/vitro-$REL ) 
git clone https://github.com/vivo-project/VIVO-languages.git
(cd VIVO-languages; git checkout tags/vivo-languages-$REL )
git clone https://github.com/vivo-project/Vitro-languages.git
(cd Vitro-languages; git checkout tags/vitro-languages-$REL)
