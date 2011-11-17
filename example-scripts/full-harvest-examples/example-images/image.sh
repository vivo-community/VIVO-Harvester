#!/bin/bash
# Copyright (c) 2010-2011 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence, Michael Barbieri.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the new BSD license
# which accompanies this distribution, and is available at
# http://www.opensource.org/licenses/bsd-license.html
# 
# Contributors:
#     Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence, Michael Barbieri - initial API and implementation


IMAGES_LOG_DIR=/usr/share/vivo/vivo-auto-harvest/images/log
# @TODO Fix these locations





############################################################################################# backup harvest 

##touch lastvivobackup.sql

##mysqldump -u vivoApp -h vivodbprod1.vivo.ufl.edu vivo -p > lastvivobackup.sql

echo " Vivo backup done"



export HARVEST_NAME=example-images
HARVESTER_INSTALL_DIR="/usr/share/vivo/harvester"
HARVESTER_LOG_DIR="$HARVESTER_INSTALL_DIR/example-scripts/example-images/logs"
IMG_HARVESTER_HOME_DIR="$HARVESTER_INSTALL_DIR/example-scripts/example-images"

IMAGES_LOG_DIR="$IMG_HARVESTER_HOME_DIR/email-log"

# set the following variable to the  Integer , number of Images   you want to fetch from the Activemq in a single execution 
MAX_FECTHED=4

# Check if MAX_Fectched is an integer

if [ "`echo $MAX_FECTHED | egrep ^[[:digit:]]+$`" = "" ]; then

echo  "Number of Images to fetch  in  image.sh  is not an integer"
exit
fi

#The email address to which the log file will be sent.

EMAIL_RECIPIENT=ufvivotech-l@lists.ufl.edu

