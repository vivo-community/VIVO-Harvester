#!/bin/bash 

###################################################################
# Script Name   :
# Description   : This script allows to validate the commands needed to run the environment
# Args          : 
# Author       	: Michel Héon PhD
# Institution   : Université du Québec à Montréal
# Copyright     : Université du Québec à Montréal (c) 2022
# Email         : heon.michel@uqam.ca
###################################################################
export SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd -P)"
source $SCRIPT_DIR/../00-env.sh
source $SCRIPT_DIR/../lib/cleanup.sh
cmd_list=$TMPDIR/cdm_list.data

###################################################################
# List of commands to evaluate
cat << EOF > $cmd_list
[
adduser
ant
as
at
awk
basename
bash
cat
chmod
chown
chroot
clear
convert
cp
curl
cut
dash
date
diff
dir
dirname
display
du
echo
edit
env
expr
factor
false
file
find
free
from
GET
git
grep
groups
gzip
HEAD
head
host
hostname
info
init
install
jar
java
jstack
kill
last
less
link
ln
locate
look
ls
lsof
make
messages
mkdir
mkfifo
mktemp
more
mv
mvn
od
POST
post
print
printf
ps
pwd
readlink
recode
rename
reset
riot
rm
script
sed
see
service
sh
size
sleep
solr
sort
sparql
split
strings
strip
su
sudo
systemctl
tail
tar
tdbloader
tdbloader2
tdbloader2data
tdbloader2index
tee
test
time
top
touch
tr
tree
true
turtle
uniq
unzip
update
useradd
users
wc
wget
which
who
write
xargs
xdg-open
xml2json
yarn
EOF

for fn in `cat $cmd_list`; do
    result=$(which $fn 2> /dev/null ) 
    rtn=$?
    if [ $rtn = 0 ]; then
        echo "$(basename $result) ok!"
    else 
        echo "$fn NOT FOUND - please install it for a suitable result"
    fi
done
