export HARVESTER_INSTALL_DIR=/usr/local/src/VIVO-Harvester
export HARVESTER_JAVA_OPTS="-Xms1024M -Xmx1024M"
export CLASSPATH=$CLASSPATH:$HARVESTER_INSTALL_DIR/build/harvester.jar:$HARVESTER_INSTALL_DIR/build/dependency/*

java $HARVESTER_JAVA_OPTS -cp $CLASSPATH org.vivoweb.harvester.util.repo.JenaConnect -j harvested-data.model.xml -q "SELECT ?subj ?pred ?obj  where { ?subj ?pred ?obj }"
