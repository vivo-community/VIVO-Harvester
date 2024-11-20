#!/bin/bash 

###################################################################
# Script Name   :
# Description   : Allows associating a DSpace subject to a VIVO Expertise
# Args          : 
# Author       	: Michel Héon PhD
# Institution   : Université du Québec à Montréal
# Copyright     : Université du Québec à Montréal (c) 2022
# Email         : heon.michel@uqam.ca
###################################################################
export SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd -P)"
source $SCRIPT_DIR/00-env.sh

CREATOR_QUERY=$TMPDIR/creator.sparql
EXPERTISE_QUERY=$TMPDIR/expertise.sparql
ITEM_URI_QUERY=$TMPDIR/item.sparql
cat  <<EOF > $CREATOR_QUERY
$(cat $QUERY_DIR/header.sparql)
select ?aut
 where { 
  ?s dc:creator ?aut  .
    } 
EOF
cat  <<EOF > $ITEM_URI_QUERY
$(cat $QUERY_DIR/header.sparql)
select DISTINCT ?s
 where { 
  ?s dc:creator ?aut  .
    } 
EOF

cat  <<EOF > $EXPERTISE_QUERY
$(cat $QUERY_DIR/header.sparql)
select ?sub
 where { 
  ?s dc:subject ?sub  .
    } 
EOF
cd $ETL_DIR_TRANSFORM
NBR_FILE=$(ls *.ntriples | wc -l)
for f in  *.ntriples
do
    ((LOOP_CTR=LOOP_CTR+1))
    fileName=$(realpath $f)
    echo "($LOOP_CTR/$NBR_FILE) Processing $f"
    BN=$(basename $f .ntriples)
    cp $fileName $TMPDIR/$BN.nt
    # Extract list of expertises
    LIST_OF_EXPERT=$(sparql --data=$TMPDIR/$BN.nt --query=$EXPERTISE_QUERY --results=TSV 2>/dev/null | func-skip-first-line.sh | func-encode_string_to_expertise.sh)
    if [ -z "${LIST_OF_EXPERT}" ]; then
        echo "    LIST_OF_EXPERTISE is empty"
        continue
    fi
    # if list of expertises exist, then extract list of persons
    LIST_OF_PERSON=$(sparql --data=$TMPDIR/$BN.nt --query=$CREATOR_QUERY --results=TSV 2>/dev/null | func-skip-first-line.sh | tr -d '"' | func-clean-begin-ending-whitespace.sh)
    if [ -z "${LIST_OF_PERSON}" ]; then
        echo "    LIST_OF_PERSON is empty"
        continue
    fi
    # if list of expertises exist and the extract list of persons exist, the extract de URI of the ITEM
    ITEM_URI=$(sparql --data=$TMPDIR/$BN.nt --query=$ITEM_URI_QUERY --results=TSV 2>/dev/null | func-skip-first-line.sh | func-remove-brace-to-uri.sh) 
    while read aPerson; do 
        while read anExpertise; do 
            map-expertise-and-item-to-a-person-to-vivo.sh "$anExpertise" "$aPerson" $ITEM_URI &
	    sleep 0.25
        done <<< " $LIST_OF_EXPERT" 
    done <<< " $LIST_OF_PERSON" 
    wait
done

###################################################################
# Execution end
exit 0

cd $ETL_DIR_TRANSFORM_EXPERTISES
NBR_FILE=$(cat $MAPPING_DATA_DIR/list-of-all-expertises.data | wc -l)
while read name; do
    ((LOOP_CTR=LOOP_CTR+1))
    echo "($LOOP_CTR/$NBR_FILE) Processing $name"
    map-expertise-to-a-person-to-vivo.sh "$name" &
    ((j=j+1))
    if [ $j = "15" ]
    then
        wait; ((j=0)) ; echo "($LOOP_CTR/$NBR_FILE)====================" ; sleep 0.1 
    else
        sleep 0.10
    fi
done < $MAPPING_DATA_DIR/list-of-all-expertises.data
wait
echo "$(basename $0) Done" >&2