# Add harvester binaries to path for execution
# The tools within this script refer to binaries supplied within the harvester
#       Since they can be located in another directory their path should be
#       included within the classpath and the path environment variables.
export PATH=$PATH:$HARVESTER_INSTALL_DIR/bin
export CLASSPATH=$CLASSPATH:$HARVESTER_INSTALL_DIR/bin/harvester.jar:$HARVESTER_INSTALL_DIR/bin/dependency/*:$HARVESTER_INSTALL_DIR/bin
export CLASSPATH=$CLASSPATH:$HARVESTER_INSTALL_DIR/build/harvester.jar:$HARVESTER_INSTALL_DIR/build/dependency/*

rm -f tmp.txt
##rm -r  $IMG_HARVESTER_HOME_DIR/fullImages/
##rm -r  $IMG_HARVESTER_HOME_DIR/thumbnails/
rm -r  $IMG_HARVESTER_HOME_DIR/images/

#rm -rf email-log
mkdir email-log

# Supply the location of the detailed log file which is generated during the script.
#       If there is an issue with a harvest, this file proves invaluable in finding
#       a solution to the problem. It has become common practice in addressing a problem
#       to request this file. The passwords and usernames are filtered out of this file
#       to prevent these logs from containing sensitive information.
echo "Full Logging in image-harvest-$DATE.log"
if [ ! -d logs ]; then
  mkdir logs
fi
cd logs
touch $HARVEST_NAME.$DATE.log
ln -sf $HARVEST_NAME.$DATE.log $HARVEST_NAME.latest.log
cd ..


if [ ! -d images ]; then
  mkdir images

fi

DATE=`date +%Y-%m-%d`
IMAGES_LOG_FILE=$IMAGES_LOG_DIR/images_$DATE.log
touch $IMAGES_LOG_FILE

#Calling script file that calls the JAVA program that fectches  images from the gator1 sever and copy then to images/ dir
START=$(date +%s)

echo " calling Image queue Consumer"
harvester-image -p $HARVESTER_INSTALL_DIR/example-scripts/example-images -m $MAX_FECTHED

count=`ls -A images/ | wc -l`

#The base page for vivo is needed to give a location to 
function get_datetime {
	date '+%Y-%m-%d %H:%M:%S'
}
#If the numbre of Images fetched in the immage dir is 0 .. print the message and exit the program

echo "Log file being created : " $IMAGES_LOG_FILE
echo `get_datetime` "Begin Image Run"
echo "Image Ingest Process" &> $IMAGES_LOG_FILE
echo "================================================================================="  &>> $IMAGES_LOG_FILE
echo `get_datetime` "Begin Image Ingest" &>> $IMAGES_LOG_FILE

echo "First moving to the harvester directory"
cd ${HARVESTER_INSTALL_DIR}

echo  "Initializing   Image Harvest"
echo  "Creating  folders  fullimage ,thumbnails"
mkdir $IMG_HARVESTER_HOME_DIR/images
mkdir $IMG_HARVESTER_HOME_DIR/fullImages
mkdir $IMG_HARVESTER_HOME_DIR/thumbnails
echo "Sourcing and Scaling Images"

if [ "$count" -ne 0  ]; then

echo "Calling harvester-Image"
### Follwing  lines scale  the intitial images to Vivo Supported image size  and put them in thumImages  folder  and fullImages folder

NEW_IMAGE=`ls -l $IMG_HARVESTER_HOME_DIR/images  | egrep -c '^-'` 
cd $IMG_HARVESTER_HOME_DIR/images

rm -f $IMAGES_LOG_DIR/fetchedimage.txt
touch $IMAGES_LOG_DIR/fetchedimage.txt

echo -e "\n" &>> $IMAGES_LOG_DIR/fetchedimage.txt
echo "List of UFIDs fetched from ActiveMQ" &>> $IMAGES_LOG_DIR/fetchedimage.txt
echo "==================================================================================" &>> $IMAGES_LOG_DIR/fetchedimage.txt
cat $IMAGES_LOG_DIR/fetchedimage.txt

for fn in *; do
 var3=`identify $fn | awk '{FS=" ";print $2}'`
 img=`identify $fn | cut -f 3 -d' '`
 WIDTH=`echo $img | cut -d'x' -f 1`
 HEIGHT=`echo $img | cut -d'x' -f 2`

if [ "$WIDTH" -le 200 ] && [ "$HEIGHT" -le 200 ]
then
              
        var2="sudo convert $fn -resize 200 ../fullImages/$fn.jpeg"
else
        var2="cp $fn ../fullImages/$fn.jpeg"
fi
var1="sudo convert $fn -resize 200 ../thumbnails/thumbnail$fn.jpeg"

echo "$fn " &>>$IMAGES_LOG_DIR/fetchedimage.txt

$var1
$var2
done
#Done for for loop

THUMB_IMAGE_COUNT=`ls -l $IMG_HARVESTER_HOME_DIR/thumbnails  | egrep -c '^-'`

echo "Total images fetched:  " $NEW_IMAGE 
echo "Total images fetched:  " $NEW_IMAGE &>> $IMAGES_LOG_FILE
else 
	echo "Total images fetched:  0"  
	echo "Total images fetched:  0"  &>> $IMAGES_LOG_FILE	
fi
###END count if

cd $IMG_HARVESTER_HOME_DIR/images
echo "Then running the script"
cd ${IMG_HARVESTER_HOME_DIR}
echo "Running Pre Image Ingest Analytics......."

bash analytics.sh
bash run-image.sh 

#Get the number of  images addded from the addition file.
UPLOADED_IMAGE=`cat data/vivo-additions.rdf.xml | grep vivo.ufl.edu/individual | wc -l`

echo "Total images uploaded: "$UPLOADED_IMAGE
echo "Total images uploaded:  " $UPLOADED_IMAGE &>> $IMAGES_LOG_FILE

END=$(date +%s)
DIFF=$(( $END - $START ))
echo "Time to complete Image Ingest:  $DIFF seconds " &>> $IMAGES_LOG_FILE
echo "Time to complete Image Ingest:  $DIFF seconds "

echo "End Image Ingest" &>> $IMAGES_LOG_FILE
echo -e "\n" &>> $IMAGES_LOG_FILE

echo "Pre Image Ingest Analytics" &>> $IMAGES_LOG_FILE
echo "================================================================================="  &>> $IMAGES_LOG_FILE
cat analytics.txt &>> $IMAGES_LOG_FILE
echo -e "\n" &>> $IMAGES_LOG_FILE

echo "Running Post Image Ingest Analytics......."
bash analytics.sh
echo "Post Image Ingest Analytics" &>> $IMAGES_LOG_FILE
echo "================================================================================="  &>> $IMAGES_LOG_FILE
cat analytics.txt &>> $IMAGES_LOG_FILE
echo -e "\n" &>> $IMAGES_LOG_FILE


if [ "$UPLOADED_IMAGE" -ne 0  ]; then

echo  "URL's Of Profile updates in current Imageharvest"  &>> $IMAGES_LOG_FILE
echo "================================================================================="  &>> $IMAGES_LOG_FILE
serverURL=`grep vivo-server-url= system.properties | cut -d'=' -f 2`

rm -f uploadURL.txt
touch uploadURL.txt
grep http://vivo.ufl.edu/individual data/vivo-additions.rdf.xml | cut -d'=' -f 2 | sed 's/.\(.*\)../\1/' >>uploadURL.txt
rm -f output.txt
touch output.txt
exec<uploadURL.txt
value=0
 while read line
  do
       echo $line >> $IMAGES_LOG_FILE;
 done
fi
#end if

echo `get_datetime` "End IMAGES Run"

#Assemble the log files to be emailed
cat $IMAGES_LOG_FILE > tmp.txt
##echo "Full Log">>tmp.txt
cat /usr/share/vivo/harvester/example-scripts/example-images/email-log/fetchedimage.txt >> tmp.txt
echo -e "\n"  >> tmp.txt
echo "Ending full log" >> tmp.txt
#Mail the assembled log file to the desired person.

mail -s "\"IMAGES harvest of $DATE\"" "$EMAIL_RECIPIENT" < tmp.txt

#Remove the assembled file
#rm -f tmp.txt
#rm -r  $IMG_HARVESTER_HOME_DIR/fullImages/
#rm -r  $IMG_HARVESTER_HOME_DIR/thumbnails/
#rm -r  $IMG_HARVESTER_HOME_DIR/images/


