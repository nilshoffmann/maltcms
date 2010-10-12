#!/bin/bash
export JARCH=""
export EXEC="apps.Maltcms"
export MXSIZE="2G"
export MALTCMSARGS=""
LOG4J_LOCATION="-Dlog4j.configuration=cfg/log4j.properties"

export JAVA_HOME="$(cat javahome)"
echo -e "Using the following java location: $JAVA_HOME\n"

function printHelp {
			echo -e "Usage: $0 [-cp] [-64] [-mx ARG] [-exec ARG|--execute ARG] [-?|--help] [-- MALTCMSARGS]"
			echo -e "\t-64 -> uses 64bit jvm"
			echo -e "\t-mx ARG -> uses -XmxARG as maximum heapsize"
			echo -e "\t-exec ARG|--execute ARG -> execute the given base class,"
		       	echo -e "\t\t\tif it contains a main method"
			echo -e "\t\t\te.g. '-exec mypackage/MyClass'"
			echo -e "\t-?|--help -> display this help"
			echo -e "\t-- MALTCMSARGS -> hands all arguments following '--' over to Maltcms"
			exit -1
}

if [ $# -eq 0 ]; then
	printHelp $1
fi

while [ $# -gt 0 ]; do
	case "$1" in
		-64)
			export JARCH="-d64"
			;;
		-exec|--execute)
			shift
			export EXEC="$1"
			;;	
		-mx)	
			shift
			export MXSIZE="$1"
			;;
		--)
			echo "Running a $JARCH VM with -Xmx $MXSIZE"
			shift
			export CLASSPATH="$(cat clspath)"
			echo -e "Passing args to $EXEC"
			echo -e "$@"
			sleep 1
			$JAVA_HOME/bin/java -cp $CLASSPATH -Xmx$MXSIZE -Xms$MXSIZE $JARCH $LOG4J_LOCATION $EXEC $@ 
			exit $?
			;;	
		-"?"|--help)
			printHelp $0
			;;

	esac
	shift
done

exit 1


