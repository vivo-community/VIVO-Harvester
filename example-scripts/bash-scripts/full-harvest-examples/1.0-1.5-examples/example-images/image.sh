#!/bin/bash
# Copyright (c) 2010-2011 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, 
# James Pence, Michael Barbieri, Vincent Sposato, Mayank Saini, Kuppuraj Gunasekaran
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the new BSD license
# which accompanies this distribution, and is available at
# http://www.opensource.org/licenses/bsd-license.html
# 
# Contributors:
#	Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence, Michael Barbieri - initial API and implementation
#	Vincent Sposato, Mayank Saini, Kuppuraj Gunasekaran - Updating of script and workflow - 2012-03-08

# Point the script to the system properties file that houses all of the data
. /usr/local/src/VIVO-Harvester/example-scripts/bash-scripts/full-harvest-examples/1.0-1.5-examples/example-images/system.properties

################## ADD OPENCONNECT CONNECTION SCRIPT - VJS 20111216 ##################
rm -f $OPENCONNECT_LOG
date >>$OPENCONNECT_LOG
echo -e "\n" >>$OPENCONNECT_LOG
sudo bash /etc/init.d/openconnect start >>$OPENCONNECT_LOG
################## ADD OPENCONNECT CONNECTION SCRIPT - VJS 20111216 ##################

# Change directory to the harvester directory
cd $IMG_HARVESTER_HOME_DIR
echo $MAX_FETCHED
# Check if MAX_Fetched is an integer
if [ "`echo $MAX_FETCHED | egrep ^[[:digit:]]+$`" = "" ]; then
	echo  "Number of Images to fetch in image.sh is not an integer"
	exit
fi

