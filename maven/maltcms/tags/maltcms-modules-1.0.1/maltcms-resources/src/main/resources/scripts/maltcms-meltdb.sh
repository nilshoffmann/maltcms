#!/bin/bash
export EXEC="net.sf.maltcms.apps.Maltcms";
LOG4J_LOCATION="-Dlog4j.configuration=file:///vol/meltdb/share/java/maltcms/cfg/log4j.properties";
DEFAULT_CONFIG="-DmaltcmsDefaultConfiguration=/vol/meltdb/share/java/maltcms/cfg/default.properties";
export JAVA_HOME="$(cat /vol/meltdb/share/java/maltcms/javahome)";
export USRCLSPATH="$CLASSPATH:$(cat /vol/meltdb/share/java/maltcms/clspath)";
$JAVA_HOME/bin/java -d64 -cp $USRCLSPATH $PROFILE -Xmx2G -Xms2G $LOG4J_LOCATION $DEFAULT_CONFIG -server $EXEC $@ 
exit $?
