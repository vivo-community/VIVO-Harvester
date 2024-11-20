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
source $SCRIPT_DIR/../lib/cleanup.sh
cd $SCRIPT_DIR/../../../


cat $(find . -type f -executable -exec file {} \; -print | grep  "shell script" | cut -f 1 -d ':' ) | tr ' ' '\n' | sort | uniq | grep -v -e '"' -e',' -e'\.' -e';' -e'>' -e'}' -e']' -e')' -e'#' -e'\$' -e'=' -e "'" -e':' -e'yes' -e'no' -e'/' -e'|' -e'-rm' > word_list.txt
for fn in `cat word_list.txt`; do
#    echo "the next file is $fn"
    result=$(which $fn 2> /dev/null ) 
    rtn=$?
    if [ $rtn = 0 ]; then
        echo "$(basename $result)" >> $TMPDIR/list_of_all_cmd.data
    fi
done
cat $TMPDIR/list_of_all_cmd.data | sort | uniq > $SCRIPT_DIR/list_of_all_cmd.data 