# Add harvester binaries to path for execution
# The tools within this script refer to binaries supplied within the harvester
#       Since they can be located in another directory their path should be
#       included within the classpath and the path environment variables.
export PATH=$PATH:$HARVESTER_INSTALL_DIR/bin
export CLASSPATH=$CLASSPATH:$HARVESTER_INSTALL_DIR/bin/harvester.jar:$HARVESTER_INSTALL_DIR/bin/dependency/*:$HARVESTER_INSTALL_DIR/bin
export CLASSPATH=$CLASSPATH:$HARVESTER_INSTALL_DIR/build/harvester.jar:$HARVESTER_INSTALL_DIR/build/dependency/*


# Check to see if temporary storage file exists, and if so, delete it
if [ -f $TEMP_FILE ]; then
	rm -rf $TEMP_FILE
fi
# Check to see if images directory exists, and if so, delete it all contents
if [ -d $IMAGE_DIR ]; then
	rm -rf $IMAGE_DIR/*
fi
# Check to see if images directory does not exist, and if so, create the directory
if [ ! -d $IMAGE_DIR ]; then
	mkdir $IMAGE_DIR
fi
# Check to see if full log directory exists, and if not, create it
if [ ! -d $HARVESTER_LOG_DIR ]; then
  mkdir $HARVESTER_LOG_DIR
fi
# Check to see if email log directory exists, and if not, create it
if [ ! -d $EMAIL_HARVEST_DIR ]; then
	mkdir $EMAIL_HARVEST_DIR
fi
# Check to see if previous harvest directory exists, and if so, delete it
if [ -d $PREV_HARVEST_DIR ]; then
	rm -rf $PREV_HARVEST_DIR
fi

# Supply the location of the detailed log file which is generated during the script.
#       If there is an issue with a harvest, this file proves invaluable in finding
#       a solution to the problem. It has become common practice in addressing a problem
#       to request this file. The passwords and usernames are filtered out of this file
#       to prevent these logs from containing sensitive information.
echo "Full Logging in $FULL_HARVEST_LOG"

# Create link to full harvest log file to a file ending in latest
ln -sf $FULL_HARVEST_LOG $HARVESTER_LOG_DIR/$HARVEST_NAME.latest.log
# Create the email log file
touch $EMAIL_HARVEST_LOG

#Calling script file that calls the JAVA program that fectches images from the gator1 sever and copy then to images/ dir
echo " calling Image queue Consumer"
harvester-image -p $IMG_HARVESTER_HOME_DIR -m $MAX_FETCHED -b no

count=`ls -A $IMAGE_DIR | wc -l`

function get_datetime {
	date '+%Y-%m-%d %H:%M:%S'
}
#If the numbre of Images fetched in the immage dir is 0 .. print the message and exit the program
START=$(date +%s)
echo "Log file being created : " $EMAIL_HARVEST_LOG
echo `get_datetime` "Begin Image Run"
echo "Image Ingest Process" &> $EMAIL_HARVEST_LOG
echo "================================================================================="  &>> $EMAIL_HARVEST_LOG
echo `get_datetime` "Begin Image Ingest" &>> $EMAIL_HARVEST_LOG

echo "First moving to the harvester directory"
cd $HARVESTER_INSTALL_DIR

echo  "Initializing   Image Harvest"
echo  "Creating  folders  fullimage ,thumbnails"

# Check to see if fullImages directory exists, if not, create it
if [ ! -d $FULLIMAGE_DIR ]; then
	mkdir $FULLIMAGE_DIR
fi

# Check to see if fullImages directory exists, if not, create it
if [ ! -d $THUMBIMAGE_DIR ]; then
        mkdir $THUMBIMAGE_DIR
fi

echo "Sourcing and Scaling Images"

if [ "$count" -ne 0  ]; then
	echo "Calling harvester-Image"
	### Follwing  lines scale  the intitial images to Vivo Supported image size  and put them in thumImages  folder  and fullImages folder
	NEW_IMAGE=`ls -l $IMAGE_DIR  | egrep -c '^-'` 
	cd $IMAGE_DIR

	# Check to see if the fetchedimage.txt file is leftover from last ingest, if so, delete it
	if [ -f $FETCHED_IMAGE_FILE ]; then
		rm -f $FETCHED_IMAGE_FILE
	fi
	# Now create a new fetchedimage.txt file
	touch $FETCHED_IMAGE_FILE

	echo -e "\n" &>> $FETCHED_IMAGE_FILE
	echo "List of UFIDs fetched from ActiveMQ" &>> $FETCHED_IMAGE_FILE
	echo "==================================================================================" &>> $FETCHED_IMAGE_FILE
	cat $FETCHED_IMAGE_FILE

	for fn in *; do
 		var3=`identify $fn | awk '{FS=" ";print $2}'`
 		img=`identify $fn | cut -f 3 -d' '`
 		WIDTH=`echo $img | cut -d'x' -f 1`
 		HEIGHT=`echo $img | cut -d'x' -f 2`

		if [ "$WIDTH" -le 200 ] && [ "$HEIGHT" -le 200 ];
		then
		        var2="sudo convert $fn -resize 200 ../fullImages/$fn.jpeg"
		else
	        	var2="cp $fn ../fullImages/$fn.jpeg"
		fi
		var1="sudo convert $fn -resize 200 ../thumbnails/thumbnail$fn.jpeg"

		echo "$fn " &>>$FETCHED_IMAGE_FILE

		$var1
		$var2
	done
	#Done for for loop

	THUMB_IMAGE_COUNT=`ls -l $IMG_HARVESTER_HOME_DIR/thumbnails  | egrep -c '^-'`

	echo "Total images fetched:  " $NEW_IMAGE 
	echo "Total images fetched:  " $NEW_IMAGE &>> $EMAIL_HARVEST_LOG
else 
	echo "Total images fetched:  0"  
	echo "Total images fetched:  0"  &>> $EMAIL_HARVEST_LOG	
fi
###END count if

echo "Then running the script"

cd $IMG_HARVESTER_HOME_DIR
echo "Running Pre Image Ingest Analytics......."

bash analytics.sh
bash run-image.sh 

#Get the number of  images addded from the addition file.
UPLOADED_IMAGE=`cat data/vivo-additions.rdf.xml | grep vivo.ufl.edu/individual | wc -l`

echo "Total images uploaded: "$UPLOADED_IMAGE
echo "Total images uploaded:  " $UPLOADED_IMAGE &>> $EMAIL_HARVEST_LOG

END=$(date +%s)
DIFF=$(( $END - $START ))
echo "Time to complete Image Ingest:  $DIFF seconds " &>> $EMAIL_HARVEST_LOG
echo "Time to complete Image Ingest:  $DIFF seconds "

echo "End Image Ingest" &>> $EMAIL_HARVEST_LOG
echo -e "\n" &>> $EMAIL_HARVEST_LOG

echo "Pre Image Ingest Analytics" &>> $EMAIL_HARVEST_LOG
echo "================================================================================="  &>> $EMAIL_HARVEST_LOG
cat analytics.txt &>> $EMAIL_HARVEST_LOG
echo -e "\n" &>> $EMAIL_HARVEST_LOG

echo "Running Post Image Ingest Analytics......."
bash analytics.sh
echo "Post Image Ingest Analytics" &>> $EMAIL_HARVEST_LOG
echo "================================================================================="  &>> $EMAIL_HARVEST_LOG
cat analytics.txt &>> $EMAIL_HARVEST_LOG
echo -e "\n" &>> $EMAIL_HARVEST_LOG


if [ "$UPLOADED_IMAGE" -ne 0  ]; then

	echo  "URL's Of Profile updates in current Imageharvest"  &>> $EMAIL_HARVEST_LOG
	echo "================================================================================="  &>> $EMAIL_HARVEST_LOG
	serverURL=`grep vivo-server-url= system.properties | cut -d'=' -f 2`

	# Check to see if the upload file already exists, and delete it if it does
	if [ -f $UPLOAD_FILE ]; then
		rm -f $UPLOAD_FILE
	fi
	# Create the upload file
	touch $UPLOAD_FILE

	# Dump the items that were added to the upload file
	grep http://vivo.ufl.edu/individual data/vivo-additions.rdf.xml | cut -d'=' -f 2 | sed 's/.\(.*\)../\1/' >>$UPLOAD_FILE

	# Check to see if the Output File exists, and if so delete it
	if [ -f $OUTPUT_FILE ]; then
		rm -f $OUTPUT_FILE
	fi

	# Create the output file
	touch $OUTPUT_FILE

	exec<$OUTPUT_FILE

	value=0
	while read line
	do
		echo $line >> $EMAIL_HARVEST_LOG;
	done
fi
#end if

echo `get_datetime` "End IMAGES Run"

#Assemble the log files to be emailed
cat $EMAIL_HARVEST_LOG > $TEMP_FILE
##echo "Full Log">>tmp.txt
cat $FETCHED_IMAGE_FILE >> $TEMP_FILE
echo -e "\n"  >> $TEMP_FILE
cat $OPENCONNECT_LOG >> $TEMP_FILE
echo -e "\n"  >> $TEMP_FILE
echo "Ending full log" >> $TEMP_FILE

################## ADD OPENCONNECT CONNECTION SCRIPT - VJS 20111216 ##################
date >>$OPENCONNECT_LOG
echo -e "\n" >>$OPENCONNECT_LOG
sudo bash /etc/init.d/openconnect stop >>$OPENCONNECT_LOG
################## ADD OPENCONNECT CONNECTION SCRIPT - VJS 20111216 ##################

#Mail the assembled log file to the desired person.
mail -a "FROM:Image_Ingest" -s "IMAGES harvest of $DATE" "$EMAIL_RECIPIENT" < $TEMP_FILE

# Backup logs and deltas into their respective directories.
mv ./logs/* $HARVESTER_LOG_DIR
mv ./data/vivo-additions.rdf.xml $HARVESTER_BACKUP_DIR/vivo-additions.$DATE.rdf.xml
#mv ./data/vivo-subtractions.rdf.xml $HARVESTER_BACKUP_DIR/vivo-subtractions.$DATE.rdf.xml

# Reformat ntriple additions / subtractions to make a logfile
sudo bash /data/vivo/manual-edits/reformat.sh image $IMG_HARVESTER_HOME_DIR

