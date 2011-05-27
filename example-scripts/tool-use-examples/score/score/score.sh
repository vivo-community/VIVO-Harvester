#!/bin/bash
source example-scripts/tool-use-examples/score/score/score.conf

HARVESTER_JAVA_OPTS="-cp lib/d2rmap-V03.jar:bin/harvester-${Version}.jar:bin/dependency/* -Dconsole-log-level=ALL"
# $HARVESTER_JAVA_OPTS

SOURCEARGS="-Itype=$Sourcetype -IdbLayout=$SourcedbLayout -IdbClass=$SourcedbClass  -IdbType=$SourcedbType -ImodelName=$SourceModelName -IdbUrl=$SourcedbUrl -IdbUser=$SourcedbUser -IdbPass=$SourcedbPass"
# $SOURCEARGS

VIVOARGS="-Vtype=$VivoType -VdbLayout=$VivodbLayout -VdbClass=$VivodbClass -VdbType=$VivodbType -VdbUrl=$VivodbUrl -VdbUser=$VivodbUser -VdbPass=$VivodbPass -Vnamespace=$VivoNamespace"
# $VIVOARGS

SCOREARGS="-Stype=$Scoretype -SdbLayout=$ScoredbLayout -SdbClass=$ScoredbClass -SdbType=$ScoredbType -SdbUrl=$ScoredbUrl -SdbUser=$ScoredbUser -SdbPass=$ScoredbPass"
#$SCOREARGS

RUN1ARGS="-A$ScoreRun1=$ScoreAlgorithm1 -W$ScoreRun1=$ScoreWeight1 -P$ScoreRun1=$ScoreVivoPredicate1 -F$ScoreRun1=$ScoreSourcePredicate1"
#$RUN1ARGS

LINEARGUMENTS="$SOURCEARGS $VIVOARGS $SCOREARGS $RUN1ARGS -t $TempCopyDirectory -b $BatchSize -n $ScoreNamespaceFiter"
#$LINEARGUMENTS
# see example-scripts/README.txt for information about HARVESTER_JAVA_OPTS

java $HARVESTER_JAVA_OPTS org.vivoweb.harvester.score.Score $LINEARGUMENTS

#$Score -i $H2MODEL -ImodelName=$MODELNAME -IdbUrl=$MODELDBURL -IcheckEmpty=$CHECKEMPTY -v $VIVOCONFIG -VcheckEmpty=$CHECKEMPTY -s $H2MODEL -SmodelName=$SCOREDATANAME -SdbUrl=$SCOREDATADBURL -ScheckEmpty=$CHECKEMPTY -t $BASEDIR/temp-copy -b $SCOREBATCHSIZE -A label=$EQTEST -W label=1.0 -F label=$RDFSLABEL -P label=$RDFSLABEL -n ${BASEURI}sponsor/

